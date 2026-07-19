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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test
import org.objectweb.asm.Attribute
import org.objectweb.asm.ConstantDynamic
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LookupSwitchInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TableSwitchInsnNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.util.Printer
import org.objectweb.asm.util.Textifier
import org.objectweb.asm.util.TraceMethodVisitor

import static groovy.test.GroovyAssert.shouldFail
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.RETURN

final class PeepholeOptimizingMethodVisitorTest extends AbstractBytecodeTestCase {

    @Test
    void discardedGroovyCastKeepsSideEffect() {
        // SC emits invokedynamic cast (not bare CHECKCAST) for a general Groovy
        // cast; the call must not be dropped when the result is discarded.
        def err = shouldFail(GroovyCastException, '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                (Thread) o
            }
            m(42)
        ''')
        assert err.message.contains('Thread')
    }

    @Test
    void discardedGroovyCastSucceedsWhenConversionApplies() {
        assertScript '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                (String) o
            }
            m(42) // Integer → String via Groovy cast
            m('ok')
        '''
    }

    @Test
    void discardedCheckcastAfterInstanceofIsPreservedInBytecode() {
        // After instanceof String, SC emits a bare CHECKCAST for a discarded
        // (String) o. The peephole must not erase that CHECKCAST (POP path).
        def bytecode = compile(method: 'm', '''
            @groovy.transform.CompileStatic
            void m(Object o) {
                if (o instanceof String) {
                    (String) o
                }
            }
        ''')
        assert bytecode.hasStrictSequence([
            'CHECKCAST java/lang/String',
            'POP',
        ])
    }

    @Test
    void compactsNumericConstants() {
        def bytecode = sequenceFor {
            visitLdcInsn(-1)
            visitLdcInsn(5)
            visitLdcInsn(6)
            visitIntInsn(Opcodes.SIPUSH, 120)
            visitLdcInsn(0L)
            visitLdcInsn(1L)
            visitLdcInsn(2L)
            visitLdcInsn(0f)
            visitLdcInsn(1f)
            visitLdcInsn(2f)
            visitLdcInsn(0d)
            visitLdcInsn(1d)
            visitVarInsn(Opcodes.DSTORE, 0)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_M1',
                'ICONST_5',
                'BIPUSH 6',
                'BIPUSH 120',
                'LCONST_0',
                'LCONST_1',
                'LDC 2L',
                'FCONST_0',
                'FCONST_1',
                'FCONST_2',
                'DCONST_0',
                'DCONST_1',
                'DSTORE 0',
                'RETURN',
        ]
    }

    @Test
    void preservesSignedZeroConstants() {
        def methodNode = methodNodeFor {
            visitLdcInsn(-0.0f)
            visitVarInsn(Opcodes.FSTORE, 0)
            visitLdcInsn(-0.0d)
            visitVarInsn(Opcodes.DSTORE, 1)
            visitInsn(RETURN)
        }
        def constants = executableInstructions(methodNode).findAll { it instanceof LdcInsnNode }*.cst
        def bytecode = traceSequence(methodNode)

        assert constants.size() == 2
        assert constants[0].equals(-0.0f)
        assert constants[1].equals(-0.0d)
        assert opcodeLines(bytecode) == ['LDC -0.0F', 'FSTORE 0', 'LDC -0.0D', 'DSTORE 1', 'RETURN']
    }

    @Test
    void rewritesIntegerComparisonsAgainstZero() {
        def zeroComparisons = [
                (Opcodes.IF_ICMPEQ): 'IFEQ',
                (Opcodes.IF_ICMPNE): 'IFNE',
                (Opcodes.IF_ICMPGE): 'IFGE',
                (Opcodes.IF_ICMPGT): 'IFGT',
                (Opcodes.IF_ICMPLE): 'IFLE',
                (Opcodes.IF_ICMPLT): 'IFLT',
        ]

        zeroComparisons.each { opcode, expected ->
            def label = new Label()
            def bytecode = sequenceFor('(I)V') {
                visitVarInsn(Opcodes.ILOAD, 0)
                visitLdcInsn(0)
                visitJumpInsn(opcode, label)
                visitInsn(RETURN)
                visitLabel(label)
                visitInsn(RETURN)
            }
            def lines = opcodeLines(bytecode)

            assert lines.size() == 4
            assert lines[0] == 'ILOAD 0'
            assert lines[1].startsWith(expected)
            assert lines[2..3] == ['RETURN', 'RETURN']
            assert !lines[1].startsWith(Printer.OPCODES[opcode])
        }
    }

    @Test
    void removesRedundantLoadsAndConstantPops() {
        def bytecode = sequenceFor('(Ljava/lang/Object;J)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.POP)
            visitVarInsn(Opcodes.LLOAD, 1)
            visitInsn(Opcodes.POP2)
            visitLdcInsn('unused')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void preservesIincWhenDroppingRedundantLoads() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['IINC 0 1', 'RETURN']
    }

    @Test
    void removesRedundantDupStorePops() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.DUP)
            visitVarInsn(Opcodes.ISTORE, 0)
            visitInsn(Opcodes.POP)
            visitLdcInsn(2L)
            visitInsn(Opcodes.DUP2)
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'VALUE', 'J')
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_1',
                'ISTORE 0',
                'LDC 2L',
                'PUTSTATIC Owner.VALUE : J',
                'RETURN',
        ]
    }

    @Test
    void lowersBigNumberConstantsBeforeEmission() {
        def bytecode = sequenceFor {
            visitLdcInsn(new BigDecimal('123.45'))
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'DECIMAL', 'Ljava/math/BigDecimal;')
            visitLdcInsn(new BigInteger('42'))
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'INTEGER', 'Ljava/math/BigInteger;')
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'NEW java/math/BigDecimal',
                'DUP',
                'LDC "123.45"',
                'INVOKESPECIAL java/math/BigDecimal.<init> (Ljava/lang/String;)V',
                'PUTSTATIC Owner.DECIMAL : Ljava/math/BigDecimal;',
                'NEW java/math/BigInteger',
                'DUP',
                'LDC "42"',
                'INVOKESPECIAL java/math/BigInteger.<init> (Ljava/lang/String;)V',
                'PUTSTATIC Owner.INTEGER : Ljava/math/BigInteger;',
                'RETURN',
        ]
    }

    @Test
    void compactsBufferedPrimitiveOpcodesAndLargeFallbacks() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_M1)
            visitInsn(Opcodes.ICONST_3)
            visitInsn(Opcodes.ICONST_4)
            visitInsn(Opcodes.ICONST_5)
            visitInsn(Opcodes.LCONST_1)
            visitInsn(Opcodes.FCONST_1)
            visitInsn(Opcodes.FCONST_2)
            visitInsn(Opcodes.DCONST_1)
            visitLdcInsn(300)
            visitLdcInsn(70_000)
            visitVarInsn(Opcodes.ISTORE, 0)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_M1',
                'ICONST_3',
                'ICONST_4',
                'ICONST_5',
                'LCONST_1',
                'FCONST_1',
                'FCONST_2',
                'DCONST_1',
                'SIPUSH 300',
                'LDC 70000',
                'ISTORE 0',
                'RETURN',
        ]
    }

    @Test
    void flushesPendingStateBeforeStructuralVisitors() {
        def defaultLabel = new Label()
        def caseLabel = new Label()
        def methodNode = methodNodeFor {
            visitLdcInsn(1)
            visitAttribute(new Attribute('Test') { })
            visitLdcInsn(new ConstantDynamic('dyn', 'I', new Handle(Opcodes.H_INVOKESTATIC, 'Owner', 'bootstrap', '()V', false)))
            visitLdcInsn(2)
            visitFrame(Opcodes.F_SAME, 0, null, 0, null)
            visitLdcInsn(3)
            visitTableSwitchInsn(0, 0, defaultLabel, caseLabel)
            visitLabel(caseLabel)
            visitLdcInsn(4)
            visitLookupSwitchInsn(defaultLabel, [1] as int[], [caseLabel] as Label[])
            visitLabel(defaultLabel)
            visitLdcInsn(1)
            visitMultiANewArrayInsn('[[I', 1)
            visitInsn(RETURN)
        }
        def instructions = executableInstructions(methodNode)
        def bytecode = traceSequence(methodNode)

        assert render(instructions[0]) == 'ICONST_1'
        assert instructions[1] instanceof LdcInsnNode && ((LdcInsnNode) instructions[1]).cst instanceof ConstantDynamic
        assert bytecode.hasStrictSequence(['ICONST_2', 'FRAME SAME', 'ICONST_3', 'TABLESWITCH'])
        assert bytecode.hasSequence(['ICONST_4', 'LOOKUPSWITCH'])
        assert bytecode.hasSequence(['ICONST_1', 'MULTIANEWARRAY'])
        assert instructions[4] instanceof TableSwitchInsnNode
        assert instructions[6] instanceof LookupSwitchInsnNode
        assert instructions[8] instanceof MultiANewArrayInsnNode
        assert render(instructions[9]) == 'RETURN'
        assert methodNode.attrs*.type == ['Test']
    }

    @Test
    void flushesPendingStateBeforeTypeAnnotationVisitors() {
        def start = new Label()
        def end = new Label()
        def handler = new Label()
        def methodNode = methodNodeFor('(Ljava/lang/Object;)V') {
            visitLabel(start)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsnAnnotation(0, null, 'Ljava/lang/Deprecated;', true)?.visitEnd()
            visitLabel(end)
            visitTryCatchBlock(start, end, handler, 'java/lang/RuntimeException')
            visitTryCatchAnnotation(1, null, 'Ljava/lang/Deprecated;', true)?.visitEnd()
            visitLocalVariable('value', 'Ljava/lang/Object;', null, start, end, 0)
            visitLocalVariableAnnotation(2, null, [start] as Label[], [end] as Label[], [0] as int[], 'Ljava/lang/Deprecated;', true)?.visitEnd()
            visitLabel(handler)
            visitInsn(RETURN)
        }
        def bytecode = traceSequence(methodNode)
        def checkcast = executableInstructions(methodNode).find { it instanceof TypeInsnNode }

        assert opcodeLines(bytecode) == ['ALOAD 0', 'CHECKCAST java/lang/String', 'RETURN']
        assert checkcast.visibleTypeAnnotations?.size() == 1
        assert methodNode.tryCatchBlocks.size() == 1
        assert methodNode.tryCatchBlocks[0].visibleTypeAnnotations?.size() == 1
        assert methodNode.visibleLocalVariableAnnotations?.size() == 1
    }

    @Test
    void flushesIncrementedLoadsWhenAnotherInstructionNeedsThem() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'IINC 0 1', 'RETURN']
    }

    @Test
    void dropsIncrementedLoadsOnReturn() {
        def bytecode = sequenceFor('(I)V', { ->
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitInsn(RETURN)
        })

        assert opcodeLines(bytecode) == ['IINC 0 1', 'RETURN']
    }

    @Test
    void preservesLoadAndAttachedCheckcastBeforePop() {
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // Discarded cast must keep its ClassCastException side effect (same rule as void RETURN).
        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void preservesStandaloneCheckcastBeforePop() {
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.NOP) // force the load to flush so CHECKCAST is standalone
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // Standalone cast is preserved; ALOAD is already flushed (NOP boundary), so it stays.
        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'NOP',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void rewritesNullComparisonsAgainstAconstNull() {
        def nullComparisons = [
                (Opcodes.IF_ACMPEQ): 'IFNULL',
                (Opcodes.IF_ACMPNE): 'IFNONNULL',
        ]

        nullComparisons.each { opcode, expected ->
            def label = new Label()
            def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
                visitVarInsn(Opcodes.ALOAD, 0)
                visitInsn(Opcodes.ACONST_NULL)
                visitJumpInsn(opcode, label)
                visitInsn(RETURN)
                visitLabel(label)
                visitInsn(RETURN)
            }
            def lines = opcodeLines(bytecode)

            assert lines.size() == 4
            assert lines[0] == 'ALOAD 0'
            assert lines[1].startsWith(expected)
            assert lines[2..3] == ['RETURN', 'RETURN']
            assert !lines[1].startsWith(Printer.OPCODES[opcode])
        }
    }

    @Test
    void removesBareDupBeforeMatchingPop() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.DUP)
            visitInsn(Opcodes.POP)
            visitLdcInsn(2L)
            visitInsn(Opcodes.DUP2)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_1',
                'LDC 2L',
                'RETURN',
        ]
    }

    @Test
    void preservesAttachedCheckcastWhenTheValueIsUsed() {
        def bytecode = sequenceFor('(Ljava/lang/Object;)Ljava/lang/String;') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.ARETURN)
        }

        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'CHECKCAST java/lang/String',
                'ARETURN',
        ]
    }

    @Test
    void flushesPendingDupStoresWhenTheValueIsKept() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.DUP)
            visitVarInsn(Opcodes.ISTORE, 0)
            visitInsn(Opcodes.NOP)
            visitLdcInsn(3L)
            visitInsn(Opcodes.DUP2)
            visitVarInsn(Opcodes.LSTORE, 1)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_1',
                'DUP',
                'ISTORE 0',
                'NOP',
                'LDC 3L',
                'LSTORE 1',
                'RETURN',
        ]
    }

    @Test
    void preservesStandaloneCheckcastBeforeReturn() {
        // POP and void RETURN use the same rule: keep CHECKCAST, discard the value.
        def withPop = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.NOP) // flush ALOAD so the cast is standalone
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }
        def returnOnly = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.NOP)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(RETURN)
        }

        assert opcodeLines(withPop) == [
                'ALOAD 0',
                'NOP',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
        assert opcodeLines(returnOnly) == [
                'ALOAD 0',
                'NOP',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void preservesAttachedCheckcastSideEffectBeforeReturn() {
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(RETURN)
        }

        // Cast is kept for its ClassCastException side effect; POP keeps void return valid.
        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void wrapIsIdempotentAndPropagatesNull() {
        assert PeepholeOptimizingMethodVisitor.wrap(null) == null

        def methodNode = new MethodNode(CompilerConfiguration.ASM_API_VERSION, ACC_PUBLIC | ACC_STATIC, 'sample', '()V', null, null)
        def once = PeepholeOptimizingMethodVisitor.wrap(methodNode)
        assert once instanceof PeepholeOptimizingMethodVisitor
        assert PeepholeOptimizingMethodVisitor.wrap(once).is(once)
    }

    @Test
    void printTraceBytecodeFindsNestedTracer() {
        def textifier = new Textifier()
        def tracer = new TraceMethodVisitor(textifier)
        def peephole = new PeepholeOptimizingMethodVisitor(tracer)
        peephole.visitCode()
        peephole.visitInsn(Opcodes.ICONST_1)
        peephole.visitVarInsn(Opcodes.ISTORE, 0) // keep the constant live for tracing
        peephole.visitInsn(RETURN)
        peephole.visitMaxs(0, 0)
        peephole.visitEnd()

        def out = new StringWriter()
        def printer = new PrintWriter(out)
        assert PeepholeOptimizingMethodVisitor.printTraceBytecode(peephole, printer)
        printer.flush()
        assert out.toString().contains('ICONST_1')
        assert out.toString().contains('ISTORE')
        assert out.toString().contains('RETURN')

        assert !PeepholeOptimizingMethodVisitor.printTraceBytecode(null, printer)
        assert !PeepholeOptimizingMethodVisitor.printTraceBytecode(new MethodNode(CompilerConfiguration.ASM_API_VERSION, ACC_PUBLIC | ACC_STATIC, 'x', '()V', null, null), printer)
    }

    @Test
    void classVisitorWrapsEveryMethodAndSkipsNullDelegates() {
        def written = []
        def delegate = new org.objectweb.asm.ClassVisitor(CompilerConfiguration.ASM_API_VERSION) {
            @Override
            MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                if (name == 'skip') {
                    return null
                }
                def mn = new MethodNode(CompilerConfiguration.ASM_API_VERSION, access, name, descriptor, signature, exceptions)
                written << mn
                return mn
            }
        }
        def peepholeClass = new PeepholeOptimizingClassVisitor(delegate)

        def optimized = peepholeClass.visitMethod(ACC_PUBLIC | ACC_STATIC, 'run', '()V', null, null)
        assert optimized instanceof PeepholeOptimizingMethodVisitor
        optimized.visitCode()
        optimized.visitLdcInsn(0)
        optimized.visitVarInsn(Opcodes.ISTORE, 0)
        optimized.visitInsn(RETURN)
        optimized.visitMaxs(0, 0)
        optimized.visitEnd()

        assert peepholeClass.visitMethod(ACC_PUBLIC | ACC_STATIC, 'skip', '()V', null, null) == null
        assert written.size() == 1
        assert opcodeLines(traceSequence(written[0])) == ['ICONST_0', 'ISTORE 0', 'RETURN']
    }

    @Test
    void doesNotRewriteNonMatchingCompareJumps() {
        def zeroLabel = new Label()
        def zeroWithIfnull = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitLdcInsn(0)
            visitJumpInsn(Opcodes.IFNULL, zeroLabel)
            visitInsn(RETURN)
            visitLabel(zeroLabel)
            visitInsn(RETURN)
        }
        assert opcodeLines(zeroWithIfnull)[1] == 'ICONST_0'
        assert opcodeLines(zeroWithIfnull)[2].startsWith('IFNULL')

        def nullLabel = new Label()
        def nullWithIfeq = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.ACONST_NULL)
            visitJumpInsn(Opcodes.IFEQ, nullLabel)
            visitInsn(RETURN)
            visitLabel(nullLabel)
            visitInsn(RETURN)
        }
        assert opcodeLines(nullWithIfeq)[1] == 'ACONST_NULL'
        assert opcodeLines(nullWithIfeq)[2].startsWith('IFEQ')
    }

    @Test
    void preservesMismatchedPopSizes() {
        def bytecode = sequenceFor('(IJ)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitInsn(Opcodes.POP2)
            visitVarInsn(Opcodes.LLOAD, 1)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ILOAD 0',
                'POP2',
                'LLOAD 1',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void attachesCheckcastToReferenceConstantsAndPreservesDiscardedCasts() {
        def type = org.objectweb.asm.Type.getType(String)
        def handle = new Handle(Opcodes.H_INVOKESTATIC, 'Owner', 'boot', '()V', false)
        def bytecode = sequenceFor {
            visitLdcInsn('text')
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitLdcInsn(type)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/Class')
            visitInsn(Opcodes.POP)
            visitLdcInsn(handle)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/invoke/MethodHandle')
            visitInsn(Opcodes.POP)
            visitInsn(Opcodes.ACONST_NULL)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // Casts attach to reference constants but are kept on discard (CCE side effect).
        assert opcodeLines(bytecode) == [
                'LDC "text"',
                'CHECKCAST java/lang/String',
                'POP',
                'LDC Ljava/lang/String;.class',
                'CHECKCAST java/lang/Class',
                'POP',
                'LDC Owner.boot()V',
                'CHECKCAST java/lang/invoke/MethodHandle',
                'POP',
                'ACONST_NULL',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void flushesBeforeMethodAndInvokeDynamicCalls() {
        def handle = new Handle(Opcodes.H_INVOKESTATIC, 'Owner', 'bootstrap', '()Ljava/lang/invoke/CallSite;', false)
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Object', 'toString', '()Ljava/lang/String;', false)
            visitInsn(Opcodes.POP)
            visitLdcInsn(1)
            visitInvokeDynamicInsn('dyn', '()I', handle)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        def lines = opcodeLines(bytecode)
        assert lines[0] == 'ALOAD 0'
        assert lines[1] == 'INVOKEVIRTUAL java/lang/Object.toString ()Ljava/lang/String;'
        assert lines[2] == 'POP'
        assert lines[3] == 'ICONST_1'
        assert lines[4].startsWith('INVOKEDYNAMIC dyn')
        assert lines[-2] == 'POP'
        assert lines[-1] == 'RETURN'
    }

    @Test
    void flushesPendingLoadOnLineNumberAndNonCheckcastTypeInsn() {
        def start = new Label()
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitLabel(start)
            visitVarInsn(Opcodes.ALOAD, 0)
            visitLineNumber(42, start)
            visitTypeInsn(Opcodes.INSTANCEOF, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'INSTANCEOF java/lang/String',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void doesNotAttachSecondCheckcastAndFlushesIincOnConflict() {
        def chainedCasts = sequenceFor('(Ljava/lang/Object;)Ljava/lang/Object;') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/CharSequence')
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.ARETURN)
        }
        assert opcodeLines(chainedCasts) == [
                'ALOAD 0',
                'CHECKCAST java/lang/CharSequence',
                'CHECKCAST java/lang/String',
                'ARETURN',
        ]

        def secondIincFlushes = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitIincInsn(0, 2) // already has IINC → flush load+first IINC, emit second
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }
        assert opcodeLines(secondIincFlushes) == [
                'ILOAD 0',
                'IINC 0 1',
                'IINC 0 2',
                'POP',
                'RETURN',
        ]

        def differentVarIincFlushes = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(1, 1) // different variable → flush load, emit IINC
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }
        assert opcodeLines(differentVarIincFlushes) == [
                'ILOAD 0',
                'IINC 1 1',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void flushesPendingDupWhenStoreIsNotEligibleForCollapse() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.DUP)
            visitFieldInsn(Opcodes.PUTFIELD, 'Owner', 'value', 'I') // not PUTSTATIC → flush dup
            visitInsn(Opcodes.ICONST_2)
            visitInsn(Opcodes.DUP)
            visitVarInsn(Opcodes.ISTORE, 0)
            visitInsn(Opcodes.NOP) // keep the duplicate
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_1',
                'DUP',
                'PUTFIELD Owner.value : I',
                'ICONST_2',
                'DUP',
                'ISTORE 0',
                'NOP',
                'RETURN',
        ]
    }

    @Test
    void passesConstantDynamicThroughWithoutBuffering() {
        def handle = new Handle(Opcodes.H_INVOKESTATIC, 'Owner', 'bootstrap', '()I', false)
        def dynamic = new ConstantDynamic('answer', 'I', handle)
        def bytecode = sequenceFor {
            visitLdcInsn(dynamic)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // Bootstrap side effects must not be dropped by dead-load elimination.
        def lines = opcodeLines(bytecode)
        assert lines.size() == 3
        assert lines[0].startsWith('LDC')
        assert lines[1] == 'POP'
        assert lines[2] == 'RETURN'
    }

    @Test
    void emitsNonSpecializedNumericConstantsViaLdc() {
        def bytecode = sequenceFor {
            visitLdcInsn(3L)
            visitLdcInsn(4f)
            visitLdcInsn(5d)
            visitLdcInsn('literal')
            visitVarInsn(Opcodes.ASTORE, 0)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'LDC 3L',
                'LDC 4.0F',
                'LDC 5.0D',
                'LDC "literal"',
                'ASTORE 0',
                'RETURN',
        ]
    }

    @Test
    void passesThroughNewarrayIntInsn() {
        def bytecode = sequenceFor {
            visitLdcInsn(2)
            visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_2',
                'NEWARRAY T_INT',
                'POP',
                'RETURN',
        ]
    }

    @Test
    void dropsDeadFloatAndDoubleLoads() {
        def bytecode = sequenceFor('(FD)V') {
            visitVarInsn(Opcodes.FLOAD, 0)
            visitInsn(Opcodes.POP)
            visitVarInsn(Opcodes.DLOAD, 1)
            visitInsn(Opcodes.POP2)
            visitInsn(Opcodes.FCONST_0)
            visitInsn(Opcodes.POP)
            visitInsn(Opcodes.DCONST_0)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void collapsesDup2WithWideVariableStore() {
        def bytecode = sequenceFor {
            visitLdcInsn(9L)
            visitInsn(Opcodes.DUP2)
            visitVarInsn(Opcodes.LSTORE, 0)
            visitInsn(Opcodes.POP2)
            visitLdcInsn(8.0d)
            visitInsn(Opcodes.DUP2)
            visitVarInsn(Opcodes.DSTORE, 2)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'LDC 9L',
                'LSTORE 0',
                'LDC 8.0D',
                'DSTORE 2',
                'RETURN',
        ]
    }

    @Test
    void collapsesDupStorePopForReferenceStaticField() {
        def bytecode = sequenceFor {
            visitLdcInsn('value')
            visitInsn(Opcodes.DUP)
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'NAME', 'Ljava/lang/String;')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'LDC "value"',
                'PUTSTATIC Owner.NAME : Ljava/lang/String;',
                'RETURN',
        ]
    }

    @Test
    void preservesMismatchedDupAndPopSizes() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitInsn(Opcodes.DUP)
            visitInsn(Opcodes.POP2) // size mismatch → keep DUP
            visitLdcInsn(2L)
            visitInsn(Opcodes.DUP2)
            visitVarInsn(Opcodes.ISTORE, 0) // wide dup + narrow store → no collapse on later pop
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ICONST_1',
                'DUP',
                'POP2',
                'LDC 2L',
                'DUP2',
                'ISTORE 0',
                'POP2',
                'RETURN',
        ]
    }

    @Test
    void preservesAttachedCheckcastOnConstantBeforePop() {
        def withPop = sequenceFor('(Ljava/lang/Object;)V') {
            visitLdcInsn('x')
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }
        // Cast side effect retained even for a reference constant (consistent with ALOAD path).
        assert opcodeLines(withPop) == [
                'LDC "x"',
                'CHECKCAST java/lang/String',
                'POP',
                'RETURN',
        ]

        // POP2 does not match a one-slot cast result; cast is flushed unchanged with POP2.
        def standalonePop2 = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.NOP) // flush ALOAD so the cast is standalone
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }
        assert opcodeLines(standalonePop2) == [
                'ALOAD 0',
                'NOP',
                'CHECKCAST java/lang/String',
                'POP2',
                'RETURN',
        ]
    }

    // --- box/unbox cancellation and Boolean folding (from gjit) ---

    @Test
    void foldsBooleanTrueWithBooleanUnbox() {
        def bytecode = sequenceFor('()Z') {
            visitFieldInsn(Opcodes.GETSTATIC, 'java/lang/Boolean', 'TRUE', 'Ljava/lang/Boolean;')
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'booleanUnbox', '(Ljava/lang/Object;)Z', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ICONST_1', 'IRETURN']
    }

    @Test
    void foldsBooleanFalseWithBooleanValue() {
        def bytecode = sequenceFor('()Z') {
            visitFieldInsn(Opcodes.GETSTATIC, 'java/lang/Boolean', 'FALSE', 'Ljava/lang/Boolean;')
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Boolean', 'booleanValue', '()Z', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ICONST_0', 'IRETURN']
    }

    @Test
    void preservesBooleanConstantWhenNotUnboxed() {
        def bytecode = sequenceFor('()Ljava/lang/Boolean;') {
            visitFieldInsn(Opcodes.GETSTATIC, 'java/lang/Boolean', 'TRUE', 'Ljava/lang/Boolean;')
            visitInsn(Opcodes.ARETURN)
        }

        assert opcodeLines(bytecode) == [
                'GETSTATIC java/lang/Boolean.TRUE : Ljava/lang/Boolean;',
                'ARETURN',
        ]
    }

    @Test
    void dropsDeadBooleanConstantOnPop() {
        def bytecode = sequenceFor('()V') {
            visitFieldInsn(Opcodes.GETSTATIC, 'java/lang/Boolean', 'TRUE', 'Ljava/lang/Boolean;')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void cancelsIntegerValueOfWithIntValue() {
        def bytecode = sequenceFor('(I)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Integer', 'intValue', '()I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'IRETURN']
    }

    @Test
    void cancelsLongValueOfWithLongValue() {
        def bytecode = sequenceFor('(J)J') {
            visitVarInsn(Opcodes.LLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Long', 'valueOf', '(J)Ljava/lang/Long;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Long', 'longValue', '()J', false)
            visitInsn(Opcodes.LRETURN)
        }

        assert opcodeLines(bytecode) == ['LLOAD 0', 'LRETURN']
    }

    @Test
    void cancelsDttBoxWithMatchingUnbox() {
        def bytecode = sequenceFor('(D)D') {
            visitVarInsn(Opcodes.DLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'box', '(D)Ljava/lang/Object;', false)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'doubleUnbox', '(Ljava/lang/Object;)D', false)
            visitInsn(Opcodes.DRETURN)
        }

        assert opcodeLines(bytecode) == ['DLOAD 0', 'DRETURN']
    }

    @Test
    void dropsBoxedValueDiscardedByPop() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // Box and pure ILOAD are both dead once the boxed value is discarded.
        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void dropsWideBoxedValueDiscardedByPop() {
        def bytecode = sequenceFor('(J)V') {
            visitVarInsn(Opcodes.LLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Long', 'valueOf', '(J)Ljava/lang/Long;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void dropsWideBoxedValueDiscardedByPop2() {
        def bytecode = sequenceFor('(D)V') {
            visitVarInsn(Opcodes.DLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'box', '(D)Ljava/lang/Object;', false)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        // POP2 matches the wide primitive under the box → drop box and producer.
        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void treatsPop2OnNarrowBoxedValueAsTwoSlotDiscard() {
        // Stack before POP2: [underneath, boxed]. Cancel pure ILOAD under the box, POP the underneath.
        def bytecode = sequenceFor('(I)V') {
            visitInsn(Opcodes.ICONST_0)
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['ICONST_0', 'POP', 'RETURN']
    }

    @Test
    void dropsBoxedValueDiscardedByVoidReturn() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void dropsBoxedConstantDiscardedByPop() {
        def bytecode = sequenceFor {
            visitInsn(Opcodes.ICONST_1)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['RETURN']
    }

    @Test
    void preservesIincWhenBoxedLoadIsDiscarded() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // IINC is a side effect of the load window and must survive dead-box elimination.
        assert opcodeLines(bytecode) == ['IINC 0 1', 'RETURN']
    }

    @Test
    void popsPrimitiveWhenBoxedProducerAlreadyFlushed() {
        // NOP flushes ILOAD before valueOf is seen, so only the box is pending.
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitInsn(Opcodes.NOP)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'NOP', 'POP', 'RETURN']
    }

    @Test
    void popsWidePrimitiveWhenBoxedProducerAlreadyFlushed() {
        def bytecode = sequenceFor('(J)V') {
            visitVarInsn(Opcodes.LLOAD, 0)
            visitInsn(Opcodes.NOP)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Long', 'valueOf', '(J)Ljava/lang/Long;', false)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        // POP of boxed long → POP2 of the long still on the stack.
        assert opcodeLines(bytecode) == ['LLOAD 0', 'NOP', 'POP2', 'RETURN']
    }

    @Test
    void treatsPop2OnFlushedNarrowBoxAsTwoSlotDiscard() {
        def bytecode = sequenceFor('(I)V') {
            visitInsn(Opcodes.ICONST_0)
            visitVarInsn(Opcodes.ILOAD, 0)
            visitInsn(Opcodes.NOP) // flush ILOAD; int remains on stack under the later box
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.POP2)
            visitInsn(RETURN)
        }

        // Drop box; POP2 removes the flushed int and the ICONST underneath.
        assert opcodeLines(bytecode) == ['ICONST_0', 'ILOAD 0', 'NOP', 'POP2', 'RETURN']
    }

    @Test
    void cancelsBoxUnboxAfterFlushedProducer() {
        def bytecode = sequenceFor('(I)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitInsn(Opcodes.NOP)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Integer', 'intValue', '()I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'NOP', 'IRETURN']
    }

    @Test
    void flushesBoxWhenValueIsStored() {
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitVarInsn(Opcodes.ASTORE, 1)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ILOAD 0',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'ASTORE 1',
                'RETURN',
        ]
    }

    @Test
    void rewritesIntegerValueOfWithIntegerLongValueToI2L() {
        // Same-wrapper Number conversion: Integer.valueOf; Integer.longValue → I2L
        def bytecode = sequenceFor('(I)J') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Integer', 'longValue', '()J', false)
            visitInsn(Opcodes.LRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'I2L', 'LRETURN']
    }

    @Test
    void doesNotBufferNonPrimitiveValueOf() {
        def bytecode = sequenceFor('(Ljava/lang/String;)Ljava/lang/Integer;') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(Ljava/lang/String;)Ljava/lang/Integer;', false)
            visitInsn(Opcodes.ARETURN)
        }

        assert opcodeLines(bytecode) == [
                'ALOAD 0',
                'INVOKESTATIC java/lang/Integer.valueOf (Ljava/lang/String;)Ljava/lang/Integer;',
                'ARETURN',
        ]
    }

    @Test
    void rewritesIntegerValueOfWithLongLongValueToI2L() {
        // Cross-wrapper unbox after a pending box: the value is known to be a
        // boxed int, so Integer.valueOf; Long.longValue compresses to I2L
        // (same as long l = intExpr). Real classgen usually emits I2L directly;
        // this recovers that shape when an intermediate box/unbox was produced.
        def bytecode = sequenceFor('(I)J') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Long', 'longValue', '()J', false)
            visitInsn(Opcodes.LRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'I2L', 'LRETURN']
    }

    @Test
    void doesNotFoldBooleanConstantWithNonUnboxCall() {
        def bytecode = sequenceFor('()Ljava/lang/String;') {
            visitFieldInsn(Opcodes.GETSTATIC, 'java/lang/Boolean', 'TRUE', 'Ljava/lang/Boolean;')
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Boolean', 'toString', '()Ljava/lang/String;', false)
            visitInsn(Opcodes.ARETURN)
        }

        assert opcodeLines(bytecode) == [
                'GETSTATIC java/lang/Boolean.TRUE : Ljava/lang/Boolean;',
                'INVOKEVIRTUAL java/lang/Boolean.toString ()Ljava/lang/String;',
                'ARETURN',
        ]
    }

    @Test
    void cancelsFloatAndDoubleValueOfPairs() {
        def floatBytecode = sequenceFor('(F)F') {
            visitVarInsn(Opcodes.FLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Float', 'valueOf', '(F)Ljava/lang/Float;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Float', 'floatValue', '()F', false)
            visitInsn(Opcodes.FRETURN)
        }
        def doubleBytecode = sequenceFor('(D)D') {
            visitVarInsn(Opcodes.DLOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Double', 'valueOf', '(D)Ljava/lang/Double;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Double', 'doubleValue', '()D', false)
            visitInsn(Opcodes.DRETURN)
        }

        assert opcodeLines(floatBytecode) == ['FLOAD 0', 'FRETURN']
        assert opcodeLines(doubleBytecode) == ['DLOAD 0', 'DRETURN']
    }

    @Test
    void cancelsBooleanByteCharShortValueOfPairs() {
        def cases = [
                ['java/lang/Boolean', 'Z', 'booleanValue', Opcodes.ILOAD, Opcodes.IRETURN],
                ['java/lang/Byte', 'B', 'byteValue', Opcodes.ILOAD, Opcodes.IRETURN],
                ['java/lang/Character', 'C', 'charValue', Opcodes.ILOAD, Opcodes.IRETURN],
                ['java/lang/Short', 'S', 'shortValue', Opcodes.ILOAD, Opcodes.IRETURN],
        ]
        cases.each { owner, prim, unboxName, load, ret ->
            def bytecode = sequenceFor("($prim)$prim") {
                visitVarInsn(load, 0)
                visitMethodInsn(Opcodes.INVOKESTATIC, owner, 'valueOf', "($prim)L${owner};", false)
                visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, unboxName, "()$prim", false)
                visitInsn(ret)
            }
            assert opcodeLines(bytecode) == ["${Printer.OPCODES[load]} 0", Printer.OPCODES[ret]]
        }
    }

    @Test
    void cancelsAllDttBoxUnboxPairs() {
        def cases = [
                ['Z', 'booleanUnbox', Opcodes.ILOAD, Opcodes.IRETURN],
                ['B', 'byteUnbox', Opcodes.ILOAD, Opcodes.IRETURN],
                ['C', 'charUnbox', Opcodes.ILOAD, Opcodes.IRETURN],
                ['S', 'shortUnbox', Opcodes.ILOAD, Opcodes.IRETURN],
                ['I', 'intUnbox', Opcodes.ILOAD, Opcodes.IRETURN],
                ['J', 'longUnbox', Opcodes.LLOAD, Opcodes.LRETURN],
                ['F', 'floatUnbox', Opcodes.FLOAD, Opcodes.FRETURN],
                ['D', 'doubleUnbox', Opcodes.DLOAD, Opcodes.DRETURN],
        ]
        cases.each { prim, unboxName, load, ret ->
            def bytecode = sequenceFor("($prim)$prim") {
                visitVarInsn(load, 0)
                visitMethodInsn(Opcodes.INVOKESTATIC,
                        'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                        'box', "($prim)Ljava/lang/Object;", false)
                visitMethodInsn(Opcodes.INVOKESTATIC,
                        'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                        unboxName, "(Ljava/lang/Object;)$prim", false)
                visitInsn(ret)
            }
            assert opcodeLines(bytecode) == ["${Printer.OPCODES[load]} 0", Printer.OPCODES[ret]]
        }
    }

    @Test
    void cancelsDttBoxWithMatchingWrapperUnbox() {
        // Same primitive type: DTT.box then Integer.intValue is an identity on the int.
        def bytecode = sequenceFor('(I)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'box', '(I)Ljava/lang/Object;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Integer', 'intValue', '()I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'IRETURN']
    }

    @Test
    void cancelsValueOfWithMatchingDttUnbox() {
        def bytecode = sequenceFor('(I)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'intUnbox', '(Ljava/lang/Object;)I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'IRETURN']
    }

    @Test
    void rewritesDttBoxIntWithLongLongValueToI2L() {
        // DTT.box(int) then Long.longValue → I2L (known boxed int → long).
        def bytecode = sequenceFor('(I)J') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'box', '(I)Ljava/lang/Object;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Long', 'longValue', '()J', false)
            visitInsn(Opcodes.LRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'I2L', 'LRETURN']
    }

    @Test
    void rewritesNumericBoxUnboxConversions() {
        // Widening / narrowing pairs after valueOf (covers I/J/F/D conversion tables).
        def cases = [
                // load, boxOwner, boxPrim, unboxOwner, unboxName, retPrim, ret, expected middle opcodes
                [Opcodes.ILOAD, 'java/lang/Integer', 'I', 'java/lang/Double', 'doubleValue', 'D', Opcodes.DRETURN, ['I2D']],
                [Opcodes.ILOAD, 'java/lang/Integer', 'I', 'java/lang/Float', 'floatValue', 'F', Opcodes.FRETURN, ['I2F']],
                [Opcodes.ILOAD, 'java/lang/Integer', 'I', 'java/lang/Byte', 'byteValue', 'B', Opcodes.IRETURN, ['I2B']],
                [Opcodes.ILOAD, 'java/lang/Integer', 'I', 'java/lang/Short', 'shortValue', 'S', Opcodes.IRETURN, ['I2S']],
                [Opcodes.ILOAD, 'java/lang/Integer', 'I', 'java/lang/Character', 'charValue', 'C', Opcodes.IRETURN, ['I2C']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Integer', 'intValue', 'I', Opcodes.IRETURN, ['L2I']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Float', 'floatValue', 'F', Opcodes.FRETURN, ['L2F']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Double', 'doubleValue', 'D', Opcodes.DRETURN, ['L2D']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Byte', 'byteValue', 'B', Opcodes.IRETURN, ['L2I', 'I2B']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Short', 'shortValue', 'S', Opcodes.IRETURN, ['L2I', 'I2S']],
                [Opcodes.LLOAD, 'java/lang/Long', 'J', 'java/lang/Character', 'charValue', 'C', Opcodes.IRETURN, ['L2I', 'I2C']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Integer', 'intValue', 'I', Opcodes.IRETURN, ['F2I']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Long', 'longValue', 'J', Opcodes.LRETURN, ['F2L']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Double', 'doubleValue', 'D', Opcodes.DRETURN, ['F2D']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Byte', 'byteValue', 'B', Opcodes.IRETURN, ['F2I', 'I2B']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Short', 'shortValue', 'S', Opcodes.IRETURN, ['F2I', 'I2S']],
                [Opcodes.FLOAD, 'java/lang/Float', 'F', 'java/lang/Character', 'charValue', 'C', Opcodes.IRETURN, ['F2I', 'I2C']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Integer', 'intValue', 'I', Opcodes.IRETURN, ['D2I']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Long', 'longValue', 'J', Opcodes.LRETURN, ['D2L']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Float', 'floatValue', 'F', Opcodes.FRETURN, ['D2F']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Byte', 'byteValue', 'B', Opcodes.IRETURN, ['D2I', 'I2B']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Short', 'shortValue', 'S', Opcodes.IRETURN, ['D2I', 'I2S']],
                [Opcodes.DLOAD, 'java/lang/Double', 'D', 'java/lang/Character', 'charValue', 'C', Opcodes.IRETURN, ['D2I', 'I2C']],
                // int-slot source (byte) widened to long
                [Opcodes.ILOAD, 'java/lang/Byte', 'B', 'java/lang/Long', 'longValue', 'J', Opcodes.LRETURN, ['I2L']],
                // int-slot source to int-slot target of different kind (no-op on the int slot after box drop)
                [Opcodes.ILOAD, 'java/lang/Byte', 'B', 'java/lang/Integer', 'intValue', 'I', Opcodes.IRETURN, []],
        ]
        cases.each { load, boxOwner, boxPrim, unboxOwner, unboxName, retPrim, ret, middle ->
            def bytecode = sequenceFor("($boxPrim)$retPrim") {
                visitVarInsn(load, 0)
                visitMethodInsn(Opcodes.INVOKESTATIC, boxOwner, 'valueOf', "($boxPrim)L${boxOwner};", false)
                visitMethodInsn(Opcodes.INVOKEVIRTUAL, unboxOwner, unboxName, "()$retPrim", false)
                visitInsn(ret)
            }
            assert opcodeLines(bytecode) == ["${Printer.OPCODES[load]} 0", *middle, Printer.OPCODES[ret]]
        }

        // DTT.box + DTT.longUnbox after int → I2L
        def dtt = sequenceFor('(I)J') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'box', '(I)Ljava/lang/Object;', false)
            visitMethodInsn(Opcodes.INVOKESTATIC,
                    'org/codehaus/groovy/runtime/typehandling/DefaultTypeTransformation',
                    'longUnbox', '(Ljava/lang/Object;)J', false)
            visitInsn(Opcodes.LRETURN)
        }
        assert opcodeLines(dtt) == ['ILOAD 0', 'I2L', 'LRETURN']
    }

    @Test
    void rewritesSameTypeWrongOwnerUnboxAsIdentity() {
        // Integer.valueOf then Long.intValue: known boxed int, wrong owner → drop both.
        def bytecode = sequenceFor('(I)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Long', 'intValue', '()I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'IRETURN']
    }

    @Test
    void doesNotConvertBooleanBoxToNumericUnbox() {
        // Boolean is excluded from numeric conversion rewrites.
        def bytecode = sequenceFor('(Z)I') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Boolean', 'valueOf', '(Z)Ljava/lang/Boolean;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Integer', 'intValue', '()I', false)
            visitInsn(Opcodes.IRETURN)
        }

        assert opcodeLines(bytecode) == [
                'ILOAD 0',
                'INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;',
                'INVOKEVIRTUAL java/lang/Integer.intValue ()I',
                'IRETURN',
        ]
    }

    @Test
    void rewritesBoxUnboxConversionAfterFlushedProducer() {
        def bytecode = sequenceFor('(I)J') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitInsn(Opcodes.NOP) // flush ILOAD; int remains on stack under the box
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitMethodInsn(Opcodes.INVOKEVIRTUAL, 'java/lang/Long', 'longValue', '()J', false)
            visitInsn(Opcodes.LRETURN)
        }

        assert opcodeLines(bytecode) == ['ILOAD 0', 'NOP', 'I2L', 'LRETURN']
    }

    @Test
    void flushesPendingBoxBeforeStructuralBoundaries() {
        def label = new Label()
        def bytecode = sequenceFor('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitMethodInsn(Opcodes.INVOKESTATIC, 'java/lang/Integer', 'valueOf', '(I)Ljava/lang/Integer;', false)
            visitLabel(label)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == [
                'ILOAD 0',
                'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
                'POP',
                'RETURN',
        ]
    }

    private InstructionSequence sequenceFor(String descriptor = '()V', @DelegatesTo(MethodVisitor) Closure emitter) {
        traceSequence(methodNodeFor(descriptor, emitter))
    }

    private static InstructionSequence traceSequence(MethodNode methodNode) {
        def out = new StringWriter()
        def printer = new TraceMethodVisitor(new Textifier())
        methodNode.accept(printer)
        def writer = new PrintWriter(out)
        printer.p.print(writer)
        writer.flush()
        new InstructionSequence(instructions: out.toString().split('\n')*.trim().findAll())
    }

    private static MethodNode methodNodeFor(String descriptor = '()V', @DelegatesTo(MethodVisitor) Closure emitter) {
        def methodNode = new MethodNode(CompilerConfiguration.ASM_API_VERSION, ACC_PUBLIC | ACC_STATIC, 'sample', descriptor, null, null)
        MethodVisitor visitor = new PeepholeOptimizingMethodVisitor(methodNode)
        visitor.visitCode()
        emitter.delegate = visitor
        emitter.resolveStrategy = Closure.DELEGATE_FIRST
        if (emitter.maximumNumberOfParameters == 0) {
            emitter.call()
        } else {
            emitter.call(visitor)
        }
        visitor.visitMaxs(0, 0)
        visitor.visitEnd()
        methodNode
    }

    private static List<AbstractInsnNode> executableInstructions(MethodNode methodNode) {
        methodNode.instructions.toArray().findAll { AbstractInsnNode insn -> insn.opcode >= 0 }
    }

    private static List<String> opcodeLines(InstructionSequence bytecode) {
        bytecode.instructions.findAll { line ->
            Printer.OPCODES.any { opcode -> opcode != null && line.startsWith(opcode) }
        }
    }

    private static String render(AbstractInsnNode insn) {
        switch (insn) {
          case FieldInsnNode:
            return "${Printer.OPCODES[insn.opcode]} ${insn.owner}.${insn.name} : ${insn.desc}"
          case IincInsnNode:
            return "IINC ${insn.var} ${insn.incr}"
          case IntInsnNode:
            return "${Printer.OPCODES[insn.opcode]} ${insn.operand}"
          case LdcInsnNode:
            return "LDC ${formatConstant(insn.cst)}"
          case MethodInsnNode:
            return "${Printer.OPCODES[insn.opcode]} ${insn.owner}.${insn.name} ${insn.desc}"
          case TypeInsnNode:
            return "${Printer.OPCODES[insn.opcode]} ${insn.desc}"
          case VarInsnNode:
            return "${Printer.OPCODES[insn.opcode]} ${insn.var}"
          default:
            return Printer.OPCODES[insn.opcode]
        }
    }

    private static String formatConstant(Object constant) {
        if (constant instanceof Long) return "${constant}L"
        if (constant instanceof Float) return "${constant}f"
        if (constant instanceof Double) return "${constant}d"
        constant.toString()
    }
}
