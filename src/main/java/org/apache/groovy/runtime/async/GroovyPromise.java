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

// AsyncContext support reserved for future enhancement
import groovy.concurrent.Awaitable;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Default {@link Awaitable} implementation backed by a {@link CompletableFuture}.
 * <p>
 * This is the concrete type returned by {@code async} methods.  It delegates
 * all operations to an underlying {@code CompletableFuture} while keeping the
 * public API limited to the {@code Awaitable} contract, thereby decoupling
 * user code from JDK-specific async APIs.
 * <p>
 * This class is an internal implementation detail and should not be referenced
 * directly by user code. Use the {@link Awaitable} interface instead.
 *
 * @param <T> the result type
 * @see Awaitable
 * @since 6.0.0
 */
public class GroovyPromise<T> implements Awaitable<T> {

    private final CompletableFuture<T> future;

    /**
     * Creates a new {@code GroovyPromise} wrapping the given {@link CompletableFuture}.
     *
     * @param future the backing future; must not be {@code null}
     * @throws NullPointerException if {@code future} is {@code null}
     */
    public GroovyPromise(CompletableFuture<T> future) {
        this.future = Objects.requireNonNull(future, "future must not be null");
    }

    /**
     * Creates a {@code GroovyPromise} wrapping the given {@link CompletableFuture}.
     * <p>
     * This is a convenience factory that delegates to
     * {@link #GroovyPromise(CompletableFuture)}.
     *
     * @param future the backing future; must not be {@code null}
     * @param <T>    the result type
     * @return a new {@code GroovyPromise} wrapping {@code future}
     * @throws NullPointerException if {@code future} is {@code null}
     */
    public static <T> GroovyPromise<T> of(CompletableFuture<T> future) {
        return new GroovyPromise<>(future);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Includes a synchronous completion fast-path: if the underlying
     * {@link CompletableFuture} is already done, the result is extracted
     * via {@link CompletableFuture#join()} which avoids the full
     * park/unpark machinery of {@link CompletableFuture#get()}.
     * This optimisation provides a synchronous completion fast-path
     * and eliminates unnecessary thread state transitions
     * on the hot path where async operations complete before being awaited.
     * <p>
     * If the future was cancelled, the original {@link CancellationException} is
     * unwrapped from the JDK 23+ wrapper for cross-version consistency.
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        // Fast path: already completed — skip wait queue and thread parking
        if (future.isDone()) {
            return getCompleted();
        }
        try {
            return future.get();
        } catch (CancellationException e) {
            throw unwrapCancellation(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Includes a synchronous completion fast-path for already-done futures,
     * consistent with the zero-argument {@link #get()} overload.
     * Unwraps JDK 23+ {@link CancellationException} wrappers for consistency.
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // Fast path: already completed — skip wait queue and thread parking
        if (future.isDone()) {
            return getCompleted();
        }
        try {
            return future.get(timeout, unit);
        } catch (CancellationException e) {
            throw unwrapCancellation(e);
        }
    }

    /**
     * Extracts the result from an already-completed future using
     * {@link CompletableFuture#join()}, which is cheaper than
     * {@link CompletableFuture#get()} for completed futures because it
     * bypasses the interruptible wait path.
     * <p>
     * Translates {@link CompletionException} to {@link ExecutionException}
     * to preserve the {@code get()} contract.
     */
    private T getCompleted() throws ExecutionException {
        try {
            return future.join();
        } catch (CompletionException e) {
            throw new ExecutionException(AsyncSupport.unwrap(e));
        } catch (CancellationException e) {
            throw unwrapCancellation(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDone() {
        return future.isDone();
    }

    /**
     * Attempts to cancel this computation. Delegates to
     * {@link CompletableFuture#cancel(boolean) CompletableFuture.cancel(true)}.
     * <p>
     * <b>Note:</b> {@code CompletableFuture} cancellation sets the future's state
     * to cancelled but does <em>not</em> reliably interrupt the underlying thread.
     * Async work already in progress may continue running in the background.
     * For cooperative cancellation, check {@link Thread#isInterrupted()} in
     * long-running async bodies.
     *
     * @return {@code true} if the future was successfully cancelled
     */
    @Override
    public boolean cancel() {
        return future.cancel(true);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCompletedExceptionally() {
        return future.isCompletedExceptionally();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new {@code GroovyPromise} whose result is obtained by applying
     * the given function to this promise's result.  The current
     * {@link AsyncContext} snapshot is captured when the continuation is
     * registered and restored when it executes.
     */
    @Override
    public <U> Awaitable<U> then(Function<? super T, ? extends U> fn) {
        return new GroovyPromise<>(future.thenApply(fn));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new {@code GroovyPromise} that is the result of composing this
     * promise with the async function, enabling flat-mapping of awaitables.
     * The current {@link AsyncContext} snapshot is captured when the
     * continuation is registered and restored when it executes.
     */
    @Override
    public <U> Awaitable<U> thenCompose(Function<? super T, ? extends Awaitable<U>> fn) {
        return new GroovyPromise<>(future.thenCompose(value ->
                fn.apply(value).toCompletableFuture()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a new {@code GroovyPromise} that handles exceptions thrown by this promise.
     * The throwable passed to the handler is deeply unwrapped to strip JDK
     * wrapper layers ({@code CompletionException}, {@code ExecutionException}).
     * The handler runs with the {@link AsyncContext} snapshot that was active
     * when the recovery continuation was registered.
     */
    @Override
    public Awaitable<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return new GroovyPromise<>(future.exceptionally(t -> fn.apply(AsyncSupport.unwrap(t))));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the underlying {@link CompletableFuture} for interop with JDK APIs.
     */
    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

    /**
     * JDK 23+ wraps a stored {@link CancellationException} in a new instance
     * with the generic message {@code "get"} when {@link CompletableFuture#get()}
     * is called.  Unwrap it here so Groovy users consistently observe the
     * original cancellation message and cause chain across all supported JDKs.
     */
    private static CancellationException unwrapCancellation(CancellationException exception) {
        Throwable cause = exception.getCause();
        return cause instanceof CancellationException ce ? ce : exception;
    }

    /**
     * Returns a human-readable representation showing the promise state:
     * {@code GroovyPromise{pending}}, {@code GroovyPromise{completed}}, or
     * {@code GroovyPromise{failed}}.
     */
    @Override
    public String toString() {
        if (future.isDone()) {
            if (future.isCompletedExceptionally()) {
                return "GroovyPromise{failed}";
            }
            return "GroovyPromise{completed}";
        }
        return "GroovyPromise{pending}";
    }
}
