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
import java.util.function.Supplier;

/**
 * Core abstraction for asynchronous computations in Groovy.
 * <p>
 * {@code Awaitable} represents a computation that may not have completed
 * yet.  It serves as both an instance type (the result of {@code async}
 * methods and the input to {@code await}) and a static API surface for
 * combinators, factories, and configuration.
 * <p>
 * <b>Static combinators</b> (all return {@code Awaitable}, suitable for use
 * with {@code await}).  Multi-argument {@code await} desugars to
 * {@code Awaitable.all(...)}, so {@code await(a, b)} and {@code await a, b}
 * are shorthand for {@code await Awaitable.all(a, b)}:
 * <ul>
 *   <li>{@link #all(Object...) Awaitable.all(a, b, c)} — waits for all
 *       tasks to complete, returning their results in order</li>
 *   <li>{@link #any(Object...) Awaitable.any(a, b)} — returns the result
 *       of the first task to complete</li>
 *   <li>{@link #first(Object...) Awaitable.first(a, b, c)} — returns the result
 *       of the first task to complete <em>successfully</em> (like JavaScript's
 *       {@code Promise.any()}; only fails when all sources fail)</li>
 *   <li>{@link #allSettled(Object...) Awaitable.allSettled(a, b)} — waits
 *       for all tasks to settle (succeed or fail), returning an
 *       {@link AwaitResult} list</li>
 *   <li>{@link #delay(long) Awaitable.delay(ms)} — completes after a
 *       non-blocking delay</li>
 *   <li>{@link #orTimeoutMillis(Object, long) Awaitable.orTimeoutMillis(task, ms)} —
 *       fails with {@link java.util.concurrent.TimeoutException} if the task
 *       does not complete in time</li>
 *   <li>{@link #completeOnTimeoutMillis(Object, Object, long)
 *       Awaitable.completeOnTimeoutMillis(task, fallback, ms)} —
 *       uses a fallback value when the timeout expires</li>
 * </ul>
 * <p>
 * <b>Static factories and conversion:</b>
 * <ul>
 *   <li>{@link #from(Object) Awaitable.from(source)} — converts any supported
 *       async type (CompletableFuture, CompletionStage, Future, etc.)
 *       to an {@code Awaitable}</li>
 *   <li>{@link #of(Object) Awaitable.of(value)} — wraps an already-available
 *       value in a completed {@code Awaitable}</li>
 *   <li>{@link #failed(Throwable) Awaitable.failed(error)} — wraps an
 *       exception in an immediately-failed {@code Awaitable}</li>
 *   <li>{@code Awaitable.go { ... }} — lightweight task spawn (Groovy extension method)</li>
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
     * Useful at API boundaries where you need to attach logging, metrics, or
     * other side effects without blocking for the result.
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
     *
     * @param action the completion callback receiving the result or failure
     * @return a new awaitable that completes with the original result
     * @since 6.0.0
     */
    default Awaitable<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return GroovyPromise.of(toCompletableFuture().whenComplete((value, error) ->
                action.accept(value, error == null ? null : AsyncSupport.unwrap(error))));
    }

    /**
     * Returns a new {@code Awaitable} that handles both the successful and the
     * exceptional completion paths in a single continuation.
     * <p>
     * The supplied throwable is transparently unwrapped so the handler sees the
     * original failure.  This provides a single place for success/failure
     * projection, combining both paths in one callback.
     *
     * @param fn the handler receiving either the value or the failure
     * @param <U> the projected result type
     * @return a new awaitable holding the handler's result
     * @since 6.0.0
     */
    default <U> Awaitable<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return GroovyPromise.of(toCompletableFuture().handle((value, error) ->
                fn.apply(value, error == null ? null : AsyncSupport.unwrap(error))));
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
     * {@link java.util.concurrent.CompletionStage}, and
     * {@link java.util.concurrent.Future}; third-party frameworks can register
     * additional adapters via the registry.
     * <p>
     * This is the recommended entry point for converting external async types
     * to {@code Awaitable}:
     * <pre>
     * Awaitable&lt;String&gt; aw = Awaitable.from(someCompletableFuture)
     * Awaitable&lt;Integer&gt; aw2 = Awaitable.from(someReactorMono)
     * </pre>
     *
     * @param source the source object; if {@code null}, returns a completed
     *               awaitable with a {@code null} result
     * @param <T>    the result type
     * @return an awaitable backed by the source
     * @throws IllegalArgumentException if no adapter supports the source type
     * @see AwaitableAdapterRegistry#toAwaitable(Object)
     * @since 6.0.0
     */
    static <T> Awaitable<T> from(Object source) {
        return AwaitableAdapterRegistry.toAwaitable(source);
    }

    /**
     * Returns an already-completed {@code Awaitable} with the given value.
     * Useful for returning a pre-computed result from an API that requires
     * an {@code Awaitable} return type.
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
     * <p>
     * Useful for signaling a synchronous error from an API that returns
     * {@code Awaitable}, without spawning a thread:
     * <pre>
     * if (id &lt; 0) return Awaitable.failed(new IllegalArgumentException("negative id"))
     * </pre>
     *
     * @param error the exception to wrap; must not be {@code null}
     * @param <T>   the nominal result type (never produced, since the awaitable is failed)
     * @return an immediately-failed awaitable
     * @throws NullPointerException if {@code error} is {@code null}
     */
    static <T> Awaitable<T> failed(Throwable error) {
        return new GroovyPromise<>(CompletableFuture.failedFuture(error));
    }

    /**
     * Spawns a lightweight task on the default executor.
     *
     * @param supplier the task body
     * @param <T>      the result type
     * @return an awaitable representing the spawned task
     * @since 6.0.0
     */
    static <T> Awaitable<T> go(Supplier<T> supplier) {
        return GroovyPromise.of(CompletableFuture.supplyAsync(supplier, AsyncSupport.getExecutor()));
    }

    /**
     * Convenience delegate to {@link AsyncScope#withScope(Function)}.
     * Creates a structured concurrency scope, executes the body within it,
     * and ensures all child tasks complete before returning.
     *
     * @param body the function receiving the scope
     * @param <T>  the result type
     * @return the body's return value
     * @see AsyncScope#withScope(Function)
     * @since 6.0.0
     */
    static <T> T withScope(Function<AsyncScope, T> body) {
        return AsyncScope.withScope(body);
    }

    // ---- Combinators ----

    /**
     * Returns an {@code Awaitable} that completes when all given sources
     * complete successfully, with a list of their results in order.
     * <p>
     * Like JavaScript's {@code Promise.all()}, the combined awaitable fails as
     * soon as the first source fails. Remaining sources are not cancelled
     * automatically; cancel them explicitly if that is required by your workflow.
     * <p>
     * Unlike blocking APIs, this returns immediately and the caller should
     * {@code await} the result.  All three forms below are equivalent:
     * <pre>
     * // Explicit all() call
     * def results = await Awaitable.all(task1, task2, task3)
     *
     * // Parenthesized multi-arg await — syntactic sugar
     * def results = await(task1, task2, task3)
     *
     * // Unparenthesized multi-arg await — most concise form
     * def results = await task1, task2, task3
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
     * Returns an {@code Awaitable} that completes with the result of the first
     * source that succeeds.  Individual failures are silently absorbed; only
     * when <em>all</em> sources have failed does the returned awaitable reject
     * with an {@link IllegalStateException} whose
     * {@linkplain Throwable#getSuppressed() suppressed} array contains every
     * individual error.
     * <p>
     * This is the Groovy equivalent of JavaScript's {@code Promise.any()}.
     * Contrast with {@link #any(Object...)} which returns the first result to
     * complete regardless of success or failure.
     * <p>
     * <b>Typical use cases:</b>
     * <ul>
     *   <li><em>Hedged requests</em> — send the same request to multiple
     *       endpoints, use whichever responds first successfully</li>
     *   <li><em>Graceful degradation</em> — try a primary data source, then a
     *       fallback, then a cache, accepting the first success</li>
     *   <li><em>Redundant queries</em> — send the same query to different
     *       database replicas for improved latency</li>
     * </ul>
     * <pre>
     * // Hedged HTTP request
     * def response = await Awaitable.first(
     *     fetchFromPrimary(),
     *     fetchFromFallback(),
     *     fetchFromCache()
     * )
     * </pre>
     *
     * @param sources the awaitables to race for first success; must not be
     *                {@code null}, empty, or contain {@code null} elements
     * @param <T>     the result type
     * @return an awaitable that resolves with the first successful result
     * @throws IllegalArgumentException if {@code sources} is {@code null},
     *         empty, or contains {@code null} elements
     * @since 6.0.0
     * @see AsyncSupport#firstAsync(Object...) AsyncSupport.firstAsync — implementation
     */
    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> first(Object... sources) {
        return AsyncSupport.firstAsync(sources);
    }

    /**
     * Returns an {@code Awaitable} that completes when all given sources
     * have settled (succeeded or failed), with a list of {@link AwaitResult}
     * objects describing each outcome.
     * <p>
     * Never throws for individual failures; they are captured in the result list.
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

    // ---- Delay ----

    /**
     * Returns an {@code Awaitable} that completes after the specified delay.
     * Does not block any thread; the delay is handled by a scheduled executor.
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
     * a concise timeout combinator that returns another awaitable rather than
     * requiring structural timeout blocks.
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
     * <p>
     * On JDK&nbsp;21+, the default is a virtual-thread-per-task executor.
     * On JDK&nbsp;17–20, a bounded cached daemon thread pool is used
     * (size controlled by the {@code groovy.async.parallelism} system property,
     * default {@code 256}).
     * <p>
     * This method is thread-safe and may be called from any thread.
     *
     * @return the current executor, never {@code null}
     * @see #setExecutor(Executor)
     */
    static Executor getExecutor() {
        return AsyncSupport.getExecutor();
    }

    /**
     * Sets the executor to use for {@code async} operations.
     * <p>
     * Pass {@code null} to reset to the default executor.  The change
     * takes effect immediately for all subsequent {@code async} method
     * invocations; tasks already in flight continue using the executor
     * that launched them.
     * <p>
     * This method is thread-safe and may be called from any thread.
     *
     * @param executor the executor to use, or {@code null} to restore
     *                 the default executor
     * @see #getExecutor()
     */
    static void setExecutor(Executor executor) {
        AsyncSupport.setExecutor(executor);
    }

    /**
     * Returns {@code true} if the running JVM supports virtual threads (JDK&nbsp;21+).
     * <p>
     * When virtual threads are available, the default executor uses a
     * virtual-thread-per-task strategy that scales to millions of
     * concurrent tasks with minimal memory overhead.
     *
     * @return {@code true} if virtual threads are available
     */
    static boolean isVirtualThreadsAvailable() {
        return AsyncSupport.isVirtualThreadsAvailable();
    }
}
