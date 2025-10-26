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
package groovy.util.concurrent.async;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a computation stage in a potentially asynchronous execution chain that
 * executes an action or computes a value upon completion of another StageAwaitable.
 * A stage completes when its computation finishes, which may subsequently trigger
 * the execution of dependent stages in the chain.
 *
 * @since 6.0.0
 */
public interface StageAwaitable<T> extends Awaitable<T> {
    /**
     * Creates a new StageAwaitable that executes the provided function with this stage's
     * result as input when this stage completes successfully.
     *
     * <p>This method follows the same pattern as {@link java.util.Optional#map Optional.map}
     * and {@link java.util.stream.Stream#map Stream.map}.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute the resulting StageAwaitable's value
     * @param <U> the return type of the function
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenApply(Function<? super T, ? extends U> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using this stage's default asynchronous execution facility when this stage
     * completes successfully. The function receives this stage's result as input.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute the resulting StageAwaitable's value
     * @param <U> the return type of the function
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenApplyAsync(Function<? super T, ? extends U> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using the specified Executor when this stage completes successfully. The function
     * receives this stage's result as input.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute the resulting StageAwaitable's value
     * @param executor the executor used for asynchronous execution
     * @param <U> the return type of the function
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action with this stage's
     * result as input when this stage completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenAccept(Consumer<? super T> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility when this stage
     * completes successfully. The action receives this stage's result as input.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenAcceptAsync(Consumer<? super T> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified Executor when this stage completes successfully. The action
     * receives this stage's result as input.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action when this stage
     * completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenRun(Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility when this stage
     * completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenRunAsync(Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified Executor when this stage completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> thenRunAsync(Runnable action, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided function with the results
     * of both this stage and the other stage when they both complete successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the type of the other StageAwaitable's result
     * @param <V> the function's return type
     * @return the newly created StageAwaitable
     */
    <U, V> StageAwaitable<V> thenCombine(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using this stage's default asynchronous execution facility when both this stage
     * and the other stage complete successfully. The function receives both results as
     * arguments.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the type of the other StageAwaitable's result
     * @param <V> the function's return type
     * @return the newly created StageAwaitable
     */
    <U, V> StageAwaitable<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using the specified executor when both this stage and the other stage complete
     * successfully. The function receives both results as arguments.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @param <U> the type of the other StageAwaitable's result
     * @param <V> the function's return type
     * @return the newly created StageAwaitable
     */
    <U, V> StageAwaitable<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action with the results
     * of both this stage and the other stage when they both complete successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param <U> the type of the other StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<Void> thenAcceptBoth(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility when both this stage
     * and the other stage complete successfully. The action receives both results as
     * arguments.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param <U> the type of the other StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified executor when both this stage and the other stage complete
     * successfully. The action receives both results as arguments.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @param <U> the type of the other StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action when both this
     * stage and the other stage complete successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterBoth(StageAwaitable<?> other, Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility when both this stage
     * and the other stage complete successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified executor when both this stage and the other stage complete
     * successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided function with the result
     * from whichever stage completes successfully first (either this stage or the other
     * stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> applyToEither(StageAwaitable<? extends T> other, Function<? super T, U> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using this stage's default asynchronous execution facility with the result from
     * whichever stage completes successfully first (either this stage or the other stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn);

    /**
     * Creates a new StageAwaitable that executes the provided function asynchronously
     * using the specified executor with the result from whichever stage completes
     * successfully first (either this stage or the other stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action with the result
     * from whichever stage completes successfully first (either this stage or the other
     * stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> acceptEither(StageAwaitable<? extends T> other, Consumer<? super T> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility with the result from
     * whichever stage completes successfully first (either this stage or the other stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified executor with the result from whichever stage completes
     * successfully first (either this stage or the other stage).
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action, Executor executor);

    /**
     * Creates a new StageAwaitable that executes the provided action when either
     * this stage or the other stage completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterEither(StageAwaitable<?> other, Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using this stage's default asynchronous execution facility when either this
     * stage or the other stage completes successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action);

    /**
     * Creates a new StageAwaitable that executes the provided action asynchronously
     * using the specified executor when either this stage or the other stage completes
     * successfully.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param other the other StageAwaitable
     * @param action the action to perform before completing the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action, Executor executor);

    /**
     * Creates a new StageAwaitable that is completed with the same value as the
     * StageAwaitable returned by the provided function.
     *
     * <p>When this stage completes successfully, the provided function is invoked
     * with this stage's result as the argument, returning another StageAwaitable.
     * When that stage completes successfully, the StageAwaitable returned by this
     * method is completed with the same value.
     *
     * <p>To ensure progress, the supplied function must arrange eventual completion
     * of its result.
     *
     * <p>This method is analogous to {@link java.util.Optional#flatMap Optional.flatMap}
     * and {@link java.util.stream.Stream#flatMap Stream.flatMap}.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute another StageAwaitable
     * @param <U> the type of the resulting StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenCompose(Function<? super T, ? extends StageAwaitable<U>> fn);

    /**
     * Creates a new StageAwaitable that is completed with the same value as the
     * StageAwaitable returned by the provided function, executed asynchronously
     * using this stage's default asynchronous execution facility.
     *
     * <p>When this stage completes successfully, the provided function is invoked
     * with this stage's result as the argument, returning another StageAwaitable.
     * When that stage completes successfully, the StageAwaitable returned by this
     * method is completed with the same value.
     *
     * <p>To ensure progress, the supplied function must arrange eventual completion
     * of its result.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute another StageAwaitable
     * @param <U> the type of the resulting StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn);

    /**
     * Creates a new StageAwaitable that is completed with the same value as the
     * StageAwaitable returned by the provided function, executed asynchronously
     * using the specified Executor.
     *
     * <p>When this stage completes successfully, the provided function is invoked
     * with this stage's result as the argument, returning another StageAwaitable.
     * When that stage completes successfully, the StageAwaitable returned by this
     * method is completed with the same value.
     *
     * <p>To ensure progress, the supplied function must arrange eventual completion
     * of its result.
     *
     * <p>Refer to the {@link StageAwaitable} documentation for behavior regarding
     * exceptional completion scenarios.
     *
     * @param fn the function used to compute another StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @param <U> the type of the resulting StageAwaitable's result
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn, Executor executor);

    /**
     * Creates a new StageAwaitable that is executed with this stage's result and
     * exception as arguments to the provided function when this stage completes
     * either successfully or exceptionally.
     *
     * <p>When this stage is complete, the provided function is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null} if none)
     * of this stage as arguments, and the function's result is used to complete the
     * resulting stage.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> handle(BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * Creates a new StageAwaitable that is executed asynchronously using this stage's
     * default asynchronous execution facility with this stage's result and exception
     * as arguments to the provided function when this stage completes either
     * successfully or exceptionally.
     *
     * <p>When this stage is complete, the provided function is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null} if none)
     * of this stage as arguments, and the function's result is used to complete the
     * resulting stage.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * Creates a new StageAwaitable that is executed asynchronously using the
     * specified executor with this stage's result and exception as arguments to
     * the provided function when this stage completes either successfully or
     * exceptionally.
     *
     * <p>When this stage is complete, the provided function is invoked with the
     * result (or {@code null} if none) and the exception (or {@code null} if none)
     * of this stage as arguments, and the function's result is used to complete the
     * resulting stage.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * @param executor the executor used for asynchronous execution
     * @param <U> the function's return type
     * @return the newly created StageAwaitable
     */
    <U> StageAwaitable<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor);

    /**
     * Creates a new StageAwaitable with the same result or exception as this stage,
     * that executes the provided action when this stage completes.
     *
     * <p>When this stage is complete, the provided action is invoked with the result
     * (or {@code null} if none) and the exception (or {@code null} if none) of this
     * stage as arguments. The resulting stage is completed when the action returns.
     *
     * <p>Unlike method {@link #handle handle}, this method is not designed to
     * translate completion outcomes, so the supplied action should not throw an
     * exception. However, if it does, the following rules apply: if this stage
     * completed successfully but the supplied action throws an exception, then the
     * resulting stage completes exceptionally with the supplied action's exception.
     * Or, if this stage completed exceptionally and the supplied action throws an
     * exception, then the resulting stage completes exceptionally with this stage's
     * exception.
     *
     * @param action the action to perform
     * @return the newly created StageAwaitable
     */
    StageAwaitable<T> whenComplete(BiConsumer<? super T, ? super Throwable> action);

    /**
     * Creates a new StageAwaitable with the same result or exception as this stage,
     * that executes the provided action asynchronously using this stage's default
     * asynchronous execution facility when this stage completes.
     *
     * <p>When this stage is complete, the provided action is invoked with the result
     * (or {@code null} if none) and the exception (or {@code null} if none) of this
     * stage as arguments. The resulting stage is completed when the action returns.
     *
     * <p>Unlike method {@link #handleAsync(BiFunction) handleAsync}, this method is
     * not designed to translate completion outcomes, so the supplied action should
     * not throw an exception. However, if it does, the following rules apply: If
     * this stage completed successfully but the supplied action throws an exception,
     * then the resulting stage completes exceptionally with the supplied action's
     * exception. Or, if this stage completed exceptionally and the supplied action
     * throws an exception, then the resulting stage completes exceptionally with
     * this stage's exception.
     *
     * @param action the action to perform
     * @return the newly created StageAwaitable
     */
    StageAwaitable<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action);

    /**
     * Creates a new StageAwaitable with the same result or exception as this stage,
     * that executes the provided action asynchronously using the specified Executor
     * when this stage completes.
     *
     * <p>When this stage is complete, the provided action is invoked with the result
     * (or {@code null} if none) and the exception (or {@code null} if none) of this
     * stage as arguments. The resulting stage is completed when the action returns.
     *
     * <p>Unlike method {@link #handleAsync(BiFunction,Executor) handleAsync}, this
     * method is not designed to translate completion outcomes, so the supplied action
     * should not throw an exception. However, if it does, the following rules apply:
     * If this stage completed successfully but the supplied action throws an
     * exception, then the resulting stage completes exceptionally with the supplied
     * action's exception. Or, if this stage completed exceptionally and the supplied
     * action throws an exception, then the resulting stage completes exceptionally
     * with this stage's exception.
     *
     * @param action the action to perform
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    StageAwaitable<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor);

    /**
     * Creates a new StageAwaitable that is executed with this stage's exception as
     * the argument to the provided function when this stage completes exceptionally.
     * Otherwise, if this stage completes successfully, then the resulting stage also
     * completes successfully with the same value.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * if this StageAwaitable completed exceptionally
     * @return the newly created StageAwaitable
     */
    StageAwaitable<T> exceptionally(Function<Throwable, ? extends T> fn);

    /**
     * Creates a new StageAwaitable that is executed asynchronously with this stage's
     * exception as the argument to the provided function using this stage's default
     * asynchronous execution facility when this stage completes exceptionally.
     * Otherwise, if this stage completes successfully, then the resulting stage also
     * completes successfully with the same value.
     *
     * @implSpec The default implementation invokes {@link #handle}, relaying to
     * {@link #handleAsync} on exception, then {@link #thenCompose} for result.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * if this StageAwaitable completed exceptionally
     * @return the newly created StageAwaitable
     */
    default StageAwaitable<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
        return handle((r, ex) -> (ex == null) ? this : this.<T>handleAsync((r1, ex1) -> fn.apply(ex1))).thenCompose(Function.identity());
    }

    /**
     * Creates a new StageAwaitable that is executed asynchronously with this stage's
     * exception as the argument to the provided function using the specified Executor
     * when this stage completes exceptionally. Otherwise, if this stage completes
     * successfully, then the resulting stage also completes successfully with the
     * same value.
     *
     * @implSpec The default implementation invokes {@link #handle}, relaying to
     * {@link #handleAsync} on exception, then {@link #thenCompose} for result.
     *
     * @param fn the function used to compute the value of the resulting StageAwaitable
     * if this StageAwaitable completed exceptionally
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    default StageAwaitable<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor) {
        return handle((r, ex) -> (ex == null) ? this : this.<T>handleAsync((r1, ex1) -> fn.apply(ex1), executor)).thenCompose(Function.identity());
    }

    /**
     * Creates a new StageAwaitable that is composed using the result of the provided
     * function applied to this stage's exception when this stage completes exceptionally.
     *
     * @implSpec The default implementation invokes {@link #handle}, invoking the
     * provided function on exception, then {@link #thenCompose} for result.
     *
     * @param fn the function used to compute the resulting StageAwaitable if this
     * StageAwaitable completed exceptionally
     * @return the newly created StageAwaitable
     */
    default StageAwaitable<T> exceptionallyCompose(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return handle((r, ex) -> (ex == null) ? this : fn.apply(ex)).thenCompose(Function.identity());
    }

    /**
     * Creates a new StageAwaitable that is composed asynchronously using the result
     * of the provided function applied to this stage's exception using this stage's
     * default asynchronous execution facility when this stage completes exceptionally.
     *
     * @implSpec The default implementation invokes {@link #handle}, relaying to
     * {@link #handleAsync} on exception, then {@link #thenCompose} for result.
     *
     * @param fn the function used to compute the resulting StageAwaitable if this
     * StageAwaitable completed exceptionally
     * @return the newly created StageAwaitable
     */
    default StageAwaitable<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return handle((r, ex) -> (ex == null) ? this : this.handleAsync((r1, ex1) -> fn.apply(ex1)).thenCompose(Function.identity())).thenCompose(Function.identity());
    }

    /**
     * Creates a new StageAwaitable that is composed asynchronously using the result
     * of the provided function applied to this stage's exception using the specified
     * Executor when this stage completes exceptionally.
     *
     * @implSpec The default implementation invokes {@link #handle}, relaying to
     * {@link #handleAsync} on exception, then {@link #thenCompose} for result.
     *
     * @param fn the function used to compute the resulting StageAwaitable if this
     * StageAwaitable completed exceptionally
     * @param executor the executor used for asynchronous execution
     * @return the newly created StageAwaitable
     */
    default StageAwaitable<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn, Executor executor) {
        return handle((r, ex) -> (ex == null) ? this : this.handleAsync((r1, ex1) -> fn.apply(ex1), executor).thenCompose(Function.identity())).thenCompose(Function.identity());
    }

    /**
     * Creates a {@link Promise} that maintains the same completion properties
     * as this stage. If this stage is already a Promise, this method may return
     * this stage itself. Otherwise, invoking this method may have the same effect as
     * {@code thenApply(x -> x)}, but returns an instance of type {@code Promise}.
     *
     * @return the Promise
     */
    Promise<T> toPromise();
}
