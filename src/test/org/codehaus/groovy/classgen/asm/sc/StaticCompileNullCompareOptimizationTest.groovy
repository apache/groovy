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

/**
 * Unit tests for static compilation: null test optimizations.
 */
final class StaticCompileNullCompareOptimizationTest extends AbstractBytecodeTestCase {

    void testShouldUseIfNull1() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                o != null
            }
        ''')
        assert bytecode.hasStrictSequence(['IFNULL'])
    }

    void testShouldUseIfNull2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                null != o
            }
        ''')
        assert bytecode.hasStrictSequence(['IFNULL'])
    }

    void testShouldUseIfNonNull1() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                o == null
            }
        ''')
        assert bytecode.hasStrictSequence(['IFNONNULL'])
    }

    void testShouldUseIfNonNull2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                null == o
            }
        ''')
        assert bytecode.hasStrictSequence(['IFNONNULL'])
    }

    void testPrimitiveWithNullShouldBeOptimized1() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(int x) {
                null == x
            }
        ''')
        assert bytecode.hasStrictSequence(['ICONST_0', 'POP'])
    }

    void testPrimitiveWithNullShouldBeOptimized2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(int x) {
                x == null
            }
        ''')
        assert bytecode.hasStrictSequence(['ICONST_0', 'POP'])
    }

    void testOptimizeGroovyTruthForPrimitiveBoolean1() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(boolean x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ILOAD 1',
            'IFEQ L1',
            'L1',
            'RETURN'
        ])
    }

    void testOptimizeGroovyTruthForPrimitiveBoolean2() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(boolean x) {
                if (!x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ILOAD 1',
            'IFNE L1',
            'ICONST_1',
            'GOTO L2',
            'L1',
            'ICONST_0',
            'L2',
            'IFEQ L3',
            'L3',
            'RETURN'
        ])
    }

    void testOptimizeGroovyTruthForPrimitiveBoolean3() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(boolean x) {
                if (!!x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ILOAD 1',
            'IFEQ L1',
            'L1',
            'RETURN'
        ])
    }

    void testOptimizeGroovyTruthForNonPrimitiveBoolean() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Boolean x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL',
            'POP',
            'ICONST_0',
            'GOTO',
            'L1',
            'INVOKEVIRTUAL',
            'L2',
            'IFEQ'
        ])
    }

    void testOptimizeGroovyTruthForPrimitiveNumberType() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(int x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ILOAD 1',
            'IFEQ L1',
            'ICONST_1',
            'GOTO L2',
            'L1',
            'ICONST_0',
            'L2',
            'IFEQ L3',
            'L3',
            'RETURN'
        ])
    }

    void testNoGroovyTruthOptimizationForObject() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(Object x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'L1',
            'INVOKEDYNAMIC cast(Ljava/lang/Object;)Z'
        ])
    }

    void testNoGroovyTruthOptimizationForString() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            void m(String x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'L1',
            'INVOKEDYNAMIC cast(Ljava/lang/String;)Z'
        ])
    }

    void testGroovyTruthOptimizationForFinalClass() {
        def bytecode = compile(method:'m', '''
            final class A {
            }
            @groovy.transform.CompileStatic
            void m(A x) {
                if (x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'POP',
            'ICONST_1'
        ])
        assert !bytecode.hasSequence(['INVOKEDYNAMIC cast(LA;)Z'])
    }

    void testGroovyTruthOptimizationForPrivateInnerClass() {
        def bytecode = compile(method:'m', '''
            class A {
                private static class B {
                }
                @groovy.transform.CompileStatic
                void m(B x) {
                    if (x) {
                    }
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'POP',
            'ICONST_1'
        ])
        assert !bytecode.hasSequence(['INVOKEDYNAMIC cast(LA$B;)Z'])
    }

    void testNoGroovyTruthOptimizationForPublicInnerClass() {
        def bytecode = compile(method:'m', '''
            class A {
                public static class B {
                }
                @groovy.transform.CompileStatic
                void m(B x) {
                    if (x) {
                    }
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'L1',
            'INVOKEDYNAMIC cast(LA$B;)Z'
        ])
    }

    // GROOVY-10711
    void testNoGroovyTruthOptimizationIfProvidesAsBoolean() {
        def bytecode = compile(method:'m', '''
            @groovy.transform.CompileStatic
            @groovy.transform.Immutable
            class C {
                boolean asBoolean() {
                }
            }

            @groovy.transform.CompileStatic
            void m(C x) {
                if (!x) {
                }
            }
        ''')
        assert bytecode.hasSequence([
            'ALOAD 1',
            'DUP',
            'IFNONNULL L1',
            'POP',
            'ICONST_0',
            'GOTO L2',
            'L1',
            'INVOKEDYNAMIC cast(LC;)Z'
        ])
    }

    void testCompare() {
        assertScript '''
            class Pogo {
            }
            @groovy.transform.CompileStatic
            class C {
                static test() {
                    Pogo pogo = null
                    def check = { -> if (pogo) { 1 } else { 0 } }
                    assert check() == 0
                }
            }

            C.test()
        '''
    }
}
