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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DCONST_0;
import static org.objectweb.asm.Opcodes.DCONST_1;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DSTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.FCONST_0;
import static org.objectweb.asm.Opcodes.FCONST_1;
import static org.objectweb.asm.Opcodes.FCONST_2;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ACMPNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LCONST_0;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SIPUSH;

/**
 * Single-pass, stack-local bytecode compaction for methods emitted by the Groovy
 * class generator. Inspired by Groovy++'s peephole adapters.
 * <p>
 * Upstream writers such as {@link OperandStack} and {@link BytecodeHelper} may emit
 * a uniform, easy-to-generate form (for example {@code visitLdcInsn} for every
 * integer, or box immediately after a primitive producer). This visitor rewrites
 * those sequences, within a single basic-block window, into the densest equivalent
 * JVM opcodes without a second compilation pass or a full data-flow analysis.
 *
 * <h2>Model</h2>
 * At most one <em>pending load</em> (constant, variable load, standalone
 * {@code CHECKCAST}, or {@code Boolean.TRUE}/{@code FALSE}), at most one
 * <em>pending box</em> ({@code Wrapper.valueOf} or
 * {@link DefaultTypeTransformation#box}), and at most one
 * <em>pending {@code DUP}/{@code DUP2}</em> (optionally followed by a buffered
 * store) are held at a time. Pending state is flushed to the delegate before any
 * non-local boundary so control flow, frames, and debug metadata stay correct:
 * <ul>
 *   <li>labels, jump targets, table/lookup switches</li>
 *   <li>stack map frames</li>
 *   <li>line numbers, local-variable tables, and type annotations</li>
 *   <li>method and {@code invokedynamic} calls (except matched box/unbox pairs)</li>
 *   <li>{@code visitMaxs} / {@code visitEnd}</li>
 * </ul>
 *
 * <h2>Rewrites</h2>
 * <ul>
 *   <li><b>Constant narrowing</b> — {@code LDC}/push forms become
 *       {@code ICONST_*}, {@code BIPUSH}, {@code SIPUSH}, {@code LCONST_*},
 *       {@code FCONST_*}, {@code DCONST_*}, or {@code ACONST_NULL} when legal.
 *       Signed floating-point zeros ({@code -0.0f}/{@code -0.0d}) are preserved
 *       via raw-bit comparison (GROOVY-9797).</li>
 *   <li><b>Dead loads</b> — a buffered load or constant followed by a matching
 *       {@code POP}/{@code POP2}, or a void {@code RETURN}, is dropped. A
 *       buffered {@code IINC} paired with {@code ILOAD} is retained as a side
 *       effect when the loaded value itself is discarded.</li>
 *   <li><b>CHECKCAST</b> — may attach to a pending {@code ALOAD} or reference
 *       constant so {@code load}; {@code CHECKCAST}; {@code POP} collapses
 *       entirely. A standalone cast before {@code POP} is dropped; before void
 *       {@code RETURN} the cast is flushed and the value is popped so the
 *       type-check side effect is kept and the void return stays verifiable.</li>
 *   <li><b>Compare-to-zero / null</b> —
 *       {@code ICONST_0}; {@code IF_ICMP*} → {@code IF*};
 *       {@code ACONST_NULL}; {@code IF_ACMP*} → {@code IFNULL}/{@code IFNONNULL}.</li>
 *   <li><b>DUP + store + pop</b> — {@code DUP}/{@code DUP2}; store;
 *       matching pop becomes a plain store; bare {@code DUP}/{@code DUP2} with a
 *       matching pop is eliminated.</li>
 *   <li><b>Box/unbox cancellation</b> — {@code Wrapper.valueOf(p)};
 *       {@code Wrapper.xxxValue()} and {@code DefaultTypeTransformation.box(p)};
 *       {@code DefaultTypeTransformation.xxxUnbox(...)} with matching primitive
 *       types cancel to a no-op (the primitive remains on the stack). A boxed
 *       value discarded by {@code POP}/{@code POP2} drops the box and pops the
 *       original primitive instead (using {@code POP2} when the primitive is
 *       wide). Mismatched unbox types are left intact.</li>
 *   <li><b>Boolean constant folding</b> — {@code GETSTATIC Boolean.TRUE/FALSE}
 *       followed by {@code booleanValue()} or
 *       {@code DefaultTypeTransformation.booleanUnbox} becomes
 *       {@code ICONST_1}/{@code ICONST_0}.</li>
 *   <li><b>Big number lowering</b> — buffered {@link BigDecimal}/{@link BigInteger}
 *       constants become {@code new Type(String)} construction on flush.</li>
 * </ul>
 *
 * <h2>Installation</h2>
 * Prefer {@link PeepholeOptimizingClassVisitor} (wired from
 * {@link WriterController}) so every method is covered. Use {@link #wrap(MethodVisitor)}
 * only when constructing a method visitor outside that chain (for example unit tests).
 * Integer constants are re-emitted through {@link BytecodeHelper#pushConstant} on the
 * <em>delegate</em> so they are not re-buffered by this visitor.
 *
 * @see PeepholeOptimizingClassVisitor
 * @see OperandStack
 * @see BytecodeHelper#pushConstant(MethodVisitor, int)
 * @since 6.0.0
 */
public final class PeepholeOptimizingMethodVisitor extends MethodVisitor {

    private static final String BIG_DECIMAL_TYPE = "java/math/BigDecimal";
    private static final String BIG_INTEGER_TYPE = "java/math/BigInteger";
    private static final String STRING_CTOR_DESCRIPTOR = "(Ljava/lang/String;)V";
    private static final String BOOLEAN_OWNER = "java/lang/Boolean";
    private static final String BOOLEAN_DESC = "Ljava/lang/Boolean;";
    private static final String DTT_OWNER = Type.getInternalName(DefaultTypeTransformation.class);
    private static final int NO_OPCODE = -1;
    private static final int FLOAT_TO_RAW_INT_BITS_0 = Float.floatToRawIntBits(0f);
    private static final int FLOAT_TO_RAW_INT_BITS_1 = Float.floatToRawIntBits(1f);
    private static final int FLOAT_TO_RAW_INT_BITS_2 = Float.floatToRawIntBits(2f);
    private static final long DOUBLE_TO_RAW_LONG_BITS_0 = Double.doubleToRawLongBits(0d);
    private static final long DOUBLE_TO_RAW_LONG_BITS_1 = Double.doubleToRawLongBits(1d);

    /** Kind of value currently held in the single-slot load window, if any. */
    private enum PendingLoadKind {
        NONE,
        CONSTANT,
        VARIABLE,
        CHECKCAST,
        /** Buffered {@code GETSTATIC java/lang/Boolean.TRUE} or {@code FALSE}. */
        BOOLEAN_CONSTANT
    }

    /** Kind of store buffered after a pending {@code DUP}/{@code DUP2}, if any. */
    private enum PendingStoreKind {
        NONE,
        VARIABLE,
        STATIC_FIELD
    }

    /**
     * Kind of boxing call buffered while waiting for a matching unbox or for the
     * boxed value to be discarded.
     */
    private enum PendingBoxKind {
        NONE,
        /** {@code Wrapper.valueOf(primitive)}. */
        VALUE_OF,
        /** {@code DefaultTypeTransformation.box(primitive)}. */
        DTT_BOX
    }

    private PendingLoadKind pendingLoadKind = PendingLoadKind.NONE;
    private Object pendingConstant;
    private int pendingLoadOpcode = NO_OPCODE;
    private int pendingLoadVar = NO_OPCODE;
    private boolean pendingLoadHasIinc;
    private int pendingLoadIncrement;
    /**
     * Internal name / type descriptor for a pending {@code CHECKCAST}.
     * <ul>
     *   <li>When {@link #pendingLoadKind} is {@link PendingLoadKind#VARIABLE} or
     *       {@link PendingLoadKind#CONSTANT}: optional cast attached to that load
     *       (emitted after the load on flush, or dropped with the load on pop).</li>
     *   <li>When the kind is {@link PendingLoadKind#CHECKCAST}: standalone cast of
     *       a value already written to the operand stack.</li>
     * </ul>
     */
    private String pendingCheckcastDescriptor;
    /** Value of a pending {@link PendingLoadKind#BOOLEAN_CONSTANT} load. */
    private boolean pendingBooleanValue;

    private int pendingDupOpcode = NO_OPCODE;
    private PendingStoreKind pendingStoreKind = PendingStoreKind.NONE;
    private int pendingStoreOpcode = NO_OPCODE;
    private int pendingStoreVar = NO_OPCODE;
    private String pendingStoreOwner;
    private String pendingStoreName;
    private String pendingStoreDescriptor;

    private PendingBoxKind pendingBoxKind = PendingBoxKind.NONE;
    /** Wrapper internal name for {@link PendingBoxKind#VALUE_OF} (e.g. {@code java/lang/Integer}). */
    private String pendingBoxOwner;
    /** Primitive descriptor of the boxed value ({@code I}, {@code J}, …). */
    private String pendingBoxPrimitiveDescriptor;
    private boolean pendingBoxIsInterface;

    /**
     * Creates a peephole visitor that forwards compacted instructions to
     * {@code delegate}.
     *
     * @param delegate the next method visitor in the chain (for example a
     *        {@link org.objectweb.asm.MethodWriter} or {@link TraceMethodVisitor})
     */
    public PeepholeOptimizingMethodVisitor(final MethodVisitor delegate) {
        super(CompilerConfiguration.ASM_API_VERSION, delegate);
    }

    /**
     * Idempotent factory: returns {@code delegate} unchanged when it is
     * {@code null} or already a {@link PeepholeOptimizingMethodVisitor},
     * otherwise wraps it.
     * <p>
     * Returning {@code null} unchanged matches the ASM contract that
     * {@link org.objectweb.asm.ClassVisitor#visitMethod} may return {@code null}
     * to skip a method body. Used by {@link PeepholeOptimizingClassVisitor#visitMethod}
     * so nested or repeated wrapping does not stack multiple peephole layers.
     *
     * @param delegate the visitor to wrap, or {@code null} to skip
     * @return a peephole-optimizing method visitor, or {@code null} when
     *         {@code delegate} is {@code null}
     */
    public static MethodVisitor wrap(final MethodVisitor delegate) {
        if (delegate == null || delegate instanceof PeepholeOptimizingMethodVisitor) {
            return delegate;
        }
        return new PeepholeOptimizingMethodVisitor(delegate);
    }

    /**
     * Walks {@code visitor} and any nested {@link PeepholeOptimizingMethodVisitor}
     * layers to find a {@link TraceMethodVisitor}, then prints that visitor's
     * recorded instruction text to {@code out}.
     * <p>
     * {@link org.codehaus.groovy.classgen.AsmClassGenerator} uses this when
     * {@code visitMaxs} fails under classgen logging: the outer visitor is a
     * peephole wrapper, so a direct {@code instanceof TraceMethodVisitor} check
     * would miss the tracer sitting further down the chain.
     *
     * @param visitor the method visitor active during class generation (may be
     *        a peephole wrapper); {@code null} is treated as “not found”
     * @param out destination for the traced bytecode listing
     * @return {@code true} if a {@link TraceMethodVisitor} was found and printed;
     *         {@code false} if none was present in the chain
     */
    public static boolean printTraceBytecode(final MethodVisitor visitor, final PrintWriter out) {
        MethodVisitor current = visitor;
        while (current instanceof PeepholeOptimizingMethodVisitor peephole) {
            current = peephole.mv;
        }
        if (current instanceof TraceMethodVisitor tmv) {
            tmv.p.print(out);
            return true;
        }
        return false;
    }

    @Override
    public void visitAttribute(final Attribute attribute) {
        flushPending();
        super.visitAttribute(attribute);
    }

    @Override
    public void visitFrame(final int type, final int numLocal, final Object[] local, final int numStack, final Object[] stack) {
        flushPending();
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitInsn(final int opcode) {
        if (tryRemovePendingLoad(opcode)
                || tryDropPendingLoadOnReturn(opcode)
                || tryCollapsePendingDup(opcode)
                || tryDropPendingBoxOnPop(opcode)) {
            return;
        }

        flushPendingLoad();
        if (opcode == DUP || opcode == DUP2) {
            flushPendingDupStore();
            flushPendingBox();
            pendingDupOpcode = opcode;
            return;
        }

        flushPendingDupStore();
        flushPendingBox();
        switch (opcode) {
          case ACONST_NULL:
            bufferConstant(null);
            return;
          case ICONST_M1:
            bufferConstant(-1);
            return;
          case ICONST_0:
            bufferConstant(0);
            return;
          case ICONST_1:
            bufferConstant(1);
            return;
          case ICONST_2:
            bufferConstant(2);
            return;
          case ICONST_3:
            bufferConstant(3);
            return;
          case ICONST_4:
            bufferConstant(4);
            return;
          case ICONST_5:
            bufferConstant(5);
            return;
          case LCONST_0:
            bufferConstant(0L);
            return;
          case LCONST_1:
            bufferConstant(1L);
            return;
          case FCONST_0:
            bufferConstant(0f);
            return;
          case FCONST_1:
            bufferConstant(1f);
            return;
          case FCONST_2:
            bufferConstant(2f);
            return;
          case DCONST_0:
            bufferConstant(0d);
            return;
          case DCONST_1:
            bufferConstant(1d);
            return;
          default:
            super.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        flushPendingLoad();
        flushPendingDupStore();
        flushPendingBox();
        if (opcode == BIPUSH || opcode == SIPUSH) {
            bufferConstant(operand);
            return;
        }
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int varIndex) {
        flushPendingLoad();
        if (pendingDupOpcode != NO_OPCODE && pendingStoreKind == PendingStoreKind.NONE && isStoreOpcode(opcode)) {
            flushPendingBox();
            bufferVariableStore(opcode, varIndex);
            return;
        }

        flushPendingDupStore();
        flushPendingBox();
        if (isLoadOpcode(opcode)) {
            bufferVariableLoad(opcode, varIndex);
            return;
        }
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String descriptor) {
        flushPendingDupStore();
        flushPendingBox();
        if (opcode == CHECKCAST) {
            if (canAttachCheckcastToPendingLoad()) {
                pendingCheckcastDescriptor = descriptor;
                return;
            }
            flushPendingLoad();
            bufferCheckcast(descriptor);
            return;
        }

        flushPendingLoad();
        super.visitTypeInsn(opcode, descriptor);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        flushPendingLoad();
        if (pendingDupOpcode != NO_OPCODE && pendingStoreKind == PendingStoreKind.NONE && opcode == PUTSTATIC) {
            flushPendingBox();
            bufferStaticStore(opcode, owner, name, descriptor);
            return;
        }

        flushPendingDupStore();
        flushPendingBox();
        if (opcode == GETSTATIC && isBooleanTrueFalse(owner, name, descriptor)) {
            bufferBooleanConstant("TRUE".equals(name));
            return;
        }
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        if (tryFoldBooleanConstantUnbox(opcode, owner, name, descriptor)) {
            return;
        }
        if (tryCancelBoxUnbox(opcode, owner, name, descriptor)) {
            return;
        }
        if (tryBufferBox(opcode, owner, name, descriptor, isInterface)) {
            return;
        }

        flushPending();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(final String name, final String descriptor, final Handle bootstrapMethodHandle, final Object... bootstrapMethodArguments) {
        flushPending();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        flushPendingDupStore();
        flushPendingBox();
        if (tryRewriteZeroCompare(opcode, label) || tryRewriteNullCompare(opcode, label)) {
            return;
        }

        flushPendingLoad();
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(final Label label) {
        flushPending();
        super.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(final Object value) {
        flushPendingLoad();
        flushPendingDupStore();
        flushPendingBox();
        if (value instanceof ConstantDynamic) {
            super.visitLdcInsn(value);
            return;
        }
        bufferConstant(value);
    }

    @Override
    public void visitIincInsn(final int varIndex, final int increment) {
        if (pendingLoadKind == PendingLoadKind.VARIABLE
                && pendingLoadOpcode == ILOAD
                && pendingLoadVar == varIndex
                && !pendingLoadHasIinc
                && pendingCheckcastDescriptor == null) {
            pendingLoadHasIinc = true;
            pendingLoadIncrement = increment;
            return;
        }

        flushPending();
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        flushPending();
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        flushPending();
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int dims) {
        flushPending();
        super.visitMultiANewArrayInsn(descriptor, dims);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        flushPending();
        return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        flushPending();
        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
        flushPending();
        return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitLocalVariable(final String name, final String descriptor, final String signature, final Label start, final Label end, final int index) {
        flushPending();
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String descriptor, final boolean visible) {
        flushPending();
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        flushPending();
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        flushPending();
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        flushPending();
        super.visitEnd();
    }

    /**
     * Flushes the load window, any pending {@code DUP}/store pair, and any
     * pending box call.
     */
    private void flushPending() {
        flushPendingLoad();
        flushPendingDupStore();
        flushPendingBox();
    }

    /**
     * Emits the buffered load/constant (and any attached {@code CHECKCAST} or
     * {@code IINC}) to the delegate, then clears the load window.
     */
    private void flushPendingLoad() {
        switch (pendingLoadKind) {
          case CONSTANT:
            emitConstant(pendingConstant);
            emitAttachedCheckcast();
            break;
          case VARIABLE:
            super.visitVarInsn(pendingLoadOpcode, pendingLoadVar);
            if (pendingLoadHasIinc) {
                super.visitIincInsn(pendingLoadVar, pendingLoadIncrement);
            }
            emitAttachedCheckcast();
            break;
          case CHECKCAST:
            super.visitTypeInsn(CHECKCAST, pendingCheckcastDescriptor);
            break;
          case BOOLEAN_CONSTANT:
            super.visitFieldInsn(GETSTATIC, BOOLEAN_OWNER,
                    pendingBooleanValue ? "TRUE" : "FALSE", BOOLEAN_DESC);
            break;
          case NONE:
          default:
        }
        clearPendingLoad();
    }

    /**
     * Emits a buffered {@code Wrapper.valueOf} or
     * {@code DefaultTypeTransformation.box} call to the delegate.
     */
    private void flushPendingBox() {
        if (pendingBoxKind == PendingBoxKind.NONE) {
            return;
        }
        switch (pendingBoxKind) {
          case VALUE_OF:
            super.visitMethodInsn(INVOKESTATIC, pendingBoxOwner, "valueOf",
                    "(" + pendingBoxPrimitiveDescriptor + ")L" + pendingBoxOwner + ";",
                    pendingBoxIsInterface);
            break;
          case DTT_BOX:
            super.visitMethodInsn(INVOKESTATIC, DTT_OWNER, "box",
                    "(" + pendingBoxPrimitiveDescriptor + ")Ljava/lang/Object;",
                    false);
            break;
          case NONE:
          default:
        }
        clearPendingBox();
    }

    /** Emits a {@code CHECKCAST} that was attached to a pending variable/constant load. */
    private void emitAttachedCheckcast() {
        if (pendingCheckcastDescriptor != null) {
            super.visitTypeInsn(CHECKCAST, pendingCheckcastDescriptor);
        }
    }

    /**
     * Emits a buffered {@code DUP}/{@code DUP2} and, when present, the following
     * store that was waiting to see whether the duplicated value would be discarded.
     */
    private void flushPendingDupStore() {
        if (pendingDupOpcode == NO_OPCODE) {
            return;
        }

        super.visitInsn(pendingDupOpcode);
        if (pendingStoreKind != PendingStoreKind.NONE) {
            emitPendingStore();
        }
        clearPendingDupStore();
    }

    /**
     * Rewrites {@code …; ICONST_0; IF_ICMPxx L} to {@code …; IFxx L} when the
     * pending constant is integer zero.
     * <p>
     * The six {@code IF_ICMP*} opcodes occupy a contiguous range whose relative
     * order matches {@code IFEQ}…{@code IFLE}, so the rewrite is a constant
     * offset rather than a per-opcode table (see JVM opcode encoding).
     *
     * @return {@code true} if the jump was rewritten and no further action is needed
     */
    private boolean tryRewriteZeroCompare(final int opcode, final Label label) {
        if (pendingLoadKind != PendingLoadKind.CONSTANT
                || pendingCheckcastDescriptor != null
                || !(pendingConstant instanceof Integer intValue)
                || intValue != 0
                || opcode < IF_ICMPEQ
                || opcode > IF_ICMPLE) {
            return false;
        }

        // IF_ICMPEQ..IF_ICMPLE share the same order as IFEQ..IFLE (offset = IFEQ - IF_ICMPEQ).
        clearPendingLoad();
        super.visitJumpInsn(opcode + (IFEQ - IF_ICMPEQ), label);
        return true;
    }

    /**
     * Rewrites {@code …; ACONST_NULL; IF_ACMPEQ/NE L} to {@code …; IFNULL/IFNONNULL L}.
     *
     * @return {@code true} if the jump was rewritten and no further action is needed
     */
    private boolean tryRewriteNullCompare(final int opcode, final Label label) {
        if (pendingLoadKind != PendingLoadKind.CONSTANT
                || pendingCheckcastDescriptor != null
                || pendingConstant != null) {
            return false;
        }

        final int replacement;
        if (opcode == IF_ACMPEQ) {
            replacement = IFNULL;
        } else if (opcode == IF_ACMPNE) {
            replacement = IFNONNULL;
        } else {
            return false;
        }

        clearPendingLoad();
        super.visitJumpInsn(replacement, label);
        return true;
    }

    /**
     * Drops a dead buffered load/constant (or standalone {@code CHECKCAST}) when
     * the next instruction is a matching {@code POP}/{@code POP2}.
     * <p>
     * For a standalone cast the value is already on the stack, so only the cast is
     * removed and the pop is still emitted. For a buffered load, both the load and
     * any attached cast are removed; a paired {@code IINC} is preserved as a side
     * effect.
     *
     * @return {@code true} if the pop was fully handled
     */
    private boolean tryRemovePendingLoad(final int opcode) {
        if (pendingLoadKind == PendingLoadKind.NONE) {
            return false;
        }

        int popSize = stackSizeForPop(opcode);
        if (popSize == 0) {
            return false;
        }

        if (pendingLoadKind == PendingLoadKind.CHECKCAST) {
            // Value already on stack: drop the dead cast, keep the pop.
            if (opcode != POP) {
                return false;
            }
            clearPendingLoad();
            super.visitInsn(POP);
            return true;
        }

        if (stackSizeForPendingLoad() != popSize) {
            return false;
        }

        emitPreservedIincSideEffect();
        clearPendingLoad();
        return true;
    }

    /**
     * Handles void {@code RETURN} against the load window:
     * <ul>
     *   <li>Pure dead load/constant — dropped (paired {@code IINC} kept).</li>
     *   <li>Load with attached {@code CHECKCAST}, or standalone cast — the cast is
     *       emitted so a type-check side effect is preserved, then {@code POP} clears
     *       the operand stack so the void return is verifiable.</li>
     * </ul>
     *
     * @return {@code true} if the return was fully handled
     */
    private boolean tryDropPendingLoadOnReturn(final int opcode) {
        if (opcode != RETURN || pendingLoadKind == PendingLoadKind.NONE) {
            return false;
        }

        if (pendingLoadKind == PendingLoadKind.CHECKCAST) {
            super.visitTypeInsn(CHECKCAST, pendingCheckcastDescriptor);
            clearPendingLoad();
            super.visitInsn(POP);
            super.visitInsn(RETURN);
            return true;
        }

        if (pendingCheckcastDescriptor != null) {
            // Keep the cast side effect; discard the value for a valid void return.
            flushPendingLoad();
            super.visitInsn(POP);
            super.visitInsn(RETURN);
            return true;
        }

        emitPreservedIincSideEffect();
        clearPendingLoad();
        super.visitInsn(RETURN);
        return true;
    }

    /** Emits a buffered {@code IINC} that must survive dead-load elimination. */
    private void emitPreservedIincSideEffect() {
        if (pendingLoadKind == PendingLoadKind.VARIABLE && pendingLoadHasIinc) {
            super.visitIincInsn(pendingLoadVar, pendingLoadIncrement);
        }
    }

    /**
     * Collapses {@code DUP}/{@code DUP2} with an optional store and a matching pop:
     * <ul>
     *   <li>{@code DUP}; {@code xSTORE}; {@code POP} → {@code xSTORE}</li>
     *   <li>{@code DUP2}; {@code PUTSTATIC J/D}; {@code POP2} → {@code PUTSTATIC}</li>
     *   <li>{@code DUP}/{@code DUP2}; matching pop with no store → eliminated entirely</li>
     * </ul>
     *
     * @return {@code true} if the pop was fully handled
     */
    private boolean tryCollapsePendingDup(final int opcode) {
        if (pendingDupOpcode == NO_OPCODE) {
            return false;
        }

        int popSize = stackSizeForPop(opcode);
        if (popSize == 0 || stackSizeForDup() != popSize) {
            return false;
        }

        if (pendingStoreKind != PendingStoreKind.NONE) {
            if (stackSizeForPendingStore() != popSize) {
                return false;
            }
            emitPendingStore();
        }
        clearPendingDupStore();
        return true;
    }

    /**
     * Whether the next {@code CHECKCAST} can be recorded as an adornment on the
     * current pending reference load instead of flushing that load first.
     * Restricted to {@code ALOAD} and reference-typed constants so primitive
     * loads are never paired with an invalid cast.
     */
    private boolean canAttachCheckcastToPendingLoad() {
        if (pendingCheckcastDescriptor != null) {
            return false;
        }
        if (pendingLoadKind == PendingLoadKind.VARIABLE
                && pendingLoadOpcode == ALOAD
                && !pendingLoadHasIinc) {
            return true;
        }
        return pendingLoadKind == PendingLoadKind.CONSTANT && isReferenceConstant(pendingConstant);
    }

    /**
     * {@code true} for constant values that occupy a single reference slot on the
     * operand stack ({@code null}, {@link String}, {@link Type}, {@link Handle}).
     * {@link ConstantDynamic} is intentionally excluded: it is never buffered
     * (see {@link #visitLdcInsn}) because resolving it may run a bootstrap method
     * with observable side effects, so it must not be subject to dead-load removal.
     */
    private static boolean isReferenceConstant(final Object value) {
        return value == null
                || value instanceof String
                || value instanceof Type
                || value instanceof Handle;
    }

    /**
     * Replaces the load window with a constant candidate. Any previously buffered
     * load is flushed first so candidates are never silently discarded.
     */
    private void bufferConstant(final Object value) {
        flushPendingLoad();
        pendingLoadKind = PendingLoadKind.CONSTANT;
        pendingConstant = value;
    }

    /**
     * Replaces the load window with a variable-load candidate, flushing any prior
     * pending load first.
     */
    private void bufferVariableLoad(final int opcode, final int varIndex) {
        flushPendingLoad();
        pendingLoadKind = PendingLoadKind.VARIABLE;
        pendingLoadOpcode = opcode;
        pendingLoadVar = varIndex;
    }

    /**
     * Buffers a standalone {@code CHECKCAST} of a value already on the stack
     * (the preceding producer was not held in the load window).
     */
    private void bufferCheckcast(final String descriptor) {
        flushPendingLoad();
        pendingLoadKind = PendingLoadKind.CHECKCAST;
        pendingCheckcastDescriptor = descriptor;
    }

    private void bufferVariableStore(final int opcode, final int varIndex) {
        pendingStoreKind = PendingStoreKind.VARIABLE;
        pendingStoreOpcode = opcode;
        pendingStoreVar = varIndex;
    }

    private void bufferStaticStore(final int opcode, final String owner, final String name, final String descriptor) {
        pendingStoreKind = PendingStoreKind.STATIC_FIELD;
        pendingStoreOpcode = opcode;
        pendingStoreOwner = owner;
        pendingStoreName = name;
        pendingStoreDescriptor = descriptor;
    }

    private void emitPendingStore() {
        switch (pendingStoreKind) {
          case VARIABLE:
            super.visitVarInsn(pendingStoreOpcode, pendingStoreVar);
            break;
          case STATIC_FIELD:
            super.visitFieldInsn(pendingStoreOpcode, pendingStoreOwner, pendingStoreName, pendingStoreDescriptor);
            break;
          case NONE:
          default:
        }
    }

    private void clearPendingLoad() {
        pendingLoadKind = PendingLoadKind.NONE;
        pendingConstant = null;
        pendingLoadOpcode = NO_OPCODE;
        pendingLoadVar = NO_OPCODE;
        pendingLoadHasIinc = false;
        pendingLoadIncrement = 0;
        pendingCheckcastDescriptor = null;
        pendingBooleanValue = false;
    }

    private void clearPendingDupStore() {
        pendingDupOpcode = NO_OPCODE;
        pendingStoreKind = PendingStoreKind.NONE;
        pendingStoreOpcode = NO_OPCODE;
        pendingStoreVar = NO_OPCODE;
        pendingStoreOwner = null;
        pendingStoreName = null;
        pendingStoreDescriptor = null;
    }

    private void clearPendingBox() {
        pendingBoxKind = PendingBoxKind.NONE;
        pendingBoxOwner = null;
        pendingBoxPrimitiveDescriptor = null;
        pendingBoxIsInterface = false;
    }

    /**
     * Buffers {@code GETSTATIC Boolean.TRUE/FALSE} so a following unbox can fold
     * to {@code ICONST_0}/{@code ICONST_1}.
     */
    private void bufferBooleanConstant(final boolean value) {
        flushPendingLoad();
        pendingLoadKind = PendingLoadKind.BOOLEAN_CONSTANT;
        pendingBooleanValue = value;
    }

    private void bufferValueOfBox(final String owner, final String primitiveDescriptor, final boolean isInterface) {
        clearPendingBox();
        pendingBoxKind = PendingBoxKind.VALUE_OF;
        pendingBoxOwner = owner;
        pendingBoxPrimitiveDescriptor = primitiveDescriptor;
        pendingBoxIsInterface = isInterface;
    }

    private void bufferDttBox(final String primitiveDescriptor) {
        clearPendingBox();
        pendingBoxKind = PendingBoxKind.DTT_BOX;
        pendingBoxOwner = DTT_OWNER;
        pendingBoxPrimitiveDescriptor = primitiveDescriptor;
        pendingBoxIsInterface = false;
    }

    /**
     * Folds {@code Boolean.TRUE/FALSE}; unbox into {@code ICONST_1}/{@code ICONST_0}.
     *
     * @return {@code true} if the unbox was fully rewritten
     */
    private boolean tryFoldBooleanConstantUnbox(final int opcode, final String owner, final String name, final String descriptor) {
        if (pendingLoadKind != PendingLoadKind.BOOLEAN_CONSTANT) {
            return false;
        }
        if (!isBooleanUnbox(opcode, owner, name, descriptor)) {
            return false;
        }
        boolean value = pendingBooleanValue;
        clearPendingLoad();
        super.visitInsn(value ? ICONST_1 : ICONST_0);
        return true;
    }

    /**
     * Cancels a pending box when the next call is the matching unbox for the same
     * primitive type. The primitive already on the stack is left untouched.
     *
     * @return {@code true} if box and unbox were both dropped
     */
    private boolean tryCancelBoxUnbox(final int opcode, final String owner, final String name, final String descriptor) {
        if (pendingBoxKind == PendingBoxKind.NONE) {
            return false;
        }
        if (!matchesPendingUnbox(opcode, owner, name, descriptor)) {
            return false;
        }
        clearPendingBox();
        return true;
    }

    /**
     * {@code valueOf}/{@code box} followed by {@code POP}/{@code POP2} means the
     * boxed reference is discarded. Drop the box and pop the original primitive
     * instead ({@code POP2} when the primitive is {@code long} or {@code double}).
     *
     * @return {@code true} if the pop was fully handled
     */
    private boolean tryDropPendingBoxOnPop(final int opcode) {
        if (pendingBoxKind == PendingBoxKind.NONE) {
            return false;
        }
        int popSize = stackSizeForPop(opcode);
        if (popSize == 0) {
            return false;
        }
        int primitiveSize = stackSizeForPrimitiveDescriptor(pendingBoxPrimitiveDescriptor);
        // POP after boxing a wide primitive is legal (boxed ref is one slot) → POP2 the long/double.
        if (popSize == 1 && primitiveSize == 2) {
            clearPendingBox();
            super.visitInsn(POP2);
            return true;
        }
        if (popSize != primitiveSize) {
            return false;
        }
        clearPendingBox();
        super.visitInsn(primitiveSize == 2 ? POP2 : POP);
        return true;
    }

    private static int stackSizeForPrimitiveDescriptor(final String descriptor) {
        return ("J".equals(descriptor) || "D".equals(descriptor)) ? 2 : 1;
    }

    /**
     * Buffers {@code Wrapper.valueOf(prim)} or {@code DefaultTypeTransformation.box(prim)}
     * so a following matching unbox (or discard via pop) can collapse the pair.
     * Any previously pending state is flushed first so the primitive producer is
     * already on the operand stack.
     *
     * @return {@code true} if the call was buffered
     */
    private boolean tryBufferBox(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
        if (opcode != INVOKESTATIC) {
            return false;
        }
        if ("valueOf".equals(name)) {
            String primitive = primitiveOperandOfValueOf(owner, descriptor);
            if (primitive == null) {
                return false;
            }
            flushPending();
            bufferValueOfBox(owner, primitive, isInterface);
            return true;
        }
        if ("box".equals(name) && DTT_OWNER.equals(owner)) {
            String primitive = primitiveOperandOfDttBox(descriptor);
            if (primitive == null) {
                return false;
            }
            flushPending();
            bufferDttBox(primitive);
            return true;
        }
        return false;
    }

    private boolean matchesPendingUnbox(final int opcode, final String owner, final String name, final String descriptor) {
        String expectedPrim = pendingBoxPrimitiveDescriptor;
        if (expectedPrim == null) {
            return false;
        }
        if (pendingBoxKind == PendingBoxKind.VALUE_OF) {
            // Wrapper.xxxValue()X — e.g. Integer.intValue()I
            if (opcode != INVOKEVIRTUAL || !pendingBoxOwner.equals(owner)) {
                return false;
            }
            String expectedName = primitiveValueMethodName(expectedPrim);
            String expectedDesc = "()" + expectedPrim;
            return expectedName != null && expectedName.equals(name) && expectedDesc.equals(descriptor);
        }
        if (pendingBoxKind == PendingBoxKind.DTT_BOX) {
            // DefaultTypeTransformation.xxxUnbox(Object)X
            if (opcode != INVOKESTATIC || !DTT_OWNER.equals(owner)) {
                return false;
            }
            String expectedName = primitiveUnboxMethodName(expectedPrim);
            String expectedDesc = "(Ljava/lang/Object;)" + expectedPrim;
            return expectedName != null && expectedName.equals(name) && expectedDesc.equals(descriptor);
        }
        return false;
    }

    private static boolean isBooleanTrueFalse(final String owner, final String name, final String descriptor) {
        return BOOLEAN_OWNER.equals(owner)
                && BOOLEAN_DESC.equals(descriptor)
                && ("TRUE".equals(name) || "FALSE".equals(name));
    }

    private static boolean isBooleanUnbox(final int opcode, final String owner, final String name, final String descriptor) {
        if (opcode == INVOKESTATIC && DTT_OWNER.equals(owner)
                && "booleanUnbox".equals(name) && "(Ljava/lang/Object;)Z".equals(descriptor)) {
            return true;
        }
        return opcode == INVOKEVIRTUAL && BOOLEAN_OWNER.equals(owner)
                && "booleanValue".equals(name) && "()Z".equals(descriptor);
    }

    /**
     * Parses {@code valueOf} descriptors such as {@code (I)Ljava/lang/Integer;} and
     * returns the primitive operand descriptor when the owner is the matching wrapper.
     */
    private static String primitiveOperandOfValueOf(final String owner, final String descriptor) {
        Type[] args = Type.getArgumentTypes(descriptor);
        Type ret = Type.getReturnType(descriptor);
        if (args.length != 1 || ret.getSort() != Type.OBJECT) {
            return null;
        }
        if (!owner.equals(ret.getInternalName())) {
            return null;
        }
        Type arg = args[0];
        if (!isBoxablePrimitive(arg)) {
            return null;
        }
        String expectedWrapper = wrapperInternalName(arg);
        return owner.equals(expectedWrapper) ? arg.getDescriptor() : null;
    }

    /**
     * Parses {@code DefaultTypeTransformation.box} descriptors such as
     * {@code (I)Ljava/lang/Object;} and returns the primitive operand descriptor.
     */
    private static String primitiveOperandOfDttBox(final String descriptor) {
        Type[] args = Type.getArgumentTypes(descriptor);
        Type ret = Type.getReturnType(descriptor);
        if (args.length != 1 || ret.getSort() != Type.OBJECT) {
            return null;
        }
        Type arg = args[0];
        return isBoxablePrimitive(arg) ? arg.getDescriptor() : null;
    }

    private static boolean isBoxablePrimitive(final Type type) {
        return switch (type.getSort()) {
          case Type.BOOLEAN, Type.BYTE, Type.CHAR, Type.SHORT,
               Type.INT, Type.LONG, Type.FLOAT, Type.DOUBLE -> true;
          default -> false;
        };
    }

    private static String wrapperInternalName(final Type primitive) {
        return switch (primitive.getSort()) {
          case Type.BOOLEAN -> "java/lang/Boolean";
          case Type.BYTE -> "java/lang/Byte";
          case Type.CHAR -> "java/lang/Character";
          case Type.SHORT -> "java/lang/Short";
          case Type.INT -> "java/lang/Integer";
          case Type.LONG -> "java/lang/Long";
          case Type.FLOAT -> "java/lang/Float";
          case Type.DOUBLE -> "java/lang/Double";
          default -> null;
        };
    }

    private static String primitiveValueMethodName(final String primitiveDescriptor) {
        return switch (primitiveDescriptor) {
          case "Z" -> "booleanValue";
          case "B" -> "byteValue";
          case "C" -> "charValue";
          case "S" -> "shortValue";
          case "I" -> "intValue";
          case "J" -> "longValue";
          case "F" -> "floatValue";
          case "D" -> "doubleValue";
          default -> null;
        };
    }

    private static String primitiveUnboxMethodName(final String primitiveDescriptor) {
        return switch (primitiveDescriptor) {
          case "Z" -> "booleanUnbox";
          case "B" -> "byteUnbox";
          case "C" -> "charUnbox";
          case "S" -> "shortUnbox";
          case "I" -> "intUnbox";
          case "J" -> "longUnbox";
          case "F" -> "floatUnbox";
          case "D" -> "doubleUnbox";
          default -> null;
        };
    }

    private int stackSizeForPendingLoad() {
        return switch (pendingLoadKind) {
          case VARIABLE -> stackSizeForLoadOpcode(pendingLoadOpcode);
          case CONSTANT -> (pendingConstant instanceof Long || pendingConstant instanceof Double) ? 2 : 1;
          case CHECKCAST, BOOLEAN_CONSTANT -> 1;
          case NONE -> 0;
        };
    }

    private int stackSizeForDup() {
        return switch (pendingDupOpcode) {
          case DUP -> 1;
          case DUP2 -> 2;
          default -> 0;
        };
    }

    private int stackSizeForPendingStore() {
        return switch (pendingStoreKind) {
          case VARIABLE -> stackSizeForStoreOpcode(pendingStoreOpcode);
          case STATIC_FIELD -> stackSizeForFieldDescriptor(pendingStoreDescriptor);
          case NONE -> 0;
        };
    }

    private static int stackSizeForPop(final int opcode) {
        return switch (opcode) {
          case POP -> 1;
          case POP2 -> 2;
          default -> 0;
        };
    }

    private static int stackSizeForLoadOpcode(final int opcode) {
        return switch (opcode) {
          case LLOAD, DLOAD -> 2;
          case ILOAD, FLOAD, ALOAD -> 1;
          default -> 0;
        };
    }

    private static int stackSizeForStoreOpcode(final int opcode) {
        return switch (opcode) {
          case LSTORE, DSTORE -> 2;
          case ISTORE, FSTORE, ASTORE -> 1;
          default -> 0;
        };
    }

    private static int stackSizeForFieldDescriptor(final String descriptor) {
        char first = descriptor.charAt(0);
        return (first == 'J' || first == 'D') ? 2 : 1;
    }

    private static boolean isLoadOpcode(final int opcode) {
        return switch (opcode) {
          case ILOAD, LLOAD, FLOAD, DLOAD, ALOAD -> true;
          default -> false;
        };
    }

    private static boolean isStoreOpcode(final int opcode) {
        return switch (opcode) {
          case ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> true;
          default -> false;
        };
    }

    /**
     * Writes {@code value} to the delegate using the densest legal constant form.
     * Integers go through {@link BytecodeHelper#pushConstant} on the ASM delegate
     * so the specialized opcodes are not intercepted and re-buffered by this visitor.
     */
    private void emitConstant(final Object value) {
        if (value == null) {
            super.visitInsn(ACONST_NULL);
            return;
        }

        if (value instanceof BigDecimal || value instanceof BigInteger) {
            emitStringConstructedConstant(value);
            return;
        }

        if (value instanceof Integer intValue) {
            // Emit through the delegate so BytecodeHelper's forms are not re-buffered.
            BytecodeHelper.pushConstant(mv, intValue);
            return;
        }

        if (value instanceof Long longValue) {
            emitLongConstant(longValue);
            return;
        }

        if (value instanceof Float floatValue) {
            emitFloatConstant(floatValue);
            return;
        }

        if (value instanceof Double doubleValue) {
            emitDoubleConstant(doubleValue);
            return;
        }

        super.visitLdcInsn(value);
    }

    /**
     * Lowers a {@link BigDecimal} or {@link BigInteger} constant to
     * {@code new Type(value.toString())} on the delegate.
     */
    private void emitStringConstructedConstant(final Object value) {
        String type = (value instanceof BigDecimal ? BIG_DECIMAL_TYPE : BIG_INTEGER_TYPE);
        super.visitTypeInsn(NEW, type);
        super.visitInsn(DUP);
        super.visitLdcInsn(value.toString());
        super.visitMethodInsn(INVOKESPECIAL, type, "<init>", STRING_CTOR_DESCRIPTOR, false);
    }

    /** Emits {@code LCONST_0}/{@code LCONST_1} when possible, otherwise {@code LDC}. */
    private void emitLongConstant(final long value) {
        if (value == 0L) {
            super.visitInsn(LCONST_0);
        } else if (value == 1L) {
            super.visitInsn(LCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }

    /**
     * Emits {@code FCONST_0/1/2} when the raw bits match; otherwise {@code LDC}.
     * Raw-bit comparison keeps {@code +0.0f} and {@code -0.0f} distinct (GROOVY-9797).
     */
    private void emitFloatConstant(final float value) {
        int rawBits = Float.floatToRawIntBits(value);
        if (rawBits == FLOAT_TO_RAW_INT_BITS_0) {
            super.visitInsn(FCONST_0);
        } else if (rawBits == FLOAT_TO_RAW_INT_BITS_1) {
            super.visitInsn(FCONST_1);
        } else if (rawBits == FLOAT_TO_RAW_INT_BITS_2) {
            super.visitInsn(FCONST_2);
        } else {
            super.visitLdcInsn(value);
        }
    }

    /**
     * Emits {@code DCONST_0/1} when the raw bits match; otherwise {@code LDC}.
     * Raw-bit comparison keeps {@code +0.0d} and {@code -0.0d} distinct (GROOVY-9797).
     */
    private void emitDoubleConstant(final double value) {
        long rawBits = Double.doubleToRawLongBits(value);
        if (rawBits == DOUBLE_TO_RAW_LONG_BITS_0) {
            super.visitInsn(DCONST_0);
        } else if (rawBits == DOUBLE_TO_RAW_LONG_BITS_1) {
            super.visitInsn(DCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }
}
