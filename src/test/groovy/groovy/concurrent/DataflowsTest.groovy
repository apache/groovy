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

final class DataflowsTest {

    @Test
    void testClassicDataflowsPattern() {
        assertScript '''
            import groovy.concurrent.Dataflows

            def df = new Dataflows()

            async { df.z = df.x + df.y }
            async { df.x = 10 }
            async { df.y = 5 }

            assert df.z == 15
        '''
    }

    @Test
    void testIsBound() {
        assertScript '''
            import groovy.concurrent.Dataflows

            def df = new Dataflows()
            assert !df.isBound('x')
            async { df.x = 42 }
            // Wait for binding
            assert df.x == 42
            assert df.isBound('x')
        '''
    }

    @Test
    void testGetVariable() {
        assertScript '''
            import groovy.concurrent.Dataflows
            import groovy.concurrent.DataflowVariable

            def df = new Dataflows()
            def v = df.getVariable('x')
            assert v instanceof DataflowVariable
            assert !v.isBound()

            async { df.x = 99 }
            assert await(v) == 99
        '''
    }

    @Test
    void testMultipleVariables() {
        assertScript '''
            import groovy.concurrent.Dataflows

            def df = new Dataflows()

            async { df.sum = df.a + df.b + df.c }
            async { df.a = 1 }
            async { df.b = 2 }
            async { df.c = 3 }

            assert df.sum == 6
        '''
    }

    @Test
    void testStringConcatenation() {
        assertScript '''
            import groovy.concurrent.Dataflows

            def df = new Dataflows()

            async { df.greeting = "${df.first} ${df.last}" }
            async { df.first = 'Hello' }
            async { df.last = 'World' }

            assert df.greeting == 'Hello World'
        '''
    }
}
