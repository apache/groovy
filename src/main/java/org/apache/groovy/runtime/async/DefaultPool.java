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
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Default implementation of {@link Pool}.
 * <p>
 * Sized pools ({@link #fixed(int)}, {@link #cpu()}) use {@link ForkJoinPool}
 * for work-stealing and parallel stream isolation. {@link #virtual()} and
 * {@link #io()} use virtual threads when that executor can be created.
 * Every worker is created with the pool it belongs to. {@link #current()}
 * uses that when the thread has no explicit binding, so a nested call sees
 * the pool without rebinding on each element.
 *
 * @see Pool
 * @since 6.0.0
 */
public final class DefaultPool implements Pool {

    // ---- Virtual thread detection ----------------------------------------

    private static final MethodHandle NEW_VT_EXECUTOR;
    private static final MethodHandle NEW_THREAD_PER_TASK;
    private static final MethodHandle VIRTUAL_NAMED_FACTORY;
    private static final MethodType NEW_VT_EXECUTOR_TYPE = MethodType.methodType(ExecutorService.class);

    /**
     * Pool carried by a platform or virtual worker for its whole lifetime.
     * Fork-join workers store the pool on the thread instead; this local
     * is only for executors whose threads cannot be a {@link PoolWorker}.
     */
    private static final ThreadLocal<Pool> CARRIER = new ThreadLocal<>();

    static {
        MethodHandle virtualExecutor = null;
        MethodHandle perTask = null;
        MethodHandle namedFactory = null;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandle factory = lookup.findStatic(
                    Executors.class, "newVirtualThreadPerTaskExecutor",
                    NEW_VT_EXECUTOR_TYPE)
                    .asType(NEW_VT_EXECUTOR_TYPE);
            // JDK 19 and 20 expose this method but throw unless preview
            // features are enabled. A successful lookup is not enough.
            ExecutorService probe = (ExecutorService) factory.invokeExact();
            probe.shutdown();
            virtualExecutor = factory;
        } catch (Throwable ignored) { }
        if (virtualExecutor != null) {
            try {
                perTask = lookup.findStatic(Executors.class, "newThreadPerTaskExecutor",
                        MethodType.methodType(ExecutorService.class, ThreadFactory.class))
                        .asType(MethodType.methodType(ExecutorService.class, ThreadFactory.class));
                namedFactory = virtualNamedFactory(lookup);
            } catch (Throwable ignored) {
                perTask = null;
                namedFactory = null;
            }
        }
        NEW_VT_EXECUTOR = virtualExecutor;
        NEW_THREAD_PER_TASK = perTask;
        VIRTUAL_NAMED_FACTORY = namedFactory;
    }

    /**
     * {@code Thread.ofVirtual().name(prefix, start).factory()}, or null when
     * that API cannot be linked. The handles stay in one place so a native
     * image can register the same methods the runtime looks up.
     */
    private static MethodHandle virtualNamedFactory(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
        Class<?> ofVirtualClass = Class.forName("java.lang.Thread$Builder$OfVirtual");
        Class<?> builderClass = Class.forName("java.lang.Thread$Builder");
        MethodHandle ofVirtual = lookup.findStatic(Thread.class, "ofVirtual",
                MethodType.methodType(ofVirtualClass));
        MethodHandle name = lookup.findVirtual(ofVirtualClass, "name",
                MethodType.methodType(ofVirtualClass, String.class, long.class));
        MethodHandle factory = lookup.findVirtual(builderClass, "factory",
                MethodType.methodType(ThreadFactory.class));
        MethodHandle named = MethodHandles.collectArguments(name, 0, ofVirtual);
        MethodHandle asBuilder = named.asType(
                MethodType.methodType(builderClass, String.class, long.class));
        return MethodHandles.collectArguments(factory, 0, asBuilder).asType(
                MethodType.methodType(ThreadFactory.class, String.class, long.class));
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
        AtomicReference<Pool> owner = new AtomicReference<>();
        ExecutorService executor = newVirtualExecutor(owner);
        boolean virtual = executor != null;
        if (!virtual) {
            executor = newCachedCarrierPool(owner);
        }
        Pool pool = new DefaultPool(executor, Integer.MAX_VALUE, virtual);
        owner.set(pool);
        return pool;
    }

    /**
     * A virtual-thread-per-task executor whose threads carry {@code owner},
     * or null when virtual threads cannot be created. The plain virtual
     * executor is the fallback when a custom factory cannot be linked;
     * those threads do not carry the pool, and the task binds it instead.
     */
    private static ExecutorService newVirtualExecutor(AtomicReference<Pool> owner) {
        if (NEW_VT_EXECUTOR == null) {
            return null;
        }
        if (NEW_THREAD_PER_TASK != null && VIRTUAL_NAMED_FACTORY != null) {
            try {
                ThreadFactory threads = (ThreadFactory) VIRTUAL_NAMED_FACTORY.invokeExact(
                        "groovy-pool-virtual-", 0L);
                ExecutorService executor = (ExecutorService) NEW_THREAD_PER_TASK.invokeExact(
                        (ThreadFactory) new CarrierFactory(threads, owner));
                return executor;
            } catch (Throwable ignored) {
                // Fall through to the executor that has no factory hook.
            }
        }
        try {
            return (ExecutorService) NEW_VT_EXECUTOR.invokeExact();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static ExecutorService newCachedCarrierPool(AtomicReference<Pool> owner) {
        AtomicLong counter = new AtomicLong();
        return Executors.newCachedThreadPool(task -> {
            Thread thread = new Thread(carrier(owner, task),
                    "groovy-pool-virtual-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });
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
        return forkJoin(size);
    }

    /**
     * Creates a pool sized to {@code availableProcessors()}, backed by
     * {@link ForkJoinPool}. Ideal for CPU-bound work and parallel collections.
     * The parallelism cap matches {@link #fixed(int)}.
     */
    public static Pool cpu() {
        return forkJoin(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Publishes {@code owner} before the pool is returned. Fork-join workers
     * are started lazily, on the first task, so a callback reads the pool
     * that was stored here and not a value captured too early.
     */
    private static Pool forkJoin(int size) {
        AtomicReference<Pool> owner = new AtomicReference<>();
        ForkJoinPool forkJoin = new ForkJoinPool(size, pool -> new PoolWorker(pool, owner), null, false);
        Pool created = new DefaultPool(forkJoin, size, false);
        owner.set(created);
        return created;
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
     * Returns the pool bound to the current scope, or the pool carried by
     * the current worker, or {@code null}. An explicit binding wins, including
     * a binding of {@code null}. A thread that is not one of this pool's
     * workers, including a common-pool worker, does not carry a pool.
     */
    public static Pool current() {
        if (CURRENT_POOL.isBound()) {
            return CURRENT_POOL.orElse(null);
        }
        Thread thread = Thread.currentThread();
        if (thread instanceof PoolWorker worker) {
            return worker.owner.get();
        }
        return CARRIER.get();
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

    /** Runs {@code task} with {@code owner} installed for the life of that run. */
    private static Runnable carrier(AtomicReference<Pool> owner, Runnable task) {
        return () -> {
            CARRIER.set(owner.get());
            try {
                task.run();
            } finally {
                CARRIER.remove();
            }
        };
    }

    /**
     * A fork-join worker that reports {@link #owner}. The reference is read
     * when a task asks for the current pool, after {@link #forkJoin(int)}
     * has published it.
     */
    private static final class PoolWorker extends ForkJoinWorkerThread {
        private final AtomicReference<Pool> owner;

        private PoolWorker(ForkJoinPool forkJoin, AtomicReference<Pool> owner) {
            super(forkJoin);
            this.owner = owner;
        }
    }

    /** Virtual-thread factory that carries {@link #owner} on each new thread. */
    private static final class CarrierFactory implements ThreadFactory {
        private final ThreadFactory delegate;
        private final AtomicReference<Pool> owner;

        private CarrierFactory(ThreadFactory delegate, AtomicReference<Pool> owner) {
            this.delegate = delegate;
            this.owner = owner;
        }

        @Override
        public Thread newThread(Runnable task) {
            return delegate.newThread(carrier(owner, task));
        }
    }
}
