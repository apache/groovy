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
import org.codehaus.groovy.classgen.asm.util.TypeUtil;
import org.codehaus.groovy.reflection.ParameterTypes;
import org.codehaus.groovy.reflection.stdclasses.CachedSAMClass;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * The shared {@link Closure} adapter family for {@code @PackedClosures} compact closure
 * compilation.
 * <p>
 * Normally the Groovy compiler generates one inner class per closure literal
 * (e.g. {@code Owner$_method_closure1}). Under {@code @PackedClosures} an <em>eligible</em>
 * closure body is instead hoisted into a synthetic method on the enclosing ("owner")
 * class, and the closure literal is replaced by an instance of the fixed-arity family
 * member matching its declared parameter count ({@link Fixed0}..{@link Fixed4},
 * {@link FixedIt} for implicit-parameter literals, {@link FixedN} for higher and vararg
 * arities), which dispatches back to that method. This removes the per-closure generated
 * class (and the deeply-nested {@code $_closure1$_closure2$_closure3} name explosion)
 * while still yielding a real {@code groovy.lang.Closure} instance, so features that
 * operate through {@code call()} (iteration, {@code curry}, {@code memoize},
 * {@code trampoline}) continue to work — and the family member's declared {@code doCall}
 * signature(s) give class-level introspection (SAM-overload selection, MOP method
 * selection) exactly the view a generated closure class would present.
 * <p>
 * Dispatch goes through the hosting class's {@link GeneratedDispatcher} tables: the adapter
 * holds the class's shared dispatchers, the hoisted method's compile-time id, and its own
 * dispatch receiver (captured at construction, so {@code dehydrate()}/{@code rehydrate()}
 * leave the closure callable). A target taking one or two values beyond the receiver — the
 * overwhelmingly common closure shapes — dispatches through the array-free per-arity tables,
 * passing the receiver, captured values and arguments as plain parameters; anything else
 * packs {@code [receiver, captured..., args...]} into one array for the general table.
 * Either way the chain is ordinary, JIT-friendly bytecode — no reflection, and no
 * per-instance {@code MethodHandle} (which the JIT cannot constant-fold, making its invoker
 * path many times slower than a direct call). The id binds the exact hoisted method, so an
 * inherited packed closure can never misdispatch to a same-named method a subclass happens
 * to declare. Argument adaptation on every route replicates metaclass dispatch on a
 * generated closure class: arity mismatches raise {@code MissingMethodException}, a single
 * {@code List} destructures across a non-one-parameter signature, trailing-array parameters
 * vararg-collect, and per-parameter coercion follows the metaclass compatibility rules
 * (including {@code Closure}-to-SAM).
 */
public abstract class PackedClosure extends Closure<Object> {

    private static final long serialVersionUID = 1L;
    private static final Object[] EMPTY = new Object[0];

    /**
     * Fixed-arity members of the adapter family: the compiler instantiates the member matching the
     * literal's declared parameter count ({@link FixedN} serves higher arities), so a packed
     * closure's arity is visible to class-level introspection exactly as on a generated closure
     * class — notably {@code MetaClassHelper}'s SAM-overload disambiguation, which reflects the
     * argument <em>class</em>'s declared {@code doCall} and cannot consult the instance. Each
     * member declares ONLY the arity-matched {@code doCall}(s) (delegating to {@link #dispatchAll})
     * and carries the {@link GeneratedClosure} marker that introspection keys on, so MOP method
     * selection sees exactly the signatures a generated closure class would declare — in
     * particular, no varargs {@code doCall(Object[])} that would exact-match a single
     * {@code Object[]} argument and spread it. The hot entry points remain the base {@code call}
     * lanes, which do not allocate for these shapes.
     */
    public static final class Fixed0 extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public Fixed0(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall() { return dispatchAll(EMPTY); }
    }

    public static final class Fixed1 extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public Fixed1(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall(final Object a) { return dispatchAll(new Object[]{a}); }
    }

    /**
     * The implicit-parameter literal ({@code { it * 2 }}): a generated closure class declares both
     * {@code doCall()} and {@code doCall(Object)} for it, which class-level introspection reads as
     * the fuzzy "0 or 1" arity that lets the closure match both zero- and one-parameter SAM
     * overloads (GROOVY-10905) — so this member declares the same pair.
     */
    public static final class FixedIt extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public FixedIt(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall() { return dispatchAll(EMPTY); }
        public Object doCall(final Object a) { return dispatchAll(new Object[]{a}); }
    }

    public static final class Fixed2 extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public Fixed2(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall(final Object a, final Object b) { return dispatchAll(new Object[]{a, b}); }
    }

    public static final class Fixed3 extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public Fixed3(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall(final Object a, final Object b, final Object c) { return dispatchAll(new Object[]{a, b, c}); }
    }

    public static final class Fixed4 extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public Fixed4(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall(final Object a, final Object b, final Object c, final Object d) { return dispatchAll(new Object[]{a, b, c, d}); }
    }

    /**
     * Arities above four (rare) and vararg-shaped literals (trailing array parameter, e.g.
     * {@code { ...z -> }}, any arity): a varargs {@code doCall} — MOP selection packs the
     * arguments back into one array, and {@link #dispatchAll} applies the same arity/coercion
     * contract as the base {@code call} lanes. For the vararg shapes this varargs declaration is
     * also the <em>faithful</em> one: their generated class declares a vararg {@code doCall},
     * whose selection flexibility (zero arguments collect to an empty trailing array where a
     * fixed one-parameter {@code doCall} would null-fill) a {@code Fixed*} member cannot
     * reproduce. A single {@code Object[]} argument spreads through MOP routes here, exactly as
     * it feeds the trailing array of a classed vararg {@code doCall}.
     */
    public static final class FixedN extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;
        public FixedN(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, strict);
        }
        public Object doCall(final Object... args) { return dispatchAll(args); }
    }

    /**
     * The single bytecode-facing entry point for creating a packed closure: the compiler emits one
     * {@code INVOKESTATIC} of this factory instead of hard-wiring {@code new PackedClosure$FixedN}
     * per literal, so the fixed-arity family members stay out of the emitted-bytecode surface (only
     * {@code PackedClosure} and {@code GeneratedDispatcher.Bundle} remain there). The runtime shape
     * switch is negligible against the object-creation cost. {@code implicit} marks an implicit-{@code it}
     * literal (fuzzy 0/1 arity), {@code vararg} a trailing-array parameter; both need the varargs
     * {@code doCall} that only {@code FixedIt}/{@code FixedN} declare.
     */
    public static PackedClosure create(final Object owner, final GeneratedDispatcher.Bundle dispatchers,
            final int id, final String method, final Object[] captured, final Class[] visibleTypes,
            final boolean strict, final boolean implicit, final boolean vararg) {
        if (implicit) return new FixedIt(owner, dispatchers, id, method, captured, visibleTypes, strict);
        if (!vararg) {
            switch (visibleTypes.length) {
                case 0: return new Fixed0(owner, dispatchers, id, method, captured, visibleTypes, strict);
                case 1: return new Fixed1(owner, dispatchers, id, method, captured, visibleTypes, strict);
                case 2: return new Fixed2(owner, dispatchers, id, method, captured, visibleTypes, strict);
                case 3: return new Fixed3(owner, dispatchers, id, method, captured, visibleTypes, strict);
                case 4: return new Fixed4(owner, dispatchers, id, method, captured, visibleTypes, strict);
                default: break;
            }
        }
        return new FixedN(owner, dispatchers, id, method, captured, visibleTypes, strict);
    }

    /**
     * The hoisted method's receiver, captured at construction. Dispatch uses this — never
     * {@code getOwner()} — so {@code dehydrate()} (which nulls the visible owner for its
     * disconnect contract) and {@code rehydrate(...)} (which installs a different one) leave the
     * closure callable, exactly as a generated closure class whose body never touches the owner.
     * Serialization stays blocked either way ({@link #writeObject}).
     */
    private final Object receiver;
    private final GeneratedDispatcher dispatcher;
    private final GeneratedDispatcher.Arity1 arity1;
    private final GeneratedDispatcher.Arity2 arity2;
    private final int id;
    private final String method;
    private final Object[] captured;
    private final boolean strict;
    // Lazy: only the adapted (arity-mismatch) path needs it, and building it in the constructor
    // would put an allocation on every closure creation and push <init> past the JIT's inline budget.
    private ParameterTypes paramInfo;

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
        this.receiver = owner;
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

    /**
     * Names the hoisted body, so diagnostics identify the literal the way a generated class's
     * name would ({@code Script$_run_closure1} becomes {@code Script.$packed$closure$0}).
     */
    @Override
    public String toString() {
        Object owner = getOwner();
        Class<?> oc = (owner instanceof Class) ? (Class<?>) owner : (owner != null ? owner.getClass() : null);
        return (oc != null ? oc.getName() : "?") + "." + method + "@" + Integer.toHexString(hashCode());
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

    // Dispatch call() straight to dispatchAll: a varargs doCall would be vararg-ambiguous
    // through the metaclass when a single argument is itself an Object[] (the metaclass would treat
    // the element as the whole argument list and spread it) — which is exactly why dispatchAll is
    // NOT named doCall. Unwrap invoker exceptions exactly as Closure.call does, so user exceptions
    // surface unwrapped.
    @Override
    public final Object call(final Object... args) {
        if (!mopUnperturbed()) return mopCall((args != null) ? args : EMPTY);
        try {
            return dispatchAll((args != null) ? args : EMPTY);
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
    public final Object call(final Object arguments) {
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

    // The MOP guard (Closure#mopUnperturbed) sits on the call entry points only; dispatchAll stays
    // direct because the subclass doCalls the MOP fallback selects delegate straight to it.

    /** The full MOP route for perturbed instances: interception, categories and EMC all apply. */
    private Object mopCall(final Object[] args) {
        try {
            return getMetaClass().invokeMethod(this, "doCall", args);
        } catch (InvokerInvocationException e) {
            org.apache.groovy.internal.util.UncheckedThrow.rethrow(e.getCause());
            return null; // unreachable
        }
    }

    /**
     * The single dispatch entry every route funnels into: the {@code call} lanes, the
     * {@code Fixed*} subclasses' arity-true {@code doCall}s, and the
     * {@code PackedClosureMetaClass} short-circuit. Deliberately NOT named {@code doCall}: a
     * public varargs {@code doCall(Object[])} inherited by every family member would, under
     * MOP-routed dispatch, exact-match a single {@code Object[]} argument, beat
     * {@code doCall(Object)}, and spread the array — a generated closure class declares no such
     * method, so a classed closure never spreads.
     */
    public final Object dispatchAll(final Object[] args) {
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
            return arity2.dispatch2(id, receiver, coerceArg(provided[0], types[0]), coerceArg(provided[1], types[1]));
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
        if (caps == 0) return arity1.dispatch1(id, receiver, arg);
        if (caps == 1) return arity2.dispatch2(id, receiver, captured[0], arg);
        Object[] all = new Object[2 + caps];
        all[0] = receiver;
        System.arraycopy(captured, 0, all, 1, caps);
        all[1 + caps] = arg;
        return dispatcher.dispatch(id, all);
    }

    /** The general shape: coerce and pack {@code [owner, captured..., args...]} for the array table. */
    private Object dispatchArray(final Object[] provided, final int arity) {
        Object[] captured = this.captured;
        int base = 1 + captured.length;
        Object[] all = new Object[base + arity];
        all[0] = receiver;
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
    // implicit-it and untyped param) exit on one comparison. Primitive parameters are checked
    // against their wrapper, exactly as metaclass selection autoboxes ({ int x -> } accepts an
    // Integer, coerces a compatible number shape, and rejects a Long with MME).
    private Object coerceArg(final Object arg, final Class<?> t) {
        // hot-path shape matters: this inlines into the call lanes, so it is three cheap tests
        // with everything rare out-of-line (a primitive parameter fails isInstance and re-checks
        // against its wrapper in the tail, exactly as metaclass selection autoboxes)
        if (t == Object.class || arg == null || t.isInstance(arg)) return arg;
        return coerceArgSlow(arg, t);
    }

    private Object coerceArgSlow(final Object arg, final Class<?> t) {
        if (t.isPrimitive()) {
            Class<?> ct = TypeUtil.autoboxType(t);
            return ct.isInstance(arg) ? arg : coerce(arg, ct);
        }
        return coerce(arg, t);
    }

    // A generated closure class accepts an argument exactly when metaclass method selection would:
    // MetaClassHelper-compatible shapes coerce (BigDecimal literal -> double parameter, GString ->
    // String, Closure -> SAM), an array parameter vararg-collects a single value, and anything else
    // is a MissingMethodException -- never a quiet castToType (String -> Integer would parse, and
    // String -> Object[] would explode into characters, where the metaclass rejects both).
    private Object coerce(final Object arg, final Class<?> t) {
        if (t.isArray()) {
            Class<?> ac = arg.getClass();
            // an array of the same dimension IS the parameter array, arriving in a different
            // guise (Integer[] where int[] is declared -- e.g. re-boxed by an upstream varargs
            // hop): convert it element-wise, exactly as CachedMethod invocation coerces after
            // ParameterTypes#fitToVargs applies the same dimension rule. Anything else is a
            // single element to vararg-wrap -- never castToType a non-array (String -> Object[]
            // would explode into characters, where the metaclass wraps).
            if (ac.isArray() && ArrayTypeUtils.dimension(ac) == ArrayTypeUtils.dimension(t)) {
                try {
                    return DefaultTypeTransformation.castToType(arg, t);
                } catch (RuntimeException e) {
                    throw new groovy.lang.MissingMethodException("doCall", getClass(), new Object[]{arg});
                }
            }
            Object boxed = java.lang.reflect.Array.newInstance(t.getComponentType(), 1);
            java.lang.reflect.Array.set(boxed, 0, arg);
            return boxed;
        }
        // a Closure argument coerces to a SAM-interface parameter (metaclass selection accepts
        // Closure for SAM params -- e.g. action.run({->}) into { Proc it -> it.doSomething() });
        // castToType performs the standard proxy conversion
        boolean closureToSam = groovy.lang.Closure.class.isAssignableFrom(arg.getClass())
                && t.isInterface()
                && CachedSAMClass.getSAMMethod(t) != null;
        if (!closureToSam && !MetaClassHelper.isAssignableFrom(t, arg.getClass())) {
            throw new groovy.lang.MissingMethodException("doCall", getClass(), new Object[]{arg});
        }
        try {
            return DefaultTypeTransformation.castToType(arg, t);
        } catch (RuntimeException e) {
            throw new groovy.lang.MissingMethodException("doCall", getClass(), new Object[]{arg});
        }
    }

    private Object doCallAdapted(Object[] provided, final int arity) {
        int base = 1 + captured.length; // packed layout: [owner, captured..., args...]
        final Object[] original = provided; // MME reports the caller's argument types, pre-adaptation
        // A real closure destructures a single List/Tuple argument across a non-one-parameter
        // signature ({ a, b -> } called with one Tuple2; { -> } driven with a Tuple0); mirror
        // that before arity normalisation. A one-parameter closure keeps the list as its argument.
        if (provided.length == 1 && arity != 1 && provided[0] instanceof java.util.List) {
            provided = ((java.util.List<?>) provided[0]).toArray();
        }
        // With the real parameter types available, adapt arguments exactly as metaclass dispatch on
        // a generated closure class would -- collecting excess args into a trailing array parameter
        // ({ Object[] args -> } called with several values), and coercing each argument to its
        // declared parameter type (Closure -> SAM interface, GString -> String, number conversions)
        // as metaclass dispatch does.
        ParameterTypes info = paramInfo;
        if (info == null) {
            // benign race: ParameterTypes is derived from the final parameterTypes, so any winner
            // is equivalent
            paramInfo = info = new ParameterTypes(parameterTypes);
        }
        // arguments to a zero-parameter closure are always a mismatch ({ -> } invoked by each());
        // checked before correctArguments, which would silently drop them for a no-arg signature
        if (arity == 0 && provided.length > 0) {
            throw new groovy.lang.MissingMethodException("doCall", getClass(), original);
        }
        try {
            provided = info.correctArguments(provided);
        } catch (RuntimeException ignore) {
            // fall through to the arity check below
        }
        // A generated closure class accepts exactly two arity adaptations beyond the vararg
        // collection above: zero arguments to a one-parameter closure ({ it -> }() and
        // { x -> }() both bind null), and the single-List destructuring already applied. Any
        // other mismatch is a MissingMethodException from metaclass method selection -- callers
        // like MockFor depend on the failure ({ -> } invoked with arguments must not run).
        if (provided.length != arity) {
            if (provided.length == 0 && arity == 1) {
                provided = new Object[]{null};
            } else {
                throw new groovy.lang.MissingMethodException("doCall", getClass(), original);
            }
        }
        Class<?>[] types = parameterTypes;
        for (int i = 0; i < provided.length && i < types.length; i++) {
            provided[i] = coerceArg(provided[i], types[i]);
        }
        Object[] all = new Object[base + arity];
        all[0] = receiver;
        System.arraycopy(captured, 0, all, 1, captured.length);
        System.arraycopy(provided, 0, all, base, arity);
        return dispatcher.dispatch(id, all);
    }
}
