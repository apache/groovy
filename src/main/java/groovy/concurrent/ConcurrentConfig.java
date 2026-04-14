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

import org.apache.groovy.runtime.async.AsyncSupport;

/**
 * Central configuration for Groovy's concurrent and parallel features.
 * <p>
 * Configuration is resolved in order:
 * <ol>
 *   <li>Programmatic overrides via setter methods</li>
 *   <li>System properties ({@code groovy.concurrent.poolsize},
 *       {@code groovy.concurrent.virtual})</li>
 *   <li>Sensible defaults (processors + 1, virtual threads on JDK 21+)</li>
 * </ol>
 * <p>
 * Inspired by GPars' {@code GParsConfig} and {@code PoolUtils}, modernised
 * for virtual threads and the {@link Pool} abstraction.
 *
 * @see Pool
 * @since 6.0.0
 */
public final class ConcurrentConfig {

    private static volatile int poolSizeOverride = 0;  // 0 means "not set"
    private static volatile Pool defaultPool;

    private ConcurrentConfig() { }

    /**
     * Returns the default parallelism level.
     * <p>
     * Checks (in order): programmatic override, system property
     * {@code groovy.concurrent.poolsize}, then
     * {@code Runtime.availableProcessors() + 1} (following the GPars convention).
     *
     * @return the default pool size, always &gt; 0
     */
    public static int getDefaultParallelism() {
        if (poolSizeOverride > 0) return poolSizeOverride;
        try {
            String prop = System.getProperty("groovy.concurrent.poolsize");
            if (prop != null) {
                int val = Integer.parseInt(prop);
                if (val > 0) return val;
            }
        } catch (SecurityException | NumberFormatException ignored) { }
        return Runtime.getRuntime().availableProcessors() + 1;
    }

    /**
     * Sets the default parallelism level programmatically.
     * Pass {@code 0} or a negative value to reset to the default.
     *
     * @param size the pool size, or &le; 0 to reset
     */
    public static void setDefaultParallelism(int size) {
        poolSizeOverride = Math.max(0, size);
    }

    /**
     * Returns {@code true} if virtual threads should be preferred.
     * <p>
     * Checks the system property {@code groovy.concurrent.virtual}
     * (default: {@code true} on JDK 21+, {@code false} otherwise).
     *
     * @return whether to prefer virtual threads
     */
    public static boolean preferVirtualThreads() {
        try {
            String prop = System.getProperty("groovy.concurrent.virtual");
            if (prop != null) return Boolean.parseBoolean(prop);
        } catch (SecurityException ignored) { }
        return AsyncSupport.isVirtualThreadsAvailable();
    }

    /**
     * Returns the global default pool. If none has been set, creates one
     * based on current configuration: a virtual-thread pool if
     * {@link #preferVirtualThreads()} is {@code true}, otherwise a
     * fixed pool sized by {@link #getDefaultParallelism()}.
     *
     * @return the default pool, never {@code null}
     */
    public static Pool getDefaultPool() {
        Pool p = defaultPool;
        if (p != null) return p;
        synchronized (ConcurrentConfig.class) {
            p = defaultPool;
            if (p != null) return p;
            p = preferVirtualThreads() ? Pool.virtual() : Pool.fixed(getDefaultParallelism());
            defaultPool = p;
            return p;
        }
    }

    /**
     * Sets the global default pool. Pass {@code null} to reset.
     *
     * @param pool the pool to use as default, or {@code null} to reset
     */
    public static void setDefaultPool(Pool pool) {
        defaultPool = pool;
    }
}
