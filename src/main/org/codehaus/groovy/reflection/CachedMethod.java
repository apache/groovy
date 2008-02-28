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
import org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite;
import org.codehaus.groovy.runtime.metaclass.MethodHelper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Alex.Tkachman
 */
public class CachedMethod extends MetaMethod implements Comparable, Opcodes{
    public final CachedClass cachedClass;

    private final Method cachedMethod;
    private volatile boolean alreadySetAccessible;
    private int methodIndex;
    private int hashCode;
    private static MyComparator comparator = new MyComparator();

    AtomicInteger invokeCounter = new AtomicInteger();

    public MyClassLoader pogoCallSiteClassLoader;

    public CachedMethod(CachedClass clazz, Method method) {
        this.cachedMethod = method;
        this.cachedClass = clazz;
        alreadySetAccessible = Modifier.isPublic(method.getModifiers()) && Modifier.isPublic(clazz.getModifiers());
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
            return setAccessible().invoke(object, arguments);
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
//        if (invokeCounter != null) {
//            updateInvokeCounter();
//        }
        if ( !alreadySetAccessible ) {
            setAccessible0();
        }
        return cachedMethod;
    }

    private void updateInvokeCounter() {
        try {
            final int count = invokeCounter.incrementAndGet();
            if (count > 20) {
                invokeCounter = null;
                CompileThread.addMethod(this);
            }
        }
        catch (NullPointerException e) {
            //invokeCounter == null already
        }
    }

    private synchronized void setAccessible0() {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                cachedMethod.setAccessible(true);
                return null;
            }
        });
        alreadySetAccessible = true;
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

    public StaticMetaMethodSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Class owner) {
        MyClassLoader loader;
        if (staticMetaMethodLoader == null || (loader = (MyClassLoader) staticMetaMethodLoader.get()) == null ) {
            ClassWriter cw = new ClassWriter(true);

            final String name = getDeclaringClass().getCachedClass().getName().replace('.','_') + "$" + getName();
            byte[] bytes = genStaticMetaMethodSite(cw, name);

            loader = new MyClassLoader(owner.getClassLoader(), name, bytes);
            staticMetaMethodLoader = new WeakReference(loader);
        }

        final Constructor constructor;
        try {
            constructor = loader.cls.getConstructor(new Class[]{CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class});
            return (StaticMetaMethodSite) constructor.newInstance(new Object[]{site, metaClass, metaMethod, params});
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        }
        return null;
    }

    private byte[] genPogoMetaMethodSite(ClassWriter cw, String name) {
        MethodVisitor mv;
        cw.visit(V1_4, ACC_PUBLIC, name, null, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", null);

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
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/codehaus/groovy/runtime/callsite/PogoMetaMethodSite", "checkCallCurrent", "(Ljava/lang/Object;[Ljava/lang/Object;)Z");
        Label l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);

        BytecodeHelper helper = new BytecodeHelper(mv);

        Class callClass = getDeclaringClass().getCachedClass();
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

        {
        mv = cw.visitMethod(ACC_PUBLIC|ACC_FINAL, "wantProvideCallSite", "()Z", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
        }
        cw.visitEnd();

        byte [] bytes = cw.toByteArray();
        return bytes;
    }

    public PogoMetaMethodSite createPogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, MetaMethod metaMethod, Class[] params, Class owner) {
        final Constructor constructor;
        try {
            constructor = pogoCallSiteClassLoader.cls.getConstructor(new Class[]{CallSite.class, MetaClassImpl.class, MetaMethod.class, Class[].class});
            return (PogoMetaMethodSite) constructor.newInstance(new Object[]{site, metaClass, metaMethod, params});
        } catch (NoSuchMethodException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (InstantiationException e) {
        }
        return null;
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
        static LinkedBlockingQueue queue = new LinkedBlockingQueue();

        static {
//            new CompileThread().start();
        }

        private CompileThread() {
            setDaemon(true);
        }

        public void run() {
            try {
                while (true) {
                    final CachedMethod method = (CachedMethod) queue.take();
                    if (method != null) {
                        ClassWriter cw = new ClassWriter(true);

                        final Class acls = method.getDeclaringClass().getCachedClass();
                        final String name = acls.getName().replace('.','_') + "$" + method.getName();
                        byte[] bytes = method.genPogoMetaMethodSite(cw, name);

                        MyClassLoader loader = new MyClassLoader(acls.getClassLoader(), name, bytes);
                        method.pogoCallSiteClassLoader = loader;
                    }
                }
            }
            catch (InterruptedException e) {
                //
            }
        }

        public static void addMethod (CachedMethod method) {
            try {
                queue.put(method);
            } catch (InterruptedException e) {
            }
        }
    }

    private static class MyClassLoader extends ClassLoader {
        final Class cls;
        final String name;

        private MyClassLoader(ClassLoader parent, String name, byte bytes []) {
            super(parent);
            this.name = name;
            cls = defineClass(name, bytes, 0, bytes.length);
            resolveClass(cls);
        }

        protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("org.codehaus.groovy.runtime") || name.startsWith("groovy.lang"))
              return getClass().getClassLoader().loadClass(name);
            return super.loadClass(name, resolve);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    protected void genInvokeMethod(MethodVisitor mv) {
        BytecodeHelper helper = new BytecodeHelper(mv);
        // compute class to make the call on
        Class callClass = getDeclaringClass().getCachedClass();
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
            Class type = parameters[i].getCachedClass();
            if (type.isPrimitive()) {
                helper.unbox(type);
            } else {
                helper.doCast(type);
            }
        }
    }
}

