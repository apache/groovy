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

import org.apache.groovy.runtime.async.DefaultPool;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * A managed thread pool for parallel and concurrent operations.
 * <p>
 * {@code Pool} extends {@link Executor} so it can be used anywhere an
 * executor is expected — including {@link AsyncScope#withScope(Executor,
 * java.util.function.Function) AsyncScope.withScope(executor, body)}.
 * It also implements {@link AutoCloseable} for use in try-with-resources
 * blocks, ensuring clean shutdown.
 * <p>
 * Factory methods provide common pool configurations:
 * <ul>
 *   <li>{@link #virtual()} — virtual-thread-per-task (JDK 21+), ideal for I/O</li>
 *   <li>{@link #fixed(int)} — fixed-size thread pool with daemon threads</li>
 *   <li>{@link #cpu()} — sized to {@code availableProcessors()}, for CPU-bound work</li>
 *   <li>{@link #io()} — larger pool for I/O-bound or blocking operations</li>
 * </ul>
 * <p>
 * Inspired by GPars' {@code PGroup} pool management, modernised for
 * virtual threads.
 *
 * <pre>{@code
 * try (var pool = Pool.cpu()) {
 *     AsyncScope.withScope(pool, scope -> {
 *         scope.async(() -> cpuIntensiveWork());
 *         scope.async(() -> moreCpuWork());
 *         return null;
 *     });
 * }
 * }</pre>
 *
 * @see AsyncScope
 * @see ConcurrentConfig
 * @since 6.0.0
 */
public interface Pool extends Executor, AutoCloseable {

    /**
     * Creates a virtual-thread-per-task pool (JDK 21+).
     * <p>
     * Each submitted task runs on its own virtual thread. This is ideal
     * for I/O-bound workloads where tasks spend time waiting. On JDK
     * versions that do not support virtual threads, falls back to a
     * cached daemon thread pool.
     *
     * @return a new virtual thread pool
     */
    static Pool virtual() {
        return DefaultPool.virtual();
    }

    /**
     * Creates a fixed-size thread pool with daemon threads.
     *
     * @param size the number of threads (must be &gt; 0)
     * @return a new fixed pool
     * @throws IllegalArgumentException if size &le; 0
     */
    static Pool fixed(int size) {
        return DefaultPool.fixed(size);
    }

    /**
     * Creates a pool sized to {@code Runtime.availableProcessors()},
     * suitable for CPU-bound work.
     *
     * @return a new CPU pool
     */
    static Pool cpu() {
        return DefaultPool.cpu();
    }

    /**
     * Creates a larger pool suitable for I/O-bound or blocking operations.
     * <p>
     * If virtual threads are available, returns a virtual thread pool
     * (the ideal choice for I/O). Otherwise returns a fixed pool sized
     * to {@link ConcurrentConfig#getDefaultParallelism()}.
     *
     * @return a new I/O pool
     */
    static Pool io() {
        return DefaultPool.io();
    }

    /**
     * Returns the pool bound to the current scope, or {@code null}.
     * <p>
     * Set by {@link ParallelScope#withPool} using {@code ScopedValue}
     * on JDK 25+ or {@code ThreadLocal} on earlier JDKs.
     *
     * @return the current pool, or {@code null} if none is bound
     * @since 6.0.0
     */
    static Pool current() {
        return DefaultPool.current();
    }

    /**
     * Executes the supplier with the given pool as current,
     * restoring the previous binding afterwards.
     *
     * @param pool     the pool to bind
     * @param supplier the work to execute
     * @param <T>      the result type
     * @return the supplier's result
     * @since 6.0.0
     */
    static <T> T withCurrent(Pool pool, Supplier<T> supplier) {
        return DefaultPool.withCurrent(pool, supplier);
    }

    /**
     * Returns the configured pool size.
     * For virtual thread pools, returns {@code Integer.MAX_VALUE}.
     */
    int getPoolSize();

    /**
     * Returns the approximate number of threads actively executing tasks.
     */
    int getActiveCount();

    /**
     * Returns {@code true} if this pool uses virtual threads.
     */
    boolean usesVirtualThreads();

    /**
     * Returns the underlying {@link ForkJoinPool}, if this pool is backed
     * by one. Required for parallel stream isolation.
     *
     * @return the ForkJoinPool
     * @throws UnsupportedOperationException if this pool is not ForkJoinPool-backed
     *         (e.g., virtual thread pools)
     * @since 6.0.0
     */
    ForkJoinPool asForkJoinPool();

    /**
     * Initiates an orderly shutdown. Previously submitted tasks are
     * executed, but no new tasks will be accepted.
     */
    void shutdown();

    /**
     * Shuts down the pool. Equivalent to {@link #shutdown()}.
     */
    @Override
    default void close() {
        shutdown();
    }
}
