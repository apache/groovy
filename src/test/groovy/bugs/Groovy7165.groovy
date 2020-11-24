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
package groovy.bugs

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy7165 {

    @Test
    void testPrivateStaticSuperField1() {
        assertScript '''
            import java.util.function.Function

            @groovy.transform.CompileStatic
            class Bug {
                private static List<String> staticfield = []

                def test() {
                    List<Function<List<String>, String>> runners = []
                    runners.add(new Called())
                    List<String> results = ['hello']
                    runners.each {
                        results.addAll(it.apply(staticfield))
                    }
                    return results.join(' ')
                }

                static class Called implements Function<List<String>, String> {
                    @Override
                    String apply(List<String> args) {
                        'world'
                    }
                }
            }

            def out = new Bug().test()
            assert out == 'hello world'
        '''
    }

    @Test
    void testPrivateStaticSuperField2() {
        def err = shouldFail '''
            class A {
                private static final String CONST = 'value'
            }
            class B extends A {
                @groovy.transform.CompileStatic
                def test() {
                    return CONST // search excludes private members from supers
                }
            }
            new B().test()
        '''

        assert err =~ /MissingPropertyException: No such property: CONST for class: B/
    }

    @Test
    void testPrivateStaticSuperField3() {
        def err = shouldFail '''
            class A {
                private static final String CONST = 'value'
            }
            class B extends A {
                @groovy.transform.CompileStatic
                def test() {
                    return A.CONST
                }
            }
            assert false : 'compilation should fail'
        '''

        assert err =~ /Access to A#CONST is forbidden/
    }
}
