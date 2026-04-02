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

/**
 * Service Provider Interface (SPI) for adapting third-party asynchronous types
 * to Groovy's {@link Awaitable} abstraction and to blocking iterables for
 * {@code for await} loops.
 * <p>
 * Implementations are discovered automatically via {@link java.util.ServiceLoader}.
 * To register an adapter, create a file
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter} containing the
 * fully-qualified class name of your implementation.
 *
 * @see AwaitableAdapterRegistry
 * @since 6.0.0
 */
public interface AwaitableAdapter {

    /**
     * Returns {@code true} if this adapter can convert instances of the given
     * type to {@link Awaitable} (single-value async result).
     */
    boolean supportsAwaitable(Class<?> type);

    /**
     * Converts the given source object to an {@link Awaitable}.
     * Called only when {@link #supportsAwaitable} returned {@code true}.
     */
    <T> Awaitable<T> toAwaitable(Object source);

    /**
     * Returns {@code true} if this adapter can convert instances of the given
     * type to a blocking {@link Iterable} for {@code for await} loops.
     * Defaults to {@code false}; override for multi-value async types
     * (e.g., Reactor {@code Flux}, RxJava {@code Observable}).
     */
    default boolean supportsIterable(Class<?> type) {
        return false;
    }

    /**
     * Converts the given source object to a blocking {@link Iterable}.
     * Called only when {@link #supportsIterable} returned {@code true}.
     * The returned iterable should block on {@code next()} until the
     * next element is available — with virtual threads this is efficient.
     */
    default <T> Iterable<T> toBlockingIterable(Object source) {
        throw new UnsupportedOperationException("Iterable conversion not supported by " + getClass().getName());
    }
}
