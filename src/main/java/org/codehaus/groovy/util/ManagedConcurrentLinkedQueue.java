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
package org.codehaus.groovy.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A queue that stores values wrapped in a Reference, the type of which is
 * determined by the provided {@link ReferenceBundle}. References stored
 * in this queue will be removed when reference processing occurs.
 * <p>
 * This queue is backed by a {@link ConcurrentLinkedQueue} and is thread safe.
 * The iterator will only return non-null values (reachable) and is based on
 * the "weakly consistent" iterator of the underlying {@link ConcurrentLinkedQueue}.
 *
 * @param <T> the type of values to store
 */
public class ManagedConcurrentLinkedQueue<T> implements Iterable<T> {

    private final ReferenceBundle bundle;
    private final ConcurrentLinkedQueue<Element<T>> queue;

    /**
     * Creates an empty ManagedConcurrentLinkedQueue that will use the provided
     * {@code ReferenceBundle} to store values as the given Reference
     * type.
     *
     * @param bundle used to create the appropriate Reference type
     *               for the values stored
     */
    public ManagedConcurrentLinkedQueue(ReferenceBundle bundle) {
        this.bundle = bundle;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Adds the specified value to the queue.
     *
     * @param value the value to add
     */
    public void add(T value) {
        Element<T> e = new Element<>(value);
        queue.offer(e);
    }

    /**
     * Returns {@code true} if this queue contains no elements.
     * <p>
     * This method does not check the elements to verify they contain
     * non-null reference values.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns an array containing all values from this queue in the sequence they
     * were added.
     *
     * @param tArray the array to populate if big enough, else a new array with
     *               the same runtime type
     * @return an array containing all non-null values in this queue
     */
    public T[] toArray(T[] tArray) {
        return values().toArray(tArray);
    }

    /**
     * Returns a list containing all values from this queue in the
     * sequence they were added.
     */
    public List<T> values() {
        List<T> result = new ArrayList<>();
        for (Iterator<T> itr = iterator(); itr.hasNext(); ) {
            result.add(itr.next());
        }
        return result;
    }

    /**
     * Returns an iterator over all non-null values in this queue.  The values should be
     * returned in the order they were added.
     */
    @Override
    public Iterator<T> iterator() {
        return new Itr(queue.iterator());
    }

    private class Element<V> extends ManagedReference<V> {

        Element(V value) {
            super(bundle, value);
        }

        @Override
        public void finalizeReference() {
            queue.remove(this);
            super.finalizeReference();
        }

    }

    private class Itr implements Iterator<T> {

        final Iterator<Element<T>> wrapped;

        T value;
        Element<T> current;
        boolean exhausted;

        Itr(Iterator<Element<T>> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean hasNext() {
            if (!exhausted && value == null) {
                advance();
            }
            return value != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T next = value;
            value = null;
            return next;
        }

        @Override
        public void remove() {
            if (current == null || value != null) {
                throw new IllegalStateException("Next method has not been called");
            }
            wrapped.remove();
            current = null;
        }

        private void advance() {
            while (wrapped.hasNext()) {
                Element<T> e = wrapped.next();
                T v = e.get();
                if (v != null) {
                    current = e;
                    value = v;
                    return;
                }
                wrapped.remove();
            }
            value = null;
            current = null;
            exhausted = true;
        }

    }

}
