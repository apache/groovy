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
package groovy.transform;

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitResult;
import groovy.lang.Closure;
import org.apache.groovy.runtime.async.AsyncSupport;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

/**
 * Backward-compatible facade for the async/await runtime support.
 * <p>
 * New code should use {@link groovy.concurrent.AsyncUtils} instead.
 * This class delegates all calls to {@link AsyncSupport}.
 *
 * @deprecated Use {@link groovy.concurrent.AsyncUtils} instead.
 * @see groovy.concurrent.AsyncUtils
 * @since 6.0.0
 */
@Deprecated
public class AsyncUtils {

    private AsyncUtils() { }

    public static <T> T await(Awaitable<T> awaitable) {
        return AsyncSupport.await(awaitable);
    }

    public static <T> T await(CompletableFuture<T> future) {
        return AsyncSupport.await(future);
    }

    public static <T> T await(CompletionStage<T> stage) {
        return AsyncSupport.await(stage);
    }

    public static <T> T await(Future<T> future) {
        return AsyncSupport.await(future);
    }

    public static <T> T await(Object source) {
        return AsyncSupport.await(source);
    }

    public static <T> Awaitable<T> async(Closure<T> closure) {
        return AsyncSupport.async(closure);
    }

    public static List<Object> awaitAll(Object... awaitables) {
        return AsyncSupport.awaitAll(awaitables);
    }

    public static Object awaitAny(Object... awaitables) {
        return AsyncSupport.awaitAny(awaitables);
    }

    public static List<AwaitResult<Object>> awaitAllSettled(Object... awaitables) {
        return AsyncSupport.awaitAllSettled(awaitables);
    }

    public static boolean isVirtualThreadsAvailable() {
        return AsyncSupport.isVirtualThreadsAvailable();
    }

    public static Executor getExecutor() {
        return AsyncSupport.getExecutor();
    }

    public static void setExecutor(Executor executor) {
        AsyncSupport.setExecutor(executor);
    }

    public static <T> AsyncStream<T> toAsyncStream(Object source) {
        return AsyncSupport.toAsyncStream(source);
    }

    public static <T> Awaitable<T> executeAsync(Closure<T> closure, Executor executor) {
        return AsyncSupport.executeAsync(closure, executor);
    }

    public static Awaitable<Void> executeAsyncVoid(Closure<?> closure, Executor executor) {
        return AsyncSupport.executeAsyncVoid(closure, executor);
    }

    public static <T> AsyncStream<T> generateAsyncStream(Closure<?> body) {
        return AsyncSupport.generateAsyncStream(body);
    }

    public static void yieldReturn(Object value) {
        AsyncSupport.yieldReturn(value);
    }

    public static void yieldReturn(Object generator, Object value) {
        AsyncSupport.yieldReturn(generator, value);
    }

    public static Throwable deepUnwrap(Throwable t) {
        return AsyncSupport.deepUnwrap(t);
    }
}
