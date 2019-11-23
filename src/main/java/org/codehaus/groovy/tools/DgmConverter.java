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
package org.codehaus.groovy.tools;

import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DgmConverter implements Opcodes {

    public static void main(String[] args) throws IOException {
        String targetDirectory = "target/classes/";
        boolean info = (args.length == 1 && args[0].equals("--info"))
                || (args.length==2 && args[0].equals("--info"));
        if (info && args.length==2) {
            targetDirectory = args[1];
            if (!targetDirectory.endsWith("/")) targetDirectory += "/";
        }
        List<CachedMethod> cachedMethodsList = new ArrayList<CachedMethod>();
        for (Class aClass : DefaultGroovyMethods.DGM_LIKE_CLASSES) {
            Collections.addAll(cachedMethodsList, ReflectionCache.getCachedClass(aClass).getMethods());
        }
        final CachedMethod[] cachedMethods = cachedMethodsList.toArray(CachedMethod.EMPTY_ARRAY);

        List<GeneratedMetaMethod.DgmMethodRecord> records = new ArrayList<GeneratedMetaMethod.DgmMethodRecord>();

        int cur = 0;
        for (CachedMethod method : cachedMethods) {
            if (!method.isStatic() || !method.isPublic())
                continue;

            if (method.getAnnotation(Deprecated.class) != null)
                continue;

            if (method.getParameterTypes().length == 0)
                continue;

            final Class returnType = method.getReturnType();

            final String className = "org/codehaus/groovy/runtime/dgm$" + cur++;

            GeneratedMetaMethod.DgmMethodRecord record = new GeneratedMetaMethod.DgmMethodRecord();
            records.add(record);

            record.methodName = method.getName();
            record.returnType = method.getReturnType();
            record.parameters = method.getNativeParameterTypes();
            record.className = className;

            ClassWriter cw = new ClassWriter(CompilerConfiguration.ASM_COMPUTE_MODE);
            cw.visit(CompilerConfiguration.DEFAULT.getAsmTargetBytecode(), ACC_PUBLIC, className, null, "org/codehaus/groovy/reflection/GeneratedMetaMethod", null);

            createConstructor(cw);

            final String methodDescriptor = BytecodeHelper.getMethodDescriptor(returnType, method.getNativeParameterTypes());

            createInvokeMethod(method, cw, returnType, methodDescriptor);

            createDoMethodInvokeMethod(method, cw, className, returnType, methodDescriptor);

            createIsValidMethodMethod(method, cw, className);

            cw.visitEnd();

            final byte[] bytes = cw.toByteArray();

            File targetFile = new File(targetDirectory + className + ".class").getCanonicalFile();
            targetFile.getParentFile().mkdirs();

            try (final FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
            }
        }

        GeneratedMetaMethod.DgmMethodRecord.saveDgmInfo(records, targetDirectory+"/META-INF/dgminfo");
        if (info)
            System.out.println("Saved " + cur + " dgm records to: "+targetDirectory+"/META-INF/dgminfo");
    }

    private static void createConstructor(ClassWriter cw) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;Lorg/codehaus/groovy/reflection/CachedClass;Ljava/lang/Class;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/reflection/GeneratedMetaMethod", "<init>", "(Ljava/lang/String;Lorg/codehaus/groovy/reflection/CachedClass;Ljava/lang/Class;[Ljava/lang/Class;)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void createIsValidMethodMethod(CachedMethod method, ClassWriter cw, String className) {
        MethodVisitor mv;
        if (method.getParamsCount() == 2 && method.getParameterTypes()[0].isNumber && method.getParameterTypes()[1].isNumber) {
            // 1 param meta method
            mv = cw.visitMethod(ACC_PUBLIC, "isValidMethod", "([Ljava/lang/Class;)Z", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 1);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNULL, l0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "getParameterTypes", "()[Lorg/codehaus/groovy/reflection/CachedClass;", false);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/CachedClass", "isAssignableFrom", "(Ljava/lang/Class;)Z", false);
            Label l1 = new Label();
            mv.visitJumpInsn(IFEQ, l1);
            mv.visitLabel(l0);
            mv.visitInsn(ICONST_1);
            Label l2 = new Label();
            mv.visitJumpInsn(GOTO, l2);
            mv.visitLabel(l1);
            mv.visitInsn(ICONST_0);
            mv.visitLabel(l2);
            mv.visitInsn(IRETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    private static void createDoMethodInvokeMethod(CachedMethod method, ClassWriter cw, String className, Class returnType, String methodDescriptor) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL, "doMethodInvoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        if (method.getParamsCount() == 2 && method.getParameterTypes()[0].isNumber && method.getParameterTypes()[1].isNumber) {
            mv.visitVarInsn(ALOAD, 1);
            BytecodeHelper.doCast(mv, method.getParameterTypes()[0].getTheClass());

            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "getParameterTypes", "()[Lorg/codehaus/groovy/reflection/CachedClass;", false);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitInsn(ICONST_0);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/reflection/CachedClass", "coerceArgument", "(Ljava/lang/Object;)Ljava/lang/Object;", false);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = method.getParameterTypes()[1].getTheClass();
            BytecodeHelper.doCast(mv, type);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "coerceArgumentsToClasses", "([Ljava/lang/Object;)[Ljava/lang/Object;", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            BytecodeHelper.doCast(mv, method.getParameterTypes()[0].getTheClass());
            loadParameters(method, 2, mv);
        }
        mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(method.getDeclaringClass().getTheClass()), method.getName(), methodDescriptor, false);
        BytecodeHelper.box(mv, returnType);
        if (method.getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void createInvokeMethod(CachedMethod method, ClassWriter cw, Class returnType, String methodDescriptor) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        BytecodeHelper.doCast(mv, method.getParameterTypes()[0].getTheClass());
        loadParameters(method, 2, mv);
        mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(method.getDeclaringClass().getTheClass()), method.getName(), methodDescriptor, false);
        BytecodeHelper.box(mv, returnType);
        if (method.getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    protected static void loadParameters(CachedMethod method, int argumentIndex, MethodVisitor mv) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length - 1;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            BytecodeHelper.pushConstant(mv, i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i + 1].getTheClass();
            BytecodeHelper.doCast(mv, type);
        }
    }
}
