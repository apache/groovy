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

import groovy.concurrent.AsyncContext;
import groovy.concurrent.AsyncScope;
import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

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
 *
 * <h2>Concurrency design</h2>
 * <p>A dedicated {@code lock} object guards the child task list and the
 * {@code closed} flag jointly.  This ensures that {@link #async(Closure)}
 * and {@link #close()} cannot race: no child can be registered after the
 * scope is marked closed.</p>
 *
 * <h3>Register-before-submit protocol</h3>
 * <p>Child {@link CompletableFuture} instances are created and registered in
 * the {@code children} list under the lock <em>before</em> the task body is
 * submitted to the executor.  This eliminates a race window where
 * {@link #close()} could snapshot the child list between task submission
 * and registration, causing a submitted task to run unsupervised.  The
 * protocol guarantees that every child is always joined (or cancelled) by
 * {@link #close()}, upholding the structured concurrency invariant.</p>
 *
 * <h2>Memory management</h2>
 * <p>Completed children are pruned from the internal list when its size
 * exceeds {@link #PRUNE_THRESHOLD}, preventing unbounded memory growth
 * in long-lived scopes.</p>
 *
 * <p>This class is an internal implementation detail of the Groovy async
 * runtime.  User code should program against the {@link AsyncScope}
 * interface and use its static factory methods.</p>
 *
 * @see AsyncScope
 * @since 6.0.0
 */
public final class DefaultAsyncScope implements AsyncScope {

    private static final ScopedLocal<AsyncScope> CURRENT = ScopedLocal.newInstance();

    /**
     * Pruning threshold: completed children are purged when the list
     * exceeds this size, keeping memory bounded for long-lived scopes.
     */
    private static final int PRUNE_THRESHOLD = 64;

    private final Object lock = new Object();
    private final List<CompletableFuture<?>> children = new ArrayList<>();
    private boolean closed;
    private final Executor executor;
    private final boolean failFast;

    /**
     * Creates a new scope with the given executor and fail-fast policy.
     *
     * @param executor the executor for child tasks; must not be {@code null}
     * @param failFast if {@code true}, cancel all siblings when any
     *                 child fails
     * @throws NullPointerException if {@code executor} is {@code null}
     */
    public DefaultAsyncScope(Executor executor, boolean failFast) {
        Objects.requireNonNull(executor, "executor must not be null");
        this.executor = executor;
        this.failFast = failFast;
    }

    /**
     * Creates a new scope with the given executor and fail-fast enabled.
     *
     * @param executor the executor for child tasks; must not be {@code null}
     * @throws NullPointerException if {@code executor} is {@code null}
     */
    public DefaultAsyncScope(Executor executor) {
        this(executor, true);
    }

    /**
     * Creates a new scope with the default async executor and fail-fast
     * enabled.
     */
    public DefaultAsyncScope() {
        this(AsyncSupport.getExecutor(), true);
    }

    // ---- Static operations (delegated from AsyncScope interface) --------

    /**
     * Returns the structured async scope currently bound to this thread,
     * or {@code null} if no scope is active.
     *
     * @return the current scope, or {@code null}
     */
    public static AsyncScope current() {
        return CURRENT.orElse(null);
    }

    /**
     * Executes the supplier with the given scope installed as the current
     * structured scope, restoring the previous binding afterwards.
     *
     * @param scope    the scope to install; may be {@code null}
     * @param supplier the action to execute; must not be {@code null}
     * @param <T>      the result type
     * @return the supplier's result
     */
    public static <T> T withCurrent(AsyncScope scope, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return CURRENT.where(scope, supplier);
    }

    /**
     * Void overload of {@link #withCurrent(AsyncScope, Supplier)}.
     *
     * @param scope  the scope to install; may be {@code null}
     * @param action the action to execute; must not be {@code null}
     */
    public static void withCurrent(AsyncScope scope, Runnable action) {
        withCurrent(scope, () -> {
            action.run();
            return null;
        });
    }

    // ---- Instance methods (AsyncScope interface) ------------------------

    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> async(Closure<T> body) {
        Objects.requireNonNull(body, "body must not be null");
        return launchChild(body::call);
    }

    @Override
    public <T> Awaitable<T> async(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return launchChild(supplier::get);
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
                Throwable cause = AsyncSupport.deepUnwrap(e);
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
     * Core child-launch logic shared by both {@link #async(Closure)} and
     * {@link #async(Supplier)}.
     *
     * <h3>Register-before-submit protocol</h3>
     * <p>The {@link CompletableFuture} is created and registered in
     * {@code children} <em>before</em> the task is submitted to the
     * executor.  This eliminates a race window that would otherwise
     * exist if the task were submitted first: {@link #close()} could
     * snapshot the children list between task submission and
     * registration, causing the submitted task to run unsupervised —
     * violating the structured concurrency guarantee that all child
     * tasks complete (or are cancelled) before the scope exits.</p>
     *
     * <h3>Fail-fast callback</h3>
     * <p>The fail-fast {@code whenComplete} callback is registered inside
     * the {@code lock} so that no concurrent {@code async()} call can add
     * a child between the callback's {@code cancelAllLocked()} sweep and
     * its own lock release.  Java's reentrant {@code synchronized} ensures
     * this is safe even when the callback fires synchronously (i.e., the
     * future has already completed when {@code whenComplete} is called).</p>
     */
    private <T> Awaitable<T> launchChild(Supplier<T> task) {
        AsyncContext.Snapshot contextSnapshot = AsyncContext.capture();

        // 1. Create the CF first — not yet wired to any computation.
        CompletableFuture<T> cf = new CompletableFuture<>();

        // 2. Register under lock BEFORE submitting work to the executor.
        //    This guarantees close() will always join this CF.
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

        // 3. Submit the task and wire its outcome to the pre-registered CF.
        executor.execute(() -> {
            try {
                T result = withCurrent(this, () ->
                        AsyncContext.withSnapshot(contextSnapshot, task));
                cf.complete(result);
            } catch (Throwable t) {
                cf.completeExceptionally(t);
            }
        });

        return GroovyPromise.of(cf);
    }

    private void cancelAllLocked() {
        for (CompletableFuture<?> child : children) {
            child.cancel(true);
        }
    }

    /**
     * Removes completed futures to prevent unbounded memory growth.
     * Must be called while holding {@code lock}.
     */
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
