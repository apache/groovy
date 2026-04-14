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

import java.util.Objects;
import java.util.function.Function;

/**
 * Convenience API for running work within a pool-isolated
 * {@link AsyncScope}.
 * <p>
 * {@code ParallelScope} combines {@link Pool} and {@link AsyncScope}
 * in a single call: the pool is created (or provided), used as the
 * executor for the scope, and shut down when the scope exits.
 * <p>
 * This is the Groovy successor to GPars' {@code GParsPool.withPool}
 * and {@code PGroup} scoping pattern, modernised for structured
 * concurrency and virtual threads.
 *
 * <pre>{@code
 * // Groovy:
 * ParallelScope.withPool(4) { scope ->
 *     def a = scope.async { cpuWork1() }
 *     def b = scope.async { cpuWork2() }
 *     [await(a), await(b)]
 * }
 *
 * // With a pre-configured pool:
 * def pool = Pool.cpu()
 * ParallelScope.withPool(pool) { scope ->
 *     scope.async { work() }
 *     null
 * }
 * pool.close()
 * }</pre>
 *
 * @see Pool
 * @see AsyncScope
 * @since 6.0.0
 */
public final class ParallelScope {

    private ParallelScope() { }

    /**
     * Creates a fixed-size pool, executes the body within a scoped context,
     * and shuts down the pool on exit.
     *
     * @param threads the number of threads in the pool
     * @param body    the function receiving the scope
     * @param <T>     the result type
     * @return the body's return value
     */
    public static <T> T withPool(int threads, Function<AsyncScope, T> body) {
        Objects.requireNonNull(body, "body must not be null");
        try (Pool pool = Pool.fixed(threads)) {
            return Pool.withCurrent(pool, () -> AsyncScope.withScope(pool, body));
        }
    }

    /**
     * Executes the body within a scope backed by the given pool.
     * <p>
     * The pool is <em>not</em> shut down on exit — the caller owns
     * the pool's lifecycle. Both {@link AsyncScope#current()} and
     * {@link Pool#current()} are bound for the duration.
     *
     * @param pool the pool to use as executor
     * @param body the function receiving the scope
     * @param <T>  the result type
     * @return the body's return value
     */
    public static <T> T withPool(Pool pool, Function<AsyncScope, T> body) {
        Objects.requireNonNull(pool, "pool must not be null");
        Objects.requireNonNull(body, "body must not be null");
        return Pool.withCurrent(pool, () -> AsyncScope.withScope(pool, body));
    }
}
