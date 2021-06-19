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
package org.codehaus.groovy.vmplugin.v16;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.vmplugin.v10.Java10;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Additional Java 16 based functions will be added here as needed.
 */
public class Java16 extends Java10 {
    private final Class<?>[] PLUGIN_DGM;

    public Java16() {
        super();
        List<Class<?>> dgmClasses = new ArrayList<>();
        Collections.addAll(dgmClasses, super.getPluginDefaultGroovyMethods());
        dgmClasses.add(PluginDefaultGroovyMethods.class);
        PLUGIN_DGM = dgmClasses.toArray(new Class<?>[0]);
    }

    @Override
    protected MethodHandles.Lookup newLookup(final Class<?> declaringClass) {
        return of(declaringClass);
    }

    public static MethodHandles.Lookup of(final Class<?> declaringClass) {
        try {
            final Method privateLookup = getPrivateLookup();
            if (privateLookup != null) {
                MethodHandles.Lookup caller = MethodHandles.lookup();
                Class<?> callerClass = caller.lookupClass();
                Module callerModule = callerClass.getModule();
                Module targetModule = declaringClass.getModule();
                if (targetModule != callerModule) {
                    if (targetModule.isNamed()) {
                        String pn = declaringClass.getPackageName();
                        if (!targetModule.isOpen(pn, callerModule)) {
                            return MethodHandles.lookup().in(declaringClass);
                        }
                    }
                }

                return (MethodHandles.Lookup) privateLookup.invoke(null, declaringClass, caller);
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
        try {
            final Class<?> receiverType = receiver.getClass();
            if (method.isDefault() && Proxy.isProxyClass(receiverType)) {
                return new ProxyDefaultMethodHandle((Proxy) receiver, method);
            }

            MethodHandles.Lookup lookup = getLookup(receiver);
            if (0 != (MethodHandles.Lookup.PRIVATE & lookup.lookupModes())) {
                return lookup.unreflectSpecial(method, receiverType).bindTo(receiver);
            }
            return lookup.unreflect(method).bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            return new GroovyRuntimeException(e);
        }
    }

    @Override
    public Object invokeHandle(Object handle, Object[] args) throws Throwable {
        if (handle instanceof ProxyDefaultMethodHandle) {
            return ((ProxyDefaultMethodHandle) handle).invokeWithArguments(args);
        }
        if (handle instanceof Throwable) throw (Throwable) handle;
        MethodHandle mh = (MethodHandle) handle;
        return mh.invokeWithArguments(args);
    }

    @Override
    public Class<?>[] getPluginDefaultGroovyMethods() {
        return PLUGIN_DGM;
    }

    @Override
    public int getVersion() {
        return 16;
    }
}
