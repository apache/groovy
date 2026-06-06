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
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode
import org.objectweb.asm.util.Printer

import java.math.BigDecimal
import java.math.BigInteger

import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_STATIC
import static org.objectweb.asm.Opcodes.RETURN

final class PeepholeOptimizingMethodVisitorTest {

    @Test
    void compactsNumericConstants() {
        def instructions = collectInstructions {
            visitLdcInsn(-1)
            visitLdcInsn(5)
            visitLdcInsn(6)
            visitIntInsn(org.objectweb.asm.Opcodes.SIPUSH, 120)
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

        assert instructions == [
                'ICONST_M1',
                'ICONST_5',
                'BIPUSH 6',
                'BIPUSH 120',
                'LCONST_0',
                'LCONST_1',
                'LDC 2',
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
        def instructions = collectInsnNodes {
            visitLdcInsn(-0.0f)
            visitVarInsn(Opcodes.FSTORE, 0)
            visitLdcInsn(-0.0d)
            visitVarInsn(Opcodes.DSTORE, 1)
            visitInsn(RETURN)
        }

        assert instructions[0] instanceof LdcInsnNode
        assert instructions[0].cst.equals(-0.0f)
        assert render(instructions[1]) == 'FSTORE 0'
        assert instructions[2] instanceof LdcInsnNode
        assert instructions[2].cst.equals(-0.0d)
        assert render(instructions[3]) == 'DSTORE 1'
        assert render(instructions[4]) == 'RETURN'
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
            def instructions = collectInstructions('(I)V') {
                visitVarInsn(Opcodes.ILOAD, 0)
                visitLdcInsn(0)
                visitJumpInsn(opcode, label)
                visitInsn(RETURN)
                visitLabel(label)
                visitInsn(RETURN)
            }

            assert instructions == ['ILOAD 0', expected, 'RETURN', 'RETURN']
        }
    }

    @Test
    void removesRedundantLoadsAndConstantPops() {
        def instructions = collectInstructions('(Ljava/lang/Object;J)V') {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitInsn(Opcodes.POP)
            visitVarInsn(Opcodes.LLOAD, 1)
            visitInsn(Opcodes.POP2)
            visitLdcInsn('unused')
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert instructions == ['RETURN']
    }

    @Test
    void preservesIincWhenDroppingRedundantLoads() {
        def instructions = collectInstructions('(I)V') {
            visitVarInsn(Opcodes.ILOAD, 0)
            visitIincInsn(0, 1)
            visitInsn(Opcodes.POP)
            visitInsn(RETURN)
        }

        assert instructions == ['IINC 0 1', 'RETURN']
    }

    @Test
    void removesRedundantDupStorePops() {
        def instructions = collectInstructions {
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

        assert instructions == [
                'ICONST_1',
                'ISTORE 0',
                'LDC 2',
                'PUTSTATIC Owner.VALUE : J',
                'RETURN',
        ]
    }

    @Test
    void lowersBigNumberConstantsBeforeEmission() {
        def instructions = collectInstructions {
            visitLdcInsn(new BigDecimal('123.45'))
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'DECIMAL', 'Ljava/math/BigDecimal;')
            visitLdcInsn(new BigInteger('42'))
            visitFieldInsn(Opcodes.PUTSTATIC, 'Owner', 'INTEGER', 'Ljava/math/BigInteger;')
            visitInsn(RETURN)
        }

        assert instructions == [
                'NEW java/math/BigDecimal',
                'DUP',
                'LDC 123.45',
                'INVOKESPECIAL java/math/BigDecimal.<init> (Ljava/lang/String;)V',
                'PUTSTATIC Owner.DECIMAL : Ljava/math/BigDecimal;',
                'NEW java/math/BigInteger',
                'DUP',
                'LDC 42',
                'INVOKESPECIAL java/math/BigInteger.<init> (Ljava/lang/String;)V',
                'PUTSTATIC Owner.INTEGER : Ljava/math/BigInteger;',
                'RETURN',
        ]
    }

    private static List<String> collectInstructions(String descriptor = '()V', @DelegatesTo(MethodVisitor) Closure emitter) {
        collectInsnNodes(descriptor, emitter).collect { AbstractInsnNode insn -> render(insn) }
    }

    private static List<AbstractInsnNode> collectInsnNodes(String descriptor = '()V', @DelegatesTo(MethodVisitor) Closure emitter) {
        def sink = new MethodNode(CompilerConfiguration.ASM_API_VERSION, ACC_PUBLIC | ACC_STATIC, 'sample', descriptor, null, null)
        def visitor = new PeepholeOptimizingMethodVisitor(sink)
        visitor.visitCode()
        emitter.delegate = visitor
        emitter.resolveStrategy = Closure.DELEGATE_FIRST
        emitter.call()
        visitor.visitMaxs(0, 0)
        visitor.visitEnd()
        sink.instructions.toArray()
                .findAll { AbstractInsnNode insn -> insn.opcode >= 0 }
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
            return "LDC ${insn.cst}"
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
}
