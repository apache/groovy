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
package org.codehaus.groovy.runtime;

/**
 * Compiler-internal signal used to implement non-local return from closures
 * ({@code return@methodName expr}). The compiler synthesizes a unique token per
 * activation of the target method and wraps the method body in a handler that
 * catches this exception, returning the carried value when the token matches.
 * <p>
 * Instances carry no stack trace, so the throw/catch round trip is cheap. If a
 * closure containing a non-local return escapes and is invoked after its target
 * activation has completed, the token no longer matches any live handler and
 * this exception surfaces to the caller rather than corrupting an unrelated
 * activation.
 * <p>
 * This class is not intended for direct use in user code.
 *
 * @since 6.0.0
 */
public class NonLocalReturn extends RuntimeException {

    private static final long serialVersionUID = -6045879712662530680L;

    private final transient Object token;
    private final transient Object value;

    public NonLocalReturn(final Object token, final Object value) {
        super("Non-local return escaped: the closure was invoked outside the dynamic extent of its target method activation",
                null, false, false);
        this.token = token;
        this.value = value;
    }

    /**
     * The per-activation token identifying the target method invocation.
     */
    public Object getToken() {
        return token;
    }

    /**
     * The value to be returned from the target method.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Whether this signal targets the activation identified by the given token.
     */
    public boolean matches(final Object candidate) {
        return token == candidate;
    }

    /**
     * Throws a {@code NonLocalReturn} carrying the given token and value.
     * Called from compiler-generated code at {@code return@target} sites;
     * declared with a return type so call sites can occupy value positions.
     */
    public static Object raise(final Object token, final Object value) {
        throw new NonLocalReturn(token, value);
    }
}
