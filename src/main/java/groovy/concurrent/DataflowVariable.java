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

import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A single-assignment variable for dataflow-style programming.
 * <p>
 * A {@code DataflowVariable} starts unbound. It can be written to exactly
 * once via {@link #bind(Object)} (or the {@code <<} operator in Groovy).
 * Any thread that reads the variable before it is bound will block until
 * a value becomes available. Once bound, all subsequent reads return the
 * same value immediately.
 * <p>
 * {@code DataflowVariable} implements {@link Awaitable}, so it works
 * naturally with {@code await}:
 *
 * <pre>{@code
 * def x = new DataflowVariable()
 * def y = new DataflowVariable()
 *
 * def z = Awaitable.go { await(x) + await(y) }
 *
 * async { x << 10 }
 * async { y << 5 }
 *
 * println "Result: ${await(z)}"  // 15
 * }</pre>
 * <p>
 * Inspired by GPars' {@code DataflowVariable}, modernised to integrate
 * with Groovy's {@code async}/{@code await} and {@link Awaitable} API.
 *
 * @param <T> the value type
 * @see Awaitable
 * @see Dataflows
 * @since 6.0.0
 */
public class DataflowVariable<T> implements Awaitable<T> {

    private final CompletableFuture<T> future = new CompletableFuture<>();
    private final Awaitable<T> awaitable = GroovyPromise.of(future);

    /**
     * Creates an unbound dataflow variable.
     */
    public DataflowVariable() { }

    /**
     * Binds this variable to the given value. Can only be called once;
     * subsequent calls throw {@link IllegalStateException}.
     *
     * @param value the value to bind (may be {@code null})
     * @throws IllegalStateException if already bound
     */
    public void bind(T value) {
        if (!future.complete(value)) {
            String current;
            try {
                current = String.valueOf(future.getNow(null));
            } catch (java.util.concurrent.CompletionException e) {
                current = "error: " + e.getCause();
            }
            throw new IllegalStateException(
                    "DataflowVariable is already bound to: " + current);
        }
    }

    /**
     * Binds this variable to an error. Any thread awaiting the value
     * will receive the exception.
     *
     * @param error the error to bind
     * @throws IllegalStateException if already bound
     */
    public void bindError(Throwable error) {
        if (!future.completeExceptionally(error)) {
            throw new IllegalStateException(
                    "DataflowVariable is already bound");
        }
    }

    /**
     * Returns {@code true} if this variable has been bound to a value
     * or an error.
     */
    public boolean isBound() {
        return future.isDone();
    }

    /**
     * Groovy operator overload: {@code variable << value} binds the value.
     *
     * @param value the value to bind
     * @return this variable (for chaining)
     */
    public DataflowVariable<T> leftShift(T value) {
        bind(value);
        return this;
    }

    // ---- Awaitable delegation -------------------------------------------

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return awaitable.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return awaitable.get(timeout, unit);
    }

    @Override
    public boolean isDone() {
        return awaitable.isDone();
    }

    @Override
    public boolean cancel() {
        return awaitable.cancel();
    }

    @Override
    public boolean isCancelled() {
        return awaitable.isCancelled();
    }

    @Override
    public boolean isCompletedExceptionally() {
        return awaitable.isCompletedExceptionally();
    }

    @Override
    public <U> Awaitable<U> then(Function<? super T, ? extends U> fn) {
        return awaitable.then(fn);
    }

    @Override
    public <U> Awaitable<U> thenCompose(Function<? super T, ? extends Awaitable<U>> fn) {
        return awaitable.thenCompose(fn);
    }

    @Override
    public Awaitable<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return awaitable.exceptionally(fn);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future;
    }

    @Override
    public String toString() {
        if (future.isDone()) {
            try {
                return "DataflowVariable[" + future.getNow(null) + "]";
            } catch (Exception e) {
                return "DataflowVariable[error: " + e.getMessage() + "]";
            }
        }
        return "DataflowVariable[unbound]";
    }
}
