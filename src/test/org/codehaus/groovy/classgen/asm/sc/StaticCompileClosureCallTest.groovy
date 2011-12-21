package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Tests for static compilation: checks that closures are called properly.
 */
class StaticCompileClosureCallTest extends AbstractBytecodeTestCase {
    void testShouldCallClosure() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { 666 }
                return closure()
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithOneArg() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x -> x }
                return closure(666)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithTwoArgs() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x, int y -> x+y }
                return closure(333,333)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

    void testShouldCallClosureWithThreeArgs() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                def closure = { int x, int y, int z -> z*(x+y) }
                return closure(333,333,1)
            }
            assert m() == 666
        ''')
        println sequence
        clazz.newInstance().run()
    }

}
