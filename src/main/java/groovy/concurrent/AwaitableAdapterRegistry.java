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
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
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
 * </ul>
 * <p>
 * JDK {@link java.util.concurrent.Flow.Publisher} support is provided by
 * the separately registered
 * {@link org.apache.groovy.runtime.async.FlowPublisherAdapter}, which is
 * auto-discovered via ServiceLoader.  Third-party frameworks (Reactor,
 * RxJava, etc.) may register their own higher-priority adapters.
 * <p>
 * Additional adapters can be registered at runtime via {@link #register}.
 *
 * @see AwaitableAdapter
 * @since 6.0.0
 */
public class AwaitableAdapterRegistry {

    private static final List<AwaitableAdapter> ADAPTERS = new CopyOnWriteArrayList<>();

    /**
     * Per-class adapter cache for {@link #toAwaitable(Object)}.
     * Uses {@link ClassValue} for lock-free, GC-friendly per-class memoization
     * that does not prevent class unloading — superior to
     * {@code ConcurrentHashMap<Class<?>, V>} which holds strong references
     * that can cause ClassLoader leaks in container environments.
     * <p>
     * Rebuilt via volatile reference swap when adapters are registered or
     * removed (an extremely low-frequency operation).
     */
    private static volatile ClassValue<AwaitableAdapter> awaitableCache = buildAwaitableCache();

    /**
     * Per-class adapter cache for {@link #toAsyncStream(Object)}.
     * Same caching strategy as {@link #awaitableCache}.
     */
    private static volatile ClassValue<AwaitableAdapter> streamCache = buildStreamCache();

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
     * <p>
     * Invalidates the per-class adapter caches so that subsequent lookups
     * re-evaluate adapter priority order.
     *
     * @param adapter the adapter to register; must not be {@code null}
     * @return an {@link AutoCloseable} that, when closed, removes this adapter
     *         from the registry. Useful for test isolation.
     */
    public static AutoCloseable register(AwaitableAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter must not be null");
        ADAPTERS.add(0, adapter);
        invalidateCaches();
        return () -> { ADAPTERS.remove(adapter); invalidateCaches(); };
    }

    /**
     * Removes the given adapter from the registry.
     * <p>
     * Invalidates the per-class adapter caches so that subsequent lookups
     * no longer consider the removed adapter.
     *
     * @param adapter the adapter to remove
     * @return {@code true} if the adapter was found and removed
     */
    public static boolean unregister(AwaitableAdapter adapter) {
        Objects.requireNonNull(adapter, "adapter must not be null");
        boolean removed = ADAPTERS.remove(adapter);
        if (removed) {
            invalidateCaches();
        }
        return removed;
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
     * <p>
     * Uses a per-class {@link ClassValue} cache to avoid repeated linear
     * scans of the adapter list on the hot path.  The first lookup for a
     * given class performs a linear scan; subsequent lookups for the same
     * class are O(1).
     * <p>
     * <b>Tip:</b> user code should generally prefer {@link Awaitable#from(Object)},
     * which delegates to this method but is more discoverable from the
     * {@code Awaitable} type itself.
     *
     * @param source the source object; must not be {@code null}
     * @param <T>    the result type
     * @return an awaitable backed by the source
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     * @see Awaitable#from(Object)
     */
    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> toAwaitable(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to Awaitable");
        }
        if (source instanceof Awaitable) return (Awaitable<T>) source;
        Class<?> type = source.getClass();
        AwaitableAdapter adapter = awaitableCache.get(type);
        if (adapter != null) {
            return adapter.toAwaitable(source);
        }
        throw new IllegalArgumentException(
                "No AwaitableAdapter found for type: " + type.getName()
                        + ". Register one via AwaitableAdapterRegistry.register() or ServiceLoader.");
    }

    /**
     * Converts the given source to an {@link AsyncStream}.
     * If the source is already an {@code AsyncStream}, it is returned as-is.
     * {@link AsyncChannel} instances are handled as a built-in special case via
     * {@link AsyncChannel#asStream()}, enabling transparent {@code for await}
     * iteration over channels.
     * <p>
     * Uses a per-class {@link ClassValue} cache to avoid repeated linear
     * scans of the adapter list on the hot path.
     * <p>
     * <b>Tip:</b> user code should generally prefer {@link AsyncStream#from(Object)},
     * which delegates to this method but is more discoverable from the
     * {@code AsyncStream} type itself.
     *
     * @param source the source object; must not be {@code null}
     * @param <T>    the element type
     * @return an async stream backed by the source
     * @throws IllegalArgumentException if {@code source} is {@code null}
     *         or no adapter supports the source type
     * @see AsyncStream#from(Object)
     */
    @SuppressWarnings("unchecked")
    static <T> AsyncStream<T> toAsyncStream(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to AsyncStream");
        }
        if (source instanceof AsyncStream) return (AsyncStream<T>) source;
        if (source instanceof AsyncChannel) return ((AsyncChannel<T>) source).asStream();
        Class<?> type = source.getClass();
        AwaitableAdapter adapter = streamCache.get(type);
        if (adapter != null) {
            return adapter.toAsyncStream(source);
        }
        throw new IllegalArgumentException(
                "No AsyncStream adapter found for type: " + type.getName()
                        + ". Register one via AwaitableAdapterRegistry.register() or ServiceLoader.");
    }

    // ---- Cache management ------------------------------------------------

    private static ClassValue<AwaitableAdapter> buildAwaitableCache() {
        return new ClassValue<>() {
            @Override
            protected AwaitableAdapter computeValue(Class<?> type) {
                for (AwaitableAdapter adapter : ADAPTERS) {
                    if (adapter.supportsAwaitable(type)) {
                        return adapter;
                    }
                }
                return null;
            }
        };
    }

    private static ClassValue<AwaitableAdapter> buildStreamCache() {
        return new ClassValue<>() {
            @Override
            protected AwaitableAdapter computeValue(Class<?> type) {
                for (AwaitableAdapter adapter : ADAPTERS) {
                    if (adapter.supportsAsyncStream(type)) {
                        return adapter;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Rebuilds the per-class adapter caches after adapter registration
     * changes.  Uses volatile reference swap — safe because
     * {@link ClassValue} instances are immutable once constructed, and
     * concurrent readers of the old cache see a consistent (if stale)
     * snapshot until the new cache is published.
     */
    private static void invalidateCaches() {
        awaitableCache = buildAwaitableCache();
        streamCache = buildStreamCache();
    }

    /**
     * Built-in adapter handling JDK {@link CompletableFuture}, {@link CompletionStage},
     * {@link Future}, and {@link Iterable}/{@link Iterator} (for async stream bridging).
     * <p>
     * {@link CompletionStage} support enables seamless integration with frameworks
     * that return {@code CompletionStage} (e.g., Spring's async APIs, Reactor's
     * {@code Mono.toFuture()}, etc.) without any additional adapter registration.
     */
    private static class BuiltInAdapter implements AwaitableAdapter {

        @Override
        public boolean supportsAwaitable(Class<?> type) {
            return CompletionStage.class.isAssignableFrom(type)
                    || Future.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Awaitable<T> toAwaitable(Object source) {
            if (source instanceof CompletionStage) {
                return new GroovyPromise<>(((CompletionStage<T>) source).toCompletableFuture());
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
                    || Iterator.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> AsyncStream<T> toAsyncStream(Object source) {
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

        private static <T> void completeFrom(CompletableFuture<T> cf, Future<T> future) {
            try {
                cf.complete(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                CancellationException ce = new CancellationException("Interrupted while adapting Future");
                ce.initCause(e);
                cf.completeExceptionally(ce);
            } catch (ExecutionException e) {
                cf.completeExceptionally(e.getCause() != null ? e.getCause() : e);
            } catch (Exception e) {
                cf.completeExceptionally(e);
            }
        }
    }
}
