package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

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
        ''', 'Variable [naamme] is undefined'
    }
}

