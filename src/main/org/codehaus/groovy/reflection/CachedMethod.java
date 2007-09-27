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
import org.codehaus.groovy.runtime.Reflector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * @author Alex.Tkachman
 */
public class CachedMethod extends ParameterTypes {
    public final CachedClass cachedClass;

    public final Method cachedMethod;
    private boolean alreadySetAccessible;
    private int methodIndex;

    public CachedMethod(CachedClass clazz, Method method) {
        this.cachedMethod = method;
        this.cachedClass = clazz;
    }

    public CachedMethod(Method method) {
        this(ReflectionCache.getCachedClass(method.getDeclaringClass()),method);
    }

    public static CachedMethod find(Method method) {
        CachedMethod[] methods = ReflectionCache.getCachedClass(method.getDeclaringClass()).getMethods();
        for (int i = 0; i < methods.length; i++) {
            CachedMethod cachedMethod = methods[i];
            if (cachedMethod.cachedMethod.equals(method))
                return cachedMethod;
        }
        return null;
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
        return cachedClass.cachedClass;
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
        return (getModifiers() & Modifier.STATIC) != 0;
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
}

