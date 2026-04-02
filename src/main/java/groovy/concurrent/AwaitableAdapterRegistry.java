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

import org.apache.groovy.runtime.async.AsyncSupport;
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
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
 *
 * @since 6.0.0
 */
public final class AwaitableAdapterRegistry {

    private static final List<AwaitableAdapter> adapters = new CopyOnWriteArrayList<>();

    private static volatile ClassValue<AwaitableAdapter> awaitableCache = buildAwaitableCache();

    static {
        // Load SPI adapters
        try {
            ServiceLoader<AwaitableAdapter> loader = ServiceLoader.load(
                    AwaitableAdapter.class, AwaitableAdapterRegistry.class.getClassLoader());
            for (Iterator<AwaitableAdapter> it = loader.iterator(); it.hasNext(); ) {
                try {
                    adapters.add(it.next());
                } catch (Throwable ignored) {
                    // Skip adapters that fail to load (missing dependencies)
                }
            }
        } catch (Throwable ignored) {
            // ServiceLoader failure — continue with built-in adapter only
        }
        // Built-in fallback adapter (lowest priority)
        adapters.add(new BuiltInAdapter());
    }

    private AwaitableAdapterRegistry() { }

    /**
     * Registers an adapter at the highest priority (before SPI-loaded adapters).
     */
    public static void register(AwaitableAdapter adapter) {
        Objects.requireNonNull(adapter);
        adapters.add(0, adapter);
        awaitableCache = buildAwaitableCache();
    }

    /**
     * Removes a previously registered adapter.
     */
    public static void unregister(AwaitableAdapter adapter) {
        adapters.remove(adapter);
        awaitableCache = buildAwaitableCache();
    }

    /**
     * Converts the given source to an {@link Awaitable}.
     */
    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> toAwaitable(Object source) {
        if (source == null) {
            return (Awaitable<T>) Awaitable.of(null);
        }
        if (source instanceof Awaitable) return (Awaitable<T>) source;
        Class<?> type = source.getClass();
        AwaitableAdapter adapter = awaitableCache.get(type);
        if (adapter != null) {
            return adapter.toAwaitable(source);
        }
        throw new IllegalArgumentException(
                "No Awaitable adapter found for type: " + type.getName()
                        + ". Register an AwaitableAdapter via ServiceLoader or AwaitableAdapterRegistry.register().");
    }

    /**
     * Converts the given source to a blocking {@link Iterable} for {@code for await}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<T> toBlockingIterable(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null to Iterable");
        }
        Class<?> type = source.getClass();
        for (AwaitableAdapter adapter : adapters) {
            if (adapter.supportsIterable(type)) {
                return adapter.toBlockingIterable(source);
            }
        }
        throw new IllegalArgumentException(
                "No Iterable adapter found for type: " + type.getName()
                        + ". Register an AwaitableAdapter via ServiceLoader or AwaitableAdapterRegistry.register().");
    }

    private static ClassValue<AwaitableAdapter> buildAwaitableCache() {
        return new ClassValue<>() {
            @Override
            protected AwaitableAdapter computeValue(Class<?> type) {
                for (AwaitableAdapter adapter : adapters) {
                    if (adapter.supportsAwaitable(type)) {
                        return adapter;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Built-in adapter for JDK Future types.
     */
    private static final class BuiltInAdapter implements AwaitableAdapter {

        @Override
        public boolean supportsAwaitable(Class<?> type) {
            return CompletionStage.class.isAssignableFrom(type)
                    || Future.class.isAssignableFrom(type);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Awaitable<T> toAwaitable(Object source) {
            if (source instanceof CompletableFuture) {
                return GroovyPromise.of((CompletableFuture<T>) source);
            }
            if (source instanceof CompletionStage) {
                return GroovyPromise.of(((CompletionStage<T>) source).toCompletableFuture());
            }
            if (source instanceof Future) {
                CompletableFuture<T> cf = new CompletableFuture<>();
                Future<T> future = (Future<T>) source;
                // Wrap blocking Future in a CF (submitted to default executor)
                CompletableFuture.runAsync(() -> {
                    try {
                        cf.complete(future.get());
                    } catch (java.util.concurrent.ExecutionException e) {
                        cf.completeExceptionally(e.getCause());
                    } catch (Throwable e) {
                        cf.completeExceptionally(e);
                    }
                }, AsyncSupport.getExecutor());
                return GroovyPromise.of(cf);
            }
            throw new IllegalArgumentException("Cannot convert to Awaitable: " + source.getClass());
        }
    }
}
