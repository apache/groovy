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
package org.apache.groovy.internal.runtime.invoke;

import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Emits a {@link DirectInvoker} class for one {@link Method}.
 *
 * <p>Two encodings, same public shape ({@code public final} class, public
 * no-arg {@code <init>}, {@code invoke(Object, Object[])}):
 * <ul>
 *   <li>direct {@code INVOKE*} of the target (Steps 1, 2, 4)</li>
 *   <li>{@code ConstantDynamic} classData {@code MethodHandle} +
 *       {@code invokeExact} (Step 3)</li>
 * </ul>
 *
 * Package-private. Dummy internal names live in this package so
 * {@code HiddenClassDefiner.alignPackage} rewrites them onto the nest host.
 */
final class InvokerBytecode {

    private static final String OBJECT = "java/lang/Object";
    private static final String DIRECT_INVOKER = Type.getInternalName(DirectInvoker.class);
    private static final String INVOKE_DESC = "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;";
    private static final String METHOD_HANDLE_DESC = "Ljava/lang/invoke/MethodHandle;";
    private static final String METHOD_HANDLE_INTERNAL = "java/lang/invoke/MethodHandle";
    private static final String THROWABLE = "java/lang/Throwable";

    /**
     * BSM for {@link java.lang.invoke.MethodHandles#classData} — already has
     * the {@code ConstantDynamic} bootstrap signature, so {@code <clinit>} is
     * not required and there is no checked {@code IllegalAccessException}.
     */
    private static final Handle CLASS_DATA_BSM = new Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/MethodHandles",
            "classData",
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;",
            false);

    private static final AtomicInteger NAMES = new AtomicInteger();

    private InvokerBytecode() {
    }

    /**
     * Unique dummy internal name in this package. Hidden-class define rewrites
     * the package to the nest host; the suffix keeps {@code javap} dumps readable.
     */
    static String nextInternalName() {
        return DIRECT_INVOKER.substring(0, DIRECT_INVOKER.lastIndexOf('/') + 1)
                + "MHInvoker$" + NAMES.getAndIncrement();
    }

    static byte[] emitInvokeStar(final Method method) {
        return emitInvokeStar(method, nextInternalName());
    }

    static byte[] emitInvokeStar(final Method method, final String internalName) {
        return emit(method, internalName, false);
    }

    static byte[] emitClassData(final Method method) {
        return emit(method, nextInternalName(), true);
    }

    private static byte[] emit(final Method method, final String internalName, final boolean classData) {
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                CompilerConfiguration.DEFAULT.getBytecodeVersion(),
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SUPER | Opcodes.ACC_SYNTHETIC,
                internalName,
                null,
                OBJECT,
                new String[]{DIRECT_INVOKER});

        emitConstructor(cw);
        emitInvoke(cw, method, classData);

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static void emitConstructor(final ClassWriter cw) {
        final MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OBJECT, "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void emitInvoke(final ClassWriter cw, final Method method, final boolean classData) {
        final MethodVisitor mv = cw.visitMethod(
                Opcodes.ACC_PUBLIC, "invoke", INVOKE_DESC, null, new String[]{THROWABLE});
        mv.visitCode();

        if (classData) {
            mv.visitLdcInsn(new ConstantDynamic("_", METHOD_HANDLE_DESC, CLASS_DATA_BSM));
            mv.visitTypeInsn(Opcodes.CHECKCAST, METHOD_HANDLE_INTERNAL);
        }

        loadReceiverAndArguments(mv, method);

        if (classData) {
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    METHOD_HANDLE_INTERNAL,
                    "invokeExact",
                    invokeExactDescriptor(method),
                    false);
        } else {
            final Class<?> declaring = method.getDeclaringClass();
            mv.visitMethodInsn(
                    invokeOpcode(method),
                    BytecodeHelper.getClassInternalName(declaring),
                    method.getName(),
                    BytecodeHelper.getMethodDescriptor(method.getReturnType(), method.getParameterTypes()),
                    declaring.isInterface());
        }

        boxAndReturn(mv, method.getReturnType());
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Receiver (instance only) then each argument, with {@link BytecodeHelper#doCast}.
     * The Java wrapper already substitutes {@code EMPTY_ARRAY} for a null
     * {@code arguments} local, so the bytecode assumes a non-null array.
     */
    private static void loadReceiverAndArguments(final MethodVisitor mv, final Method method) {
        final boolean isStatic = Modifier.isStatic(method.getModifiers());
        if (!isStatic) {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            BytecodeHelper.doCast(mv, method.getDeclaringClass());
        }
        final Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            BytecodeHelper.pushConstant(mv, i);
            mv.visitInsn(Opcodes.AALOAD);
            BytecodeHelper.doCast(mv, params[i]);
        }
    }

    static int invokeOpcode(final Method method) {
        final int mods = method.getModifiers();
        if (Modifier.isStatic(mods)) {
            return Opcodes.INVOKESTATIC;
        }
        if (Modifier.isPrivate(mods)) {
            // Hidden nestmates extend Object; they do not subclass / implement
            // the declaring type. INVOKESPECIAL of a non-<init> method then
            // fails verification ("current class isn't assignable to reference
            // class"). JEP 371: use invokevirtual / invokeinterface instead.
            // itf is still declaring.isInterface() at the call site.
            if (method.getDeclaringClass().isInterface()) {
                return Opcodes.INVOKEINTERFACE;
            }
            return Opcodes.INVOKEVIRTUAL;
        }
        if (method.getDeclaringClass().isInterface()) {
            return Opcodes.INVOKEINTERFACE;
        }
        return Opcodes.INVOKEVIRTUAL;
    }

    /**
     * {@code invokeExact} descriptor: instance prepends the declaring type;
     * return is the method's (possibly primitive) type.
     */
    static String invokeExactDescriptor(final Method method) {
        final StringBuilder sb = new StringBuilder(64);
        sb.append('(');
        if (!Modifier.isStatic(method.getModifiers())) {
            sb.append(BytecodeHelper.getTypeDescription(method.getDeclaringClass()));
        }
        for (Class<?> p : method.getParameterTypes()) {
            sb.append(BytecodeHelper.getTypeDescription(p));
        }
        sb.append(')');
        sb.append(BytecodeHelper.getTypeDescription(method.getReturnType()));
        return sb.toString();
    }

    private static void boxAndReturn(final MethodVisitor mv, final Class<?> returnType) {
        if (returnType == void.class) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else if (returnType.isPrimitive()) {
            final Class<?> wrapper = TypeUtil.autoboxType(returnType);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    BytecodeHelper.getClassInternalName(wrapper),
                    "valueOf",
                    "(" + BytecodeHelper.getTypeDescription(returnType) + ")"
                            + BytecodeHelper.getTypeDescription(wrapper),
                    false);
        }
        mv.visitInsn(Opcodes.ARETURN);
    }
}
