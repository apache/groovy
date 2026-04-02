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

import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.groovy.runtime.async.GroovyPromise;

import java.util.concurrent.CompletableFuture;

/**
 * Adapter for RxJava 3 types, enabling:
 * <ul>
 *   <li>{@code await single} — awaits a {@link Single}</li>
 *   <li>{@code await maybe} — awaits a {@link Maybe} (nullable result)</li>
 *   <li>{@code await completable} — awaits a {@link Completable}</li>
 *   <li>{@code for await (item in observable)} — iterates over an {@link Observable}</li>
 *   <li>{@code for await (item in flowable)} — iterates over a {@link Flowable}</li>
 * </ul>
 * <p>
 * Auto-discovered via {@link java.util.ServiceLoader} when {@code groovy-rxjava}
 * is on the classpath.
 *
 * @since 6.0.0
 */
public class RxJavaAwaitableAdapter implements AwaitableAdapter {

    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Single.class.isAssignableFrom(type)
                || Maybe.class.isAssignableFrom(type)
                || Completable.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        if (source instanceof Single<?> single) {
            return GroovyPromise.of(
                    (CompletableFuture<T>) single.toCompletionStage().toCompletableFuture());
        }
        if (source instanceof Maybe<?> maybe) {
            return GroovyPromise.of(
                    (CompletableFuture<T>) maybe.toCompletionStage(null).toCompletableFuture());
        }
        if (source instanceof Completable completable) {
            return (Awaitable<T>) GroovyPromise.of(
                    completable.toCompletionStage(null).toCompletableFuture());
        }
        throw new IllegalArgumentException("Cannot convert to Awaitable: " + source.getClass());
    }

    @Override
    public boolean supportsIterable(Class<?> type) {
        return Observable.class.isAssignableFrom(type)
                || Flowable.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Iterable<T> toBlockingIterable(Object source) {
        if (source instanceof Observable<?> observable) {
            return (Iterable<T>) observable.blockingIterable();
        }
        if (source instanceof Flowable<?> flowable) {
            return (Iterable<T>) flowable.blockingIterable();
        }
        throw new IllegalArgumentException("Cannot convert to Iterable: " + source.getClass());
    }
}
