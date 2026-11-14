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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Default implementation of {@link AsyncScope} providing structured
 * concurrency with configurable failure policy.
 * <p>
 * A dedicated lock guards the child task list and the closed flag jointly,
 * ensuring that {@link #async(Supplier)} and {@link #close()} cannot race.
 * Child futures are registered under the lock <em>before</em> task submission
 * (register-before-submit protocol), guaranteeing every child is joined or
 * cancelled by {@link #close()}.
 *
 * @see AsyncScope
 * @since 6.0.0
 */
public final class DefaultAsyncScope implements AsyncScope {

    private static final ScopedLocal<AsyncScope> CURRENT_SCOPE = ScopedLocal.newInstance();

    private static final int PRUNE_THRESHOLD = 64;

    private static final int TIMER_PENDING = 0;
    private static final int TIMER_STOPPED = 1;
    private static final int TIMER_FIRING = 2;
    private static final int TIMER_FIRED = 3;

    private final Object lock = new Object();
    private final List<CompletableFuture<?>> children = new ArrayList<>();
    private final List<DefaultAsyncScope> childScopes = new ArrayList<>();
    private boolean closed;
    private final Executor executor;
    private final boolean failFast;
    private final AsyncScope parent;

    public DefaultAsyncScope(Executor executor, boolean failFast) {
        Objects.requireNonNull(executor, "executor must not be null");
        this.executor = executor;
        this.failFast = failFast;
        this.parent = current();
        if (parent instanceof DefaultAsyncScope parentScope) {
            parentScope.registerChildScope(this);
        }
    }

    public DefaultAsyncScope(Executor executor) {
        this(executor, true);
    }

    public DefaultAsyncScope() {
        this(AsyncSupport.getExecutor(), true);
    }

    // ---- Static operations ----------------------------------------------

    /**
     * Returns the current scope, or {@code null} when no scope is bound.
     */
    public static AsyncScope current() {
        return CURRENT_SCOPE.orElse(null);
    }

    /**
     * Executes the supplier with the given scope as current,
     * restoring the previous binding afterwards.
     */
    public static <T> T withCurrent(AsyncScope scope, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return CURRENT_SCOPE.where(scope, supplier);
    }

    // ---- Instance methods -----------------------------------------------

    @Override
    public AsyncScope getParent() {
        return parent;
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
        List<CompletableFuture<?>> taskSnapshot;
        List<DefaultAsyncScope> scopeSnapshot;
        synchronized (lock) {
            taskSnapshot = new ArrayList<>(children);
            scopeSnapshot = new ArrayList<>(childScopes);
        }
        for (CompletableFuture<?> child : taskSnapshot) {
            child.cancel(true);
        }
        for (DefaultAsyncScope child : scopeSnapshot) {
            child.cancelAll();
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
                    AsyncSupport.addSuppressedOnce(firstError, cause);
                }
            } catch (Exception e) {
                if (firstError == null) {
                    firstError = e;
                } else {
                    AsyncSupport.addSuppressedOnce(firstError, e);
                }
            }
        }
        // Deregister from parent
        if (parent instanceof DefaultAsyncScope parentScope) {
            parentScope.deregisterChildScope(this);
        }
        if (firstError != null) {
            throw AsyncSupport.rethrow(firstError);
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

    // ---- Child scope management -----------------------------------------

    void registerChildScope(DefaultAsyncScope child) {
        synchronized (lock) {
            if (!closed) {
                childScopes.add(child);
            }
        }
    }

    void deregisterChildScope(DefaultAsyncScope child) {
        synchronized (lock) {
            childScopes.remove(child);
        }
    }

    // ---- Timeout support ------------------------------------------------

    /**
     * Creates a scope with a timeout. If the body does not complete within
     * the duration, all children are cancelled and {@link TimeoutException}
     * is thrown.
     */
    public static <T> T withScopeTimeout(Executor executor, Duration timeout,
                                          Function<AsyncScope, T> body) throws TimeoutException {
        Objects.requireNonNull(timeout, "timeout must not be null");
        Objects.requireNonNull(body, "body must not be null");
        var timerState = new AtomicInteger(TIMER_PENDING);
        Thread bodyThread = Thread.currentThread();
        DefaultAsyncScope scope = new DefaultAsyncScope(executor);
        ScheduledFuture<?> timer = null;
        T result;
        // The scope is closed by hand, as in AsyncScope.withScope: try-with-resources
        // would add a failure shared by the body and a child to its own suppressed list
        try {
            timer = AsyncSupport.getScheduler().schedule(() -> {
                if (timerState.compareAndSet(TIMER_PENDING, TIMER_FIRING)) {
                    try {
                        scope.cancelAll();
                    } finally {
                        bodyThread.interrupt();
                        timerState.set(TIMER_FIRED);
                    }
                }
            }, timeout.toMillis(), TimeUnit.MILLISECONDS);
            result = withCurrent(scope, () -> body.apply(scope));
        } catch (Throwable thrown) {
            // A checked exception from a Closure body arrives wrapped by its SAM coercion
            Throwable bodyError = AsyncSupport.unwrapUndeclared(thrown);
            if (stopTimer(timer, timerState) && !(bodyError instanceof Error)) {
                TimeoutException te = new TimeoutException("Scope timed out after " + timeout);
                te.initCause(bodyError);
                bodyError = te;
            }
            try {
                scope.close();
            } catch (Throwable closeError) {
                AsyncSupport.addSuppressedOnce(bodyError, closeError);
            }
            throw AsyncSupport.rethrow(bodyError);
        }
        stopTimer(timer, timerState);
        scope.close();
        return result;
    }

    /**
     * Stops a scope timer once the body has finished.
     *
     * @return whether the timer fired first
     */
    private static boolean stopTimer(ScheduledFuture<?> timer, AtomicInteger timerState) {
        if (timer != null) {
            timer.cancel(false);
        }
        if (timerState.compareAndSet(TIMER_PENDING, TIMER_STOPPED)) {
            return false;
        }
        // Wait for the timer's interrupt, then clear it, so it cannot reach the caller later
        while (timerState.get() != TIMER_FIRED) {
            Thread.onSpinWait();
        }
        Thread.interrupted();
        return true;
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
