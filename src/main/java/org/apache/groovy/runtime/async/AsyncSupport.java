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

import groovy.concurrent.AwaitResult;
import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
 * Internal runtime support for the {@code async}/{@code await}/{@code defer} language features.
 * <p>
 * This class contains the actual implementation invoked by compiler-generated code.
 * User code should prefer the static methods on {@link groovy.concurrent.Awaitable}
 * for combinators and configuration.
 * <p>
 * <b>Thread pool configuration:</b>
 * <ul>
 *   <li>On JDK 21+ the default executor is a virtual-thread-per-task executor.
 *       Virtual threads make blocking within {@code await()} essentially free.</li>
 *   <li>On JDK 17-20 the fallback is a cached daemon thread pool
 *       whose maximum size is controlled by the system property
 *       {@code groovy.async.parallelism} (default: {@code 256}).</li>
 *   <li>The executor can be overridden at any time via {@link #setExecutor}.</li>
 * </ul>
 * <p>
 * <b>Exception handling</b> follows a transparency principle: the
 * <em>original</em> exception is rethrown without being wrapped.
 *
 * @see groovy.concurrent.Awaitable
 * @since 6.0.0
 */
public class AsyncSupport {

    private static final boolean VIRTUAL_THREADS_AVAILABLE;
    private static final Executor VIRTUAL_THREAD_EXECUTOR;
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

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "groovy-async-scheduler");
                t.setDaemon(true);
                return t;
            });

    private AsyncSupport() { }

    // ---- executor configuration -----------------------------------------

    /** Returns {@code true} if running on JDK 21+ with virtual thread support. */
    public static boolean isVirtualThreadsAvailable() {
        return VIRTUAL_THREADS_AVAILABLE;
    }

    /** Returns the current executor used for async tasks. */
    public static Executor getExecutor() {
        return defaultExecutor;
    }

    /** Sets the executor used for async tasks. */
    public static void setExecutor(Executor executor) {
        defaultExecutor = Objects.requireNonNull(executor, "executor must not be null");
    }

    /** Resets the executor to the default (virtual threads on JDK 21+, cached pool otherwise). */
    public static void resetExecutor() {
        defaultExecutor = VIRTUAL_THREADS_AVAILABLE ? VIRTUAL_THREAD_EXECUTOR : FALLBACK_EXECUTOR;
    }

    // ---- await overloads ------------------------------------------------

    /**
     * Awaits the result of an {@link Awaitable}.
     * Blocks the calling thread until the computation completes.
     * The original exception is rethrown transparently.
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

    /** Awaits a {@link CompletableFuture} using non-interruptible {@code join()}. */
    public static <T> T await(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            throw rethrowUnwrapped(e);
        } catch (CancellationException e) {
            throw e;
        }
    }

    /** Awaits a {@link CompletionStage} by converting to CompletableFuture. */
    public static <T> T await(CompletionStage<T> stage) {
        return await(stage.toCompletableFuture());
    }

    /** Awaits a {@link Future}. Delegates to the CF overload if applicable. */
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
     * Awaits an arbitrary object by adapting it via {@link Awaitable#from(Object)}.
     * This is the fallback overload called by compiler-generated await expressions.
     */
    @SuppressWarnings("unchecked")
    public static <T> T await(Object source) {
        if (source == null) return null;
        if (source instanceof Awaitable) return await((Awaitable<T>) source);
        if (source instanceof CompletableFuture) return await((CompletableFuture<T>) source);
        if (source instanceof CompletionStage) return await((CompletionStage<T>) source);
        if (source instanceof Future) return await((Future<T>) source);
        if (source instanceof Closure) {
            throw new IllegalArgumentException(
                    "Cannot await a Closure directly. Call the closure first: await myClosure()");
        }
        return await(Awaitable.from(source));
    }

    // ---- async execution ------------------------------------------------

    /**
     * Executes the given closure asynchronously on the specified executor,
     * returning an {@link Awaitable}.
     */
    public static <T> Awaitable<T> executeAsync(Closure<T> closure, Executor executor) {
        Objects.requireNonNull(closure, "closure must not be null");
        Executor targetExecutor = executor != null ? executor : defaultExecutor;
        return GroovyPromise.of(CompletableFuture.supplyAsync(() -> {
            try {
                return closure.call();
            } catch (Throwable t) {
                throw wrapForFuture(t);
            }
        }, targetExecutor));
    }

    /**
     * Executes the given closure asynchronously using the default executor.
     */
    public static <T> Awaitable<T> async(Closure<T> closure) {
        return executeAsync(closure, defaultExecutor);
    }

    /**
     * Lightweight task spawn. Executes the closure asynchronously using the default executor.
     */
    public static <T> Awaitable<T> go(Closure<T> closure) {
        return executeAsync(closure, defaultExecutor);
    }

    /**
     * Wraps a closure so that each invocation executes the body asynchronously
     * and returns an {@link Awaitable}. This is the runtime entry point for
     * the {@code async { ... }} expression syntax.
     * <pre>
     * def task = async { expensiveWork() }
     * def result = await task()  // explicit call required
     * </pre>
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Awaitable<T>> wrapAsync(Closure<T> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
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

    // ---- generators (yield return) ----------------------------------------

    /**
     * Called by compiler-generated code for {@code yield return expr} inside
     * an async generator closure. Delegates to the bridge's yield method.
     *
     * @param bridge the GeneratorBridge instance (injected as synthetic parameter)
     * @param value  the value to yield
     */
    public static void yieldReturn(Object bridge, Object value) {
        ((GeneratorBridge<?>) bridge).yield(value);
    }

    /**
     * Wraps a generator closure so that each invocation returns an {@link Iterable}
     * backed by a {@link GeneratorBridge}. The generator runs on a background thread
     * and yields values via the bridge.
     * <p>
     * This is the runtime entry point for {@code async { ... yield return ... }}
     * expressions that contain {@code yield return}.
     *
     * @param closure the generator closure; receives a GeneratorBridge as first parameter
     * @param <T>     the element type
     * @return a closure that produces an Iterable when called
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Iterable<T>> wrapAsyncGenerator(Closure<?> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        return new Closure<Iterable<T>>(closure.getOwner(), closure.getThisObject()) {
            @SuppressWarnings("unused")
            public Iterable<T> doCall(Object... args) {
                GeneratorBridge<T> bridge = new GeneratorBridge<>();
                Object[] allArgs = new Object[args.length + 1];
                allArgs[0] = bridge;
                System.arraycopy(args, 0, allArgs, 1, args.length);
                defaultExecutor.execute(() -> {
                    try {
                        closure.call(allArgs);
                        bridge.complete();
                    } catch (GeneratorBridge.GeneratorClosedException ignored) {
                        // Consumer closed early — normal for break in for-await
                    } catch (Throwable t) {
                        bridge.completeExceptionally(t);
                    }
                });
                return () -> bridge;
            }
        };
    }

    /**
     * Starts a generator immediately, returning an {@link Iterable} backed by a
     * {@link GeneratorBridge}. This is the runtime entry point for
     * {@code async { ... yield return ... }} expressions.
     *
     * @param closure the generator closure; receives a GeneratorBridge as first parameter
     * @param <T>     the element type
     * @return an Iterable that yields values from the generator
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> asyncGenerator(Closure<?> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        GeneratorBridge<T> bridge = new GeneratorBridge<>();
        Object[] args = new Object[]{bridge};
        defaultExecutor.execute(() -> {
            try {
                closure.call(args);
                bridge.complete();
            } catch (GeneratorBridge.GeneratorClosedException ignored) {
                // Consumer closed early — normal for break in for-await
            } catch (Throwable t) {
                bridge.completeExceptionally(t);
            }
        });
        return () -> bridge;
    }

    // ---- for-await (blocking iterable conversion) -----------------------

    /**
     * Converts an arbitrary source to a blocking {@link Iterable} for use
     * in {@code for await} loops. Handles arrays, collections, iterables,
     * iterators, and adapter-supported types.
     *
     * @param source the source to convert
     * @param <T>    the element type
     * @return a blocking iterable
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> toBlockingIterable(Object source) {
        if (source == null) return Collections.emptyList();
        if (source instanceof Iterable) return (Iterable<T>) source;
        if (source instanceof Iterator) {
            Iterator<T> iter = (Iterator<T>) source;
            return () -> iter;
        }
        if (source instanceof Object[]) return (Iterable<T>) Arrays.asList((Object[]) source);
        // Try adapter registry
        return groovy.concurrent.AwaitableAdapterRegistry.toBlockingIterable(source);
    }

    /**
     * Closes a source if it implements {@link java.io.Closeable} or
     * {@link AutoCloseable}. Called by compiler-generated finally block
     * in {@code for await} loops.
     */
    public static void closeIterable(Object source) {
        if (source instanceof java.io.Closeable c) {
            try { c.close(); } catch (Exception ignored) { }
        } else if (source instanceof AutoCloseable c) {
            try { c.close(); } catch (Exception ignored) { }
        }
    }

    // ---- combinators ----------------------------------------------------

    /**
     * Waits for all given sources to complete, returning their results in order.
     * Multi-arg {@code await(a, b, c)} desugars to this.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> all(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).join();
        List<T> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> f : futures) {
            results.add((T) f.join());
        }
        return results;
    }

    /**
     * Returns the result of the first source to complete (success or failure).
     */
    @SuppressWarnings("unchecked")
    public static <T> T any(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        return (T) CompletableFuture.anyOf(futures).join();
    }

    /**
     * Returns the result of the first source to complete <em>successfully</em>.
     * Only fails when all sources fail.
     */
    @SuppressWarnings("unchecked")
    public static <T> T first(Object... sources) {
        CompletableFuture<T>[] futures = Arrays.stream(sources)
                .map(s -> (CompletableFuture<T>) Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);

        CompletableFuture<T> result = new CompletableFuture<>();
        var remaining = new java.util.concurrent.atomic.AtomicInteger(futures.length);
        List<Throwable> errors = java.util.Collections.synchronizedList(new ArrayList<>());
        for (CompletableFuture<T> f : futures) {
            f.whenComplete((value, error) -> {
                if (error == null) {
                    result.complete(value);
                } else {
                    errors.add(error);
                    if (remaining.decrementAndGet() == 0) {
                        CompletionException aggregate = new CompletionException(
                                "All " + futures.length + " tasks failed", errors.get(0));
                        for (int i = 1; i < errors.size(); i++) {
                            aggregate.addSuppressed(errors.get(i));
                        }
                        result.completeExceptionally(aggregate);
                    }
                }
            });
        }
        try {
            return result.join();
        } catch (CompletionException e) {
            throw rethrowUnwrapped(e);
        }
    }

    /**
     * Waits for all sources to settle (succeed or fail), returning a list of
     * {@link AwaitResult} without throwing.
     */
    @SuppressWarnings("unchecked")
    public static List<AwaitResult<Object>> allSettled(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(
                Arrays.stream(futures)
                        .map(f -> f.handle((v, e) -> null))
                        .toArray(CompletableFuture[]::new)
        ).join();
        List<AwaitResult<Object>> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> f : futures) {
            try {
                results.add(AwaitResult.success(f.join()));
            } catch (CompletionException e) {
                results.add(AwaitResult.failure(unwrap(e)));
            } catch (CancellationException e) {
                results.add(AwaitResult.failure(e));
            }
        }
        return results;
    }

    // ---- async combinator variants (return Awaitable, non-blocking) ------

    /** Non-blocking variant of {@link #all} — returns an Awaitable. */
    @SuppressWarnings("unchecked")
    public static Awaitable<List<Object>> allAsync(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);

        // Track the temporally-first failure explicitly, since
        // CompletableFuture.allOf() doesn't guarantee which exception
        // propagates when multiple futures fail.
        var firstError = new java.util.concurrent.atomic.AtomicReference<Throwable>();
        for (CompletableFuture<?> f : futures) {
            f.whenComplete((v, e) -> {
                if (e != null) firstError.compareAndSet(null, e);
            });
        }

        CompletableFuture<List<Object>> combined = CompletableFuture.allOf(
                Arrays.stream(futures).map(f -> f.handle((v, e) -> null)).toArray(CompletableFuture[]::new)
        ).thenApply(v -> {
            Throwable err = firstError.get();
            if (err != null) throw err instanceof CompletionException ce ? ce : new CompletionException(err);
            List<Object> results = new ArrayList<>(futures.length);
            for (CompletableFuture<?> f : futures) results.add(f.join());
            return results;
        });
        return GroovyPromise.of(combined);
    }

    /** Non-blocking variant of {@link #any} — returns an Awaitable. */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> anyAsync(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        return (Awaitable<T>) GroovyPromise.of(CompletableFuture.anyOf(futures));
    }

    /** Non-blocking variant of {@link #first} — returns an Awaitable. */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> firstAsync(Object... sources) {
        CompletableFuture<T>[] futures = Arrays.stream(sources)
                .map(s -> (CompletableFuture<T>) Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        CompletableFuture<T> result = new CompletableFuture<>();
        var remaining = new java.util.concurrent.atomic.AtomicInteger(futures.length);
        List<Throwable> errors = java.util.Collections.synchronizedList(new ArrayList<>());
        for (CompletableFuture<T> f : futures) {
            f.whenComplete((value, error) -> {
                if (error == null) {
                    result.complete(value);
                } else {
                    errors.add(error);
                    if (remaining.decrementAndGet() == 0) {
                        CompletionException aggregate = new CompletionException(
                                "All " + futures.length + " tasks failed", errors.get(0));
                        for (int i = 1; i < errors.size(); i++) {
                            aggregate.addSuppressed(errors.get(i));
                        }
                        result.completeExceptionally(aggregate);
                    }
                }
            });
        }
        return GroovyPromise.of(result);
    }

    /** Non-blocking variant of {@link #allSettled} — returns an Awaitable. */
    @SuppressWarnings("unchecked")
    public static Awaitable<List<AwaitResult<Object>>> allSettledAsync(Object... sources) {
        CompletableFuture<?>[] futures = Arrays.stream(sources)
                .map(s -> Awaitable.from(s).toCompletableFuture())
                .toArray(CompletableFuture[]::new);
        CompletableFuture<List<AwaitResult<Object>>> combined = CompletableFuture.allOf(
                Arrays.stream(futures).map(f -> f.handle((v, e) -> null)).toArray(CompletableFuture[]::new)
        ).thenApply(v -> {
            List<AwaitResult<Object>> results = new ArrayList<>(futures.length);
            for (CompletableFuture<?> f : futures) {
                try {
                    results.add(AwaitResult.success(f.join()));
                } catch (CompletionException e) {
                    results.add(AwaitResult.failure(unwrap(e)));
                } catch (CancellationException e) {
                    results.add(AwaitResult.failure(e));
                }
            }
            return results;
        });
        return GroovyPromise.of(combined);
    }

    // ---- delay and timeout ----------------------------------------------

    /**
     * Returns an Awaitable that completes after the specified delay.
     */
    public static Awaitable<Void> delay(long millis) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SCHEDULER.schedule(() -> future.complete(null), millis, TimeUnit.MILLISECONDS);
        return GroovyPromise.of(future);
    }

    /** Delay with explicit time unit. */
    public static Awaitable<Void> delay(long duration, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        SCHEDULER.schedule(() -> future.complete(null), duration, unit);
        return GroovyPromise.of(future);
    }

    /**
     * Wraps a source with a timeout. If the source does not complete within
     * the specified time, the returned Awaitable fails with {@link TimeoutException}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> orTimeout(Object source, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = (CompletableFuture<T>) Awaitable.from(source).toCompletableFuture();
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledFuture<?> timer = SCHEDULER.schedule(() -> {
            if (!result.isDone()) {
                result.completeExceptionally(new TimeoutException("Timed out after " + timeout + " " + unit));
                future.cancel(true);
            }
        }, timeout, unit);
        future.whenComplete((v, e) -> {
            timer.cancel(false);
            if (e != null) result.completeExceptionally(e);
            else result.complete(v);
        });
        return GroovyPromise.of(result);
    }

    /** Convenience: timeout in milliseconds. */
    public static <T> Awaitable<T> orTimeoutMillis(Object source, long millis) {
        return orTimeout(source, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Wraps a source with a timeout that uses a fallback value instead of throwing.
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> completeOnTimeout(Object source, T fallback, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = (CompletableFuture<T>) Awaitable.from(source).toCompletableFuture();
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledFuture<?> timer = SCHEDULER.schedule(() -> {
            if (!result.isDone()) {
                result.complete(fallback);
                future.cancel(true);
            }
        }, timeout, unit);
        future.whenComplete((v, e) -> {
            timer.cancel(false);
            if (e != null) result.completeExceptionally(e);
            else result.complete(v);
        });
        return GroovyPromise.of(result);
    }

    /** Convenience: timeout in milliseconds. */
    public static <T> Awaitable<T> completeOnTimeoutMillis(Object source, T fallback, long millis) {
        return completeOnTimeout(source, fallback, millis, TimeUnit.MILLISECONDS);
    }

    // ---- defer ----------------------------------------------------------

    /**
     * Creates a new defer scope (LIFO stack of cleanup actions).
     * Called by compiler-generated code at the start of closures
     * containing {@code defer} statements.
     */
    public static Deque<Closure<?>> createDeferScope() {
        return new ArrayDeque<>();
    }

    /**
     * Registers a deferred action in the given scope. Actions execute in LIFO
     * order when {@link #executeDeferScope} is called (in the finally block).
     */
    public static void defer(Deque<Closure<?>> scope, Closure<?> action) {
        if (scope == null) {
            throw new IllegalStateException("defer must be used inside an async closure");
        }
        if (action == null) {
            throw new IllegalArgumentException("defer action must not be null");
        }
        scope.push(action);
    }

    /**
     * Executes all deferred actions in LIFO order. If multiple actions throw,
     * subsequent exceptions are added as suppressed. If a deferred action returns
     * a Future/Awaitable, the result is awaited before continuing.
     */
    public static void executeDeferScope(Deque<Closure<?>> scope) {
        if (scope == null || scope.isEmpty()) return;
        Throwable firstError = null;
        while (!scope.isEmpty()) {
            try {
                Object result = scope.pop().call();
                awaitDeferredResult(result);
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

    @SuppressWarnings("unchecked")
    private static void awaitDeferredResult(Object result) {
        if (result instanceof Awaitable<?>) {
            await((Awaitable<Object>) result);
        } else if (result instanceof CompletionStage<?>) {
            await(((CompletionStage<Object>) result).toCompletableFuture());
        } else if (result instanceof Future<?>) {
            await((Future<Object>) result);
        }
    }

    // ---- exception utilities --------------------------------------------

    private static CompletionException wrapForFuture(Throwable t) {
        if (t instanceof CompletionException ce) return ce;
        return new CompletionException(t);
    }

    static RuntimeException rethrowUnwrapped(Throwable wrapper) {
        Throwable cause = unwrap(wrapper);
        sneakyThrow(cause);
        return null; // unreachable
    }

    public static Throwable unwrap(Throwable t) {
        while ((t instanceof CompletionException || t instanceof ExecutionException
                || t instanceof java.lang.reflect.InvocationTargetException
                || t instanceof java.lang.reflect.UndeclaredThrowableException)
                && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
