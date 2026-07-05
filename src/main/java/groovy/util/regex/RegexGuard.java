/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util.regex;

import groovy.lang.Closure;
import org.apache.groovy.lang.annotation.Incubating;
import org.apache.groovy.runtime.async.ScopedLocal;
import org.codehaus.groovy.runtime.FormatHelper;
import org.codehaus.groovy.runtime.RegexSupport;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Deadline-guarded regular expression evaluation, protecting against
 * Regular Expression Denial of Service (ReDoS) when patterns or inputs
 * come from untrusted sources. Java's regex engine has no native timeout,
 * so catastrophic backtracking can hang a thread indefinitely.
 * <p>
 * The guard works by wrapping the input {@code CharSequence} so that
 * {@code charAt} periodically checks a deadline and throws
 * {@link RegexTimeoutException} once it has passed. Backtracking repeatedly
 * re-reads input characters, so a runaway match is cut off shortly after the
 * deadline without needing watchdog threads or thread interruption.
 * <pre class="groovyTestCase">
 * import groovy.util.regex.RegexGuard
 * import groovy.util.regex.RegexTimeoutException
 * import java.time.Duration
 *
 * assert RegexGuard.matches(/gro+vy/, 'groovy', 200)
 * def m = RegexGuard.matcher(/(\d+)/, 'abc 123', Duration.ofMillis(200))
 * assert m.find() && m.group(1) == '123'
 *
 * try {
 *     RegexGuard.matches(/(.*,){16}X/, '1,' * 40, 200) // catastrophic backtracking
 *     assert false, 'should have timed out'
 * } catch (RegexTimeoutException expected) {
 * }
 * </pre>
 * Notes and limitations:
 * <ul>
 * <li>The deadline starts when the guarded matcher is created and covers all
 * subsequent use of it, e.g. repeated {@code find()} calls.</li>
 * <li>The clock is only consulted while the engine reads input characters,
 * every 512 reads; evaluations finishing in fewer reads
 * never pay for a clock call. Pathological patterns perform millions of reads
 * per second, so overshoot past the deadline is negligible in practice.</li>
 * <li>{@code Pattern.compile} itself is not guarded; pattern compilation is
 * not subject to backtracking.</li>
 * </ul>
 *
 * @see groovy.transform.SafeRegex
 * @since 6.0.0
 */
@Incubating
public final class RegexGuard {

    private RegexGuard() {
    }

    /**
     * Matches the whole input against the pattern like the {@code ==~} operator,
     * giving up once the timeout has elapsed. Follows the operator's conventions:
     * a {@code null} pattern or input yields {@code false}, non-{@code Pattern}
     * patterns and non-{@code String} inputs are converted via their default
     * string representation, and the matcher is stored for
     * {@code Matcher.lastMatcher}.
     *
     * @param pattern the pattern, a {@link Pattern} or an object whose string representation is the regex
     * @param input   the text to match
     * @param millis  the timeout in milliseconds (must be positive)
     * @return {@code true} if the whole input matches the pattern
     * @throws RegexTimeoutException if evaluation exceeds the timeout
     */
    public static boolean matches(Object pattern, Object input, long millis) {
        return doMatches(pattern, input, toDeadlineNanos(millis), millis + " ms");
    }

    /**
     * Variant of {@link #matches(Object, Object, long)} taking the timeout as a {@link Duration}.
     */
    public static boolean matches(Object pattern, Object input, Duration timeout) {
        return doMatches(pattern, input, toDeadlineNanos(timeout), timeout.toString());
    }

    /**
     * Creates a matcher of the pattern over deadline-guarded input, like the
     * {@code =~} operator. Matcher operations that read the input, e.g.
     * {@code find()} or {@code matches()}, throw {@link RegexTimeoutException}
     * once the deadline, measured from this call, has passed.
     *
     * @param pattern the pattern, a {@link Pattern} or an object whose string representation is the regex
     * @param input   the text to match
     * @param millis  the timeout in milliseconds (must be positive)
     * @return a matcher over the guarded input
     */
    public static Matcher matcher(Object pattern, Object input, long millis) {
        return doMatcher(pattern, input, toDeadlineNanos(millis), millis + " ms");
    }

    /**
     * Variant of {@link #matcher(Object, Object, long)} taking the timeout as a {@link Duration}.
     */
    public static Matcher matcher(Object pattern, Object input, Duration timeout) {
        return doMatcher(pattern, input, toDeadlineNanos(timeout), timeout.toString());
    }

    /**
     * Runtime hook used by the {@link groovy.transform.SafeRegex} transform:
     * equivalent to {@link #matches(Object, Object, long)} but with the operands
     * in the {@code ==~} operator's order, so that a rewritten expression still
     * evaluates its operands left to right, exactly as the original did.
     *
     * @param input   the text to match (the operator's left operand)
     * @param pattern the pattern (the operator's right operand)
     * @param millis  the timeout in milliseconds (must be positive)
     * @return {@code true} if the whole input matches the pattern
     * @throws RegexTimeoutException if evaluation exceeds the timeout
     */
    public static boolean matchRegex(Object input, Object pattern, long millis) {
        return matches(pattern, input, millis);
    }

    /**
     * Runtime hook used by the {@link groovy.transform.SafeRegex} transform:
     * equivalent to {@link #matcher(Object, Object, long)} but with the operands
     * in the {@code =~} operator's order, so that a rewritten expression still
     * evaluates its operands left to right, exactly as the original did.
     *
     * @param input   the text to match (the operator's left operand)
     * @param pattern the pattern (the operator's right operand)
     * @param millis  the timeout in milliseconds (must be positive)
     * @return a matcher over the guarded input
     */
    public static Matcher findRegex(Object input, Object pattern, long millis) {
        return matcher(pattern, input, millis);
    }

    /**
     * Wraps a character sequence so that reads beyond the deadline throw
     * {@link RegexTimeoutException}. The deadline is attached to the data: it is
     * a single total budget covering every subsequent regex evaluation against
     * the guarded sequence, wherever and on whatever thread it happens. Groovy's
     * regex-evaluating {@code CharSequence} extension methods ({@code find},
     * {@code findAll}, {@code findGroups}, {@code eachMatch}, {@code replaceAll},
     * {@code switch/case} matching, ...) honour the guard, as does anything that
     * passes the sequence through to {@code java.util.regex} untouched, e.g.
     * {@code Pattern.split} or manual matcher creation. This complements
     * {@link #guard(long, Closure)}, which instead guards a region of code with
     * a per-evaluation timeout.
     * <p>
     * Note the deadline is only consulted while the regex engine reads input
     * characters, so a well-behaved evaluation over a short input may still
     * succeed after the deadline; runaway evaluations are always cut off.
     * A sequence other than a {@code String} (e.g. a {@code GString} or
     * {@code StringBuilder}) is snapshotted via {@code toString()} first, so
     * later mutations of the original are not seen by the guarded view.
     *
     * @param input  the sequence to guard (must not be {@code null})
     * @param millis the timeout in milliseconds (must be positive)
     * @return a guarded view of the input
     */
    public static CharSequence guard(CharSequence input, long millis) {
        return guarded(snapshot(input), toDeadlineNanos(millis), millis + " ms");
    }

    /**
     * Variant of {@link #guard(CharSequence, long)} taking the timeout as a {@link Duration}.
     */
    public static CharSequence guard(CharSequence input, Duration timeout) {
        return guarded(snapshot(input), toDeadlineNanos(timeout), timeout.toString());
    }

    /**
     * Runs the closure with an ambient regex timeout active on the current thread
     * and returns the closure's result. While active, every regex evaluation
     * reached through Groovy's regex operators ({@code ==~}, {@code =~}, and
     * {@code switch/case}, {@code grep} and {@code in} against a {@code Pattern})
     * or the {@code CharSequence} extension methods ({@code find}, {@code findAll},
     * {@code findGroups}, {@code findAllGroups}, {@code eachMatch}, {@code matches},
     * {@code replaceAll}, {@code replaceFirst}, {@code splitEachLine}, ...) is
     * guarded: each individual evaluation that exceeds the timeout throws
     * {@link RegexTimeoutException}.
     * <pre class="groovyTestCase">
     * import groovy.util.regex.RegexGuard
     * import groovy.util.regex.RegexTimeoutException
     *
     * def groups = RegexGuard.guard(200) {
     *     '6.0.0-beta-2'.findGroups(/(\d+)\.(\d+)\.(\d+)(?:-(.+))?/)
     * }
     * assert groups == ['6.0.0-beta-2', '6', '0', '0', 'beta-2']
     *
     * try {
     *     RegexGuard.guard(200) {
     *         ('1,' * 40).findGroups(/(.*,){16}X/) // catastrophic backtracking
     *     }
     *     assert false, 'should have timed out'
     * } catch (RegexTimeoutException expected) {
     * }
     * </pre>
     * Semantics:
     * <ul>
     * <li>The timeout applies <em>per evaluation</em>, not to the block as a whole;
     * any number of well-behaved evaluations may run within the scope.</li>
     * <li>The scope is dynamic: regex operations in methods called from the closure
     * are guarded too, as long as they run on the same thread. Threads spawned
     * within the closure are not covered; on JDK 25+, however, the guard does
     * propagate into {@code StructuredTaskScope} forks, which by construction run
     * within the enclosing scope.</li>
     * <li>Nested guards only tighten: the smaller per-evaluation timeout wins.</li>
     * <li>An explicit timeout, e.g. from {@link #matches(Object, Object, long)} or a
     * {@link groovy.transform.SafeRegex} scope, is likewise capped by the ambient timeout.</li>
     * <li>A matcher created within the scope (e.g. via {@code =~}) keeps its deadline,
     * measured from creation, when used after the block exits.</li>
     * <li>Direct {@code java.util.regex} calls, e.g. a hand-written
     * {@code pattern.matcher(input)}, bypass the ambient guard; combine them with
     * {@link #guard(CharSequence, long)} instead. The same applies to the JDK's own
     * {@code String} methods: on a {@code String} receiver, {@code matches(String)},
     * {@code replaceAll(String, String)}, {@code replaceFirst(String, String)} and
     * {@code split(String)} dispatch to {@code java.lang.String} and never enter
     * Groovy's runtime. Their {@code Pattern}-argument variants, e.g.
     * {@code str.replaceAll(~/expensive/, 'x')}, are extension methods and are covered.</li>
     * </ul>
     *
     * @param millis  the per-evaluation timeout in milliseconds (must be positive)
     * @param closure the code to run under the ambient guard
     * @return the closure's result
     */
    public static <T> T guard(long millis, Closure<T> closure) {
        return withAmbient(toTimeoutNanos(millis), millis + " ms", closure);
    }

    /**
     * Variant of {@link #guard(long, Closure)} taking the timeout as a {@link Duration}.
     */
    public static <T> T guard(Duration timeout, Closure<T> closure) {
        return withAmbient(toTimeoutNanos(timeout), timeout.toString(), closure);
    }

    /**
     * Runtime hook used by Groovy's regex operators and extension methods:
     * wraps the input so that reads are guarded when an ambient guard
     * (see {@link #guard(long, Closure)}) is active on the current thread,
     * and returns the input unchanged otherwise.
     *
     * @param input the text a regex is about to be evaluated against
     * @return the input, guarded if an ambient guard is active
     */
    public static CharSequence ambientGuard(CharSequence input) {
        if (!ambientEverUsed || input == null) return input;
        Ambient ambient = AMBIENT.orElse(null);
        if (ambient == null) return input;
        return new GuardedCharSequence(input, System.nanoTime() + ambient.timeoutNanos, ambient.timeoutText);
    }

    /**
     * Runtime hook used by Groovy's regex extension methods: normalizes an input
     * for regex evaluation. A {@code String}, or a sequence guarded via
     * {@link #guard(CharSequence, long)}, is passed through, so an attached
     * deadline is honoured by the evaluation; any other sequence (e.g. a
     * {@code GString} or {@code StringBuilder}) is snapshotted via
     * {@code toString()}, as the extension methods have always done. An ambient
     * guard (see {@link #guard(long, Closure)}), if active, is then applied on top.
     *
     * @param input the text a regex is about to be evaluated against
     * @return the normalized, possibly guarded, input
     */
    public static CharSequence prepare(CharSequence input) {
        if (input == null) return null;
        return ambientGuard(snapshot(input));
    }

    private static CharSequence snapshot(CharSequence input) {
        if (input == null) throw new IllegalArgumentException("input must not be null");
        // snapshot sequences with costly charAt implementations (e.g. GString),
        // as the regex extension methods have always done
        return (input instanceof String || input instanceof GuardedCharSequence) ? input : input.toString();
    }

    private static boolean doMatches(Object pattern, Object input, long deadline, String timeoutText) {
        if (pattern == null || input == null) return false;
        Matcher matcher = toPattern(pattern).matcher(guarded(toCharSequence(input), deadline, timeoutText));
        RegexSupport.setLastMatcher(matcher);
        return matcher.matches();
    }

    private static Matcher doMatcher(Object pattern, Object input, long deadline, String timeoutText) {
        return toPattern(pattern).matcher(guarded(toCharSequence(input), deadline, timeoutText));
    }

    private static GuardedCharSequence guarded(CharSequence input, long deadline, String timeoutText) {
        if (ambientEverUsed) {
            Ambient ambient = AMBIENT.orElse(null);
            if (ambient != null) {
                long ambientDeadline = System.nanoTime() + ambient.timeoutNanos;
                if (ambientDeadline - deadline < 0) {
                    deadline = ambientDeadline;
                    timeoutText = ambient.timeoutText;
                }
            }
        }
        return new GuardedCharSequence(input, deadline, timeoutText);
    }

    private static <T> T withAmbient(long timeoutNanos, String timeoutText, Closure<T> closure) {
        ambientEverUsed = true; // set before the binding, so this thread always sees it
        Ambient previous = AMBIENT.orElse(null);
        // nested guards never extend an enclosing guard: the tighter per-evaluation timeout wins
        Ambient effective = (previous != null && previous.timeoutNanos <= timeoutNanos)
                ? previous : new Ambient(timeoutNanos, timeoutText);
        // the binding unwinds with the scope, so the guard cannot outlive the closure
        return ScopedLocal.where(AMBIENT, effective).call(closure::call);
    }

    private static long toDeadlineNanos(long millis) {
        return System.nanoTime() + toTimeoutNanos(millis);
    }

    private static long toDeadlineNanos(Duration timeout) {
        return System.nanoTime() + toTimeoutNanos(timeout);
    }

    private static long toTimeoutNanos(long millis) {
        if (millis <= 0) throw new IllegalArgumentException("timeout must be positive but was " + millis);
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }

    private static long toTimeoutNanos(Duration timeout) {
        if (timeout == null || timeout.isZero() || timeout.isNegative())
            throw new IllegalArgumentException("timeout must be positive but was " + timeout);
        try {
            return timeout.toNanos();
        } catch (ArithmeticException e) {
            // a Duration can span more than Long.MAX_VALUE nanoseconds, unlike a millis
            // timeout, which TimeUnit saturates; report it as an invalid argument either way
            throw new IllegalArgumentException("timeout is too large to express in nanoseconds: " + timeout, e);
        }
    }

    private static final ScopedLocal<Ambient> AMBIENT = ScopedLocal.newInstance();

    // Fast path for the common case of a process that never installs an ambient guard: until
    // one is, the scoped-local lookup on every regex evaluation is skipped in favour of a
    // cached read, which on the ThreadLocal backend also avoids materializing a per-thread
    // map entry. Only ever set, never cleared, so there is no race to unset it; a thread
    // reading a stale false holds no binding of its own either, so it takes the same branch.
    private static volatile boolean ambientEverUsed;

    private static final class Ambient {
        private final long timeoutNanos;
        private final String timeoutText;

        private Ambient(long timeoutNanos, String timeoutText) {
            this.timeoutNanos = timeoutNanos;
            this.timeoutText = timeoutText;
        }
    }

    private static Pattern toPattern(Object pattern) {
        if (pattern instanceof Pattern p) return p;
        return Pattern.compile(pattern instanceof String s ? s : FormatHelper.toString(pattern));
    }

    private static CharSequence toCharSequence(Object input) {
        // convert like the regex operators do; also avoids guarding sequences
        // with costly charAt implementations (e.g. GString)
        return input instanceof String s ? s : FormatHelper.toString(input);
    }

    private static final int CHECK_INTERVAL = 512;

    private static final class GuardedCharSequence implements CharSequence {
        private final CharSequence delegate;
        private final long deadline;
        private final String timeoutText;
        private int reads; // single matching thread; racy updates only affect check cadence

        private GuardedCharSequence(CharSequence delegate, long deadline, String timeoutText) {
            this.delegate = delegate;
            this.deadline = deadline;
            this.timeoutText = timeoutText;
        }

        @Override
        public char charAt(int index) {
            if ((++reads & (CHECK_INTERVAL - 1)) == 0 && System.nanoTime() - deadline > 0) {
                throw new RegexTimeoutException("regex evaluation exceeded timeout of " + timeoutText);
            }
            return delegate.charAt(index);
        }

        @Override
        public int length() {
            return delegate.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new GuardedCharSequence(delegate.subSequence(start, end), deadline, timeoutText);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }
    }
}
