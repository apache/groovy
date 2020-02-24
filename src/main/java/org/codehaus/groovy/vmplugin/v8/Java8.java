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
package org.codehaus.groovy.vmplugin.v8;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.vmplugin.v7.Java7;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Java 8 based functions.
 *
 * @since 2.5.0
 */
public class Java8 extends Java7 {

    private final Class<?>[] PLUGIN_DGM;

    public Java8() {
        super();
        List<Class<?>> dgmClasses = new ArrayList<>();
        Collections.addAll(dgmClasses, (Class<?>[]) super.getPluginDefaultGroovyMethods());
        dgmClasses.add(PluginDefaultGroovyMethods.class);
        PLUGIN_DGM = dgmClasses.toArray(new Class<?>[0]);
    }

    @Override
    public Class<?>[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    @Override
    public int getVersion() {
        return 8;
    }

    @Override
    protected int getElementCode(ElementType value) {
        switch (value) {
            case TYPE_PARAMETER:
                return AnnotationNode.TYPE_PARAMETER_TARGET;
            case TYPE_USE:
                return AnnotationNode.TYPE_USE_TARGET;
        }
        return super.getElementCode(value);
    }

    @Override
    protected Parameter[] processParameters(CompileUnit compileUnit, Method m) {
        java.lang.reflect.Parameter[] parameters = m.getParameters();
        Type[] types = m.getGenericParameterTypes();
        Parameter[] params = Parameter.EMPTY_ARRAY;
        if (types.length > 0) {
            params = new Parameter[types.length];
            for (int i = 0; i < params.length; i++) {
                java.lang.reflect.Parameter p = parameters[i];
                String name = p.isNamePresent() ? p.getName() : "param" + i;
                params[i] = makeParameter(compileUnit, types[i], m.getParameterTypes()[i], m.getParameterAnnotations()[i], name);
            }
        }
        return params;
    }

    private static class LookupHolder {
        private static final Method PRIVATE_LOOKUP;
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;

        static {
            Constructor<MethodHandles.Lookup> lookup = null;
            Method privateLookup = null;
            try { // java 9+ friendly
                privateLookup = MethodHandles.class.getMethod("privateLookupIn", Class.class, MethodHandles.Lookup.class);
            } catch (final NoSuchMethodException | RuntimeException e) { // java 8 or fallback if anything else goes wrong
                try {
                    lookup = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);
                    if (!lookup.isAccessible()) {
                        trySetAccessible(lookup);
                    }
                } catch (final NoSuchMethodException ex) {
                    throw new IllegalStateException("Incompatible JVM", e);
                }
            }
            PRIVATE_LOOKUP = privateLookup;
            LOOKUP_Constructor = lookup;
        }
    }

    private static boolean trySetAccessible(AccessibleObject ao) {
        try {
            ao.setAccessible(true);
            return true;
        } catch (SecurityException e) {
            throw e;
        } catch (Throwable t) {
            return false;
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
            final Method privateLookup = getPrivateLookup();
            if (privateLookup != null) {
                return (MethodHandles.Lookup) privateLookup.invoke(null, declaringClass, MethodHandles.lookup());
            }
            return getLookupConstructor().newInstance(declaringClass, MethodHandles.Lookup.PRIVATE).in(declaringClass);
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (final InvocationTargetException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    @Override
    public Object getInvokeSpecialHandle(Method method, Object receiver) {
        final Class<?> receiverType = receiver.getClass();
        try {
            return of(receiverType).unreflectSpecial(method, receiverType).bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            return super.getInvokeSpecialHandle(method, receiver);
        }
    }
}
