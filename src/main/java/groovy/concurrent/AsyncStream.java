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
 * Asynchronous iteration abstraction for producing and consuming
 * values that arrive over time.
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
 *   <li>Viewing a {@link AsyncChannel} as a stream via {@link AsyncChannel#asStream()} —
 *       each {@code moveNext()} call maps to a {@code receive()}, and
 *       {@link ChannelClosedException} is translated to end-of-stream.
 *       This conversion is registered automatically, so {@code for await (item in channel)}
 *       works out of the box.</li>
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
     * Converts the given source to an {@code AsyncStream}.
     * <p>
     * If the source is already an {@code AsyncStream}, it is returned as-is.
     * Otherwise, the {@link AwaitableAdapterRegistry} is consulted to find a
     * suitable adapter. Built-in adapters handle {@link Iterable} and
     * {@link java.util.Iterator}; the auto-discovered {@code FlowPublisherAdapter}
     * handles {@link java.util.concurrent.Flow.Publisher}; third-party frameworks
     * can register additional adapters via the registry.
     * <p>
     * This is the recommended entry point for converting external collection or
     * reactive types to {@code AsyncStream}:
     * <pre>
     * AsyncStream&lt;String&gt; stream = AsyncStream.from(myList)
     * AsyncStream&lt;Integer&gt; stream2 = AsyncStream.from(myFlowPublisher)
     * </pre>
     *
     * @param source the source object; must not be {@code null}
     * @param <T>    the element type
     * @return an async stream backed by the source
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     * @see AwaitableAdapterRegistry#toAsyncStream(Object)
     * @since 6.0.0
     */
    @SuppressWarnings("unchecked")
    static <T> AsyncStream<T> from(Object source) {
        return AwaitableAdapterRegistry.toAsyncStream(source);
    }

    /**
     * Returns an empty {@code AsyncStream} that completes immediately.
     */
    @SuppressWarnings("unchecked")
    static <T> AsyncStream<T> empty() {
        return (AsyncStream<T>) EMPTY;
    }

    /**
     * Cached awaitable for {@code moveNext()} returning {@code true}.
     * Eliminates per-call allocation on the hot path.  Shared by all
     * {@code AsyncStream} implementations (e.g. via
     * {@link org.apache.groovy.runtime.async.AbstractAsyncStream AbstractAsyncStream}).
     */
    Awaitable<Boolean> MOVE_NEXT_TRUE = Awaitable.of(Boolean.TRUE);

    /**
     * Cached awaitable for {@code moveNext()} returning {@code false}.
     * Eliminates per-call allocation on the stream-end path.
     */
    Awaitable<Boolean> MOVE_NEXT_FALSE = Awaitable.of(Boolean.FALSE);

    /**
     * Singleton empty stream instance.
     * <p>
     * This is an implementation detail backing {@link #empty()}.
     * User code should call {@code AsyncStream.empty()} rather than
     * referencing this field directly.
     */
    AsyncStream<Object> EMPTY = new AsyncStream<>() {
        @Override public Awaitable<Boolean> moveNext() { return MOVE_NEXT_FALSE; }
        @Override public Object getCurrent() { return null; }
    };
}
