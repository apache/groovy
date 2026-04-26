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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Regression coverage for the synthesised lower-arity bridge constructor that
 * {@code @CompileStatic} emits for a constructor with a default-valued list
 * parameter. The bridge inlines the default {@code [...]} literal as a
 * {@code new ArrayList(n)} followed by {@code .add(...)} calls; when the
 * constructor goes through a dynamic call site (which happens in both indy
 * and non-indy modes for this bridge) it returns {@code Object} on the JVM
 * stack, so the {@code INVOKEVIRTUAL ArrayList.add} that follows must be
 * preceded by a {@code CHECKCAST ArrayList} to satisfy the verifier.
 */
final class Groovy11967 {

    @Test
    void testDefaultListClassParam() {
        assertScript '''
            interface MessageSource {}
            @groovy.transform.CompileStatic
            class C {
                List<Class> types
                C(Class<?> a, MessageSource b, List<Class> targetTypes = [Object]) {
                    this.types = targetTypes
                }
            }
            class FakeMs implements MessageSource {}

            def c = new C(String, new FakeMs())
            assert c.types == [Object]
        '''
    }

    @Test
    void testDefaultListStringParam() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                List<String> values
                C(int x, List<String> values = ['hi']) { this.values = values }
            }
            assert new C(1).values == ['hi']
        '''
    }

    @Test
    void testDefaultListIntegerParam() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                List<Integer> values
                C(int x, List<Integer> values = [1, 2, 3]) { this.values = values }
            }
            assert new C(1).values == [1, 2, 3]
        '''
    }

    @Test
    void testDefaultNestedListParam() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                List<List<String>> values
                C(int x, List<List<String>> values = [['a'], ['b']]) { this.values = values }
            }
            assert new C(1).values == [['a'], ['b']]
        '''
    }

    @Test
    void testDefaultEmptyListParam() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                List<String> values
                C(int x, List<String> values = []) { this.values = values }
            }
            assert new C(1).values == []
        '''
    }

    @Test
    void testDefaultListInMiddleParam() {
        // exercises the bridge that drops *only* the trailing parameter,
        // leaving the list-default in non-final position when the next
        // bridge layer is generated.
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                List<String> values
                String tag
                C(int x, List<String> values = ['a'], String tag = 't') {
                    this.values = values
                    this.tag = tag
                }
            }
            assert new C(1).values == ['a']
            assert new C(1).tag == 't'
            assert new C(1, ['b']).tag == 't'
        '''
    }

    @Test
    void testDefaultListClassParam_nonIndy() {
        // The bridge constructor's `new ArrayList(n)` is dispatched through a
        // dynamic call site even in non-indy mode, so the same CHECKCAST is
        // required there.
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        new GroovyShell(config).evaluate '''
            @groovy.transform.CompileStatic
            class C {
                List<Class> types
                C(Class<?> a, Object b, List<Class> targetTypes = [Object]) {
                    this.types = targetTypes
                }
            }
            assert new C(String, "x").types == [Object]
        '''
    }
}
