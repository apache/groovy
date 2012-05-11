package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompileDGMMethodTest extends AbstractBytecodeTestCase {
    void testShouldCallToIntegerOnString() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                return "123".toInteger() // toInteger() is a DGM method
            }
            assert m() == 123
        ''')
        assert bytecode.hasStrictSequence(
                ['LDC','INVOKESTATIC org/codehaus/groovy/runtime/StringGroovyMethods.toInteger', 'INVOKEVIRTUAL java/lang/Integer.intValue','IRETURN']
        )
        clazz.newInstance().run()
    }

    void testShouldCallToIntegerOnGString() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                int i = 123
                return "$i".toInteger() // toInteger() is a DGM method
            }
            assert m() == 123
        ''')
        assert bytecode.hasStrictSequence(
                ['INVOKESTATIC org/codehaus/groovy/runtime/StringGroovyMethods.toInteger','INVOKEVIRTUAL java/lang/Integer.intValue','IRETURN']
        )
        clazz.newInstance().run()
    }

    void testEach() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m() {
                int x
                [1,2,3,4].each {int i-> x = i }
                return x
            }
            assert m() == 4
        ''')
        clazz.newInstance().run()
    }

    void testCollect() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int[] m() {
                int[] result = [1,2,3,4].collect { int x -> x*x } as int[]
                println result
                return result
            }
            assert m() == [1,4,9,16]
        ''')
        clazz.newInstance().run()
    }

}
