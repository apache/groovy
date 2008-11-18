/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime.callsite;

import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.MethodHandleFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class CallSiteGenerator {
    private static final String[] EXCEPTIONS = new String[] { "java/lang/Throwable" };

    private CallSiteGenerator () {}

    public static void genCallWithFixedParams(ClassWriter cw, String name, final String superClass, CachedMethod cachedMethod, String receiverType ) {
        MethodVisitor mv;
        if (cachedMethod.getParamsCount() <= 4) {
            StringBuilder pdescb = new StringBuilder();
            final int pc = cachedMethod.getParamsCount();
            for (int i = 0; i != pc; ++i) pdescb.append("Ljava/lang/Object;");

            String pdesc = pdescb.toString();

            mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call" + name, "(L" + receiverType + ";" + pdesc + ")Ljava/lang/Object;", null, EXCEPTIONS);
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            for (int i = 0; i != pc; ++i)
                mv.visitVarInsn(Opcodes.ALOAD, i+2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, superClass, "checkCall", "(Ljava/lang/Object;" + pdesc + ")Z");
            Label l0 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);

            BytecodeHelper helper = new BytecodeHelper(mv);

            Class callClass = cachedMethod.getDeclaringClass().getTheClass();
            boolean useInterface = callClass.isInterface();

            String type = BytecodeHelper.getClassInternalName(callClass.getName());
            String descriptor = BytecodeHelper.getMethodDescriptor(cachedMethod.getReturnType(), cachedMethod.getNativeParameterTypes());

            // make call
            if (cachedMethod.isStatic()) {
                MethodHandleFactory.genLoadParametersDirect(2, mv, helper, cachedMethod.setAccessible());
                mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, cachedMethod.getName(), descriptor);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                helper.doCast(callClass);
                MethodHandleFactory.genLoadParametersDirect(2, mv, helper, cachedMethod.setAccessible());
                mv.visitMethodInsn((useInterface) ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, type, cachedMethod.getName(), descriptor);
            }

            helper.box(cachedMethod.getReturnType());
            if (cachedMethod.getReturnType() == void.class) {
                mv.visitInsn(Opcodes.ACONST_NULL);
            }

            mv.visitInsn(Opcodes.ARETURN);
            mv.visitLabel(l0);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            for (int i = 0; i != pc; ++i)
                mv.visitVarInsn(Opcodes.ALOAD, i+2);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/ArrayUtil", "createArray", "(" + pdesc + ")[Ljava/lang/Object;");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "defaultCall" + name, "(Lorg/codehaus/groovy/runtime/callsite/CallSite;L" + receiverType + ";[Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    public static void getCallXxxWithArray(ClassWriter cw, final String name, final String superClass, CachedMethod cachedMethod, String receiverType) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call" + name, "(L" + receiverType + ";[Ljava/lang/Object;)Ljava/lang/Object;", null, EXCEPTIONS);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, superClass, "checkCall", "(Ljava/lang/Object;[Ljava/lang/Object;)Z");
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l0);

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = cachedMethod.getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(cachedMethod.getReturnType(), cachedMethod.getNativeParameterTypes());

        // make call
        if (cachedMethod.isStatic()) {
            MethodHandleFactory.genLoadParameters(2, mv, helper, cachedMethod.setAccessible());
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, cachedMethod.getName(), descriptor);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            helper.doCast(callClass);
            MethodHandleFactory.genLoadParameters(2, mv, helper, cachedMethod.setAccessible());
            mv.visitMethodInsn((useInterface) ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL, type, cachedMethod.getName(), descriptor);
        }

        helper.box(cachedMethod.getReturnType());
        if (cachedMethod.getReturnType() == void.class) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitLabel(l0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "defaultCall" + name, "(Lorg/codehaus/groovy/runtime/callsite/CallSite;L" + receiverType + ";[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void genConstructor(ClassWriter cw, final String superClass) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    public static byte[] genPogoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", null);

        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite");

        getCallXxxWithArray(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "groovy/lang/GroovyObject");
        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "java/lang/Object");

        genCallWithFixedParams(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "groovy/lang/GroovyObject");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "java/lang/Object");


        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genPojoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", null);

        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite");

        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod, "java/lang/Object");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod, "java/lang/Object");

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genStaticMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", null);

        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite");

        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Object");
        getCallXxxWithArray(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Class");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Object");
        genCallWithFixedParams(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Class");

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static Constructor compilePogoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.setAccessible());

        final byte[] bytes = genPogoMetaMethodSite(cachedMethod, cw, name);

        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static Constructor compilePojoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.setAccessible());

        final byte[] bytes = genPojoMetaMethodSite(cachedMethod, cw, name);


        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static Constructor compileStaticMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.setAccessible());

        final byte[] bytes = genStaticMetaMethodSite(cachedMethod, cw, name);

        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static boolean isCompilable (CachedMethod method) {
        return GroovySunClassLoader.sunVM != null || Modifier.isPublic(method.cachedClass.getModifiers()) && method.isPublic() && publicParams(method);
    }

    private static boolean publicParams(CachedMethod method) {
        for (Class nativeParamType : method.getNativeParameterTypes()) {
            if (!Modifier.isPublic(nativeParamType.getModifiers()))
                return false;
        }
        return true;
    }

}
