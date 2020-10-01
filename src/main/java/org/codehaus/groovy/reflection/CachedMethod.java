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
package org.codehaus.groovy.reflection;

import groovy.lang.MetaClassImpl;
import groovy.lang.MetaMethod;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteGenerator;
import org.codehaus.groovy.runtime.callsite.PogoMetaMethodSite;
import org.codehaus.groovy.runtime.callsite.PojoMetaMethodSite;
import org.codehaus.groovy.runtime.callsite.StaticMetaMethodSite;
import org.codehaus.groovy.runtime.metaclass.MethodHelper;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

import static org.codehaus.groovy.reflection.ReflectionUtils.makeAccessibleInPrivilegedAction;

public class CachedMethod extends MetaMethod implements Comparable {
    public static final CachedMethod[] EMPTY_ARRAY = new CachedMethod[0];
    public final CachedClass cachedClass;

    private final Method cachedMethod;
    private int hashCode;
    private CachedMethod transformedMethod;

    private static final MyComparator COMPARATOR = new MyComparator();

    private SoftReference<Constructor> pogoCallSiteConstructor, pojoCallSiteConstructor, staticCallSiteConstructor;

    private boolean skipCompiled;

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
        int i = Arrays.binarySearch(methods, method, COMPARATOR);
        if (i < 0)
          return null;

        return methods[i];
    }

    @Override
    public Class[] getPT() {
        return cachedMethod.getParameterTypes();
    }

    @Override
    public String getName() {
        return cachedMethod.getName();
    }

    @Override
    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    @Override
    public CachedClass getDeclaringClass() {
        return cachedClass;
    }

    @Override
    public final Object invoke(Object object, Object[] arguments) {
        makeAccessibleIfNecessary();

        try {
            AccessPermissionChecker.checkAccessPermission(cachedMethod);
        } catch (CacheAccessControlException ex) {
            throw new InvokerInvocationException(ex);
        }
        try {
            return cachedMethod.invoke(object, arguments);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause(); 
            throw (cause instanceof RuntimeException && !(cause instanceof MissingMethodException)) ? 
                    (RuntimeException) cause : new InvokerInvocationException(e);
        }
    }

    public ParameterTypes getParamTypes() {
        return null;
    }

    @Override
    public Class getReturnType() {
        return cachedMethod.getReturnType();
    }

    public int getParamsCount() {
        return getParameterTypes().length;
    }

    @Override
    public int getModifiers() {
        return cachedMethod.getModifiers();
    }


    @Override
    public String getSignature() {
        return getName() + getDescriptor();
    }

    public final Method setAccessible() {
        makeAccessibleIfNecessary();

        AccessPermissionChecker.checkAccessPermission(cachedMethod);
//        if (queuedToCompile.compareAndSet(false,true)) {
//            if (isCompilable())
//              CompileThread.addMethod(this);
//        }
        return cachedMethod;
    }

    @Override
    public boolean isStatic() {
        return MethodHelper.isStatic(cachedMethod);
    }

    public CachedMethod getTransformedMethod() {
        return transformedMethod;
    }

    public void setTransformedMethod(CachedMethod transformedMethod) {
        this.transformedMethod = transformedMethod;
    }

    @Override
    public int compareTo(Object o) {
      if (o instanceof CachedMethod)
        return compareToCachedMethod((CachedMethod)o);
      else
        return compareToMethod((Method)o);
    }

    private int compareToCachedMethod(CachedMethod other) {
        if (other == null)
            return -1;

        final int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        final int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        CachedClass[] otherParams = other.getParameterTypes();

        final int pd = params.length - otherParams.length;
        if (pd != 0)
            return pd;

        for (int i = 0; i != params.length; ++i) {
            final int nameComp = params[i].getName().compareTo(otherParams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        final int classComp = cachedClass.toString().compareTo(other.getDeclaringClass().toString());
        if (classComp != 0)
            return classComp;

        throw new RuntimeException("Should never happen");
    }

    private int compareToMethod(Method other) {
        if (other == null)
            return -1;

        final int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        final int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        Class[] mparams = other.getParameterTypes();

        final int pd = params.length - mparams.length;
        if (pd != 0)
            return pd;

        for (int i = 0; i != params.length; ++i) {
            final int nameComp = params[i].getName().compareTo(mparams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CachedMethod && cachedMethod.equals(((CachedMethod)o).cachedMethod))
                || (o instanceof Method && cachedMethod.equals(o));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
           hashCode = cachedMethod.hashCode();
           if (hashCode == 0)
             hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return cachedMethod.toString();
    }
    
    private static Constructor getConstructor(SoftReference<Constructor> ref) {
        if (ref==null) return null;
        return ref.get();
    }

    public CallSite createPogoMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!skipCompiled) {
            Constructor constr = getConstructor(pogoCallSiteConstructor);
            if (constr==null) {
                if (CallSiteGenerator.isCompilable(this)) {
                  constr = CallSiteGenerator.compilePogoMethod(this);
                }
                if (constr != null) {
                     pogoCallSiteConstructor = new SoftReference<Constructor> (constr);
                } else {
                    skipCompiled = true;
                }
            }
    
            if (constr!=null) {
                try {
                    return (CallSite) constr.newInstance(site, metaClass, this, params, constr);
                } catch (Error e) {
                    skipCompiled=true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled=true;
                }
            }
        }
        return new PogoMetaMethodSite.PogoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }


    public CallSite createPojoMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!skipCompiled) {
            Constructor constr = getConstructor(pojoCallSiteConstructor);
            if (constr==null) {
                if (CallSiteGenerator.isCompilable(this)) {
                  constr = CallSiteGenerator.compilePojoMethod(this);
                }
                if (constr != null) {
                    pojoCallSiteConstructor = new SoftReference<Constructor> (constr);
                } else {
                    skipCompiled = true;
                }
            }
    
            if (constr!=null) {
                try {
                    return (CallSite) constr.newInstance(site, metaClass, this, params, constr);
                } catch (Error e) {
                    skipCompiled=true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled=true;
                }
            }
        }
        return new PojoMetaMethodSite.PojoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public CallSite createStaticMetaMethodSite(CallSite site, MetaClassImpl metaClass, Class[] params) {
        if (!skipCompiled) {
            Constructor constr = getConstructor(staticCallSiteConstructor);
            if (constr==null) {
                if (CallSiteGenerator.isCompilable(this)) {
                  constr = CallSiteGenerator.compileStaticMethod(this);
                }
                if (constr != null) {
                    staticCallSiteConstructor = new SoftReference<Constructor> (constr);
                } else {
                    skipCompiled = true;
                }
            }
    
            if (constr!=null) {
                try {
                    return (CallSite) constr.newInstance(site, metaClass, this, params, constr);
                } catch (Error e) {
                    skipCompiled=true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled=true;
                }
            }
        }

        return new StaticMetaMethodSite.StaticMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    private static class MyComparator implements Comparator, Serializable {
        private static final long serialVersionUID = 8909277090690131302L;

        @Override
        public int compare(Object o1, Object o2) {
            if (o1 instanceof CachedMethod)
                return ((CachedMethod)o1).compareTo(o2);
            else if (o2 instanceof CachedMethod)
                return -((CachedMethod)o2).compareTo(o1);
            else
                // really, this should never happen, it's evidence of corruption if it does
                throw new ClassCastException("One of the two comparables must be a CachedMethod");
        }
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return cachedMethod.getAnnotation(annotationClass);
    }

    public boolean isSynthetic() {
        return cachedMethod.isSynthetic();
    }

    public Method getCachedMethod() {
        makeAccessibleIfNecessary();
        AccessPermissionChecker.checkAccessPermission(cachedMethod);
        return cachedMethod;
    }

    public boolean canAccessLegally(Class<?> callerClass) {
        return ReflectionUtils.checkAccessible(callerClass, cachedMethod.getDeclaringClass(), cachedMethod.getModifiers(), false);
    }

    private boolean makeAccessibleDone = false;
    private void makeAccessibleIfNecessary() {
        if (!makeAccessibleDone) {
            makeAccessibleInPrivilegedAction(cachedMethod);
            makeAccessibleDone = true;
        }
    }
}
