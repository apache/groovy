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
package org.apache.groovy.runtime.async;

import groovy.concurrent.Awaitable;
import groovy.lang.Closure;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Closure-specific async utilities that have no pure-Java equivalent.
 * <p>
 * These methods create {@link Closure} subclasses and are therefore
 * inherently Groovy-specific. They predate the {@code async} keyword
 * and are retained for programmatic use. The {@code async { ... }}
 * keyword syntax is preferred for most use cases.
 *
 * @see AsyncSupport
 * @since 6.0.0
 */
public final class AsyncClosureUtils {

    private AsyncClosureUtils() { }

    /**
     * Wraps a closure so that each invocation executes the body
     * asynchronously and returns an {@link Awaitable}.
     *
     * <pre>{@code
     * def asyncTask = AsyncClosureUtils.wrapAsync { expensiveWork() }
     * def result = await asyncTask()
     * }</pre>
     *
     * @param closure the closure to wrap
     * @param <T>     the result type
     * @return a closure that returns an Awaitable on each call
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Awaitable<T>> wrapAsync(Closure<T> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        return new Closure<Awaitable<T>>(closure.getOwner(), closure.getThisObject()) {
            @SuppressWarnings("unused")
            public Awaitable<T> doCall(Object... args) {
                return GroovyPromise.of(CompletableFuture.supplyAsync(() -> {
                    try {
                        return closure.call(args);
                    } catch (Throwable t) {
                        throw AsyncSupport.wrapForFuture(t);
                    }
                }, AsyncSupport.getExecutor()));
            }
        };
    }

    /**
     * Wraps a generator closure so that each invocation returns an
     * {@link Iterable} backed by a {@link GeneratorBridge}.
     *
     * @param closure the generator closure; receives a GeneratorBridge
     *                as its first parameter
     * @param <T>     the element type
     * @return a closure that produces an Iterable on each call
     */
    @SuppressWarnings("unchecked")
    public static <T> Closure<Iterable<T>> wrapAsyncGenerator(Closure<?> closure) {
        Objects.requireNonNull(closure, "closure must not be null");
        return new Closure<Iterable<T>>(closure.getOwner(), closure.getThisObject()) {
            @SuppressWarnings("unused")
            public Iterable<T> doCall(Object... args) {
                GeneratorBridge<T> bridge = new GeneratorBridge<>();
                Object[] allArgs = new Object[args.length + 1];
                allArgs[0] = bridge;
                System.arraycopy(args, 0, allArgs, 1, args.length);
                AsyncSupport.getExecutor().execute(() -> {
                    try {
                        closure.call(allArgs);
                        bridge.complete();
                    } catch (GeneratorBridge.GeneratorClosedException ignored) {
                    } catch (Throwable t) {
                        bridge.completeExceptionally(t);
                    }
                });
                return () -> bridge;
            }
        };
    }
}
