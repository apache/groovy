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
package org.apache.groovy.contracts.tests.other

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for {@code @ThrowsIf}, the exceptional contract: the method throws the
 * given exception when the given condition holds. The default {@code woven = true}
 * generates the guard-throw at method entry; unwoven arms generate nothing —
 * {@code direct} says where the existing throw lives (body vs invoked code),
 * pure information for readers and tools.
 */
class ThrowsIfTests extends BaseTestClass {

    @Test
    void wovenGuardThrowsWhenConditionHolds() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                @ThrowsIf(value = { b == 0 }, exception = ArithmeticException)
                static int divide(int a, int b) { a.intdiv(b) }
            }
            assert C.divide(4, 2) == 2
            try {
                C.divide(1, 0)
                assert false, 'expected the woven guard to throw'
            } catch (ArithmeticException e) {
                assert e.message.contains('b == 0')
            }
        '''
    }

    @Test
    void wovenGuardWorksUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import groovy.transform.CompileStatic

            @CompileStatic
            class C {
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException)
                static int half(int n) { n.intdiv(2) }
            }
            assert C.half(8) == 4
            try {
                C.half(-2)
                assert false, 'expected the woven guard to throw'
            } catch (IllegalArgumentException expected) {
            }
        '''
    }

    @Test
    void unwovenArmGeneratesNothing() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                // woven = false (direct defaults true): the body is expected to implement the throw; here it
                // deliberately does not, demonstrating that no guard was generated.
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false)
                static int identity(int n) { n }
            }
            assert C.identity(-5) == -5
        '''
    }

    @Test
    void trustedArmGeneratesNothing() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                // direct = false: the throw would originate in invoked code; never woven.
                @ThrowsIf(value = { s == null }, exception = NullPointerException, woven = false, direct = false)
                static Object passThrough(Object s) { s }
            }
            assert C.passThrough(null) == null
        '''
    }

    @Test
    void repeatedArmsEachGuardIndependently() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                @ThrowsIf(value = { x == null }, exception = NullPointerException)
                @ThrowsIf(value = { y < 0 }, exception = IllegalArgumentException)
                static String tag(Object x, int y) { "$x:$y" }
            }
            assert C.tag('a', 1) == 'a:1'
            try {
                C.tag(null, 1)
                assert false
            } catch (NullPointerException expected) {
            }
            try {
                C.tag('a', -1)
                assert false
            } catch (IllegalArgumentException expected) {
            }
        '''
    }

    @Test
    void mixedWovenAndUnwovenArms() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                @ThrowsIf(value = { x == null }, exception = NullPointerException)                        // woven
                @ThrowsIf(value = { y == null }, exception = NullPointerException, woven = false)   // body's job (deliberately absent here)
                static String pair(Object x, Object y) { "$x:$y" }
            }
            assert C.pair('a', 'b') == 'a:b'
            try {
                C.pair(null, 'b')
                assert false
            } catch (NullPointerException expected) {
            }
            assert C.pair('a', null) == 'a:null'   // unwoven arm generated nothing
        '''
    }

    @Test
    void constructorGuard() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class Account {
                int balance
                @ThrowsIf(value = { opening < 0 }, exception = IllegalArgumentException)
                Account(int opening) { balance = opening }
            }
            assert new Account(10).balance == 10
            try {
                new Account(-1)
                assert false
            } catch (IllegalArgumentException expected) {
            }
        '''
    }

    @Test
    void armsAreRuntimeRetainedStructuredMetadata() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                @ThrowsIf(value = { s == null }, exception = NumberFormatException, woven = false, direct = false, exhaustive = false)
                @ThrowsIf(value = { radix < 2 }, exception = IllegalArgumentException, woven = false)
                static int parse(String s, int radix) { Integer.parseInt(s, radix) }
            }
            def arms = C.getDeclaredMethod('parse', String, int).getAnnotationsByType(ThrowsIf)
            assert arms.length == 2
            def byExc = arms.collectEntries { [(it.exception().simpleName): it] }
            assert !byExc.NumberFormatException.exhaustive() && !byExc.NumberFormatException.direct()
            assert byExc.IllegalArgumentException.exhaustive() && byExc.IllegalArgumentException.direct() && !byExc.IllegalArgumentException.woven()
        '''
    }

    @Test
    void checkedMustThrowViolationOnSilentReturn() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class C {
                // checked + unwoven: the body SHOULD throw when n < 0 but doesn't — a broken
                // implementation, reported as a violation (never the declared exception).
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false, checked = true)
                static int identity(int n) { n }
            }
            assert C.identity(5) == 5
            try {
                C.identity(-5)
                assert false, 'expected ThrowsIfViolation'
            } catch (ThrowsIfViolation e) {
                assert e.message.contains('returned normally')
            }
        '''
    }

    @Test
    void checkedOnlyWhenViolationOnUnjustifiedThrow() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class C {
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false, checked = true)
                static int f(int n) {
                    if (n < 0) throw new IllegalArgumentException('negative')
                    if (n > 100) throw new IllegalArgumentException('too big')   // unlisted reason
                    n
                }
            }
            assert C.f(5) == 5
            try {
                C.f(200)
                assert false, 'expected ThrowsIfViolation'
            } catch (ThrowsIfViolation e) {
                assert e.message.contains('no matching condition')
            }
        '''
    }

    @Test
    void checkedJustifiedThrowPropagatesUntouched() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false, checked = true)
                static int f(int n) {
                    if (n < 0) throw new IllegalArgumentException('negative')
                    n
                }
            }
            // the defined behaviour must reach the caller as-is — checking never swallows it
            try {
                C.f(-1)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message == 'negative'
            }
        '''
    }

    @Test
    void checkedNonExhaustiveSetSkipsOnlyWhen() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                // exhaustive = false: one-directional — an unlisted throw reason is in-contract,
                // so only-when is not checked (must-throw still is).
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false, checked = true, exhaustive = false)
                static int f(int n) {
                    if (n < 0) throw new IllegalArgumentException('negative')
                    if (n > 100) throw new IllegalArgumentException('too big')
                    n
                }
            }
            try {
                C.f(200)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message == 'too big'   // propagated, not a violation
            }
        '''
    }

    @Test
    void checkedWithWovenArmStillChecksOnlyWhen() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class C {
                // woven + checked: the guard implements must-throw; only-when is still verified.
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, checked = true)
                static int f(int n) {
                    if (n > 100) throw new IllegalArgumentException('too big')   // unlisted reason
                    n
                }
            }
            assert C.f(5) == 5
            try {
                C.f(-1)
                assert false
            } catch (IllegalArgumentException e) {
                assert e.message.contains('n < 0')   // the woven guard, propagated through the checker
            }
            try {
                C.f(200)
                assert false
            } catch (ThrowsIfViolation expected) {
            }
        '''
    }

    @Test
    void checkedTrustedArmValidatesTheThirdPartyClaim() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class C {
                // direct = false (never woven) + checked: the claim about invoked code is validated at runtime —
                // here the callee does NOT actually throw, so the trusted claim is exposed as wrong.
                @ThrowsIf(value = { s == null }, exception = NullPointerException, woven = false, direct = false, checked = true)
                static Object passThrough(Object s) { s }
            }
            assert C.passThrough('x') == 'x'
            try {
                C.passThrough(null)
                assert false, 'expected ThrowsIfViolation'
            } catch (ThrowsIfViolation expected) {
            }
        '''
    }

    @Test
    void preconditionViolationFiresBeforeTheWovenGuard() {
        assertScript '''
            import groovy.contracts.Requires
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.PreconditionViolation

            class C {
                @Requires({ n >= 0 })
                @ThrowsIf(value = { n < -10 }, exception = IllegalArgumentException)
                static int f(int n) { n }
            }
            // an input violating BOTH: the caller's obligation is judged first — the @Requires
            // assertion weaves ahead of the @ThrowsIf guard, so a caller bug is reported as one
            try {
                C.f(-20)
                assert false
            } catch (PreconditionViolation expected) {
            }
        '''
    }

    @Test
    void requiresEvaluationThrowIsNotMisjudgedByCheckedArms() {
        assertScript '''
            import groovy.contracts.Requires
            import groovy.contracts.ThrowsIf

            class C {
                // the @Requires EVALUATION itself throws ArithmeticException for d == 0; a checked
                // ArithmeticException arm must not misjudge that pre-body throw as a contract
                // violation — the precondition assertion weaves outside the checked wrapper
                @Requires({ 1.intdiv(d) >= 0 })
                @ThrowsIf(value = { d < 0 }, exception = ArithmeticException, woven = false, checked = true)
                static int g(int d) {
                    if (d < 0) throw new ArithmeticException('negative')
                    d
                }
            }
            try {
                C.g(0)
                assert false
            } catch (ArithmeticException e) {
                assert e.message.contains('/ by zero')   // the raw evaluation throw, untouched
            }
        '''
    }

    @Test
    void directIsIgnoredForWovenArms() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                // direct is implicitly true for woven code — setting it changes nothing
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, direct = false)
                static int half(int n) { n.intdiv(2) }
            }
            assert C.half(8) == 4
            try {
                C.half(-2)
                assert false, 'the guard weaves regardless of direct'
            } catch (IllegalArgumentException expected) {
            }
        '''
    }

    @Test
    void vmErrorsAreNeverJudgedByCheckedArms() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class C {
                // even a broad, exhaustive, checked arm never judges a VM error: resource
                // conditions are outside contract semantics — the error propagates untouched
                @ThrowsIf(value = { n < 0 }, exception = Throwable, woven = false, checked = true)
                static int f(int n) {
                    if (n < 0) throw new IllegalStateException('negative')
                    if (n > 100) throw new StackOverflowError('simulated')
                    n
                }
            }
            try {
                C.f(200)
                assert false
            } catch (StackOverflowError e) {
                assert e.message == 'simulated'   // no ThrowsIfViolation masking it
            }
        '''
    }

    @Test
    void explicitSuperCallStaysFirstUnderWovenGuard() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class Base { int b; Base(int b) { this.b = b } }
            class Derived extends Base {
                @ThrowsIf(value = { opening < 0 }, exception = IllegalArgumentException)
                Derived(int opening) {
                    super(opening * 2)
                }
            }
            assert new Derived(5).b == 10
            try {
                new Derived(-1)
                assert false
            } catch (IllegalArgumentException expected) {   // guard runs just after the super call
            }
        '''
    }

    @Test
    void explicitSuperCallStaysFirstUnderCheckedArm() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class Base2 { int b; Base2(int b) { this.b = b } }
            class Derived2 extends Base2 {
                @ThrowsIf(value = { opening < 0 }, exception = IllegalArgumentException, woven = false, checked = true)
                Derived2(int opening) {
                    super(opening * 2)
                }
            }
            assert new Derived2(5).b == 10
            try {
                new Derived2(-1)   // body never throws: must-throw violation, after the super call
                assert false
            } catch (ThrowsIfViolation expected) {
            }
        '''
    }

    @Test
    void thisDelegationStaysFirstUnderWovenGuard() {
        assertScript '''
            import groovy.contracts.ThrowsIf

            class Box {
                int v
                Box() { this(1) }
                @ThrowsIf(value = { v < 0 }, exception = IllegalArgumentException)
                Box(int v) { this.v = v }
            }
            assert new Box().v == 1
            try {
                new Box(-1)
                assert false
            } catch (IllegalArgumentException expected) {
            }
        '''
    }

    @Test
    void checkedIsSetLevelForMustThrow() {
        assertScript '''
            import groovy.contracts.ThrowsIf
            import org.apache.groovy.contracts.ThrowsIfViolation

            class C {
                // only the second arm says checked = true — but checked is SET-level, so the
                // first (unwoven, unchecked) arm is must-throw checked too: it cannot silently
                // opt out while serving as an only-when justifier
                @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false)
                @ThrowsIf(value = { n > 100 }, exception = IllegalArgumentException, woven = false, checked = true)
                static int f(int n) { n }
            }
            assert C.f(5) == 5
            try {
                C.f(-1)   // the UNCHECKED sibling's condition — still a must-throw violation
                assert false
            } catch (ThrowsIfViolation expected) {
            }
        '''
    }

    @Test
    void guardComposesWithRequires() {
        assertScript '''
            import groovy.contracts.Requires
            import groovy.contracts.ThrowsIf

            class C {
                @Requires({ a >= 0 })
                @ThrowsIf(value = { b == 0 }, exception = ArithmeticException)
                static int divide(int a, int b) { a.intdiv(b) }
            }
            assert C.divide(4, 2) == 2
            try {
                C.divide(4, 0)
                assert false
            } catch (ArithmeticException expected) {
            }
        '''
    }
}
