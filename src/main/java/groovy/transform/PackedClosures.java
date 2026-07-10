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
package groovy.transform;

import org.apache.groovy.lang.annotation.Incubating;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Opt-in for compact closure compilation: within the annotated class or method, an eligible closure
 * literal's body is hoisted into a synthetic method on the enclosing class and the literal is replaced
 * by a shared {@link org.codehaus.groovy.runtime.PackedClosure} adapter, instead of generating one
 * inner class per closure. The value stays a real {@code groovy.lang.Closure}, so {@code curry},
 * {@code memoize}, {@code trampoline} and iteration keep working; captured values are threaded by
 * value (read-only) or via a shared {@code groovy.lang.Reference} (when written), so a packed closure
 * behaves identically to the class-based form.
 * <p>
 * Packing is best-effort: a closure that cannot be packed is compiled exactly as today (a generated
 * closure class), so the annotation is always safe to add. A closure is kept as a class when it:
 * <ul>
 *   <li>references {@code owner}, {@code delegate}, {@code thisObject}, {@code resolveStrategy},
 *       {@code directive}, {@code metaClass} or {@code super} — it needs a real {@code Closure};</li>
 *   <li>has default parameter values, or contains an anonymous inner class;</li>
 *   <li>is nested inside another closure, or visibly escapes its method (returned, stored to a
 *       field/property/index, appended, or placed in a collection literal);</li>
 *   <li>under {@code @CompileStatic}, resolves a name against a delegate (e.g. via {@code @DelegatesTo}
 *       or {@code with}) — packing is otherwise proven sound from the type checker's resolution.</li>
 * </ul>
 * Under dynamic compilation the annotation is a trust assertion the compiler cannot verify; the
 * {@code PackedClosure} adapter then fails fast if a delegate is later set on a packed closure.
 * Automatic packing of provably-safe {@code @CompileStatic} closures — without this annotation — is
 * available behind the {@code groovy.target.closure.pack} flag. Use {@link #mode()} to have declines
 * reported (or to opt a scope out).
 *
 * @see PackMode
 */
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface PackedClosures {
    /**
     * The packing behaviour of this scope: {@link PackMode#LENIENT} (pack, silent on declines, the
     * default), {@link PackMode#WARN} (pack, a compiler warning per decline with the reason),
     * {@link PackMode#STRICT} (pack, a compiler error on any decline), or {@link PackMode#DISABLED}
     * (do not pack). The most-specific declaration wins, so a {@code DISABLED} method opts out of a
     * packed class (and {@code DISABLED} also overrides the automatic {@code groovy.target.closure.pack}
     * flag); the WARN/STRICT diagnostics apply only to the annotated scope, never to the flag.
     */
    PackMode mode() default PackMode.LENIENT;

    /**
     * The packing behaviour of a {@link PackedClosures} scope. {@link #LENIENT}, {@link #WARN} and
     * {@link #STRICT} all pack, differing only in how they report a closure that could <em>not</em> be
     * packed (declines are otherwise silent). {@link #DISABLED} is the opposite: it opts the scope out
     * of packing entirely, so the most-specific declaration wins — a {@code DISABLED} method inside a
     * packed class, or vice versa, and it overrides the automatic {@code groovy.target.closure.pack}
     * flag as well.
     */
    @Incubating
    enum PackMode {

        /** Do not pack any closure in this scope; overrides an enclosing opt-in and the automatic flag. */
        DISABLED,

        /** Silently fall back to a generated closure class for any closure that cannot be packed (default). */
        LENIENT,

        /** Emit a compiler warning, naming the closure and why it declined, for each that cannot be packed. */
        WARN,

        /** Emit a compiler error for any closure in the scope that cannot be packed. */
        STRICT
    }
}
