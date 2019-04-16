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
package org.codehaus.groovy.vmplugin.v7;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.reflection.ReflectionUtils;
import org.codehaus.groovy.vmplugin.v6.Java6;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Java 7 based functions.
 *
 * For crude customization, you can add your own methods to your own version and place it on the classpath ahead of this one.
 */
public class Java7 extends Java6 {
    private static class LookupHolder {
        private static final Constructor<MethodHandles.Lookup> LOOKUP_Constructor;
        static {
            Constructor<MethodHandles.Lookup> con = null;
            try {
                con = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            } catch (NoSuchMethodException e) {
                throw new GroovyBugError(e);
            }
            try {
                if (!con.isAccessible()) {
                    final Constructor tmp = con;
                    AccessController.doPrivileged(new PrivilegedAction<Object>() {
                        @Override
                        public Object run() {
                            ReflectionUtils.trySetAccessible(tmp);
                            return null;
                        }
                    });
                }
            } catch (SecurityException se) {
                con = null;
            } catch (RuntimeException re) {
                // test for JDK9 JIGSAW
                if (!"java.lang.reflect.InaccessibleObjectException".equals(re.getClass().getName())) throw re;
                con = null;
            }
            LOOKUP_Constructor = con;
        }
    }

    private static Constructor<MethodHandles.Lookup> getLookupConstructor() {
        return LookupHolder.LOOKUP_Constructor;
    }

    @Override
    public void invalidateCallSites() {
    	IndyInterface.invalidateSwitchPoints();
    }

    @Override
    public int getVersion() {
        return 7;
    }

    @Override
    public Object getInvokeSpecialHandle(final Method method, final Object receiver) {
        if (getLookupConstructor() == null) {
            return super.getInvokeSpecialHandle(method, receiver);
        }
        if (!method.isAccessible()) {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    ReflectionUtils.trySetAccessible(method);
                    return null;
                }
            });
        }
        Class declaringClass = method.getDeclaringClass();
        try {
            return getLookupConstructor().newInstance(declaringClass, -1).
                    unreflectSpecial(method, declaringClass).
                    bindTo(receiver);
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    @Override
    public Object invokeHandle(Object handle, Object[] args) throws Throwable {
        MethodHandle mh = (MethodHandle) handle;
        return mh.invokeWithArguments(args);
    }
}
