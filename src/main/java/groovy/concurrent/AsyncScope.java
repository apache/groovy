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
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.apache.groovy.runtime.async.AsyncSupport;
import org.apache.groovy.runtime.async.DefaultAsyncScope;

import java.util.Objects;
import java.util.concurrent.Executor;
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
     * @param body the async body to execute
     * @param <T>  the result type
     * @return an {@link Awaitable} representing the child task
     * @throws IllegalStateException if the scope has already been closed
     */
    <T> Awaitable<T> async(Closure<T> body);

    /**
     * Launches a child task using a {@link Supplier} for Java interop.
     */
    <T> Awaitable<T> async(Supplier<T> supplier);

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
     * Creates a scope, executes the closure within it, and ensures the
     * scope is closed on exit. The closure receives the scope as its
     * argument for launching child tasks.
     *
     * <pre>{@code
     * def result = AsyncScope.withScope { scope ->
     *     def a = scope.async { computeA() }
     *     def b = scope.async { computeB() }
     *     return [await(a), await(b)]
     * }
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    static <T> T withScope(
            @ClosureParams(value = SimpleType.class, options = "groovy.concurrent.AsyncScope") Closure<T> body) {
        return withScope(AsyncSupport.getExecutor(), body);
    }

    /**
     * Creates a scope with the given executor, executes the closure,
     * and ensures the scope is closed on exit.
     */
    @SuppressWarnings("unchecked")
    static <T> T withScope(Executor executor,
            @ClosureParams(value = SimpleType.class, options = "groovy.concurrent.AsyncScope") Closure<T> body) {
        Objects.requireNonNull(body, "body must not be null");
        try (AsyncScope scope = create(executor)) {
            return withCurrent(scope, () -> body.call(scope));
        }
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
