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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class ConditionalJumpFoldingMethodVisitorTest extends AbstractBytecodeTestCase {

    // GROOVY-12542
    @Test
    void forLoopConditionBranchesDirectly() {
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            int m(int n) {
                int sum = 0
                for (int i = 0; i < n; i++) { sum += i }
                sum
            }
        ''')
        assert bytecode.hasStrictSequence(['ILOAD 3', 'ILOAD 1', 'IF_ICMPGE'])
        assert !bytecode.hasSequence(['ICONST_1', 'GOTO', 'ICONST_0', 'IFEQ'])
    }

    // GROOVY-12542
    @Test
    void ifConditionBranchesDirectly() {
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            String m(int a, int b) {
                if (a <= b) return 'le'
                'gt'
            }
        ''')
        assert bytecode.hasStrictSequence(['ILOAD 1', 'ILOAD 2', 'IF_ICMPGT'])
        assert !bytecode.hasSequence(['ICONST_1', 'GOTO', 'ICONST_0', 'IFEQ'])
    }

    // GROOVY-12542
    @Test
    void nullConditionBranchesDirectly() {
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            String m(Object o) {
                if (o == null) return 'null'
                if (o != null) return 'nonnull'
                'unreachable'
            }
        ''')
        assert bytecode.hasStrictSequence(['ALOAD 1', 'IFNONNULL'])
        assert bytecode.hasStrictSequence(['ALOAD 1', 'IFNULL'])
        assert !bytecode.hasSequence(['ICONST_1', 'GOTO', 'ICONST_0', 'IFEQ'])
    }

    // GROOVY-12542
    @Test
    void longConditionBranchesDirectly() {
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            String m(long a, long b) {
                if (a <= b) return 'le'
                'gt'
            }
        ''')
        assert bytecode.hasStrictSequence(['LLOAD 1', 'LLOAD 3', 'LCMP', 'IFGT'])
        assert !bytecode.hasSequence(['ICONST_1', 'GOTO', 'ICONST_0', 'IFEQ'])
    }

    // GROOVY-12542
    @Test
    void storedComparisonIsStillMaterialized() {
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            boolean m(int a, int b) {
                boolean r = a < b
                r
            }
        ''')
        assert bytecode.hasSequence(['IF_ICMPGE', 'ICONST_1', 'GOTO', 'ICONST_0', 'ISTORE'])
    }

    // GROOVY-12542
    @Test
    void foldedConditionsKeepTheirMeaning() {
        ['', '@groovy.transform.CompileStatic'].each { mode ->
            assertScript """
                $mode
                class C {
                    static int count(int n) { int c = 0; for (int i = 0; i < n; i++) c++; c }
                    static int countDown(int n) { int c = 0; while (n > 0) { n--; c++ }; c }
                    static boolean lt(int a, int b) { if (a < b) return true; false }
                    static boolean notLt(int a, int b) { if (!(a < b)) return true; false }
                    static boolean between(int x, int lo, int hi) { if (lo <= x && x < hi) return true; false }
                    static boolean outside(int x, int lo, int hi) { if (x < lo || x >= hi) return true; false }
                    static boolean longLe(long a, long b) { if (a <= b) return true; false }
                    static boolean dblLt(double a, double b) { if (a < b) return true; false }
                    static boolean dblGe(double a, double b) { if (a >= b) return true; false }
                    static boolean dblNotLt(double a, double b) { if (!(a < b)) return true; false }
                    static boolean fltGt(float a, float b) { if (a > b) return true; false }
                    static boolean isNull(Object o) { if (o == null) return true; false }
                    static boolean same(Object a, Object b) { if (a.is(b)) return true; false }
                    static int ternary(int a, int b) { a < b ? a : b }
                    static boolean stored(int a, int b) { boolean r = a < b; r }
                    // stored (so never folded) counterparts, to check folded branches agree for NaN
                    static boolean dblLtValue(double a, double b) { boolean r = a < b; r }
                    static boolean dblGeValue(double a, double b) { boolean r = a >= b; r }
                    static boolean fltGtValue(float a, float b) { boolean r = a > b; r }
                }

                assert C.count(5) == 5 && C.count(0) == 0 && C.count(-3) == 0
                assert C.countDown(4) == 4
                assert C.lt(1, 2) && !C.lt(2, 2) && !C.lt(3, 2)
                assert !C.notLt(1, 2) && C.notLt(2, 2)
                assert C.between(5, 0, 10) && !C.between(10, 0, 10) && !C.between(-1, 0, 10)
                assert C.outside(-1, 0, 10) && C.outside(10, 0, 10) && !C.outside(5, 0, 10)
                assert C.longLe(1L, 1L) && !C.longLe(2L, 1L)
                assert C.dblLt(1d, 2d) && !C.dblLt(2d, 1d)
                // NaN: a folded branch must agree with the materialized comparison
                [[Double.NaN, 1d], [1d, Double.NaN], [Double.NaN, Double.NaN]].each { double a, double b ->
                    assert C.dblLt(a, b) == C.dblLtValue(a, b)
                    assert C.dblNotLt(a, b) == !C.dblLtValue(a, b)
                    assert C.dblGe(a, b) == C.dblGeValue(a, b)
                    assert C.fltGt((float) a, (float) b) == C.fltGtValue((float) a, (float) b)
                }
                assert C.fltGt(1f, 0f) && !C.fltGt(0f, 1f)
                assert C.isNull(null) && !C.isNull('x')
                def o = new Object()
                assert C.same(o, o) && !C.same(o, new Object())
                assert C.ternary(1, 2) == 1 && C.ternary(3, 2) == 2
                assert C.stored(1, 2) && !C.stored(2, 1)
            """
        }
    }
}
