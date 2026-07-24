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
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

/**
 * GEP-27 <b>OpenClosure spike</b> (Groovy 7 reference implementation; enabled by the
 * {@code groovy.spike.openclosure} system property — not production code).
 * <p>
 * The idea (from the GEP-27 design discussion): split a closure into a <em>lexically bound
 * core</em> — the hoisted body, its receiver, and its captured environment, all bound at
 * creation — plus a single pluggable {@link Resolver} that answers only for <em>free names</em>
 * (unqualified calls and bare names the classic runtime would resolve through the
 * owner/delegate/strategy triple). Under static compilation the resolver is provably
 * {@linkplain #EMPTY empty}; under dynamic compilation it carries the MOP. The classic
 * {@code groovy.lang.Closure} contract is then a <em>wrapper</em> over the core
 * ({@link AsClosure}) whose {@code setDelegate}/{@code setResolveStrategy} mutate a
 * {@link ClassicResolver} instead of re-routing name resolution through the closure object.
 * <p>
 * Alignment with GROOVY-12151: the core's engine <em>is</em> the packed-closure machinery — the
 * same hoisted body, the same per-class {@link GeneratedDispatcher} table and id, the same
 * capture threading. The compiler passes the resolver as a synthetic leading capture
 * ({@code captured[0]}), so the dispatch pipeline is unchanged, and {@link AsClosure} literally
 * extends {@link PackedClosure}: a packed closure is an open closure whose resolver is
 * statically known to be empty (the {@code strict} guard is the degenerate resolver that
 * forbids anyone supplying meanings). Free-name closures — which GROOVY-12151 must decline —
 * pack under this spike with full classic delegate semantics and still no per-literal class.
 */
public final class OpenClosure {

    /**
     * The single boundary between a lexically bound body and the dynamic world: free-name
     * reads and unqualified calls inside an open body are compiled into these two methods.
     * "In case of static compilation that is empty."
     */
    public interface Resolver {
        Object property(String name);
        /** Writes a free name; returns {@code value} (assignment-expression semantics). */
        Object setProperty(String name, Object value);
        Object invoke(String name, Object[] args);
    }

    /** The statically-proven resolver: there are no free names, so nothing may be asked for. */
    public static final Resolver EMPTY = new Resolver() {
        @Override public Object property(final String name) {
            throw new MissingPropertyException(name, OpenClosure.class);
        }
        @Override public Object setProperty(final String name, final Object value) {
            throw new MissingPropertyException(name, OpenClosure.class);
        }
        @Override public Object invoke(final String name, final Object[] args) {
            throw new MissingMethodException(name, OpenClosure.class, args);
        }
    };

    /**
     * Classic {@code groovy.lang.Closure} name resolution as a resolver: the owner/delegate/
     * resolveStrategy triple, moved out of the closure. Mutable exactly where the classic API
     * is ({@code setDelegate}/{@code setResolveStrategy}); resolution itself goes through the
     * MOP ({@code InvokerHelper}), so metaclass magic on the owner or delegate applies as today.
     */
    public static final class ClassicResolver implements Resolver {
        private Object owner;
        private Object delegate;
        private int strategy = Closure.OWNER_FIRST;
        private Object self; // the wrapping Closure, for TO_SELF

        public ClassicResolver(final Object owner) {
            this.owner = owner;
            this.delegate = owner;
        }

        void bind(final Object self) {
            this.self = self;
        }

        public void setDelegate(final Object delegate) { this.delegate = delegate; }
        public Object getDelegate() { return delegate; }
        public void setResolveStrategy(final int strategy) { this.strategy = strategy; }
        public int getResolveStrategy() { return strategy; }

        @Override
        public Object property(final String name) {
            switch (strategy) {
                case Closure.DELEGATE_FIRST:
                    return firstProperty(name, delegate, owner);
                case Closure.DELEGATE_ONLY:
                    return InvokerHelper.getProperty(delegate, name);
                case Closure.OWNER_ONLY:
                    return InvokerHelper.getProperty(owner, name);
                case Closure.TO_SELF:
                    return InvokerHelper.getProperty(self, name);
                default: // OWNER_FIRST
                    return firstProperty(name, owner, delegate);
            }
        }

        @Override
        public Object setProperty(final String name, final Object value) {
            switch (strategy) {
                case Closure.DELEGATE_FIRST:
                    firstSetProperty(name, value, delegate, owner);
                    break;
                case Closure.DELEGATE_ONLY:
                    InvokerHelper.setProperty(delegate, name, value);
                    break;
                case Closure.OWNER_ONLY:
                    InvokerHelper.setProperty(owner, name, value);
                    break;
                case Closure.TO_SELF:
                    InvokerHelper.setProperty(self, name, value);
                    break;
                default: // OWNER_FIRST
                    firstSetProperty(name, value, owner, delegate);
            }
            return value;
        }

        @Override
        public Object invoke(final String name, final Object[] args) {
            switch (strategy) {
                case Closure.DELEGATE_FIRST:
                    return firstInvoke(name, args, delegate, owner);
                case Closure.DELEGATE_ONLY:
                    return InvokerHelper.invokeMethod(delegate, name, args);
                case Closure.OWNER_ONLY:
                    return InvokerHelper.invokeMethod(owner, name, args);
                case Closure.TO_SELF:
                    return InvokerHelper.invokeMethod(self, name, args);
                default: // OWNER_FIRST
                    return firstInvoke(name, args, owner, delegate);
            }
        }

        private static Object firstProperty(final String name, final Object first, final Object second) {
            try {
                return InvokerHelper.getProperty(first, name);
            } catch (MissingPropertyException e) {
                if (second == null || second == first) throw e;
                return InvokerHelper.getProperty(second, name);
            }
        }

        private static void firstSetProperty(final String name, final Object value, final Object first, final Object second) {
            try {
                InvokerHelper.setProperty(first, name, value);
            } catch (MissingPropertyException e) {
                if (second == null || second == first) throw e;
                InvokerHelper.setProperty(second, name, value);
            }
        }

        private static Object firstInvoke(final String name, final Object[] args, final Object first, final Object second) {
            try {
                return InvokerHelper.invokeMethod(first, name, args);
            } catch (MissingMethodException e) {
                if (second == null || second == first) throw e;
                return InvokerHelper.invokeMethod(second, name, args);
            }
        }
    }

    /**
     * The coexistence wrapper: a real {@code groovy.lang.Closure} whose core is open. It IS a
     * {@link PackedClosure} — same dispatch tables, same capture threading (the resolver rides
     * as {@code captured[0]}, matching the hoisted body's leading {@link Resolver} parameter) —
     * with the strict guard replaced by a live {@link ClassicResolver}: {@code setDelegate} and
     * {@code setResolveStrategy} reconfigure name resolution instead of failing fast, restoring
     * the full classic contract for free-name closures without a per-literal class.
     */
    public static final class AsClosure extends PackedClosure implements GeneratedClosure {
        private static final long serialVersionUID = 1L;

        private final ClassicResolver classic;

        public AsClosure(final Object owner, final GeneratedDispatcher.Bundle dispatchers, final int id, final String method, final Object[] captured, final Class[] visibleTypes, final boolean strict) {
            super(owner, dispatchers, id, method, captured, visibleTypes, false);
            this.classic = (ClassicResolver) captured[0];
            this.classic.bind(this);
        }

        public Object doCall(final Object... args) { return dispatchAll(args); }

        @Override
        public void setDelegate(final Object delegate) {
            classic.setDelegate(delegate);
            super.setDelegate(delegate); // keep the Closure-level field consistent for getDelegate()
        }

        @Override
        public void setResolveStrategy(final int resolveStrategy) {
            classic.setResolveStrategy(resolveStrategy);
            super.setResolveStrategy(resolveStrategy);
        }
    }

    // ---------------------------------------------------------------------------------------
    // The pure core (not a Closure): the shape the GEP-27 discussion calls the future default
    // construct. Spike-grade: exact-arity dispatch through the general table; the production
    // version would share PackedClosure's argument-adaptation machinery.
    // ---------------------------------------------------------------------------------------

    private final Object receiver;
    private final GeneratedDispatcher dispatcher;
    private final int id;
    private final Object[] captured; // captured[0] is the resolver for open bodies
    private final Class[] parameterTypes;

    public OpenClosure(final Object receiver, final GeneratedDispatcher.Bundle dispatchers, final int id, final Object[] captured, final Class[] parameterTypes) {
        this.receiver = receiver;
        this.dispatcher = dispatchers.dispatcher;
        this.id = id;
        this.captured = captured;
        this.parameterTypes = parameterTypes;
    }

    public Class[] getParameterTypes() { return parameterTypes; }

    public Object call(final Object... args) {
        Object[] provided = (args != null) ? args : new Object[0];
        if (provided.length != parameterTypes.length) {
            throw new MissingMethodException("call", OpenClosure.class, provided);
        }
        Object[] all = new Object[1 + captured.length + provided.length];
        all[0] = receiver;
        System.arraycopy(captured, 0, all, 1, captured.length);
        System.arraycopy(provided, 0, all, 1 + captured.length, provided.length);
        return dispatcher.dispatch(id, all);
    }
}
