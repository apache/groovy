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

import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Flow;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Central registry for {@link AwaitableAdapter} instances.
 * <p>
 * On class-load, adapters are discovered via {@link ServiceLoader} from
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter}. A built-in
 * adapter is always present as the lowest-priority fallback, handling:
 * <ul>
 *   <li>{@link CompletableFuture} and {@link CompletionStage}</li>
 *   <li>{@link Future} (adapted via a blocking wrapper)</li>
 *   <li>JDK {@link Flow.Publisher} — single-value
 *       ({@link #toAwaitable}) and multi-value ({@link #toAsyncStream})
 *       with backpressure support and upstream cancellation when the
 *       resulting {@link AsyncStream} is {@linkplain AsyncStream#close() closed}</li>
 * </ul>
 * <p>
 * Additional adapters can be registered at runtime via {@link #register}.
 *
 * @see AwaitableAdapter
 * @since 6.0.0
 */
public class AwaitableAdapterRegistry {

    private static final List<AwaitableAdapter> ADAPTERS = new CopyOnWriteArrayList<>();

    /**
     * Optional executor supplier for blocking Future adaptation, to avoid
     * starving the common pool. Defaults to null; when set, the provided
     * executor is used instead of {@code CompletableFuture.runAsync}'s default.
     */
    private static volatile Executor blockingExecutor;

    static {
        // SPI-discovered adapters
        for (AwaitableAdapter adapter : ServiceLoader.load(AwaitableAdapter.class)) {
            ADAPTERS.add(adapter);
        }
        // Built-in fallback (lowest priority)
        ADAPTERS.add(new BuiltInAdapter());
    }

    private AwaitableAdapterRegistry() { }

    /**
     * Registers an adapter with higher priority than existing ones.
     *
     * @return an {@link AutoCloseable} that, when closed, removes this adapter
     *         from the registry. Useful for test isolation.
     */
    public static AutoCloseable register(AwaitableAdapter adapter) {
        ADAPTERS.add(0, adapter);
        return () -> ADAPTERS.remove(adapter);
    }

    /**
     * Removes the given adapter from the registry.
     *
     * @return {@code true} if the adapter was found and removed
     */
    public static boolean unregister(AwaitableAdapter adapter) {
        return ADAPTERS.remove(adapter);
    }

    /**
     * Sets the executor used for blocking {@link Future#get()} adaptation.
     * When non-null, this executor is used instead of
     * {@link CompletableFuture#runAsync(Runnable)}'s default executor to avoid
     * pool starvation when many blocking futures are being adapted
     * simultaneously.
     *
     * @param executor the executor to use, or {@code null} to use the default
     */
    public static void setBlockingExecutor(Executor executor) {
        blockingExecutor = executor;
    }

    /**
     * Converts the given source to an {@link Awaitable}.
     * If the source is already an {@code Awaitable}, it is returned as-is.
     *
     * @param source the source object; must not be {@code null}
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> toAwaitable(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to Awaitable");
        }
        if (source instanceof Awaitable) return (Awaitable<T>) source;
        Class<?> type = source.getClass();
        for (AwaitableAdapter adapter : ADAPTERS) {
            if (adapter.supportsAwaitable(type)) {
                return adapter.toAwaitable(source);
            }
        }
        throw new IllegalArgumentException(
                "No AwaitableAdapter found for type: " + type.getName()
                        + ". Register one via AwaitableAdapterRegistry.register() or ServiceLoader.");
    }

    /**
     * Converts the given source to an {@link AsyncStream}.
     * If the source is already an {@code AsyncStream}, it is returned as-is.
     *
     * @param source the source object; must not be {@code null}
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     */
    @SuppressWarnings("unchecked")
    public static <T> AsyncStream<T> toAsyncStream(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to AsyncStream");
        }
        if (source instanceof AsyncStream) return (AsyncStream<T>) source;
        Class<?> type = source.getClass();
        for (AwaitableAdapter adapter : ADAPTERS) {
            if (adapter.supportsAsyncStream(type)) {
                return adapter.toAsyncStream(source);
            }
        }
        throw new IllegalArgumentException(
                "No AsyncStream adapter found for type: " + type.getName()
                        + ". Register one via AwaitableAdapterRegistry.register() or ServiceLoader.");
    }

    /**
     * Built-in adapter handling JDK {@link CompletableFuture}, {@link CompletionStage},
     * {@link Future}, {@link Flow.Publisher},
     * and {@link Iterable}/{@link Iterator} (for async stream bridging).
     * <p>
     * {@link CompletionStage} support enables seamless integration with frameworks
     * that return {@code CompletionStage} (e.g., Spring's async APIs, Reactor's
     * {@code Mono.toFuture()}, etc.) without any additional adapter registration.
     * <p>
     * {@link Flow.Publisher} support enables seamless
     * consumption of reactive streams via {@code for await} without any adapter
     * registration.  This covers any reactive library that implements the JDK
     * standard reactive-streams interface (Reactor, RxJava via adapters, etc.).
     */
    private static class BuiltInAdapter implements AwaitableAdapter {

        @Override
        public boolean supportsAwaitable(Class<?> type) {
            return CompletionStage.class.isAssignableFrom(type)
                    || Future.class.isAssignableFrom(type)
                    || Flow.Publisher.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Awaitable<T> toAwaitable(Object source) {
            if (source instanceof CompletionStage) {
                return new GroovyPromise<>(((CompletionStage<T>) source).toCompletableFuture());
            }
            if (source instanceof Flow.Publisher<?> pub) {
                return publisherToAwaitable(pub);
            }
            if (source instanceof Future) {
                Future<T> future = (Future<T>) source;
                CompletableFuture<T> cf = new CompletableFuture<>();
                if (future.isDone()) {
                    completeFrom(cf, future);
                } else {
                    Executor exec = blockingExecutor;
                    if (exec != null) {
                        CompletableFuture.runAsync(() -> completeFrom(cf, future), exec);
                    } else {
                        CompletableFuture.runAsync(() -> completeFrom(cf, future));
                    }
                }
                return new GroovyPromise<>(cf);
            }
            throw new IllegalArgumentException("Cannot convert to Awaitable: " + source.getClass());
        }

        @Override
        public boolean supportsAsyncStream(Class<?> type) {
            return Iterable.class.isAssignableFrom(type)
                    || Iterator.class.isAssignableFrom(type)
                    || Flow.Publisher.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> AsyncStream<T> toAsyncStream(Object source) {
            if (source instanceof Flow.Publisher<?> pub) {
                return publisherToAsyncStream((Flow.Publisher<T>) pub);
            }
            final Iterator<T> iterator;
            if (source instanceof Iterable) {
                iterator = ((Iterable<T>) source).iterator();
            } else if (source instanceof Iterator) {
                iterator = (Iterator<T>) source;
            } else {
                throw new IllegalArgumentException("Cannot convert to AsyncStream: " + source.getClass());
            }
            return new AsyncStream<T>() {
                private T current;

                @Override
                public Awaitable<Boolean> moveNext() {
                    boolean hasNext = iterator.hasNext();
                    if (hasNext) current = iterator.next();
                    return Awaitable.of(hasNext);
                }

                @Override
                public T getCurrent() {
                    return current;
                }
            };
        }

        // Signal wrappers for the publisher-to-async-stream queue, ensuring
        // that values, errors, and completion are never confused — even when
        // the element type T extends Throwable.
        private static final Object COMPLETE_SENTINEL = new Object();
        private record ValueSignal<T>(T value) { }
        private record ErrorSignal(Throwable error) { }

        /**
         * Adapts a {@link Flow.Publisher} to an {@link AsyncStream} using a
         * bounded blocking queue to bridge the push-based reactive-streams
         * protocol to the pull-based {@code moveNext}/{@code getCurrent}
         * pattern.  Backpressure is managed by requesting one item at a time:
         * each {@code moveNext()} call requests the next item from the upstream
         * subscription only after the previous item has been consumed.
         * <p>
         * All queue entries are wrapped in typed signal objects
         * ({@code ValueSignal}, {@code ErrorSignal}, or a completion sentinel)
         * so that element types extending {@link Throwable} are never
         * misidentified as error signals.
         * <p>
         * Thread interruption during {@code queue.take()} is converted to a
         * {@link CancellationException} that is <em>thrown directly</em> rather
         * than stored in a {@code CompletableFuture}.  On JDK 23+,
         * {@code CompletableFuture.get()} wraps stored
         * {@code CancellationException}s in a new instance with message
         * {@code "get"}, discarding the original message and cause chain.
         * Throwing directly bypasses {@code CF.get()} entirely, ensuring
         * deterministic behaviour across all JDK versions (17 – 25+).
         * The interrupt flag is restored per Java convention, and the original
         * {@code InterruptedException} is preserved as the
         * {@linkplain Throwable#getCause() cause}.  This matches the pattern
         * used by {@link org.apache.groovy.runtime.async.AsyncStreamGenerator#moveNext()}.
         */
        @SuppressWarnings("unchecked")
        private static <T> AsyncStream<T> publisherToAsyncStream(Flow.Publisher<T> publisher) {
            BlockingQueue<Object> queue = new LinkedBlockingQueue<>(256);
            AtomicReference<Flow.Subscription> subRef = new AtomicReference<>();
            AtomicBoolean closedRef = new AtomicBoolean(false);

            publisher.subscribe(new Flow.Subscriber<T>() {
                @Override
                public void onSubscribe(Flow.Subscription s) {
                    if (!closedRef.get()) {
                        subRef.set(s);
                        s.request(1);
                    } else {
                        s.cancel();
                    }
                }

                @Override
                public void onNext(T item) {
                    if (!closedRef.get()) {
                        queue.offer(new ValueSignal<>(item));
                    }
                }

                @Override
                public void onError(Throwable t) {
                    if (!closedRef.get()) {
                        queue.offer(new ErrorSignal(t));
                    }
                }

                @Override
                public void onComplete() {
                    if (!closedRef.get()) {
                        queue.offer(COMPLETE_SENTINEL);
                    }
                }
            });

            return new AsyncStream<T>() {
                private volatile T current;
                private final AtomicBoolean streamClosed = new AtomicBoolean(false);

                @Override
                public Awaitable<Boolean> moveNext() {
                    if (streamClosed.get()) {
                        return Awaitable.of(false);
                    }
                    CompletableFuture<Boolean> cf = new CompletableFuture<>();
                    try {
                        Object signal = queue.take();
                        if (signal == COMPLETE_SENTINEL) {
                            streamClosed.set(true);
                            cf.complete(false);
                        } else if (signal instanceof ErrorSignal es) {
                            streamClosed.set(true);
                            cf.completeExceptionally(es.error());
                        } else if (signal instanceof ValueSignal<?> vs) {
                            current = (T) vs.value();
                            cf.complete(true);
                            Flow.Subscription sub = subRef.get();
                            if (sub != null) sub.request(1);
                        }
                    } catch (InterruptedException e) {
                        if (streamClosed.get()) {
                            return Awaitable.of(false);
                        }
                        // Throw directly instead of storing in the CompletableFuture.
                        // On JDK 23+, CF.get() wraps stored CancellationExceptions in a
                        // new CancellationException("get"), discarding our message and
                        // cause chain.  Throwing directly avoids CF.get() entirely and
                        // ensures deterministic behaviour across all JDK versions.
                        // This matches the pattern used by AsyncStreamGenerator.moveNext().
                        Thread.currentThread().interrupt();
                        CancellationException ce = new CancellationException("Interrupted while waiting for next item");
                        ce.initCause(e);
                        throw ce;
                    }
                    return new GroovyPromise<>(cf);
                }

                @Override
                public T getCurrent() {
                    return current;
                }

                @Override
                public void close() {
                    if (!streamClosed.compareAndSet(false, true)) {
                        return;
                    }
                    closedRef.set(true);
                    Flow.Subscription subscription = subRef.getAndSet(null);
                    if (subscription != null) {
                        subscription.cancel();
                    }
                    queue.clear();
                    queue.offer(COMPLETE_SENTINEL);
                }
            };
        }

        /**
         * Adapts a single-value {@link Flow.Publisher} to
         * an {@link Awaitable}.  Subscribes and takes the first emitted value.
         */
        @SuppressWarnings("unchecked")
        private static <T> Awaitable<T> publisherToAwaitable(Flow.Publisher<?> publisher) {
            CompletableFuture<T> cf = new CompletableFuture<>();
            publisher.subscribe(new Flow.Subscriber<Object>() {
                private Flow.Subscription subscription;

                @Override
                public void onSubscribe(Flow.Subscription s) {
                    this.subscription = s;
                    s.request(1);
                }

                @Override
                @SuppressWarnings("unchecked")
                public void onNext(Object item) {
                    cf.complete((T) item);
                    subscription.cancel();
                }

                @Override
                public void onError(Throwable t) {
                    cf.completeExceptionally(t);
                }

                @Override
                public void onComplete() {
                    if (!cf.isDone()) cf.complete(null);
                }
            });
            return new GroovyPromise<>(cf);
        }

        private static <T> void completeFrom(CompletableFuture<T> cf, Future<T> future) {
            try {
                cf.complete(future.get());
            } catch (Exception e) {
                cf.completeExceptionally(e.getCause() != null ? e.getCause() : e);
            }
        }
    }
}
