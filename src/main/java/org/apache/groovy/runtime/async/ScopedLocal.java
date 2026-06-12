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
 * <p>On JDK&nbsp;25+, where finalized {@code java.lang.ScopedValue} is
 * available, this class delegates to the {@code ScopedValue} API.
 * On earlier JDK versions it falls back to a conventional
 * {@code ThreadLocal} with save-and-restore semantics.</p>
 *
 * <h2>API overview</h2>
 * <p>The API mirrors the main {@code ScopedValue} operations:</p>
 * <ul>
 *   <li>{@link #get()}, {@link #orElse(Object)}, {@link #orElseThrow(Supplier)},
 *       {@link #isBound()} — query the current binding.</li>
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
 * ScopedLocal.where(REQUEST_ID, "req-42").run(() -> {
 *     assert "req-42".equals(REQUEST_ID.get());
 * });
 *
 * ScopedLocal.where(REQUEST_ID, "req-1")
 *         .where(TENANT_ID, "acme")
 *         .call(() -> handleRequest());
 *
 * String result = REQUEST_ID.where("req-7", () -> process());
 *
 * private static final ScopedLocal<MyCtx> CTX =
 *         ScopedLocal.withInitial(MyCtx::new);
 * }</pre>
 *
 * @param <T> the type of the scoped value
 * @since 6.0.0
 */
public abstract class ScopedLocal<T> {
    /** Logger used for backend-detection diagnostics. */
    private static final Logger LOGGER = Logger.getLogger(ScopedLocal.class.getName());

    /** Standard message used when an unbound scoped-local is queried without a default supplier. */
    private static final String UNBOUND_MESSAGE =
            "ScopedLocal is not bound and has no initial supplier";

    /** Cached reflective view of the optional JDK {@code ScopedValue} API. */
    private static final ScopedValueBindings SCOPED_VALUE_BINDINGS = ScopedValueBindings.detect();

    /**
     * Whether the runtime provides a usable {@code ScopedValue} backend.
     * <p>
     * This field has package visibility so the test suite can verify backend
     * selection without duplicating the detection logic.
     */
    static final boolean SCOPED_VALUE_AVAILABLE = SCOPED_VALUE_BINDINGS.isAvailable();

    /**
     * Method handle for {@code ScopedValue.newInstance()}, adapted to the erased
     * signature {@code ()Object} for stable {@code invokeExact} usage.
     */
    static final MethodHandle SV_NEW_INSTANCE = SCOPED_VALUE_BINDINGS.newInstanceHandle();

    /**
     * Method handle for {@code ScopedValue.where(ScopedValue, Object)}, adapted
     * to the erased signature {@code (Object, Object)Object}.
     */
    static final MethodHandle SV_WHERE = SCOPED_VALUE_BINDINGS.whereHandle();

    /**
     * Method handle for {@code ScopedValue.get()}, adapted to the erased
     * signature {@code (Object)Object}.
     */
    static final MethodHandle SV_GET = SCOPED_VALUE_BINDINGS.getHandle();

    /**
     * Method handle for {@code ScopedValue.isBound()}, adapted to the erased
     * signature {@code (Object)boolean}.
     */
    static final MethodHandle SV_IS_BOUND = SCOPED_VALUE_BINDINGS.isBoundHandle();

    /**
     * Method handle for {@code ScopedValue.Carrier.run(Runnable)}, adapted to
     * the erased signature {@code (Object, Runnable)void}.
     */
    static final MethodHandle CARRIER_RUN = SCOPED_VALUE_BINDINGS.carrierRunHandle();

    /**
     * Creates a new {@code ScopedLocal} with no initial value.
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
     * returns a lazily initialized default when no explicit binding exists.
     * The supplier is invoked at most once per thread and the result is cached,
     * analogous to {@link ThreadLocal#withInitial(Supplier)}.
     *
     * @param <T>             the value type
     * @param initialSupplier supplies the default value
     * @return a new scoped-local instance with a default supplier
     */
    public static <T> ScopedLocal<T> withInitial(Supplier<? extends T> initialSupplier) {
        Objects.requireNonNull(initialSupplier, "initialSupplier must not be null");
        if (SCOPED_VALUE_AVAILABLE) {
            return new ScopedValueImpl<>(initialSupplier);
        }
        return new ThreadLocalImpl<>(initialSupplier);
    }

    /**
     * Creates a {@link Carrier} that binds {@code key} to {@code value}.
     * The binding takes effect when {@link Carrier#run(Runnable)} or
     * {@link Carrier#call(Supplier)} is invoked.
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

    /**
     * Returns the value bound to this scoped-local on the current thread.
     *
     * @return the current value
     * @throws NoSuchElementException if no value is bound and no initial supplier was provided
     */
    public abstract T get();

    /**
     * Returns the value bound to this scoped-local, or {@code other}
     * if no binding is active and no initial supplier is configured.
     *
     * @param other the fallback value
     * @return the current value or {@code other}
     */
    public abstract T orElse(T other);

    /**
     * Returns the value bound to this scoped-local if available, otherwise throws
     * an exception produced by the {@code exceptionSupplier}.
     * <p>
     * If an initial supplier is configured (via {@link #withInitial(Supplier)}),
     * this method returns the initial value rather than throwing.
     *
     * @param <X>               the type of the exception to throw if not bound
     * @param exceptionSupplier a supplier that produces the exception to throw;
     *                          must not be {@code null}
     * @return the current value
     * @throws X                    if no value is bound and no initial supplier is configured
     * @throws NullPointerException if {@code exceptionSupplier} is {@code null}
     */
    public abstract <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X;

    /**
     * Returns {@code true} if an explicit binding is active or
     * an initial supplier is configured.
     *
     * @return whether a value is available via {@link #get()}
     */
    public abstract boolean isBound();

    /**
     * Binds this scoped-local to {@code value} for the duration of
     * the supplier, then restores the previous binding.
     *
     * @param <R>      the result type
     * @param value    the value to bind; may be {@code null}
     * @param supplier the action to execute with the binding active
     * @return the supplier's result
     */
    public final <R> R where(T value, Supplier<R> supplier) {
        return ScopedLocal.where(this, value).call(supplier);
    }

    /**
     * Binds this scoped-local to {@code value} for the duration of
     * the action, then restores the previous binding.
     *
     * @param value  the value to bind; may be {@code null}
     * @param action the action to execute with the binding active
     */
    public final void where(T value, Runnable action) {
        ScopedLocal.where(this, value).run(action);
    }

    /**
     * Applies {@code value} as the active binding for the duration of
     * {@code action}, restoring any prior state when the action finishes.
     *
     * @param value  the value to bind; may be {@code null}
     * @param action the action to execute with the binding active
     */
    abstract void bind(Object value, Runnable action);

    /**
     * Rethrows {@code t} without wrapping it, preserving the original runtime
     * type while satisfying the compiler's checked-exception rules.
     *
     * @param <E> the inferred exception type
     * @param t   the throwable to rethrow
     * @throws E always, as the original throwable
     */
    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneakyThrow(Throwable t) throws E {
        throw (E) t;
    }

    /**
     * An immutable set of scoped-local bindings that can be applied
     * atomically for the duration of a {@link Runnable} or {@link Supplier}.
     *
     * @since 6.0.0
     */
    public static final class Carrier {
        /** Scoped-local key represented by this carrier node. */
        private final ScopedLocal<?> key;

        /** Value bound to {@link #key} for this carrier node; may be {@code null}. */
        private final Object value;

        /** Previous carrier node, allowing multiple bindings to be chained immutably. */
        private final Carrier prev;

        /**
         * Creates a carrier node for one binding in the immutable carrier chain.
         *
         * @param key   the scoped-local key for this node
         * @param value the value bound for this node; may be {@code null}
         * @param prev  the previously accumulated carrier chain, or {@code null}
         */
        private Carrier(ScopedLocal<?> key, Object value, Carrier prev) {
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
         * @param value the value to bind; may be {@code null}
         * @return a new carrier with the additional binding
         */
        public <T> Carrier where(ScopedLocal<T> key, T value) {
            Objects.requireNonNull(key, "key must not be null");
            return new Carrier(key, value, this);
        }

        /**
         * Executes the action with all bindings in this carrier active.
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
            R value = (R) result[0];
            return value;
        }

        /**
         * Composes the binding stack represented by this carrier around the
         * supplied action and runs the resulting scoped execution.
         *
         * @param action the terminal action to run once all bindings are active
         */
        private void execute(Runnable action) {
            Runnable scopedAction = action;
            for (Carrier carrier = this; carrier != null; carrier = carrier.prev) {
                ScopedLocal<?> scopedLocal = carrier.key;
                Object scopedValue = carrier.value;
                Runnable next = scopedAction;
                scopedAction = () -> scopedLocal.bind(scopedValue, next);
            }
            scopedAction.run();
        }
    }

    /**
     * {@link ThreadLocal}-backed implementation used when the platform does not
     * provide {@code ScopedValue}.
     */
    private static final class ThreadLocalImpl<T> extends ScopedLocal<T> {
        /** Sentinel representing the absence of both an explicit binding and a cached initial value. */
        private static final Object UNSET = new Object();

        /** Sentinel that lets the delegate distinguish a bound {@code null} from {@link #UNSET}. */
        private static final Object NULL_SENTINEL = new Object();

        /** Per-thread storage for the current binding or cached initial value. */
        private final ThreadLocal<Object> delegate = ThreadLocal.withInitial(() -> UNSET);

        /** Optional supplier used to lazily initialize a per-thread default value. */
        private final Supplier<? extends T> initialSupplier;

        /** Creates an instance without a default supplier. */
        private ThreadLocalImpl() {
            this.initialSupplier = null;
        }

        /**
         * Creates an instance whose unbound reads are initialized from
         * {@code initialSupplier} once per thread.
         *
         * @param initialSupplier supplies the per-thread default value
         */
        private ThreadLocalImpl(Supplier<? extends T> initialSupplier) {
            this.initialSupplier = initialSupplier;
        }

        /** {@inheritDoc} */
        @Override
        public T get() {
            Object value = delegate.get();
            if (value != UNSET) {
                return decode(value);
            }
            if (initialSupplier != null) {
                return initializeAndCache();
            }
            throw new NoSuchElementException(UNBOUND_MESSAGE);
        }

        /** {@inheritDoc} */
        @Override
        public T orElse(T other) {
            Object value = delegate.get();
            if (value != UNSET) {
                return decode(value);
            }
            if (initialSupplier != null) {
                return initializeAndCache();
            }
            return other;
        }

        /** {@inheritDoc} */
        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            Objects.requireNonNull(exceptionSupplier, "exceptionSupplier must not be null");
            Object value = delegate.get();
            if (value != UNSET) {
                return decode(value);
            }
            if (initialSupplier != null) {
                return initializeAndCache();
            }
            throw exceptionSupplier.get();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isBound() {
            return delegate.get() != UNSET || initialSupplier != null;
        }

        /** {@inheritDoc} */
        @Override
        void bind(Object value, Runnable action) {
            Object previous = delegate.get();
            delegate.set(encode(value));
            try {
                action.run();
            } finally {
                if (previous == UNSET) {
                    delegate.remove();
                } else {
                    delegate.set(previous);
                }
            }
        }

        /**
         * Computes, stores, and returns this thread's lazily initialized value.
         *
         * @return the initialized value, which may be {@code null}
         */
        private T initializeAndCache() {
            T initialValue = initialSupplier.get();
            delegate.set(encode(initialValue));
            return initialValue;
        }

        /**
         * Decodes an internal sentinel-backed representation into the user value.
         *
         * @param value the stored value
         * @return the decoded user value, possibly {@code null}
         */
        @SuppressWarnings("unchecked")
        private T decode(Object value) {
            return value == NULL_SENTINEL ? null : (T) value;
        }

        /**
         * Encodes {@code null} values using a sentinel so they can be stored in
         * the {@link ThreadLocal} without being confused with an uninitialized slot.
         *
         * @param value the user value, possibly {@code null}
         * @return the encoded storage form
         */
        private Object encode(Object value) {
            return value == null ? NULL_SENTINEL : value;
        }
    }

    /**
     * {@code ScopedValue}-backed implementation used on runtimes that expose the
     * JDK 25 API, with an auxiliary {@link ThreadLocal} only for
     * {@link #withInitial(Supplier)} semantics.
     */
    private static final class ScopedValueImpl<T> extends ScopedLocal<T> {
        /** Sentinel that preserves explicit {@code null} bindings across the erased backend API. */
        private static final Object NULL_SENTINEL = new Object();

        /** Backend-specific {@code ScopedValue} instance, stored as {@code Object} for cross-JDK compatibility. */
        private final Object scopedValue;

        /** Optional per-thread fallback used only to cache values from {@link #withInitial(Supplier)}. */
        private final ThreadLocal<T> fallback;

        /** Creates an instance without a default supplier. */
        private ScopedValueImpl() {
            this.scopedValue = newScopedValue();
            this.fallback = null;
        }

        /**
         * Creates an instance with lazily initialized per-thread fallback values
         * for unbound reads.
         *
         * @param initialSupplier supplies the fallback value for each thread
         */
        private ScopedValueImpl(Supplier<? extends T> initialSupplier) {
            this.scopedValue = newScopedValue();
            this.fallback = ThreadLocal.withInitial(initialSupplier::get);
        }

        /** {@inheritDoc} */
        @Override
        public T get() {
            if (isScopedBindingPresent()) {
                return decode(readScopedValue());
            }
            if (fallback != null) {
                return fallback.get();
            }
            throw new NoSuchElementException(UNBOUND_MESSAGE);
        }

        /** {@inheritDoc} */
        @Override
        public T orElse(T other) {
            if (isScopedBindingPresent()) {
                return decode(readScopedValue());
            }
            if (fallback != null) {
                return fallback.get();
            }
            return other;
        }

        /** {@inheritDoc} */
        @Override
        public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
            Objects.requireNonNull(exceptionSupplier, "exceptionSupplier must not be null");
            if (isScopedBindingPresent()) {
                return decode(readScopedValue());
            }
            if (fallback != null) {
                return fallback.get();
            }
            throw exceptionSupplier.get();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isBound() {
            return isScopedBindingPresent() || fallback != null;
        }

        /** {@inheritDoc} */
        @Override
        void bind(Object value, Runnable action) {
            try {
                Object carrier = SV_WHERE.invokeExact(scopedValue, encode(value));
                CARRIER_RUN.invokeExact(carrier, action);
            } catch (Throwable t) {
                sneakyThrow(t);
            }
        }

        /**
         * Returns whether the underlying {@code ScopedValue} currently has an
         * explicit binding on this thread.
         *
         * @return {@code true} if a scoped binding is active
         */
        private boolean isScopedBindingPresent() {
            try {
                return (boolean) SV_IS_BOUND.invokeExact(scopedValue);
            } catch (Throwable t) {
                sneakyThrow(t);
                return false;
            }
        }

        /**
         * Reads the raw value from the underlying {@code ScopedValue}.
         *
         * @return the stored raw value, possibly the internal null sentinel
         */
        private Object readScopedValue() {
            try {
                return SV_GET.invokeExact(scopedValue);
            } catch (Throwable t) {
                sneakyThrow(t);
                return null;
            }
        }

        /**
         * Decodes the erased backend value into the user-visible value.
         *
         * @param value the raw backend value
         * @return the decoded value, possibly {@code null}
         */
        @SuppressWarnings("unchecked")
        private T decode(Object value) {
            return value == NULL_SENTINEL ? null : (T) value;
        }

        /**
         * Encodes a user value for storage in the erased backend API.
         *
         * @param value the user value, possibly {@code null}
         * @return the encoded backend value
         */
        private Object encode(Object value) {
            return value == null ? NULL_SENTINEL : value;
        }

        /**
         * Creates a new backend {@code ScopedValue} instance through the adapted
         * method handle.
         *
         * @return the newly created backend object
         */
        private static Object newScopedValue() {
            try {
                return SV_NEW_INSTANCE.invokeExact();
            } catch (Throwable t) {
                throw new IllegalStateException("Unable to initialize ScopedValue backend", t);
            }
        }
    }

    /**
     * Holder for optional method-handle bindings to the JDK {@code ScopedValue}
     * API, allowing this class to link lazily and remain binary-compatible with
     * earlier Java versions.
     */
    private static final class ScopedValueBindings {
        /** Whether all required {@code ScopedValue} entry points were discovered successfully. */
        private final boolean available;

        /** Adapted handle for {@code ScopedValue.newInstance()}. */
        private final MethodHandle newInstance;

        /** Adapted handle for {@code ScopedValue.where(ScopedValue, Object)}. */
        private final MethodHandle where;

        /** Adapted handle for {@code ScopedValue.get()}. */
        private final MethodHandle get;

        /** Adapted handle for {@code ScopedValue.isBound()}. */
        private final MethodHandle isBound;

        /** Adapted handle for {@code ScopedValue.Carrier.run(Runnable)}. */
        private final MethodHandle carrierRun;

        /**
         * Creates a binding container for either the detected backend handles or
         * the sentinel unavailable state.
         */
        private ScopedValueBindings(boolean available, MethodHandle newInstance, MethodHandle where,
                                    MethodHandle get, MethodHandle isBound, MethodHandle carrierRun) {
            this.available = available;
            this.newInstance = newInstance;
            this.where = where;
            this.get = get;
            this.isBound = isBound;
            this.carrierRun = carrierRun;
        }

        /**
         * Detects the availability of finalized {@code ScopedValue} support and,
         * when present, prepares adapted method handles for exact invocation.
         *
         * @return the discovered bindings, or an unavailable marker on failure
         */
        private static ScopedValueBindings detect() {
            if (Runtime.version().feature() < 25) {
                return unavailable();
            }

            try {
                Class<?> scopedValueClass = Class.forName("java.lang.ScopedValue");
                Class<?> carrierClass = Class.forName("java.lang.ScopedValue$Carrier");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();

                MethodHandle newInstance = lookup.findStatic(scopedValueClass, "newInstance",
                                MethodType.methodType(scopedValueClass))
                        .asType(MethodType.methodType(Object.class));
                MethodHandle where = lookup.findStatic(scopedValueClass, "where",
                                MethodType.methodType(carrierClass, scopedValueClass, Object.class))
                        .asType(MethodType.methodType(Object.class, Object.class, Object.class));
                MethodHandle get = lookup.findVirtual(scopedValueClass, "get",
                                MethodType.methodType(Object.class))
                        .asType(MethodType.methodType(Object.class, Object.class));
                MethodHandle isBound = lookup.findVirtual(scopedValueClass, "isBound",
                                MethodType.methodType(boolean.class))
                        .asType(MethodType.methodType(boolean.class, Object.class));
                MethodHandle carrierRun = lookup.findVirtual(carrierClass, "run",
                                MethodType.methodType(void.class, Runnable.class))
                        .asType(MethodType.methodType(void.class, Object.class, Runnable.class));

                return new ScopedValueBindings(true, newInstance, where, get, isBound, carrierRun);
            } catch (Throwable t) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE,
                            "ScopedValue not available on JDK " + Runtime.version().feature()
                                    + "; using ThreadLocal fallback",
                            t);
                }
                return unavailable();
            }
        }

        /**
         * Returns a binding container representing the absence of a usable
         * {@code ScopedValue} backend.
         *
         * @return an unavailable binding descriptor
         */
        private static ScopedValueBindings unavailable() {
            return new ScopedValueBindings(false, null, null, null, null, null);
        }

        /**
         * Returns whether the runtime supports the {@code ScopedValue} backend.
         *
         * @return {@code true} when the adapted handles are available
         */
        private boolean isAvailable() {
            return available;
        }

        /**
         * Returns the adapted handle for creating new backend instances.
         *
         * @return the creation handle, or {@code null} when unavailable
         */
        private MethodHandle newInstanceHandle() {
            return newInstance;
        }

        /**
         * Returns the adapted handle for binding a scoped value.
         *
         * @return the binding handle, or {@code null} when unavailable
         */
        private MethodHandle whereHandle() {
            return where;
        }

        /**
         * Returns the adapted handle for reading a scoped value.
         *
         * @return the read handle, or {@code null} when unavailable
         */
        private MethodHandle getHandle() {
            return get;
        }

        /**
         * Returns the adapted handle for checking whether a scoped value is bound.
         *
         * @return the bound-check handle, or {@code null} when unavailable
         */
        private MethodHandle isBoundHandle() {
            return isBound;
        }

        /**
         * Returns the adapted handle for running a backend carrier.
         *
         * @return the carrier-run handle, or {@code null} when unavailable
         */
        private MethodHandle carrierRunHandle() {
            return carrierRun;
        }
    }
}
