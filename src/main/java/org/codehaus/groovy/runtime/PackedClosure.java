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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * A single, shared {@link Closure} adapter for {@code @PackedClosures} compact closure compilation.
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
 * Captured values are bound into the {@link java.lang.invoke.MethodHandle} at construction (the
 * hoisted method has the shape {@code method(captured..., closureParams...)}), so a call dispatches
 * the caller's arguments directly, without prepending the captures each time.
 * Dispatch goes through a {@link java.lang.invoke.MethodHandle} constant to the private hoisted
 * method (bound to the receiver for an instance method), so there is no reflection.
 */
public final class PackedClosure extends Closure<Object> {

    private static final long serialVersionUID = 1L;
    private static final Object[] EMPTY = new Object[0];

    private final MethodHandle handle;
    private final String method;
    private final boolean strict;
    private final org.codehaus.groovy.reflection.ParameterTypes paramInfo;

    /**
     * @param owner        the enclosing instance the hoisted method lives on (also used as thisObject)
     * @param handle       an invoke handle to the private hoisted method (a constant in the hosting
     *                     class): dispatch needs no reflection and binds the exact method, so an
     *                     inherited packed closure can never misdispatch to a subclass's method
     * @param method       the name of the hoisted synthetic method (kept for diagnostics only)
     * @param captured     values captured from the enclosing scope, bound into the handle at construction
     * @param visibleTypes the closure's declared parameter types, so callers that key behaviour on
     *                     {@code getParameterTypes()} (DGM arity/type decisions) and argument
     *                     adaptation (vararg collection into a trailing array parameter) behave
     *                     exactly as with a generated closure class
     * @param strict       whether the delegate guard throws. {@code true} for the dynamic trust path
     *                     (an unverifiable {@code @PackedClosures} assertion must fail fast on misuse);
     *                     {@code false} when the type checker PROVED every free name owner-resolved.
     */
    public PackedClosure(final Object owner, final MethodHandle handle, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
        super(owner, owner);
        // Bind the receiver for an instance method so the handle takes (captured..., args...); a static
        // hoisted method passes the class as owner and its handle already takes no receiver. Then bind
        // the captured values in ONCE and adapt to a canonical (Object[])->Object shape over the
        // closure's own arguments, so each call is a single invokeExact over the caller's argument
        // array -- no per-call array prepending the captures, no arg-boxing invokeWithArguments.
        MethodHandle mh = (owner instanceof Class) ? handle : handle.bindTo(owner);
        mh = mh.asType(mh.type().generic());
        if (captured != null && captured.length > 0) {
            mh = MethodHandles.insertArguments(mh, 0, captured);
        }
        this.handle = mh.asSpreader(Object[].class, visibleTypes.length);
        this.method = method;
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
        // Fast path: the caller supplied exactly the declared number of arguments (the common
        // each()/collect() call). Skip the List-destructuring, correctArguments and per-arg coercion
        // adaptation -- the canonical handle's asType does any primitive (un)boxing -- and dispatch.
        if (provided.length == arity) {
            Class<?>[] types = getParameterTypes();
            Object[] a = provided;
            for (int i = 0; i < arity; i++) {
                Object arg = a[i];
                Class<?> t = types[i];
                // The canonical handle's asType unboxes/widens primitives and passes exact reference
                // matches; only a reference type the argument is NOT already an instance of needs Groovy
                // coercion here (e.g. GString -> String), matching a generated closure class.
                if (arg != null && !t.isPrimitive() && t != Object.class && !t.isInstance(arg)) {
                    if (a == provided) a = provided.clone(); // don't mutate the caller's array
                    try {
                        a[i] = org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType(arg, t);
                    } catch (RuntimeException ignore) {
                        // leave the argument as-is; the invoke below reports the mismatch
                    }
                }
            }
            return invokeHoisted(a); // captures are bound into the handle; dispatch just the arguments
        }
        // A real closure destructures a single List/Tuple argument across a multi-parameter
        // signature ({ a, b -> } called with one Tuple2); mirror that before arity normalisation.
        if (provided.length == 1 && arity > 1 && provided[0] instanceof java.util.List) {
            provided = ((java.util.List<?>) provided[0]).toArray();
        }
        // With the real parameter types available, adapt arguments exactly as metaclass dispatch on
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
        Object[] a = new Object[arity];
        for (int i = 0; i < arity; i++) {
            a[i] = (i < provided.length) ? provided[i] : null;
        }
        return invokeHoisted(a);
    }

    /**
     * Invokes the hoisted method through its {@link MethodHandle} with the caller's arguments (the
     * captured values are already bound into the handle). The handle is a compile-time constant, so
     * there is no reflection, no access override, and no name/arity resolution; a written capture's
     * shared {@code groovy.lang.Reference} is bound unchanged (the handle does not dereference it,
     * unlike metaclass argument coercion would), so writes still propagate.
     */
    private Object invokeHoisted(final Object[] args) {
        try {
            return (Object) handle.invokeExact(args);
        } catch (Throwable t) { // propagate the hoisted body's own throwable unchanged
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(t);
            return null; // unreachable
        }
    }
}
