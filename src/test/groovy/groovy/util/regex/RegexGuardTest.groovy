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
package groovy.util.regex

import groovy.test.GroovyTestCase

import java.time.Duration
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Tests for the {@link RegexGuard} runtime helper.
 */
class RegexGuardTest extends GroovyTestCase {

    // catastrophic backtracking far beyond any test timeout; note that modern JDKs
    // memoize the classic (a+)+ style exponential patterns, but combinatorial
    // patterns like this one still explode
    private static final String EVIL_PATTERN = /(.*,){16}X/
    private static final String EVIL_INPUT = '1,' * 40

    void testMatchesForFriendlyPattern() {
        assert RegexGuard.matches(/gro+vy/, 'groovy', 1000)
        assert !RegexGuard.matches(/gro+vy/, 'java', 1000)
        assert RegexGuard.matches(Pattern.compile('gro+vy'), 'grooovy', 1000)
        assert RegexGuard.matches(/gro+vy/, 'groovy', Duration.ofSeconds(1))
    }

    void testMatchesReturnsFalseForNullOperands() {
        assert !RegexGuard.matches(null, 'groovy', 1000)
        assert !RegexGuard.matches(/gro+vy/, null, 1000)
    }

    void testMatchesSetsLastMatcherLikeMatchOperator() {
        assert RegexGuard.matches(/f(o+)/, 'foo', 1000)
        assert Matcher.lastMatcher.group(1) == 'oo'
    }

    void testMatchesTimesOutForCatastrophicPattern() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.matches(EVIL_PATTERN, EVIL_INPUT, 100)
        }
        shouldFail(RegexTimeoutException) {
            RegexGuard.matches(EVIL_PATTERN, EVIL_INPUT, Duration.ofMillis(100))
        }
    }

    void testMatcherForFriendlyPattern() {
        def m = RegexGuard.matcher(/(\d+)/, 'abc 123 def 45', 1000)
        assert m.find() && m.group(1) == '123'
        assert m.find() && m.group(1) == '45'
        assert !m.find()
    }

    void testMatcherTimesOutForCatastrophicPattern() {
        def m = RegexGuard.matcher(EVIL_PATTERN, EVIL_INPUT, Duration.ofMillis(100))
        shouldFail(RegexTimeoutException) {
            m.find()
        }
    }

    void testOperatorOrderEntryPointsMirrorMatchesAndMatcher() {
        assert RegexGuard.matchRegex('groovy', /gro+vy/, 1000)
        assert !RegexGuard.matchRegex('java', /gro+vy/, 1000)
        def m = RegexGuard.findRegex('abc 123', /(\d+)/, 1000)
        assert m.find() && m.group(1) == '123'
        shouldFail(RegexTimeoutException) {
            RegexGuard.matchRegex(EVIL_INPUT, EVIL_PATTERN, 100)
        }
    }

    void testGuardedSequencePreservesContent() {
        def guarded = RegexGuard.guard('groovy rocks', 1000)
        assert guarded.length() == 12
        assert guarded.charAt(0) == 'g'
        assert guarded.toString() == 'groovy rocks'
        assert guarded.subSequence(7, 12).toString() == 'rocks'
    }

    void testGuardWorksWithOtherRegexApis() {
        shouldFail(RegexTimeoutException) {
            Pattern.compile(EVIL_PATTERN).split(RegexGuard.guard(EVIL_INPUT, 100))
        }
    }

    void testGuardRejectsNullInput() {
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard((CharSequence) null, 100)
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard((CharSequence) null, Duration.ofMillis(100))
        }
    }

    void testGuardSnapshotsNonStringSequences() {
        def sb = new StringBuilder('123')
        def guarded = RegexGuard.guard("abc ${sb}", 10_000)
        sb.append('456')   // a later mutation must not be seen by the guarded view
        assert guarded.toString() == 'abc 123'
        assert guarded.findGroups(/(\d+)/) == ['123', '123']
    }

    void testVeryLargeTimeoutMeansEffectivelyNoTimeout() {
        // deadlines are only ever compared as nanoTime() differences, which stays correct
        // across wrap-around, so a timeout large enough to overflow the deadline must read
        // as "effectively no timeout" rather than expiring instantly
        long huge = Long.MAX_VALUE.intdiv(1_000_000)   // milliseconds, ~Long.MAX_VALUE nanos
        assert RegexGuard.matches(/gro+vy/, 'groovy', huge)
        assert RegexGuard.matcher(/(\d+)/, 'abc 123', huge).find()
        assert RegexGuard.guard('abc 123', huge).findAll(/\d/) == ['1', '2', '3']
        assert RegexGuard.guard(huge) { 'groovy' ==~ /gro+vy/ }

        // TimeUnit saturates rather than wrapping, so the extreme is well defined too
        assert RegexGuard.matches(/gro+vy/, 'groovy', Long.MAX_VALUE)
        assert RegexGuard.matches(/gro+vy/, 'groovy', Duration.ofNanos(Long.MAX_VALUE))
    }

    void testOutOfRangeDurationRejected() {
        // a Duration can express more than Long.MAX_VALUE nanoseconds; that surfaces as
        // IllegalArgumentException like any other bad timeout, not as ArithmeticException
        def tooLong = Duration.ofDays(1_000_000)
        assert shouldFail(IllegalArgumentException) {
            RegexGuard.matches(/x/, 'x', tooLong)
        }.contains('too large')
        shouldFail(IllegalArgumentException) {
            RegexGuard.matcher(/x/, 'x', tooLong)
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard('x', tooLong)
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard(tooLong) { }
        }
    }

    void testNonPositiveTimeoutRejected() {
        shouldFail(IllegalArgumentException) {
            RegexGuard.matches(/x/, 'x', 0)
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.matcher(/x/, 'x', Duration.ofMillis(-1))
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard('x', -5)
        }
        shouldFail(IllegalArgumentException) {
            RegexGuard.guard(0) { }
        }
    }

    void testClosureGuardReturnsClosureResult() {
        assert RegexGuard.guard(1000) { 6 * 7 } == 42
        assert RegexGuard.guard(Duration.ofSeconds(1)) { 'ok' } == 'ok'
    }

    void testClosureGuardLeavesFriendlyRegexesAlone() {
        RegexGuard.guard(1000) {
            assert 'groovy' ==~ /gro+vy/
            assert '6.0.0-beta-2'.findGroups(/(\d+)\.(\d+)\.(\d+)(?:-(.+))?/) ==
                    ['6.0.0-beta-2', '6', '0', '0', 'beta-2']
            assert 'hello world'.replaceAll(~/o/, '0') == 'hell0 w0rld'
        }
    }

    void testClosureGuardCoversFindGroups() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                EVIL_INPUT.findGroups(EVIL_PATTERN)
            }
        }
    }

    void testClosureGuardCoversEachMatch() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                EVIL_INPUT.eachMatch(EVIL_PATTERN) { }
            }
        }
    }

    void testClosureGuardCoversPatternReplaceAll() {
        // NOTE: deliberately the Pattern-argument variant. With two String arguments on a
        // String receiver, replaceAll dispatches to the JDK's own String#replaceAll, which
        // never enters Groovy's runtime and so cannot be covered by the ambient guard.
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                EVIL_INPUT.replaceAll(Pattern.compile(EVIL_PATTERN), 'x')
            }
        }
    }

    void testClosureGuardCoversStringRegexReplaceAllOnNonStringReceiver() {
        // a non-String CharSequence receiver does route through Groovy's extension method
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                new StringBuilder(EVIL_INPUT).replaceAll(EVIL_PATTERN, 'x')
            }
        }
    }

    void testClosureGuardCoversMatchOperator() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                EVIL_INPUT ==~ EVIL_PATTERN
            }
        }
    }

    void testClosureGuardCoversFindOperator() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                def m = EVIL_INPUT =~ EVIL_PATTERN
                m.find()
            }
        }
    }

    void testClosureGuardCoversSwitchCase() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                switch (EVIL_INPUT) {
                    case Pattern.compile(EVIL_PATTERN): return 'matched'
                    default: return 'unmatched'
                }
            }
        }
    }

    void testClosureGuardHasDynamicScope() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                evilHelper()
            }
        }
    }

    private static boolean evilHelper() {
        EVIL_INPUT ==~ EVIL_PATTERN
    }

    void testClosureGuardIsClearedAfterBlockAndOnException() {
        assert RegexGuard.ambientGuard('abc') === 'abc'   // pass-through when inactive
        RegexGuard.guard(1000) {
            assert !(RegexGuard.ambientGuard('abc') instanceof String)  // wrapped when active
        }
        assert RegexGuard.ambientGuard('abc') === 'abc'

        shouldFail(IllegalStateException) {
            RegexGuard.guard(1000) { throw new IllegalStateException('boom') }
        }
        assert RegexGuard.ambientGuard('abc') === 'abc'   // restored despite exception
    }

    void testClosureGuardAppliesOnAnyThread() {
        // the fast path that skips the thread-local lookup keys off a process-wide flag,
        // so a thread installing its own guard must be protected no matter which thread
        // first used one; join() gives the happens-before for reading the result
        def caught = []
        def t = Thread.start {
            try {
                RegexGuard.guard(100) { EVIL_INPUT ==~ EVIL_PATTERN }
            } catch (RegexTimeoutException expected) {
                caught << expected
            }
        }
        t.join(30_000)
        assert caught.size() == 1
    }

    void testNestedClosureGuardsOnlyTighten() {
        long start = System.currentTimeMillis()
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(60_000) {
                RegexGuard.guard(100) {
                    EVIL_INPUT ==~ EVIL_PATTERN
                }
            }
        }
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                RegexGuard.guard(60_000) {          // cannot loosen the enclosing guard
                    EVIL_INPUT ==~ EVIL_PATTERN
                }
            }
        }
        assert System.currentTimeMillis() - start < 30_000
    }

    void testInputGuardComposesWithExtensionMethods() {
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(EVIL_INPUT, 100).findGroups(Pattern.compile(EVIL_PATTERN))
        }
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(EVIL_INPUT, 100).findAll(Pattern.compile(EVIL_PATTERN))
        }
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(EVIL_INPUT, 100).eachMatch(EVIL_PATTERN) { }
        }
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(EVIL_INPUT, 100).replaceAll(Pattern.compile(EVIL_PATTERN)) { it }
        }
        shouldFail(RegexTimeoutException) {
            switch (RegexGuard.guard(EVIL_INPUT, 100)) {
                case Pattern.compile(EVIL_PATTERN): return 'matched'
                default: return 'unmatched'
            }
        }
    }

    void testInputGuardStillAllowsFriendlyExtensionMethods() {
        def guarded = RegexGuard.guard('abc 123 def', 10_000)
        assert guarded.findGroups(/(\d+)/) == ['123', '123']
        assert guarded.findAll(~/\w+/) == ['abc', '123', 'def']
        assert guarded.replaceAll(~/\d/, 'x') == 'abc xxx def'
    }

    void testNonStringSequencesAreStillSnapshotted() {
        assert new StringBuilder('abc 123').findGroups(/(\d+)/) == ['123', '123']
        def n = 123
        assert "abc ${n}".findGroups(/(\d+)/) == ['123', '123']
        assert new StringBuilder('hello').replaceAll(~/l/) { 'L' } == 'heLLo'
    }

    void testExplicitTimeoutCappedByAmbientGuard() {
        long start = System.currentTimeMillis()
        shouldFail(RegexTimeoutException) {
            RegexGuard.guard(100) {
                RegexGuard.matches(EVIL_PATTERN, EVIL_INPUT, 60_000)
            }
        }
        assert System.currentTimeMillis() - start < 30_000
    }
}
