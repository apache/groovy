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
import java.util.function.Function;

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
 * Each wrapper is a lambda behind a JDK {@link Function}, and the only handle
 * looked up by name is {@code Function.apply}, a library member: nothing of this
 * class is looked up by name, so a shrinker such as R8 needs no keep rule for it
 * (GROOVY-12395). The wrappers box and collect arguments on every call, which is
 * acceptable on ART, where method handle chains are interpreted anyway.
 */
final class IndyCatchCompat {

    private static final MethodType INVOKE_TYPE =
            MethodType.methodType(Object.class, Object.class, String.class, Object[].class);
    private static final MethodType SPREAD_TYPE =
            MethodType.methodType(Object.class, Object[].class);

    /** {@code (Function, Object)Object}: the JDK interface method every wrapper runs through. */
    private static final MethodHandle APPLY;

    static {
        try {
            APPLY = MethodHandles.publicLookup().findVirtual(Function.class, "apply",
                    MethodType.methodType(Object.class, Object.class));
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
        MethodHandle exact = target.asType(INVOKE_TYPE);
        Function<Object[], Object> body = args -> {
            Object receiver = args[0];
            String name = (String) args[1];
            Object[] arguments = (Object[]) args[2];
            try {
                return exact.invokeExact(receiver, name, arguments);
            } catch (MissingMethodException e) {
                return IndyGuardsFiltersAndSignatures.invokeGroovyObjectInvoker(e, receiver, name, arguments);
            } catch (Throwable t) {
                throw sneakyThrow(t);
            }
        };
        return asHandle(body, 3).asType(INVOKE_TYPE);
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
        Function<Object[], Object> body = args -> {
            try {
                return spread.invokeExact(args);
            } catch (GroovyRuntimeException e) {
                throw sneakyThrow(ScriptBytecodeAdapter.unwrap(e));
            } catch (Throwable t) {
                throw sneakyThrow(t);
            }
        };
        return asHandle(body, arity).asType(type);
    }

    /** A handle of {@code arity} {@code Object} parameters that collects them into the function's array. */
    private static MethodHandle asHandle(final Function<Object[], Object> body, final int arity) {
        return APPLY.bindTo(body).asType(SPREAD_TYPE).asCollector(Object[].class, arity);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(final Throwable t) throws T {
        throw (T) t;
    }
}
