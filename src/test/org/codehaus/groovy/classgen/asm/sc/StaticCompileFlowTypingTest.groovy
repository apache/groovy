package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompileFlowTypingTest extends AbstractBytecodeTestCase {
    void testFlowTyping() {
        assertScript '''
            @groovy.transform.CompileStatic
            Object m() {
                def o = 1
                def s = o.toString()
                o = 'string'
                println o.toUpperCase()
                o = '123'
                o = o.toInteger()
            }
            assert m() == 123
        '''
    }

    void testInstanceOf() {
        assertScript '''
            @groovy.transform.CompileStatic
            Object m(Object o) {
                if (o instanceof String) {
                    return o.toUpperCase()
                }
                return null
            }
            assert m('happy new year') == 'HAPPY NEW YEAR'
            assert m(123) == null
        '''
    }

    void testMethodSelection() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                int foo(String o) { 1 }
                int foo(int x) { 2 }
            }
            A a = new A()
            assert a.foo('happy new year') == 1
            assert a.foo(123) == 2
        '''
    }

    void testMethodSelectionDifferentFromDynamicGroovy() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                int foo(String o) { 1 }
                int foo(int x) { 2 }
                int foo(Object x) { 3 }
            }

            // if tests are not wrapped in a statically compiled section, method
            // selection is dynamic
            @groovy.transform.CompileStatic
            void performTests() {
                A a = new A()
                Object[] arr = [ 'happy new year', 123, new Object() ]
                assert a.foo(arr[0]) == 3
                assert a.foo(arr[1]) == 3
                assert a.foo(arr[2]) == 3
            }
            performTests()

            // tests that behaviour is different from regular Groovy
            A a = new A()
            Object[] arr = [ 'happy new year', 123, new Object() ]
            assert a.foo(arr[0]) == 1
            assert a.foo(arr[1]) == 2
            assert a.foo(arr[2]) == 3

        '''
    }
}
