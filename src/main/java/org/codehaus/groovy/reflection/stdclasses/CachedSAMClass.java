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
package org.codehaus.groovy.reflection.stdclasses;

import groovy.lang.Closure;
import groovy.util.ProxyGenerator;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;
import org.codehaus.groovy.reflection.ReflectionCache;
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.transform.trait.Traits;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class CachedSAMClass extends CachedClass {

    private static final int ABSTRACT_STATIC_PRIVATE =
            Modifier.ABSTRACT|Modifier.PRIVATE|Modifier.STATIC;
    private static final int VISIBILITY = 5; // public|protected
    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
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

    public static Object coerceToSAM(Closure argument, Method method, Class clazz) {
        return coerceToSAM(argument, method, clazz, clazz.isInterface());
    }

    /* Should we make the following method private? */
    @SuppressWarnings("unchecked")
    public static Object coerceToSAM(Closure argument, Method method, Class clazz, boolean isInterface) {
        if (argument!=null && clazz.isAssignableFrom(argument.getClass())) {
            return argument;
        }
        if (isInterface) {
            if (Traits.isTrait(clazz)) {
                Map<String,Closure> impl = Collections.singletonMap(
                        method.getName(),
                        argument
                );
                return ProxyGenerator.INSTANCE.instantiateAggregate(impl,Collections.singletonList(clazz));
            }
            return Proxy.newProxyInstance(
                    clazz.getClassLoader(),
                    new Class[]{clazz},
                    new ConvertedClosure(argument));
        } else {
            Map<String, Object> m = new HashMap<String,Object>();
            m.put(method.getName(), argument);
            return ProxyGenerator.INSTANCE.
                    instantiateAggregateFromBaseClass(m, clazz);
        }
    }
    
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Closure) {
            Class clazz = getTheClass();
            return coerceToSAM((Closure) argument, method, clazz);
        } else {
            return argument;
        }
    }

    private static Method[] getDeclaredMethods(final Class c) {
        try {
            Method[] methods = AccessController.doPrivileged((PrivilegedAction<Method[]>) () -> c.getDeclaredMethods());
            if (methods!=null) return methods;
        } catch (java.security.AccessControlException ace) {
            // swallow and do as if no method is available
        }
        return EMPTY_METHOD_ARRAY;
    }

    private static void getAbstractMethods(Class c, List<Method> current) {
        if (c==null || !Modifier.isAbstract(c.getModifiers())) return;
        getAbstractMethods(c.getSuperclass(), current);
        for (Class ci : c.getInterfaces()) {
            getAbstractMethods(ci, current);
        }
        for (Method m : getDeclaredMethods(c)) {
            if (Modifier.isPrivate(m.getModifiers())) continue;
            if (Modifier.isAbstract(m.getModifiers())) current.add(m);
        }
    }

    private static boolean hasUsableImplementation(Class c, Method m) {
        if (c==m.getDeclaringClass()) return false;
        Method found;
        try {
            found = c.getMethod(m.getName(), m.getParameterTypes());
            int asp = found.getModifiers() & ABSTRACT_STATIC_PRIVATE;
            int visible = found.getModifiers() & VISIBILITY;
            if (visible !=0 && asp == 0) return true;
        } catch (NoSuchMethodException e) {/*ignore*/}
        if (c==Object.class) return false;
        return hasUsableImplementation(c.getSuperclass(), m);
    }

    private static Method getSingleNonDuplicateMethod(List<Method> current) {
        if (current.isEmpty()) return null;
        if (current.size()==1) return current.get(0);
        Method m = current.remove(0);
        for (Method m2 : current) {
            if (m.getName().equals(m2.getName()) && 
                Arrays.equals(m.getParameterTypes(), m2.getParameterTypes()))
            {
                continue;
            }
            return null;
        }
        return m;
    }

    /**
     * returns the abstract method from a SAM type, if it is a SAM type.
     * @param c the SAM class
     * @return null if nothing was found, the method otherwise
     */
    public static Method getSAMMethod(Class<?> c) {
      try {
        return getSAMMethodImpl(c);
      } catch (NoClassDefFoundError ignore) {
        return null;
      }
    }

    private static Method getSAMMethodImpl(Class<?> c) {
        // SAM = single public abstract method
        // if the class is not abstract there is no abstract method
        if (!Modifier.isAbstract(c.getModifiers())) return null;
        if (c.isInterface()) {
            Method[] methods = c.getMethods();
            // res stores the first found abstract method
            Method res = null;
            for (Method mi : methods) {
                // ignore methods, that are not abstract and from Object
                if (!Modifier.isAbstract(mi.getModifiers())) continue;
                // ignore trait methods which have a default implementation
                if (mi.getAnnotation(Traits.Implemented.class)!=null) continue;
                try {
                    Object.class.getMethod(mi.getName(), mi.getParameterTypes());
                    continue;
                } catch (NoSuchMethodException e) {/*ignore*/}

                // we have two methods, so no SAM
                if (res!=null) return null;
                res = mi;
            }
            return res;

        } else {

            LinkedList<Method> methods = new LinkedList();
            getAbstractMethods(c, methods);
            if (methods.isEmpty()) return null;
            methods.removeIf(m -> hasUsableImplementation(c, m));
            return getSingleNonDuplicateMethod(methods);
        }
    }
}
