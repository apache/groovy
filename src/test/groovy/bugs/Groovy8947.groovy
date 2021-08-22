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
final class Groovy8947 {

    @Test
    void testResolvingNonStaticInnerClass() {
        assertScript '''
            class Foo {
                @groovy.transform.TupleConstructor(defaults=false)
                class Bar {
                    def baz
                }
                static newBar(def baz) {
                    new Foo().new Bar(baz)
                }
            }

            assert Foo.newBar('baz').baz == 'baz'
            assert new Foo().new Bar('baz').baz == 'baz'
        '''
    }

    @Test
    void testResolvingNonStaticInnerClass2() {
        assertScript '''
            class Foo {
                @groovy.transform.TupleConstructor(defaults=false)
                class Bar {
                    def baz
                }
                static newFoo() {
                    new Foo()
                }
                static newBar(def baz) {
                    return newFoo().new Bar(baz)
                }
            }

            assert Foo.newBar('baz').baz == 'baz'
        '''
    }

    @Test
    void testResolvingNonStaticInnerClass3() {
        assertScript '''
            class Foo {
                @groovy.transform.TupleConstructor(defaults=false)
                class Bar {
                    def baz
                }
                static newFoo() {
                    new Foo()
                }
            }

            assert Foo.newFoo().new Foo.Bar('baz').baz == 'baz'
        '''
    }

    @Test
    void testResolvingNonStaticInnerClass4() {
        def err = shouldFail '''
            class Foo {
                @groovy.transform.TupleConstructor(defaults=false)
                class Bar {
                    def baz
                }
                static newFoo() {
                    new Foo()
                }
            }

            // this form isn't supported outside of enclosing class
            Foo.newFoo().new Bar('baz')
        '''

        assert err =~ 'unable to resolve class Bar'
    }

    @Test
    void testResolvingNonStaticInnerClass5() {
        assertScript '''
            class Foo {
                class Bar {
                    class Baz {
                        final int n = 42
                    }
                }
            }

            assert new Foo().new Bar().new Baz().getN() == 42
        '''
    }
}
