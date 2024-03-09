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
package groovy

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for the spread arg(s) operator "m(*x)".
 */
final class SpreadArgTest {

    // GROOVY-9515
    @Test
    void testSpreadList() {
        assertScript '''
            int f(int x, int y) { x + y }
            int f(int x) { x }
            int g(x) { f(*x) }

            assert g([1]) == 1
            assert g([1, 2]) == 3
        '''
    }

    @Test
    void testSpreadArray() {
        assertScript '''
            int f(int x, int y, int z) {
                x + y + z
            }

            Number[] nums = [1, 2, 39]
            assert f(*nums) == 42
        '''
    }

    // GROOVY-11186
    @Test
    void testSpreadOther() {
        assertScript '''
            int f(int x, int y, int z) {
                x + y + z
            }

            Set<Number> nums = [1, 2, 39]
            assert f(*nums) == 42
        '''
    }

    // GROOVY-11186
    @Test
    void testSpreadStream() {
        assertScript '''
            int f(int x, int y, int z) {
                x + y + z
            }

            def nums = java.util.stream.IntStream.of(1, 2, 39)
            assert f(*nums) == 42
        '''
    }

    // GROOVY-5647
    @Test
    void testSpreadSkipSTC() {
        assertScript '''
            import groovy.transform.CompileStatic
            import static groovy.transform.TypeCheckingMode.SKIP

            @CompileStatic
            class C {
                @CompileStatic(SKIP)
                def foo(fun, args) {
                    new Runnable() { // create an anonymous class which should *not* be visited
                        void run() {
                            fun(*args) // spread operator is disallowed with STC/SC, but SKIP should prevent from an error
                        }
                    }
                }
            }

            new C()
        '''
    }

    @Test
    void testSpreadVarargs() {
        assertScript '''
            int f(String... strings) {
                g(*strings)
            }
            int g(String... strings) {
                strings.length
            }

            assert f("1","2") == 2
        '''
    }
}
