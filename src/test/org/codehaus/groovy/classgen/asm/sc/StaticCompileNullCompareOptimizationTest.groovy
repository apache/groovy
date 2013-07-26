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


    void testOptimizeGroovyTruthForPrimitiveBoolean() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(boolean x) {
                if (x) {
                    println 'ok'
                }
            }
        ''')
        assert bytecode.hasStrictSequence(['ILOAD 1', 'IFEQ'])
    }

    void testOptimizeGroovyTruthForBoxedBoolean() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Boolean x) {
                if (x) {
                    println 'ok'
                }
            }
        ''')
        assert bytecode.hasStrictSequence(['ALOAD 1', 'DUP', 'IFNONNULL', 'POP', 'ICONST_0', 'GOTO', 'L1', 'INVOKEVIRTUAL', 'L2', 'IFEQ'])
    }

    void testOptimizeGroovyTruthWithStringShouldNotBeTriggered() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(String x) {
                if (x) {
                    println 'ok'
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                'IFEQ'
        ])
    }

    void testGroovyTruthOptimizationWithObjectShouldNotBeTriggered() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object x) {
                if (x) {
                    println 'ok'
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                'IFEQ'
        ])
    }

    void testGroovyTruthOptimizationWithFinalClass() {
        def bytecode = compile(method:'m', '''
            final class A {}
            @groovy.transform.CompileStatic
            void m(A x) {
                if (x) {
                    println 'ok'
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'IFNULL',
        ])
    }

    void testGroovyTruthOptimizationWithPrivateInnerClass() {
        def bytecode = compile(method:'m', '''
            class A {
                private static class B {}
                @groovy.transform.CompileStatic
                void m(B x) {
                    if (x) {
                        println 'ok'
                    }
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'IFNULL',
        ])
    }

    void testGroovyTruthOptimizationWithPublicInnerClass() {
        def bytecode = compile(method:'m', '''
            class A {
                public static class B {}
                @groovy.transform.CompileStatic
                void m(B x) {
                    if (x) {
                        println 'ok'
                    }
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                'IFEQ'
        ])
    }

    void testCompare() {
        def bytecode=compile(method:'stat', '''
            class Doc {}

            @groovy.transform.CompileStatic
            class A {
                static void foo() {
                    Doc doc = null
                    def cl = { if (doc) { 1 } else { 0 } }
                    assert cl() == 0
                }
            }

            A.foo()

        ''')
        clazz.main()
    }

}
