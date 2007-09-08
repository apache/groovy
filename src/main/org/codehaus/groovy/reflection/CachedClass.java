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

import java.lang.reflect.Method;

/**
 * @author Alex.Tkachman
 */
public class CachedClass {
    private CachedClass cachedSuperClass;

    private CachedMethod[] methods;
    private Class cachedClass;

    public final boolean isArray;
    public final boolean isPrimitive;
    public final int modifiers;
    int distance = -1;

    CachedClass(Class klazz) {
        cachedClass = klazz;
        isArray = klazz.isArray();
        isPrimitive = klazz.isPrimitive();
        modifiers = klazz.getModifiers();
    }

    public synchronized CachedClass getCachedSuperClass() {
        if (cachedSuperClass == null)
            cachedSuperClass = ReflectionCache.getCachedClass(cachedClass.getSuperclass());

        return cachedSuperClass;
    }

    public synchronized CachedMethod[] getMethods() {
        if (methods == null) {
            final Method[] declaredMethods = cachedClass.getDeclaredMethods();
            methods = new CachedMethod[declaredMethods.length];
            for (int i = 0; i != methods.length; ++i)
                methods[i] = new CachedMethod(CachedClass.this, declaredMethods[i]);
        }
        return methods;
    }

    public Method searchMethods(String name, Class[] parameterTypes) {
        CachedMethod[] methods = getMethods();

        Method res = null;
        for (int i = 0; i < methods.length; i++) {
            CachedMethod m = methods[i];
            if (m.method.getName().equals(name)
                    && ReflectionCache.arrayContentsEq(parameterTypes, m.getParameterTypes())
                    && (res == null || res.getReturnType().isAssignableFrom(m.method.getReturnType())))
                res = m.method;
        }

        return res;
    }

    public Class getCachedClass() {
        return cachedClass;
    }

    public int getModifiers() {
        return modifiers;
    }
}
