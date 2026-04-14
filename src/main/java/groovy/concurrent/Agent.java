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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * A thread-safe mutable-value container inspired by Clojure's agents and
 * GPars' {@code Agent}.
 * <p>
 * An {@code Agent} wraps a value that can be read by any thread but
 * modified only through serialised update functions. Updates are queued
 * and applied one at a time on a dedicated executor, guaranteeing that
 * the value is never corrupted by concurrent writes.
 * <p>
 * Reading the current value via {@link #get()} is non-blocking and
 * returns a snapshot. Sending an update via {@link #send(Function)} is
 * also non-blocking — the function is queued and applied asynchronously.
 * Use {@link #sendAndGet(Function)} to obtain an {@link Awaitable} that
 * completes with the new value after the update is applied.
 *
 * <pre>{@code
 * // Groovy:
 * def counter = Agent.create(0)
 * counter.send { it + 1 }
 * counter.send { it + 1 }
 * assert await(counter.getAsync()) == 2
 *
 * // Java:
 * Agent<Integer> counter = Agent.create(0);
 * counter.send(n -> n + 1);
 * Awaitable<Integer> result = counter.sendAndGet(n -> n + 1);
 * }</pre>
 *
 * @param <T> the value type
 * @see Pool
 * @see AsyncScope
 * @since 6.0.0
 */
public final class Agent<T> {

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ExecutorService updateExecutor;
    private volatile T value;

    private Agent(T initialValue, ExecutorService executor) {
        this.value = initialValue;
        this.updateExecutor = executor;
    }

    /**
     * Creates an agent with the given initial value, using a
     * single-thread executor for serialised updates.
     *
     * @param initialValue the starting value
     * @param <T>          the value type
     * @return a new agent
     */
    public static <T> Agent<T> create(T initialValue) {
        return new Agent<>(initialValue,
                Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "groovy-agent");
                    t.setDaemon(true);
                    return t;
                }));
    }

    /**
     * Creates an agent backed by the given pool for update execution.
     * Updates are still serialised (only one at a time), but they run
     * on the pool's threads.
     *
     * @param initialValue the starting value
     * @param pool         the pool to use for updates
     * @param <T>          the value type
     * @return a new agent
     */
    public static <T> Agent<T> create(T initialValue, Pool pool) {
        Objects.requireNonNull(pool, "pool must not be null");
        // Use a SerialExecutor to serialise updates on the pool's threads.
        // We cannot use newSingleThreadExecutor with a delegating ThreadFactory
        // because that breaks the executor's internal task loop.
        return new Agent<>(initialValue, new SerialExecutor(pool));
    }

    /**
     * Returns the current value. This is a non-blocking snapshot read.
     *
     * @return the current value
     */
    public T get() {
        rwLock.readLock().lock();
        try {
            return value;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Returns the current value as an {@link Awaitable}. The awaitable
     * completes after all previously queued updates have been applied,
     * ensuring a consistent read.
     *
     * @return an awaitable holding the value after pending updates
     */
    public Awaitable<T> getAsync() {
        CompletableFuture<T> cf = new CompletableFuture<>();
        updateExecutor.execute(() -> {
            rwLock.readLock().lock();
            try {
                cf.complete(value);
            } finally {
                rwLock.readLock().unlock();
            }
        });
        return GroovyPromise.of(cf);
    }

    /**
     * Queues an update function to be applied to the current value.
     * The function receives the current value and returns the new value.
     * <p>
     * Updates are applied asynchronously and serialised: only one update
     * runs at a time.
     *
     * @param updateFn a function from current value to new value
     */
    public void send(Function<T, T> updateFn) {
        Objects.requireNonNull(updateFn, "update function must not be null");
        updateExecutor.execute(() -> applyUpdate(updateFn));
    }

    /**
     * Queues an update function and returns an {@link Awaitable} that
     * completes with the new value after the update is applied.
     *
     * @param updateFn a function from current value to new value
     * @return an awaitable holding the new value
     */
    public Awaitable<T> sendAndGet(Function<T, T> updateFn) {
        Objects.requireNonNull(updateFn, "update function must not be null");
        CompletableFuture<T> cf = new CompletableFuture<>();
        updateExecutor.execute(() -> {
            try {
                T newVal = applyUpdate(updateFn);
                cf.complete(newVal);
            } catch (Throwable t) {
                cf.completeExceptionally(t);
            }
        });
        return GroovyPromise.of(cf);
    }

    /**
     * Shuts down the agent's update executor. No further updates will
     * be accepted. Pending updates are executed before shutdown completes.
     */
    public void shutdown() {
        updateExecutor.shutdown();
    }

    @Override
    public String toString() {
        return "Agent[" + get() + "]";
    }

    private T applyUpdate(Function<T, T> updateFn) {
        rwLock.writeLock().lock();
        try {
            value = updateFn.apply(value);
            return value;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * An executor that serialises task execution on a delegate executor.
     * Tasks are queued and executed one at a time — when one completes,
     * the next is submitted to the delegate.
     */
    private static final class SerialExecutor implements ExecutorService {
        private final Executor delegate;
        private final java.util.Queue<Runnable> queue = new java.util.concurrent.ConcurrentLinkedQueue<>();
        private final java.util.concurrent.atomic.AtomicBoolean active = new java.util.concurrent.atomic.AtomicBoolean();
        private volatile boolean shutdown;

        SerialExecutor(Executor delegate) {
            this.delegate = delegate;
        }

        @Override
        public void execute(Runnable command) {
            if (shutdown) throw new java.util.concurrent.RejectedExecutionException("shutdown");
            queue.add(command);
            scheduleNext();
        }

        private void scheduleNext() {
            if (!queue.isEmpty() && active.compareAndSet(false, true)) {
                delegate.execute(() -> {
                    Runnable task = queue.poll();
                    if (task != null) {
                        try {
                            task.run();
                        } finally {
                            active.set(false);
                            scheduleNext();
                        }
                    } else {
                        active.set(false);
                    }
                });
            }
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public java.util.List<Runnable> shutdownNow() {
            shutdown = true;
            return java.util.List.of();
        }

        @Override public boolean isShutdown() { return shutdown; }
        @Override public boolean isTerminated() { return shutdown && queue.isEmpty(); }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) {
            return isTerminated();
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(java.util.concurrent.Callable<T> task) {
            java.util.concurrent.FutureTask<T> ft = new java.util.concurrent.FutureTask<>(task);
            execute(ft);
            return ft;
        }

        @Override
        public <T> java.util.concurrent.Future<T> submit(Runnable task, T result) {
            java.util.concurrent.FutureTask<T> ft = new java.util.concurrent.FutureTask<>(task, result);
            execute(ft);
            return ft;
        }

        @Override
        public java.util.concurrent.Future<?> submit(Runnable task) {
            java.util.concurrent.FutureTask<Void> ft = new java.util.concurrent.FutureTask<>(task, null);
            execute(ft);
            return ft;
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> java.util.List<java.util.concurrent.Future<T>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<T>> tasks, long timeout, java.util.concurrent.TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }
}
