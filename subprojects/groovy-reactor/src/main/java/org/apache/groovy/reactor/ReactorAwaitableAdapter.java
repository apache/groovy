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
package org.apache.groovy.reactor;

import groovy.concurrent.Awaitable;
import groovy.concurrent.AwaitableAdapter;
import org.apache.groovy.runtime.async.GroovyPromise;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adapter for Project Reactor types, enabling:
 * <ul>
 *   <li>{@code await mono} — awaits a single-value {@link Mono}</li>
 *   <li>{@code for await (item in flux)} — iterates over a {@link Flux}</li>
 * </ul>
 * <p>
 * Auto-discovered via {@link java.util.ServiceLoader} when {@code groovy-reactor}
 * is on the classpath.
 *
 * @since 6.0.0
 */
public class ReactorAwaitableAdapter implements AwaitableAdapter {

    @Override
    public boolean supportsAwaitable(Class<?> type) {
        return Mono.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Awaitable<T> toAwaitable(Object source) {
        if (source instanceof Mono<?> mono) {
            return (Awaitable<T>) GroovyPromise.of(mono.toFuture());
        }
        throw new IllegalArgumentException("Cannot convert to Awaitable: " + source.getClass());
    }

    @Override
    public boolean supportsIterable(Class<?> type) {
        return Flux.class.isAssignableFrom(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Iterable<T> toBlockingIterable(Object source) {
        if (source instanceof Flux<?> flux) {
            return (Iterable<T>) flux.toIterable();
        }
        throw new IllegalArgumentException("Cannot convert to Iterable: " + source.getClass());
    }
}
