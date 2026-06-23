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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * A subscript assignment {@code a[i] = v} must evaluate the receiver and the
 * index before the right-hand side, matching Java's left-to-right evaluation
 * order. GROOVY-2556 inadvertently reversed this for plain assignment.
 */
final class Groovy12097 {

    @Test
    void testArrayAssignmentEvaluationOrder() {
        assertScript '''
            int[] a = [-1, -1, -1, -1]
            int x = 0, y = 3
            a[x] = x++
            a[y] = --y
            assert a == [0, -1, -1, 2] as int[]
        '''
    }

    @Test
    void testListAssignmentEvaluationOrder() {
        assertScript '''
            List<Integer> a = [-1, -1, -1, -1]
            int x = 0, y = 3
            a[x] = x++
            a[y] = --y
            assert a == [0, -1, -1, 2]
        '''
    }

    @Test
    void testReceiverIndexAndValueOrder() {
        assertScript '''
            def order = []
            def receiver = { order << 'receiver'; new int[3] }
            def index    = { order << 'index'; 0 }
            def value    = { order << 'value'; 42 }
            receiver()[index()] = value()
            assert order == ['receiver', 'index', 'value']
        '''
    }

    @Test
    void testCompileStaticArrayAssignmentEvaluationOrder() {
        assertScript '''
            @groovy.transform.CompileStatic
            int[] go() {
                int[] a = [-1, -1, -1, -1]
                int x = 0, y = 3
                a[x] = x++
                a[y] = --y
                a
            }
            assert go() == [0, -1, -1, 2] as int[]
        '''
    }

    @Test
    void testCompileStaticListAssignmentEvaluationOrder() {
        assertScript '''
            @groovy.transform.CompileStatic
            List<Integer> go() {
                List<Integer> a = [-1, -1, -1, -1]
                int x = 0, y = 3
                a[x] = x++
                a[y] = --y
                a
            }
            assert go() == [0, -1, -1, 2]
        '''
    }

    // The assignment expression still yields the right-hand side value.
    @Test
    void testAssignmentValueIsRightHandSide() {
        assertScript '''
            int[] a = new int[3]
            assert (a[0] = 7) == 7
            assert a[0] == 7
        '''
    }

    // JLS 15.26.1 step 2: the index subexpression is evaluated (including its side
    // effects) before the right-hand side. For "a[i++] = i": index is 0 (i -> 1),
    // then the RHS reads i == 1, so a[0] = 1. Verified against javac.
    @Test
    void testIndexSubexpressionSideEffectBeforeRhs() {
        assertScript '''
            int[] a = new int[3]
            int i = 0
            a[i++] = i
            assert a == [1, 0, 0] as int[]
            assert i == 1
        '''
    }

    // JLS 15.26.1: index of LHS (step 2) before RHS (step 3). For "a[i++] = a[i++]"
    // the LHS index is 0 (i -> 1), the RHS is a[1] == 2 (i -> 2), so a[0] = 2.
    @Test
    void testSelfReferentialCopyWithIncrement() {
        assertScript '''
            int[] a = [1, 2, 3]
            int i = 0
            a[i++] = a[i++]
            assert a == [2, 2, 3] as int[]
            assert i == 2
        '''
    }

    // JLS 15.26.1 steps 1-3: array reference, then index, then right-hand side.
    @Test
    void testArrayReferenceThenIndexThenRhsOrder() {
        assertScript '''
            def order = []
            int[] arr = new int[3]
            def ref   = { order << 'ref'; arr }
            def index = { order << 'index'; 0 }
            def value = { order << 'value'; 9 }
            ref()[index()] = value()
            assert order == ['ref', 'index', 'value']
            assert arr == [9, 0, 0] as int[]
        '''
    }

    // Multiple increments/decrements across both the index and the RHS.
    @Test
    void testMultipleIncrementDecrement() {
        assertScript '''
            int[] a = [10, 20, 30, 40]
            int i = 0, j = 3
            a[i++] = j--      // a[0] = 3 (i -> 1, j -> 2)
            a[++i] = --j      // i -> 2, index 2, j -> 1, a[2] = 1
            assert a == [3, 20, 1, 40] as int[]
            assert i == 2 && j == 1
        '''
    }

    // JLS 15.26.1 step 4: bounds are checked only after the RHS is evaluated, so the
    // RHS side effect happens even when the index is out of bounds. Verified vs javac.
    @Test
    void testRightHandSideEvaluatedBeforeBoundsCheck() {
        assertScript '''
            int[] a = new int[2]
            int rhsCalls = 0
            try {
                a[5] = (rhsCalls += 1)
                assert false : 'expected IndexOutOfBoundsException'
            } catch (IndexOutOfBoundsException expected) {
                assert rhsCalls == 1
            }
        '''
    }

    // JLS 15.26.1 step 4: the null array reference is reported only after the index
    // and the RHS have been evaluated. Verified against javac.
    @Test
    void testIndexAndRightHandSideEvaluatedBeforeNullCheck() {
        assertScript '''
            int[] a = null
            int i = 0
            int rhsCalls = 0
            try {
                a[i++] = (rhsCalls += 1)
                assert false : 'expected NullPointerException'
            } catch (NullPointerException expected) {
                assert i == 1
                assert rhsCalls == 1
            }
        '''
    }

    @Test
    void testCompileStaticIndexSubexpressionSideEffectBeforeRhs() {
        assertScript '''
            @groovy.transform.CompileStatic
            int[] go() {
                int[] a = new int[3]
                int i = 0
                a[i++] = i
                a
            }
            assert go() == [1, 0, 0] as int[]
        '''
    }

    @Test
    void testCompileStaticSelfReferentialCopyWithIncrement() {
        assertScript '''
            @groovy.transform.CompileStatic
            int[] go() {
                int[] a = [1, 2, 3]
                int i = 0
                a[i++] = a[i++]
                a
            }
            assert go() == [2, 2, 3] as int[]
        '''
    }

    @Test
    void testCompileStaticRightHandSideEvaluatedBeforeBoundsCheck() {
        assertScript '''
            @groovy.transform.CompileStatic
            int go() {
                int[] a = new int[2]
                int rhsCalls = 0
                try {
                    a[5] = (rhsCalls += 1)
                    assert false : 'expected IndexOutOfBoundsException'
                } catch (IndexOutOfBoundsException expected) {
                }
                rhsCalls
            }
            assert go() == 1
        '''
    }

    @Test
    void testCompileStaticIndexAndRightHandSideEvaluatedBeforeNullCheck() {
        assertScript '''
            @groovy.transform.CompileStatic
            String go() {
                int[] a = null
                int i = 0
                int rhsCalls = 0
                try {
                    a[i++] = (rhsCalls += 1)
                    assert false : 'expected NullPointerException'
                } catch (NullPointerException expected) {
                }
                "i=$i rhsCalls=$rhsCalls"
            }
            assert go() == 'i=1 rhsCalls=1'
        '''
    }

    // Complement to the assignment cases: array reads (getAt) within an arithmetic
    // expression are evaluated left-to-right too, including subscript side effects.
    // For i=0: a[i++]=a[0]=10 (i->1); a[++i]=a[2]=3000 (i->2); a[i++]=a[2]=3000 (i->3).
    @Test
    void testReadSideSubscriptEvaluationOrder() {
        assertScript '''
            int[] a = [10, 200, 3000, 40000]
            int i = 0
            int b = a[i++] + a[++i] + a[i++]
            assert b == 6010
            assert i == 3
        '''
    }

    @Test
    void testCompileStaticReadSideSubscriptEvaluationOrder() {
        assertScript '''
            @groovy.transform.CompileStatic
            String go() {
                int[] a = [10, 200, 3000, 40000]
                int i = 0
                int b = a[i++] + a[++i] + a[i++]
                "b=$b i=$i"
            }
            assert go() == 'b=6010 i=3'
        '''
    }
}
