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
import groovy.concurrent.AwaitableAdapterRegistry;
import groovy.lang.Closure;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Internal runtime support for the {@code async}/{@code await} language feature
 * and the {@link groovy.transform.Async @Async} annotation.
 * <p>
 * This class contains the actual implementation invoked by compiler-generated
 * code.  User code should use the public-facing
 * {@link groovy.concurrent.AsyncUtils} facade instead.
 * <p>
 * All overloads of {@code await()} go through the
 * {@link AwaitableAdapterRegistry} so that third-party async types (RxJava
 * {@code Single}, Reactor {@code Mono}, etc.) are supported transparently
 * once an adapter is registered.
 * <p>
 * <b>Thread pool configuration</b>
 * <ul>
 *   <li>On JDK 21+ the default executor is a virtual-thread-per-task executor
 *       obtained via {@code Executors.newVirtualThreadPerTaskExecutor()}.</li>
 *   <li>On earlier JDKs the fallback is a dedicated fixed thread pool whose
 *       size is controlled by the system property {@code groovy.async.parallelism}
 *       (default: {@code availableProcessors + 1}).  All threads in this pool
 *       are daemon threads named {@code groovy-async-<id>}.</li>
 *   <li>The executor can be overridden at any time via {@link #setExecutor}.</li>
 * </ul>
 * <p>
 * <b>Exception handling</b> follows the same transparency principle as C# and
 * JavaScript: the <em>original</em> exception is rethrown without being wrapped
 * in an {@link java.util.concurrent.ExecutionException ExecutionException} or
 * {@link java.util.concurrent.CompletionException CompletionException}.
 *
 * @see groovy.concurrent.AsyncUtils
 * @see groovy.transform.Async
 * @see Awaitable
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
     * Fallback thread pool size when virtual threads are unavailable.
     * Configurable via the system property {@code groovy.async.parallelism}.
     * Defaults to {@code Runtime.getRuntime().availableProcessors() + 1}.
     */
    private static final int FALLBACK_PARALLELISM;

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

        FALLBACK_PARALLELISM = org.apache.groovy.util.SystemUtil.getIntegerSafe(
                "groovy.async.parallelism",
                Runtime.getRuntime().availableProcessors() + 1);
        if (!VIRTUAL_THREADS_AVAILABLE) {
            FALLBACK_EXECUTOR = Executors.newFixedThreadPool(FALLBACK_PARALLELISM, r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("groovy-async-" + t.getId());
                return t;
            });
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
     * Awaits an arbitrary object by adapting it to {@link Awaitable} via the
     * {@link AwaitableAdapterRegistry}.  This is the fallback overload called
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
        return await(AwaitableAdapterRegistry.<T>toAwaitable(source));
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
     * returning an {@link Awaitable}.  This is the runtime entry point for
     * the {@code async { ... }} expression syntax.
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
     * the {@code async { arg -> ... }} expression syntax (parameterised async
     * closures).
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
     * yielded elements.  This is the runtime entry point for parameterised
     * async generator closures.
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
                    try {
                        Object[] allArgs = new Object[args.length + 1];
                        allArgs[0] = gen;
                        System.arraycopy(args, 0, allArgs, 1, args.length);
                        closure.call(allArgs);
                        gen.complete();
                    } catch (Throwable t) {
                        gen.error(t);
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
     *
     * @param awaitables the awaitables (or futures, completion stages, etc.)
     * @return a list of results in the same order as the arguments
     */
    @SuppressWarnings("unchecked")
    public static List<Object> awaitAll(Object... awaitables) {
        CompletableFuture<?>[] futures = new CompletableFuture[awaitables.length];
        for (int i = 0; i < awaitables.length; i++) {
            futures[i] = toCompletableFuture(awaitables[i]);
        }
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
     * its result.  Analogous to JavaScript's {@code Promise.race()}.
     *
     * @param awaitables the awaitables to race
     * @return the result of the first awaitable that completes
     */
    @SuppressWarnings("unchecked")
    public static Object awaitAny(Object... awaitables) {
        CompletableFuture<?>[] futures = new CompletableFuture[awaitables.length];
        for (int i = 0; i < awaitables.length; i++) {
            futures[i] = toCompletableFuture(awaitables[i]);
        }
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
     *
     * @param awaitables the awaitables to settle
     * @return a list of results in the same order as the arguments
     */
    @SuppressWarnings("unchecked")
    public static List<AwaitResult<Object>> awaitAllSettled(Object... awaitables) {
        CompletableFuture<?>[] futures = new CompletableFuture[awaitables.length];
        for (int i = 0; i < awaitables.length; i++) {
            futures[i] = toCompletableFuture(awaitables[i]);
        }
        CompletableFuture.allOf(
            java.util.Arrays.stream(futures)
                .map(f -> f.handle((v, t) -> null))
                .toArray(CompletableFuture[]::new)
        ).join();

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

    /** Converts an arbitrary source to a {@link CompletableFuture} for internal use. */
    private static CompletableFuture<?> toCompletableFuture(Object source) {
        if (source instanceof CompletableFuture<?> cf) return cf;
        if (source instanceof Awaitable<?> a) return a.toCompletableFuture();
        if (source instanceof CompletionStage<?> cs) return cs.toCompletableFuture();
        return AwaitableAdapterRegistry.toAwaitable(source).toCompletableFuture();
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
        return AwaitableAdapterRegistry.toAsyncStream(source);
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
     * This is the runtime entry point for zero-parameter async generator
     * closures and {@link groovy.transform.Async @Async} generator methods.
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
            try {
                body.call(gen);
                gen.complete();
            } catch (Throwable t) {
                gen.error(t);
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
     * or a dedicated fixed thread pool whose size is controlled by the system
     * property {@code groovy.async.parallelism}).
     *
     * @param executor the custom executor, or {@code null} to reset
     */
    public static void setExecutor(Executor executor) {
        defaultExecutor = executor != null
                ? executor
                : (VIRTUAL_THREADS_AVAILABLE ? VIRTUAL_THREAD_EXECUTOR : FALLBACK_EXECUTOR);
    }

    // ---- internal -------------------------------------------------------

    /**
     * Deeply unwraps nested exception wrapper layers ({@link CompletionException},
     * {@link ExecutionException}, {@link UndeclaredThrowableException},
     * {@link InvocationTargetException}) to find the original root cause.
     *
     * @param t the wrapper exception
     * @return the innermost non-wrapper exception
     */
    public static Throwable deepUnwrap(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null
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
