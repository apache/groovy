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
import org.codehaus.groovy.runtime.callsite.*;
import org.codehaus.groovy.runtime.metaclass.MethodHelper;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Alex.Tkachman
 */
public class CachedMethod extends MetaMethod implements Comparable {
    public final CachedClass cachedClass;

    private final Method cachedMethod;
    private int hashCode;

    private static MyComparator comparator = new MyComparator();

    private SoftReference<Constructor> pogoCallSiteConstructor, pojoCallSiteConstructor, staticCallSiteConstructor;

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

    public CallSite createPogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!hasPogoCallSiteConstructor()) {
          Constructor constr = null;
          if (CallSiteGenerator.isCompilable(this)) {
              constr = CallSiteGenerator.compilePogoMethod(this);

              if (constr != null)
                 pogoCallSiteConstructor = new SoftReference<Constructor> (constr);
          }
        }

        if (hasPogoCallSiteConstructor()) {
            final Constructor constructor = pogoCallSiteConstructor.get();
            if (constructor != null) {
                try {
                return (CallSite) constructor.newInstance(site, metaClass, this, params);
                } catch (Throwable e) { //
                }
            }
        }

        return new PogoMetaMethodSite.PogoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public CallSite createPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!hasPojoCallSiteConstructor()) {
          Constructor constr = null;
          if (CallSiteGenerator.isCompilable(this)) {
              constr = CallSiteGenerator.compilePojoMethod(this);

              if (constr != null)
                 pojoCallSiteConstructor = new SoftReference<Constructor> (constr);
          }
        }

        if (hasPogoCallSiteConstructor()) {
            final Constructor constructor = pojoCallSiteConstructor.get();
            if (constructor != null) {
                try {
                return (CallSite) constructor.newInstance(site, metaClass, this, params);
                } catch (Throwable e) { //
                }
            }
        }

        return new PojoMetaMethodSite.PojoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public CallSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!hasStaticCallSiteConstructor()) {
          Constructor constr = null;
          if (CallSiteGenerator.isCompilable(this)) {
              constr = CallSiteGenerator.compileStaticMethod(this);

              if (constr != null)
                 staticCallSiteConstructor = new SoftReference<Constructor> (constr);
          }
        }

        if (hasStaticCallSiteConstructor()) {
            final Constructor constructor = staticCallSiteConstructor.get();
            if (constructor != null) {
                try {
                return (CallSite) constructor.newInstance(site, metaClass, this, params);
                } catch (Throwable e) { //
                }
            }
        }

        return new StaticMetaMethodSite.StaticMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public boolean hasPogoCallSiteConstructor() {
        return pogoCallSiteConstructor != null && pogoCallSiteConstructor.get() != null;
    }

    public boolean hasPojoCallSiteConstructor() {
        return pojoCallSiteConstructor != null && pojoCallSiteConstructor.get() != null;
    }

    public boolean hasStaticCallSiteConstructor() {
        return staticCallSiteConstructor != null && staticCallSiteConstructor.get() != null;
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

    public Method getCachedMethod() {
        return cachedMethod;
    }

//    private static class CompileThread extends Thread {
//        static final LinkedBlockingQueue queue = new LinkedBlockingQueue();
//
//        static {
//            new CompileThread().start();
//        }
//
//        private CompileThread() {
//            setDaemon(true);
//            setPriority(Thread.MAX_PRIORITY-2);
//        }
//
//        public void run() {
//            try {
//                while (true) {
//                    final CachedMethod method = (CachedMethod) queue.take();
//                    if (method != null) {
//                        CallSiteGenerator.compilePogoMethod(method);
//                    }
//                }
//            }
//            catch (InterruptedException e) {//
//            }
//        }
//
//        public static void addMethod (CachedMethod method) {
//            try {
//                queue.put(method);
//            } catch (InterruptedException e) {
//            }
//        }
//    }

}

