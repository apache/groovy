/*
 * Copyright 2003-2013 the original author or authors.
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
package org.codehaus.groovy.reflection.stdclasses;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import groovy.lang.Closure;
import groovy.util.ProxyGenerator;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.ConvertedClosure;

public class CachedSAMClass extends CachedClass {

    private final Method method;

    public CachedSAMClass(Class klazz, ClassInfo classInfo) {
        super(klazz, classInfo);
        method = getSAMMethod(klazz);
        if (method==null) throw new GroovyBugError("assigned method should not have been null!");
    }

    @Override
    public boolean isAssignableFrom(Class argument) {
        return argument == null ||
                Closure.class.isAssignableFrom(argument) ||
                ReflectionCache.isAssignableFrom(getTheClass(), argument);
    }

    public static Object coerceToSAM(Closure argument, Method method, Class clazz, boolean isInterface) {
        if (isInterface) {
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure((Closure) argument));
        } else {
            Map<String, Object> m = new HashMap();
            m.put(method.getName(), argument);
            return ProxyGenerator.INSTANCE.
                    instantiateAggregateFromBaseClass(m, clazz);
        }
    }
    
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Closure) {
            Class clazz = getTheClass();
            return coerceToSAM((Closure) argument, method, clazz, clazz.isInterface()); 
        } else {
            return argument;
        }
    }

    /**
     * returns the abstract method from a SAM type, if it is a SAM type.
     * @param c the SAM class
     * @return null if nothing was found, the method otherwise
     */
    public static Method getSAMMethod(Class<?> c) {
        // SAM = single public abstract method
        // if the class is not abstract there is no abstract method
        if (!Modifier.isAbstract(c.getModifiers())) return null;
        Method[] methods = c.getMethods();
        // res stores the first found abstract method
        Method res = null;
        for (Method mi:methods) {
            // ignore methods, that are not abstract
            if (!Modifier.isAbstract(mi.getModifiers())) continue;
            // if we did already find one, then this is no SAM
            if (res!=null) return null;
            res = mi;
        }
        // res!=null here means we found a single public abstract method
        return res;
    }
}
