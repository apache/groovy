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

/**
 * This class provides a very simple linked list of memory managed elements.
 * This class does not support concurrent modifications nor will it check
 * for them. This class is also not thread safe.
 *
 * @since 1.6
 * @deprecated replaced by {@link ManagedConcurrentLinkedQueue}
 */
@Deprecated
public class ManagedLinkedList<T> {

    private final class Element<V> extends ManagedReference<V> {
        Element next;
        Element previous;

        public Element(ReferenceBundle bundle, V value) {
            super(bundle, value);
        }

        @Override
        public void finalizeReference() {
            if (previous != null && previous.next != null) {
                previous.next = next;
            }
            if (next != null && next.previous != null) {
                next.previous = previous;
            }
            if (this == head) head = next;
            next = null;
            if (this == tail) tail = previous;
            previous = null;
            super.finalizeReference();
        }
    }

    private final class Iter implements Iterator<T> {
        private Element<T> current;
        private boolean currentHandled = false;

        Iter() {
            current = head;
        }

        public boolean hasNext() {
            if (current == null) return false;
            if (currentHandled) {
                return current.next != null;
            } else {
                return true;
            }
        }

        public T next() {
            if (currentHandled) current = current.next;
            currentHandled = true;
            if (current == null) return null;
            return current.get();
        }

        public void remove() {
            if (current != null) current.finalizeReference();
        }
    }

    private Element<T> tail;
    private Element<T> head;
    private final ReferenceBundle bundle;

    public ManagedLinkedList(ReferenceBundle bundle) {
        this.bundle = bundle;
    }

    /**
     * adds a value to the list
     *
     * @param value the value
     */
    public void add(T value) {
        Element<T> element = new Element<T>(bundle, value);
        element.previous = tail;
        if (tail != null) tail.next = element;
        tail = element;
        if (head == null) head = element;
    }

    /**
     * returns an iterator, which allows the removal of elements.
     * The next() method of the iterator may return null values. This
     * is especially the case if the value was removed.
     *
     * @return the Iterator
     */
    public Iterator<T> iterator() {
        return new Iter();
    }

    /**
     * Returns an array of non null elements from the source array.
     *
     * @param tArray the source array
     * @return the array
     */
    public T[] toArray(T[] tArray) {
        List<T> array = new ArrayList<T>(100);
        for (Iterator<T> it = iterator(); it.hasNext();) {
            T val = it.next();
            if (val != null) array.add(val);
        }
        return array.toArray(tArray);
    }

    /**
     * returns if the list is empty
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return head == null;
    }
}
