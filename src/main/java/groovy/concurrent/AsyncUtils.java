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

import groovy.lang.Closure;
import org.apache.groovy.runtime.async.AsyncSupport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * User-facing API for Groovy's {@code async}/{@code await} language feature.
 * <p>
 * Provides methods for awaiting asynchronous results, creating async
 * computations, and configuring the async executor.  Inside {@code async}
 * methods the {@code await expr} expression is compiled to call the internal
 * runtime support directly, but users may also call these methods explicitly:
 * <pre>
 * import static groovy.concurrent.AsyncUtils.*
 *
 * def future = async {
 *     def result = await someAsyncCall()
 *     return result * 2
 * }
 * </pre>
 * <p>
 * <b>Thread pool configuration:</b> On JDK 21+ the default executor uses
 * virtual threads.  On earlier JDKs a dedicated daemon thread pool is used,
 * whose size is controlled by the system property
 * {@code groovy.async.parallelism} (default: {@code availableProcessors + 1}).
 * The executor can be overridden at any time via {@link #setExecutor}.
 *
 * @see groovy.transform.Async
 * @see Awaitable
 * @see AwaitableAdapterRegistry
 * @since 6.0.0
 */
public class AsyncUtils {

    private AsyncUtils() { }

    // ---- await overloads ------------------------------------------------

    /**
     * Awaits the result of an {@link Awaitable}.
     * <p>
     * Exception handling follows the same principle as C# and JavaScript:
     * the <em>original</em> exception is rethrown transparently, even if it
     * is a checked exception.
     */
    public static <T> T await(Awaitable<T> awaitable) {
        return AsyncSupport.await(awaitable);
    }

    /**
     * Awaits the result of a {@link CompletableFuture}.
     */
    public static <T> T await(CompletableFuture<T> future) {
        return AsyncSupport.await(future);
    }

    /**
     * Awaits the result of a {@link CompletionStage}.
     */
    public static <T> T await(CompletionStage<T> stage) {
        return AsyncSupport.await(stage);
    }

    /**
     * Awaits the result of a {@link Future}.
     */
    public static <T> T await(Future<T> future) {
        return AsyncSupport.await(future);
    }

    /**
     * Awaits an arbitrary object by adapting it to {@link Awaitable} via the
     * {@link AwaitableAdapterRegistry}.
     */
    public static <T> T await(Object source) {
        return AsyncSupport.await(source);
    }

    // ---- async ----------------------------------------------------------

    /**
     * Executes the given closure asynchronously using the default executor,
     * returning an {@link Awaitable}.
     */
    public static <T> Awaitable<T> async(Closure<T> closure) {
        return AsyncSupport.async(closure);
    }

    // ---- awaitAll / awaitAny / awaitAllSettled ---------------------------

    /**
     * Waits for all given awaitables and returns their results as a list.
     * Analogous to JavaScript's {@code Promise.all()}.
     */
    public static List<Object> awaitAll(Object... awaitables) {
        return AsyncSupport.awaitAll(awaitables);
    }

    /**
     * Waits for any one of the given awaitables and returns its result.
     * Analogous to JavaScript's {@code Promise.race()}.
     */
    public static Object awaitAny(Object... awaitables) {
        return AsyncSupport.awaitAny(awaitables);
    }

    /**
     * Waits for all given awaitables and returns a list of {@link AwaitResult}
     * objects. Analogous to JavaScript's {@code Promise.allSettled()}.
     */
    public static List<AwaitResult<Object>> awaitAllSettled(Object... awaitables) {
        return AsyncSupport.awaitAllSettled(awaitables);
    }

    // ---- for-await support ----------------------------------------------

    /**
     * Converts an arbitrary source to an {@link AsyncStream} via the adapter
     * registry. Useful for manual iteration with {@code for await} loops.
     * Returns an empty stream for {@code null} input.
     */
    public static <T> AsyncStream<T> toAsyncStream(Object source) {
        return AsyncSupport.toAsyncStream(source);
    }

    // ---- async execution ------------------------------------------------

    /**
     * Executes the given closure asynchronously on the specified executor,
     * returning an {@link Awaitable}.
     */
    public static <T> Awaitable<T> executeAsync(Closure<T> closure, Executor executor) {
        return AsyncSupport.executeAsync(closure, executor);
    }

    /**
     * Void variant of {@link #executeAsync(Closure, Executor)} for void methods.
     */
    public static Awaitable<Void> executeAsyncVoid(Closure<?> closure, Executor executor) {
        return AsyncSupport.executeAsyncVoid(closure, executor);
    }

    // ---- exception utilities --------------------------------------------

    /**
     * Deeply unwraps nested exception wrapper layers to find the original exception.
     */
    public static Throwable deepUnwrap(Throwable t) {
        return AsyncSupport.deepUnwrap(t);
    }

    // ---- executor configuration -----------------------------------------

    /**
     * Returns {@code true} if the running JVM supports virtual threads (JDK 21+).
     */
    public static boolean isVirtualThreadsAvailable() {
        return AsyncSupport.isVirtualThreadsAvailable();
    }

    /**
     * Returns the current executor used for async operations.
     */
    public static Executor getExecutor() {
        return AsyncSupport.getExecutor();
    }

    /**
     * Sets the executor to use for async operations. Pass {@code null}
     * to reset to the default (virtual thread executor on JDK 21+,
     * or a dedicated daemon thread pool whose size is controlled by the
     * system property {@code groovy.async.parallelism}).
     */
    public static void setExecutor(Executor executor) {
        AsyncSupport.setExecutor(executor);
    }
}
