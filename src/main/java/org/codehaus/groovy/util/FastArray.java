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

import java.io.Serial;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Resizable object array optimized for append-heavy internal use and lightweight list views.
 */
public class FastArray implements Cloneable, Serializable {
    @Serial private static final long serialVersionUID = -9143440116071577249L;

    /**
     * Shared empty instance with zero capacity.
     */
    public static final FastArray EMPTY_LIST = new FastArray(0);
    private Object[] data;

    /**
     * Number of populated elements in the backing array.
     */
    public int size;

    /**
     * Creates an empty array with the specified initial capacity.
     *
     * @param initialCapacity the initial storage size
     */
    public FastArray(int initialCapacity) {
        data = new Object[initialCapacity];
    }

    /**
     * Creates an empty array with the default initial capacity.
     */
    public FastArray() {
       this (8);
    }

    /**
     * Creates an array containing the elements of the supplied collection.
     *
     * @param c the values to copy
     */
    public FastArray(Collection c) {
        this (c.toArray());
    }

    /**
     * Creates an array backed by the supplied object array.
     *
     * @param objects the backing storage containing the initial elements
     */
    public FastArray(Object[] objects) {
        data = objects;
        size = objects.length;
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index the populated index to read
     * @return the stored element
     */
    public Object get(int index) {
        return data [index];
    }

    /**
     * Appends an element to the end of this array, growing the backing storage when needed.
     *
     * @param o the element to append
     */
    public void add(Object o) {
        if (size == data.length) {
            Object [] newData = new Object[size == 0 ? 8 : size*2];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
        data [size++] = o;
    }

    /**
     * Replaces the element stored at the specified index.
     *
     * @param index the populated index to update
     * @param o the replacement element
     */
    public void set(int index, Object o) {
        data [index] = o;
    }

    /**
     * Returns the number of populated elements.
     *
     * @return the logical size of this array
     */
    public int size() {
        return size;
    }

    /**
     * Removes all populated elements while preserving the current capacity.
     */
    public void clear() {
        data = new Object[data.length];
        size = 0;
    }

    /**
     * Appends the populated contents of another {@code FastArray}.
     *
     * @param newData the source array to append
     */
    public void addAll(FastArray newData) {
        addAll(newData.data, newData.size);
    }

    /**
     * Appends the first {@code size} elements from the supplied array.
     *
     * @param newData the source storage
     * @param size the number of elements to append
     */
    public void addAll(Object [] newData, int size) {
        if (size == 0)
          return;
        final int newSize = this.size + size;
        if (newSize > data.length) {
            Object[] nd = new Object [newSize];
            System.arraycopy(data, 0, nd, 0, this.size);
            data = nd;
        }
        System.arraycopy(newData, 0, data, this.size, size);
        this.size = newSize;
    }

    /**
     * Returns a copy containing the populated elements of this array.
     *
     * @return a new {@code FastArray} with copied storage
     */
    public FastArray copy() {
        final Object[] newData = new Object[size];
        System.arraycopy(data, 0, newData, 0, size);
        return new FastArray(newData);
    }

    /**
     * Indicates whether this array contains any populated elements.
     *
     * @return {@code true} if no elements are stored
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Appends the contents of the supplied list.
     *
     * @param coll the values to append
     */
    public void addAll(List coll) {
        final Object[] newData = coll.toArray();
        addAll(newData, newData.length);
    }

    /**
     * Removes the element at the specified index and compacts the populated range.
     *
     * @param index the populated index to remove
     */
    public void remove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index+1, data, index, numMoved);
        data[--size] = null;
    }

    /**
     * Returns a list view of the populated elements.
     *
     * @return a list reflecting the current contents of this array
     */
    public List toList () {
        if (size==0) {
            return Collections.emptyList();
        } else if (size==1) {
            return Collections.singletonList(data[0]);
        }
        return new AbstractList() {

            /** {@inheritDoc} */
            @Override
            public Object get(int index) {
                return FastArray.this.get(index);
            }

            /** {@inheritDoc} */
            @Override
            public int size() {
                return size;
            }
        };
    }

    /**
     * Returns the backing storage array.
     *
     * @return the underlying array, including unused slots
     */
    public Object[] getArray() {
        return data;
    }

    /**
     * Returns a list-style string containing the populated elements.
     *
     * @return the string form of the populated contents
     */
    @Override
    public String toString() {
        if (size() == 0) return "[]";
        return toList().toString();
    }

    /**
     * Returns a shallow copy with duplicated backing storage.
     *
     * @return a cloned {@code FastArray}
     */
    @Override
    public FastArray clone() {
        try {
            FastArray clone = (FastArray) super.clone();
            clone.size = size;
            clone.data = data.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
