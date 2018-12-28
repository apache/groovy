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
package groovy.swing.impl;

import javax.swing.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@code ListModel} implementation that's backed by a live {@code List}.
 */
public class ListWrapperListModel<E> extends AbstractListModel {
    private final List<E> delegate;

    public ListWrapperListModel(List<E> delegateList) {
        this.delegate = delegateList;
    }

    public List<E> getDelegateList() {
        return delegate;
    }

    public int getSize() {
        return delegate.size();
    }

    public Object getElementAt(int i) {
        return delegate.get(i);
    }

    public E set(int i, E e) {
        E element = delegate.set(i, e);
        fireContentsChanged(this, i, i);
        return element;
    }

    public void clear() {
        int i = delegate.size() - 1;
        delegate.clear();
        if (i >= 0) {
            fireIntervalRemoved(this, 0, i);
        }
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public ListIterator<E> listIterator() {
        return delegate.listIterator();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public void add(int i, E e) {
        int index = delegate.size();
        delegate.add(i, e);
        fireIntervalAdded(this, index, index);
    }

    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    public boolean addAll(Collection<? extends E> es) {
        int i = delegate.size();
        boolean added = delegate.addAll(es);
        if (added) fireIntervalAdded(this, i, i + es.size());
        return added;
    }

    public E remove(int i) {
        E element = delegate.remove(i);
        fireIntervalRemoved(this, i, i);
        return element;
    }

    public boolean addAll(int i, Collection<? extends E> es) {
        boolean added = delegate.addAll(i, es);
        if (added) fireIntervalAdded(this, i, i + es.size());
        return added;
    }

    public ListIterator<E> listIterator(int i) {
        return delegate.listIterator(i);
    }

    public boolean containsAll(Collection<?> objects) {
        return delegate.containsAll(objects);
    }

    public boolean remove(Object o) {
        int i = indexOf(o);
        boolean rv = delegate.remove(o);
        if (i >= 0) {
            fireIntervalRemoved(this, i, i);
        }
        return rv;
    }

    public boolean add(E e) {
        int i = delegate.size();
        boolean added = delegate.add(e);
        if (added) fireIntervalAdded(this, i, i);
        return added;
    }

    public E get(int i) {
        return delegate.get(i);
    }

    public <T> T[] toArray(T[] ts) {
        return delegate.toArray(ts);
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        delegate.subList(fromIndex, toIndex + 1).clear();
        fireIntervalRemoved(this, fromIndex, toIndex);
    }
}