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
package org.codehaus.groovy.classgen.asm;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;

/**
 * Folds a boolean that is materialized only to be branched on straight away.
 * <p>
 * Comparisons such as {@code i < n} are written as a {@code 0}/{@code 1} value
 * (see {@link BinaryExpressionWriter}), and conditions of {@code if}, {@code while}
 * and {@code for} statements then branch on that value. This visitor rewrites
 * <pre>
 *     Jcc L1; ICONST_1; GOTO L2; L1: ICONST_0; L2: IFEQ L3     ==&gt;   Jcc L3
 *     Jcc L1; ICONST_1; GOTO L2; L1: ICONST_0; L2: IFNE L3     ==&gt;   !Jcc L3
 * </pre>
 * which is the shape javac produces. Besides being smaller and faster, some
 * bytecode consumers (e.g. GPU kernel compilers detecting counted loops) only
 * recognize loop conditions in this form.
 * <p>
 * The rewrite is exact: the folded jump tests the same operands (or the same
 * {@code LCMP}/{@code FCMPx}/{@code DCMPx} result) as the original jump. It is
 * only applied when {@code L1} and {@code L2} are referenced solely by the
 * pattern itself, so labels shared with other jumps (e.g. from {@code &&}/{@code ||})
 * are left untouched.
 *
 * @see PeepholeOptimizingClassVisitor
 * @since 7.0.0
 */
public class ConditionalJumpFoldingMethodVisitor extends MethodVisitor {

    private static final int IDLE = 0, JCC = 1, ONE = 2, GOTO_L2 = 3, AT_L1 = 4, ZERO = 5, AT_L2 = 6;

    private int state = IDLE;
    private int jcc;
    private Label l1, l2;
    private final Map<Label, Integer> references = new HashMap<>();
    private Set<Label> dropped;

    /**
     * Creates a folding visitor that forwards to {@code delegate}.
     *
     * @param delegate the next method visitor in the chain
     */
    public ConditionalJumpFoldingMethodVisitor(final MethodVisitor delegate) {
        super(CompilerConfiguration.ASM_API_VERSION, delegate);
    }

    /**
     * Idempotent factory: returns {@code delegate} unchanged when it is
     * {@code null} or already a {@link ConditionalJumpFoldingMethodVisitor},
     * otherwise wraps it.
     *
     * @param delegate the visitor to wrap, or {@code null} to skip
     * @return a folding method visitor, or {@code null} when {@code delegate} is {@code null}
     */
    public static MethodVisitor wrap(final MethodVisitor delegate) {
        if (delegate == null || delegate instanceof ConditionalJumpFoldingMethodVisitor) {
            return delegate;
        }
        return new ConditionalJumpFoldingMethodVisitor(delegate);
    }

    MethodVisitor delegate() {
        return mv;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        reference(label);
        if (state == ONE && opcode == GOTO) {
            l2 = label;
            state = GOTO_L2;
            return;
        }
        if (state == AT_L2 && (opcode == IFEQ || opcode == IFNE)) {
            int folded = opcode == IFEQ ? jcc : negate(jcc);
            drop(l1); drop(l2);
            state = IDLE;
            startOrEmitJump(folded, label);
            return;
        }
        flush();
        startOrEmitJump(opcode, label);
    }

    private void startOrEmitJump(final int opcode, final Label label) {
        if (negate(opcode) != -1) {
            jcc = opcode;
            l1 = label;
            state = JCC;
        } else {
            super.visitJumpInsn(opcode, label);
        }
    }

    @Override
    public void visitInsn(final int opcode) {
        if (state == JCC && opcode == ICONST_1) {
            state = ONE;
            return;
        }
        if (state == AT_L1 && opcode == ICONST_0) {
            state = ZERO;
            return;
        }
        flush();
        super.visitInsn(opcode);
    }

    @Override
    public void visitLabel(final Label label) {
        if (state == GOTO_L2 && label == l1 && references(l1) == 1) {
            state = AT_L1;
            return;
        }
        if (state == ZERO && label == l2 && references(l2) == 1) {
            state = AT_L2;
            return;
        }
        flush();
        super.visitLabel(label);
    }

    /**
     * Emits any partially matched pattern unchanged.
     */
    private void flush() {
        int matched = state;
        state = IDLE;
        if (matched >= JCC)     super.visitJumpInsn(jcc, l1);
        if (matched >= ONE)     super.visitInsn(ICONST_1);
        if (matched >= GOTO_L2) super.visitJumpInsn(GOTO, l2);
        if (matched >= AT_L1)   super.visitLabel(l1);
        if (matched >= ZERO)    super.visitInsn(ICONST_0);
        if (matched >= AT_L2)   super.visitLabel(l2);
    }

    private void reference(final Label label) {
        if (dropped != null && dropped.contains(label)) {
            throw new GroovyBugError("Jump to a label removed by conditional jump folding");
        }
        references.merge(label, 1, Integer::sum);
    }

    private int references(final Label label) {
        return references.getOrDefault(label, 0);
    }

    private void drop(final Label label) {
        if (dropped == null) dropped = new HashSet<>();
        dropped.add(label);
    }

    private static int negate(final int opcode) {
        switch (opcode) {
            case IFEQ:      return IFNE;
            case IFNE:      return IFEQ;
            case IFLT:      return IFGE;
            case IFGE:      return IFLT;
            case IFGT:      return IFLE;
            case IFLE:      return IFGT;
            case IF_ICMPEQ: return IF_ICMPNE;
            case IF_ICMPNE: return IF_ICMPEQ;
            case IF_ICMPLT: return IF_ICMPGE;
            case IF_ICMPGE: return IF_ICMPLT;
            case IF_ICMPGT: return IF_ICMPLE;
            case IF_ICMPLE: return IF_ICMPGT;
            case IF_ACMPEQ: return IF_ACMPNE;
            case IF_ACMPNE: return IF_ACMPEQ;
            case IFNULL:    return IFNONNULL;
            case IFNONNULL: return IFNULL;
            default:        return -1;
        }
    }

    //--------------------------------------------------------------------------
    // everything else ends a partial match

    @Override
    public void visitParameter(final String name, final int access) {
        flush();
        super.visitParameter(name, access);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        flush();
        return super.visitAnnotationDefault();
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        flush();
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        flush();
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAnnotableParameterCount(final int parameterCount, final boolean visible) {
        flush();
        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
        flush();
        return super.visitParameterAnnotation(parameter, descriptor, visible);
    }

    @Override
    public void visitAttribute(final Attribute attribute) {
        flush();
        super.visitAttribute(attribute);
    }

    @Override
    public void visitCode() {
        flush();
        super.visitCode();
    }

    @Override
    public void visitFrame(final int type, final int numLocal, final Object[] local, final int numStack, final Object[] stack) {
        flush();
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        flush();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int varIndex) {
        flush();
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        flush();
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        flush();
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        flush();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        flush();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitLdcInsn(final Object value) {
        flush();
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(final int varIndex, final int increment) {
        flush();
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        flush();
        reference(dflt);
        for (Label label : labels) reference(label);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        flush();
        reference(dflt);
        for (Label label : labels) reference(label);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
        flush();
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        flush();
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        flush();
        reference(start); reference(end); reference(handler);
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        flush();
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitLocalVariable(final String name, final String descriptor, final String signature, final Label start, final Label end, final int index) {
        flush();
        reference(start); reference(end);
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String descriptor, final boolean visible) {
        flush();
        for (Label label : start) reference(label);
        for (Label label : end) reference(label);
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        flush();
        reference(start);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        flush();
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        flush();
        super.visitEnd();
    }
}
