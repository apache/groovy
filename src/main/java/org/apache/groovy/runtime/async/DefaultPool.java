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
 * for work-stealing and parallel stream isolation. I/O pools
 * ({@link #virtual()}, {@link #io()}) use virtual threads on JDK 21+.
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
            mh = MethodHandles.lookup().findStatic(
                    Executors.class, "newVirtualThreadPerTaskExecutor",
                    NEW_VT_EXECUTOR_TYPE)
                    .asType(NEW_VT_EXECUTOR_TYPE);
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
     * as fallback on JDK &lt; 21.
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
     */
    public static Pool fixed(int size) {
        if (size <= 0) throw new IllegalArgumentException("Pool size must be > 0, got: " + size);
        ForkJoinPool fjp = new ForkJoinPool(size);
        return new DefaultPool(fjp, size, false);
    }

    /**
     * Creates a pool sized to {@code availableProcessors()}, backed by
     * {@link ForkJoinPool}. Ideal for CPU-bound work and parallel collections.
     */
    public static Pool cpu() {
        int size = Runtime.getRuntime().availableProcessors();
        ForkJoinPool fjp = new ForkJoinPool(size);
        return new DefaultPool(fjp, size, false);
    }

    /**
     * Creates a pool for I/O-bound work. Uses virtual threads if available,
     * otherwise a fixed pool sized by {@link ConcurrentConfig#getDefaultParallelism()}.
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
