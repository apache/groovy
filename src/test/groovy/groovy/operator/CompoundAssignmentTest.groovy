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
package groovy.operator

import groovy.test.GroovyTestCase

/**
 * GEP-15: tests for dedicated compound-assignment operator overloads
 * (plusAssign, minusAssign, ...).
 *
 * Test scripts use a uniquely-named entry method (not run()) so they do not
 * collide with the script class's auto-generated run() method.
 */
final class CompoundAssignmentTest extends GroovyTestCase {

    void testStaticPlusAssignChosenOverPlus() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Acc {
                int total = 0
                int plusCalls = 0
                int plusAssignCalls = 0
                void plusAssign(int n) { total += n; plusAssignCalls++ }
                Acc plus(int n) { plusCalls++; def a = new Acc(); a.total = total + n; a }
            }

            @CompileStatic
            void exercise() {
                def a = new Acc()
                a += 5
                assert a.total == 5
                assert a.plusAssignCalls == 1
                assert a.plusCalls == 0
                def b = a + 10
                assert a.plusCalls == 1
                assert b.total == 15
            }
            exercise()
        '''
    }

    void testStaticPlusFallbackWhenNoPlusAssign() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class V {
                int n = 0
                V plus(int x) { def v = new V(); v.n = n + x; v }
            }

            @CompileStatic
            void exercise() {
                def v = new V()
                v += 7
                assert v.n == 7
            }
            exercise()
        '''
    }

    void testStaticExpressionValueIsReceiver() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class C {
                int n = 0
                void plusAssign(int x) { n += x }
            }

            @CompileStatic
            void exercise() {
                def c = new C()
                def r = (c += 4)
                assert c.n == 4
                assert r.is(c)
            }
            exercise()
        '''
    }

    void testDynamicPlusAssignChosenOverPlus() {
        assertScript '''
            class Acc {
                int total = 0
                int plusCalls = 0
                int plusAssignCalls = 0
                void plusAssign(int n) { total += n; plusAssignCalls++ }
                Acc plus(int n) { plusCalls++; def a = new Acc(); a.total = total + n; a }
            }

            def a = new Acc()
            a += 5
            assert a.total == 5
            assert a.plusAssignCalls == 1
            assert a.plusCalls == 0
        '''
    }

    void testDynamicPlusFallbackWhenNoPlusAssign() {
        assertScript '''
            class V {
                int n = 0
                V plus(int x) { def v = new V(); v.n = n + x; v }
            }

            def v = new V()
            v += 7
            assert v.n == 7
        '''
    }

    void testStaticPropertyCompoundAssignSkipsSetter() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Counter {
                int n = 0
                void plusAssign(int x) { n += x }
            }

            @CompileStatic
            class Holder {
                Counter counter = new Counter()
                int setCounterCalls = 0
                void setCounter(Counter c) { setCounterCalls++; this.@counter = c }
            }

            @CompileStatic
            void exercise() {
                def h = new Holder()
                h.counter += 3
                assert h.counter.n == 3
                assert h.setCounterCalls == 0
            }
            exercise()
        '''
    }

    void testStaticPrimitiveFastPathUnaffected() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            int sum(int n) {
                int total = 0
                for (i in 0..n) total += i
                total
            }

            assert sum(10) == 55
        '''
    }

    void testStaticFinalLocalWithPlusAssign() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Counter {
                int n = 0
                void plusAssign(int x) { n += x }
            }

            @CompileStatic
            void exercise() {
                final Counter c = new Counter()
                c += 5
                assert c.n == 5
            }
            exercise()
        '''
    }

    void testStaticFinalFieldWithPlusAssign() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Counter {
                int n = 0
                void plusAssign(int x) { n += x }
            }

            @CompileStatic
            class Holder {
                final Counter c = new Counter()
            }

            @CompileStatic
            void exercise() {
                def h = new Holder()
                h.c += 5
                assert h.c.n == 5
            }
            exercise()
        '''
    }

    void testOperatorRenameAssignVariant() {
        assertScript '''
            import groovy.transform.OperatorRename

            class Bag {
                int total = 0
                void addInPlace(int n) { total += n }
                int add(int n) { total + n }
            }

            @OperatorRename(plus="add", plusAssign="addInPlace")
            def exercise() {
                def b = new Bag()
                b += 5
                assert b.total == 5
                def s = b + 3
                assert s == 8
                assert b.total == 5
            }
            exercise()
        '''
    }

    void testOperatorRenameAssignExpressionValueIsReceiver() {
        assertScript '''
            import groovy.transform.OperatorRename

            class Bag {
                int total = 0
                void addInPlace(int n) { total += n }
            }

            @OperatorRename(plusAssign="addInPlace")
            def exercise() {
                def b = new Bag()
                def r = (b += 5)
                assert b.total == 5
                assert r.is(b)
            }
            exercise()
        '''
    }

    void testOperatorRenameBaseOnlyKeepsAssignWrap() {
        assertScript '''
            import groovy.transform.OperatorRename

            class Bag {
                int total = 0
                Bag add(int n) { def b = new Bag(); b.total = total + n; b }
            }

            @OperatorRename(plus="add")
            def exercise() {
                def b = new Bag()
                b += 5
                assert b.total == 5
            }
            exercise()
        '''
    }

    void testStaticAllTwelveOperators() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Probe {
                String last = ''
                void plusAssign(int n)                 { last = 'plusAssign' }
                void minusAssign(int n)                { last = 'minusAssign' }
                void multiplyAssign(int n)             { last = 'multiplyAssign' }
                void divAssign(int n)                  { last = 'divAssign' }
                void remainderAssign(int n)            { last = 'remainderAssign' }
                void powerAssign(int n)                { last = 'powerAssign' }
                void leftShiftAssign(int n)            { last = 'leftShiftAssign' }
                void rightShiftAssign(int n)           { last = 'rightShiftAssign' }
                void rightShiftUnsignedAssign(int n)   { last = 'rightShiftUnsignedAssign' }
                void andAssign(int n)                  { last = 'andAssign' }
                void orAssign(int n)                   { last = 'orAssign' }
                void xorAssign(int n)                  { last = 'xorAssign' }
            }

            @CompileStatic
            void exercise() {
                def p = new Probe()
                p += 1;   assert p.last == 'plusAssign'
                p -= 1;   assert p.last == 'minusAssign'
                p *= 1;   assert p.last == 'multiplyAssign'
                p /= 1;   assert p.last == 'divAssign'
                p %= 1;   assert p.last == 'remainderAssign'
                p **= 1;  assert p.last == 'powerAssign'
                p <<= 1;  assert p.last == 'leftShiftAssign'
                p >>= 1;  assert p.last == 'rightShiftAssign'
                p >>>= 1; assert p.last == 'rightShiftUnsignedAssign'
                p &= 1;   assert p.last == 'andAssign'
                p |= 1;   assert p.last == 'orAssign'
                p ^= 1;   assert p.last == 'xorAssign'
            }
            exercise()
        '''
    }
}
