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
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.GeneratedMetaMethod;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.Logger.Level.INFO;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Type.getMethodType;

/**
 * Generates {@code GeneratedMetaMethod} adapter classes and metadata for the
 * default Groovy methods.
 */
public class DgmConverter {

    private static final System.Logger LOGGER = System.getLogger(DgmConverter.class.getName());
    private static final String TARGET = "TARGET";
    private static final String METHOD_HANDLE_CLASS_NAME = "Ljava/lang/invoke/MethodHandle;";

    private static final String DGM_CLASS_PREFIX = "org/codehaus/groovy/runtime/dgm$";
    private static final String GENERATED_META_METHOD = "org/codehaus/groovy/reflection/GeneratedMetaMethod";
    private static final String ADAPTER_CONSTRUCTOR_DESCRIPTOR = "(Ljava/lang/String;Lorg/codehaus/groovy/reflection/CachedClass;Ljava/lang/Class;[Ljava/lang/Class;)V";

    /** Binary name of the generated adapter factory; {@code GeneratedMetaMethod.Proxy} loads it by this constant. */
    public static final String PROXY_FACTORY_CLASS_NAME = "org/codehaus/groovy/runtime/DgmProxyFactory";
    private static final String PROXY_FACTORY_INTERFACE = GENERATED_META_METHOD + "$ProxyFactory";
    private static final String PROXY_CLASS = GENERATED_META_METHOD + "$Proxy";
    private static final String CREATE_DESCRIPTOR = "(ILjava/lang/String;Lorg/codehaus/groovy/reflection/CachedClass;Ljava/lang/Class;[Ljava/lang/Class;)Lgroovy/lang/MetaMethod;";
    private static final String SHARD_DESCRIPTOR = "(ILjava/lang/String;Lorg/codehaus/groovy/reflection/CachedClass;Ljava/lang/Class;[Ljava/lang/Class;)Ljava/lang/Object;";
    /**
     * Adapters per factory shard method. Each case costs sixteen bytes of
     * bytecode including its switch-table entry, so a shard stays under
     * HotSpot's 8000-byte limit for JIT compilation, with room for DGM to grow.
     */
    public static final int PROXY_FACTORY_SHARD_SIZE = 400;

    /**
     * Generates DGM adapter classes into the target directory.
     *
     * @param args optional {@code --info} flag and target directory
     * @throws IOException if generated classes or metadata cannot be written
     */
    public static void main(String[] args) throws IOException {
        String targetDirectory = "build/classes/";
        boolean info = (args.length == 1 && "--info".equals(args[0]))
                || (args.length==2 && "--info".equals(args[0]));
        if (info && args.length==2) {
            targetDirectory = args[1];
            if (!targetDirectory.endsWith("/")) targetDirectory += "/";
        }
        List<CachedMethod> cachedMethodsList = new ArrayList<>();
        for (Class<?> aClass : DefaultGroovyMethods.DGM_LIKE_CLASSES) {
            Collections.addAll(cachedMethodsList, ReflectionCache.getCachedClass(aClass).getMethods());
        }
        final CachedMethod[] cachedMethods = cachedMethodsList.toArray(CachedMethod.EMPTY_ARRAY);

        List<GeneratedMetaMethod.DgmMethodRecord> records = new ArrayList<>();

        int cur = 0;
        for (CachedMethod method : cachedMethods) {
            if (skipMethod(method)) continue;

            final Class<?> returnType = method.getReturnType();

            final String className = DGM_CLASS_PREFIX + cur++;

            GeneratedMetaMethod.DgmMethodRecord dgmMethodRecord = new GeneratedMetaMethod.DgmMethodRecord();
            records.add(dgmMethodRecord);

            dgmMethodRecord.methodName = method.getName();
            dgmMethodRecord.returnType = method.getReturnType();
            dgmMethodRecord.parameters = method.getNativeParameterTypes();
            dgmMethodRecord.className = className;

            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cw.visit(CompilerConfiguration.DEFAULT.getBytecodeVersion(), ACC_PUBLIC, className, null, GENERATED_META_METHOD, null);

            createConstructor(cw);

            final String methodDescriptor = BytecodeHelper.getMethodDescriptor(returnType, method.getNativeParameterTypes());

            createTargetMethodHandleField(cw, method, className);

            createInvokeMethod(method, cw, returnType, methodDescriptor);

            createDoMethodInvokeMethod(method, cw, className, returnType, methodDescriptor);

            createIsValidMethodMethod(method, cw, className);

            createGetTargetMethodHandleMethod(cw, className);

            cw.visitEnd();

            final byte[] bytes = cw.toByteArray();

            File targetFile = new File(targetDirectory + className + ".class").getCanonicalFile();
            targetFile.getParentFile().mkdirs();

            try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
            }
        }

        writeClass(targetDirectory, PROXY_FACTORY_CLASS_NAME, createProxyFactory(cur));

        GeneratedMetaMethod.DgmMethodRecord.saveDgmInfo(records, targetDirectory+"/META-INF/dgminfo");
        if (info)
            LOGGER.log(INFO, "Saved {0} dgm records to: {1}/META-INF/dgminfo", cur, targetDirectory);

        String metadata = NativeImageMetadataGenerator.write(records, targetDirectory);
        if (info)
            LOGGER.log(INFO, "Saved native-image reachability metadata to: {0}", metadata);
    }

    private static void writeClass(String targetDirectory, String className, byte[] bytes) throws IOException {
        File targetFile = new File(targetDirectory + className + ".class").getCanonicalFile();
        targetFile.getParentFile().mkdirs();
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            fileOutputStream.write(bytes);
            fileOutputStream.flush();
        }
    }

    /**
     * Generates {@code DgmProxyFactory}: a {@code GeneratedMetaMethod.ProxyFactory}
     * whose {@code create} method instantiates adapter {@code index} with a
     * direct constructor call. The adapters are referenced statically, so
     * native-image analysis reaches them without reflection metadata, and the
     * class loads none of them eagerly: the shard methods return
     * {@code Object}, which spares the verifier the subtype checks that would
     * otherwise load every adapter when the factory is linked.
     * <p>
     * Layout:
     * <pre>
     * public final class DgmProxyFactory implements GeneratedMetaMethod.ProxyFactory {
     *     static { GeneratedMetaMethod.Proxy.register(new DgmProxyFactory()); }
     *     public MetaMethod create(int index, String n, CachedClass c, Class r, Class[] p) {
     *         switch (index / SHARD_SIZE) { case 0: return (MetaMethod) create$0(index, n, c, r, p); ... default: return null; }
     *     }
     *     private static Object create$0(int index, ...) {
     *         switch (index) { case 0: return new dgm$0(n, c, r, p); ... default: return null; }
     *     }
     * }
     * </pre>
     *
     * @param adapterCount the number of generated adapters, {@code dgm$0} to {@code dgm$(adapterCount - 1)}
     */
    static byte[] createProxyFactory(int adapterCount) {
        int shards = (adapterCount + PROXY_FACTORY_SHARD_SIZE - 1) / PROXY_FACTORY_SHARD_SIZE;

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(CompilerConfiguration.DEFAULT.getBytecodeVersion(), ACC_PUBLIC | ACC_FINAL | ACC_SUPER,
                PROXY_FACTORY_CLASS_NAME, null, "java/lang/Object", new String[]{PROXY_FACTORY_INTERFACE});

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // static { GeneratedMetaMethod.Proxy.register(new DgmProxyFactory()); }
        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, PROXY_FACTORY_CLASS_NAME);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, PROXY_FACTORY_CLASS_NAME, "<init>", "()V", false);
        mv.visitMethodInsn(INVOKESTATIC, PROXY_CLASS, "register", "(L" + PROXY_FACTORY_INTERFACE + ";)V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // public MetaMethod create(int index, String name, CachedClass declaringClass, Class returnType, Class[] parameters)
        mv = cw.visitMethod(ACC_PUBLIC, "create", CREATE_DESCRIPTOR, null, null);
        mv.visitCode();
        Label createDefault = new Label();
        if (shards > 0) {
            Label[] shardLabels = new Label[shards];
            for (int i = 0; i < shards; i++) shardLabels[i] = new Label();
            mv.visitVarInsn(ILOAD, 1);
            BytecodeHelper.pushConstant(mv, PROXY_FACTORY_SHARD_SIZE);
            mv.visitInsn(IDIV);
            mv.visitTableSwitchInsn(0, shards - 1, createDefault, shardLabels);
            for (int i = 0; i < shards; i++) {
                mv.visitLabel(shardLabels[i]);
                mv.visitVarInsn(ILOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitVarInsn(ALOAD, 5);
                mv.visitMethodInsn(INVOKESTATIC, PROXY_FACTORY_CLASS_NAME, "create$" + i, SHARD_DESCRIPTOR, false);
                mv.visitTypeInsn(CHECKCAST, "groovy/lang/MetaMethod");
                mv.visitInsn(ARETURN);
            }
        }
        mv.visitLabel(createDefault);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // private static Object create$N(int index, String name, CachedClass declaringClass, Class returnType, Class[] parameters)
        for (int shard = 0; shard < shards; shard++) {
            int low = shard * PROXY_FACTORY_SHARD_SIZE;
            int high = Math.min(low + PROXY_FACTORY_SHARD_SIZE, adapterCount) - 1;
            mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC, "create$" + shard, SHARD_DESCRIPTOR, null, null);
            mv.visitCode();
            Label shardDefault = new Label();
            Label[] caseLabels = new Label[high - low + 1];
            for (int i = 0; i < caseLabels.length; i++) caseLabels[i] = new Label();
            mv.visitVarInsn(ILOAD, 0);
            mv.visitTableSwitchInsn(low, high, shardDefault, caseLabels);
            for (int index = low; index <= high; index++) {
                String adapter = DGM_CLASS_PREFIX + index;
                mv.visitLabel(caseLabels[index - low]);
                mv.visitTypeInsn(NEW, adapter);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitVarInsn(ALOAD, 3);
                mv.visitVarInsn(ALOAD, 4);
                mv.visitMethodInsn(INVOKESPECIAL, adapter, "<init>", ADAPTER_CONSTRUCTOR_DESCRIPTOR, false);
                mv.visitInsn(ARETURN);
            }
            mv.visitLabel(shardDefault);
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private static boolean skipMethod(CachedMethod method) {
        if (!method.isStatic() || !method.isPublic())
            return true;

        if (method.getAnnotation(Deprecated.class) != null)
            return true;

        return method.getParameterTypes().length == 0;
    }

    private static void createConstructor(ClassWriter cw) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", ADAPTER_CONSTRUCTOR_DESCRIPTOR, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, GENERATED_META_METHOD, "<init>", ADAPTER_CONSTRUCTOR_DESCRIPTOR, false);
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

    /**
     * Generates bytecode for method invocation with argument coercion
     */
    private static void createDoMethodInvokeMethod(CachedMethod method, ClassWriter cw, String className, Class<?> returnType, String methodDescriptor) {
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
            Class<?> type = method.getParameterTypes()[1].getTheClass();
            BytecodeHelper.doCast(mv, type);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, className, "coerceArgumentsToClasses", "([Ljava/lang/Object;)[Ljava/lang/Object;", false);
            mv.visitVarInsn(ASTORE, 2);
            mv.visitVarInsn(ALOAD, 1);
            BytecodeHelper.doCast(mv, method.getParameterTypes()[0].getTheClass());
            loadParameters(method, mv);
        }
        writeMethodCall(method, returnType, methodDescriptor, mv);
    }

    private static void writeMethodCall(CachedMethod method, Class<?> returnType, String methodDescriptor, MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(method.getDeclaringClass().getTheClass()), method.getName(), methodDescriptor, false);
        // Handles primitive return types via autoboxing
        if (returnType == void.class) {
            mv.visitInsn(ACONST_NULL);
        } else if (returnType.isPrimitive()) {
            Class<?> wrapperType = TypeUtil.autoboxType(returnType);
            mv.visitMethodInsn(INVOKESTATIC, BytecodeHelper.getClassInternalName(wrapperType), "valueOf", "(" + BytecodeHelper.getTypeDescription(returnType) + ")" + BytecodeHelper.getTypeDescription(wrapperType), false);
        }
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Generates bytecode for method invocation with return handling
     */
    private static void createInvokeMethod(CachedMethod method, ClassWriter cw, Class<?> returnType, String methodDescriptor) {
        MethodVisitor mv;
        mv = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        BytecodeHelper.doCast(mv, method.getParameterTypes()[0].getTheClass());
        loadParameters(method, mv);
        writeMethodCall(method, returnType, methodDescriptor, mv);
    }

    /**
     * Loads and casts the non-receiver arguments for the supplied cached
     * method from an {@code Object[]} local variable.
     *
     * @param method the cached method whose parameters are being loaded
     * @param mv the visitor receiving the bytecode instructions
     */
    protected static void loadParameters(CachedMethod method, MethodVisitor mv) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length - 1;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, 2);
            BytecodeHelper.pushConstant(mv, i);
            mv.visitInsn(AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class<?> type = parameters[i + 1].getTheClass();
            BytecodeHelper.doCast(mv, type);
        }
    }

    private static void createTargetMethodHandleField(ClassWriter cw, CachedMethod method, String className) {
        // private static final java.lang.invoke.MethodHandle TARGET
        cw.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL,
            TARGET, METHOD_HANDLE_CLASS_NAME, null, null).visitEnd();

        // static initializer
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        // Lookup lookup = java.lang.invoke.MethodHandles.lookup()
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup",
            "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
        // Class ownerClass = <declaring class>.class
        String ownerInternal = BytecodeHelper.getClassInternalName(method.getDeclaringClass().getTheClass());
        mv.visitLdcInsn(Type.getObjectType(ownerInternal));
        // String methodName = "<method name>"
        mv.visitLdcInsn(method.getName());
        // MethodType methodType = MethodType.methodType(<return>, <param1>, <param2>, ...)
        mv.visitLdcInsn(getMethodType(method.getDescriptor()));
        // TARGET = lookup.findStatic(ownerClass, methodName, methodType)
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic",
            "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
        mv.visitFieldInsn(PUTSTATIC, className, TARGET, METHOD_HANDLE_CLASS_NAME);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private static void createGetTargetMethodHandleMethod(ClassWriter cw, String className) {
        MethodVisitor mv;
        // public MethodHandle getTargetMethodHandle() { return TARGET; }
        mv = cw.visitMethod(ACC_PUBLIC, "getTargetMethodHandle",
            "()Ljava/lang/invoke/MethodHandle;", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, className, TARGET, METHOD_HANDLE_CLASS_NAME);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
