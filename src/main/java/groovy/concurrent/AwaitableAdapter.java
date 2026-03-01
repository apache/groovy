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
 * to Groovy's {@link Awaitable} and {@link AsyncStream} abstractions.
 * <p>
 * Implementations are discovered automatically via {@link java.util.ServiceLoader}.
 * To register an adapter, create a file
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter} containing the
 * fully-qualified class name of your implementation.
 * <p>
 * <b>Example – RxJava adapter:</b>
 * <pre>
 * public class RxJavaAwaitableAdapter implements AwaitableAdapter {
 *     public boolean supportsAwaitable(Class&lt;?&gt; type) {
 *         return io.reactivex.rxjava3.core.Single.class.isAssignableFrom(type);
 *     }
 *     public &lt;T&gt; Awaitable&lt;T&gt; toAwaitable(Object source) {
 *         Single&lt;T&gt; single = (Single&lt;T&gt;) source;
 *         return new org.apache.groovy.runtime.async.GroovyPromise&lt;&gt;(single.toCompletionStage().toCompletableFuture());
 *     }
 *     // ... supportsAsyncStream / toAsyncStream for Observable, Flowable, etc.
 * }
 * </pre>
 *
 * @see AwaitableAdapterRegistry
 * @since 6.0.0
 */
public interface AwaitableAdapter {

    /**
     * Returns {@code true} if this adapter can convert instances of the given
     * type to {@link Awaitable}.
     */
    boolean supportsAwaitable(Class<?> type);

    /**
     * Converts the given source object to an {@link Awaitable}.
     * Called only when {@link #supportsAwaitable} returned {@code true}.
     */
    <T> Awaitable<T> toAwaitable(Object source);

    /**
     * Returns {@code true} if this adapter can convert instances of the given
     * type to {@link AsyncStream}.
     */
    default boolean supportsAsyncStream(Class<?> type) {
        return false;
    }

    /**
     * Converts the given source object to an {@link AsyncStream}.
     * Called only when {@link #supportsAsyncStream} returned {@code true}.
     */
    default <T> AsyncStream<T> toAsyncStream(Object source) {
        throw new UnsupportedOperationException("AsyncStream conversion not supported by " + getClass().getName());
    }
}
