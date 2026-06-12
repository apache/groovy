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
@SuppressWarnings("rawtypes")
public class ListWrapperListModel<E> extends AbstractListModel {
    private final List<E> delegate;

    /**
     * Creates a list model backed by the supplied live list.
     *
     * @param delegateList the backing list
     */
    public ListWrapperListModel(List<E> delegateList) {
        this.delegate = delegateList;
    }

    /**
     * Returns the live backing list.
     *
     * @return the delegate list
     */
    public List<E> getDelegateList() {
        return delegate;
    }

    /**
     * Returns the number of elements currently exposed by the backing list.
     *
     * @return the current model size
     */
    @Override
    public int getSize() {
        return delegate.size();
    }

    /**
     * Returns the element at the supplied index from the backing list.
     *
     * @param i the requested element index
     * @return the element at {@code i}
     */
    @Override
    public Object getElementAt(int i) {
        return delegate.get(i);
    }

    /**
     * Replaces the element at the supplied index and fires a content-changed event.
     *
     * @param i the element index
     * @param e the replacement element
     * @return the previous element
     */
    public E set(int i, E e) {
        E element = delegate.set(i, e);
        fireContentsChanged(this, i, i);
        return element;
    }

    /**
     * Removes every element from the backing list and notifies listeners.
     */
    public void clear() {
        int i = delegate.size() - 1;
        delegate.clear();
        if (i >= 0) {
            fireIntervalRemoved(this, 0, i);
        }
    }

    /**
     * Returns the last index of the supplied element in the backing list.
     *
     * @param o the element to search for
     * @return the last matching index, or {@code -1}
     */
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    /**
     * Returns whether the backing list contains the supplied element.
     *
     * @param o the element to search for
     * @return {@code true} when the element is present
     */
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    /**
     * Returns a list iterator over the backing list.
     *
     * @return a list iterator over the current contents
     */
    public ListIterator<E> listIterator() {
        return delegate.listIterator();
    }

    /**
     * Returns whether the backing list is empty.
     *
     * @return {@code true} when the model contains no elements
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * Returns the first index of the supplied element in the backing list.
     *
     * @param o the element to search for
     * @return the first matching index, or {@code -1}
     */
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    /**
     * Inserts an element at the supplied index and notifies listeners.
     *
     * @param i the insertion index
     * @param e the element to insert
     */
    public void add(int i, E e) {
        int index = delegate.size();
        delegate.add(i, e);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Returns an iterator over the backing list.
     *
     * @return an iterator over the current contents
     */
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    /**
     * Appends every element from the supplied collection and notifies listeners when anything was added.
     *
     * @param es the elements to append
     * @return {@code true} when the backing list changed
     */
    public boolean addAll(Collection<? extends E> es) {
        int i = delegate.size();
        boolean added = delegate.addAll(es);
        if (added) fireIntervalAdded(this, i, i + es.size());
        return added;
    }

    /**
     * Removes the element at the supplied index and notifies listeners.
     *
     * @param i the element index
     * @return the removed element
     */
    public E remove(int i) {
        E element = delegate.remove(i);
        fireIntervalRemoved(this, i, i);
        return element;
    }

    /**
     * Inserts every element from the supplied collection at the supplied index.
     *
     * @param i the insertion index
     * @param es the elements to insert
     * @return {@code true} when the backing list changed
     */
    public boolean addAll(int i, Collection<? extends E> es) {
        boolean added = delegate.addAll(i, es);
        if (added) fireIntervalAdded(this, i, i + es.size());
        return added;
    }

    /**
     * Returns a list iterator that starts at the supplied index.
     *
     * @param i the starting index
     * @return a list iterator positioned at the supplied index
     */
    public ListIterator<E> listIterator(int i) {
        return delegate.listIterator(i);
    }

    /**
     * Returns whether the backing list contains every supplied element.
     *
     * @param objects the elements to test
     * @return {@code true} when every element is present
     */
    public boolean containsAll(Collection<?> objects) {
        return delegate.containsAll(objects);
    }

    /**
     * Removes the supplied element from the backing list and notifies listeners when it was present.
     *
     * @param o the element to remove
     * @return {@code true} when the backing list changed
     */
    public boolean remove(Object o) {
        int i = indexOf(o);
        boolean rv = delegate.remove(o);
        if (i >= 0) {
            fireIntervalRemoved(this, i, i);
        }
        return rv;
    }

    /**
     * Appends the supplied element to the backing list and notifies listeners.
     *
     * @param e the element to append
     * @return {@code true} when the backing list changed
     */
    public boolean add(E e) {
        int i = delegate.size();
        boolean added = delegate.add(e);
        if (added) fireIntervalAdded(this, i, i);
        return added;
    }

    /**
     * Returns the element at the supplied index.
     *
     * @param i the element index
     * @return the element at that index
     */
    public E get(int i) {
        return delegate.get(i);
    }

    /**
     * Copies the backing list into the supplied destination array.
     *
     * @param ts the destination array
     * @param <T> the array component type
     * @return the populated array
     */
    public <T> T[] toArray(T[] ts) {
        return delegate.toArray(ts);
    }

    /**
     * Copies the backing list into a new object array.
     *
     * @return an array containing the current contents
     */
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * Removes the inclusive index range from the backing list and notifies listeners.
     *
     * @param fromIndex the first index to remove
     * @param toIndex the last index to remove
     */
    public void removeRange(int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex must be <= toIndex");
        }
        delegate.subList(fromIndex, toIndex + 1).clear();
        fireIntervalRemoved(this, fromIndex, toIndex);
    }
}
