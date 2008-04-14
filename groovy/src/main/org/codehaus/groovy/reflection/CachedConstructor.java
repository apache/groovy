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

import java.lang.reflect.Constructor;

/**
 * @author Alex.Tkachman
 */
public class CachedConstructor extends ParameterTypes {
    CachedClass clazz;

    public final Constructor cachedConstructor;

    public CachedConstructor(CachedClass clazz, Constructor c) {
        this.cachedConstructor = c;
        this.clazz = clazz;
        try {
            c.setAccessible(true);
        }
        catch (SecurityException e) {
            // IGNORE
        }
    }

    public CachedConstructor(Constructor c) {
        this(ReflectionCache.getCachedClass(c.getDeclaringClass()), c);
    }

    protected Class[] getPT() {
        return cachedConstructor.getParameterTypes();
    }

    public static CachedConstructor find(Constructor constructor) {
        CachedConstructor[] constructors = ReflectionCache.getCachedClass(constructor.getDeclaringClass()).getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            CachedConstructor cachedConstructor = constructors[i];
            if (cachedConstructor.cachedConstructor.equals(constructor))
                return cachedConstructor;
        }
        throw new RuntimeException("Couldn't find method: " + constructor);
    }

}
