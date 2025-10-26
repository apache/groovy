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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents the result of an asynchronous computation that may be explicitly completed (setting its
 * value and status), and may be used as a {@link StageAwaitable},
 * supporting dependent functions and actions that trigger upon its
 * completion.
 *
 * @since 6.0.0
 */
public interface Promise<T> extends StageAwaitable<T>, Future<T> {
    /**
     * Causes invocations of {@link #get()} and related methods to throw the provided
     * exception if not already completed.
     *
     * @param ex the exception
     * @return {@code true} if this invocation caused this Promise to transition to a
     * completed state, else {@code false}
     */
    boolean completeExceptionally(Throwable ex);

    /**
     * Forcibly causes subsequent invocations of {@link #get()} and related methods
     * to throw the provided exception, regardless of whether already completed.
     * This method is intended for use only in error recovery scenarios and may cause
     * ongoing dependent completions to use established outcomes instead of the
     * overwritten outcome even in such situations.
     *
     * @param ex the exception
     * @throws NullPointerException if the exception is null
     */
    void obtrudeException(Throwable ex);

    /**
     * Completes this Promise exceptionally with a {@link TimeoutException} if not
     * otherwise completed before the specified timeout.
     *
     * @param timeout the duration to wait before completing exceptionally with a
     *        TimeoutException, measured in units of {@code unit}
     * @param unit the {@code TimeUnit} that determines how to interpret the
     *        {@code timeout} parameter
     * @return this Promise
     */
    Promise<T> orTimeout(long timeout, TimeUnit unit);

    /**
     * Returns the result value when complete, or throws an unchecked exception if
     * completed exceptionally. To better conform with the use of common functional
     * forms, if a computation involved in the completion of this Promise threw an
     * exception, this method throws an unchecked {@link CompletionException} with
     * the underlying exception as its cause.
     *
     * @return the result value
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed exceptionally or a
     * completion computation threw an exception
     */
    T join();

    /**
     * Returns the default Executor used for async methods that do not specify an
     * Executor.
     *
     * @return the executor
     */
    Executor defaultExecutor();

    /**
     * Completes this Promise with the provided value if not otherwise completed
     * before the specified timeout.
     *
     * @param value the value to use upon timeout
     * @param timeout the duration to wait before completing normally with the
     *        provided value, measured in units of {@code unit}
     * @param unit the {@code TimeUnit} that determines how to interpret the
     *        {@code timeout} parameter
     * @return this Promise
     */
    Promise<T> completeOnTimeout(T value, long timeout, TimeUnit unit);

    /**
     * If not already completed, sets the value returned by {@link #get()} and
     * related methods to the provided value.
     *
     * @param value the result value
     * @return {@code true} if this invocation caused this Promise to transition
     * to a completed state, else {@code false}
     */
    boolean complete(T value);

    /**
     * Returns the estimated number of Promises whose completions are awaiting
     * completion of this Promise. This method is designed for use in monitoring
     * system state, not for synchronization control.
     *
     * @return the number of dependent Promises
     */
    int getNumberOfDependents();

    /**
     * Returns {@code true} if this Promise completed exceptionally, in any way.
     * Possible causes include cancellation, explicit invocation of
     * {@code completeExceptionally}, and abrupt termination of a CompletionStage
     * action.
     *
     * @return {@code true} if this Promise completed exceptionally
     */
    boolean isCompletedExceptionally();

    /**
     * Completes this Promise with the result of the provided Supplier function
     * invoked from an asynchronous task using the specified executor.
     *
     * @param supplier a function returning the value to be used to complete this
     *        Promise
     * @param executor the executor to use for asynchronous execution
     * @return this Promise
     */
    Promise<T> completeAsync(Supplier<? extends T> supplier, Executor executor);

    /**
     * Forcibly sets or resets the value subsequently returned by method
     * {@link #get()} and related methods, regardless of whether already
     * completed. This method is designed for use only in error recovery actions,
     * and even in such situations may result in ongoing dependent completions
     * using established versus overwritten outcomes.
     *
     * @param value the completion value
     */
    void obtrudeValue(T value);

    /**
     * Returns a new Promise that is completed normally with the same value as
     * this Promise when it completes normally. If this Promise completes
     * exceptionally, then the returned Promise completes exceptionally with a
     * CompletionException with this exception as cause. The behavior is
     * equivalent to {@code thenApply(x -> x)}. This method may be useful as a
     * form of "defensive copying", to prevent clients from completing, while
     * still being able to arrange dependent actions.
     *
     * @return the new Promise
     */
    Promise<T> copy();

    /**
     * Completes this Promise with the result of the provided Supplier function
     * invoked from an asynchronous task using the default executor.
     *
     * @param supplier a function returning the value to be used to complete this
     *        Promise
     * @return this Promise
     */
    Promise<T> completeAsync(Supplier<? extends T> supplier);

    /**
     * Returns the result value (or throws any encountered exception) if
     * completed, else returns the provided valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the provided valueIfAbsent
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed exceptionally or a
     *         completion computation threw an exception
     */
    T getNow(T valueIfAbsent);

    /**
     * Returns a {@link CompletableFuture} representation of the object.
     *
     * @return the CompletableFuture
     */
    CompletableFuture<T> toCompletableFuture();

    @Override
    <U> Promise<U> thenApply(Function<? super T, ? extends U> fn);

    @Override
    <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn);

    @Override
    <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor);

    @Override
    Promise<Void> thenAccept(Consumer<? super T> action);

    @Override
    Promise<Void> thenAcceptAsync(Consumer<? super T> action);

    @Override
    Promise<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor);

    @Override
    Promise<Void> thenRun(Runnable action);

    @Override
    Promise<Void> thenRunAsync(Runnable action);

    @Override
    Promise<Void> thenRunAsync(Runnable action, Executor executor);

    @Override
    <U, V> Promise<V> thenCombine(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn);

    @Override
    <U, V> Promise<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn);

    @Override
    <U, V> Promise<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor);

    @Override
    <U> Promise<Void> thenAcceptBoth(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action);

    @Override
    <U> Promise<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action);

    @Override
    <U> Promise<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor);

    @Override
    Promise<Void> runAfterBoth(StageAwaitable<?> other, Runnable action);

    @Override
    Promise<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action);

    @Override
    Promise<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action, Executor executor);

    @Override
    <U> Promise<U> applyToEither(StageAwaitable<? extends T> other, Function<? super T, U> fn);

    @Override
    <U> Promise<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn);

    @Override
    <U> Promise<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn, Executor executor);

    @Override
    Promise<Void> acceptEither(StageAwaitable<? extends T> other, Consumer<? super T> action);

    @Override
    Promise<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action);

    @Override
    Promise<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action, Executor executor);

    @Override
    Promise<Void> runAfterEither(StageAwaitable<?> other, Runnable action);

    @Override
    Promise<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action);

    @Override
    Promise<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action, Executor executor);

    @Override
    <U> Promise<U> thenCompose(Function<? super T, ? extends StageAwaitable<U>> fn);

    @Override
    <U> Promise<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn);

    @Override
    <U> Promise<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn, Executor executor);

    @Override
    <U> Promise<U> handle(BiFunction<? super T, Throwable, ? extends U> fn);

    @Override
    <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn);

    @Override
    <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor);

    @Override
    Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> action);

    @Override
    Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action);

    @Override
    Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor);

    @Override
    Promise<T> exceptionally(Function<Throwable, ? extends T> fn);

    @Override
    default Promise<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
        return StageAwaitable.super.exceptionallyAsync(fn).toPromise();
    }

    @Override
    default Promise<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor) {
        return StageAwaitable.super.exceptionallyAsync(fn, executor).toPromise();
    }

    @Override
    default Promise<T> exceptionallyCompose(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return StageAwaitable.super.exceptionallyCompose(fn).toPromise();
    }

    @Override
    default Promise<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return StageAwaitable.super.exceptionallyComposeAsync(fn).toPromise();
    }

    @Override
    default Promise<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn, Executor executor) {
        return StageAwaitable.super.exceptionallyComposeAsync(fn, executor).toPromise();
    }
}
