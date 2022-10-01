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

final class Groovy10770 {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
        }
    }

    @Test
    void testCheckError1() {
        def err = shouldFail shell, '''
            @TypeChecked
            class C {
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
        '''
        assert err =~ /The variable .bar. is undeclared/
    }

    @Test
    void testCheckError2() {
        def err = shouldFail shell, '''
            @TypeChecked(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
        '''
        assert err =~ /No such property: baz for class: java.lang.String/
    }

    @Test
    void testExtensions1() {
        assertScript shell, '''
            @TypeChecked(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                @TypeChecked(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
            def c = new C()
            def d = new C.D(c)
        '''
    }

    @Test
    void testExtensions2() {
        def err = shouldFail shell, '''
            @CompileStatic(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                @CompileStatic(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
            def c = new C()
            def d = new C.D(c)
        '''
        assert err =~ /Access to java.lang.String#baz is forbidden/ // passes STC but fails during compilation
    }

    @Test
    void testExtensions3() {
        def err = shouldFail shell, '''
            @CompileStatic(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                @TypeChecked(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
            def c = new C()
            def d = new C.D(c)
        '''
        assert err =~ /Access to java.lang.String#baz is forbidden/ // passes STC but fails during compilation
    }

    @Test @NotYetImplemented
    void testExtensions4() {
        def err = shouldFail shell, '''
            @TypeChecked(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                @CompileStatic(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                class D {
                    def foo() {
                        bar.baz
                    }
                }
            }
            def c = new C()
            def d = new C.D(c)
        '''
        assert err =~ /Access to java.lang.String#baz is forbidden/ // passes STC but fails during compilation
    }

    @Test // annotate method
    void testExtensions5() {
        assertScript shell, '''
            @TypeChecked(extensions='groovy/transform/stc/UndefinedVariableTestExtension.groovy')
            class C {
                class D {
                    @TypeChecked(extensions='groovy/transform/stc/UnresolvedPropertyTestExtension.groovy')
                    def foo() {
                        bar.baz
                    }
                }
            }
            def c = new C()
            def d = new C.D(c)
        '''
    }
}
