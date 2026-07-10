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
 * Dispatch goes through the hosting class's {@link GeneratedDispatcher} tables: the adapter
 * holds the class's shared dispatchers and the hoisted method's compile-time id. A target
 * taking one or two values beyond the receiver — the overwhelmingly common closure shapes —
 * dispatches through the array-free per-arity tables, passing the receiver, captured values
 * and arguments as plain parameters; anything else packs {@code [owner, captured..., args...]}
 * into one array for the general table. Either way the chain is ordinary, JIT-friendly
 * bytecode — no reflection, and no per-instance {@code MethodHandle} (which the JIT cannot
 * constant-fold, making its invoker path many times slower than a direct call). The id binds
 * the exact hoisted method, so an inherited packed closure can never misdispatch to a
 * same-named method a subclass happens to declare.
 */
public final class PackedClosure extends Closure<Object> {

    private static final long serialVersionUID = 1L;
    private static final Object[] EMPTY = new Object[0];

    private final GeneratedDispatcher dispatcher;
    private final GeneratedDispatcher.Arity1 arity1;
    private final GeneratedDispatcher.Arity2 arity2;
    private final int id;
    private final String method;
    private final Object[] captured;
    private final boolean strict;
    // Lazy: only the adapted (arity-mismatch) path needs it, and building it in the constructor
    // would put an allocation on every closure creation and push <init> past the JIT's inline budget.
    private org.codehaus.groovy.reflection.ParameterTypes paramInfo;

    /**
     * @param owner        the enclosing instance the hoisted method lives on (also used as thisObject;
     *                     the enclosing class itself for a hoisted method in a static context)
     * @param dispatchers  the hosting class's shared dispatch tables (see {@link GeneratedDispatcher})
     * @param id           the hoisted method's compile-time-assigned index in those tables
     * @param method       the name of the hoisted synthetic method (kept for diagnostics only)
     * @param captured     values captured from the enclosing scope, passed before the call arguments
     *                     on every dispatch (a written capture passes its shared
     *                     {@code groovy.lang.Reference} unchanged, so writes still propagate)
     * @param visibleTypes the closure's declared parameter types, so callers that key behaviour on
     *                     {@code getParameterTypes()} (DGM arity/type decisions) and argument
     *                     adaptation (vararg collection into a trailing array parameter) behave
     *                     exactly as with a generated closure class
     * @param strict       whether the delegate guard throws. {@code true} for the dynamic trust path
     *                     (an unverifiable {@code @PackedClosures} assertion must fail fast on misuse);
     *                     {@code false} when the type checker PROVED every free name owner-resolved.
     */
    public PackedClosure(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
        super(owner, owner);
        this.dispatcher = dispatchers.dispatcher;
        this.arity1 = dispatchers.arity1;
        this.arity2 = dispatchers.arity2;
        this.id = id;
        this.method = method;
        this.captured = (captured != null) ? captured : EMPTY;
        this.strict = strict;
        this.maximumNumberOfParameters = visibleTypes.length;
        this.parameterTypes = visibleTypes;
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

    /**
     * Packed closures are not serializable: the dispatch state is bound to hidden classes of the
     * hosting class's module. Without this, the default field walk would fail deep inside
     * {@code ObjectOutputStream} with a cryptic hidden-class name; instead fail fast with a
     * message naming the closure and the opt-out. Note {@code Closure#dehydrate()} — the classed
     * closure's route to serializability — does not help a packed closure: the dispatch fields
     * remain, so opting the declaring scope out of packing is the remedy.
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws java.io.IOException {
        Object owner = getOwner();
        String where = (owner == null) ? ""
                : " on " + ((owner instanceof Class) ? ((Class<?>) owner).getName() : owner.getClass().getName());
        throw new java.io.NotSerializableException(
                "packed closure (hoisted body '" + method + "'" + where + "). Packed closures are not"
                + " serializable and dehydrate() does not help (the dispatch state remains); exclude"
                + " the declaring scope from packing -- e.g. @PackedClosures(mode = DISABLED) on the"
                + " method or class, or compile without groovy.target.closure.pack -- if this closure"
                + " must be serialized.");
    }

    // Dispatch call() straight to doCall: the generic varargs doCall is otherwise vararg-ambiguous
    // through the metaclass when a single argument is itself an Object[] (the metaclass would treat
    // the element as the whole argument list and spread it). Unwrap invoker exceptions exactly as
    // Closure.call does, so user exceptions surface unwrapped.
    @Override
    public Object call(final Object... args) {
        if (!mopUnperturbed()) return mopCall((args != null) ? args : EMPTY);
        try {
            return doCall(args);
        } catch (InvokerInvocationException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        }
    }

    // Fast lane for the ubiquitous one-argument call (each/collect on an arity-1 closure): the base
    // class would wrap the argument in an Object[] and re-enter the varargs path, costing an array
    // and a hop through doCall. This method must stay SMALL: it sits between a hot caller loop and
    // the hoisted body, and inlines into it when hot — with the per-arity tables there is then no
    // argument array at all on the common shapes.
    @Override
    public Object call(final Object arguments) {
        // one fused branch to the cold path keeps this lane small enough to inline even at
        // cool call sites (and its own standalone compilation lean): the argument-wrapping
        // fallbacks allocate, so they live out-of-line in callGeneral
        if (maximumNumberOfParameters != 1 || !mopUnperturbed()) return callGeneral(arguments);
        try {
            return dispatchOne(coerceArg(arguments, parameterTypes[0]));
        } catch (InvokerInvocationException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        }
    }

    private Object callGeneral(final Object arguments) {
        if (maximumNumberOfParameters != 1) return call(new Object[]{arguments});
        return mopCall(new Object[]{arguments});
    }

    // The MOP guard (Closure#mopUnperturbed) sits on the call entry points only; doCall stays
    // direct because it is the very method the MOP fallback dispatches to.

    /** The full MOP route for perturbed instances: interception, categories and EMC all apply. */
    private Object mopCall(final Object[] args) {
        try {
            return getMetaClass().invokeMethod(this, "doCall", args);
        } catch (InvokerInvocationException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        }
    }

    public Object doCall(final Object... args) {
        // Like call(Object), the exact-arity paths here must stay small enough to inline.
        // Anything rare lives in doCallAdapted/coerce, out of the inlining budget.
        Object[] provided = (args != null) ? args : EMPTY;
        int arity = maximumNumberOfParameters; // direct field reads: cheaper than the virtual getters
        // the caller supplied exactly the declared number of arguments (the common each()/collect()
        // call): skip the List-destructuring, correctArguments and vararg-collection adaptation --
        // the dispatch table's case does any primitive unboxing -- and dispatch
        if (provided.length != arity) return doCallAdapted(provided, arity);
        Class<?>[] types = parameterTypes;
        if (arity == 1) return dispatchOne(coerceArg(provided[0], types[0]));
        if (arity == 2 && captured.length == 0) {
            return arity2.dispatch2(id, getOwner(), coerceArg(provided[0], types[0]), coerceArg(provided[1], types[1]));
        }
        return dispatchArray(provided, arity);
    }

    /**
     * Dispatches a single value: through the array-free tables while the captured values fit
     * ({@code dispatch1} for none, {@code dispatch2} for one — together the overwhelmingly
     * common closure shapes), else through the general array table.
     */
    private Object dispatchOne(final Object arg) {
        Object[] captured = this.captured;
        int caps = captured.length;
        if (caps == 0) return arity1.dispatch1(id, getOwner(), arg);
        if (caps == 1) return arity2.dispatch2(id, getOwner(), captured[0], arg);
        Object[] all = new Object[2 + caps];
        all[0] = getOwner();
        System.arraycopy(captured, 0, all, 1, caps);
        all[1 + caps] = arg;
        return dispatcher.dispatch(id, all);
    }

    /** The general shape: coerce and pack {@code [owner, captured..., args...]} for the array table. */
    private Object dispatchArray(final Object[] provided, final int arity) {
        Object[] captured = this.captured;
        int base = 1 + captured.length;
        Object[] all = new Object[base + arity];
        all[0] = getOwner();
        System.arraycopy(captured, 0, all, 1, captured.length);
        Class<?>[] types = parameterTypes;
        for (int i = 0; i < arity; i++) {
            all[base + i] = coerceArg(provided[i], types[i]);
        }
        return dispatcher.dispatch(id, all);
    }

    // The dispatch table's case checkcasts/unboxes to the declared types; only a reference type
    // the argument is NOT already an instance of needs Groovy coercion (e.g. GString -> String),
    // matching a generated closure class. Cheapest test first: Object-typed parameters (every
    // implicit-it and untyped param) exit on one comparison.
    private static Object coerceArg(final Object arg, final Class<?> t) {
        if (t != Object.class && arg != null && !t.isPrimitive() && !t.isInstance(arg)) {
            return coerce(arg, t);
        }
        return arg;
    }

    private static Object coerce(final Object arg, final Class<?> t) {
        try {
            return org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation.castToType(arg, t);
        } catch (RuntimeException ignore) {
            return arg; // leave the argument as-is; the dispatch reports the mismatch
        }
    }

    private Object doCallAdapted(Object[] provided, final int arity) {
        int base = 1 + captured.length; // packed layout: [owner, captured..., args...]
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
        org.codehaus.groovy.reflection.ParameterTypes info = paramInfo;
        if (info == null) {
            // benign race: ParameterTypes is derived from the final parameterTypes, so any winner
            // is equivalent
            paramInfo = info = new org.codehaus.groovy.reflection.ParameterTypes(parameterTypes);
        }
        try {
            provided = info.correctArguments(provided);
        } catch (RuntimeException ignore) {
            // fall through to the arity normalisation below
        }
        Class<?>[] types = parameterTypes;
        for (int i = 0; i < provided.length && i < types.length; i++) {
            Object arg = provided[i];
            Class<?> type = types[i];
            if (arg != null && type != Object.class && !type.isInstance(arg)) {
                provided[i] = coerce(arg, type);
            }
        }
        // Normalise the visible arguments to the closure's declared arity: pad missing with
        // null (implicit-it / under-application) and drop extras (over-application), matching
        // how a normal Groovy closure tolerates arity mismatches from callers such as each().
        Object[] all = new Object[base + arity];
        all[0] = getOwner();
        System.arraycopy(captured, 0, all, 1, captured.length);
        for (int i = 0; i < arity; i++) {
            all[base + i] = (i < provided.length) ? provided[i] : null;
        }
        return dispatcher.dispatch(id, all);
    }
}
