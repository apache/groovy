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
import org.apache.groovy.runtime.async.DefaultAsyncScope;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A structured concurrency scope that ensures all child tasks complete
 * (or are cancelled) before the scope exits.
 * <p>
 * {@code AsyncScope} provides a bounded lifetime for async tasks,
 * following the <em>structured concurrency</em> model. Unlike
 * fire-and-forget {@code async { ... }}, tasks launched within a scope
 * are guaranteed to complete before the scope closes. This prevents:
 * <ul>
 *   <li>Orphaned tasks that outlive their logical parent</li>
 *   <li>Resource leaks from uncollected async work</li>
 *   <li>Silent failures from unobserved exceptions</li>
 * </ul>
 * <p>
 * By default, the scope uses a <b>fail-fast</b> policy: when any child
 * task completes exceptionally, all sibling tasks are cancelled
 * immediately. The first failure becomes the primary exception;
 * subsequent failures are added as suppressed exceptions.
 *
 * <pre>{@code
 * def results = AsyncScope.withScope { scope ->
 *     def userTask  = scope.async { fetchUser(id) }
 *     def orderTask = scope.async { fetchOrders(id) }
 *     return [user: await(userTask), orders: await(orderTask)]
 * }
 * // Both tasks guaranteed complete here
 * }</pre>
 *
 * @see Awaitable
 * @since 6.0.0
 */
public interface AsyncScope extends AutoCloseable {

    /**
     * Launches a child task within this scope.
     * The task's lifetime is bound to the scope: when the scope is closed,
     * all incomplete child tasks are cancelled.
     *
     * @param supplier the task body to execute
     * @param <T>      the result type
     * @return an {@link Awaitable} representing the child task
     * @throws IllegalStateException if the scope has already been closed
     */
    <T> Awaitable<T> async(Supplier<T> supplier);

    /**
     * Returns the parent scope, or {@code null} if this is a root scope.
     * <p>
     * When a scope is created inside another scope (via {@link #withScope}),
     * the outer scope becomes the parent. Cancelling a parent scope
     * propagates cancellation to all child scopes.
     *
     * @return the parent scope, or {@code null}
     * @since 6.0.0
     */
    AsyncScope getParent();

    /**
     * Returns the number of tracked child tasks (including completed ones
     * that have not yet been pruned).
     */
    int getChildCount();

    /**
     * Cancels all child tasks.
     */
    void cancelAll();

    /**
     * Closes the scope, waiting for all child tasks to complete.
     * If any child failed and fail-fast is enabled, remaining children
     * are cancelled and the first failure is rethrown.
     */
    @Override
    void close();

    // ---- Static methods -------------------------------------------------

    /**
     * Returns the scope currently bound to this thread, or {@code null}.
     */
    static AsyncScope current() {
        return DefaultAsyncScope.current();
    }

    /**
     * Executes the supplier with the given scope installed as current,
     * restoring the previous binding afterwards.
     */
    static <T> T withCurrent(AsyncScope scope, Supplier<T> supplier) {
        return DefaultAsyncScope.withCurrent(scope, supplier);
    }

    /**
     * Creates a scope, executes the body within it, and ensures the
     * scope is closed on exit. The body receives the scope as its
     * argument for launching child tasks.
     *
     * <pre>{@code
     * // Java:
     * var result = AsyncScope.withScope(scope -> {
     *     var a = scope.async(() -> computeA());
     *     var b = scope.async(() -> computeB());
     *     return List.of(AsyncSupport.await(a), AsyncSupport.await(b));
     * });
     *
     * // Groovy (Closure overload added via extension method):
     * def result = AsyncScope.withScope { scope ->
     *     def a = scope.async { computeA() }
     *     def b = scope.async { computeB() }
     *     return [await(a), await(b)]
     * }
     * }</pre>
     *
     * @param body the function to execute within the scope
     * @param <T>  the result type
     * @return the body's return value
     */
    static <T> T withScope(Function<AsyncScope, T> body) {
        return withScope(AsyncSupport.getExecutor(), body);
    }

    /**
     * Creates a scope with the given executor, executes the body,
     * and ensures the scope is closed on exit.
     *
     * @param executor the executor for child tasks
     * @param body     the function to execute within the scope
     * @param <T>      the result type
     * @return the body's return value
     */
    static <T> T withScope(Executor executor, Function<AsyncScope, T> body) {
        Objects.requireNonNull(body, "body must not be null");
        AsyncScope scope = create(executor);
        T result;
        try {
            result = withCurrent(scope, () -> body.apply(scope));
        } catch (Throwable bodyError) {
            try {
                scope.close();
            } catch (Throwable closeError) {
                if (closeError != bodyError) {
                    bodyError.addSuppressed(closeError);
                }
            }
            if (bodyError instanceof RuntimeException re) throw re;
            if (bodyError instanceof Error err) throw err;
            throw new RuntimeException(bodyError);
        }
        scope.close();
        return result;
    }

    /**
     * Creates a scope with a timeout. If the body does not complete within
     * the specified duration, all child tasks are cancelled and the scope
     * throws {@link TimeoutException}.
     *
     * @param timeout the maximum duration for the scope
     * @param body    the function to execute within the scope
     * @param <T>     the result type
     * @return the body's return value
     * @throws TimeoutException if the timeout expires
     * @since 6.0.0
     */
    static <T> T withScope(Duration timeout, Function<AsyncScope, T> body) throws TimeoutException {
        return withScope(AsyncSupport.getExecutor(), timeout, body);
    }

    /**
     * Creates a scope with the given executor and a timeout.
     *
     * @param executor the executor for child tasks
     * @param timeout  the maximum duration for the scope
     * @param body     the function to execute within the scope
     * @param <T>      the result type
     * @return the body's return value
     * @throws TimeoutException if the timeout expires
     * @since 6.0.0
     */
    static <T> T withScope(Executor executor, Duration timeout, Function<AsyncScope, T> body) throws TimeoutException {
        return DefaultAsyncScope.withScopeTimeout(executor, timeout, body);
    }

    /** Creates a new scope with the default executor and fail-fast enabled. */
    static AsyncScope create() {
        return new DefaultAsyncScope();
    }

    /** Creates a new scope with the given executor and fail-fast enabled. */
    static AsyncScope create(Executor executor) {
        return new DefaultAsyncScope(executor);
    }

    /** Creates a new scope with the given executor and failure policy. */
    static AsyncScope create(Executor executor, boolean failFast) {
        return new DefaultAsyncScope(executor, failFast);
    }
}
