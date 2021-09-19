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

import org.codehaus.groovy.GroovyBugError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Represents default method handle of proxy.
 * <p>
 * The given {@code method} must be a default method declared in a proxy interface of the {@code proxy}'s class
 * or inherited from its superinterface directly or indirectly.
 *
 * @since 4.0.0
 */
class ProxyDefaultMethodHandle {
    private static final MethodHandle INVOKE_DEFAULT_METHOD_HANDLE;
    static {
        try {
            // `invokeDefault` is JDK 16+ API, but we still build Groovy with JDK11,
            // so use method handle instead of invoking the method directly
            INVOKE_DEFAULT_METHOD_HANDLE = MethodHandles.lookup().findStatic(
                                                InvocationHandler.class, "invokeDefault",
                                                MethodType.methodType(Object.class, Object.class, Method.class, Object[].class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new GroovyBugError(e);
        }
    }

    private final Proxy proxy;
    private final Method method;

    ProxyDefaultMethodHandle(Proxy proxy, Method method) {
        this.proxy = proxy;
        this.method = method;
    }

    Object invokeWithArguments(Object... arguments) throws Throwable {
        return INVOKE_DEFAULT_METHOD_HANDLE.invokeExact(((Object) this.proxy), method, arguments);
    }
}
