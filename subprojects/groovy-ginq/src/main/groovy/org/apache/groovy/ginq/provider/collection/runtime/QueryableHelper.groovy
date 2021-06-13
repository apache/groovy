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
package org.apache.groovy.ginq.provider.collection.runtime

import groovy.transform.CompileStatic

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

import static org.apache.groovy.ginq.provider.collection.runtime.Queryable.from
/**
 * Helper for {@link Queryable}
 *
 * @since 4.0.0
 */
@CompileStatic
class QueryableHelper {
    /**
     * Make {@link Queryable} instance's data source records being able to access via aliases
     *
     * @param queryable the original {@link Queryable} instance
     * @param aliasList the aliases of clause {@code from} and joins
     * @return the result {@link Queryable} instance
     * @since 4.0.0
     */
    static <T> Queryable<SourceRecord<T>> navigate(Queryable<? extends T> queryable, List<String> aliasList) {
        List<SourceRecord<T>> sourceRecordList =
                queryable.stream()
                        .map(e -> new SourceRecord<T>(e, aliasList))
                        .collect(Collectors.toList())

        return from(sourceRecordList)
    }

    /**
     * Returns single value of {@link Queryable} instance
     *
     * @param queryable the {@link Queryable} instance
     * @return the single value
     * @throws TooManyValuesException if the {@link Queryable} instance contains more than one value
     * @since 4.0.0
     */
    static <T> T singleValue(final Queryable<? extends T> queryable) {
        List<? extends T> list = queryable.toList()
        int size = list.size()

        if (0 == size) {
            return null
        }
        if (1 == size) {
            return list.get(0)
        }

        throw new TooManyValuesException("subquery returns more than one value: $list")
    }

    static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, ThreadPoolHolder.THREAD_POOL)
    }

    static <T, U> CompletableFuture<U> supplyAsync(Function<? super T, ? extends U> function, T param) {
        return CompletableFuture.supplyAsync(() -> { function.apply(param) }, ThreadPoolHolder.THREAD_POOL)
    }

    static boolean isParallel() {
        return TRUE_STR == getVar(PARALLEL)
    }

    static <T> void setVar(String name, T value) {
        VAR_HOLDER.get().put(name, value)
    }

    static <T> T getVar(String name) {
        (T) VAR_HOLDER.get().get(name)
    }

    static <T> T  removeVar(String name) {
        (T) VAR_HOLDER.get().remove(name)
    }

    /**
     * Shutdown to release resources
     *
     * @param mode 0: immediate, 1: abort
     * @return list of tasks that never commenced execution
     */
    static List<Runnable> shutdown(int mode) {
        if (0 == mode) {
            ThreadPoolHolder.THREAD_POOL.shutdown()
            while (!ThreadPoolHolder.THREAD_POOL.awaitTermination(250, TimeUnit.MILLISECONDS)) {
                // do nothing, just wait to terminate
            }
            return Collections.emptyList()
        } else if (1 == mode) {
            return ThreadPoolHolder.THREAD_POOL.shutdownNow()
        } else {
            throw new IllegalArgumentException("Invalid mode: $mode")
        }
    }

    private static final ThreadLocal<Map<String, Object>> VAR_HOLDER = ThreadLocal.<Map<String, Object>> withInitial(() -> new LinkedHashMap<>())
    private static final String PARALLEL = "parallel"
    private static final String TRUE_STR = "true"

    private QueryableHelper() {}

    private static class ThreadPoolHolder {
        static int seq
        static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), (Runnable r) -> {
            Thread t = new Thread(r)
            t.setName("ginq-thread-" + seq++)
            t.setDaemon(true)
            return t
        })
        private ThreadPoolHolder() {}
    }
}
