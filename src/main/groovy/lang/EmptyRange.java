/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.*;

/**
 * Constructing Ranges like 0..<0
 * @author Dierk Koenig
 * @author Edwin Tellman
 */
public class EmptyRange extends AbstractList implements Range {
    
    /**
     * The value at which the range originates (may be <code>null</code>).
     */
    protected Comparable at;

    /**
     * Creates a new {@link EmptyRange}.
     * 
     * @param at the value at which the range starts (may be <code>null</code>).
     */
    public EmptyRange(Comparable at) {
       this.at = at;
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getFrom() {
        return at;
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getTo() {
        return at;
    }

    /**
     * Returns <code>false</code>
     * 
     * @return <code>false</code>
     */
    public boolean isReverse() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsWithinBounds(Object o) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String inspect() {
        return InvokerHelper.inspect(at) + "..<" + InvokerHelper.inspect(at);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return (null == at) 
            ? "null..<null"
            : at.toString() + "..<" + at.toString();
    }

    /**
     * Returns 0
     * 
     * @return 0
     */
    public int size() {
        return 0;
    }

    /**
     * Throws <code>IndexOutOfBoundsException</code>.
     * 
     * @throws IndexOutOfBoundsException always
     */
    public Object get(int index) {
        throw new IndexOutOfBoundsException("can't get values from Empty Ranges");
    }

    /**
     * @throws UnsupportedOperationException always
     */
    public boolean add(Object o) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }

     /**
     * @throws UnsupportedOperationException
     */
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException("cannot add to Empty Ranges");
    }
    
    /** 
     * @throws UnsupportedOperationException
     */
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /** 
     * @throws UnsupportedOperationException
     */
    public Object remove(int index) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException("cannot remove from Empty Ranges");
    }

    /**
     * @throws UnsupportedOperationException
     */
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException("cannot retainAll in Empty Ranges");
    }

     /**
     * @throws UnsupportedOperationException
     */
    public Object set(int index, Object element) {
        throw new UnsupportedOperationException("cannot set in Empty Ranges");
    }

    /**
     * {@inheritDoc}
     */
    public void step(int step, Closure closure) {
    }

    /**
     * {@inheritDoc}
     */
    public List step(int step) {
        return new ArrayList();
    }
}
