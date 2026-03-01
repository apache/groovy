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

import groovy.concurrent.Awaitable;

import java.util.Objects;
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

    public GroovyPromise(CompletableFuture<T> future) {
        this.future = Objects.requireNonNull(future, "future must not be null");
    }

    /**
     * Creates a {@code GroovyPromise} wrapping the given {@link CompletableFuture}.
     */
    public static <T> GroovyPromise<T> of(CompletableFuture<T> future) {
        return new GroovyPromise<>(future);
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

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

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return future.isCompletedExceptionally();
    }

    @Override
    public <U> Awaitable<U> then(Function<? super T, ? extends U> fn) {
        return new GroovyPromise<>(future.thenApply(fn));
    }

    @Override
    public <U> Awaitable<U> thenCompose(Function<? super T, ? extends Awaitable<U>> fn) {
        return new GroovyPromise<>(future.thenCompose(t -> fn.apply(t).toCompletableFuture()));
    }

    @Override
    public Awaitable<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return new GroovyPromise<>(future.exceptionally(t -> {
            // Unwrap all wrapper layers so handler sees the original exception
            Throwable cause = t;
            while (cause.getCause() != null
                    && (cause instanceof CompletionException
                        || cause instanceof ExecutionException
                        || cause instanceof java.lang.reflect.UndeclaredThrowableException
                        || cause instanceof java.lang.reflect.InvocationTargetException)) {
                cause = cause.getCause();
            }
            return fn.apply(cause);
        }));
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

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
