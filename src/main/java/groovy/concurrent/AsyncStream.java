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
 * Third-party reactive types (Reactor {@code Flux}, RxJava {@code Observable})
 * can be adapted to {@code AsyncStream} via {@link AwaitableAdapter}.
 *
 * @param <T> the element type
 * @see AwaitableAdapter
 * @since 6.0.0
 */
public interface AsyncStream<T> {

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
