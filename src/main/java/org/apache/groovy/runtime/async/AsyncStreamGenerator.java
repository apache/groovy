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

import groovy.concurrent.AsyncStream;
import groovy.concurrent.Awaitable;

import java.util.concurrent.SynchronousQueue;

/**
 * A producer/consumer implementation of {@link AsyncStream} used by
 * {@code async} methods that contain {@code yield return} statements.
 * <p>
 * The producer (method body) runs on a separate thread and calls
 * {@link #yield(Object)} for each emitted element. The consumer
 * calls {@link #moveNext()}/{@link #getCurrent()} — typically via
 * a {@code for await} loop.
 * <p>
 * Uses a {@link SynchronousQueue} to provide natural back-pressure:
 * the producer thread blocks at each {@code yield return} until the
 * consumer has consumed the previous element (mirroring C#'s async
 * iterator suspension semantics).
 * <p>
 * This class is an internal implementation detail and should not be referenced
 * directly by user code.
 *
 * @param <T> the element type
 * @since 6.0.0
 */
public class AsyncStreamGenerator<T> implements AsyncStream<T> {

    private static final Object DONE = new Object();

    private final SynchronousQueue<Object> queue = new SynchronousQueue<>();
    private T current;

    /**
     * Produces the next element. Called from the generator body when
     * a {@code yield return expr} statement is executed. Blocks until
     * the consumer is ready.
     */
    public void yield(Object value) {
        try {
            queue.put(new Item(value));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new java.util.concurrent.CancellationException("Interrupted during yield");
        }
    }

    /**
     * Signals that the generator has completed (no more elements).
     */
    public void complete() {
        try {
            queue.put(DONE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Signals that the generator failed with an exception.
     */
    public void error(Throwable t) {
        try {
            queue.put(new ErrorItem(t));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Awaitable<Boolean> moveNext() {
        try {
            Object next = queue.take();
            if (next == DONE) {
                return Awaitable.of(false);
            }
            if (next instanceof ErrorItem ei) {
                Throwable cause = ei.error;
                if (cause instanceof Error err) throw err;
                throw sneakyThrow(cause);
            }
            current = (T) ((Item) next).value;
            return Awaitable.of(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new java.util.concurrent.CancellationException("Interrupted during moveNext");
        }
    }

    @Override
    public T getCurrent() {
        return current;
    }

    // Wrapper to handle null values in the queue
    private record Item(Object value) { }
    private record ErrorItem(Throwable error) { }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}
