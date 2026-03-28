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
package org.apache.groovy.rxjava;

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.Iterator;

/**
 * {@link AwaitableAdapter} implementation for
 * <a href="https://github.com/ReactiveX/RxJava">RxJava 3</a>, bridging
 * RxJava's reactive types into Groovy's native {@code async}/{@code await} system.
 *
 * <h2>Supported types</h2>
 * <table>
 *   <caption>RxJava type mapping</caption>
 *   <tr><th>RxJava Type</th><th>Groovy Abstraction</th><th>Usage</th></tr>
 *   <tr><td>{@link Single}</td><td>{@link Awaitable}</td>
 *       <td>{@code await single} — resolves to the single emitted value</td></tr>
 *   <tr><td>{@link Maybe}</td><td>{@link Awaitable}</td>
 *       <td>{@code await maybe} — resolves to the value or {@code null} if empty</td></tr>
 *   <tr><td>{@link Observable}</td><td>{@link AsyncStream}</td>
 *       <td>{@code for await (item in observable)} — iterates over all emitted values</td></tr>
 *   <tr><td>{@link Flowable}</td><td>{@link AsyncStream}</td>
 *       <td>{@code for await (item in flowable)} — iterates with back-pressure</td></tr>
 * </table>
 *
 * <h2>Adaptation strategy</h2>
 * <ul>
 *   <li><b>Single → Awaitable:</b> Delegates to {@link Single#toCompletionStage()},
 *       which subscribes and returns a {@link java.util.concurrent.CompletionStage}
 *       that completes with the emitted value. The resulting future is wrapped in a
 *       {@link GroovyPromise}.</li>
 *   <li><b>Maybe → Awaitable:</b> Delegates to {@link Maybe#toCompletionStage(Object)}
 *       with a {@code null} default value. An empty {@code Maybe} resolves to
 *       {@code null}, consistent with Reactor's empty {@code Mono} behavior.</li>
 *   <li><b>Observable → AsyncStream:</b> Delegates to
 *       {@link Observable#blockingIterable()}, which subscribes and produces a
 *       blocking {@link Iterable}. The iterator is wrapped in an {@link AsyncStream}
 *       that advances one element per {@code moveNext()} call.</li>
 *   <li><b>Flowable → AsyncStream:</b> Delegates to
 *       {@link Flowable#blockingIterable()}, which provides the same blocking
 *       iteration with built-in Reactive Streams back-pressure support.</li>
 * </ul>
 *
 * <h2>Registration</h2>
 * <p>This adapter is auto-discovered by the
 * {@link groovy.concurrent.AwaitableAdapterRegistry} via the
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter} file
 * shipped with the {@code groovy-rxjava} module. Adding {@code groovy-rxjava}
 * to the classpath is sufficient — no manual registration is required.</p>
 *
 * <h2>Thread safety</h2>
 * <p>This adapter is stateless and thread-safe. All mutable state resides in the
 * RxJava sources and the blocking iterators they produce, both of which are
 * designed for safe concurrent use.</p>
 *
 * @see groovy.concurrent.AwaitableAdapterRegistry
 * @see Single
 * @see Maybe
 * @see Observable
 * @see Flowable
 * @since 6.0.0
 */
public class RxJavaAwaitableAdapter implements AwaitableAdapter {

    /**
     * Returns {@code true} if the given type is assignable to {@link Single}
     * or {@link Maybe}, enabling single-value {@code await}.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can convert the type to {@link Awaitable}
     */
    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Single.class.isAssignableFrom(type)
                || Maybe.class.isAssignableFrom(type);
    }

    /**
     * Converts a {@link Single} or {@link Maybe} to an {@link Awaitable}.
     * <p>
     * For {@code Single}, subscribes and collects the emitted value into a
     * {@link java.util.concurrent.CompletableFuture}. For {@code Maybe}, uses
     * {@code null} as the default value when empty, consistent with Reactor's
     * empty Mono behavior.
     * <p>
     * If the source signals an error, the future completes exceptionally,
     * propagating the original exception through Groovy's await unwrapping.
     *
     * @param source the RxJava source; must be a {@link Single} or {@link Maybe}
     * @param <T>    the value type
     * @return an awaitable that resolves to the emitted value
     * @throws IllegalArgumentException if the source is not a supported RxJava type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        if (source instanceof Single) {
            return new GroovyPromise<>(((Single<T>) source).toCompletionStage().toCompletableFuture());
        }
        if (source instanceof Maybe) {
            return new GroovyPromise<>(((Maybe<T>) source).toCompletionStage(null).toCompletableFuture());
        }
        throw new IllegalArgumentException("Unsupported RxJava type: " + source.getClass().getName());
    }

    /**
     * Returns {@code true} if the given type is assignable to {@link Observable}
     * or {@link Flowable}, enabling multi-value {@code for await} iteration.
     *
     * @param type the source type to check
     * @return {@code true} if this adapter can convert the type to {@link AsyncStream}
     */
    @Override
    public boolean supportsAsyncStream(Class<?> type) {
        return Observable.class.isAssignableFrom(type)
                || Flowable.class.isAssignableFrom(type);
    }

    /**
     * Converts an {@link Observable} or {@link Flowable} to an {@link AsyncStream}
     * for use with {@code for await} loops.
     * <p>
     * Both types are converted to a blocking {@link Iterable} via their respective
     * {@code blockingIterable(1)} methods with a prefetch of {@code 1} to ensure
     * demand-driven consumption. For {@link Flowable}, this preserves Reactive
     * Streams back-pressure semantics with minimal buffering. For {@link Observable},
     * the small buffer ensures errors are not eagerly consumed ahead of values.
     * <p>
     * If the source signals an error, the iterator's {@code hasNext()} or
     * {@code next()} call will throw the original exception.
     *
     * @param source the RxJava source; must be an {@link Observable} or {@link Flowable}
     * @param <T>    the element type
     * @return an async stream yielding elements from the source
     * @throws IllegalArgumentException if the source is not a supported RxJava type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> AsyncStream<T> toAsyncStream(Object source) {
        final Iterator<T> iterator;
        if (source instanceof Observable) {
            iterator = ((Observable<T>) source).blockingIterable(1).iterator();
        } else if (source instanceof Flowable) {
            iterator = ((Flowable<T>) source).blockingIterable(1).iterator();
        } else {
            throw new IllegalArgumentException("Unsupported RxJava stream type: " + source.getClass().getName());
        }
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
