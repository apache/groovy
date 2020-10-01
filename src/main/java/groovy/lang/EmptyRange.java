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

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Constructing Ranges like 0..&lt;0
 */
public class EmptyRange<T extends Comparable> extends AbstractList<T> implements Range<T> {

    /**
     * The value at which the range originates (may be <code>null</code>).
     */
    protected T at;

    /**
     * Creates a new {@link EmptyRange}.
     *
     * @param at the value at which the range starts (may be <code>null</code>).
     */
    public EmptyRange(T at) {
        this.at = at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getFrom() {
        return at;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T getTo() {
        return at;
    }

    /**
     * Never true for an empty range.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean isReverse() {
        return false;
    }

    /**
     * Never true for an empty range.
     *
     * @return <code>false</code>
     */
    @Override
    public boolean containsWithinBounds(Object o) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String inspect() {
        return InvokerHelper.inspect(at) + "..<" + InvokerHelper.inspect(at);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return (null == at)
                ? "null..<null"
                : at + "..<" + at;
    }

    /**
     * Always 0 for an empty range.
     *
     * @return 0
     */
    @Override
    public int size() {
        return 0;
    }

    /**
     * Always throws <code>IndexOutOfBoundsException</code> for an empty range.
     *
     * @throws IndexOutOfBoundsException always
     */
    @Override
    public T get(int index) {
        throw new IndexOutOfBoundsException("can't get values from Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException always
     */
    @Override
    public boolean add(T o) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("cannot retainAll in Empty Ranges");
    }

    /**
     * Always throws <code>UnsupportedOperationException</code> for an empty range.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException("cannot set in Empty Ranges");
    }

    /**
     * Always does nothing for an empty range.
     */
    @Override
    public void step(int step, Closure closure) {
    }

    /**
     * Always returns an empty list for an empty range.
     */
    @Override
    public List<T> step(int step) {
        return new ArrayList<T>();
    }
}
