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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FastArray implements Cloneable, Serializable {
    private static final long serialVersionUID = -9143440116071577249L;
    private Object[] data;
    public int size;
    public static final FastArray EMPTY_LIST = new FastArray(0);

    public FastArray(int initialCapacity) {
        data = new Object[initialCapacity];
    }

    public FastArray() {
       this (8);
    }

    public FastArray(Collection c) {
        this (c.toArray());
    }

    public FastArray(Object[] objects) {
        data = objects;
        size = objects.length;
    }

    public Object get(int index) {
        return data [index];
    }

    public void add(Object o) {
        if (size == data.length) {
            Object [] newData = new Object[size == 0 ? 8 : size*2];
            System.arraycopy(data, 0, newData, 0, size);
            data = newData;
        }
        data [size++] = o;
    }

    public void set(int index, Object o) {
        data [index] = o;
    }

    public int size() {
        return size;
    }

    public void clear() {
        data = new Object[data.length];
        size = 0;
    }

    public void addAll(FastArray newData) {
        addAll(newData.data, newData.size);
    }

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

    public FastArray copy() {
        final Object[] newData = new Object[size];
        System.arraycopy(data, 0, newData, 0, size);
        return new FastArray(newData);
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addAll(List coll) {
        final Object[] newData = coll.toArray();
        addAll(newData, newData.length);
    }

    public void remove(int index) {
        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(data, index+1, data, index, numMoved);
        data[--size] = null;
    }

    public List toList () {
        if (size==0) {
            return Collections.emptyList();
        } else if (size==1) {
            return Collections.singletonList(data[0]);
        }
        return new AbstractList() {

            @Override
            public Object get(int index) {
                return FastArray.this.get(index);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    public Object[] getArray() {
        return data;
    }

    public String toString() {
        if (size() == 0) return "[]";
        return toList().toString();
    }
}
