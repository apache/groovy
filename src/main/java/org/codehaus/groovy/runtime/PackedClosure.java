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

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;

import java.util.Arrays;

/**
 * A single, shared {@link Closure} adapter used by the {@code @PackedClosures} prototype.
 * <p>
 * Normally the Groovy compiler generates one inner class per closure literal
 * (e.g. {@code Owner$_method_closure1}). Under {@code @PackedClosures} an <em>eligible</em>
 * closure body is instead hoisted into a synthetic method on the enclosing ("owner")
 * class, and the closure literal is replaced by an instance of this one adapter class,
 * which dispatches back to that method. This removes the per-closure generated class
 * (and the deeply-nested {@code $_closure1$_closure2$_closure3} name explosion) while
 * still yielding a real {@code groovy.lang.Closure} instance, so features that operate
 * through {@code call()} (iteration, {@code curry}, {@code memoize}, {@code trampoline})
 * continue to work.
 * <p>
 * Captured values are stored at construction and prepended to the call arguments before
 * dispatch, so the hoisted method has the shape {@code method(captured..., closureParams...)}.
 * Dispatch goes through a cached reflective {@link java.lang.reflect.Method} rather than an
 * {@code invokedynamic} call site.
 */
public final class PackedClosure extends Closure<Object> {

    private static final long serialVersionUID = 1L;
    private static final Object[] EMPTY = new Object[0];

    private final String method;
    private final Object[] captured;
    private final boolean strict;
    private final org.codehaus.groovy.reflection.ParameterTypes paramInfo;

    /**
     * @param owner     the enclosing instance the hoisted method lives on (also used as thisObject)
     * @param method    the name of the hoisted synthetic method
     * @param captured  values captured from the enclosing scope, prepended to each call
     * @param maxParams the number of parameters the original closure declared (its visible arity)
     */
    public PackedClosure(final Object owner, final String method, final Object[] captured, final int maxParams) {
        this(owner, method, captured, maxParams, true);
    }

    /**
     * @param owner     the enclosing instance the hoisted method lives on (also used as thisObject)
     * @param method    the name of the hoisted synthetic method
     * @param captured  values captured from the enclosing scope, prepended to each call
     * @param maxParams the number of parameters the original closure declared (its visible arity)
     * @param strict    whether the delegate guard throws. {@code true} for the dynamic trust path
     *                  (an unverifiable {@code @PackedClosures} assertion must fail fast on misuse);
     *                  {@code false} when the type checker PROVED every free name owner-resolved —
     *                  then a caller-set delegate is provably never consulted, so it is stored and
     *                  ignored, exactly like a statically-compiled closure class behaves today.
     */
    public PackedClosure(final Object owner, final String method, final Object[] captured, final int maxParams, final boolean strict) {
        super(owner, owner);
        this.method = method;
        this.captured = (captured != null) ? captured : EMPTY;
        this.strict = strict;
        this.maximumNumberOfParameters = maxParams;
        Class<?>[] pt = new Class<?>[maxParams];
        Arrays.fill(pt, Object.class);
        this.parameterTypes = pt;
        this.paramInfo = null;
    }

    /**
     * @param owner        the enclosing instance the hoisted method lives on (also used as thisObject)
     * @param method       the name of the hoisted synthetic method
     * @param captured     values captured from the enclosing scope, prepended to each call
     * @param visibleTypes the closure's declared parameter types, so callers that key behaviour on
     *                     {@code getParameterTypes()} (DGM arity/type decisions) and argument
     *                     adaptation (vararg collection into a trailing array parameter) behave
     *                     exactly as with a generated closure class
     * @param strict       whether the delegate guard throws (see the other constructor)
     */
    public PackedClosure(final Object owner, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
        super(owner, owner);
        this.method = method;
        this.captured = (captured != null) ? captured : EMPTY;
        this.strict = strict;
        this.maximumNumberOfParameters = visibleTypes.length;
        this.parameterTypes = visibleTypes;
        this.paramInfo = new org.codehaus.groovy.reflection.ParameterTypes(visibleTypes);
    }

    // GEP-27 runtime guard: a hoisted body binds free names to the owner at compile time, so it
    // cannot honour a caller-set delegate. Rather than silently mis-resolve (returning the owner's
    // member where a normal closure would reach the delegate), fail fast at the point of misuse.
    // Setting the delegate to the owner is the harmless default and stays a no-op.
    @Override
    public void setDelegate(final Object delegate) {
        if (strict && delegate != null && delegate != getOwner()) {
            throw new UnsupportedOperationException(
                    "Cannot set a delegate on a @PackedClosures closure (hoisted method '" + method
                    + "'): its free names were bound to the owner at compile time, so an external "
                    + "delegate cannot be honoured. Remove @PackedClosures from this scope, or "
                    + "exclude this closure, if it relies on delegate-based resolution.");
        }
        super.setDelegate(delegate);
    }

    @Override
    public void setResolveStrategy(final int resolveStrategy) {
        // DELEGATE_FIRST/DELEGATE_ONLY need a delegate the hoisted body cannot consult; TO_SELF
        // would resolve names against this shared adapter, which has no user-defined members.
        // Only owner-based resolution (OWNER_FIRST/OWNER_ONLY) is reproducible by a hoisted body.
        if (strict && resolveStrategy != OWNER_FIRST && resolveStrategy != OWNER_ONLY) {
            throw new UnsupportedOperationException(
                    "Cannot set resolveStrategy=" + strategyName(resolveStrategy)
                    + " on a @PackedClosures closure (hoisted method '" + method + "'): a hoisted "
                    + "body resolves free names against the owner at compile time, so only "
                    + "OWNER_FIRST/OWNER_ONLY are supported. Remove @PackedClosures from this "
                    + "scope, or exclude this closure.");
        }
        super.setResolveStrategy(resolveStrategy);
    }

    private static String strategyName(final int s) {
        switch (s) {
            case DELEGATE_FIRST: return "DELEGATE_FIRST";
            case DELEGATE_ONLY:  return "DELEGATE_ONLY";
            case TO_SELF:        return "TO_SELF";
            default:             return String.valueOf(s);
        }
    }

    // Dispatch call() straight to doCall: the generic varargs doCall is otherwise vararg-ambiguous
    // through the metaclass when a single argument is itself an Object[] (the metaclass would treat
    // the element as the whole argument list and spread it). Unwrap invoker exceptions exactly as
    // Closure.call does, so user exceptions surface unwrapped.
    @Override
    public Object call(final Object... args) {
        try {
            return doCall(args);
        } catch (InvokerInvocationException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        }
    }

    public Object doCall(final Object... args) {
        Object[] provided = (args != null) ? args : EMPTY;
        int arity = getMaximumNumberOfParameters();
        // A real closure destructures a single List/Tuple argument across a multi-parameter
        // signature ({ a, b -> } called with one Tuple2); mirror that before arity normalisation.
        if (provided.length == 1 && arity > 1 && provided[0] instanceof java.util.List) {
            provided = ((java.util.List<?>) provided[0]).toArray();
        }
        // With the real parameter types available, adapt arguments exactly as reflective dispatch on
        // a generated closure class would -- collecting excess args into a trailing array parameter
        // ({ Object[] args -> } called with several values), and coercing each argument to its
        // declared parameter type (Closure -> SAM interface, GString -> String, number conversions)
        // as metaclass dispatch does.
        if (paramInfo != null) {
            try {
                provided = paramInfo.correctArguments(provided);
            } catch (RuntimeException ignore) {
                // fall through to the arity normalisation below
            }
            Class<?>[] types = getParameterTypes();
            for (int i = 0; i < provided.length && i < types.length; i++) {
                Object arg = provided[i];
                Class<?> type = types[i];
                if (arg != null && type != Object.class && !type.isInstance(arg)) {
                    try {
                        provided[i] = org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType(arg, type);
                    } catch (RuntimeException ignore) {
                        // leave the argument as-is; the invoke below reports the mismatch
                    }
                }
            }
        }
        // Normalise the visible arguments to the closure's declared arity: pad missing with
        // null (implicit-it / under-application) and drop extras (over-application), matching
        // how a normal Groovy closure tolerates arity mismatches from callers such as each().
        Object[] all = new Object[captured.length + arity];
        System.arraycopy(captured, 0, all, 0, captured.length);
        for (int i = 0; i < arity; i++) {
            all[captured.length + i] = (i < provided.length) ? provided[i] : null;
        }
        return invokeHoisted(all);
    }

    private transient java.lang.reflect.Method target;

    /**
     * Invokes the hoisted method reflectively via a cached {@link java.lang.reflect.Method}, NOT the
     * metaclass: metaclass argument coercion auto-dereferences a {@code groovy.lang.Reference}
     * argument, which would unwrap the shared Reference a written capture must arrive as.
     */
    private Object invokeHoisted(final Object[] all) {
        java.lang.reflect.Method m = target;
        if (m == null) {
            Object owner = getOwner();
            // for a closure in a static context the owner IS the declaring class
            Class<?> c = (owner instanceof Class) ? (Class<?>) owner : owner.getClass();
            outer:
            for (; c != null; c = c.getSuperclass()) {  // hoisted onto a superclass when inherited
                for (java.lang.reflect.Method cand : c.getDeclaredMethods()) {
                    if (cand.getName().equals(method) && cand.getParameterCount() == all.length) {
                        m = cand;
                        break outer;
                    }
                }
            }
            if (m == null) {
                throw new GroovyRuntimeException("Hoisted closure method not found: " + method);
            }
            m.setAccessible(true);
            target = m;
        }
        try {
            Object receiver = java.lang.reflect.Modifier.isStatic(m.getModifiers()) ? null : getOwner();
            return m.invoke(receiver, all);
        } catch (java.lang.reflect.InvocationTargetException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new GroovyRuntimeException("Failed to invoke hoisted closure method " + method, e);
        }
    }
}
