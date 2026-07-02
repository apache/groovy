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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

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
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
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
 * Single-pass bytecode compaction inspired by Groovy++'s peephole adapters.
 * The visitor only buffers the current stack-local candidate and flushes before
 * labels, frames, debug metadata, and other non-local boundaries.
 */
public final class PeepholeOptimizingMethodVisitor extends MethodVisitor {

    private static final String BIG_DECIMAL_TYPE = "java/math/BigDecimal";
    private static final String BIG_INTEGER_TYPE = "java/math/BigInteger";
    private static final String STRING_CTOR_DESCRIPTOR = "(Ljava/lang/String;)V";
    private static final int NO_OPCODE = -1;

    private enum PendingLoadKind {
        NONE,
        CONSTANT,
        VARIABLE,
        CHECKCAST
    }

    private enum PendingStoreKind {
        NONE,
        VARIABLE,
        STATIC_FIELD
    }

    private PendingLoadKind pendingLoadKind = PendingLoadKind.NONE;
    private Object pendingConstant;
    private int pendingLoadOpcode = NO_OPCODE;
    private int pendingLoadVar = NO_OPCODE;
    private boolean pendingLoadHasIinc;
    private int pendingLoadIncrement;
    private String pendingCheckcastDescriptor;

    private int pendingDupOpcode = NO_OPCODE;
    private PendingStoreKind pendingStoreKind = PendingStoreKind.NONE;
    private int pendingStoreOpcode = NO_OPCODE;
    private int pendingStoreVar = NO_OPCODE;
    private String pendingStoreOwner;
    private String pendingStoreName;
    private String pendingStoreDescriptor;

    public PeepholeOptimizingMethodVisitor(final MethodVisitor delegate) {
        super(CompilerConfiguration.ASM_API_VERSION, delegate);
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
        if (tryRemovePendingLoad(opcode) || tryDropPendingLoadOnReturn(opcode) || tryRemovePendingDupStore(opcode)) {
            return;
        }

        flushPendingLoad();
        if (opcode == DUP || opcode == DUP2) {
            flushPendingDupStore();
            pendingDupOpcode = opcode;
            return;
        }

        flushPendingDupStore();
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
            bufferVariableStore(opcode, varIndex);
            return;
        }

        flushPendingDupStore();
        if (isLoadOpcode(opcode)) {
            bufferVariableLoad(opcode, varIndex);
            return;
        }
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String descriptor) {
        flushPendingLoad();
        flushPendingDupStore();
        if (opcode == CHECKCAST) {
            bufferCheckcast(descriptor);
            return;
        }
        super.visitTypeInsn(opcode, descriptor);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
        flushPendingLoad();
        if (pendingDupOpcode != NO_OPCODE && pendingStoreKind == PendingStoreKind.NONE && opcode == PUTSTATIC) {
            bufferStaticStore(opcode, owner, name, descriptor);
            return;
        }

        flushPendingDupStore();
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
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
        if (tryRewriteZeroCompare(opcode, label)) {
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
                && !pendingLoadHasIinc) {
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

    private void flushPending() {
        flushPendingLoad();
        flushPendingDupStore();
    }

    private void flushPendingLoad() {
        switch (pendingLoadKind) {
          case CONSTANT:
            emitConstant(pendingConstant);
            break;
          case VARIABLE:
            super.visitVarInsn(pendingLoadOpcode, pendingLoadVar);
            if (pendingLoadHasIinc) {
                super.visitIincInsn(pendingLoadVar, pendingLoadIncrement);
            }
            break;
          case CHECKCAST:
            super.visitTypeInsn(CHECKCAST, pendingCheckcastDescriptor);
            break;
          case NONE:
          default:
        }
        clearPendingLoad();
    }

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

    private boolean tryRewriteZeroCompare(final int opcode, final Label label) {
        if (!(pendingLoadKind == PendingLoadKind.CONSTANT && pendingConstant instanceof Integer intValue && intValue == 0)) {
            return false;
        }

        int replacement = switch (opcode) {
          case IF_ICMPEQ -> IFEQ;
          case IF_ICMPNE -> IFNE;
          case IF_ICMPGE -> IFGE;
          case IF_ICMPGT -> IFGT;
          case IF_ICMPLE -> IFLE;
          case IF_ICMPLT -> IFLT;
          default -> NO_OPCODE;
        };
        if (replacement == NO_OPCODE) {
            return false;
        }

        clearPendingLoad();
        super.visitJumpInsn(replacement, label);
        return true;
    }

    private boolean tryRemovePendingLoad(final int opcode) {
        if (pendingLoadKind == PendingLoadKind.NONE) {
            return false;
        }

        int popSize = stackSizeForPop(opcode);
        if (popSize == 0) {
            return false;
        }

        if (pendingLoadKind == PendingLoadKind.CHECKCAST) {
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

        if (pendingLoadKind == PendingLoadKind.VARIABLE && pendingLoadHasIinc) {
            super.visitIincInsn(pendingLoadVar, pendingLoadIncrement);
        }
        clearPendingLoad();
        return true;
    }

    private boolean tryDropPendingLoadOnReturn(final int opcode) {
        if (opcode != RETURN || pendingLoadKind == PendingLoadKind.NONE) {
            return false;
        }

        if (pendingLoadKind == PendingLoadKind.VARIABLE && pendingLoadHasIinc) {
            super.visitIincInsn(pendingLoadVar, pendingLoadIncrement);
        }
        clearPendingLoad();
        super.visitInsn(RETURN);
        return true;
    }

    private boolean tryRemovePendingDupStore(final int opcode) {
        if (pendingStoreKind == PendingStoreKind.NONE) {
            return false;
        }

        int popSize = stackSizeForPop(opcode);
        if (popSize == 0 || stackSizeForDup() != popSize || stackSizeForPendingStore() != popSize) {
            return false;
        }

        emitPendingStore();
        clearPendingDupStore();
        return true;
    }

    private void bufferConstant(final Object value) {
        clearPendingLoad();
        pendingLoadKind = PendingLoadKind.CONSTANT;
        pendingConstant = value;
    }

    private void bufferVariableLoad(final int opcode, final int varIndex) {
        clearPendingLoad();
        pendingLoadKind = PendingLoadKind.VARIABLE;
        pendingLoadOpcode = opcode;
        pendingLoadVar = varIndex;
    }

    private void bufferCheckcast(final String descriptor) {
        clearPendingLoad();
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

    private int stackSizeForPendingLoad() {
        return switch (pendingLoadKind) {
          case VARIABLE -> stackSizeForLoadOpcode(pendingLoadOpcode);
          case CONSTANT -> (pendingConstant instanceof Long || pendingConstant instanceof Double) ? 2 : 1;
          case CHECKCAST -> 1;
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
            emitIntConstant(intValue);
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

    private void emitStringConstructedConstant(final Object value) {
        String type = (value instanceof BigDecimal ? BIG_DECIMAL_TYPE : BIG_INTEGER_TYPE);
        super.visitTypeInsn(NEW, type);
        super.visitInsn(DUP);
        super.visitLdcInsn(value.toString());
        super.visitMethodInsn(INVOKESPECIAL, type, "<init>", STRING_CTOR_DESCRIPTOR, false);
    }

    private void emitIntConstant(final int value) {
        switch (value) {
          case -1:
            super.visitInsn(ICONST_M1);
            return;
          case 0:
            super.visitInsn(ICONST_0);
            return;
          case 1:
            super.visitInsn(ICONST_1);
            return;
          case 2:
            super.visitInsn(ICONST_2);
            return;
          case 3:
            super.visitInsn(ICONST_3);
            return;
          case 4:
            super.visitInsn(ICONST_4);
            return;
          case 5:
            super.visitInsn(ICONST_5);
            return;
          default:
        }

        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            super.visitIntInsn(BIPUSH, value);
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            super.visitIntInsn(SIPUSH, value);
        } else {
            super.visitLdcInsn(value);
        }
    }

    private void emitLongConstant(final long value) {
        if (value == 0L) {
            super.visitInsn(LCONST_0);
        } else if (value == 1L) {
            super.visitInsn(LCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }

    private void emitFloatConstant(final float value) {
        int rawBits = Float.floatToRawIntBits(value);
        if (rawBits == Float.floatToRawIntBits(0f)) {
            super.visitInsn(FCONST_0);
        } else if (rawBits == Float.floatToRawIntBits(1f)) {
            super.visitInsn(FCONST_1);
        } else if (rawBits == Float.floatToRawIntBits(2f)) {
            super.visitInsn(FCONST_2);
        } else {
            super.visitLdcInsn(value);
        }
    }

    private void emitDoubleConstant(final double value) {
        long rawBits = Double.doubleToRawLongBits(value);
        if (rawBits == Double.doubleToRawLongBits(0d)) {
            super.visitInsn(DCONST_0);
        } else if (rawBits == Double.doubleToRawLongBits(1d)) {
            super.visitInsn(DCONST_1);
        } else {
            super.visitLdcInsn(value);
        }
    }
}
