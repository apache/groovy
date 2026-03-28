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
 *
 * <h2>Design philosophy</h2>
 * <p>{@code AsyncScope} provides a bounded lifetime for async tasks,
 * following the <em>structured concurrency</em> model described in
 * <a href="https://openjdk.org/jeps/453">JEP 453</a> and inspired by
 * Kotlin's {@code coroutineScope}, Swift's {@code TaskGroup}, and Go's
 * {@code errgroup.Group}.  Unlike fire-and-forget
 * {@link AsyncSupport#executeAsync}, tasks launched within a scope are
 * guaranteed to complete before the scope closes.  This prevents:</p>
 * <ul>
 *   <li>Orphaned tasks that outlive their logical parent</li>
 *   <li>Resource leaks from uncollected async work</li>
 *   <li>Silent failures from unobserved exceptions</li>
 * </ul>
 *
 * <h2>Failure policy</h2>
 * <p>By default, the scope uses a <b>fail-fast</b> policy: when any child
 * task completes exceptionally, all sibling tasks are cancelled
 * immediately.  The first failure becomes the primary exception;
 * subsequent failures are added as
 * {@linkplain Throwable#addSuppressed(Throwable) suppressed} exceptions.</p>
 *
 * <h2>Usage in Groovy</h2>
 * <pre>{@code
 * import groovy.concurrent.AsyncScope
 * import groovy.concurrent.Awaitable
 *
 * def results = AsyncScope.withScope { scope ->
 *     def userTask  = scope.async { fetchUser(id) }
 *     def orderTask = scope.async { fetchOrders(id) }
 *     return [user: await userTask, orders: await orderTask]
 * }
 * // Both tasks guaranteed complete here
 * }</pre>
 *
 * <h2>Thread safety</h2>
 * <p>All public methods of conforming implementations are thread-safe.
 * A dedicated lock guards the child task list and the {@code closed}
 * flag jointly, ensuring that {@link #async(Closure)} and
 * {@link #close()} cannot race.</p>
 *
 * <h2>Thread-scoped state management</h2>
 * <p>The {@link #withCurrent(AsyncScope, Supplier)} method uses
 * {@link org.apache.groovy.runtime.async.ScopedLocal ScopedLocal} to
 * manage the current scope binding.  On JDK&nbsp;25+, this leverages
 * {@code ScopedValue} for optimal virtual-thread performance; on earlier
 * JDKs it falls back to {@code ThreadLocal}.</p>
 *
 * @see Awaitable
 * @see AsyncContext
 * @see AsyncSupport
 * @since 6.0.0
 */
public interface AsyncScope extends AutoCloseable {

    // ---- Instance methods ------------------------------------------------

    /**
     * Launches a child task within this scope.
     * <p>
     * The task's lifetime is bound to the scope: when the scope is closed,
     * all incomplete child tasks are cancelled.  The child inherits a
     * snapshot of the current {@link AsyncContext}, but any child-side
     * context mutations remain isolated from the parent and siblings.
     * <p>
     * Implementations must atomically check the closed state and register
     * the child, preventing the race where a task is submitted to the
     * executor but never joined by {@link #close()}.
     *
     * @param body the async body to execute; must not be {@code null}
     * @param <T>  the result type
     * @return an {@link Awaitable} representing the child task
     * @throws NullPointerException  if {@code body} is {@code null}
     * @throws IllegalStateException if the scope has already been closed
     */
    <T> Awaitable<T> async(Closure<T> body);

    /**
     * Launches a child task within this scope using a {@link Supplier},
     * providing idiomatic Java interop.
     * <p>
     * Behaves identically to {@link #async(Closure)} in all respects —
     * context propagation, scope binding, and fail-fast cancellation.
     *
     * @param supplier the async body to execute; must not be {@code null}
     * @param <T>      the result type
     * @return an {@link Awaitable} representing the child task
     * @throws NullPointerException  if {@code supplier} is {@code null}
     * @throws IllegalStateException if the scope has already been closed
     */
    <T> Awaitable<T> async(Supplier<T> supplier);

    /**
     * Returns the number of child tasks currently tracked by this scope.
     * <p>
     * Completed children may have been pruned, so this count reflects
     * active or recently-completed tasks.
     *
     * @return the child task count
     */
    int getChildCount();

    /**
     * Cancels all child tasks.  Idempotent — safe to call multiple times.
     * <p>
     * Does <em>not</em> close the scope — the scope remains open so that
     * {@link #close()} can still join all children and collect errors.
     */
    void cancelAll();

    /**
     * Waits for all child tasks to complete, then closes the scope.
     * <p>
     * If any child failed, the first failure is rethrown with subsequent
     * failures as {@linkplain Throwable#addSuppressed(Throwable) suppressed}
     * exceptions.  Cancelled tasks are silently ignored.
     * <p>
     * This method is idempotent: only the first invocation waits for
     * children; subsequent calls are no-ops.
     */
    @Override
    void close();

    // ---- Static factory and utility methods ------------------------------

    /**
     * Returns the structured async scope currently bound to this thread,
     * or {@code null} if execution is not inside
     * {@link #withScope(Closure)} or a child launched from such a scope.
     *
     * @return the current scope, or {@code null}
     */
    static AsyncScope current() {
        return DefaultAsyncScope.current();
    }

    /**
     * Executes the supplier with the given scope installed as the current
     * structured scope, restoring the previous binding afterwards.
     *
     * @param scope    the scope to install; may be {@code null}
     * @param supplier the action to execute; must not be {@code null}
     * @param <T>      the result type
     * @return the supplier's result
     * @throws NullPointerException if {@code supplier} is {@code null}
     */
    static <T> T withCurrent(AsyncScope scope, Supplier<T> supplier) {
        return DefaultAsyncScope.withCurrent(scope, supplier);
    }

    /**
     * Void overload of {@link #withCurrent(AsyncScope, Supplier)}.
     *
     * @param scope  the scope to install; may be {@code null}
     * @param action the action to execute; must not be {@code null}
     * @throws NullPointerException if {@code action} is {@code null}
     */
    static void withCurrent(AsyncScope scope, Runnable action) {
        DefaultAsyncScope.withCurrent(scope, action);
    }

    /**
     * Convenience method that creates a scope, executes the given closure
     * within it, and ensures the scope is closed on exit.
     * <p>
     * The closure receives the {@code AsyncScope} as its argument and can
     * launch child tasks via {@link #async(Closure)}.  The scope is
     * automatically closed (and all children awaited) when the closure
     * returns or throws.
     *
     * <pre>{@code
     * def result = AsyncScope.withScope { scope ->
     *     def a = scope.async { computeA() }
     *     def b = scope.async { computeB() }
     *     return [await a, await b]
     * }
     * }</pre>
     *
     * @param body the closure to execute within the scope;
     *             must not be {@code null}
     * @param <T>  the result type
     * @return the closure's return value
     * @throws NullPointerException if {@code body} is {@code null}
     */
    @SuppressWarnings("unchecked")
    static <T> T withScope(@ClosureParams(value = SimpleType.class, options = "groovy.concurrent.AsyncScope") Closure<T> body) {
        return withScope(AsyncSupport.getExecutor(), body);
    }

    /**
     * Convenience method that creates a scope with the given executor,
     * executes the closure, and ensures the scope is closed on exit.
     *
     * @param executor the executor for child tasks;
     *                 must not be {@code null}
     * @param body     the closure to execute within the scope;
     *                 must not be {@code null}
     * @param <T>      the result type
     * @return the closure's return value
     * @throws NullPointerException if {@code executor} or {@code body}
     *                              is {@code null}
     */
    @SuppressWarnings("unchecked")
    static <T> T withScope(Executor executor,
            @ClosureParams(value = SimpleType.class, options = "groovy.concurrent.AsyncScope") Closure<T> body) {
        Objects.requireNonNull(body, "body must not be null");
        try (AsyncScope scope = AsyncScope.create(executor)) {
            return withCurrent(scope, () -> body.call(scope));
        }
    }

    /**
     * Creates a new scope with the default async executor and fail-fast
     * enabled.
     *
     * @return a new scope
     */
    static AsyncScope create() {
        return new DefaultAsyncScope();
    }

    /**
     * Creates a new scope with the given executor and fail-fast enabled.
     *
     * @param executor the executor for child tasks;
     *                 must not be {@code null}
     * @return a new scope
     * @throws NullPointerException if {@code executor} is {@code null}
     */
    static AsyncScope create(Executor executor) {
        return new DefaultAsyncScope(executor);
    }

    /**
     * Creates a new scope with the given executor and failure policy.
     *
     * @param executor the executor for child tasks;
     *                 must not be {@code null}
     * @param failFast if {@code true}, cancel all siblings when any
     *                 child fails
     * @return a new scope
     * @throws NullPointerException if {@code executor} is {@code null}
     */
    static AsyncScope create(Executor executor, boolean failFast) {
        return new DefaultAsyncScope(executor, failFast);
    }
}
