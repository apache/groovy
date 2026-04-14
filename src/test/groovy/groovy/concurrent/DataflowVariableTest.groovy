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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class DataflowVariableTest {

    @Test
    void testBindAndRead() {
        assertScript '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            assert !v.isBound()
            v.bind(42)
            assert v.isBound()
            assert await(v) == 42
        '''
    }

    @Test
    void testLeftShiftOperator() {
        assertScript '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            v << 'hello'
            assert await(v) == 'hello'
        '''
    }

    @Test
    void testDoubleBindThrows() {
        shouldFail(IllegalStateException, '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            v << 10
            v << 20
        ''')
    }

    @Test
    void testBindError() {
        assertScript '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            v.bindError(new RuntimeException('oops'))
            assert v.isCompletedExceptionally()
            try {
                await(v)
                assert false : 'should have thrown'
            } catch (RuntimeException e) {
                assert e.message == 'oops'
            }
        '''
    }

    @Test
    void testAsyncBindAndAwait() {
        assertScript '''
            import groovy.concurrent.DataflowVariable
            import groovy.concurrent.Awaitable

            def x = new DataflowVariable()
            def y = new DataflowVariable()

            def z = Awaitable.go { await(x) + await(y) }

            async { x << 10 }
            async { y << 5 }

            assert await(z) == 15
        '''
    }

    @Test
    void testClassicDataflowPattern() {
        assertScript '''
            import groovy.concurrent.DataflowVariable
            import groovy.concurrent.Awaitable

            def x = new DataflowVariable()
            def y = new DataflowVariable()
            def z = new DataflowVariable()

            async {
                z << await(x) + await(y)
            }

            async { x << 10 }
            async { y << 5 }

            assert await(z) == 15
        '''
    }

    @Test
    void testThenChaining() {
        assertScript '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            def doubled = v.then { it * 2 }

            async { v << 21 }

            assert await(doubled) == 42
        '''
    }

    @Test
    void testAllCombinator() {
        assertScript '''
            import groovy.concurrent.DataflowVariable
            import groovy.concurrent.Awaitable

            def a = new DataflowVariable()
            def b = new DataflowVariable()
            def c = new DataflowVariable()

            async { a << 1 }
            async { b << 2 }
            async { c << 3 }

            def results = await(a, b, c)
            assert results == [1, 2, 3]
        '''
    }

    @Test
    void testToString() {
        assertScript '''
            import groovy.concurrent.DataflowVariable

            def v = new DataflowVariable()
            assert v.toString() == 'DataflowVariable[unbound]'
            v << 'hi'
            assert v.toString() == 'DataflowVariable[hi]'
        '''
    }
}
