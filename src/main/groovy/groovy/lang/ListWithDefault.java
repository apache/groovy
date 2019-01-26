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
package groovy.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A wrapper for {@link List} which automatically grows the list when either {@link #get(int)} or
 * {@link #getAt(int)} is called with an index greater than or equal to {@code size()}.
 *
 * @since 1.8.7
 */
public final class ListWithDefault<T> implements List<T> {

    private final List<T> delegate;
    private final boolean lazyDefaultValues;

    private final Closure initClosure;

    private ListWithDefault(List<T> items, boolean lazyDefaultValues, Closure initClosure) {
        this.delegate = items;
        this.lazyDefaultValues = lazyDefaultValues;
        this.initClosure = initClosure;
    }

    public List<T> getDelegate() {
        return delegate != null ? new ArrayList<>(delegate) : null;
    }

    public boolean isLazyDefaultValues() {
        return lazyDefaultValues;
    }

    public Closure getInitClosure() {
        return initClosure != null ? (Closure) initClosure.clone() : null;
    }

    public static <T> ListWithDefault<T> newInstance(List<T> items, boolean lazyDefaultValues, Closure initClosure) {
        if (items == null)
            throw new IllegalArgumentException("Parameter \"items\" must not be null");
        if (initClosure == null)
            throw new IllegalArgumentException("Parameter \"initClosure\" must not be null");

        return new ListWithDefault<>(new ArrayList<>(items), lazyDefaultValues, (Closure) initClosure.clone());
    }

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    public Object[] toArray() {
        return delegate.toArray();
    }

    public <T> T[] toArray(T[] ts) {
        return delegate.toArray(ts);
    }

    public boolean add(T t) {
        return delegate.add(t);
    }

    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    public boolean containsAll(Collection<?> objects) {
        return delegate.containsAll(objects);
    }

    public boolean addAll(Collection<? extends T> ts) {
        return delegate.addAll(ts);
    }

    public boolean addAll(int i, Collection<? extends T> ts) {
        return delegate.addAll(i, ts);
    }

    public boolean removeAll(Collection<?> objects) {
        return delegate.removeAll(objects);
    }

    public boolean retainAll(Collection<?> objects) {
        return delegate.retainAll(objects);
    }

    public void clear() {
        delegate.clear();
    }

    /**
     * Overwrites subscript operator handling by redirecting to {@link #get(int)}.
     *
     * @param index an index (might be greater or equal to {@code size()}, or smaller than 0)
     * @return the value at the given {@code index} or the default value
     */
    public T getAt(int index) {
        return get(index);
    }

    /**
     * Returns the element at the given index but grows the list if needed. If the requested {@code index} is
     * greater than or equal to {@code size()}, the list will grow to the new size and a default value calculated
     * using the <code>initClosure</code> will be used to populate the missing value and returned.
     * <p>
     * If <code>lazyDefaultValues</code> is <code>true</code> any gaps when growing the list are filled
     * with nulls. Subsequent attempts to retrieve items from the list from those gap index values
     * will, upon finding null, call the <code>initClosure</code> to populate the list for the
     * given list value. Hence, when in this mode, nulls cannot be stored in this list.
     * If <code>lazyDefaultValues</code> is <code>false</code> any gaps when growing the list are filled
     * eagerly by calling the <code>initClosure</code> for all gap indexes during list growth.
     * No calls to <code>initClosure</code> are made except during list growth and it is ok to
     * store null values in the list when in this mode.
     * <p>
     * This implementation breaks
     * the contract of {@link java.util.List#get(int)} as it a) possibly modifies the underlying list and b) does
     * NOT throw an {@link IndexOutOfBoundsException} when {@code index < 0 || index >= size()}.
     *
     * @param index an index (might be greater or equal to {@code size()}, or smaller than 0)
     * @return the value at the given {@code index} or the default value
     */
    public T get(int index) {

        final int size = size();
        int normalisedIndex = normaliseIndex(index, size);
        if (normalisedIndex < 0) {
            throw new IndexOutOfBoundsException("Negative index [" + normalisedIndex + "] too large for list size " + size);
        }

        // either index >= size or the normalised index is negative
        if (normalisedIndex >= size) {
            // find out the number of gaps to fill with null/the default value
            final int gapCount = normalisedIndex - size;

            // fill all gaps
            for (int i = 0; i < gapCount; i++) {
                final int idx = size();

                // if we lazily create default values, use 'null' as placeholder
                if (lazyDefaultValues)
                    delegate.add(idx, null);
                else
                    delegate.add(idx, getDefaultValue(idx));
            }

            // add the first/last element being always the default value
            final int idx = normalisedIndex;
            delegate.add(idx, getDefaultValue(idx));

            // normalise index again to get positive index
            normalisedIndex = normaliseIndex(index, size());
        }

        T item = delegate.get(normalisedIndex);
        if (item == null && lazyDefaultValues) {
            item = getDefaultValue(normalisedIndex);
            delegate.set(normalisedIndex, item);
        }

        return item;
    }

    @SuppressWarnings("unchecked")
    private T getDefaultValue(int idx) {
        return (T) initClosure.call(new Object[]{idx});
    }

    private static int normaliseIndex(int index, int size) {
        if (index < 0) {
            index += size;
        }
        return index;
    }

    public T set(int i, T t) {
        return delegate.set(i, t);
    }

    public void add(int i, T t) {
        delegate.add(i, t);
    }

    public T remove(int i) {
        return delegate.remove(i);
    }

    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    public ListIterator<T> listIterator(int i) {
        return delegate.listIterator(i);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Returns a view of a portion of this list. This method returns a list with the same
     * lazy list settings as the original list.
     *
     * @param fromIndex low endpoint of the subList (inclusive)
     * @param toIndex   upper endpoint of the subList (exclusive)
     * @return a view of a specified range within this list, keeping all lazy list settings
     */
    public ListWithDefault<T> subList(int fromIndex, int toIndex) {
        return new ListWithDefault<>(delegate.subList(fromIndex, toIndex), lazyDefaultValues, (Closure) initClosure.clone());
    }
}
