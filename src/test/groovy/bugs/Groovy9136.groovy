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

import groovy.test.NotYetImplemented
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy9136 {

    @Test
    void testMethodParameterFieldAccessFromClosure1() {
        assertScript '''
            class Foo {
                public String field = 'foo'
            }
            class Bar {
                @groovy.transform.CompileStatic
                def test(Foo foo) {
                    'baz'.with {
                        foo.field // Access to Foo#foo is forbidden at line: -1, column: -1
                    }
                }
            }

            def bar = new Bar()
            def out = bar.test(new Foo())
            assert out == 'foo'
        '''
    }

    @Test @NotYetImplemented // GROOVY-9195
    void testMethodParameterFieldAccessFromClosure2() {
        def err = shouldFail '''
            class Foo {
                private String field = 'foo'
            }
            class Bar {
                @groovy.transform.CompileStatic
                def test(Foo foo) {
                    'baz'.with {
                        foo.field
                    }
                }
            }

            def bar = new Bar()
            def out = bar.test(new Foo())
        '''

        assert err =~ /Access to Foo#field is forbidden/
    }

    @Test
    void testMethodParameterFieldAccessFromClosure3() {
        def err = shouldFail '''
            class Foo {
                private String field = 'foo'
            }
            @groovy.transform.CompileStatic
            class Bar {
                def test(Foo foo) {
                    foo.with {
                        field
                    }
                }
            }

            def bar = new Bar()
            def out = bar.test(new Foo())
        '''

        assert err =~ /Access to Foo#field is forbidden/
    }
}
