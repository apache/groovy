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

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A simple implementation of {@link Promise} based on {@link CompletableFuture}.
 *
 * @since 6.0.0
 */
public class SimplePromise<T> implements Promise<T> {
    private final CompletableFuture<T> future;

    private SimplePromise(CompletableFuture<T> future) {
        this.future = future;
    }

    /**
     * Creates a new Promise backed by the given CompletableFuture.
     *
     * @param future the CompletableFuture to back the Promise
     * @param <T>    the type of the Promise's result
     * @return the new Promise
     */
    public static <T> SimplePromise<T> of(CompletableFuture<T> future) {
        return new SimplePromise<>(future);
    }

    /**
     * Returns a new Promise that is not yet completed.
     *
     * @param <T> the type of the Promise's result
     * @return the new Promise
     */
    public static <T> SimplePromise<T> of() {
        return of(new CompletableFuture<>());
    }

    /**
     * Returns a new Promise that is asynchronously completed by a task running in
     * the {@link ForkJoinPool#commonPool()} with the value obtained by calling the
     * provided Supplier.
     *
     * @param supplier a function returning the value to be used to complete the
     *                 returned Promise
     * @param <U>      the function's return type
     * @return the new Promise
     */
    public static <U> SimplePromise<U> of(Supplier<U> supplier) {
        return of(CompletableFuture.supplyAsync(supplier));
    }

    /**
     * Returns a new Promise that is asynchronously completed by a task running in
     * the provided executor with the value obtained by calling the provided Supplier.
     *
     * @param supplier a function returning the value to be used to complete the
     *                 returned Promise
     * @param executor the executor to use for asynchronous execution
     * @param <U>      the function's return type
     * @return the new Promise
     */
    public static <U> SimplePromise<U> of(Supplier<U> supplier, Executor executor) {
        return of(CompletableFuture.supplyAsync(supplier, executor));
    }

//    /**
//     * Returns a new Promise that is asynchronously completed by a task running in
//     * the {@link ForkJoinPool#commonPool()} after it runs the provided action.
//     *
//     * @param runnable the action to run before completing the returned Promise
//     * @return the new Promise
//     */
//    public static SimplePromise<Void> of(Runnable runnable) {
//        return of(CompletableFuture.runAsync(runnable));
//    }
//
//    /**
//     * Returns a new Promise that is asynchronously completed by a task running in
//     * the provided executor after it runs the provided action.
//     *
//     * @param runnable the action to run before completing the returned Promise
//     * @param executor the executor to use for asynchronous execution
//     * @return the new Promise
//     */
//    public static SimplePromise<Void> of(Runnable runnable, Executor executor) {
//        return of(CompletableFuture.runAsync(runnable, executor));
//    }

    /**
     * Returns a new Promise that is already completed with the provided value.
     *
     * @param value the value
     * @param <U>   the type of the value
     * @return the completed Promise
     */
    public static <U> SimplePromise<U> completed(U value) {
        return of(CompletableFuture.completedFuture(value));
    }

    /**
     * Returns a new Promise that is completed when all the provided Promises
     * complete. If any of the provided Promises complete exceptionally, then the
     * returned Promise also does so, with a CompletionException holding this
     * exception as its cause. Otherwise, the results, if any, of the provided
     * Promises are not reflected in the returned Promise, but may be obtained by
     * inspecting them individually. If no Promises are provided, returns a Promise
     * completed with the value {@code null}.
     *
     * <p>Among the applications of this method is to await completion of a set of
     * independent Promises before continuing a program, as in:
     * {@code SimplePromise.allOf(p1, p2, p3).join();}.
     *
     * @param ps the Promises
     * @return a new Promise that is completed when all the provided Promises complete
     * @throws NullPointerException if the array or any of its elements are {@code null}
     */
    public static SimplePromise<Void> allOf(Promise<?>... ps) {
        return of(CompletableFuture.allOf(
                    Arrays.stream(ps)
                        .map(Promise::toCompletableFuture)
                        .toArray(CompletableFuture[]::new)
        ));
    }

    /**
     * Returns a new Promise that is completed when any of the provided Promises
     * complete, with the same result. Otherwise, if it completed exceptionally,
     * the returned Promise also does so, with a CompletionException holding this
     * exception as its cause. If no Promises are provided, returns an incomplete
     * Promise.
     *
     * @param ps the Promises
     * @return a new Promise that is completed with the result or exception to any
     *         of the provided Promises when one completes
     * @throws NullPointerException if the array or any of its elements are
     *         {@code null}
     */
    public static SimplePromise<Object> anyOf(Promise<?>... ps) {
        return of(CompletableFuture.anyOf(
                    Arrays.stream(ps)
                        .map(Promise::toCompletableFuture)
                        .toArray(CompletableFuture[]::new)
        ));
    }

    @Override
    public T await() {
        try {
            return this.join();
        } catch (Throwable t) {
            throw new AwaitException(t);
        }
    }

    @Override
    public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor) {
        return of(future.whenCompleteAsync(action, executor));
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        return future.completeExceptionally(ex);
    }

    @Override
    public Promise<Void> thenRun(Runnable action) {
        return of(future.thenRun(action));
    }

    @Override
    public <U> Promise<U> applyToEither(StageAwaitable<? extends T> other, Function<? super T, U> fn) {
        return of(future.applyToEither(other.toPromise().toCompletableFuture(), fn));
    }

    @Override
    public void obtrudeException(Throwable ex) {
        future.obtrudeException(ex);
    }

    @Override
    public Promise<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return of(future.exceptionallyComposeAsync(t -> {
            final StageAwaitable<T> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }));
    }

    @Override
    public Promise<T> whenComplete(BiConsumer<? super T, ? super Throwable> action) {
        return of(future.whenComplete(action));
    }

    @Override
    public <U> Promise<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn, Executor executor) {
        return of(future.applyToEitherAsync(other.toPromise().toCompletableFuture(), fn, executor));
    }

    @Override
    public <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn) {
        return of(future.thenApplyAsync(fn));
    }

    @Override
    public <U> Promise<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return of(future.thenAcceptBothAsync(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public Promise<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor) {
        return of(future.exceptionallyAsync(fn, executor));
    }

    @Override
    public Promise<Void> thenRunAsync(Runnable action, Executor executor) {
        return of(future.thenRunAsync(action, executor));
    }

    @Override
    public Promise<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action) {
        return of(future.runAfterEitherAsync(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public Promise<T> orTimeout(long timeout, TimeUnit unit) {
        return of(future.orTimeout(timeout, unit));
    }

    @Override
    public <U, V> Promise<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return of(future.thenCombineAsync(other.toPromise().toCompletableFuture(), fn));
    }

    @Override
    public Promise<Void> runAfterBoth(StageAwaitable<?> other, Runnable action) {
        return of(future.runAfterBoth(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public <U> Promise<U> thenCompose(Function<? super T, ? extends StageAwaitable<U>> fn) {
        return of(future.thenCompose(t -> {
            final StageAwaitable<U> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }));
    }

    @Override
    public Promise<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action, Executor executor) {
        return of(future.runAfterBothAsync(other.toPromise().toCompletableFuture(), action, executor));
    }

    @Override
    public <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn) {
        return of(future.handleAsync(fn));
    }

    @Override
    public <U> Promise<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn, Executor executor) {
        return of(future.thenComposeAsync(t -> {
            final StageAwaitable<U> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }, executor));
    }

    @Override
    public Promise<Void> thenAccept(Consumer<? super T> action) {
        return of(future.thenAccept(action));
    }

    @Override
    public T join() {
        return future.join();
    }

    @Override
    public Promise<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action) {
        return of(future.acceptEitherAsync(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public Executor defaultExecutor() {
        return future.defaultExecutor();
    }

    @Override
    public Promise<T> exceptionallyCompose(Function<Throwable, ? extends StageAwaitable<T>> fn) {
        return of(future.exceptionallyCompose(t -> {
            final StageAwaitable<T> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }));
    }

    @Override
    public <U> Promise<Void> thenAcceptBoth(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action) {
        return of(future.thenAcceptBoth(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public Promise<Void> runAfterEither(StageAwaitable<?> other, Runnable action) {
        return of(future.runAfterEither(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public Promise<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        return of(future.completeOnTimeout(value, timeout, unit));
    }

    @Override
    public <U> Promise<U> handle(BiFunction<? super T, Throwable, ? extends U> fn) {
        return of(future.handle(fn));
    }

    @Override
    public boolean complete(T value) {
        return future.complete(value);
    }

    @Override
    public Promise<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor) {
        return of(future.thenAcceptAsync(action, executor));
    }

    @Override
    public int getNumberOfDependents() {
        return future.getNumberOfDependents();
    }

    @Override
    public <U> Promise<Void> thenAcceptBothAsync(StageAwaitable<? extends U> other, BiConsumer<? super T, ? super U> action, Executor executor) {
        return of(future.thenAcceptBothAsync(other.toPromise().toCompletableFuture(), action, executor));
    }

    @Override
    public Promise<T> exceptionallyAsync(Function<Throwable, ? extends T> fn) {
        return of(future.exceptionallyAsync(fn));
    }

    @Override
    public Promise<Void> runAfterEitherAsync(StageAwaitable<?> other, Runnable action, Executor executor) {
        return of(future.runAfterEitherAsync(other.toPromise().toCompletableFuture(), action, executor));
    }

    @Override
    public boolean isCompletedExceptionally() {
        return future.isCompletedExceptionally();
    }

    @Override
    public Promise<T> completeAsync(Supplier<? extends T> supplier) {
        return of(future.completeAsync(supplier));
    }

    @Override
    public <U> Promise<U> applyToEitherAsync(StageAwaitable<? extends T> other, Function<? super T, U> fn) {
        return of(future.applyToEitherAsync(other.toPromise().toCompletableFuture(), fn));
    }

    @Override
    public Promise<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action) {
        return of(future.whenCompleteAsync(action));
    }

    @Override
    public Promise<Void> thenRunAsync(Runnable action) {
        return of(future.thenRunAsync(action));
    }

    @Override
    public <U> Promise<U> thenApply(Function<? super T, ? extends U> fn) {
        return of(future.thenApply(fn));
    }

    @Override
    public void obtrudeValue(T value) {
        future.obtrudeValue(value);
    }

    @Override
    public <U> Promise<U> thenComposeAsync(Function<? super T, ? extends StageAwaitable<U>> fn) {
        return of(future.thenComposeAsync(t -> {
            final StageAwaitable<U> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }));
    }

    @Override
    public Promise<T> copy() {
        return of(future.copy());
    }

    @Override
    public Promise<Void> acceptEither(StageAwaitable<? extends T> other, Consumer<? super T> action) {
        return of(future.acceptEither(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public <U> Promise<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor) {
        return of(future.thenApplyAsync(fn, executor));
    }

    @Override
    public <U, V> Promise<V> thenCombine(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn) {
        return of(future.thenCombine(other.toPromise().toCompletableFuture(), fn));
    }

    @Override
    public Promise<T> exceptionally(Function<Throwable, ? extends T> fn) {
        return of(future.exceptionally(fn));
    }

    @Override
    public Promise<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        return of(future.completeAsync(supplier, executor));
    }

    @Override
    public Promise<Void> acceptEitherAsync(StageAwaitable<? extends T> other, Consumer<? super T> action, Executor executor) {
        return of(future.acceptEitherAsync(other.toPromise().toCompletableFuture(), action, executor));
    }

    @Override
    public Promise<T> exceptionallyComposeAsync(Function<Throwable, ? extends StageAwaitable<T>> fn, Executor executor) {
        return of(future.exceptionallyComposeAsync(t -> {
            final StageAwaitable<T> p = fn.apply(t);
            return p.toPromise().toCompletableFuture();
        }, executor));
    }

    @Override
    public Promise<T> toPromise() {
        return this;
    }

    @Override
    public Promise<Void> thenAcceptAsync(Consumer<? super T> action) {
        return of(future.thenAcceptAsync(action));
    }

    @Override
    public <U, V> Promise<V> thenCombineAsync(StageAwaitable<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor) {
        return of(future.thenCombineAsync(other.toPromise().toCompletableFuture(), fn, executor));
    }

    @Override
    public Promise<Void> runAfterBothAsync(StageAwaitable<?> other, Runnable action) {
        return of(future.runAfterBothAsync(other.toPromise().toCompletableFuture(), action));
    }

    @Override
    public <U> Promise<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor) {
        return of(future.handleAsync(fn, executor));
    }

    @Override
    public T getNow(T valueIfAbsent) {
        return future.getNow(valueIfAbsent);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    @Override
    public CompletableFuture<T> toCompletableFuture() {
        return future.toCompletableFuture();
    }
}
