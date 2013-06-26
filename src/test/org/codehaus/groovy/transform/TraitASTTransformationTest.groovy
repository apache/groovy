/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform

import groovy.transform.CompileStatic
import groovy.transform.Trait

import java.lang.invoke.MethodHandles

class TraitASTTransformationTest extends GroovyTestCase {
    void testTraitWithNoMethod() {
        assertScript '''
        trait MyTrait {}

        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo instanceof MyTrait
        '''
    }

    void testTraitWithOneMethod() {
        assertScript '''
        trait MyTrait {
            int a() { 1 }

        }

        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo instanceof MyTrait
        assert foo.a() == 1
        '''

    }

    void testTraitWithTwoMethods() {
        assertScript '''
        trait MyTrait {
            int a() { 1 }
            int b() { a() }
        }

        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo instanceof MyTrait
        assert foo.a() == 1
        assert foo.b() == 1
        '''

    }

    void testTraitWithTwoMethodsAndOneOverride() {
        assertScript '''
        trait MyTrait {
            int a() { 1 }
            int b() { a() }
        }

        class Foo implements MyTrait {
            int a() { 2 }

        }
        def foo = new Foo()
        assert foo instanceof MyTrait
        assert foo.a() == 2
        assert foo.b() == 2
        '''

    }

    void testTraitWithTwoMethodsAndOneAbstract() {
        assertScript '''
        trait MyTrait {
            abstract int a()
            int b() { a() }
        }

        class Foo implements MyTrait {
            int a() { 2 }

        }
        def foo = new Foo()
        assert foo instanceof MyTrait
        assert foo.a() == 2
        assert foo.b() == 2
        '''

    }

    void testTraitWithTwoMethodsAndOneAbstractNotImplemented() {
        shouldFail {
            assertScript '''
            abstract trait MyTrait {
                abstract int a()
                int b() { a() }
            }

            class Foo implements MyTrait {
            }
            def foo = new Foo()
        '''
        }
    }

    void testWithPrecompiledTraitWithOneMethod() {
        assertScript '''import org.codehaus.groovy.transform.TraitASTTransformationTest.TestTrait as TestTrait

            class Foo implements TestTrait {}
            def foo = new Foo()
            assert foo.a() == 123
        '''
    }

    void testTraitWithConstructor() {
        shouldFail {
            assertScript '''
            abstract trait MyTrait {
                MyTrait() {
                    println 'woo'
                }
            }

            class Foo implements MyTrait {
            }
            def foo = new Foo()
        '''
        }
    }

    void testTraitWithField() {
        assertScript '''
        trait MyTrait {
            private String message = 'Hello'
            String getBlah() {
                message
            }

        }
        class Foo implements MyTrait {}
        def foo = new Foo()
        assert foo.blah == 'Hello'
        '''
    }

    void testTraitWithField2() {
        assertScript '''import org.codehaus.groovy.transform.TraitASTTransformationTest.TestTrait2 as TestTrait2
        class Foo implements TestTrait2 {
            def cat() { "cat" }
        }
        def foo = new Foo()
        assert foo.message == 'Hello'
        assert foo.@message == 'Hello'
        assert foo.blah() == 'Hello'
        assert foo.meow() == /Meow! I'm a cat/
        '''
    }

    void testTraitWithSetValue() {
        assertScript '''
            import groovy.transform.Trait

            trait Named {
                private String name
                void setLabel(String val) { name = val }
                void setLabel2(String val) { this.name = val }
                void setLabel3(String val) { this.@name = val }
                void getName() { name }
            }

            class Person implements Named {}
            def p = new Person()
            assert p.name == null
            p.setLabel('label')
            assert p.name == 'label'
            p.setLabel2('label2')
            assert p.name == 'label2'
            p.setLabel3('label3')
            assert p.name == 'label3'

        '''
    }

    void testTraitWithProperty() {
        assertScript '''import groovy.transform.Trait

            trait Named {
                String name
            }

            class Person implements Named {}

            def p = new Person(name:'Stromae')

            assert p.name == 'Stromae'

        '''
    }

    void testClosureExpressionInTrait() {
        assertScript '''import groovy.transform.*

        trait GreetingObject {
            String greeting = 'Welcome!'
            Closure greeter() {
                return { -> greeting }
            }
        }
        class Hello implements GreetingObject {}
        def hello = new Hello()
        def greeter = hello.greeter()
        assert greeter.thisObject.is(hello)
        assert greeter() == 'Welcome!'

        '''
    }


    static trait TestTrait2 {
        private String message = 'Hello'
        String getMessage() { this.message }
        String blah() { message }
        def meow() {
            "Meow! I'm a ${cat()}"
        }
    }


    static trait TestTrait {
        int a() { 123 }
    }
}