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
package org.apache.groovy.reactor;

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;
import org.apache.groovy.runtime.async.GroovyPromise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;

/**
 * {@link AwaitableAdapter} implementation for
 * <a href="https://projectreactor.io/">Project Reactor</a>, bridging
 * Reactor's {@link Mono} and {@link Flux} types into Groovy's native
 * {@code async}/{@code await} system.
 *
 * <h2>Supported types</h2>
 * <table>
 *   <caption>Reactor type mapping</caption>
 *   <tr><th>Reactor Type</th><th>Groovy Abstraction</th><th>Usage</th></tr>
 *   <tr><td>{@link Mono}</td><td>{@link Awaitable}</td>
 *       <td>{@code await mono} — resolves to the single emitted value</td></tr>
 *   <tr><td>{@link Flux}</td><td>{@link AsyncStream}</td>
 *       <td>{@code for await (item in flux)} — iterates over all emitted values</td></tr>
 * </table>
 *
 * <h2>Adaptation strategy</h2>
 * <ul>
 *   <li><b>Mono → Awaitable:</b> Delegates to {@link Mono#toFuture()}, which subscribes
 *       to the {@code Mono} and returns a {@link java.util.concurrent.CompletableFuture}
 *       that completes with the emitted value (or {@code null} for empty Monos).
 *       The resulting future is wrapped in a {@link GroovyPromise} for seamless
 *       integration with Groovy's await mechanism.</li>
 *   <li><b>Flux → AsyncStream:</b> Delegates to {@link Flux#toIterable()}, which
 *       subscribes with back-pressure and produces a blocking {@link Iterable}.
 *       The iterator is wrapped in a simple {@link AsyncStream} that advances
 *       one element per {@code moveNext()} call.  This provides natural
 *       back-pressure: the consumer controls the pace of iteration.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 * <p>This adapter is auto-discovered by the
 * {@link groovy.concurrent.AwaitableAdapterRegistry} via the
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter} file
 * shipped with the {@code groovy-reactor} module. Adding {@code groovy-reactor}
 * to the classpath is sufficient — no manual registration is required.</p>
 *
 * <h2>Thread safety</h2>
 * <p>This adapter is stateless and thread-safe. All mutable state resides in the
 * Reactor publishers and the iterator produced by {@code Flux.toIterable()},
 * both of which are designed for concurrent use.</p>
 *
 * @see groovy.concurrent.AwaitableAdapterRegistry
 * @see Mono
 * @see Flux
 * @since 6.0.0
 */
public class ReactorAwaitableAdapter implements AwaitableAdapter {

    /**
     * Returns {@code true} if the given type is assignable to {@link Mono},
     * enabling single-value {@code await}.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can convert the type to {@link Awaitable}
     */
    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Mono.class.isAssignableFrom(type);
    }

    /**
     * Converts a {@link Mono} to an {@link Awaitable} by subscribing and
     * collecting its result into a {@link java.util.concurrent.CompletableFuture}.
     * <p>
     * An empty {@code Mono} resolves to {@code null}. A {@code Mono} that
     * signals an error completes the future exceptionally, propagating the
     * original exception through Groovy's await unwrapping mechanism.
     *
     * @param source the Mono instance; must be assignable to {@link Mono}
     * @param <T>    the value type
     * @return an awaitable that resolves to the Mono's emitted value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        return new GroovyPromise<>(((Mono<T>) source).toFuture());
    }

    /**
     * Returns {@code true} if the given type is assignable to {@link Flux},
     * enabling multi-value {@code for await} iteration.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can convert the type to {@link AsyncStream}
     */
    @Override
    public boolean supportsAsyncStream(Class<?> type) {
        return Flux.class.isAssignableFrom(type);
    }

    /**
     * Converts a {@link Flux} to an {@link AsyncStream} for use with
     * {@code for await} loops.
     * <p>
     * Back-pressure is handled by {@link Flux#toIterable(int)} with a prefetch
     * of {@code 1}, which requests elements one at a time. This ensures that
     * errors in the upstream Flux are not eagerly consumed, allowing partial
     * iteration results to be observed before the error propagates. The
     * consumer controls iteration pace: {@code moveNext()} blocks until the
     * next element arrives or the Flux completes.
     * <p>
     * If the Flux signals an error, the iterator's {@code hasNext()} or
     * {@code next()} call will throw the original exception, which is then
     * propagated through the {@code for await} loop's exception handling.
     *
     * @param source the Flux instance; must be assignable to {@link Flux}
     * @param <T>    the element type
     * @return an async stream yielding elements from the Flux
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> AsyncStream<T> toAsyncStream(Object source) {
        final Iterator<T> iterator = ((Flux<T>) source).toIterable(1).iterator();
        return new AsyncStream<T>() {
            private T current;

            @Override
            public Awaitable<Boolean> moveNext() {
                boolean hasNext = iterator.hasNext();
                if (hasNext) {
                    current = iterator.next();
                }
                return Awaitable.of(hasNext);
            }

            @Override
            public T getCurrent() {
                return current;
            }
        };
    }
}
