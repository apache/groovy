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
package org.codehaus.groovy.reflection;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite;
import org.codehaus.groovy.runtime.metaclass.MethodHelper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Alex.Tkachman
 */
public class CachedMethod extends MetaMethod implements Comparable, Opcodes{
    public final CachedClass cachedClass;

    private final Method cachedMethod;
    private int methodIndex;
    private int hashCode;

    private final AtomicBoolean queuedToCompile = new AtomicBoolean();

    private static MyComparator comparator = new MyComparator();

    public SoftReference<Constructor> pogoCallSiteConstructor, pojoCallSiteConstructor, staticCallSiteConstructor;

    public CachedMethod(CachedClass clazz, Method method) {
        this.cachedMethod = method;
        this.cachedClass = clazz;
    }

    public CachedMethod(Method method) {
        this(ReflectionCache.getCachedClass(method.getDeclaringClass()),method);
    }

    public static CachedMethod find(Method method) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(method.getDeclaringClass()).getMethods();
//        for (int i = 0; i < methods.length; i++) {
//            CachedMethod cachedMethod = methods[i];
//            if (cachedMethod.cachedMethod.equals(method))
//                return cachedMethod;
//        }
//        return null;
        int i = Arrays.binarySearch(methods, method, comparator);
        if (i < 0)
          return null;

        return methods[i];
    }

    protected Class[] getPT() {
        return cachedMethod.getParameterTypes();
    }

    public String getName() {
        return cachedMethod.getName();
    }

    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    public CachedClass getDeclaringClass() {
        return cachedClass;
    }

    public final Object invoke(Object object, Object[] arguments) {
        try {
            return cachedMethod.invoke(object, arguments);
        } catch (IllegalArgumentException e) {
            throw new InvokerInvocationException(e);
        } catch (IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            throw new InvokerInvocationException(e);
        }
    }

    public ParameterTypes getParamTypes() {
        return null;
    }

    public Class getReturnType() {
        return cachedMethod.getReturnType();
    }

    public int getParamsCount() {
        return getParameterTypes().length;
    }

    public int getModifiers() {
        return cachedMethod.getModifiers();
    }


    public String getSignature() {
        return getName() + getDescriptor();
    }

    public final Method setAccessible() {
//        if (queuedToCompile.compareAndSet(false,true)) {
//            if (isCompilable())
//              CompileThread.addMethod(this);
//        }
        return cachedMethod;
    }

    public boolean isStatic() {
        return MethodHelper.isStatic(cachedMethod);
    }

    public void setMethodIndex(int i) {
        methodIndex = i;
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public boolean canBeCalledByReflector () {
        if (!Modifier.isPublic(cachedClass.getModifiers()))
            return false;

        if (!Modifier.isPublic(getModifiers()))
          return false;

        getParameterTypes();
        for (int i = 0; i != parameterTypes.length; ++i) {
            if (!parameterTypes[i].isPrimitive && !Modifier.isPublic(parameterTypes[i].getModifiers()))
              return false;
        }
        return true;
    }

    public int compareTo(Object o) {
      if (o instanceof CachedMethod)
        return compareToCachedMethod((CachedMethod)o);
      else
        return compareToMethod((Method)o);
    }

    private int compareToCachedMethod(CachedMethod m) {
        if (m == null)
         return -1;

        final int strComp = getName().compareTo(m.getName());
        if (strComp != 0)
          return strComp;

        final int retComp = getReturnType().getName().compareTo(m.getReturnType().getName());
        if (retComp != 0)
          return retComp;

        CachedClass[]  params =   getParameterTypes();
        CachedClass [] mparams = m.getParameterTypes();

        final int pd = params.length - mparams.length;
        if (pd != 0)
          return pd;

        for (int i = 0; i != params.length; ++i)
        {
            final int nameComp = params[i].getName().compareTo(mparams[i].getName());
            if (nameComp != 0)
              return nameComp;
        }

        throw new RuntimeException("Should never happen");
    }

    private int compareToMethod(Method m) {
        if (m == null)
         return -1;

        final int strComp = getName().compareTo(m.getName());
        if (strComp != 0)
          return strComp;

        final int retComp = getReturnType().getName().compareTo(m.getReturnType().getName());
        if (retComp != 0)
          return retComp;

        CachedClass[]  params =   getParameterTypes();
        Class [] mparams = m.getParameterTypes();

        final int pd = params.length - mparams.length;
        if (pd != 0)
          return pd;

        for (int i = 0; i != params.length; ++i)
        {
            final int nameComp = params[i].getName().compareTo(mparams[i].getName());
            if (nameComp != 0)
              return nameComp;
        }

        return 0;
    }

    public boolean equals(Object o) {
        return (o instanceof CachedMethod && cachedMethod.equals(((CachedMethod)o).cachedMethod))
                || (o instanceof Method && cachedMethod.equals(o));
    }

    public int hashCode() {
        if (hashCode == 0) {
           hashCode = cachedMethod.hashCode();
           if (hashCode == 0)
             hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    public String toString() {
        return cachedMethod.toString();
    }

    private WeakReference staticMetaMethodLoader;

//    public StaticMetaMethodSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Class owner) {
//        MyClassLoader loader;
//        if (staticMetaMethodLoader == null || (loader = (MyClassLoader) staticMetaMethodLoader.get()) == null ) {
//            ClassWriter cw = new ClassWriter(true);
//
//            final String name = getDeclaringClass().getTheClass().getName().replace('.','_') + "$" + getName();
//            byte[] bytes = genStaticMetaMethodSite(cw, name);
//
//            loader = new MyClassLoader(owner.getClassLoader(), name, bytes);
//            staticMetaMethodLoader = new WeakReference(loader);
//        }
//
//        final Constructor constructor;
//        try {
//            constructor = loader.cls.getConstructor(new Class[]{CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class});
//            return (StaticMetaMethodSite) constructor.newInstance(new Object[]{site, metaClass, metaMethod, params});
//        } catch (NoSuchMethodException e) {
//        } catch (IllegalAccessException e) {
//        } catch (InvocationTargetException e) {
//        } catch (InstantiationException e) {
//        }
//        return null;
//    }

    private byte[] genPogoMetaMethodSite(ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(V1_4, ACC_PUBLIC, name.replace('.','/'), null, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", null);

        {
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }

        {
        mv = cw.visitMethod(ACC_PUBLIC, "callCurrent", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "checkCall", "(Ljava/lang/Object;[Ljava/lang/Object;)Z");
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());

        // make call
        if (isStatic()) {
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn(INVOKESTATIC, type, getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            helper.doCast(callClass);
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, getName(), descriptor);
        }

        helper.box(getReturnType());
        if (getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }

        mv.visitInsn(ARETURN);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "array", "Lorg/codehaus/groovy/runtime/callsite/CallSiteArray;");
        mv.visitFieldInsn(GETFIELD, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "owner", "Ljava/lang/Class;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "createCallCurrentSite", "(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;)Lorg/codehaus/groovy/runtime/callsite/CallSite;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/CallSite", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(4, 3);
        mv.visitEnd();
        }

        if (getParamsCount() <= 4)
        {
            StringBuilder pdescb = new StringBuilder();
            final int pc = getParamsCount();
            for (int i = 0; i != pc; ++i)
              pdescb.append("Ljava/lang/Object;");

            String pdesc = pdescb.toString();

        mv = cw.visitMethod(ACC_PUBLIC, "callCurrent", "(Ljava/lang/Object;" + pdesc + ")Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        for (int i = 0; i != pc; ++i)
            mv.visitVarInsn(ALOAD, i+2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "checkCall", "(Ljava/lang/Object;" + pdesc + ")Z");
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());

        // make call
        if (isStatic()) {
            genLoadParametersDirect(2, mv, helper);
            mv.visitMethodInsn(INVOKESTATIC, type, getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            helper.doCast(callClass);
            genLoadParametersDirect(2, mv, helper);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, getName(), descriptor);
        }

        helper.box(getReturnType());
        if (getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }

        mv.visitInsn(ARETURN);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        for (int i = 0; i != pc; ++i)
            mv.visitVarInsn(ALOAD, i+2);
        mv.visitMethodInsn(INVOKESTATIC, "org/codehaus/groovy/runtime/ArrayUtil", "createArray", "(" + pdesc + ")[Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, pc + 2);
        mv.visitVarInsn(ALOAD, pc + 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "array", "Lorg/codehaus/groovy/runtime/callsite/CallSiteArray;");
        mv.visitFieldInsn(GETFIELD, "org/codehaus/groovy/runtime/callsite/CallSiteArray", "owner", "Ljava/lang/Class;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "createCallCurrentSite", "(Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;)Lorg/codehaus/groovy/runtime/callsite/CallSite;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, pc + 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/CallSite", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }


        {
        mv = cw.visitMethod(ACC_PUBLIC, "call", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "checkCall", "(Ljava/lang/Object;[Ljava/lang/Object;)Z");
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();

        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());

        // make call
        if (isStatic()) {
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn(INVOKESTATIC, type, getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            helper.doCast(callClass);
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, getName(), descriptor);
        }

        helper.box(getReturnType());
        if (getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }

        mv.visitInsn(ARETURN);
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "createCallSite", "(Ljava/lang/Object;[Ljava/lang/Object;)Lorg/codehaus/groovy/runtime/callsite/CallSite;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/CallSite", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(4, 3);
        mv.visitEnd();
        }

        cw.visitEnd();

        byte [] bytes = cw.toByteArray();
        return bytes;
    }

    public PogoMetaMethodSite createPogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!hasPogoCallSiteConstructor())
          compileMethod();

        if (pogoCallSiteConstructor != null) {
            final Constructor constructor = pogoCallSiteConstructor.get();
            if (constructor != null) {
                try {
                return (PogoMetaMethodSite) constructor.newInstance(site, metaClass, this, params);
                } catch (IllegalAccessException e) { //
                } catch (InvocationTargetException e) { //
                } catch (InstantiationException e) { //
                }
            }
        }
//        if (queuedToCompile.compareAndSet(false,true)) {
//            CompileThread.addMethod(this);
//        }
        return new PogoMetaMethodSite.PogoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    private byte[] genStaticMetaMethodSite(ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(V1_4, ACC_PUBLIC, name, null, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", null);

        {
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, "org/codehaus/groovy/runtime/callsite/StaticMetaMethodSite", "<init>", "(Lorg/codehaus/groovy/runtime/callsite/CallSite;Lgroovy/lang/MetaClassImpl;Lgroovy/lang/MetaMethod;[Ljava/lang/Class;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }

        {
        mv = cw.visitMethod(ACC_PUBLIC|ACC_FINAL, "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        genInvokeMethod(mv);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }
        cw.visitEnd();

        byte [] bytes = cw.toByteArray();
        return bytes;
    }

    public boolean hasPogoCallSiteConstructor() {
        return pogoCallSiteConstructor != null && pogoCallSiteConstructor.get() != null;
    }

    private void compileMethod() {
        if(!isCompilable())
          return;

        ClassWriter cw = new ClassWriter(true);

        final CachedClass declClass = getDeclaringClass();
        final Class acls = declClass.getTheClass();
        final String name;
        if (declClass.getName().startsWith("java."))
          name = acls.getName().replace('.','_') + "$" + getName();
        else
          name = declClass.getName() + "$" + getName();

        final byte[] bytes = genPogoMetaMethodSite(cw, name);

        final Class pogoSiteClass = AccessController.doPrivileged( new PrivilegedAction<Class>(){
            public Class run() {
                return new MyClassLoader(acls, name, bytes).cls;
            }
        });

        if (pogoSiteClass != null) {
            try {
                final Constructor constructor = pogoSiteClass.getConstructor(CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class);
                pogoCallSiteConstructor = new SoftReference<Constructor>(constructor);
            } catch (NoSuchMethodException e) {
                pogoCallSiteConstructor = null;
            }
        }
    }

    private static class MyComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            if (o1 instanceof CachedMethod)
                return ((CachedMethod)o1).compareTo(o2);
            else if (o2 instanceof CachedMethod)
                return -((CachedMethod)o2).compareTo(o1);
            else
                // really, this should never happen, it's eveidence of corruption if it does
                throw new ClassCastException("One of the two comperables must be a CachedMethod");
        }
    }

    private static class CompileThread extends Thread {
        static final LinkedBlockingQueue queue = new LinkedBlockingQueue();

        static {
//            new CompileThread().start();
        }

        private CompileThread() {
            setDaemon(true);
            setPriority(Thread.MAX_PRIORITY-2);
        }

        public void run() {
            try {
                while (true) {
                    final CachedMethod method = (CachedMethod) queue.take();
                    if (method != null) {
                        method.compileMethod();
                    }
                }
            }
            catch (InterruptedException e) {//
            }
        }

        public static void addMethod (CachedMethod method) {
            try {
                queue.put(method);
            } catch (InterruptedException e) {
            }
        }
    }

    protected void genInvokeMethod(MethodVisitor mv) {
        BytecodeHelper helper = new BytecodeHelper(mv);
        // compute class to make the call on
        Class callClass = getDeclaringClass().getTheClass();
        boolean useInterface = callClass.isInterface();
//        if (callClass == null) {
//            callClass = method.getCallClass();
//        } else {
//            useInterface = true;
//        }
        // get bytecode information
        String type = BytecodeHelper.getClassInternalName(callClass.getName());
        String descriptor = BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());

        // make call
        if (isStatic()) {
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn(INVOKESTATIC, type, getName(), descriptor);
        } else {
            mv.visitVarInsn(ALOAD, 1);
            helper.doCast(callClass);
            genLoadParameters(2, mv, helper);
            mv.visitMethodInsn((useInterface) ? INVOKEINTERFACE : INVOKEVIRTUAL, type, getName(), descriptor);
        }

        helper.box(getReturnType());
        if (getReturnType() == void.class) {
            mv.visitInsn(ACONST_NULL);
        }
    }

    protected void genLoadParameters(int argumentIndex, MethodVisitor mv, BytecodeHelper helper) {
        CachedClass[] parameters = getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex);
            helper.pushConstant(i);
            mv.visitInsn(AALOAD);

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

    protected void genLoadParametersDirect(int argumentIndex, MethodVisitor mv, BytecodeHelper helper) {
        CachedClass[] parameters = getParameterTypes();
        int size = parameters.length;
        for (int i = 0; i < size; i++) {
            // unpack argument from Object[]
            mv.visitVarInsn(ALOAD, argumentIndex+i);

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

    private boolean isCompilable () {
        return Modifier.isPublic(cachedClass.getModifiers()) && isPublic() && publicParams();
    }

    private boolean publicParams() {
        for (Class nativeParamType : nativeParamTypes) {
            if (!Modifier.isPublic(nativeParamType.getModifiers()))
              return false;
        }
        return true;
    }

    private static class MyClassLoader extends ClassLoader {
        final Class cls;

        private MyClassLoader(Class parent, String name, byte bytes []) {
            super(parent.getClassLoader());
            cls = defineClass(name, bytes, 0, bytes.length, parent.getProtectionDomain());
            resolveClass(cls);
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.codehaus.groovy.runtime") || name.startsWith("groovy.lang"))
              return getClass().getClassLoader().loadClass(name);
            return super.loadClass(name, resolve);
        }
    }
}

