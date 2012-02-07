package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

/**
 * Unit tests for static compilation: null test optimizations.
 * 
 * @author Cedric Champeau
 */
class StaticCompileNullCompareOptimizationTest extends AbstractBytecodeTestCase {
    void testShouldUseIfNonNull() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                o == null
            }
        ''')
        assert bytecode.hasStrictSequence([
                'IFNONNULL'
        ])
    }
    void testShouldUseIfNull() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                o != null
            }
        ''')
        assert bytecode.hasStrictSequence([
                'IFNULL'
        ])
    }

    void testShouldUseIfNonNull2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                null == o
            }
        ''')
        assert bytecode.hasStrictSequence([
                'IFNONNULL'
        ])
    }

    void testShouldUseIfNull2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                null != o
            }
        ''')
        assert bytecode.hasStrictSequence([
                'IFNULL'
        ])
    }

    void testPrimitiveWithNullShouldBeOptimized() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(int x) {
                null == x
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ICONST_0',
                'POP'
        ])

    }

    void testPrimitiveWithNullShouldBeOptimized2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(int x) {
                x == null
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ICONST_0',
                'POP'
        ])
    }

}
