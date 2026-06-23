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
 * A pre/post increment or decrement of an array (or list) element, e.g. {@code a[i]++},
 * must evaluate the receiver and the index exactly once -- reusing them for both the
 * read (getAt) and the write (putAt) -- and in left-to-right order (receiver, then index).
 * Previously the receiver was evaluated twice and the index was evaluated before it.
 */
final class Groovy12098 {

    @Test
    void testPostfixEvaluatesReceiverAndIndexOnceInOrder() {
        assertScript '''
            def order = []
            def data = [10, 20, 30]
            def receiver = { order << 'receiver'; data }
            def index    = { order << 'index'; 1 }
            receiver()[index()]++
            assert order == ['receiver', 'index']
            assert data == [10, 21, 30]
        '''
    }

    @Test
    void testPrefixEvaluatesReceiverAndIndexOnceInOrder() {
        assertScript '''
            def order = []
            def data = [10, 20, 30]
            def receiver = { order << 'receiver'; data }
            def index    = { order << 'index'; 1 }
            ++receiver()[index()]
            assert order == ['receiver', 'index']
            assert data == [10, 21, 30]
        '''
    }

    @Test
    void testDecrementEvaluatesReceiverAndIndexOnceInOrder() {
        assertScript '''
            def order = []
            def data = [10, 20, 30]
            def receiver = { order << 'receiver'; data }
            def index    = { order << 'index'; 1 }
            receiver()[index()]--
            --receiver()[index()]
            assert order == ['receiver', 'index', 'receiver', 'index']
            assert data == [10, 18, 30]
        '''
    }

    // A receiver that is not referentially transparent (a fresh value each call) must be
    // evaluated once, otherwise the read and the write would target different objects.
    @Test
    void testNonIdempotentReceiverEvaluatedOnce() {
        assertScript '''
            int calls = 0
            def receiver = { calls += 1; [10, 20, 30] as int[] }
            receiver()[1]++
            assert calls == 1
        '''
    }

    // The index subexpression's side effect happens exactly once.
    @Test
    void testIndexSideEffectHappensOnce() {
        assertScript '''
            int[] a = [10, 20, 30]
            int i = 0
            a[i++]++
            assert a == [11, 20, 30] as int[]
            assert i == 1
        '''
    }

    @Test
    void testValuesForArray() {
        assertScript '''
            int[] a = [10, 20, 30]
            assert a[1]++ == 20 && a == [10, 21, 30] as int[]
            assert ++a[1] == 22 && a == [10, 22, 30] as int[]
            assert a[1]-- == 22 && a == [10, 21, 30] as int[]
            assert --a[1] == 20 && a == [10, 20, 30] as int[]
        '''
    }

    @Test
    void testValuesForList() {
        assertScript '''
            def a = [10, 20, 30]
            assert a[1]++ == 20 && a == [10, 21, 30]
            assert ++a[1] == 22 && a == [10, 22, 30]
        '''
    }

    @Test
    void testCompileStaticPostfixEvaluatesReceiverOnce() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static int calls = 0
                static int[] data = [10, 20, 30]
                static int[] receiver() { calls += 1; data }
                static void run() { receiver()[1]++ }
            }
            C.run()
            assert C.calls == 1
            assert C.data == [10, 21, 30] as int[]
        '''
    }

    @Test
    void testCompileStaticPrefixAndDecrementEvaluateReceiverOnce() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static int incCalls = 0, decCalls = 0
                static int[] inc = [10, 20, 30]
                static int[] dec = [10, 20, 30]
                static int[] incReceiver() { incCalls += 1; inc }
                static int[] decReceiver() { decCalls += 1; dec }
                static void run() {
                    ++incReceiver()[1]
                    --decReceiver()[1]
                }
            }
            C.run()
            assert C.incCalls == 1
            assert C.decCalls == 1
            assert C.inc == [10, 21, 30] as int[]
            assert C.dec == [10, 19, 30] as int[]
        '''
    }

    @Test
    void testCompileStaticValuesForArray() {
        assertScript '''
            @groovy.transform.CompileStatic
            String go() {
                int[] a = [10, 20, 30]
                int i = 0
                int p = a[i++]++   // i -> 1, a[0]: 10 -> 11, p == 10
                int q = ++a[1]     // a[1]: 20 -> 21, q == 21
                "p=$p q=$q a=${a.toList()} i=$i"
            }
            assert go() == 'p=10 q=21 a=[11, 21, 30] i=1'
        '''
    }
}
