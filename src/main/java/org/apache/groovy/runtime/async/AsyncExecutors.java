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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executor and scheduler configuration for the async runtime.
 * <p>
 * Package-private implementation detail of {@link AsyncSupport}. On JDK&nbsp;21+
 * the default executor is a virtual-thread-per-task executor; earlier JDKs use a
 * bounded cached daemon pool sized by {@code groovy.async.parallelism}.
 *
 * @since 6.0.0
 */
final class AsyncExecutors {

    private static final boolean VIRTUAL_THREADS_AVAILABLE;
    private static final Executor VIRTUAL_THREAD_EXECUTOR;
    private static final Executor FALLBACK_EXECUTOR;

    static {
        Executor vtExecutor = null;
        boolean vtAvailable = false;
        try {
            MethodHandle mh = MethodHandles.lookup().findStatic(
                    Executors.class, "newVirtualThreadPerTaskExecutor",
                    MethodType.methodType(ExecutorService.class));
            vtExecutor = (Executor) mh.invoke();
            vtAvailable = true;
        } catch (Throwable ignored) {
            // JDK < 21 — virtual threads not available
        }
        VIRTUAL_THREAD_EXECUTOR = vtExecutor;
        VIRTUAL_THREADS_AVAILABLE = vtAvailable;

        int maxThreads = getIntegerSafe("groovy.async.parallelism", 256);
        if (!VIRTUAL_THREADS_AVAILABLE) {
            FALLBACK_EXECUTOR = new ThreadPoolExecutor(
                    0, maxThreads,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        @SuppressWarnings("deprecation")
                        long id = t.getId();
                        t.setName("groovy-async-" + id);
                        return t;
                    },
                    new ThreadPoolExecutor.CallerRunsPolicy());
        } else {
            FALLBACK_EXECUTOR = null;
        }
    }

    private static volatile Executor defaultExecutor = createDefaultExecutor();

    private static final ScheduledExecutorService SCHEDULER =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "groovy-async-scheduler");
                t.setDaemon(true);
                return t;
            });

    private AsyncExecutors() { }

    static boolean isVirtualThreadsAvailable() {
        return VIRTUAL_THREADS_AVAILABLE;
    }

    static Executor getExecutor() {
        return defaultExecutor;
    }

    /**
     * Sets the executor used for async tasks. Passing {@code null} restores the
     * platform default (virtual threads on JDK&nbsp;21+, cached pool otherwise).
     */
    static void setExecutor(Executor executor) {
        defaultExecutor = executor != null ? executor : createDefaultExecutor();
    }

    static void resetExecutor() {
        defaultExecutor = createDefaultExecutor();
    }

    static ScheduledExecutorService getScheduler() {
        return SCHEDULER;
    }

    private static Executor createDefaultExecutor() {
        return VIRTUAL_THREADS_AVAILABLE ? VIRTUAL_THREAD_EXECUTOR : FALLBACK_EXECUTOR;
    }

    private static int getIntegerSafe(String name, int defaultValue) {
        try {
            return Integer.getInteger(name, defaultValue);
        } catch (SecurityException ignore) {
            return defaultValue;
        }
    }
}
