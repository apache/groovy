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
package org.codehaus.groovy.vmplugin.v9;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.vmplugin.v8.Java8;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Additional Java 9 based functions will be added here as needed.
 */
public class Java9 extends Java8 {

    private static class LookupHolder {
        private static final Method PRIVATE_LOOKUP;
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;
        static {
            Constructor<MethodHandles.Lookup> lookup = null;
            Method privateLookup = null;
            try { // java 9
                privateLookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            } catch (final NoSuchMethodException | RuntimeException e) { // java 8 or fallback if anything else goes wrong
                try {
                    lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookup.isAccessible()) {
                        lookup.setAccessible(true);
                    }
                } catch (final NoSuchMethodException ex) {
                    throw new IllegalStateException("Incompatible JVM", e);
                }
            }
            PRIVATE_LOOKUP = privateLookup;
            LOOKUP_Constructor = lookup;
        }
    }

    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        return LookupHolder.LOOKUP_Constructor;
    }

    private static Method getPrivateLookup() {
        return LookupHolder.PRIVATE_LOOKUP;
    }

    public static MethodHandles.Lookup of(final Class<?> declaringClass) {
        try {
            if (getPrivateLookup() != null) {
                return MethodHandles.Lookup.class.cast(getPrivateLookup().invoke(null, declaringClass, MethodHandles.lookup()));
            }
            return getLookupConstructor().newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).in(declaringClass);
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getVersion() {
        return 9;
    }

    @Override
    public Object getInvokeSpecialHandle(Method method, Object receiver) {
        if (getLookupConstructor() != null) {
            Class declaringClass = method.getDeclaringClass();
            try {
                return of(declaringClass).unreflectSpecial(method, receiver.getClass()).bindTo(receiver);
            } catch (ReflectiveOperationException e) {
                throw new GroovyBugError(e);
            }
        }
        return super.getInvokeSpecialHandle(method, receiver);
    }
}
