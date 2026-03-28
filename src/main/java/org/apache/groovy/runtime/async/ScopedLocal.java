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
package org.apache.groovy.runtime.async;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread-scoped value holder that abstracts over {@link ThreadLocal}
 * (JDK&nbsp;&lt;&nbsp;25) and {@code java.lang.ScopedValue} (JDK&nbsp;25+),
 * presenting a unified API modelled after {@code ScopedValue}.
 *
 * <h2>Backend selection</h2>
 * <p>On JDK&nbsp;25+, where {@code java.lang.ScopedValue} is available,
 * this class delegates to the {@code ScopedValue} API, which is optimized
 * for virtual threads: bindings are automatically inherited by child
 * virtual threads, require no explicit cleanup, and impose lower
 * per-thread memory overhead than {@code ThreadLocal}.</p>
 * <p>On earlier JDK versions, the implementation falls back to a
 * conventional {@code ThreadLocal} with a save-and-restore pattern
 * inside {@code try}/{@code finally} blocks.</p>
 *
 * <h2>API overview</h2>
 * <p>The API mirrors {@code ScopedValue} as closely as possible:</p>
 * <ul>
 *   <li>{@link #get()}, {@link #orElse(Object)}, {@link #isBound()} —
 *       query the current binding.</li>
 *   <li>{@link #where(ScopedLocal, Object)} — create a {@link Carrier}
 *       that can bind one or more values for a scoped execution.</li>
 *   <li>{@link Carrier#run(Runnable)}, {@link Carrier#call(Supplier)} —
 *       execute code with the bindings active.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * private static final ScopedLocal<String> REQUEST_ID =
 *         ScopedLocal.newInstance();
 *
 * // Single binding:
 * ScopedLocal.where(REQUEST_ID, "req-42").run(() -> {
 *     assert "req-42".equals(REQUEST_ID.get());
 * });
 *
 * // Chained bindings:
 * ScopedLocal.where(REQUEST_ID, "req-1")
 *            .where(TENANT_ID, "acme")
 *            .call(() -> handleRequest());
 *
 * // Convenience instance methods:
 * String result = REQUEST_ID.where("req-7", () -> process());
 *
 * // With an initial-value supplier:
 * private static final ScopedLocal<MyCtx> CTX =
 *         ScopedLocal.withInitial(MyCtx::new);
 * }</pre>
 *
 * @param <T> the type of the scoped value
 * @since 6.0.0
 */
public abstract class ScopedLocal<T> {
    private static final boolean SCOPED_VALUE_AVAILABLE;
    private static final MethodHandle SV_NEW_INSTANCE;   // () → Object  [adapted from ScopedValue.newInstance()]
    private static final MethodHandle SV_WHERE;          // (Object, Object) → Object  [adapted from ScopedValue.where(SV, Object)]
    private static final MethodHandle SV_GET;            // (Object) → Object  [adapted from scopedValue.get()]
    private static final MethodHandle SV_IS_BOUND;       // (Object) → boolean  [adapted from scopedValue.isBound()]
    private static final MethodHandle CARRIER_RUN;       // (Object, Runnable) → void  [adapted from carrier.run(Runnable)]

    static {
        boolean available = false;
        MethodHandle newInstance = null, where = null, get = null;
        MethodHandle isBound = null;
        MethodHandle carrierRun = null;

        // ScopedValue is available as a preview API since JDK 21, but
        // was only finalized (non-preview) in JDK 25.  We require the
        // finalized version to avoid depending on preview semantics
        // that may change between releases.
        if (Runtime.version().feature() >= 25) {
            try {
                Class<?> svClass = Class.forName("java.lang.ScopedValue");
                Class<?> carrierCls = Class.forName("java.lang.ScopedValue$Carrier");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();

                // Look up the raw handles, then adapt their types so that
                // every ScopedValue/Carrier parameter and return type is
                // erased to Object.  This allows call sites to use
                // invokeExact — which the JVM can inline aggressively —
                // without needing compile-time access to the ScopedValue class.
                newInstance = lookup.findStatic(svClass, "newInstance",
                        MethodType.methodType(svClass))
                        .asType(MethodType.methodType(Object.class));

                where = lookup.findStatic(svClass, "where",
                        MethodType.methodType(carrierCls, svClass, Object.class))
                        .asType(MethodType.methodType(Object.class, Object.class, Object.class));

                get = lookup.findVirtual(svClass, "get",
                        MethodType.methodType(Object.class))
                        .asType(MethodType.methodType(Object.class, Object.class));

                isBound = lookup.findVirtual(svClass, "isBound",
                        MethodType.methodType(boolean.class))
                        .asType(MethodType.methodType(boolean.class, Object.class));

                carrierRun = lookup.findVirtual(carrierCls, "run",
                        MethodType.methodType(void.class, Runnable.class))
                        .asType(MethodType.methodType(void.class, Object.class, Runnable.class));

                available = true;
            } catch (Throwable t) {
                final Logger logger = Logger.getLogger(ScopedLocal.class.getName());
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("ScopedValue not available on JDK "
                            + Runtime.version().feature()
                            + "; using ThreadLocal fallback: " + t);
                }
            }
        }

        SCOPED_VALUE_AVAILABLE = available;
        SV_NEW_INSTANCE = newInstance;
        SV_WHERE = where;
        SV_GET = get;
        SV_IS_BOUND = isBound;
        CARRIER_RUN = carrierRun;
    }

    // ---- Factory methods ----

    /**
     * Creates a new {@code ScopedLocal} with no initial value.
     * {@link #get()} throws {@link NoSuchElementException} when no
     * binding is active; use {@link #orElse(Object)} for a safe
     * default, or check {@link #isBound()} first.
     *
     * @param <T> the value type
     * @return a new unbound scoped-local instance
     */
    public static <T> ScopedLocal<T> newInstance() {
        if (SCOPED_VALUE_AVAILABLE) {
            return new ScopedValueImpl<>();
        }
        return new ThreadLocalImpl<>();
    }

    /**
     * Creates a new {@code ScopedLocal} whose {@link #get()} method
     * returns a lazily-initialized default when no explicit binding
     * exists.  The supplier is invoked at most once per thread and
     * the result is cached, analogous to
     * {@link ThreadLocal#withInitial(Supplier)}.
     *
     * @param <T>             the value type
     * @param initialSupplier supplies the default value; must not be
     *                        {@code null}
     * @return a new scoped-local instance with a default supplier
     */
    public static <T> ScopedLocal<T> withInitial(Supplier<T> initialSupplier) {
        Objects.requireNonNull(initialSupplier, "initialSupplier must not be null");
        if (SCOPED_VALUE_AVAILABLE) {
            return new ScopedValueImpl<>(initialSupplier);
        }
        return new ThreadLocalImpl<>(initialSupplier);
    }

    // ---- Static binding (mirrors ScopedValue.where) ----

    /**
     * Creates a {@link Carrier} that binds {@code key} to {@code value}.
     * The binding takes effect when
     * {@link Carrier#run(Runnable) Carrier.run()} or
     * {@link Carrier#call(Supplier) Carrier.call()} is invoked.
     * Multiple bindings can be chained via
     * {@link Carrier#where(ScopedLocal, Object)}.
     *
     * @param <T>   the value type
     * @param key   the scoped-local to bind
     * @param value the value to bind; may be {@code null}
     * @return a carrier holding the binding
     */
    public static <T> Carrier where(ScopedLocal<T> key, T value) {
        Objects.requireNonNull(key, "key must not be null");
        return new Carrier(key, value, null);
    }

    // ---- Accessors ----

    /**
     * Returns the value bound to this scoped-local on the current
     * thread.  If created with {@link #withInitial(Supplier)}, the
     * initial value is returned (and cached) when no explicit binding
     * is active.  Otherwise, throws {@link NoSuchElementException}.
     *
     * @return the current value
     * @throws NoSuchElementException if no value is bound and no
     *                                initial supplier was provided
     */
    public abstract T get();

    /**
     * Returns the value bound to this scoped-local, or {@code other}
     * if no explicit binding and no initial supplier are active.
     *
     * @param other the fallback value
     * @return the current value or {@code other}
     */
    public abstract T orElse(T other);

    /**
     * Returns {@code true} if an explicit binding is active or an
     * initial supplier was provided.
     *
     * @return whether a value is available via {@link #get()}
     */
    public abstract boolean isBound();

    // ---- Convenience instance methods ----

    /**
     * Binds this scoped-local to {@code value} for the duration of
     * the supplier, then restores the previous binding.
     *
     * @param <R>      the result type
     * @param value    the value to bind; may be {@code null}
     * @param supplier the action to execute with the binding active
     * @return the supplier's result
     */
    public <R> R where(T value, Supplier<R> supplier) {
        return ScopedLocal.where(this, value).call(supplier);
    }

    /**
     * Binds this scoped-local to {@code value} for the duration of
     * the action, then restores the previous binding.
     *
     * @param value  the value to bind; may be {@code null}
     * @param action the action to execute with the binding active
     */
    public void where(T value, Runnable action) {
        ScopedLocal.where(this, value).run(action);
    }

    // ---- Internal ----

    /**
     * Binds this scoped-local within a scope.  Called by
     * {@link Carrier#run(Runnable)} and {@link Carrier#call(Supplier)}.
     */
    abstract void bind(Object value, Runnable action);

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    // ================================================================
    //  Carrier — mirrors ScopedValue.Carrier
    // ================================================================

    /**
     * An immutable set of scoped-local bindings that can be applied
     * atomically for the duration of a {@link Runnable} or
     * {@link Supplier}.  Obtain via
     * {@link ScopedLocal#where(ScopedLocal, Object)}.
     *
     * <p>Carriers are immutable; calling {@link #where(ScopedLocal, Object)}
     * returns a new carrier that includes the additional binding.</p>
     *
     * @since 6.0.0
     */
    public static final class Carrier {

        private final ScopedLocal<?> key;
        private final Object value;
        private final Carrier prev;

        Carrier(ScopedLocal<?> key, Object value, Carrier prev) {
            this.key = key;
            this.value = value;
            this.prev = prev;
        }

        /**
         * Adds another binding to this carrier, returning a new
         * carrier that includes all previous bindings plus the new one.
         *
         * @param <T>   the value type
         * @param key   the scoped-local to bind
         * @param value the value; may be {@code null}
         * @return a new carrier with the additional binding
         */
        public <T> Carrier where(ScopedLocal<T> key, T value) {
            Objects.requireNonNull(key, "key must not be null");
            return new Carrier(key, value, this);
        }

        /**
         * Executes the action with all bindings in this carrier active.
         * Bindings are applied in the order they were added and
         * automatically restored when the action completes (normally or
         * via exception).
         *
         * @param action the action to execute
         */
        public void run(Runnable action) {
            Objects.requireNonNull(action, "action must not be null");
            execute(action);
        }

        /**
         * Executes the supplier with all bindings in this carrier active
         * and returns its result.
         *
         * @param <R>      the result type
         * @param supplier the supplier to execute
         * @return the supplier's result
         */
        public <R> R call(Supplier<R> supplier) {
            Objects.requireNonNull(supplier, "supplier must not be null");
            Object[] result = new Object[1];
            execute(() -> result[0] = supplier.get());
            @SuppressWarnings("unchecked")
            R r = (R) result[0];
            return r;
        }

        private void execute(Runnable action) {
            if (prev == null) {
                key.bind(value, action);
            } else {
                prev.execute(() -> key.bind(value, action));
            }
        }
    }

    // ================================================================
    //  ThreadLocal-based implementation (JDK < 25)
    // ================================================================

    private static final class ThreadLocalImpl<T> extends ScopedLocal<T> {

        // Distinguishes "not set" from an explicit null binding
        private static final Object UNSET = new Object();
        private static final Object NULL_SENTINEL = new Object();

        private final ThreadLocal<Object> delegate =
                ThreadLocal.withInitial(() -> UNSET);
        private final Supplier<T> initialSupplier;

        ThreadLocalImpl() {
            this.initialSupplier = null;
        }

        ThreadLocalImpl(Supplier<T> supplier) {
            this.initialSupplier = supplier;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            Object val = delegate.get();
            if (val != UNSET) {
                return val == NULL_SENTINEL ? null : (T) val;
            }
            if (initialSupplier != null) {
                T initial = initialSupplier.get();
                // Cache per-thread (same semantics as ThreadLocal.withInitial)
                delegate.set(initial == null ? NULL_SENTINEL : initial);
                return initial;
            }
            throw new NoSuchElementException(
                    "ScopedLocal is not bound and has no initial supplier");
        }

        @Override
        @SuppressWarnings("unchecked")
        public T orElse(T other) {
            Object val = delegate.get();
            if (val != UNSET) {
                return val == NULL_SENTINEL ? null : (T) val;
            }
            if (initialSupplier != null) {
                T initial = initialSupplier.get();
                delegate.set(initial == null ? NULL_SENTINEL : initial);
                return initial;
            }
            return other;
        }

        @Override
        public boolean isBound() {
            return delegate.get() != UNSET || initialSupplier != null;
        }

        @Override
        void bind(Object value, Runnable action) {
            Object encoded = value == null ? NULL_SENTINEL : value;
            Object previous = delegate.get();
            delegate.set(encoded);
            try {
                action.run();
            } finally {
                delegate.set(previous);
            }
        }
    }

    // ================================================================
    //  ScopedValue-based implementation (JDK 25+)
    // ================================================================

    private static final class ScopedValueImpl<T> extends ScopedLocal<T> {

        /**
         * Sentinel for {@code null} bindings.
         * {@code ScopedValue} does not accept {@code null} values,
         * so we wrap/unwrap through this sentinel transparently.
         */
        private static final Object NULL_SENTINEL = new Object();

        /** The underlying {@code ScopedValue<Object>}. */
        private final Object scopedValue;

        /** Fallback for the withInitial pattern (per-thread cache). */
        private final ThreadLocal<T> fallback;

        ScopedValueImpl() {
            try {
                this.scopedValue = (Object) SV_NEW_INSTANCE.invokeExact();
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }
            this.fallback = null;
        }

        ScopedValueImpl(Supplier<T> initialSupplier) {
            try {
                this.scopedValue = (Object) SV_NEW_INSTANCE.invokeExact();
            } catch (Throwable t) {
                throw new ExceptionInInitializerError(t);
            }
            this.fallback = ThreadLocal.withInitial(initialSupplier);
        }

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            try {
                if ((boolean) SV_IS_BOUND.invokeExact(scopedValue)) {
                    Object val = (Object) SV_GET.invokeExact(scopedValue);
                    return val == NULL_SENTINEL ? null : (T) val;
                }
            } catch (Throwable t) {
                sneakyThrow(t);
            }
            if (fallback != null) {
                return fallback.get();
            }
            throw new NoSuchElementException(
                    "ScopedLocal is not bound and has no initial supplier");
        }

        @Override
        @SuppressWarnings("unchecked")
        public T orElse(T other) {
            try {
                if ((boolean) SV_IS_BOUND.invokeExact(scopedValue)) {
                    Object val = (Object) SV_GET.invokeExact(scopedValue);
                    return val == NULL_SENTINEL ? null : (T) val;
                }
            } catch (Throwable t) {
                sneakyThrow(t);
            }
            if (fallback != null) {
                return fallback.get();
            }
            return other;
        }

        @Override
        public boolean isBound() {
            try {
                if ((boolean) SV_IS_BOUND.invokeExact(scopedValue)) {
                    return true;
                }
            } catch (Throwable t) {
                sneakyThrow(t);
            }
            return fallback != null;
        }

        @Override
        void bind(Object value, Runnable action) {
            Object encoded = value == null ? NULL_SENTINEL : value;
            try {
                Object carrier = (Object) SV_WHERE.invokeExact(scopedValue, encoded);
                CARRIER_RUN.invokeExact(carrier, action);
            } catch (Throwable t) {
                sneakyThrow(t);
            }
        }
    }
}
