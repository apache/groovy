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

import groovy.concurrent.AsyncScope;
import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * Default implementation of {@link AsyncScope} providing structured
 * concurrency with configurable failure policy.
 * <p>
 * A dedicated lock guards the child task list and the closed flag jointly,
 * ensuring that {@link #async(Closure)} and {@link #close()} cannot race.
 * Child futures are registered under the lock <em>before</em> task submission
 * (register-before-submit protocol), guaranteeing every child is joined or
 * cancelled by {@link #close()}.
 *
 * @see AsyncScope
 * @since 6.0.0
 */
public final class DefaultAsyncScope implements AsyncScope {

    // ---- Scope-tracking: ScopedValue on JDK 25+, ThreadLocal fallback ---

    private static final boolean SCOPED_VALUE_AVAILABLE;
    private static final MethodHandle SV_WHERE;    // ScopedValue.where(ScopedValue, Object)
    private static final MethodHandle SV_GET;      // ScopedValue.get()
    private static final MethodHandle SV_IS_BOUND; // ScopedValue.isBound()
    private static final MethodHandle CARRIER_CALL; // Carrier.call(Callable)
    private static final Object SCOPED_VALUE;      // ScopedValue<AsyncScope> instance

    private static final ThreadLocal<AsyncScope> CURRENT_TL = new ThreadLocal<>();

    static {
        boolean available = false;
        MethodHandle svWhere = null, svGet = null, svIsBound = null, carrierCall = null;
        Object sv = null;
        try {
            Class<?> svClass = Class.forName("java.lang.ScopedValue");
            Class<?> carrierClass = Class.forName("java.lang.ScopedValue$Carrier");
            // ScopedValue.newInstance()
            MethodHandle newInstance = MethodHandles.lookup().findStatic(
                    svClass, "newInstance", MethodType.methodType(svClass));
            sv = newInstance.invoke();
            // ScopedValue.where(ScopedValue, Object) -> Carrier
            svWhere = MethodHandles.lookup().findStatic(svClass, "where",
                    MethodType.methodType(carrierClass, svClass, Object.class));
            // ScopedValue.get()
            svGet = MethodHandles.lookup().findVirtual(svClass, "get",
                    MethodType.methodType(Object.class));
            // ScopedValue.isBound()
            svIsBound = MethodHandles.lookup().findVirtual(svClass, "isBound",
                    MethodType.methodType(boolean.class));
            // Carrier.call(Callable) -> Object
            carrierCall = MethodHandles.lookup().findVirtual(carrierClass, "call",
                    MethodType.methodType(Object.class, java.util.concurrent.Callable.class));
            available = true;
        } catch (Throwable ignored) {
            // JDK < 25 — ScopedValue not available
        }
        SCOPED_VALUE_AVAILABLE = available;
        SV_WHERE = svWhere;
        SV_GET = svGet;
        SV_IS_BOUND = svIsBound;
        CARRIER_CALL = carrierCall;
        SCOPED_VALUE = sv;
    }

    private static final int PRUNE_THRESHOLD = 64;

    private final Object lock = new Object();
    private final List<CompletableFuture<?>> children = new ArrayList<>();
    private boolean closed;
    private final Executor executor;
    private final boolean failFast;

    public DefaultAsyncScope(Executor executor, boolean failFast) {
        Objects.requireNonNull(executor, "executor must not be null");
        this.executor = executor;
        this.failFast = failFast;
    }

    public DefaultAsyncScope(Executor executor) {
        this(executor, true);
    }

    public DefaultAsyncScope() {
        this(AsyncSupport.getExecutor(), true);
    }

    // ---- Static operations ----------------------------------------------

    /**
     * Returns the current scope. Uses {@code ScopedValue} on JDK 25+,
     * {@code ThreadLocal} on earlier JDKs.
     */
    public static AsyncScope current() {
        if (SCOPED_VALUE_AVAILABLE) {
            try {
                if ((boolean) SV_IS_BOUND.invoke(SCOPED_VALUE)) {
                    return (AsyncScope) SV_GET.invoke(SCOPED_VALUE);
                }
                return null;
            } catch (Throwable e) {
                return null;
            }
        }
        return CURRENT_TL.get();
    }

    /**
     * Executes the supplier with the given scope as current.
     * Uses {@code ScopedValue.where().call()} on JDK 25+ for
     * optimal virtual thread performance; falls back to
     * {@code ThreadLocal} set/restore on earlier JDKs.
     */
    @SuppressWarnings("unchecked")
    public static <T> T withCurrent(AsyncScope scope, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        if (SCOPED_VALUE_AVAILABLE) {
            try {
                Object carrier = SV_WHERE.invoke(SCOPED_VALUE, scope);
                return (T) CARRIER_CALL.invoke(carrier, (java.util.concurrent.Callable<T>) supplier::get);
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        // ThreadLocal fallback for JDK 17-24
        AsyncScope previous = CURRENT_TL.get();
        CURRENT_TL.set(scope);
        try {
            return supplier.get();
        } finally {
            if (previous == null) {
                CURRENT_TL.remove();
            } else {
                CURRENT_TL.set(previous);
            }
        }
    }

    // ---- Instance methods -----------------------------------------------

    @Override
    public <T> Awaitable<T> async(Closure<T> body) {
        Objects.requireNonNull(body, "body must not be null");
        return launchChild(body::call);
    }

    @Override
    public <T> Awaitable<T> async(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return launchChild(supplier);
    }

    @Override
    public int getChildCount() {
        synchronized (lock) {
            return children.size();
        }
    }

    @Override
    public void cancelAll() {
        List<CompletableFuture<?>> snapshot;
        synchronized (lock) {
            snapshot = new ArrayList<>(children);
        }
        for (CompletableFuture<?> child : snapshot) {
            child.cancel(true);
        }
    }

    @Override
    public void close() {
        List<CompletableFuture<?>> snapshot;
        synchronized (lock) {
            if (closed) return;
            closed = true;
            snapshot = new ArrayList<>(children);
        }
        Throwable firstError = null;
        for (CompletableFuture<?> child : snapshot) {
            try {
                child.join();
            } catch (CancellationException ignored) {
                // Cancelled tasks are silently ignored
            } catch (CompletionException e) {
                Throwable cause = AsyncSupport.unwrap(e);
                if (cause instanceof CancellationException) {
                    continue;
                }
                if (firstError == null) {
                    firstError = cause;
                } else {
                    firstError.addSuppressed(cause);
                }
            } catch (Exception e) {
                if (firstError == null) {
                    firstError = e;
                } else {
                    firstError.addSuppressed(e);
                }
            }
        }
        if (firstError != null) {
            if (firstError instanceof RuntimeException re) throw re;
            if (firstError instanceof Error err) throw err;
            throw new RuntimeException(firstError);
        }
    }

    // ---- Internal -------------------------------------------------------

    /**
     * Core child-launch logic using register-before-submit protocol.
     * The CF is created and registered under the lock before work is
     * submitted, guaranteeing close() will always join it.
     */
    private <T> Awaitable<T> launchChild(Supplier<T> task) {
        CompletableFuture<T> cf = new CompletableFuture<>();

        synchronized (lock) {
            if (closed) {
                cf.cancel(true);
                throw new IllegalStateException(
                        "AsyncScope is closed — cannot launch new tasks");
            }
            pruneCompleted();
            children.add(cf);
            if (failFast) {
                cf.whenComplete((v, err) -> {
                    if (err != null) {
                        synchronized (lock) {
                            if (!closed) cancelAllLocked();
                        }
                    }
                });
            }
        }

        try {
            executor.execute(() -> {
                try {
                    T result = withCurrent(this, task);
                    cf.complete(result);
                } catch (Throwable t) {
                    cf.completeExceptionally(t);
                }
            });
        } catch (RuntimeException | Error e) {
            synchronized (lock) {
                children.remove(cf);
            }
            cf.completeExceptionally(e);
            throw e;
        }

        return GroovyPromise.of(cf);
    }

    private void cancelAllLocked() {
        for (CompletableFuture<?> child : children) {
            child.cancel(true);
        }
    }

    private void pruneCompleted() {
        if (children.size() >= PRUNE_THRESHOLD) {
            children.removeIf(CompletableFuture::isDone);
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            return "AsyncScope[children=" + children.size()
                    + ", closed=" + closed
                    + ", failFast=" + failFast + "]";
        }
    }
}
