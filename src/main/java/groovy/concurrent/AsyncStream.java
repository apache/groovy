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
 * Asynchronous iteration abstraction, analogous to C#'s
 * {@code IAsyncEnumerable<T>} or JavaScript's async iterables.
 * <p>
 * Used with the {@code for await} syntax:
 * <pre>
 * for await (item in asyncStream) {
 *     process(item)
 * }
 * </pre>
 * <p>
 * An {@code AsyncStream} can be produced in several ways:
 * <ul>
 *   <li>Using {@code yield return} inside an {@code async} method or closure
 *       to create a generator-style stream</li>
 *   <li>Adapting JDK {@link java.util.concurrent.Flow.Publisher} instances
 *       (supported out of the box by the built-in adapter)</li>
 *   <li>Adapting third-party reactive types (Reactor {@code Flux}, RxJava
 *       {@code Observable}) via {@link AwaitableAdapter}</li>
 * </ul>
 *
 * @param <T> the element type
 * @see AwaitableAdapter
 * @see AwaitableAdapterRegistry
 * @since 6.0.0
 */
public interface AsyncStream<T> extends AutoCloseable {

    /**
     * Asynchronously advances to the next element. Returns an {@link Awaitable}
     * that completes with {@code true} if an element is available, or
     * {@code false} if the stream is exhausted.
     */
    Awaitable<Boolean> moveNext();

    /**
     * Returns the current element. Must only be called after {@link #moveNext()}
     * has completed with {@code true}.
     */
    T getCurrent();

    /**
     * Closes the stream and releases any associated resources.
     * <p>
     * The default implementation is a no-op.  Implementations that bridge to
     * generators, publishers, or other resource-owning sources may override
     * this to propagate cancellation upstream.  Compiler-generated
     * {@code for await} loops invoke {@code close()} automatically from a
     * {@code finally} block, including on early {@code break}, {@code return},
     * and exceptional exit.
     *
     * @since 6.0.0
     */
    @Override
    default void close() {
    }

    /**
     * Returns an empty {@code AsyncStream} that completes immediately.
     */
    @SuppressWarnings("unchecked")
    static <T> AsyncStream<T> empty() {
        return (AsyncStream<T>) EMPTY;
    }

    /** Singleton empty stream instance. */
    AsyncStream<Object> EMPTY = new AsyncStream<>() {
        @Override public Awaitable<Boolean> moveNext() { return Awaitable.of(false); }
        @Override public Object getCurrent() { return null; }
    };
}
