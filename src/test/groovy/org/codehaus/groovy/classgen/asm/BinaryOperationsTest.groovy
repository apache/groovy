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
package org.codehaus.groovy.classgen.asm

import org.junit.jupiter.api.Test

import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config
import static org.junit.jupiter.api.Assumptions.assumeFalse

final class BinaryOperationsTest extends AbstractBytecodeTestCase {

    @Test
    void testIntPlus() {
        assumeFalse(config.indyEnabled)
        assert compile("""\
            int i = 1
            int j = 2
            int k = i + j
        """).hasSequence([
                "ILOAD",
                "ILOAD",
                "IADD"
        ])
    }

    @Test
    void testIntCompareLessThan() {
        assumeFalse(config.indyEnabled)
        assert compile("""\
            int i = 0
            if (i < 100) println "true"
        """).hasSequence([
                "ILOAD",
                "BIPUSH 100",
                "IF_ICMPGE"
        ])
    }

    // GROOVY-4741
    @Test
    void testCompareLessThanInClosure() {
        assumeFalse(config.indyEnabled)
        assert """
            int a = 0
            [].each {
                if (a < 0) {}
            }
            true
        """
    }

    @Test
    void testLongLeftShift() {
        assumeFalse(config.indyEnabled)
        assert compile("""\
            long a = 1
            long b = a << 32
        """).hasStrictSequence([
                "BIPUSH 32",
                "LSHL"
        ])
    }

    @Test
    void testIntConstants() {
        assumeFalse(config.indyEnabled)
        (0..5).each {
            assert compile("""\
                int a = $it
            """).hasStrictSequence([
                    "ICONST_$it",
            ])
        }
        assert compile("""\
            int a = -1
        """).hasStrictSequence([
                "ICONST_M1",
        ])
        [6,Byte.MIN_VALUE,Byte.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "BIPUSH",
            ])
        }
        [Byte.MIN_VALUE-1,Byte.MAX_VALUE+1,Short.MIN_VALUE,Short.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "SIPUSH",
            ])
        }
        [Short.MAX_VALUE+1,Integer.MAX_VALUE].each {
            assert compile("""\
                    int a = $it
                """).hasStrictSequence([
                    "LDC",
            ])
        }
    }

    @Test
    void testPrimitiveZeroConstants() {
        assumeFalse(config.indyEnabled)
        assert compile("""\
            long a = 0L
        """).hasStrictSequence([
                'LCONST_0',
        ])
        assert compile("""\
            float a = 0f
        """).hasStrictSequence([
                'FCONST_0',
        ])
        assert compile("""\
            double a = 0d
        """).hasStrictSequence([
                'DCONST_0',
        ])
    }

    @Test
    void testCharXor() {
        assumeFalse(config.indyEnabled)
        assert compile('''
            int i = ('a' as char) ^ ('b' as char)
        ''').hasStrictSequence ([
            'IXOR'
        ])
    }

    // GROOVY-10657
    @Test
    void testPutAtValue() {
        def sequence = compile '''
            class C {
                void putAt(String key, Object value) {
                }
            }

            def obj = new C()
            obj['key'] = 'xx'
        '''
    }

    // GROOVY-10657: same store sequences as StaticCompilationTest, without @CompileStatic.
    // The classic backend (indy off) uses the array-store helpers. A statement
    // evaluates receiver, index and value as those arguments and allocates no temporary.
    @Test
    void testDynamicSubscriptAssignmentStatement() {
        def seq = compile(method: 'm', classNamePattern: 'C', indy: false, '''
            class C {
                void m(int[] i) {
                    i[0] = 1
                }
            }
        ''')
        assert seq.hasStrictSequence([
                'ALOAD 1',
                'ICONST_0',
                'ICONST_1',
                'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.intArraySet ([III)V',
        ])
        assert !methodBody(seq).contains('DUP_X2')
        assert !methodBody(seq).contains('ACONST_NULL')
    }

    // GROOVY-10657
    @Test
    void testDynamicSubscriptAssignmentReference() {
        def seq = compile(method: 'm', classNamePattern: 'C', indy: false, '''
            class C {
                void m(Object[] a, Object v) {
                    a[0] = v
                }
            }
        ''')
        assert seq.hasStrictSequence([
                'ALOAD 1',
                'ICONST_0',
                'ALOAD 2',
                'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.objectArraySet ([Ljava/lang/Object;ILjava/lang/Object;)V',
        ])
        assert !methodBody(seq).contains('ACONST_NULL')
    }

    // GROOVY-10657: the assignment value is the right-hand side, duplicated under the store operands
    @Test
    void testDynamicSubscriptAssignmentValue() {
        def seq = compile(method: 'm', classNamePattern: 'C', indy: false, '''
            class C {
                Object m(Object[] a, Object v) {
                    return (a[0] = v)
                }
            }
        ''')
        assert seq.hasStrictSequence([
                'ALOAD 1',
                'ICONST_0',
                'ALOAD 2',
                'DUP_X2',
                'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.objectArraySet ([Ljava/lang/Object;ILjava/lang/Object;)V',
                'ARETURN'
        ])
        // The callsite fallback still uses one temporary and clears it.
        assert seq.hasStrictSequence(['ACONST_NULL', 'ASTORE'])
    }

    // GROOVY-10657: a category-2 value uses DUP2_X2 so the copy keeps both words
    @Test
    void testDynamicSubscriptAssignmentLongValue() {
        def seq = compile(method: 'm', classNamePattern: 'C', indy: false, '''
            class C {
                long m(long[] a) {
                    return (a[0] = 1L)
                }
            }
        ''')
        assert seq.hasStrictSequence([
                'ALOAD 1',
                'ICONST_0',
                'LCONST_1',
                'DUP2_X2',
                'INVOKESTATIC org/codehaus/groovy/runtime/BytecodeInterface8.lArraySet ([JIJ)V',
                'LRETURN'
        ])
        assert !methodBody(seq).contains('ACONST_NULL')
    }

    // GROOVY-10657: indy compiles an array store as putAt. A statement still
    // passes the right-hand side as the call argument and allocates no slot.
    @Test
    void testIndySubscriptAssignmentStatement() {
        def body = methodBody(compile(method: 'm', classNamePattern: 'C', indy: true, '''
            class C {
                void m(Object[] a, Object v) {
                    a[0] = v
                }
            }
        '''))
        assert body.contains('INVOKEDYNAMIC')
        assert body.contains('"putAt"')
        assert !body.contains('ASTORE')
        assert !body.contains('ACONST_NULL')
    }

    // GROOVY-10657: an indy putAt expression keeps one temporary across the call and clears it
    @Test
    void testIndySubscriptAssignmentValueClearsRhsSlot() {
        def seq = compile(method: 'm', classNamePattern: 'C', indy: true, '''
            class C {
                Object m(Object[] a, Object v) {
                    return (a[0] = v)
                }
            }
        ''')
        def body = methodBody(seq)
        assert body.contains('INVOKEDYNAMIC')
        assert body.contains('"putAt"')
        assert body.contains('ARETURN')
        assert seq.hasStrictSequence(['ACONST_NULL', 'ASTORE'])
    }

    // GROOVY-10657: a putAt statement consumes the right-hand side as the call argument
    @Test
    void testDynamicPutAtStatementAllocatesNoRhsSlot() {
        [true, false].each { indy ->
            def body = methodBody(compile(method: 'm', classNamePattern: 'C', indy: indy, '''
                class C {
                    void putAt(int index, Object value) {
                    }
                    void m(Object v) {
                        this[0] = v
                    }
                }
            '''))
            assert !body.contains('ACONST_NULL') : "indy=$indy\n$body"
            if (indy) assert !body.contains('ASTORE') : body
        }
    }

    // GROOVY-10657: a putAt expression keeps one temporary across the call and clears it
    @Test
    void testDynamicPutAtExpressionClearsRhsSlot() {
        [true, false].each { indy ->
            def seq = compile(method: 'm', classNamePattern: 'C', indy: indy, '''
                class C {
                    void putAt(int index, Object value) {
                    }
                    Object m(Object v) {
                        return (this[0] = v)
                    }
                }
            ''')
            assert methodBody(seq).contains('ARETURN') : "indy=$indy"
            assert seq.hasStrictSequence(['ACONST_NULL', 'ASTORE']) : "indy=$indy\n$seq"
        }
    }

    private static String methodBody(InstructionSequence seq) {
        String text = seq.toString()
        int begin = text.indexOf('--BEGIN--')
        int end = text.indexOf('--END--')
        assert begin >= 0 && end > begin : text
        text.substring(begin, end)
    }

    @Test
    void testPrimitiveOrAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
                $type[] array = new $type[1]
                array[0] = 16
                array[0] |= 2
                assert array[0] == 18 : "Failure for type $type"
            """
        }
    }

    @Test
    void testPrimitiveAndAssign() {
        ['byte','int','short','long'].each { type ->
            assertScript """
                $type[] array = new $type[1]
                array[0] = 18
                array[0] &= 2
                assert array[0] == 2 : "Failure for type $type"
            """
        }
    }
}
