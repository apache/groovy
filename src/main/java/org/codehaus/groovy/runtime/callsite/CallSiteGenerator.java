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
package org.codehaus.groovy.runtime.callsite;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.android.AndroidSupport;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class CallSiteGenerator {

    private static final String GRE = BytecodeHelper.getClassInternalName(ClassHelper.make(GroovyRuntimeException.class));
    
    private CallSiteGenerator () {}
    
    private static MethodVisitor writeMethod(ClassWriter cw, String name, int argumentCount, final String superClass, CachedMethod cachedMethod, String receiverType, String parameterDescription, boolean useArray) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call" + name, "(L" + receiverType + ";" + parameterDescription + ")Ljava/lang/Object;", null, null);
        mv.visitCode();
        
        final Label tryStart = new Label();
        mv.visitLabel(tryStart);
        
        // call for checking if method is still valid
        for (int i = 0; i < argumentCount; ++i) mv.visitVarInsn(Opcodes.ALOAD, i);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, superClass, "checkCall", "(Ljava/lang/Object;" + parameterDescription + ")Z", false);
        Label l0 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l0);
        
        // valid method branch

        Class callClass = cachedMethod.getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(cachedMethod.getReturnType(), cachedMethod.getNativeParameterTypes());
        
        // prepare call
        int invokeMethodCode = Opcodes.INVOKEVIRTUAL;
        if (cachedMethod.isStatic()) {
            invokeMethodCode = Opcodes.INVOKESTATIC;
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            BytecodeHelper.doCast(mv, callClass);
            if (useInterface) invokeMethodCode = Opcodes.INVOKEINTERFACE;
        }

        Class<?>[] parameters = cachedMethod.getPT();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            if (useArray) {
                // unpack argument from Object[]
                mv.visitVarInsn(Opcodes.ALOAD, 2);
                BytecodeHelper.pushConstant(mv, i);
                mv.visitInsn(Opcodes.AALOAD);
            } else {
                mv.visitVarInsn(Opcodes.ALOAD, i+2);
            }

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            BytecodeHelper.doCast(mv, parameters[i]);
        }        
        
        // make call
        mv.visitMethodInsn(invokeMethodCode, type, cachedMethod.getName(), descriptor, useInterface);

        // produce result
        BytecodeHelper.box(mv, cachedMethod.getReturnType());
        if (cachedMethod.getReturnType() == void.class) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }

        // return
        mv.visitInsn(Opcodes.ARETURN);
        
        // fall back after method change
        mv.visitLabel(l0);
        for (int i = 0; i < argumentCount; ++i) mv.visitVarInsn(Opcodes.ALOAD, i);
        if (!useArray) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/ArrayUtil", "createArray", "(" + parameterDescription + ")[Ljava/lang/Object;", false);
        }
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "defaultCall" + name, "(Lorg/codehaus/groovy/runtime/callsite/CallSite;L" + receiverType + ";[Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitInsn(Opcodes.ARETURN);
        
        // exception unwrapping for stackless exceptions
        final Label tryEnd = new Label();
        mv.visitLabel(tryEnd);
        final Label catchStart = new Label();
        mv.visitLabel(catchStart);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/ScriptBytecodeAdapter", "unwrap", "(Lgroovy/lang/GroovyRuntimeException;)Ljava/lang/Throwable;", false);
        mv.visitInsn(Opcodes.ATHROW);        
        mv.visitTryCatchBlock(tryStart, tryEnd, catchStart, GRE);
        
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        return mv;
    }
    

    public static void genCallWithFixedParams(ClassWriter cw, String name, final String superClass, CachedMethod cachedMethod, String receiverType ) {
        if (cachedMethod.getParamsCount() > 4) return;
        
        StringBuilder pdescb = new StringBuilder();
        final int pc = cachedMethod.getParamsCount();
        for (int i = 0; i != pc; ++i) pdescb.append("Ljava/lang/Object;");
        
        writeMethod(cw,name,pc+2,superClass,cachedMethod,receiverType,pdescb.toString(),false);
    }

    public static void genCallXxxWithArray(ClassWriter cw, final String name, final String superClass, CachedMethod cachedMethod, String receiverType) {
        writeMethod(cw,name,3,superClass,cachedMethod,receiverType,"[Ljava/lang/Object;",true);
    }

    private static void genConstructor(ClassWriter cw, final String superClass, String internalName) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;Ljava/lang/reflect/Constructor;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClass, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", false);

        mv.visitVarInsn(Opcodes.ALOAD, 5);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, internalName, "__constructor__", "Ljava/lang/reflect/Constructor;");

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void classHeader(ClassWriter cw, String internalName, String superName) {
        if (VMPluginFactory.getPlugin().getVersion()>=8) {
            cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, internalName, null, superName, null);
        } else {
            cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, internalName, null, superName, null);
        }
    }

    public static byte[] genPogoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        String internalName = name.replace('.', '/');
        classHeader(cw, internalName, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite");
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "__constructor__", "Ljava/lang/reflect/Constructor;", null, null);

        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", internalName);

        genCallXxxWithArray(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "groovy/lang/GroovyObject");
        genCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "java/lang/Object");

        genCallWithFixedParams(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "groovy/lang/GroovyObject");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod, "java/lang/Object");


        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genPojoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        String internalName = name.replace('.', '/');
        classHeader(cw, internalName, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite");
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "__constructor__", "Ljava/lang/reflect/Constructor;", null, null);

        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", internalName);

        genCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod, "java/lang/Object");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod, "java/lang/Object");

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genStaticMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        String internalName = name.replace('.', '/');
        classHeader(cw, internalName, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite");
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "__constructor__", "Ljava/lang/reflect/Constructor;", null, null);
 
        genConstructor(cw, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", internalName);

        genCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Object");
        genCallXxxWithArray(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Class");
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Object");
        genCallWithFixedParams(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod, "java/lang/Class");

        cw.visitEnd();

        return cw.toByteArray();
    }

    private static ClassWriter makeClassWriter() {
        if (VMPluginFactory.getPlugin().getVersion()>=8) {
            return new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        } else {
            return new ClassWriter(ClassWriter.COMPUTE_MAXS);
        }
    }

    public static Constructor compilePogoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = makeClassWriter();

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.getName());

        final byte[] bytes = genPogoMetaMethodSite(cachedMethod, cw, name);
        
        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static Constructor compilePojoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = makeClassWriter();

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.getName());

        final byte[] bytes = genPojoMetaMethodSite(cachedMethod, cw, name);
        
        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static Constructor compileStaticMethod(CachedMethod cachedMethod) {
        ClassWriter cw = makeClassWriter();

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final CallSiteClassLoader callSiteLoader = declClass.getCallSiteLoader();
        final String name = callSiteLoader.createClassName(cachedMethod.getName());

        final byte[] bytes = genStaticMetaMethodSite(cachedMethod, cw, name);
        
        return callSiteLoader.defineClassAndGetConstructor(name, bytes);
    }

    public static boolean isCompilable (CachedMethod method) {
        return (GroovySunClassLoader.sunVM != null || Modifier.isPublic(method.cachedClass.getModifiers()) && method.isPublic() && publicParams(method))
                && !AndroidSupport.isRunningAndroid() && containsOnlyValidChars(method.getName());
    }

    private static boolean publicParams(CachedMethod method) {
        for (Class nativeParamType : method.getNativeParameterTypes()) {
            if (!Modifier.isPublic(nativeParamType.getModifiers()))
                return false;
        }
        return true;
    }

    private static boolean containsOnlyValidChars(String name) {
        // TODO: this might not do enough or too much
        // But it is a good start without spreading logic everywhere
        String encoded = GeneratorContext.encodeAsValidClassName(name);
        return encoded.equals(name);
    }

}
