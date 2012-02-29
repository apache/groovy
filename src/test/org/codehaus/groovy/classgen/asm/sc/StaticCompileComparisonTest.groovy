package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompileComparisonTest extends AbstractBytecodeTestCase {
    void testCompareInts() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1 < 2
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ICONST_1','ICONST_2', 'IF_ICMPGE']
        )
    }

    void testCompareDoubles() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1d < 2d
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['DCONST_1','LDC 2.0', 'DCMPG']
        )
    }

    void testCompareDoubleWithInt() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m() {
                return 1d < 2i
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['DCONST_1','ICONST_2', 'I2D','DCMPG']
        )
    }

    void testCompareArrayLen() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object[] arr) {
                return arr.length >0
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ARRAYLENGTH', 'ICONST_0','IF_ICMPLE']
        )
    }

    void testCompareArrayLenUsingIf() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object[] arr) {
                if (arr.length >0) {
                    return true
                } else {
                    return false
                }
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ARRAYLENGTH', 'ICONST_0','IF_ICMPLE']
        )
    }

    void testIdentityCompare() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            boolean m(Object o) {
                return o.is(o)
            }
            assert m(new Object())
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ALOAD', 'INVOKESTATIC org/codehaus/groovy/runtime/DefaultGroovyMethods.is (Ljava/lang/Object;Ljava/lang/Object;)Z']
        )
        clazz.newInstance().main()
    }


}
