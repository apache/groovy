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
    private final Proxy proxy;
    private final Method method;

    ProxyDefaultMethodHandle(Proxy proxy, Method method) {
        this.proxy = proxy;
        this.method = method;
    }

    Object invokeWithArguments(Object... arguments) throws Throwable {
        return InvocationHandler.invokeDefault(proxy, method, arguments);
    }
}
