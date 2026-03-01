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
package groovy.concurrent;

import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Core abstraction for asynchronous computations in Groovy, decoupled from
 * any specific JDK or third-party async API. Analogous to C#'s {@code Task<T>},
 * JavaScript's {@code Promise<T>}.
 * <p>
 * {@code Awaitable} is the return type of {@code async} methods and the input
 * type for the {@code await} expression. Third-party frameworks (RxJava, Reactor,
 * etc.) can integrate by registering an {@link AwaitableAdapter} via
 * {@link AwaitableAdapterRegistry}.
 * <p>
 * The default implementation, {@link org.apache.groovy.runtime.async.GroovyPromise GroovyPromise}, delegates to
 * {@link CompletableFuture} but users never need to depend on that detail.
 *
 * @param <T> the result type
 * @see org.apache.groovy.runtime.async.GroovyPromise
 * @see AwaitableAdapter
 * @since 6.0.0
 */
public interface Awaitable<T> {

    /**
     * Blocks until the computation completes and returns the result.
     */
    T get() throws InterruptedException, ExecutionException;

    /**
     * Blocks until the computation completes or the timeout expires.
     */
    T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Returns {@code true} if the computation has completed (normally or exceptionally).
     */
    boolean isDone();

    /**
     * Attempts to cancel the computation. If the computation has not yet started
     * or is still running, it will be cancelled with a {@link CancellationException}.
     * Analogous to C#'s {@code CancellationToken}.
     *
     * @return {@code true} if the computation was successfully cancelled
     */
    boolean cancel();

    /**
     * Returns {@code true} if the computation was cancelled before completing normally.
     */
    boolean isCancelled();

    /**
     * Returns {@code true} if this computation completed exceptionally
     * (including cancellation).
     */
    boolean isCompletedExceptionally();

    /**
     * Returns a new {@code Awaitable} whose result is obtained by applying the
     * given function to this awaitable's result when it completes.
     */
    <U> Awaitable<U> then(Function<? super T, ? extends U> fn);

    /**
     * Returns a new {@code Awaitable} produced by applying the given function
     * to this awaitable's result, flattening the nested {@code Awaitable}.
     */
    <U> Awaitable<U> thenCompose(Function<? super T, ? extends Awaitable<U>> fn);

    /**
     * Returns a new {@code Awaitable} that, if this one completes exceptionally,
     * applies the given function to the exception to produce a recovery value.
     */
    Awaitable<T> exceptionally(Function<Throwable, ? extends T> fn);

    /**
     * Converts this {@code Awaitable} to a JDK {@link CompletableFuture}
     * for interoperability with APIs that require it.
     */
    CompletableFuture<T> toCompletableFuture();

    // ---- Static factories ----

    /**
     * Returns an already-completed {@code Awaitable} with the given value.
     */
    static <T> Awaitable<T> of(T value) {
        return new GroovyPromise<>(CompletableFuture.completedFuture(value));
    }

    /**
     * Returns an already-failed {@code Awaitable} with the given exception.
     */
    static <T> Awaitable<T> failed(Throwable error) {
        return new GroovyPromise<>(CompletableFuture.failedFuture(error));
    }
}
