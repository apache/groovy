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
package org.apache.groovy.runtime.async;

import groovy.concurrent.ConcurrentConfig;
import groovy.concurrent.Pool;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Default implementation of {@link Pool}.
 * <p>
 * Sized pools ({@link #fixed(int)}, {@link #cpu()}) use {@link ForkJoinPool}
 * for work-stealing and parallel stream isolation. {@link #virtual()} and
 * {@link #io()} use virtual threads when that executor can be created.
 *
 * @see Pool
 * @since 6.0.0
 */
public final class DefaultPool implements Pool {

    // ---- Virtual thread detection ----------------------------------------

    private static final MethodHandle NEW_VT_EXECUTOR;
    private static final MethodType NEW_VT_EXECUTOR_TYPE = MethodType.methodType(ExecutorService.class);

    static {
        MethodHandle mh = null;
        try {
            MethodHandle factory = MethodHandles.lookup().findStatic(
                    Executors.class, "newVirtualThreadPerTaskExecutor",
                    NEW_VT_EXECUTOR_TYPE)
                    .asType(NEW_VT_EXECUTOR_TYPE);
            // JDK 19 and 20 expose this method but throw unless preview
            // features are enabled. A successful lookup is not enough.
            ExecutorService probe = (ExecutorService) factory.invokeExact();
            probe.shutdown();
            mh = factory;
        } catch (Throwable ignored) { }
        NEW_VT_EXECUTOR = mh;
    }

    private static final ScopedLocal<Pool> CURRENT_POOL = ScopedLocal.newInstance();

    // ---- Instance fields -------------------------------------------------

    private final ExecutorService executor;
    private final int poolSize;
    private final boolean virtualThreads;

    private DefaultPool(ExecutorService executor, int poolSize, boolean virtualThreads) {
        this.executor = executor;
        this.poolSize = poolSize;
        this.virtualThreads = virtualThreads;
    }

    // ---- Factory methods ------------------------------------------------

    /**
     * Creates a virtual-thread-per-task pool, or a cached daemon pool
     * when virtual threads cannot be created.
     */
    public static Pool virtual() {
        if (NEW_VT_EXECUTOR != null) {
            try {
                ExecutorService es = (ExecutorService) NEW_VT_EXECUTOR.invokeExact();
                return new DefaultPool(es, Integer.MAX_VALUE, true);
            } catch (Throwable ignored) { }
        }
        AtomicLong counter = new AtomicLong();
        ExecutorService es = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "groovy-pool-virtual-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
        return new DefaultPool(es, Integer.MAX_VALUE, false);
    }

    /**
     * Creates a fixed-size pool backed by {@link ForkJoinPool} for
     * work-stealing and parallel stream isolation.
     * <p>
     * Parallelism is {@code size} and is a hard cap. Blocked workers are
     * not replaced, so CPU-bound work cannot raise the thread count.
     * A parallel-collection callback that blocks can stall the pool when
     * every worker blocks with a sibling still queued; leave one spare
     * thread, or use {@link #virtual()} / {@link #io()} for waiting work.
     */
    public static Pool fixed(int size) {
        if (size <= 0) throw new IllegalArgumentException("Pool size must be > 0, got: " + size);
        return new DefaultPool(new ForkJoinPool(size), size, false);
    }

    /**
     * Creates a pool sized to {@code availableProcessors()}, backed by
     * {@link ForkJoinPool}. Ideal for CPU-bound work and parallel collections.
     * The parallelism cap matches {@link #fixed(int)}.
     */
    public static Pool cpu() {
        int size = Runtime.getRuntime().availableProcessors();
        return new DefaultPool(new ForkJoinPool(size), size, false);
    }

    /**
     * Creates a pool for I/O-bound work. Uses virtual threads when that
     * executor can be created, otherwise a fixed pool sized by
     * {@link ConcurrentConfig#getDefaultParallelism()}.
     */
    public static Pool io() {
        if (NEW_VT_EXECUTOR != null) {
            return virtual();
        }
        return fixed(ConcurrentConfig.getDefaultParallelism());
    }

    // ---- Pool.current() -------------------------------------------------

    /**
     * Returns the pool bound to the current scope, or {@code null}.
     */
    public static Pool current() {
        return CURRENT_POOL.orElse(null);
    }

    /**
     * Executes the supplier with the given pool as current.
     */
    public static <T> T withCurrent(Pool pool, Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier must not be null");
        return CURRENT_POOL.where(pool, supplier);
    }

    // ---- Pool interface -------------------------------------------------

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public int getActiveCount() {
        if (executor instanceof ForkJoinPool fjp) {
            return fjp.getActiveThreadCount();
        }
        return -1;
    }

    @Override
    public boolean usesVirtualThreads() {
        return virtualThreads;
    }

    @Override
    public ForkJoinPool asForkJoinPool() {
        if (executor instanceof ForkJoinPool fjp) {
            return fjp;
        }
        throw new UnsupportedOperationException(
                "This pool is not backed by a ForkJoinPool (virtual=" + virtualThreads + ")");
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public String toString() {
        return "Pool[size=" + (poolSize == Integer.MAX_VALUE ? "unbounded" : poolSize)
                + ", virtual=" + virtualThreads
                + ", forkJoin=" + (executor instanceof ForkJoinPool) + "]";
    }
}
