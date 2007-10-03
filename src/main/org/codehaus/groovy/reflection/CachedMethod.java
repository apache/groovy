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

import org.codehaus.groovy.classgen.BytecodeHelper;
import org.codehaus.groovy.runtime.metaclass.MethodHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;

/**
 * @author Alex.Tkachman
 */
public class CachedMethod extends ParameterTypes implements Comparable{
    public final CachedClass cachedClass;

    public final Method cachedMethod;
    private boolean alreadySetAccessible;
    private int methodIndex;
    private int hashCode;

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
        int i = Arrays.binarySearch(methods, method);
        if (i < 0)
          return null;

        return methods[i];
    }

    Class[] getPT() {
        return cachedMethod.getParameterTypes();
    }

    public String getName() {
        return cachedMethod.getName();
    }

    public String getDescriptor() {
        return BytecodeHelper.getMethodDescriptor(getReturnType(), getNativeParameterTypes());
    }

    public Class getDeclaringClass() {
        return cachedClass.getCachedClass();
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

    public Object invokeByReflection(Object object, Object[] arguments) throws IllegalAccessException, InvocationTargetException {
        if ( !alreadySetAccessible ) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    cachedMethod.setAccessible(true);
                    return null;
                }
            });
            alreadySetAccessible = true;
        }

        return cachedMethod.invoke(object, arguments);
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

    public Object invoke(Object object, Object[] arguments) throws IllegalAccessException, InvocationTargetException {
//        final Reflector reflector = cachedClass.getReflector();
//        if (methodIndex != 0)
//          return reflector.invoke(this, object, arguments);
//        else
          return invokeByReflection(object, arguments);
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

}

