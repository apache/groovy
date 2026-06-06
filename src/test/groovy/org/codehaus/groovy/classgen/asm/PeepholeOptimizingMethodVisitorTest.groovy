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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.RETURN

final class PeepholeOptimizingMethodVisitorTest {

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
    void removesStandaloneCheckcastBeforePop() {
        def bytecode = sequenceFor('(Ljava/lang/Object;)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitTypeInsn(Opcodes.CHECKCAST, 'java/lang/String')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert opcodeLines(bytecode) == ['ALOAD 0', 'POP', 'RETURN']
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
