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
package groovy.util.concurrent.async;

import org.apache.groovy.util.SystemUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Helper class for async/await operations
 *
 * @since 6.0
 */
public class AsyncHelper {
    private static final int PARALLELISM = SystemUtil.getIntegerSafe("groovy.async.parallelism", Runtime.getRuntime().availableProcessors() + 1);
    private static final Executor DEFAULT_EXECUTOR;
    private static int seq;

    static {
        Executor tmpExecutor;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle mh = lookup.findStatic(
                Executors.class,
                "newVirtualThreadPerTaskExecutor",
                MethodType.methodType(ExecutorService.class)
            );
            tmpExecutor = (Executor) mh.invoke();
        } catch (Throwable throwable) {
            // Fallback to default thread pool if virtual threads are not available
            tmpExecutor = Executors.newFixedThreadPool(PARALLELISM, r -> {
                Thread t = new Thread(r);
                t.setName("async-thread-" + seq++);
                return t;
            });
        }
        DEFAULT_EXECUTOR = tmpExecutor;
    }

    /**
     * Submits a supplier for asynchronous execution using the default executor
     *
     * @param supplier the supplier
     * @param <T>      the result type
     * @return the promise
     */
    public static <T> Promise<T> async(Supplier<T> supplier) {
        return SimplePromise.of(supplier, DEFAULT_EXECUTOR);
    }

    /**
     * Submits a supplier for asynchronous execution using the provided executor
     *
     * @param supplier the supplier
     * @param executor the executor
     * @param <T>      the result type
     * @return the promise
     */
    public static <T> Promise<T> async(Supplier<T> supplier, Executor executor) {
        return SimplePromise.of(supplier, executor);
    }

    /**
     * Awaits the result of an awaitable
     *
     * @param awaitable the awaitable
     * @param <T>       the result type
     * @return the result
     */
    public static <T> T await(Awaitable<T> awaitable) {
        return awaitable.await();
    }

    private AsyncHelper() {}
}
