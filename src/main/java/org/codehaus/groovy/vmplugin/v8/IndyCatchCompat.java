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
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Exception handling around a call-site target expressed in plain Java rather
 * than with {@link MethodHandles#catchException} (GROOVY-12387).
 * <p>
 * Android's ART implements {@code catchException} with an exact class test,
 * {@code thrown.getClass() == exType}, where the JDK applies
 * {@code exType.isInstance(thrown)}, so an exception of a subclass of the
 * declared type bypasses the handler (libcore {@code Transformers.CatchException}).
 * The runtime throws subclasses at both of the Selector's handler sites:
 * {@code MissingMethodExceptionNoStack} where {@code MissingMethodException} is
 * declared, and the whole {@code GroovyRuntimeException} hierarchy at the
 * unwrapper. On ART the GroovyObject fallback behind a failed metaclass call
 * therefore never ran and runtime exceptions escaped unwrapped. The
 * {@link Selector} uses these wrappers instead of the combinator when running
 * on Android; on a JVM the combinator stays and the runtime never calls
 * these wrappers.
 * <p>
 * The wrappers box and collect arguments on every call, which is acceptable
 * on ART, where method handle chains are interpreted anyway.
 */
final class IndyCatchCompat {

    private static final MethodType INVOKE_TYPE =
            MethodType.methodType(Object.class, Object.class, String.class, Object[].class);
    private static final MethodType SPREAD_TYPE =
            MethodType.methodType(Object.class, Object[].class);

    private static final MethodHandle INVOKE_WITH_FALLBACK;
    private static final MethodHandle INVOKE_UNWRAPPING;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            INVOKE_WITH_FALLBACK = lookup.findStatic(IndyCatchCompat.class, "invokeWithFallback",
                    MethodType.methodType(Object.class, MethodHandle.class, Object.class, String.class, Object[].class));
            INVOKE_UNWRAPPING = lookup.findStatic(IndyCatchCompat.class, "invokeUnwrapping",
                    MethodType.methodType(Object.class, MethodHandle.class, Object[].class));
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    private IndyCatchCompat() {
    }

    /**
     * Wraps a metaclass invocation handle of type {@code (Object receiver,
     * String name, Object[] args)Object} so that a {@link MissingMethodException}
     * is routed to {@link IndyGuardsFiltersAndSignatures#invokeGroovyObjectInvoker}.
     *
     * @param target the metaclass invocation handle
     * @return a handle of the same type with the fallback attached
     */
    static MethodHandle withGroovyObjectFallback(final MethodHandle target) {
        return INVOKE_WITH_FALLBACK.bindTo(target.asType(INVOKE_TYPE));
    }

    /**
     * Wraps a handle of any type so that a {@link GroovyRuntimeException} thrown
     * by it is replaced with {@link ScriptBytecodeAdapter#unwrap}'s result, as
     * {@link Selector.MethodSelector#addExceptionHandler} does with the combinator.
     *
     * @param target the call-site target
     * @return a handle of the same type that unwraps runtime exceptions
     */
    static MethodHandle unwrapping(final MethodHandle target) {
        MethodType type = target.type();
        int arity = type.parameterCount();
        MethodHandle spread = target.asSpreader(Object[].class, arity).asType(SPREAD_TYPE);
        return INVOKE_UNWRAPPING.bindTo(spread).asCollector(Object[].class, arity).asType(type);
    }

    private static Object invokeWithFallback(final MethodHandle target, final Object receiver, final String name, final Object[] args) throws Throwable {
        try {
            return target.invokeExact(receiver, name, args);
        } catch (MissingMethodException e) {
            return IndyGuardsFiltersAndSignatures.invokeGroovyObjectInvoker(e, receiver, name, args);
        }
    }

    private static Object invokeUnwrapping(final MethodHandle target, final Object[] args) throws Throwable {
        try {
            return target.invokeExact(args);
        } catch (GroovyRuntimeException e) {
            throw ScriptBytecodeAdapter.unwrap(e);
        }
    }
}
