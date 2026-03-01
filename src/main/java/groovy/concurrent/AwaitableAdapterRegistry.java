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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Central registry for {@link AwaitableAdapter} instances.
 * <p>
 * On class-load, adapters are discovered via {@link ServiceLoader} from
 * {@code META-INF/services/groovy.concurrent.AwaitableAdapter}. A built-in
 * adapter is always present as the lowest-priority fallback, handling:
 * <ul>
 *   <li>{@link CompletableFuture} and {@link CompletionStage}</li>
 *   <li>{@link Future} (adapted via a blocking wrapper)</li>
 *   <li>JDK {@link java.util.concurrent.Flow.Publisher} — single-value
 *       ({@link #toAwaitable}) and multi-value ({@link #toAsyncStream})
 *       with backpressure support</li>
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
     * @throws IllegalArgumentException if no adapter supports the source type
     */
    @SuppressWarnings("unchecked")
    public static <T> Awaitable<T> toAwaitable(Object source) {
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
     * @throws IllegalArgumentException if no adapter supports the source type
     */
    @SuppressWarnings("unchecked")
    public static <T> AsyncStream<T> toAsyncStream(Object source) {
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
     * {@link Future}, {@link java.util.concurrent.Flow.Publisher},
     * and {@link Iterable}/{@link Iterator} (for async stream bridging).
     * <p>
     * {@link CompletionStage} support enables seamless integration with frameworks
     * that return {@code CompletionStage} (e.g., Spring's async APIs, Reactor's
     * {@code Mono.toFuture()}, etc.) without any additional adapter registration.
     * <p>
     * {@link java.util.concurrent.Flow.Publisher} support enables seamless
     * consumption of reactive streams via {@code for await} without any adapter
     * registration.  This covers any reactive library that implements the JDK
     * standard reactive-streams interface (Reactor, RxJava via adapters, etc.).
     */
    private static class BuiltInAdapter implements AwaitableAdapter {

        @Override
        public boolean supportsAwaitable(Class<?> type) {
            return CompletionStage.class.isAssignableFrom(type)
                    || Future.class.isAssignableFrom(type)
                    || java.util.concurrent.Flow.Publisher.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Awaitable<T> toAwaitable(Object source) {
            if (source instanceof CompletionStage) {
                return new GroovyPromise<>(((CompletionStage<T>) source).toCompletableFuture());
            }
            if (source instanceof java.util.concurrent.Flow.Publisher<?> pub) {
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
                    || java.util.concurrent.Flow.Publisher.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> AsyncStream<T> toAsyncStream(Object source) {
            if (source instanceof java.util.concurrent.Flow.Publisher<?> pub) {
                return publisherToAsyncStream((java.util.concurrent.Flow.Publisher<T>) pub);
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

        /**
         * Adapts a {@link java.util.concurrent.Flow.Publisher} to an
         * {@link AsyncStream} using a blocking queue to bridge the push-based
         * reactive-streams protocol to the pull-based {@code moveNext}/{@code getCurrent}
         * pattern.  Backpressure is managed by requesting one item at a time:
         * each {@code moveNext()} call requests the next item from the upstream
         * subscription only after the previous item has been consumed.
         */
        @SuppressWarnings("unchecked")
        private static <T> AsyncStream<T> publisherToAsyncStream(java.util.concurrent.Flow.Publisher<T> publisher) {
            java.util.concurrent.LinkedBlockingQueue<Object> queue = new java.util.concurrent.LinkedBlockingQueue<>();
            Object COMPLETE_SENTINEL = new Object();
            java.util.concurrent.atomic.AtomicReference<java.util.concurrent.Flow.Subscription> subRef =
                    new java.util.concurrent.atomic.AtomicReference<>();

            publisher.subscribe(new java.util.concurrent.Flow.Subscriber<T>() {
                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription s) {
                    subRef.set(s);
                    s.request(1);
                }

                @Override
                public void onNext(T item) {
                    queue.add(item);
                }

                @Override
                public void onError(Throwable t) {
                    queue.add(t instanceof Exception ? t : new RuntimeException(t));
                }

                @Override
                public void onComplete() {
                    queue.add(COMPLETE_SENTINEL);
                }
            });

            return new AsyncStream<T>() {
                private T current;

                @Override
                public Awaitable<Boolean> moveNext() {
                    CompletableFuture<Boolean> cf = new CompletableFuture<>();
                    try {
                        Object item = queue.take();
                        if (item == COMPLETE_SENTINEL) {
                            cf.complete(false);
                        } else if (item instanceof Throwable) {
                            cf.completeExceptionally((Throwable) item);
                        } else {
                            current = (T) item;
                            cf.complete(true);
                            // Request next item for the subsequent moveNext() call
                            java.util.concurrent.Flow.Subscription sub = subRef.get();
                            if (sub != null) sub.request(1);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        cf.completeExceptionally(e);
                    }
                    return new GroovyPromise<>(cf);
                }

                @Override
                public T getCurrent() {
                    return current;
                }
            };
        }

        /**
         * Adapts a single-value {@link java.util.concurrent.Flow.Publisher} to
         * an {@link Awaitable}.  Subscribes and takes the first emitted value.
         */
        @SuppressWarnings("unchecked")
        private static <T> Awaitable<T> publisherToAwaitable(java.util.concurrent.Flow.Publisher<?> publisher) {
            CompletableFuture<T> cf = new CompletableFuture<>();
            publisher.subscribe(new java.util.concurrent.Flow.Subscriber<Object>() {
                private java.util.concurrent.Flow.Subscription subscription;

                @Override
                public void onSubscribe(java.util.concurrent.Flow.Subscription s) {
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
