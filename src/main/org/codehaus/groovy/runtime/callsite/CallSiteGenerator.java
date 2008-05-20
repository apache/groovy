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

import org.objectweb.asm.*;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.classgen.BytecodeHelper;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.io.InputStream;
import java.io.IOException;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;

public class CallSiteGenerator {
    private static final SunClassLoader sunVM;

    static {
        Class cls;
        try {
            cls = ClassLoader.getSystemClassLoader().loadClass("sun.reflect.MagicAccessorImpl");
        } catch (Throwable e) {
            cls = null;
        }

        if (cls != null) {
            final Class cls1 = cls;
            SunClassLoader res;
            try {
                res = AccessController.doPrivileged(new PrivilegedAction<SunClassLoader>() {
                    public SunClassLoader run() {
                        try {
                            return new SunClassLoader(cls1);
                        } catch (Throwable e) {
                            return null;
                        }
                    }
                });
            }
            catch (Throwable e) {
                res = null;
            }
            sunVM = res;
        }
        else {
            sunVM = null;
        }
    }

    private CallSiteGenerator () {
    }

    public static void genCallWithFixedParams(ClassWriter cw, String name, final String superClass, CachedMethod cachedMethod) {
        MethodVisitor mv;
        if (cachedMethod.getParamsCount() <= 4)
        {
            StringBuilder pdescb = new StringBuilder();
            final int pc = cachedMethod.getParamsCount();
            for (int i = 0; i != pc; ++i)
              pdescb.append("Ljava/lang/Object;");

            String pdesc = pdescb.toString();

        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call" + name, "(Ljava/lang/Object;" + pdesc + ")Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
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
            genLoadParametersDirect(2, mv, helper, cachedMethod);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, cachedMethod.getName(), descriptor);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            helper.doCast(callClass);
            genLoadParametersDirect(2, mv, helper, cachedMethod);
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
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "defaultCall" + name, "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }
    }

    public static void getCallXxxWithArray(ClassWriter cw, final String name, final String superClass, CachedMethod cachedMethod) {
        MethodVisitor mv;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "call" + name, "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
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
            genLoadParameters(2, mv, helper, cachedMethod);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, type, cachedMethod.getName(), descriptor);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            helper.doCast(callClass);
            genLoadParameters(2, mv, helper, cachedMethod);
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
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "defaultCall" + name, "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    protected static void genLoadParameters(int argumentIndex, MethodVisitor mv, BytecodeHelper helper, CachedMethod method) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(Opcodes.ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(Opcodes.AALOAD);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i].getTheClass();
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }

    protected static void genLoadParametersDirect(int argumentIndex, MethodVisitor mv, BytecodeHelper helper, CachedMethod method) {
        CachedClass[] parameters = method.getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(Opcodes.ALOAD, argumentIndex+i);

            // cast argument to parameter class, inclusive unboxing
            // for methods with primitive types
            Class type = parameters[i].getTheClass();
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }

    public static byte[] genPogoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", null);

        {
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }

        getCallXxxWithArray(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod);
        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod);

        genCallWithFixedParams(cw, "Current", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod);
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", cachedMethod);


        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genPojoMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", null);

        {
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }

        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod);
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/PojoMetaMethodSite", cachedMethod);

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static byte[] genStaticMetaMethodSite(CachedMethod cachedMethod, ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", null);

        {
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitVarInsn(Opcodes.ALOAD, 3);
        mv.visitVarInsn(Opcodes.ALOAD, 4);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }

        getCallXxxWithArray(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod);
        getCallXxxWithArray(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod);
        genCallWithFixedParams(cw, "", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod);
        genCallWithFixedParams(cw, "Static", "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", cachedMethod);

        cw.visitEnd();

        return cw.toByteArray();
    }

    public static Constructor compilePogoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final Class acls = declClass.getTheClass();
        final String name = createCallSiteClassName(cachedMethod, declClass, acls);

        final byte[] bytes = genPogoMetaMethodSite(cachedMethod, cw, name);

        final Class pogoSiteClass = AccessController.doPrivileged( new PrivilegedAction<Class>(){
            public Class run() {
                return new CallSiteClassLoader(acls, name, bytes).cls;
            }
        });

        if (pogoSiteClass != null) {
            try {
                return pogoSiteClass.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class);
            } catch (NoSuchMethodException e) { //
            }
        }
        return null;
    }

    public static Constructor compilePojoMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final Class acls = declClass.getTheClass();
        final String name = createCallSiteClassName(cachedMethod, declClass, acls);

        final byte[] bytes = genPojoMetaMethodSite(cachedMethod, cw, name);

        final Class pojoSiteClass = AccessController.doPrivileged( new PrivilegedAction<Class>(){
            public Class run() {
                return new CallSiteClassLoader(acls, name, bytes).cls;
            }
        });

        if (pojoSiteClass != null) {
            try {
                return pojoSiteClass.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class);
            } catch (NoSuchMethodException e) { //
            }
        }
        return null;
    }

    public static Constructor compileStaticMethod(CachedMethod cachedMethod) {
        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = cachedMethod.getDeclaringClass();
        final Class acls = declClass.getTheClass();
        final String name = createCallSiteClassName(cachedMethod, declClass, acls);

        final byte[] bytes = genStaticMetaMethodSite(cachedMethod, cw, name);

        final Class staticSiteClass = AccessController.doPrivileged( new PrivilegedAction<Class>(){
            public Class run() {
                return new CallSiteClassLoader(acls, name, bytes).cls;
            }
        });

        if (staticSiteClass != null) {
            try {
                return staticSiteClass.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class);
            } catch (NoSuchMethodException e) { //
            }
        }
        return null;
    }

    private static String createCallSiteClassName(CachedMethod cachedMethod, CachedClass declClass, Class acls) {
        final String name;
        if (declClass.getName().startsWith("java."))
          name = acls.getName().replace('.','_') + "$" + cachedMethod.getName();
        else
          name = declClass.getName() + "$" + cachedMethod.getName();
        return name;
    }

    public static boolean isCompilable (CachedMethod method) {
        return sunVM != null || Modifier.isPublic(method.cachedClass.getModifiers()) && method.isPublic() && publicParams(method);
    }

    private static boolean publicParams(CachedMethod method) {
        for (Class nativeParamType : method.getNativeParameterTypes()) {
            if (!Modifier.isPublic(nativeParamType.getModifiers()))
              return false;
        }
        return true;
    }

    private static class SunClassLoader extends ClassLoader implements Opcodes {
        final static Map<String,Class> knownClasses = new HashMap<String,Class>();

        SunClassLoader (Class magic) throws IOException {
            super (SunClassLoader.class.getClassLoader());

            knownClasses.put("sun.reflect.MagicAccessorImpl", magic);

            loadMagic ();
            loadAbstract ();
            loadFromFile ("org.codehaus.groovy.runtime.callsite.MetaClassSite");
            loadFromFile ("org.codehaus.groovy.runtime.callsite.MetaMethodSite");
            loadFromFile ("org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite");
            loadFromFile ("org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite");
            loadFromFile ("org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite");
        }

        private void loadMagic() {
            ClassWriter cw = new ClassWriter(true);
            cw.visit(Opcodes.V1_4, Opcodes.ACC_PUBLIC, "sun/reflect/GroovyMagic", null, "sun/reflect/MagicAccessorImpl", null);
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, "sun/reflect/MagicAccessorImpl", "<init>", "()V");
            mv.visitInsn(RETURN);
            mv.visitMaxs(0,0);
            mv.visitEnd();
            cw.visitEnd();

            define(cw.toByteArray(), "sun.reflect.GroovyMagic");
        }

        private void loadAbstract() throws IOException {
            final InputStream asStream = getClass().getClassLoader().getResourceAsStream(resName("org.codehaus.groovy.runtime.callsite.AbstractCallSite"));
            ClassReader reader = new ClassReader(asStream);
            final ClassWriter cw = new ClassWriter(true) {
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, "sun/reflect/GroovyMagic", interfaces);
                }
            };
            reader.accept(cw, true);
            asStream.close();
            define(cw.toByteArray(), "org.codehaus.groovy.runtime.callsite.AbstractCallSite");
        }

        private void loadFromFile(String name) throws IOException {
            final InputStream asStream = getClass().getClassLoader().getResourceAsStream(resName(name));
            ClassReader reader = new ClassReader(asStream);
            final ClassWriter cw = new ClassWriter(true) {
            };
            reader.accept(cw, true);
            asStream.close();
            define(cw.toByteArray(), name);
        }

        private String resName(String s) {
            return s.replace('.','/') + ".class";
        }

        private void define(byte[] bytes, final String name) {
            knownClasses.put(name, defineClass(name, bytes, 0, bytes.length));
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            final Class aClass = knownClasses.get(name);
            if (aClass != null)
              return aClass;
            else {
                try {
                    return super.loadClass(name, resolve);
                }
                catch (ClassNotFoundException e) {
                    return getClass().getClassLoader().loadClass(name);
                }
            }
        }
    }

    public static class CallSiteClassLoader extends ClassLoader {
        public final Class cls;

        private final static Set<String> knownClasses = new HashSet<String>();
        static {
            Collections.addAll(knownClasses
                    , "org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite"
                    , "org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite"
                    , "org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite"
                    , "org.codehaus.groovy.runtime.callsite.CallSite"
                    , "org.codehaus.groovy.runtime.callsite.CallSiteArray"
                    , "groovy.lang.MetaMethod"
                    , "groovy.lang.MetaClassImpl"
                    );
        }

        public CallSiteClassLoader(Class parent, String name, byte bytes []) {
            super(parent.getClassLoader());
            cls = defineClass(name, bytes, 0, bytes.length, parent.getProtectionDomain());
            resolveClass(cls);
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (sunVM != null) {
                final Class aClass = SunClassLoader.knownClasses.get(name);
                if (aClass != null)
                  return aClass;
            }

            if (knownClasses.contains(name))
              return getClass().getClassLoader().loadClass(name);
            else {
                try {
                    return super.loadClass(name, resolve);
                }
                catch (ClassNotFoundException e) {
                    return getClass().getClassLoader().loadClass(name);
                }
            }
        }
    }
}
