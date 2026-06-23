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
 * An Elvis-assignment whose left-hand side is a subscript, e.g. {@code a[i] ?= b}, must
 * evaluate the receiver and the index exactly once -- reusing them for both the read
 * (getAt, in the Elvis test) and the write (putAt) -- and in left-to-right order.
 * Previously the receiver and index were each evaluated twice.
 */
final class Groovy12099 {

    @Test
    void testEvaluatesReceiverAndIndexOnceWhenFalsy() {
        assertScript '''
            def order = []
            def data = [0, 20, 30]
            def receiver = { order << 'receiver'; data }
            def index    = { order << 'index'; 0 }
            def result = (receiver()[index()] ?= 9)
            assert order == ['receiver', 'index']
            assert result == 9
            assert data == [9, 20, 30]
        '''
    }

    @Test
    void testEvaluatesReceiverAndIndexOnceWhenTruthy() {
        assertScript '''
            def order = []
            def data = [5, 20, 30]
            def receiver = { order << 'receiver'; data }
            def index    = { order << 'index'; 0 }
            def result = (receiver()[index()] ?= 9)
            assert order == ['receiver', 'index']
            assert result == 5
            assert data == [5, 20, 30]
        '''
    }

    // A receiver that is not referentially transparent (a fresh value each call) must be
    // evaluated once, otherwise the read and the write would target different objects.
    @Test
    void testNonIdempotentReceiverEvaluatedOnce() {
        assertScript '''
            int calls = 0
            def receiver = { calls += 1; [0, 2, 3] as int[] }
            receiver()[0] ?= 9
            assert calls == 1
        '''
    }

    @Test
    void testIndexSideEffectHappensOnce() {
        assertScript '''
            int[] a = [0, 0, 0]
            int i = 0
            a[i++] ?= 9
            assert a == [9, 0, 0] as int[]
            assert i == 1
        '''
    }

    @Test
    void testValuesForArray() {
        assertScript '''
            int[] a = [0, 2, 0]
            assert (a[0] ?= 9) == 9    // falsy -> assigned
            assert (a[1] ?= 9) == 2    // truthy -> kept
            assert (a[2] ?= 9) == 9
            assert a == [9, 2, 9] as int[]
        '''
    }

    @Test
    void testValuesForList() {
        assertScript '''
            def a = [0, 2, 0]
            assert (a[0] ?= 9) == 9
            assert (a[1] ?= 9) == 2
            assert a == [9, 2, 0]
        '''
    }

    @Test
    void testCompileStaticEvaluatesReceiverOnce() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static int calls = 0
                static int[] data = [0, 2, 3]
                static int[] receiver() { calls += 1; data }
                static void run() { receiver()[0] ?= 9 }
            }
            C.run()
            assert C.calls == 1
            assert C.data == [9, 2, 3] as int[]
        '''
    }

    @Test
    void testCompileStaticValuesForArray() {
        assertScript '''
            @groovy.transform.CompileStatic
            String go() {
                int[] a = [0, 2, 0]
                int x = (a[0] ?= 9)   // 0 -> 9
                int y = (a[1] ?= 9)   // 2 kept
                "x=$x y=$y a=${a.toList()}"
            }
            assert go() == 'x=9 y=2 a=[9, 2, 0]'
        '''
    }

    @Test
    void testCompileStaticValuesForList() {
        assertScript '''
            @groovy.transform.CompileStatic
            String go() {
                List<Integer> a = [0, 2, 0]
                a[0] ?= 9
                a[2] ?= 9
                a.toString()
            }
            assert go() == '[9, 2, 9]'
        '''
    }

    // The fix is scoped to subscript LHS; ordinary Elvis-assignments are unaffected.
    @Test
    void testNonSubscriptElvisAssignmentStillWorks() {
        assertScript '''
            def x = null
            x ?= 42
            assert x == 42

            def m = [k: null]
            m.k ?= 7
            assert m.k == 7
        '''
    }
}
