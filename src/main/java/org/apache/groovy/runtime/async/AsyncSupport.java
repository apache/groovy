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

import groovy.concurrent.AsyncStream;
import groovy.concurrent.AwaitResult;
import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Internal runtime support for the {@code async}/{@code await} language feature
 * and the {@link groovy.transform.Async @Async} annotation.
 * <p>
 * This class contains the actual implementation invoked by compiler-generated
 * code.  User code should prefer the static methods on
 * {@link groovy.concurrent.Awaitable} (e.g., {@code Awaitable.all()},
 * {@code Awaitable.delay()}) for combinators and configuration.
 * <p>
 * Key responsibilities:
 * <ul>
 *   <li><b>Async execution</b> — {@code executeAsync()}, {@code executeAsyncVoid()},
 *       and {@code wrapAsync()} run closures on the configured executor</li>
 *   <li><b>Await</b> — all {@code await()} overloads use
 *       {@link groovy.concurrent.Awaitable#from(Object) Awaitable.from()} so
 *       that third-party async types
 *       (RxJava {@code Single}, Reactor {@code Mono}, etc.) are supported
 *       transparently once an adapter is registered</li>
 *   <li><b>Async generators</b> — {@code generateAsyncStream()} manages the
 *       producer/consumer bridge for methods using {@code yield return}</li>
 *   <li><b>Async stream conversion</b> — {@code toAsyncStream()} adapts
 *       collections, arrays, and adapter-supported types for {@code for await}</li>
 *   <li><b>Async stream cleanup</b> — {@code closeStream()} lets compiler-generated
 *       {@code for await} loops release resources and propagate early-exit
 *       cancellation upstream</li>
 *   <li><b>Defer</b> — {@code createDeferScope()}, {@code defer()}, and
 *       {@code executeDeferScope()} implement Go-style deferred cleanup with
 *       LIFO execution and exception suppression</li>
 *   <li><b>Delay</b> — {@code delay()} provides non-blocking delays using a
 *       shared {@link java.util.concurrent.ScheduledExecutorService}</li>
 *   <li><b>Timeouts</b> — {@code orTimeout()} and {@code completeOnTimeout()} apply
 *       non-blocking deadlines while preserving the {@link Awaitable} abstraction</li>
 * </ul>
 * <p>
 * <b>Thread pool configuration</b>
 * <ul>
 *   <li>On JDK 21+ the default executor is a virtual-thread-per-task executor
 *       obtained via {@code Executors.newVirtualThreadPerTaskExecutor()}.
 *       Virtual threads make blocking within {@code await()} essentially free:
 *       the JVM parks the virtual thread and releases the underlying carrier
 *       thread, achieving efficiency comparable to C#'s state-machine-based
 *       approach without requiring compiler-level control-flow rewriting.</li>
 *   <li>On earlier JDKs the fallback is a cached (elastic) daemon thread pool
 *       whose maximum size is controlled by the system property
 *       {@code groovy.async.parallelism} (default: {@code 256}).  Idle threads
 *       are reaped after 60 seconds.  A cached pool is used instead of a
 *       fixed pool to prevent thread starvation when async tasks block
 *       (e.g., in generators or nested {@code await} calls).  A
 *       {@link java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy
 *       CallerRunsPolicy} is used so that task submission never fails
 *       with {@link java.util.concurrent.RejectedExecutionException} when
 *       the pool is saturated.</li>
 *   <li>The executor can be overridden at any time via {@link #setExecutor}.</li>
 * </ul>
 * <p>
 * <b>Exception handling</b> follows the same transparency principle as C# and
 * JavaScript: the <em>original</em> exception is rethrown without being wrapped
 * in an {@link java.util.concurrent.ExecutionException ExecutionException} or
 * {@link java.util.concurrent.CompletionException CompletionException}.
 *
 * @see groovy.concurrent.Awaitable
 * @see groovy.transform.Async
 * @since 6.0.0
 */
public class AsyncSupport {

    /**
     * {@code true} if the running JVM supports virtual threads (JDK 21+).
     * Detected once at class-load time via {@link MethodHandle} reflection.
     */
    private static final boolean VIRTUAL_THREADS_AVAILABLE;

    private static final Executor VIRTUAL_THREAD_EXECUTOR;

    /**
     * Maximum thread pool size when virtual threads are unavailable.
     * Configurable via the system property {@code groovy.async.parallelism}.
     * Defaults to {@code 256} to prevent runaway thread creation while still
     * allowing ample concurrency for blocking async operations.
     */
    private static final int FALLBACK_MAX_THREADS;

    private static final Executor FALLBACK_EXECUTOR;

    static {
        Executor vtExecutor = null;
        boolean vtAvailable = false;
        try {
            MethodHandle mh = MethodHandles.lookup().findStatic(
                    Executors.class, "newVirtualThreadPerTaskExecutor",
                    MethodType.methodType(ExecutorService.class));
            vtExecutor = (Executor) mh.invoke();
            vtAvailable = true;
        } catch (Throwable ignored) {
            // JDK < 21 — virtual threads not available
        }
        VIRTUAL_THREAD_EXECUTOR = vtExecutor;
        VIRTUAL_THREADS_AVAILABLE = vtAvailable;

        FALLBACK_MAX_THREADS = org.apache.groovy.util.SystemUtil.getIntegerSafe(
                "groovy.async.parallelism", 256);
        if (!VIRTUAL_THREADS_AVAILABLE) {
            // Use a cached thread pool (threads created on demand, idle threads
            // reaped after 60 s) to avoid thread starvation.  Async/await relies
            // on blocking operations (SynchronousQueue in generators, Future.get
            // in await) — a fixed-size pool can deadlock when all threads block.
            // CallerRunsPolicy provides graceful degradation when all threads
            // are busy: the submitting thread runs the task itself, preventing
            // RejectedExecutionException at the cost of temporary back-pressure.
            FALLBACK_EXECUTOR = new ThreadPoolExecutor(
                    0, FALLBACK_MAX_THREADS,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("groovy-async-" + t.getId());
                        return t;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy());
        } else {
            FALLBACK_EXECUTOR = null;
        }
    }

    private static volatile Executor defaultExecutor =
            VIRTUAL_THREADS_AVAILABLE ? VIRTUAL_THREAD_EXECUTOR : FALLBACK_EXECUTOR;

    private AsyncSupport() { }

    // ---- await overloads ------------------------------------------------

    /**
     * Awaits the result of an {@link Awaitable}.
     * <p>
     * Blocks the calling thread until the computation completes and returns
     * its result.  If the computation failed, the original exception is
     * rethrown transparently (even if it is a checked exception).  If the
     * waiting thread is interrupted, an {@link java.util.concurrent.CancellationException}
     * is thrown and the interrupt flag is restored.
     *
     * @param awaitable the computation to await
     * @param <T>       the result type
     * @return the computed result
     */
    public static <T> T await(Awaitable<T> awaitable) {
        try {
            return awaitable.get();
        } catch (ExecutionException e) {
            throw rethrowUnwrapped(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CancellationException ce = new CancellationException("Interrupted while awaiting");
            ce.initCause(e);
            throw ce;
        }
    }

    /**
     * Awaits the result of a {@link CompletableFuture}.
     * Uses {@link CompletableFuture#join()} for non-interruptible waiting.
     *
     * @param future the future to await
     * @param <T>    the result type
     * @return the computed result
     */
    public static <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            throw rethrowUnwrapped(e);
        } catch (CancellationException e) {
            throw e;
        }
    }

    /**
     * Awaits the result of a {@link CompletionStage} by converting it to a
     * {@link CompletableFuture} first.
     *
     * @param stage the completion stage to await
     * @param <T>   the result type
     * @return the computed result
     */
    public static <T> T await(CompletionStage<T> stage) {
        return await(stage.toCompletableFuture());
    }

    /**
     * Awaits the result of a {@link Future}.  If the future is a
     * {@link CompletableFuture}, delegates to the more efficient
     * {@link #await(CompletableFuture)} overload.
     *
     * @param future the future to await
     * @param <T>    the result type
     * @return the computed result
     */
    public static <T> T await(Future<T> future) {
        if (future instanceof CompletableFuture<T> cf) {
            return await(cf);
        }
        try {
            return future.get();
        } catch (ExecutionException e) {
            throw rethrowUnwrapped(e);
        } catch (CancellationException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            CancellationException ce = new CancellationException("Interrupted while awaiting future");
            ce.initCause(e);
            throw ce;
        }
    }

    /**
     * Awaits an arbitrary object by adapting it to {@link Awaitable} via
     * {@link Awaitable#from(Object)}.  This is the fallback overload called
     * by the {@code await} expression when the compile-time type is not one
     * of the other supported types.  Returns {@code null} for a {@code null}
     * argument.
     *
     * @param source the object to await
     * @param <T>    the result type
     * @return the computed result, or {@code null} if {@code source} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> T await(Object source) {
        if (source == null) return null;
        if (source instanceof Awaitable) return await((Awaitable<T>) source);
        if (source instanceof CompletableFuture) return await((CompletableFuture<T>) source);
        if (source instanceof CompletionStage) return await((CompletionStage<T>) source);
        if (source instanceof Future) return await((Future<T>) source);
        if (source instanceof Closure) return awaitClosure((Closure<?>) source);
        return await(Awaitable.<T>from(source));
    }

    /**
     * Handles the case where a {@link Closure} is passed to {@code await}.
     * <p>
     * An {@code async} closure or lambda must be explicitly called before
     * awaiting.  For example, {@code await(myClosure())} is correct, while
     * {@code await(myClosure)} is not.  This method always throws an
     * {@link IllegalArgumentException} with guidance on the correct usage.
     *
     * @param closure the closure that was incorrectly passed to await
     * @param <T>     the result type (never actually returned)
     * @return never returns
     * @throws IllegalArgumentException always
     */
    @SuppressWarnings("unchecked")
    private static <T> T awaitClosure(Closure<?> closure) {
        throw new IllegalArgumentException(
                "Cannot await a Closure directly. "
                + "Call the closure first, then await the result: "
                + "await(myClosure()) or await(myClosure(args))");
    }

    // ---- async ----------------------------------------------------------

    /**
     * Executes the given closure asynchronously on the specified executor,
     * returning an {@link Awaitable} that completes with the closure's return
     * value.  Used by {@link groovy.transform.Async @Async} methods.
     *
     * @param closure  the closure to execute
     * @param executor the executor on which to run the closure
     * @param <T>      the result type
     * @return an awaitable representing the pending computation
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> executeAsync(Closure<T> closure, Executor executor) {
        return GroovyPromise.of(CompletableFuture.supplyAsync(() -> {
            try {
                return closure.call();
            } catch (Throwable t) {
                throw wrapForFuture(t);
            }
        }, executor));
    }

    /**
     * Void variant of {@link #executeAsync(Closure, Executor)} for
     * {@link groovy.transform.Async @Async} methods whose return type is
     * {@code void}.
     *
     * @param closure  the closure to execute
     * @param executor the executor on which to run the closure
     * @return an awaitable that completes when the closure finishes
     */
    public static Awaitable<Void> executeAsyncVoid(Closure<?> closure, Executor executor) {
        return GroovyPromise.of(CompletableFuture.runAsync(() -> {
            try {
                closure.call();
            } catch (Throwable t) {
                throw wrapForFuture(t);
            }
        }, executor));
    }

    /**
     * Executes the given closure asynchronously using the default executor,
     * returning an {@link Awaitable}.  This method is used internally by the
     * {@link groovy.transform.Async @Async} annotation transformation and
     * the {@code async} method modifier.  For the {@code async { ... }}
     * closure expression syntax, see {@link #wrapAsync(Closure)}.
     *
     * @param closure the closure to execute
     * @param <T>     the result type
     * @return an awaitable representing the pending computation
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> async(Closure<T> closure) {
        return executeAsync(closure, defaultExecutor);
    }

    /**
     * Wraps a closure so that each invocation executes the body asynchronously
     * and returns an {@link Awaitable}.  This is the runtime entry point for
     * the {@code async { ... }} expression syntax.  The returned closure must
     * be explicitly called to start the async computation:
     * <pre>
     * def task = async { expensiveWork() }
     * def result = await(task())  // explicit call required
     * </pre>
     *
     * @param closure the closure to wrap
     * @param <T>     the result type
     * @return a new closure whose calls produce awaitables
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Awaitable<T>> wrapAsync(Closure<T> closure) {
        return new Closure<Awaitable<T>>(closure.getOwner(), closure.getThisObject()) {
            @SuppressWarnings("unused")
            public Awaitable<T> doCall(Object... args) {
                return GroovyPromise.of(CompletableFuture.supplyAsync(() -> {
                    try {
                        return closure.call(args);
                    } catch (Throwable t) {
                        throw wrapForFuture(t);
                    }
                }, defaultExecutor));
            }
        };
    }

    /**
     * Wraps a generator closure (one containing {@code yield return} statements)
     * so that each invocation returns an {@link AsyncStream} producing the
     * yielded elements.  This is the runtime entry point for all async
     * generator closures ({@code async { yield return ... }}).  The returned
     * closure must be explicitly called to start the generation:
     * <pre>
     * def gen = async { yield return 1; yield return 2 }
     * for await (item in gen()) { println item }
     * </pre>
     *
     * @param closure the generator closure to wrap
     * @param <T>     the element type
     * @return a new closure whose calls produce async streams
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<AsyncStream<T>> wrapAsyncGenerator(Closure<?> closure) {
        return new Closure<AsyncStream<T>>(closure.getOwner(), closure.getThisObject()) {
            @SuppressWarnings("unused")
            public AsyncStream<T> doCall(Object... args) {
                AsyncStreamGenerator<T> gen = new AsyncStreamGenerator<>();
                CompletableFuture.runAsync(() -> {
                    gen.attachProducer(Thread.currentThread());
                    try {
                        Object[] allArgs = new Object[args.length + 1];
                        allArgs[0] = gen;
                        System.arraycopy(args, 0, allArgs, 1, args.length);
                        closure.call(allArgs);
                        gen.complete();
                    } catch (Throwable t) {
                        gen.error(t);
                    } finally {
                        gen.detachProducer(Thread.currentThread());
                    }
                }, defaultExecutor);
                return gen;
            }
        };
    }

    // ---- awaitAll / awaitAny / awaitAllSettled ---------------------------

    /**
     * Waits for all given awaitables to complete and returns their results
     * as a list, preserving order.  Analogous to JavaScript's
     * {@code Promise.all()}.  If any awaitable fails, the exception is
     * rethrown immediately.
     * <p>
     * Returns an empty list if {@code awaitables} is {@code null} or empty.
     *
     * @param awaitables the awaitables (or futures, completion stages, etc.);
     *                   elements must not be {@code null}
     * @return a list of results in the same order as the arguments
     * @throws IllegalArgumentException if any element is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static List<Object> awaitAll(Object... awaitables) {
        if (awaitables == null || awaitables.length == 0) {
            return new ArrayList<>();
        }
        for (int i = 0; i < awaitables.length; i++) {
            if (awaitables[i] == null) {
                throw new IllegalArgumentException("awaitAll: element at index " + i + " is null");
            }
        }
        CompletableFuture<?>[] futures =
            Arrays.stream(awaitables).map(AsyncSupport::toCompletableFuture).toArray(CompletableFuture[]::new);
        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException e) {
            throw rethrowUnwrapped(e);
        }
        List<Object> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> f : futures) {
            results.add(f.join());
        }
        return results;
    }

    /**
     * Waits for any one of the given awaitables to complete and returns
     * its result.  Analogous to JavaScript's {@code Promise.any()} / {@code Promise.race()}.
     *
     * @param awaitables the awaitables to race; must not be {@code null},
     *                   empty, or contain {@code null} elements
     * @return the result of the first awaitable that completes
     * @throws IllegalArgumentException if {@code awaitables} is {@code null},
     *         empty, or contains {@code null} elements
     */
    @SuppressWarnings("unchecked")
    public static Object awaitAny(Object... awaitables) {
        if (awaitables == null || awaitables.length == 0) {
            throw new IllegalArgumentException("awaitAny requires at least one awaitable");
        }
        for (int i = 0; i < awaitables.length; i++) {
            if (awaitables[i] == null) {
                throw new IllegalArgumentException("awaitAny: element at index " + i + " is null");
            }
        }
        CompletableFuture<?>[] futures =
            Arrays.stream(awaitables).map(AsyncSupport::toCompletableFuture).toArray(CompletableFuture[]::new);
        try {
            return CompletableFuture.anyOf(futures).join();
        } catch (CompletionException e) {
            throw rethrowUnwrapped(e);
        }
    }

    /**
     * Waits for all given awaitables to settle (complete either successfully
     * or with an exception) and returns a list of {@link AwaitResult} objects.
     * Analogous to JavaScript's {@code Promise.allSettled()}.  Never throws
     * for individual failures; failures are captured in the result list.
     * <p>
     * Returns an empty list if {@code awaitables} is {@code null} or empty.
     *
     * @param awaitables the awaitables to settle; elements must not be {@code null}
     * @return a list of results in the same order as the arguments
     * @throws IllegalArgumentException if any element is {@code null}
     */
    @SuppressWarnings("unchecked")
    public static List<AwaitResult<Object>> awaitAllSettled(Object... awaitables) {
        if (awaitables == null || awaitables.length == 0) {
            return new ArrayList<>();
        }
        for (int i = 0; i < awaitables.length; i++) {
            if (awaitables[i] == null) {
                throw new IllegalArgumentException("awaitAllSettled: element at index " + i + " is null");
            }
        }
        CompletableFuture<?>[] futures =
            Arrays.stream(awaitables).map(AsyncSupport::toCompletableFuture).toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(
            Arrays.stream(futures)
                .map(f -> f.handle((v, t) -> null))
                .toArray(CompletableFuture[]::new)
        ).join();

        return getAwaitResults(futures);
    }

    /** Converts an arbitrary source to a {@link CompletableFuture} for internal use. */
    private static CompletableFuture<?> toCompletableFuture(Object source) {
        if (source instanceof CompletableFuture<?> cf) return cf;
        if (source instanceof Awaitable<?> a) return a.toCompletableFuture();
        if (source instanceof CompletionStage<?> cs) return cs.toCompletableFuture();
        return Awaitable.from(source).toCompletableFuture();
    }

    // ---- non-blocking combinators (return Awaitable) --------------------

    /**
     * Non-blocking variant of {@link #awaitAll(Object...)} that returns an
     * {@link Awaitable} instead of blocking.  Used by
     * {@link Awaitable#all(Object...)}.
     */
    public static Awaitable<List<Object>> allAsync(Object... sources) {
        if (sources == null || sources.length == 0) {
            return Awaitable.of(new ArrayList<>());
        }
        CompletableFuture<?>[] futures = new CompletableFuture[sources.length];
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] == null) {
                throw new IllegalArgumentException("Awaitable.all: element at index " + i + " is null");
            }
            futures[i] = toCompletableFuture(sources[i]);
        }
        CompletableFuture<List<Object>> combined = CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    List<Object> results = new ArrayList<>(futures.length);
                    for (CompletableFuture<?> f : futures) {
                        results.add(f.join());
                    }
                    return results;
                });
        return GroovyPromise.of(combined);
    }

    /**
     * Non-blocking variant of {@link #awaitAny(Object...)} that returns an
     * {@link Awaitable} instead of blocking.  Used by
     * {@link Awaitable#any(Object...)}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> anyAsync(Object... sources) {
        if (sources == null || sources.length == 0) {
            throw new IllegalArgumentException("Awaitable.any requires at least one source");
        }
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] == null) {
                throw new IllegalArgumentException("Awaitable.any: element at index " + i + " is null");
            }
        }
        CompletableFuture<?>[] futures = Arrays.stream(sources).map(AsyncSupport::toCompletableFuture).toArray(CompletableFuture[]::new);
        return GroovyPromise.of((CompletableFuture<T>) CompletableFuture.anyOf(futures));
    }

    /**
     * Non-blocking variant of {@link #awaitAllSettled(Object...)} that returns an
     * {@link Awaitable} instead of blocking.  Used by
     * {@link Awaitable#allSettled(Object...)}.
     */
    public static Awaitable<List<AwaitResult<Object>>> allSettledAsync(Object... sources) {
        if (sources == null || sources.length == 0) {
            return Awaitable.of(new ArrayList<>());
        }
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] == null) {
                throw new IllegalArgumentException("Awaitable.allSettled: element at index " + i + " is null");
            }
        }
        CompletableFuture<?>[] futures = Arrays.stream(sources).map(AsyncSupport::toCompletableFuture).toArray(CompletableFuture[]::new);
        // Wait for all to settle (handle converts failures to non-exceptional completions)
        CompletableFuture<List<AwaitResult<Object>>> combined = CompletableFuture.allOf(
                Arrays.stream(futures)
                        .map(f -> f.handle((v, t) -> null))
                        .toArray(CompletableFuture[]::new)
        ).thenApply(v -> getAwaitResults(futures));
        return GroovyPromise.of(combined);
    }

    private static List<AwaitResult<Object>> getAwaitResults(CompletableFuture<?>[] futures) {
        List<AwaitResult<Object>> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> f : futures) {
            try {
                results.add(AwaitResult.success(f.join()));
            } catch (CompletionException e) {
                results.add(AwaitResult.failure(deepUnwrap(e)));
            } catch (CancellationException e) {
                results.add(AwaitResult.failure(e));
            }
        }
        return results;
    }

    /**
     * Applies fail-fast timeout semantics to the given source.
     * Returns an awaitable that fails with {@link TimeoutException} if the
     * source does not complete before the timeout elapses.
     * <p>
     * The timeout does <em>not</em> cancel the original source automatically.
     * This mirrors value-level race composition and keeps cancellation explicit.
     *
     * @param source the async source to time out
     * @param timeout the timeout duration
     * @param unit the timeout unit
     * @param <T> the result type
     * @return an awaitable that fails on timeout
     * @throws IllegalArgumentException if any argument is invalid
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> orTimeout(Object source, long timeout, TimeUnit unit) {
        validateTimeoutArguments(source, timeout, unit, "Awaitable.orTimeout");
        CompletableFuture<T> sourceFuture = (CompletableFuture<T>) toCompletableFuture(source);
        CompletableFuture<T> result = new CompletableFuture<>();
        TimeoutException te = new TimeoutException(
                "Timed out after " + timeout + " " + unit.name().toLowerCase(Locale.ROOT));
        scheduleTimeoutRace(sourceFuture, result, () -> result.completeExceptionally(te), timeout, unit);
        return GroovyPromise.of(result);
    }

    /**
     * Millisecond shortcut for {@link #orTimeout(Object, long, TimeUnit)}.
     *
     * @param source the async source to time out
     * @param timeoutMillis timeout in milliseconds
     * @param <T> result type
     * @return an awaitable that fails on timeout
     * @since 6.0.0
     */
    public static <T> Awaitable<T> orTimeoutMillis(Object source, long timeoutMillis) {
        return orTimeout(source, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Applies fallback-on-timeout semantics to the given source.
     * Returns an awaitable that completes with the supplied fallback value if
     * the source has not completed before the timeout elapses.
     *
     * @param source the async source to wait for
     * @param fallback fallback value when timeout elapses first
     * @param timeout the timeout duration
     * @param unit the timeout unit
     * @param <T> the result type
     * @return an awaitable that yields either source result or fallback value
     * @throws IllegalArgumentException if any argument is invalid
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> completeOnTimeout(Object source, T fallback, long timeout, TimeUnit unit) {
        validateTimeoutArguments(source, timeout, unit, "Awaitable.completeOnTimeout");
        CompletableFuture<T> sourceFuture = (CompletableFuture<T>) toCompletableFuture(source);
        CompletableFuture<T> result = new CompletableFuture<>();
        scheduleTimeoutRace(sourceFuture, result, () -> result.complete(fallback), timeout, unit);
        return GroovyPromise.of(result);
    }

    /**
     * Millisecond shortcut for {@link #completeOnTimeout(Object, Object, long, TimeUnit)}.
     *
     * @param source the async source to wait for
     * @param fallback fallback value when timeout elapses first
     * @param timeoutMillis timeout in milliseconds
     * @param <T> result type
     * @return an awaitable that yields either source result or fallback
     * @since 6.0.0
     */
    public static <T> Awaitable<T> completeOnTimeoutMillis(Object source, T fallback, long timeoutMillis) {
        return completeOnTimeout(source, fallback, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Shared logic for {@link #orTimeout} and {@link #completeOnTimeout}:
     * schedules the timeout action and wires up source completion to cancel
     * the timer and propagate the result or error.
     */
    private static <T> void scheduleTimeoutRace(
            CompletableFuture<T> sourceFuture,
            CompletableFuture<T> result,
            Runnable onTimeout,
            long duration, TimeUnit unit) {
        ScheduledFuture<?> timeoutTask = DELAY_SCHEDULER.schedule(onTimeout, duration, unit);
        sourceFuture.whenComplete((value, error) -> {
            timeoutTask.cancel(false);
            if (error != null) {
                result.completeExceptionally(deepUnwrap(error));
            } else {
                result.complete(value);
            }
        });
    }

    private static void validateTimeoutArguments(Object source, long duration, TimeUnit unit, String methodName) {
        if (source == null) {
            throw new IllegalArgumentException(methodName + ": source must not be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException(methodName + ": TimeUnit must not be null");
        }
        if (duration < 0) {
            throw new IllegalArgumentException(methodName + ": duration must not be negative: " + duration);
        }
    }

    // ---- delay ----------------------------------------------------------

    /**
     * Shared single-thread scheduler for {@link #delay} operations.
     * Uses a daemon thread so it does not prevent JVM shutdown.
     */
    private static final ScheduledExecutorService DELAY_SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "groovy-async-delay");
                t.setDaemon(true);
                return t;
            });

    /**
     * Returns an {@link Awaitable} that completes (with {@code null}) after
     * the specified delay.  Analogous to C#'s {@code Task.Delay()} or
     * JavaScript's {@code new Promise(resolve => setTimeout(resolve, ms))}.
     * <p>
     * This is a non-blocking delay: the calling thread is not blocked.
     * Use {@code await delay(ms)} inside an {@code async} context to pause
     * the async workflow for the given duration without blocking a thread.
     *
     * @param milliseconds the delay in milliseconds (must be &ge; 0)
     * @return an awaitable that completes after the delay
     * @throws IllegalArgumentException if {@code milliseconds} is negative
     */
    public static Awaitable<Void> delay(long milliseconds) {
        return delay(milliseconds, TimeUnit.MILLISECONDS);
    }

    /**
     * Returns an {@link Awaitable} that completes (with {@code null}) after
     * the specified delay in the given time unit.
     *
     * @param duration the delay duration (must be &ge; 0)
     * @param unit     the time unit (must not be {@code null})
     * @return an awaitable that completes after the delay
     * @throws IllegalArgumentException if {@code duration} is negative or
     *         {@code unit} is {@code null}
     */
    public static Awaitable<Void> delay(long duration, TimeUnit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit must not be null");
        }
        if (duration < 0) {
            throw new IllegalArgumentException("delay duration must not be negative: " + duration);
        }
        if (duration == 0) {
            return Awaitable.of(null);
        }
        CompletableFuture<Void> cf = new CompletableFuture<>();
        DELAY_SCHEDULER.schedule(() -> cf.complete(null), duration, unit);
        return GroovyPromise.of(cf);
    }

    // ---- for-await support ----------------------------------------------

    /**
     * Converts an arbitrary source to an {@link AsyncStream} via the adapter
     * registry.  Used by compiler-generated code for {@code for await} loops.
     * Returns {@link AsyncStream#empty()} for {@code null} input to ensure
     * null-safe iteration.
     *
     * @param source the source object, or {@code null}
     * @param <T>    the element type
     * @return an async stream, never {@code null}
     */
    @SuppressWarnings("unchecked")
    public static <T> AsyncStream<T> toAsyncStream(Object source) {
        if (source == null) return AsyncStream.empty();
        if (source instanceof AsyncStream) return (AsyncStream<T>) source;
        return AsyncStream.from(source);
    }

    /**
     * Closes the given async stream if it is non-null.
     * <p>
     * Compiler-generated {@code for await} loops call this from a synthetic
     * {@code finally} block so that early {@code break}, {@code return}, or
     * exceptional exit reliably releases the underlying stream resources and
     * propagates cancellation to upstream producers where supported.
     *
     * @param stream the stream to close, or {@code null}
     * @since 6.0.0
     */
    public static void closeStream(Object stream) {
        if (stream instanceof AsyncStream<?> asyncStream) {
            asyncStream.close();
        }
    }

    // ---- yield return / async generator ----------------------------------

    /**
     * Safety overload: if {@code yield return} is used outside an async
     * generator context, this single-argument overload produces a clear
     * error message at runtime.
     *
     * @param value ignored — the method always throws
     * @throws IllegalStateException always
     */
    public static void yieldReturn(Object value) {
        throw new IllegalStateException(
                "yield return can only be used inside an async generator method or closure");
    }

    /**
     * Called by compiler-generated code for {@code yield return expr} statements
     * inside async generator methods and closures.
     *
     * @param generator the {@link AsyncStreamGenerator} instance (injected as a
     *                  synthetic parameter by the compiler)
     * @param value     the value to yield to the consumer
     * @throws IllegalStateException if {@code generator} is {@code null}
     */
    public static void yieldReturn(Object generator, Object value) {
        if (generator == null) {
            throw new IllegalStateException("yield return used outside of an async generator context");
        }
        ((AsyncStreamGenerator<?>) generator).yield(value);
    }

    /**
     * Creates an {@link AsyncStream} from a generator closure.  The closure
     * body is executed asynchronously on the default executor; each
     * {@code yield return} statement produces an element in the stream.
     * This method is used internally by {@link groovy.transform.Async @Async}
     * generator methods.  For the {@code async { yield return ... }} closure
     * expression syntax, see {@link #wrapAsyncGenerator(Closure)}.
     *
     * @param body the generator closure (receives an {@link AsyncStreamGenerator}
     *             as its single argument)
     * @param <T>  the element type
     * @return a live async stream backed by the running generator
     */
    @SuppressWarnings("unchecked")
    public static <T> AsyncStream<T> generateAsyncStream(Closure<?> body) {
        AsyncStreamGenerator<T> gen = new AsyncStreamGenerator<>();
        CompletableFuture.runAsync(() -> {
            gen.attachProducer(Thread.currentThread());
            try {
                body.call(gen);
                gen.complete();
            } catch (Throwable t) {
                gen.error(t);
            } finally {
                gen.detachProducer(Thread.currentThread());
            }
        }, defaultExecutor);
        return gen;
    }

    // ---- executor configuration -----------------------------------------

    /**
     * Returns {@code true} if the running JVM supports virtual threads (JDK 21+).
     */
    public static boolean isVirtualThreadsAvailable() {
        return VIRTUAL_THREADS_AVAILABLE;
    }

    /**
     * Returns the current executor used for {@code async} operations.
     *
     * @return the active executor, never {@code null}
     */
    public static Executor getExecutor() {
        return defaultExecutor;
    }

    /**
     * Sets the executor to use for {@code async} operations.  Pass {@code null}
     * to reset to the default executor (virtual thread executor on JDK 21+,
     * or a cached daemon thread pool whose maximum size is controlled by the
     * system property {@code groovy.async.parallelism}).
     *
     * @param executor the custom executor, or {@code null} to reset
     */
    public static void setExecutor(Executor executor) {
        defaultExecutor = executor != null
                ? executor
                : (VIRTUAL_THREADS_AVAILABLE ? VIRTUAL_THREAD_EXECUTOR : FALLBACK_EXECUTOR);
    }

    // ---- defer support -----------------------------------------------------

    /**
     * Creates a new defer scope — a LIFO list of {@link Closure} actions
     * to be executed when the enclosing method returns.  Called by
     * compiler-generated code at the start of methods that contain
     * {@code defer} statements.
     *
     * @return a new, empty defer scope (a mutable list of closures)
     */
    public static Deque<Closure<?>> createDeferScope() {
        return new ArrayDeque<>();
    }

    /**
     * Registers a deferred action in the given scope.  The action will be
     * executed in LIFO order when {@link #executeDeferScope} is called
     * (typically in a {@code finally} block generated by the compiler).
     * <p>
     * This method is the runtime entry point for the {@code defer { ... }}
     * statement, inspired by Go's {@code defer} keyword.
     *
     * @param scope  the defer scope (created by {@link #createDeferScope()});
     *               must not be {@code null}
     * @param action the closure to execute on method exit;
     *               must not be {@code null}
     * @throws IllegalStateException    if {@code scope} is {@code null}
     *         (indicates {@code defer} used outside an async context)
     * @throws IllegalArgumentException if {@code action} is {@code null}
     */
    public static void defer(Deque<Closure<?>> scope, Closure<?> action) {
        if (scope == null) {
            throw new IllegalStateException("defer must be used inside an async method or closure");
        }
        if (action == null) {
            throw new IllegalArgumentException("defer action must not be null");
        }
        scope.push(action);
    }

    /**
     * Executes all deferred actions in the given scope in LIFO order.
     * Each action is executed independently: if one throws an exception,
     * subsequent actions still execute.  If multiple actions throw, the
     * first exception is rethrown and subsequent ones are added as
     * {@linkplain Throwable#addSuppressed(Throwable) suppressed exceptions}.
     * <p>
     * Called by compiler-generated code in the {@code finally} block of
     * methods that contain {@code defer} statements.
     * <p>
     * A {@code null} or empty scope is treated as a no-op for robustness.
     *
     * @param scope the defer scope to execute; may be {@code null}
     */
    public static void executeDeferScope(Deque<Closure<?>> scope) {
        if (scope == null || scope.isEmpty()) return;
        Throwable firstError = null;
        while (!scope.isEmpty()) {
            try {
                scope.pop().call();
            } catch (Throwable t) {
                if (firstError == null) {
                    firstError = t;
                } else {
                    firstError.addSuppressed(t);
                }
            }
        }
        if (firstError != null) {
            sneakyThrow(firstError);
        }
    }

    // ---- internal -------------------------------------------------------

    /**
     * Deeply unwraps nested exception wrapper layers ({@link CompletionException},
     * {@link ExecutionException}, {@link UndeclaredThrowableException},
     * {@link InvocationTargetException}) to find the original root cause.
     * <p>
     * A depth limit of 64 is imposed to guard against circular cause chains,
     * which could otherwise cause an infinite loop.
     *
     * @param t the wrapper exception
     * @return the innermost non-wrapper exception, or the deepest reachable
     *         cause if the depth limit is reached
     */
    public static Throwable deepUnwrap(Throwable t) {
        Throwable cause = t;
        int depth = 0;
        while (cause.getCause() != null && depth++ < 64
                && (cause instanceof CompletionException
                    || cause instanceof ExecutionException
                    || cause instanceof UndeclaredThrowableException
                    || cause instanceof InvocationTargetException)) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Unwraps and rethrows the original exception transparently, bypassing
     * Java's checked-exception constraints via {@link #sneakyThrow}.
     */
    static RuntimeException rethrowUnwrapped(Throwable wrapper) {
        Throwable cause = deepUnwrap(wrapper);
        if (cause instanceof Error err) throw err;
        throw sneakyThrow(cause);
    }

    /**
     * Throws the given exception as an unchecked type using type-erasure.
     * Enables transparent propagation of checked exceptions from async code.
     */
    @SuppressWarnings("unchecked")
    static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Wraps a throwable in a {@link CompletionException} if it is not already one.
     * Used when rethrowing from within {@link CompletableFuture} lambdas.
     */
    static CompletionException wrapForFuture(Throwable t) {
        if (t instanceof CompletionException ce) return ce;
        return new CompletionException(t);
    }
}
