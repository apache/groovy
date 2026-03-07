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

import org.apache.groovy.runtime.async.AsyncSupport;
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Core abstraction for asynchronous computations in Groovy, analogous to
 * C#'s {@code Task<T>} and JavaScript's {@code Promise<T>}.
 * <p>
 * {@code Awaitable} serves as both an instance type (the result of {@code async}
 * methods and the input to {@code await}) and a static API surface for
 * combinators, factories, and configuration — just as C#'s {@code Task} exposes
 * {@code Task.WhenAll}, {@code Task.WhenAny}, {@code Task.Delay}, and
 * {@code Task.FromResult} as static members.
 * <p>
 * <b>Static combinators</b> (all return {@code Awaitable}, suitable for use
 * with {@code await}):
 * <ul>
 *   <li>{@link #all(Object...) Awaitable.all(a, b, c)} — like
 *       {@code Task.WhenAll()} / {@code Promise.all()}</li>
 *   <li>{@link #any(Object...) Awaitable.any(a, b)} — like
 *       {@code Task.WhenAny()} / {@code Promise.race()}</li>
 *   <li>{@link #allSettled(Object...) Awaitable.allSettled(a, b)} — like
 *       {@code Promise.allSettled()}</li>
 *   <li>{@link #delay(long) Awaitable.delay(ms)} — like
 *       {@code Task.Delay()} / {@code setTimeout}</li>
 *   <li>{@link #orTimeoutMillis(Object, long) Awaitable.orTimeoutMillis(task, ms)} — like
 *       Kotlin's {@code withTimeout} or a JavaScript promise raced against a timer</li>
 *   <li>{@link #completeOnTimeoutMillis(Object, Object, long)
 *       Awaitable.completeOnTimeoutMillis(task, fallback, ms)} —
 *       like a timeout with fallback/default value</li>
 * </ul>
 * <p>
 * <b>Static factories and conversion:</b>
 * <ul>
 *   <li>{@link #from(Object) Awaitable.from(source)} — converts any supported
 *       async type (CompletableFuture, CompletionStage, Future, Flow.Publisher, etc.)
 *       to an {@code Awaitable}</li>
 *   <li>{@link #of(Object) Awaitable.of(value)} — like
 *       {@code Task.FromResult()} / {@code Promise.resolve()}</li>
 *   <li>{@link #failed(Throwable) Awaitable.failed(error)} — like
 *       {@code Task.FromException()} / {@code Promise.reject()}</li>
 * </ul>
 * <p>
 * <b>Instance continuations</b> provide ergonomic composition without exposing
 * raw {@link CompletableFuture} APIs:
 * <ul>
 *   <li>{@link #then(Function)} and {@link #thenCompose(Function)} for success chaining</li>
 *   <li>{@link #thenAccept(Consumer)} for side-effecting continuations</li>
 *   <li>{@link #exceptionally(Function)}, {@link #whenComplete(BiConsumer)},
 *       and {@link #handle(BiFunction)} for failure/completion handling</li>
 *   <li>{@link #orTimeout(long, TimeUnit)} and
 *       {@link #completeOnTimeout(Object, long, TimeUnit)} for deadline composition</li>
 * </ul>
 * <p>
 * Third-party frameworks (RxJava, Reactor, etc.) can integrate by registering
 * an {@link AwaitableAdapter} via {@link AwaitableAdapterRegistry}.
 * <p>
 * The default implementation, {@link GroovyPromise}, delegates to
 * {@link CompletableFuture} but users never need to depend on that detail.
 *
 * @param <T> the result type
 * @see GroovyPromise
 * @see AwaitableAdapter
 * @since 6.0.0
 */
public interface Awaitable<T> {

    /**
     * Blocks until the computation completes and returns the result.
     *
     * @return the computed result
     * @throws InterruptedException if the calling thread is interrupted while waiting
     * @throws ExecutionException if the computation completed exceptionally
     */
    T get() throws InterruptedException, ExecutionException;

    /**
     * Blocks until the computation completes or the timeout expires.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return the computed result
     * @throws InterruptedException if the calling thread is interrupted while waiting
     * @throws ExecutionException if the computation completed exceptionally
     * @throws TimeoutException if the wait timed out
     */
    T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * Returns {@code true} if the computation has completed (normally,
     * exceptionally, or via cancellation).
     *
     * @return {@code true} if complete
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
     *
     * @return {@code true} if cancelled
     */
    boolean isCancelled();

    /**
     * Returns {@code true} if this computation completed exceptionally
     * (including cancellation).
     *
     * @return {@code true} if completed with an error or cancellation
     */
    boolean isCompletedExceptionally();

    /**
     * Returns a new {@code Awaitable} whose result is obtained by applying the
     * given function to this awaitable's result when it completes.
     *
     * @param fn the mapping function
     * @param <U> the type of the mapped result
     * @return a new awaitable holding the mapped result
     */
    <U> Awaitable<U> then(Function<? super T, ? extends U> fn);

    /**
     * Returns a new {@code Awaitable} produced by applying the given async
     * function to this awaitable's result, flattening the nested {@code Awaitable}.
     * This is the monadic {@code flatMap} operation for awaitables.
     *
     * @param fn the async mapping function that returns an {@code Awaitable}
     * @param <U> the type of the inner awaitable's result
     * @return a new awaitable holding the inner result
     */
    <U> Awaitable<U> thenCompose(Function<? super T, ? extends Awaitable<U>> fn);

    /**
     * Returns a new {@code Awaitable} that, when this one completes normally,
     * invokes the given consumer and completes with {@code null}.
     * <p>
     * This is analogous to JavaScript's {@code promise.then(v =&gt; sideEffect(v))}
     * when the result value is no longer needed, and to the consumer-oriented
     * continuations commonly used with C#, Kotlin, and Swift async APIs.
     *
     * @param action the side-effecting consumer to invoke on success
     * @return a new awaitable that completes after the action runs
     * @since 6.0.0
     */
    default Awaitable<Void> thenAccept(Consumer<? super T> action) {
        return GroovyPromise.of(toCompletableFuture().thenAccept(action));
    }

    /**
     * Returns a new {@code Awaitable} that, if this one completes exceptionally,
     * applies the given function to the exception to produce a recovery value.
     * The throwable passed to the function is deeply unwrapped to strip JDK
     * wrapper layers.
     *
     * @param fn the recovery function
     * @return a new awaitable that recovers from failures
     */
    Awaitable<T> exceptionally(Function<Throwable, ? extends T> fn);

    /**
     * Returns a new {@code Awaitable} that invokes the given action when this
     * computation completes, regardless of success or failure.
     * <p>
     * The supplied throwable is transparently unwrapped so handlers see the
     * original failure rather than {@link ExecutionException} /
     * {@link java.util.concurrent.CompletionException} wrappers.
     * This is analogous to JavaScript's {@code Promise.prototype.finally()} and
     * to completion callbacks in C#, Kotlin, and Swift.
     *
     * @param action the completion callback receiving the result or failure
     * @return a new awaitable that completes with the original result
     * @since 6.0.0
     */
    default Awaitable<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return GroovyPromise.of(toCompletableFuture().whenComplete((value, error) ->
                action.accept(value, error == null ? null : AsyncSupport.deepUnwrap(error))));
    }

    /**
     * Returns a new {@code Awaitable} that handles both the successful and the
     * exceptional completion paths in a single continuation.
     * <p>
     * The supplied throwable is transparently unwrapped so the handler sees the
     * original failure.  This mirrors Java's {@code CompletableFuture.handle()},
     * while providing the same “single place for success/failure projection”
     * convenience commonly used in C#, Kotlin, and Swift async code.
     *
     * @param fn the handler receiving either the value or the failure
     * @param <U> the projected result type
     * @return a new awaitable holding the handler's result
     * @since 6.0.0
     */
    default <U> Awaitable<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return GroovyPromise.of(toCompletableFuture().handle((value, error) ->
                fn.apply(value, error == null ? null : AsyncSupport.deepUnwrap(error))));
    }

    /**
     * Returns a new {@code Awaitable} that fails with {@link TimeoutException}
     * if this computation does not complete within the specified milliseconds.
     * <p>
     * Unlike {@link #get(long, TimeUnit)}, this is a non-blocking, composable
     * timeout combinator: it returns another {@code Awaitable} that can itself
     * be awaited, chained, or passed to {@link #all(Object...)} / {@link #any(Object...)}.
     *
     * @param timeoutMillis the timeout duration in milliseconds
     * @return a new awaitable with timeout semantics
     * @since 6.0.0
     */
    default Awaitable<T> orTimeoutMillis(long timeoutMillis) {
        return Awaitable.orTimeout(this, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a new {@code Awaitable} that fails with {@link TimeoutException}
     * if this computation does not complete within the specified duration.
     *
     * @param duration the timeout duration
     * @param unit the time unit
     * @return a new awaitable with timeout semantics
     * @since 6.0.0
     */
    default Awaitable<T> orTimeout(long duration, TimeUnit unit) {
        return Awaitable.orTimeout(this, duration, unit);
    }

    /**
     * Returns a new {@code Awaitable} that completes with the supplied fallback
     * value if this computation does not finish before the timeout expires.
     *
     * @param fallback the value to use when the timeout expires
     * @param timeoutMillis the timeout duration in milliseconds
     * @return a new awaitable that yields either the original result or the fallback
     * @since 6.0.0
     */
    default Awaitable<T> completeOnTimeoutMillis(T fallback, long timeoutMillis) {
        return Awaitable.completeOnTimeout(this, fallback, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns a new {@code Awaitable} that completes with the supplied fallback
     * value if this computation does not finish before the timeout expires.
     *
     * @param fallback the value to use when the timeout expires
     * @param duration the timeout duration
     * @param unit the time unit
     * @return a new awaitable that yields either the original result or the fallback
     * @since 6.0.0
     */
    default Awaitable<T> completeOnTimeout(T fallback, long duration, TimeUnit unit) {
        return Awaitable.completeOnTimeout(this, fallback, duration, unit);
    }

    /**
     * Converts this {@code Awaitable} to a JDK {@link CompletableFuture}
     * for interoperability with APIs that require it.
     *
     * @return a {@code CompletableFuture} representing this computation
     */
    CompletableFuture<T> toCompletableFuture();

    // ---- Static factories ----

    /**
     * Converts the given source to an {@code Awaitable}.
     * <p>
     * If the source is already an {@code Awaitable}, it is returned as-is.
     * Otherwise, the {@link AwaitableAdapterRegistry} is consulted to find a
     * suitable adapter. Built-in adapters handle {@link CompletableFuture},
     * {@link java.util.concurrent.CompletionStage}, {@link java.util.concurrent.Future},
     * and {@link java.util.concurrent.Flow.Publisher}; third-party frameworks
     * can register additional adapters via the registry.
     * <p>
     * This is the recommended entry point for converting external async types
     * to {@code Awaitable}:
     * <pre>
     * Awaitable&lt;String&gt; aw = Awaitable.from(someCompletableFuture)
     * Awaitable&lt;Integer&gt; aw2 = Awaitable.from(someReactorMono)
     * </pre>
     *
     * @param source the source object; must not be {@code null}
     * @param <T>    the result type
     * @return an awaitable backed by the source
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     * @see AwaitableAdapterRegistry#toAwaitable(Object)
     * @since 6.0.0
     */
    static <T> Awaitable<T> from(Object source) {
        return AwaitableAdapterRegistry.toAwaitable(source);
    }

    /**
     * Returns an already-completed {@code Awaitable} with the given value.
     * Analogous to C#'s {@code Task.FromResult()} or JavaScript's
     * {@code Promise.resolve()}.
     *
     * @param value the result value (may be {@code null})
     * @param <T>   the result type
     * @return a completed awaitable
     */
    static <T> Awaitable<T> of(T value) {
        return new GroovyPromise<>(CompletableFuture.completedFuture(value));
    }

    /**
     * Returns an already-failed {@code Awaitable} with the given exception.
     * Analogous to C#'s {@code Task.FromException()} or JavaScript's
     * {@code Promise.reject()}.
     */
    static <T> Awaitable<T> failed(Throwable error) {
        return new GroovyPromise<>(CompletableFuture.failedFuture(error));
    }

    // ---- Combinators (like C#'s Task.WhenAll/WhenAny, JS's Promise.all/any) ----

    /**
     * Returns an {@code Awaitable} that completes when all given sources
     * complete, with a list of their results in order.
     * <p>
     * Analogous to C#'s {@code Task.WhenAll()} or JavaScript's
     * {@code Promise.all()}.  Unlike blocking APIs, this returns immediately
     * and the caller should {@code await} the result:
     * <pre>
     * def results = await Awaitable.all(task1, task2, task3)
     * </pre>
     *
     * @param sources the awaitables, futures, or adapted objects to wait for
     * @return an awaitable that resolves to a list of results
     */
    static Awaitable<List<Object>> all(Object... sources) {
        return AsyncSupport.allAsync(sources);
    }

    /**
     * Returns an {@code Awaitable} that completes when the first of the given
     * sources completes, with the winner's result.
     * <p>
     * Analogous to C#'s {@code Task.WhenAny()} or JavaScript's
     * {@code Promise.any()} / {@code Promise.race()}.
     * <pre>
     * def winner = await Awaitable.any(task1, task2)
     * </pre>
     *
     * @param sources the awaitables to race
     * @return an awaitable that resolves to the first completed result
     */
    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> any(Object... sources) {
        return AsyncSupport.anyAsync(sources);
    }

    /**
     * Returns an {@code Awaitable} that completes when all given sources
     * have settled (succeeded or failed), with a list of {@link AwaitResult}
     * objects describing each outcome.
     * <p>
     * Analogous to JavaScript's {@code Promise.allSettled()}.  Never throws
     * for individual failures; they are captured in the result list.
     * <pre>
     * def results = await Awaitable.allSettled(task1, task2)
     * results.each { println it.success ? it.value : it.error }
     * </pre>
     *
     * @param sources the awaitables to settle
     * @return an awaitable that resolves to a list of settled results
     */
    static Awaitable<List<AwaitResult<Object>>> allSettled(Object... sources) {
        return AsyncSupport.allSettledAsync(sources);
    }

    // ---- Delay (like C#'s Task.Delay, JS's setTimeout) ----

    /**
     * Returns an {@code Awaitable} that completes after the specified delay.
     * Analogous to C#'s {@code Task.Delay()} or JavaScript's
     * {@code new Promise(resolve =&gt; setTimeout(resolve, ms))}.
     * <pre>
     * await Awaitable.delay(1000)  // pause for 1 second
     * </pre>
     *
     * @param milliseconds the delay in milliseconds (must be &ge; 0)
     * @return an awaitable that completes after the delay
     */
    static Awaitable<Void> delay(long milliseconds) {
        return AsyncSupport.delay(milliseconds);
    }

    /**
     * Returns an {@code Awaitable} that completes after the specified delay.
     *
     * @param duration the delay duration (must be &ge; 0)
     * @param unit     the time unit
     * @return an awaitable that completes after the delay
     */
    static Awaitable<Void> delay(long duration, TimeUnit unit) {
        return AsyncSupport.delay(duration, unit);
    }

    // ---- Timeout combinators ----

    /**
     * Adapts the given source to an {@code Awaitable} and applies a non-blocking
     * fail-fast timeout.  Returns a new awaitable that fails with
     * {@link TimeoutException} if the source does not complete before the
     * deadline elapses.
     * <p>
     * The source may be a Groovy {@link Awaitable}, a JDK
     * {@link CompletableFuture}/{@link java.util.concurrent.CompletionStage},
     * or any type supported by {@link AwaitableAdapterRegistry}.  This provides
     * a concise timeout combinator analogous to Kotlin's {@code withTimeout},
     * but as a value-level operation that returns another awaitable.
     *
     * @param source        the async source to time out
     * @param timeoutMillis the timeout duration in milliseconds
     * @param <T>           the result type
     * @return a new awaitable with timeout semantics
     * @since 6.0.0
     */
    static <T> Awaitable<T> orTimeoutMillis(Object source, long timeoutMillis) {
        return AsyncSupport.orTimeout(source, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Adapts the given source and applies a non-blocking fail-fast timeout
     * with explicit {@link TimeUnit}.
     *
     * @param source   the async source to time out
     * @param duration the timeout duration
     * @param unit     the time unit
     * @param <T>      the result type
     * @return a new awaitable with timeout semantics
     * @since 6.0.0
     */
    static <T> Awaitable<T> orTimeout(Object source, long duration, TimeUnit unit) {
        return AsyncSupport.orTimeout(source, duration, unit);
    }

    /**
     * Adapts the given source and returns a new awaitable that yields the
     * supplied fallback value if the timeout expires first.
     *
     * @param source        the async source to wait for
     * @param fallback      the fallback value to use on timeout
     * @param timeoutMillis the timeout duration in milliseconds
     * @param <T>           the result type
     * @return a new awaitable yielding either the original result or the fallback
     * @since 6.0.0
     */
    static <T> Awaitable<T> completeOnTimeoutMillis(Object source, T fallback, long timeoutMillis) {
        return AsyncSupport.completeOnTimeout(source, fallback, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Adapts the given source and returns a new awaitable that yields the
     * supplied fallback value if the timeout expires first.
     *
     * @param source   the async source to wait for
     * @param fallback the fallback value to use on timeout
     * @param duration the timeout duration
     * @param unit     the time unit
     * @param <T>      the result type
     * @return a new awaitable yielding either the original result or the fallback
     * @since 6.0.0
     */
    static <T> Awaitable<T> completeOnTimeout(Object source, T fallback, long duration, TimeUnit unit) {
        return AsyncSupport.completeOnTimeout(source, fallback, duration, unit);
    }

    // ---- Executor configuration ----

    /**
     * Returns the current executor used for {@code async} operations.
     * On JDK 21+, this defaults to a virtual-thread-per-task executor.
     * On earlier JDKs, a cached daemon thread pool is used.
     */
    static Executor getExecutor() {
        return AsyncSupport.getExecutor();
    }

    /**
     * Sets the executor to use for {@code async} operations.
     * Pass {@code null} to reset to the default executor.
     *
     * @param executor the executor to use, or {@code null} for default
     */
    static void setExecutor(Executor executor) {
        AsyncSupport.setExecutor(executor);
    }

    /**
     * Returns {@code true} if the running JVM supports virtual threads (JDK 21+).
     */
    static boolean isVirtualThreadsAvailable() {
        return AsyncSupport.isVirtualThreadsAvailable();
    }
}
