/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * Unit tests for static compilation: null test optimizations.
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
        if (config.indyEnabled) {
            return
        }
        assert  bytecode.hasStrictSequence(['ALOAD 1', 'DUP', 'IFNONNULL', 'POP', 'ICONST_0', 'GOTO', 'L1',                'INVOKEVIRTUAL', 'L2',                'IFEQ']) ||
                bytecode.hasStrictSequence(['ALOAD 1', 'DUP', 'IFNONNULL', 'POP', 'ICONST_0', 'GOTO', 'L1', 'FRAME SAME1', 'INVOKEVIRTUAL', 'L2', 'FRAME SAME1', 'IFEQ']) // bytecode with stack map frame
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
        if (config.indyEnabled) {
            assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKEDYNAMIC cast(Ljava/lang/String;)Z',
                '',
                '',
                '',
                '',
                '',
                ']',
                'IFEQ'
            ])
        } else {
            assert bytecode.hasStrictSequence([
                    'ALOAD 1',
                    'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                    'IFEQ'
            ])
        }
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
        if (config.indyEnabled) {
            assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKEDYNAMIC cast(Ljava/lang/Object;)Z',
                '',
                '',
                '',
                '',
                '',
                ']',
                'IFEQ'
            ])
        } else {
            assert bytecode.hasStrictSequence([
                    'ALOAD 1',
                    'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                    'IFEQ'
            ])
        }
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
        if (config.indyEnabled) {
            assert bytecode.hasStrictSequence([
                'ALOAD 1',
                'INVOKEDYNAMIC cast(LA$B;)Z',
                '',
                '',
                '',
                '',
                '',
                ']',
                'IFEQ'
            ])
        } else {
            assert bytecode.hasStrictSequence([
                    'ALOAD 1',
                    'INVOKESTATIC org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation.booleanUnbox (Ljava/lang/Object;)Z',
                    'IFEQ'
            ])
        }
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
