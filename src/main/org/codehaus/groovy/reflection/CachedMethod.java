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
public class CachedMethod extends ParameterTypes {
    CachedClass clazz;

    public final Method cachedMethod;

    public CachedMethod(CachedClass clazz, Method method) {
        this.cachedMethod = method;
        this.clazz = clazz;
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
}
