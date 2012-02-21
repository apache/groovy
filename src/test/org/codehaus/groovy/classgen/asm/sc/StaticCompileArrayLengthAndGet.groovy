package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

class StaticCompileArrayLengthAndGet extends AbstractBytecodeTestCase {
    void testShouldCompileArrayLengthStatically() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m(Object[] arr) {
                return arr.length
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD 1','ARRAYLENGTH']
        )
        def obj = clazz.newInstance()
        assert obj.m([4,5,6] as Object[]) == 3
    }

    void testArrayGet1() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            int m(int[] arr) {
                return arr[0]
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['ALOAD','ICONST_0', 'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArrayGet', 'IRETURN']
        )
        def obj = clazz.newInstance()
        assert obj.m([4,5,6] as int[]) == 4

    }

    void testArraySet1() {
        def bytecode = compile([method:'m'],'''
            @groovy.transform.CompileStatic
            void m(int[] arr) {
                arr[0] = 666
            }
        ''')
        assert bytecode.hasStrictSequence(
                ['SIPUSH 666','ISTORE','ALOAD','ICONST_0','ILOAD','INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet']
        )
        def obj = clazz.newInstance()
        int[] arr = [1,2,3]
        obj.m(arr)
        assert arr[0] == 666

    }
}
