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
import groovy.concurrent.AwaitableAdapterRegistry;

import java.io.Closeable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Internal runtime support for the {@code async}/{@code await}/{@code defer} language features.
 * <p>
 * This class is the entry point invoked by compiler-generated code and also
 * exposes the combinator and configuration surface used by
 * {@link groovy.concurrent.Awaitable}. Combinator algorithms live in
 * {@link AwaitCombinators}; executor/scheduler configuration lives in
 * {@link AsyncExecutors}.
 * <p>
 * <b>Thread pool configuration:</b>
 * <ul>
 *   <li>On JDK 21+ the default executor is a virtual-thread-per-task executor.
 *       Virtual threads make blocking within {@code await()} essentially free.</li>
 *   <li>On JDK 17-20 the fallback is a cached daemon thread pool
 *       whose maximum size is controlled by the system property
 *       {@code groovy.async.parallelism} (default: {@code 256}).</li>
 *   <li>The executor can be overridden via {@link #setExecutor}; pass
 *       {@code null} to restore the platform default.</li>
 * </ul>
 * <p>
 * <b>Exception handling</b> follows a transparency principle: the
 * <em>original</em> exception is rethrown without being wrapped.
 *
 * @see groovy.concurrent.Awaitable
 * @since 6.0.0
 */
public class AsyncSupport {

    private AsyncSupport() { }

    // ---- executor configuration -----------------------------------------

    /** Returns the shared scheduler for delays, timeouts, and scope deadlines. */
    public static ScheduledExecutorService getScheduler() {
        return AsyncExecutors.getScheduler();
    }

    /** Returns {@code true} if running on JDK 21+ with virtual thread support. */
    public static boolean isVirtualThreadsAvailable() {
        return AsyncExecutors.isVirtualThreadsAvailable();
    }

    /** Returns the current executor used for async tasks. */
    public static Executor getExecutor() {
        return AsyncExecutors.getExecutor();
    }

    /**
     * Sets the executor used for async tasks.
     * <p>
     * Pass {@code null} to restore the platform default (virtual threads on
     * JDK&nbsp;21+, cached daemon pool otherwise). The change takes effect
     * for subsequent {@code async} launches; in-flight tasks keep the
     * executor that started them.
     *
     * @param executor the executor to use, or {@code null} to reset
     */
    public static void setExecutor(Executor executor) {
        AsyncExecutors.setExecutor(executor);
    }

    /** Resets the executor to the platform default. Equivalent to {@code setExecutor(null)}. */
    public static void resetExecutor() {
        AsyncExecutors.resetExecutor();
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
            throw interruptedAwait("Interrupted while awaiting", e);
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
            throw interruptedAwait("Interrupted while awaiting future", e);
        }
    }

    /**
     * Awaits an arbitrary object by adapting it via {@link Awaitable#from(Object)}.
     * <p>
     * This is the fallback overload called by compiler-generated {@code await}
     * expressions. The compiler inserts a cast to {@link Object} so that
     * overload resolution does not have to choose among {@code Awaitable},
     * {@code CompletionStage}, and {@code Future} when a value implements more
     * than one of them (e.g. {@link CompletableFuture}).
     */
    @SuppressWarnings("unchecked")
    public static <T> T await(Object source) {
        if (source == null) return null;
        if (source instanceof Awaitable) return await((Awaitable<T>) source);
        if (source instanceof CompletableFuture) return await((CompletableFuture<T>) source);
        if (source instanceof CompletionStage) return await((CompletionStage<T>) source);
        if (source instanceof Future) return await((Future<T>) source);
        return await(Awaitable.from(source));
    }

    // ---- async execution ------------------------------------------------

    /**
     * Executes the given supplier asynchronously on the specified executor,
     * returning an {@link Awaitable}.
     */
    public static <T> Awaitable<T> executeAsync(Supplier<T> supplier, Executor executor) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        Executor targetExecutor = executor != null ? executor : AsyncExecutors.getExecutor();
        return GroovyPromise.of(CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Throwable t) {
                throw wrapForFuture(t);
            }
        }, targetExecutor));
    }

    /**
     * Executes the given supplier asynchronously using the default executor.
     */
    public static <T> Awaitable<T> async(Supplier<T> supplier) {
        return executeAsync(supplier, AsyncExecutors.getExecutor());
    }

    /**
     * Lightweight task spawn. Alias of {@link #async(Supplier)} for Go-style
     * ergonomics; semantics are identical.
     */
    public static <T> Awaitable<T> go(Supplier<T> supplier) {
        return async(supplier);
    }

    // ---- defer ----------------------------------------------------------

    /**
     * Creates a new defer scope (LIFO stack of cleanup actions).
     * Called by compiler-generated code at the start of closures
     * containing {@code defer} statements.
     */
    public static Deque<Callable<?>> createDeferScope() {
        return new ArrayDeque<>();
    }

    /**
     * Registers a deferred action in the given scope. Actions execute in LIFO
     * order when {@link #executeDeferScope} is called (in the finally block).
     */
    public static void defer(Deque<Callable<?>> scope,
                             Callable<?> action) {
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
    public static void executeDeferScope(Deque<Callable<?>> scope) {
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
            if (firstError instanceof RuntimeException re) throw re;
            if (firstError instanceof Error err) throw err;
            throw new RuntimeException(firstError);
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

    // ---- generators (yield return) ----------------------------------------

    /**
     * Called by compiler-generated code for {@code yield return expr} inside
     * an async generator closure. Delegates to the bridge's yield method.
     *
     * @param bridge the GeneratorBridge instance (injected as synthetic parameter)
     * @param value  the value to yield
     */
    public static void yieldReturn(Object bridge, Object value) {
        if (!(bridge instanceof GeneratorBridge<?>)) {
            throw new IllegalStateException("yield return can only be used inside an async generator");
        }
        ((GeneratorBridge<?>) bridge).yield(value);
    }

    /**
     * Starts a generator immediately, returning an {@link Iterable} backed by a
     * {@link GeneratorBridge}. The consumer receives the bridge as its argument
     * and should call {@link #yieldReturn} to produce values.
     * <p>
     * This is the runtime entry point for {@code async { ... yield return ... }}
     * expressions. The compiler generates a closure that SAM-coerces to
     * {@code Consumer<Object>}.
     *
     * @param body the generator body; receives a {@link GeneratorBridge}
     * @param <T>  the element type
     * @return an Iterable that yields values from the generator
     */
    public static <T> Iterable<T> asyncGenerator(Consumer<Object> body) {
        Objects.requireNonNull(body, "body must not be null");
        GeneratorBridge<T> bridge = new GeneratorBridge<>();
        AsyncExecutors.getExecutor().execute(() -> {
            try {
                body.accept(bridge);
                bridge.complete();
            } catch (GeneratorBridge.GeneratorClosedException ignored) {
                // Consumer closed early — normal for break in for-await
            } catch (Throwable t) {
                bridge.completeExceptionally(t);
            }
        });
        return () -> bridge;
    }

    // ---- for-await (iterable conversion) --------------------------------

    /**
     * Converts an arbitrary source to an {@link Iterable} for use in
     * {@code for await} loops. Handles arrays, collections, iterables,
     * iterators, and adapter-supported types. The returned iterable may
     * block on {@code next()} for async sources.
     *
     * @param source the source to convert
     * @param <T>    the element type
     * @return an iterable
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> toIterable(Object source) {
        if (source == null) return Collections.emptyList();
        if (source instanceof Iterable) return (Iterable<T>) source;
        if (source instanceof Iterator) {
            Iterator<T> iter = (Iterator<T>) source;
            return () -> iter;
        }
        if (source instanceof Object[]) return (Iterable<T>) Arrays.asList((Object[]) source);
        return AwaitableAdapterRegistry.toIterable(source);
    }

    /**
     * Closes a source if it implements {@link Closeable} or
     * {@link AutoCloseable}. Called by compiler-generated finally block
     * in {@code for await} loops. Cleanup exceptions are swallowed so they
     * cannot mask the original loop error; prefer robust {@code close()}
     * implementations.
     */
    public static void closeIterable(Object source) {
        if (source instanceof Closeable c) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        } else if (source instanceof AutoCloseable c) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }

    // ---- combinators (delegate to AwaitCombinators) ---------------------

    /**
     * Waits for all given sources to complete, returning their results in order.
     * Multi-arg {@code await(a, b, c)} desugars to the non-blocking
     * {@link #allAsync(Object...)} form, then awaits it.
     */
    public static <T> List<T> all(Object... sources) {
        return AwaitCombinators.all(sources);
    }

    /**
     * Returns the result of the first source to complete (success or failure).
     */
    public static <T> T any(Object... sources) {
        return AwaitCombinators.any(sources);
    }

    /**
     * Returns the result of the first source to complete <em>successfully</em>.
     * Only fails when all sources fail (aggregate {@link CompletionException};
     * {@code await} transparency rethrows the cause).
     */
    public static <T> T first(Object... sources) {
        return AwaitCombinators.first(sources);
    }

    /**
     * Waits for all sources to settle (succeed or fail), returning a list of
     * {@link AwaitResult} without throwing.
     */
    public static List<AwaitResult<Object>> allSettled(Object... sources) {
        return AwaitCombinators.allSettled(sources);
    }

    /** Non-blocking variant of {@link #all} — returns an Awaitable. */
    public static Awaitable<List<Object>> allAsync(Object... sources) {
        return AwaitCombinators.allAsync(sources);
    }

    /** Non-blocking variant of {@link #any} — returns an Awaitable. */
    public static <T> Awaitable<T> anyAsync(Object... sources) {
        return AwaitCombinators.anyAsync(sources);
    }

    /** Non-blocking variant of {@link #first} — returns an Awaitable. */
    public static <T> Awaitable<T> firstAsync(Object... sources) {
        return AwaitCombinators.firstAsync(sources);
    }

    /** Non-blocking variant of {@link #allSettled} — returns an Awaitable. */
    public static Awaitable<List<AwaitResult<Object>>> allSettledAsync(Object... sources) {
        return AwaitCombinators.allSettledAsync(sources);
    }

    // ---- delay and timeout ----------------------------------------------

    /**
     * Returns an Awaitable that completes after the specified delay.
     */
    public static Awaitable<Void> delay(long millis) {
        return delay(millis, TimeUnit.MILLISECONDS);
    }

    /** Delay with explicit time unit. */
    public static Awaitable<Void> delay(long duration, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        AsyncExecutors.getScheduler().schedule(() -> future.complete(null), duration, unit);
        return GroovyPromise.of(future);
    }

    /**
     * Wraps a source with a timeout. If the source does not complete within
     * the specified time, the returned Awaitable fails with {@link TimeoutException}
     * and the underlying computation is cancelled.
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> orTimeout(Object source, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = (CompletableFuture<T>) Awaitable.from(source).toCompletableFuture();
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledFuture<?> timer = AsyncExecutors.getScheduler().schedule(() -> {
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
     * On timeout the underlying computation is cancelled.
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> completeOnTimeout(Object source, T fallback, long timeout, TimeUnit unit) {
        CompletableFuture<T> future = (CompletableFuture<T>) Awaitable.from(source).toCompletableFuture();
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledFuture<?> timer = AsyncExecutors.getScheduler().schedule(() -> {
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

    // ---- exception utilities --------------------------------------------

    public static CompletionException wrapForFuture(Throwable t) {
        if (t instanceof CompletionException ce) return ce;
        return new CompletionException(t);
    }

    private static RuntimeException rethrowUnwrapped(Throwable wrapper) {
        Throwable cause = unwrap(wrapper);
        sneakyThrow(cause);
        return null; // unreachable
    }

    /**
     * Strips JDK wrapper layers ({@link CompletionException},
     * {@link ExecutionException}, {@link InvocationTargetException},
     * {@link UndeclaredThrowableException}) to expose the original cause.
     */
    public static Throwable unwrap(Throwable t) {
        while ((t instanceof CompletionException || t instanceof ExecutionException
                || t instanceof InvocationTargetException
                || t instanceof UndeclaredThrowableException)
                && t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    // ---- internal utilities ---------------------------------------------

    private static CancellationException interruptedAwait(String message, InterruptedException cause) {
        Thread.currentThread().interrupt();
        CancellationException cancellation = new CancellationException(message);
        cancellation.initCause(cause);
        return cancellation;
    }
}
