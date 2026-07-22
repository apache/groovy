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

import groovy.concurrent.AwaitResult;
import groovy.concurrent.Awaitable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Combinators for joining multiple async sources ({@code all}, {@code any},
 * {@code first}, {@code allSettled}).
 * <p>
 * Package-private implementation detail of {@link AsyncSupport}. Blocking
 * variants are thin wrappers over the non-blocking ones via
 * {@link AsyncSupport#await(Object)}, so each combinator has a single
 * implementation of its joining algorithm.
 *
 * @since 6.0.0
 */
final class AwaitCombinators {

    private AwaitCombinators() { }

    // ---- blocking -------------------------------------------------------

    @SuppressWarnings("unchecked")
    static <T> List<T> all(Object... sources) {
        return (List<T>) AsyncSupport.await(allAsync(sources));
    }

    static <T> T any(Object... sources) {
        return AsyncSupport.await(AwaitCombinators.<T>anyAsync(sources));
    }

    static <T> T first(Object... sources) {
        return AsyncSupport.await(AwaitCombinators.<T>firstAsync(sources));
    }

    static List<AwaitResult<Object>> allSettled(Object... sources) {
        return AsyncSupport.await(allSettledAsync(sources));
    }

    // ---- non-blocking ---------------------------------------------------

    static Awaitable<List<Object>> allAsync(Object... sources) {
        CompletableFuture<?>[] futures = toFutures(sources);

        // allOf is fail-fast but does not guarantee which exception propagates
        // when several futures fail; track the temporally-first error ourselves.
        var firstError = new AtomicReference<Throwable>();
        for (CompletableFuture<?> future : futures) {
            future.whenComplete((value, error) -> {
                if (error != null) {
                    firstError.compareAndSet(null, error);
                }
            });
        }

        CompletableFuture<List<Object>> combined = CompletableFuture.allOf(futures)
                .thenApply(ignored -> getJoinedResults(futures));

        CompletableFuture<List<Object>> withFirstError = combined.exceptionally(error -> {
            Throwable first = firstError.get();
            if (first != null && first != error && first != error.getCause()) {
                throw asCompletionException(first);
            }
            throw asCompletionException(error);
        });
        return GroovyPromise.of(withFirstError);
    }

    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> anyAsync(Object... sources) {
        // Guard against empty sources: CompletableFuture.anyOf() over an empty
        // array returns a future that never completes, which would hang await().
        validateNonEmptySources(sources);
        return (Awaitable<T>) GroovyPromise.of(CompletableFuture.anyOf(toFutures(sources)));
    }

    @SuppressWarnings("unchecked")
    static <T> Awaitable<T> firstAsync(Object... sources) {
        validateNonEmptySources(sources);
        CompletableFuture<T>[] futures = (CompletableFuture<T>[]) toFutures(sources);
        CompletableFuture<T> result = new CompletableFuture<>();
        var remainingFailures = new AtomicInteger(futures.length);
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>(futures.length));
        for (CompletableFuture<T> future : futures) {
            future.whenComplete((value, error) -> {
                if (error == null) {
                    result.complete(value);
                    return;
                }
                errors.add(error);
                if (remainingFailures.decrementAndGet() == 0) {
                    result.completeExceptionally(aggregateFirstFailures(futures.length, errors));
                }
            });
        }
        return GroovyPromise.of(result);
    }

    static Awaitable<List<AwaitResult<Object>>> allSettledAsync(Object... sources) {
        CompletableFuture<?>[] futures = toFutures(sources);
        CompletableFuture<List<AwaitResult<Object>>> combined = CompletableFuture.allOf(
                        Arrays.stream(futures)
                                .map(future -> future.handle((value, error) -> null))
                                .toArray(CompletableFuture[]::new))
                .thenApply(ignored -> getAwaitResults(futures));
        return GroovyPromise.of(combined);
    }

    // ---- helpers --------------------------------------------------------

    private static CompletableFuture<?>[] toFutures(Object... sources) {
        if (sources == null) {
            return new CompletableFuture<?>[0];
        }
        CompletableFuture<?>[] futures = new CompletableFuture<?>[sources.length];
        for (int i = 0; i < sources.length; i++) {
            futures[i] = Awaitable.from(sources[i]).toCompletableFuture();
        }
        return futures;
    }

    private static void validateNonEmptySources(Object[] sources) {
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null");
        }
        if (sources.length == 0) {
            throw new IllegalArgumentException("sources must not be empty");
        }
        for (Object source : sources) {
            if (source == null) {
                throw new IllegalArgumentException("sources must not contain null elements");
            }
        }
    }

    /**
     * Builds the aggregate {@link CompletionException} for {@code first}
     * when every source fails. The first failure is the cause; remaining
     * failures are attached as suppressed exceptions.
     */
    private static CompletionException aggregateFirstFailures(int sourceCount, List<Throwable> errors) {
        CompletionException aggregate = new CompletionException(
                "All " + sourceCount + " tasks failed", errors.get(0));
        for (int i = 1; i < errors.size(); i++) {
            aggregate.addSuppressed(errors.get(i));
        }
        return aggregate;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> getJoinedResults(CompletableFuture<?>[] futures) {
        List<T> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> future : futures) {
            results.add((T) future.join());
        }
        return results;
    }

    private static List<AwaitResult<Object>> getAwaitResults(CompletableFuture<?>[] futures) {
        List<AwaitResult<Object>> results = new ArrayList<>(futures.length);
        for (CompletableFuture<?> future : futures) {
            try {
                results.add(AwaitResult.success(future.join()));
            } catch (CompletionException e) {
                results.add(AwaitResult.failure(AsyncSupport.unwrap(e)));
            } catch (CancellationException e) {
                results.add(AwaitResult.failure(e));
            }
        }
        return results;
    }

    private static CompletionException asCompletionException(Throwable error) {
        return error instanceof CompletionException ce ? ce : new CompletionException(error);
    }
}
