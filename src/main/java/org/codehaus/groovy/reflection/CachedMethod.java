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

import java.lang.annotation.Annotation;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@SuppressWarnings("rawtypes")
public class CachedMethod extends MetaMethod implements Comparable {

    public static final CachedMethod[] EMPTY_ARRAY = new CachedMethod[0];

    public static CachedMethod find(final Method method) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(method.getDeclaringClass()).getMethods();
        int i = Arrays.binarySearch(methods, method, (o1, o2) -> {
            if (o1 instanceof CachedMethod) {
                return ((CachedMethod) o1).compareTo(o2);
            } else if (o2 instanceof CachedMethod) {
                return -((CachedMethod) o2).compareTo(o1);
            }
            // really, this should never happen, it's evidence of corruption if it does
            throw new ClassCastException("One of the two comparables must be a CachedMethod");
        });
        return (i < 0 ? null : methods[i]);
    }

    //--------------------------------------------------------------------------

    public  final CachedClass cachedClass;
    private final Method cachedMethod;

    private int hashCode;
    private boolean skipCompiled;
    private boolean accessAllowed;
    private boolean makeAccessibleDone;
    private CachedMethod transformedMethod;
    private SoftReference<Constructor<CallSite>> pogoCallSiteConstructor, pojoCallSiteConstructor, staticCallSiteConstructor;

    public CachedMethod(final CachedClass clazz, final Method method) {
        this.cachedMethod = method;
        this.cachedClass = clazz;
    }

    public CachedMethod(final Method method) {
        this(ReflectionCache.getCachedClass(method.getDeclaringClass()), method);
    }

    @Override
    public int compareTo(final Object other) {
        if (other == this) return 0;
        if (other == null) return -1;
        return (other instanceof CachedMethod ? compareToCachedMethod((CachedMethod) other) : compareToMethod((Method) other));
    }

    private int compareToCachedMethod(final CachedMethod other) {
        int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        CachedClass[] otherParams = other.getParameterTypes();

        int pd = params.length - otherParams.length;
        if (pd != 0)
            return pd;

        for (int i = 0, n = params.length; i < n; i += 1) {
            final int nameComp = params[i].getName().compareTo(otherParams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        final int classComp = cachedClass.toString().compareTo(other.getDeclaringClass().toString());
        if (classComp != 0)
            return classComp;

        throw new RuntimeException("Should never happen");
    }

    private int compareToMethod(final Method other) {
        int strComp = getName().compareTo(other.getName());
        if (strComp != 0)
            return strComp;

        int retComp = getReturnType().getName().compareTo(other.getReturnType().getName());
        if (retComp != 0)
            return retComp;

        CachedClass[] params = getParameterTypes();
        Class<?>[] mparams = other.getParameterTypes();

        int pd = params.length - mparams.length;
        if (pd != 0)
            return pd;

        for (int i = 0, n = params.length; i < n; i += 1) {
            final int nameComp = params[i].getName().compareTo(mparams[i].getName());
            if (nameComp != 0)
                return nameComp;
        }

        return 0;
    }

    @Override
    public boolean equals(final Object other) {
        return (other instanceof CachedMethod && cachedMethod.equals(((CachedMethod) other).cachedMethod))
            || (other instanceof Method && cachedMethod.equals(other));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
           hashCode = cachedMethod.hashCode();
           if (hashCode == 0) hashCode = 0xcafebebe;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return cachedMethod.toString();
    }

    //--------------------------------------------------------------------------

    public boolean canAccessLegally(final Class<?> callerClass) {
        return ReflectionUtils.checkAccessible(callerClass, cachedMethod.getDeclaringClass(), cachedMethod.getModifiers(), false);
    }

    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return cachedMethod.getAnnotation(annotationClass);
    }

    public Method getCachedMethod() {
        makeAccessibleIfNecessary();
        if (!accessAllowed) {
            AccessPermissionChecker.checkAccessPermission(cachedMethod);
            accessAllowed = true;
        }
        return cachedMethod;
    }

    @Override
    public CachedClass getDeclaringClass() {
        return cachedClass;
    }

    @Override
    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    @Override
    public int getModifiers() {
        return cachedMethod.getModifiers();
    }

    @Override
    public String getName() {
        return cachedMethod.getName();
    }

    public int getParamsCount() {
        return getParameterTypes().length;
    }

    public ParameterTypes getParamTypes() {
        return null;
    }

    @Override
    public Class[] getPT() {
        return cachedMethod.getParameterTypes();
    }

    @Override
    public Class getReturnType() {
        return cachedMethod.getReturnType();
    }

    @Override
    public String getSignature() {
        return getName() + getDescriptor();
    }

    public CachedMethod getTransformedMethod() {
        return transformedMethod;
    }

    public void setTransformedMethod(final CachedMethod transformedMethod) {
        this.transformedMethod = transformedMethod;
    }

    @Override
    public boolean isStatic() {
        return MethodHelper.isStatic(cachedMethod);
    }

    public boolean isSynthetic() {
        return cachedMethod.isSynthetic();
    }

    //--------------------------------------------------------------------------

    public CallSite createPogoMetaMethodSite(final CallSite site, final MetaClassImpl metaClass, final Class[] params) {
        if (!skipCompiled) {
            Constructor<CallSite> ctor = deref(pogoCallSiteConstructor);
            if (ctor == null) {
                if (CallSiteGenerator.isCompilable(this)) {
                    ctor = CallSiteGenerator.compilePogoMethod(this);
                }
                if (ctor != null) {
                    pogoCallSiteConstructor = new SoftReference<>(ctor);
                } else {
                    skipCompiled = true;
                }
            }

            if (ctor != null) {
                try {
                    return ctor.newInstance(site, metaClass, this, params, ctor);
                } catch (Error e) {
                    skipCompiled = true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled = true;
                }
            }
        }
        return new PogoMetaMethodSite.PogoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public CallSite createPojoMetaMethodSite(final CallSite site, final MetaClassImpl metaClass, final Class[] params) {
        if (!skipCompiled) {
            Constructor<CallSite> ctor = deref(pojoCallSiteConstructor);
            if (ctor == null) {
                if (CallSiteGenerator.isCompilable(this)) {
                    ctor = CallSiteGenerator.compilePojoMethod(this);
                }
                if (ctor != null) {
                    pojoCallSiteConstructor = new SoftReference<>(ctor);
                } else {
                    skipCompiled = true;
                }
            }

            if (ctor != null) {
                try {
                    return ctor.newInstance(site, metaClass, this, params, ctor);
                } catch (Error e) {
                    skipCompiled = true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled = true;
                }
            }
        }
        return new PojoMetaMethodSite.PojoCachedMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    public CallSite createStaticMetaMethodSite(final CallSite site, final MetaClassImpl metaClass, final Class[] params) {
        if (!skipCompiled) {
            Constructor<CallSite> ctor = deref(staticCallSiteConstructor);
            if (ctor == null) {
                if (CallSiteGenerator.isCompilable(this)) {
                    ctor = CallSiteGenerator.compileStaticMethod(this);
                }
                if (ctor != null) {
                    staticCallSiteConstructor = new SoftReference<>(ctor);
                } else {
                    skipCompiled = true;
                }
            }

            if (ctor != null) {
                try {
                    return ctor.newInstance(site, metaClass, this, params, ctor);
                } catch (Error e) {
                    skipCompiled = true;
                    throw e;
                } catch (Throwable e) {
                    skipCompiled = true;
                }
            }
        }
        return new StaticMetaMethodSite.StaticMetaMethodSiteNoUnwrapNoCoerce(site, metaClass, this, params);
    }

    private static <T> Constructor<T> deref(final SoftReference<Constructor<T>> ref) {
        return (ref != null ? ref.get() : null);
    }

    @Override
    public final Object invoke(final Object object, final Object[] arguments) {
        makeAccessibleIfNecessary();
        if (!accessAllowed) {
            try {
                AccessPermissionChecker.checkAccessPermission(cachedMethod);
                accessAllowed = true;
            } catch (CacheAccessControlException ex) {
                throw new InvokerInvocationException(ex);
            }
        }

        try {
            return cachedMethod.invoke(object, arguments);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new InvokerInvocationException(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw (cause instanceof RuntimeException && !(cause instanceof MissingMethodException)) ? (RuntimeException) cause : new InvokerInvocationException(e);
        }
    }

    private void makeAccessibleIfNecessary() {
        if (!makeAccessibleDone) {
            ReflectionUtils.makeAccessibleInPrivilegedAction(cachedMethod);
            makeAccessibleDone = true;
        }
    }

    public final Method setAccessible() {
        return getCachedMethod();
    }
}
