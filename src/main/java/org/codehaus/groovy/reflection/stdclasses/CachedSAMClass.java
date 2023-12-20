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
import org.codehaus.groovy.runtime.ConvertedClosure;
import org.codehaus.groovy.transform.trait.Traits;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.PrivilegedAction;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class CachedSAMClass extends CachedClass {

    private final Method method;

    public CachedSAMClass(Class clazz, ClassInfo classInfo) {
        super(clazz, classInfo);
        method = getSAMMethod(clazz);
        if (method == null) throw new GroovyBugError("assigned method should not have been null!");
    }

    @Override
    public boolean isAssignableFrom(Class argument) {
        return argument == null
            || Closure.class.isAssignableFrom(argument)
            || getTheClass().isAssignableFrom(argument);
    }

    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Closure) {
            Class<?> clazz = getTheClass();
            return coerceToSAM((Closure<?>) argument, method, clazz);
        } else {
            return argument;
        }
    }

    //--------------------------------------------------------------------------

    private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];
    private static final int PUBLIC_OR_PROTECTED = Modifier.PUBLIC | Modifier.PROTECTED;
    private static final int ABSTRACT_STATIC_PRIVATE = Modifier.ABSTRACT | Modifier.STATIC | Modifier.PRIVATE;
    private static final Set<String> OBJECT_METHOD_NAMES = Arrays.stream(Object.class.getMethods()).map(Method::getName).collect(Collectors.toUnmodifiableSet());

    public static Object coerceToSAM(Closure argument, Method method, Class clazz) {
        return coerceToSAM(argument, method, clazz, clazz.isInterface());
    }

    public static Object coerceToSAM(Closure argument, Method method, Class clazz, boolean isInterface) {
        if (argument != null && clazz.isAssignableFrom(argument.getClass())) {
            return argument;
        }

        if (!isInterface) {
            return ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(Collections.singletonMap(method.getName(), argument), clazz);
        } else if (method != null && isOrImplementsTrait(clazz)) { // GROOVY-8243
            return ProxyGenerator.INSTANCE.instantiateAggregate(Collections.singletonMap(method.getName(), argument), Collections.singletonList(clazz));
        } else {
            return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ConvertedClosure(argument));
        }
    }

    private static boolean isOrImplementsTrait(Class<?> c) {
        if (Traits.isTrait(c)) return true; // quick check

        Queue<Class<?>> todo = new ArrayDeque<>(Arrays.asList(c.getInterfaces()));
        Set<Class<?>> done = new HashSet<>();
        while ((c = todo.poll()) != null) {
            if (done.add(c)) {
                if (Traits.isTrait(c)) return true;
                Collections.addAll(todo, c.getInterfaces());
            }
        }
        return false;
    }

    @SuppressWarnings("removal") // TODO: a future Groovy version should remove the security check
    private static Method[] getDeclaredMethods(final Class<?> c) {
        try {
            Method[] methods = java.security.AccessController.doPrivileged((PrivilegedAction<Method[]>) c::getDeclaredMethods);
            if (methods != null) return methods;
        } catch (java.security.AccessControlException ace) {
            // swallow and do as if no method is available
        }
        return EMPTY_METHOD_ARRAY;
    }

    private static void getAbstractMethods(Class<?> c, List<Method> current) {
        if (c == null || !Modifier.isAbstract(c.getModifiers())) return;
        getAbstractMethods(c.getSuperclass(), current);
        for (Class<?> ci : c.getInterfaces()) {
            getAbstractMethods(ci, current);
        }
        for (Method m : getDeclaredMethods(c)) {
            final int modifiers = m.getModifiers();
            if (Modifier.isPrivate(modifiers)) continue;
            if (Modifier.isAbstract(modifiers)) current.add(m);
        }
    }

    private static boolean hasUsableImplementation(Class<?> c, Method m) {
        if (c == m.getDeclaringClass()) return false;
        Method found;
        try {
            found = c.getMethod(m.getName(), m.getParameterTypes());
            final int modifiers = found.getModifiers();
            int asp = modifiers & ABSTRACT_STATIC_PRIVATE;
            int visible = modifiers & PUBLIC_OR_PROTECTED;
            if (visible != 0 && asp == 0) return true;
        } catch (NoSuchMethodException ignore) {
        }
        if (c == Object.class) return false;
        return hasUsableImplementation(c.getSuperclass(), m);
    }

    private static Method getSingleNonDuplicateMethod(List<Method> current) {
        final int size = current.size();
        if (size == 0) return null;
        if (size == 1) return current.get(0);
        Method m = current.remove(0);
        for (Method m2 : current) {
            if (m.getName().equals(m2.getName()) && Arrays.equals(m.getParameterTypes(), m2.getParameterTypes())) {
                continue;
            }
            return null;
        }
        return m;
    }

    /**
     * Finds the abstract method of given class, if it is a SAM type.
     */
    public static Method getSAMMethod(Class<?> c) {
        // if the class is not abstract there is no abstract method
        if (!Modifier.isAbstract(c.getModifiers())) return null;
        try {
            if (c.isInterface()) {
                // res stores the first found abstract method
                Method res = null;
                for (Method mi : c.getMethods()) {
                    // ignore methods, that are not abstract and from Object
                    if (!Modifier.isAbstract(mi.getModifiers())) continue;
                    // ignore trait methods which have a default implementation
                    if (mi.getAnnotation(Traits.Implemented.class) != null) continue;

                    String name = mi.getName();
                    // avoid throwing `NoSuchMethodException` as possible as we could
                    if (OBJECT_METHOD_NAMES.contains(name)) {
                        try {
                            Object.class.getMethod(name, mi.getParameterTypes());
                            continue;
                        } catch (NoSuchMethodException ignore) {
                        }
                    }
                    // we have two methods, so no SAM
                    if (res != null) return null;
                    res = mi;
                }
                return res;
            } else {
                List<Method> methods = new LinkedList<>();
                getAbstractMethods(c, methods);
                if (methods.isEmpty()) return null;
                methods.removeIf(m -> hasUsableImplementation(c, m));
                return getSingleNonDuplicateMethod(methods);
            }
        } catch (NoClassDefFoundError ignore) {
            return null;
        }
    }
}
