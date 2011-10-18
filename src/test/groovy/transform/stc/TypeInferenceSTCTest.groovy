/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

/**
 * Unit tests for static type checking : type inference.
 *
 * @author Cedric Champeau
 */
class TypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    void testStringToInteger() {
        assertScript """
        def name = "123" // we want type inference
        name.toInteger() // toInteger() is defined by DGM
        """
    }

    void testGStringMethods() {
        assertScript '''
            def myname = 'Cedric'
            "My upper case name is ${myname.toUpperCase()}"
            println "My upper case name is ${myname}".toUpperCase()
        '''
    }

    void testAnnotationOnSingleMethod() {
        GroovyShell shell = new GroovyShell()
        shell.evaluate '''
            // calling a method which has got some dynamic stuff in it

            import groovy.transform.StaticTypes
            import groovy.xml.MarkupBuilder

            class Greeter {
                @StaticTypes
                String greeting(String name) {
                    generateMarkup(name.toUpperCase())
                }

                // MarkupBuilder is dynamic so we won't do typechecking here
                String generateMarkup(String name) {
                    def sw = new StringWriter()
                    def mkp = new MarkupBuilder()
                    mkp.html {
                        body {
                            div name
                        }
                    }
                    sw
                }
            }

            def g = new Greeter()
            g.greeting("Guillaume")

        '''
    }

    void testInstanceOf() {
        assertScript """
        Object o
        if (o instanceof String) o.toUpperCase()
        """
    }

    void testInstanceOfAfterEach() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            }
            o.toUpperCase() // ensure that type information is lost after if()
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testInstanceOfInElseBranch() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            } else {
                o.toUpperCase() // ensure that type information is lost in else()
            }
        ''', 'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testMultipleInstanceOf() {
        assertScript '''
            class A {
               void foo() { println 'ok' }
            }

            class B {
               void foo() { println 'ok' }
               void foo2() { println 'ok 2' }
            }


            def o = new A()

            if (o instanceof A) {
               o.foo()
            }

            if (o instanceof B) {
               o.foo()
            }

            if (o instanceof A || o instanceof B) {
              o.foo()
            }

        '''
    }

    void testInstanceOfInTernaryOp() {
        assertScript '''
            class A {
               int foo() { 1 }
            }

            class B {
               int foo2() { 2 }
            }


            def o = new A()

            int result = o instanceof A?o.foo():(o instanceof B?o.foo2():3)

        '''
    }

    void testShouldNotAllowDynamicVariable() {
        shouldFailWithMessages '''
            String name = 'Guillaume'
            println naamme
        ''', 'variable [naamme] is undeclared'
    }

    void testInstanceOfInferenceWithImplicitIt() {
        assertScript '''
        ['a', 'b', 'c'].each {
            if (it instanceof String) {
                println it.toUpperCase()
            }
        }
        '''
    }

    void testInstanceOfTypeInferenceWithDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            def item = profile[0]
            if (item instanceof String) {
                println item.toUpperCase()
            }
        '''
    }

    void testInstanceOfTypeInferenceWithoutDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            if (profile[0] instanceof String) {
                println profile[0].toUpperCase()
            }
        '''
    }

    void testInstanceOfInferenceWithField() {
        assertScript '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.x = 2
            }
        '''
    }

    void testInstanceOfInferenceWithFieldAndAssignment() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            if (a instanceof A) {
                a.x = '2'
            }
        ''', 'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testInstanceOfInferenceWithMissingField() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.y = 2
            }
        ''', 'No such property: y for class: A'
    }
}

