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

import java.io.Closeable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.SynchronousQueue;

/**
 * A producer/consumer bridge for async generators ({@code yield return}).
 * <p>
 * The generator closure runs on a separate thread and calls {@link #yield(Object)}
 * to produce values. The consumer iterates using {@link #hasNext()}/{@link #next()}.
 * A {@link SynchronousQueue} provides the handoff — each {@code yield} blocks
 * until the consumer takes the value, providing natural back-pressure.
 * <p>
 * With virtual threads (JDK 21+), both the producer and consumer block cheaply.
 * On JDK 17-20, the producer runs on a platform thread from the cached pool.
 *
 * @param <T> the element type
 * @since 6.0.0
 */
public final class GeneratorBridge<T> implements Iterator<T>, Closeable {

    private static final Object DONE = new Object();
    private static final Object ERROR = new Object();

    private final SynchronousQueue<Object[]> handoff = new SynchronousQueue<>();
    private Object[] pending;
    private boolean done;
    private volatile boolean closed;

    /**
     * Called by the generator (producer thread) to yield a value.
     * Blocks until the consumer calls {@link #next()}.
     *
     * @param value the value to yield (may be null)
     * @throws GeneratorClosedException if the consumer has closed the bridge
     */
    public void yield(Object value) {
        if (closed) throw new GeneratorClosedException();
        try {
            handoff.put(new Object[]{value});
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GeneratorClosedException();
        }
    }

    /**
     * Called internally when the generator completes normally.
     */
    void complete() {
        try {
            handoff.put(new Object[]{DONE});
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Called internally when the generator throws an exception.
     */
    void completeExceptionally(Throwable error) {
        try {
            handoff.put(new Object[]{ERROR, error});
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean hasNext() {
        if (done) return false;
        if (pending != null) return true;
        try {
            pending = handoff.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            done = true;
            return false;
        }
        if (pending[0] == DONE) {
            done = true;
            pending = null;
            return false;
        }
        if (pending[0] == ERROR) {
            done = true;
            Throwable error = (Throwable) pending[1];
            pending = null;
            if (error instanceof RuntimeException re) throw re;
            if (error instanceof Error err) throw err;
            throw new RuntimeException(error);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T next() {
        if (!hasNext()) throw new NoSuchElementException();
        T value = (T) pending[0];
        pending = null;
        return value;
    }

    @Override
    public void close() {
        closed = true;
        done = true;
        // Drain any pending put from the producer so it can unblock
        handoff.poll();
    }

    /**
     * Thrown when a generator tries to yield after the consumer has closed the bridge.
     */
    static final class GeneratorClosedException extends RuntimeException {
        GeneratorClosedException() {
            super("Generator closed by consumer");
        }
    }
}
