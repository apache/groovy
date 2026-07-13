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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.classgen.asm.InstructionSequence
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode

/**
 * Bytecode-shape checks for try/catch/finally emission in {@link StatementWriter}.
 * Plain try/catch (empty finally) must not emit a catch-all identity rethrow;
 * try/finally must keep a catch-all so uncaught exceptions still run finally.
 */
final class Groovy12161 extends AbstractBytecodeTestCase {

    @Test
    void testTryCatchWithoutFinallyHasNoCatchAllHandler() {
        compile method: 'm', '''
            int m(int x) {
                try {
                    return x
                } catch (RuntimeException e) {
                    return -1
                }
            }
        '''

        def blocks = tryCatchBlocks('m')
        assert blocks.every { it.type != null }: "unexpected catch-all in\n${sequence}"
        assert blocks.every { it.type == 'java/lang/RuntimeException' }
        assert !hasIdentityCatchAllRethrow(sequence)
    }

    @Test
    void testMultiCatchWithoutFinallyRegistersOnlyTypedHandlers() {
        compile method: 'm', '''
            int m(int x) {
                try {
                    return x
                } catch (IllegalArgumentException e) {
                    return -1
                } catch (RuntimeException e) {
                    return -2
                }
            }
        '''

        def blocks = tryCatchBlocks('m')
        assert blocks.every { it.type != null }
        assert blocks*.type as Set == ['java/lang/IllegalArgumentException', 'java/lang/RuntimeException'] as Set
    }

    @Test
    void testTryFinallyKeepsCatchAllForUncaughtExceptions() {
        compile method: 'm', '''
            int m(int x) {
                int y = 0
                try {
                    y = x
                } finally {
                    y = y + 1
                }
                return y
            }
        '''

        def blocks = tryCatchBlocks('m')
        assert blocks.any { it.type == null }: "expected catch-all for non-empty finally\n${sequence}"
        assert new GroovyShell().evaluate('''
            int m(int x) {
                int y = 0
                try {
                    y = x
                } finally {
                    y = y + 1
                }
                return y
            }
            m(41)
        ''') == 42
    }

    @Test
    void testTryCatchFinallySemanticsAndCatchAll() {
        def source = '''
            int m(int x) {
                def side = 0
                try {
                    if (x < 0) throw new IllegalArgumentException('neg')
                    return x
                } catch (IllegalArgumentException e) {
                    return -1
                } finally {
                    side = side + 1
                }
            }
            assert m(7) == 7
            assert m(-3) == -1
            m(7)
        '''
        assert new GroovyShell().evaluate(source) == 7

        compile method: 'm', '''
            int m(int x) {
                try {
                    if (x < 0) throw new IllegalArgumentException('neg')
                    return x
                } catch (IllegalArgumentException e) {
                    return -1
                } finally {
                    x = x + 1
                }
            }
        '''
        def blocks = tryCatchBlocks('m')
        assert blocks.any { it.type == 'java/lang/IllegalArgumentException' }
        assert blocks.any { it.type == null }
    }

    @Test
    void testTryFinallyWithReturnInlinesFinallyWithoutTrailingNops() {
        def bytecode = compile(method: 'm', '''
            int m(int x) {
                try {
                    return x
                } finally {
                    x = x
                }
            }
        ''')

        // Leading NOP may remain so the protected range before an inlined finally
        // is non-empty; trailing NOP after the finally (old applyBlockRecorder
        // shape) should not appear as NOP; load; return.
        assert !bytecode.hasStrictSequence(['NOP', 'ILOAD', 'IRETURN'])
        assert new GroovyShell().evaluate('''
            def holder = new Object() {
                def log = []
                int m(int x) {
                    try {
                        return x
                    } finally {
                        log << 'f'
                    }
                }
            }
            assert holder.m(3) == 3
            holder.log
        ''') == ['f']
    }

    @Test
    void testBreakInTryRunsFinally() {
        assert new GroovyShell().evaluate('''
            def called = false
            while (true) {
                try {
                    break
                } finally {
                    called = true
                }
            }
            called
        ''')
    }

    @Test
    void testEmptyCatchWithoutFinally() {
        assert new GroovyShell().evaluate('''
            int m(int x) {
                try {
                    if (x < 0) throw new RuntimeException('x')
                    return x
                } catch (RuntimeException e) {
                }
                return -1
            }
            assert m(2) == 2
            assert m(-1) == -1
            m(2)
        ''') == 2
    }

    @Test
    void testTryCatchFallthroughWithoutFinally() {
        assert new GroovyShell().evaluate('''
            int m(int x) {
                try {
                    x = x + 1
                } catch (Exception e) {
                    x = -1
                }
                return x
            }
            m(10)
        ''') == 11
    }

    @Test
    void testNestedTryCatchInsideFinallyRunsFinallyOnce() {
        // GROOVY-8229: outer finally must not be re-entered via an inner catch
        // that incorrectly covers the outer finally's inlined body.
        assert new GroovyShell().evaluate('''
            class TryCatchProblem {
                static int count = 0
                static void main(args) {
                    def cl = {
                        try {
                            try {
                                assert count == 0
                            } catch (Throwable e) { }
                        } finally {
                            check()
                        }
                    }
                    cl()
                }
                static void check() {
                    throw new UnsupportedOperationException("check call count: ${++count}")
                }
            }
            try {
                TryCatchProblem.main()
                return 'no throw'
            } catch (UnsupportedOperationException e) {
                return e.message
            }
        ''') == 'check call count: 1'
    }

    //--------------------------------------------------------------------------

    private List<TryCatchBlockNode> tryCatchBlocks(final String methodName) {
        assert classBytes != null: 'compile(...) first'
        def cn = new ClassNode()
        new ClassReader(classBytes).accept(cn, ClassReader.SKIP_DEBUG)
        MethodNode mn = cn.methods.find { it.name == methodName }
        assert mn != null: "method ${methodName} not found"
        mn.tryCatchBlocks
    }

    private static boolean hasIdentityCatchAllRethrow(final InstructionSequence seq) {
        // Pattern produced by the old empty-finally path: store throwable, reload, athrow
        // with no intervening finally body (adjacent ALOAD/ATHROW after ASTORE).
        def ops = opcodeNames(seq)
        for (int i = 0; i < ops.size() - 2; i += 1) {
            if (ops[i].startsWith('ASTORE') && ops[i + 1].startsWith('ALOAD') && ops[i + 2] == 'ATHROW') {
                return true
            }
        }
        false
    }

    private static List<String> opcodeNames(final InstructionSequence seq) {
        seq.instructions.findAll { it && !it.startsWith('//') && !it.startsWith('L') && it != '--BEGIN--' && it != '--END--' }
                .collect { line ->
                    def token = line.tokenize()[0]
                    token.startsWith('FRAME') ? 'FRAME' : token
                }
    }
}
