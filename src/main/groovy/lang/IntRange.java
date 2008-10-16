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

import org.codehaus.groovy.runtime.IteratorClosureAdapter;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of Integer objects from a specified int up (or down) to and including
 * a given to.<p>
 * <p/>
 * This class is a copy of {@link ObjectRange} optimized for <code>int</code>.  If you make any
 * changes to this class, you might consider making parallel changes to {@link ObjectRange}.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IntRange extends AbstractList implements Range {

    /**
     * Iterates through each number in an <code>IntRange</code>.
     */
    private class IntRangeIterator implements Iterator {
        /**
         * Counts from 0 up to size - 1.
         */
        private int index;

        /**
         * The number of values in the range.
         */
        private int size = size();

        /**
         * The next value to return.
         */
        private int value = reverse ? to : from;

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return index < size;
        }

        /**
         * {@inheritDoc}
         */
        public Object next() {
            if (index++ > 0) {
                if (index > size) {
                    return null;
                } else {
                    if (reverse) {
                        --value;
                    } else {
                        ++value;
                    }
                }
            }
            return Integer.valueOf(value);
        }

        /**
         * Not supported.
         *
         * @throws java.lang.UnsupportedOperationException always
         */
        public void remove() {
            IntRange.this.remove(index);
        }
    }

    /**
     * The first number in the range.  <code>from </code> is always less than or equal to <code>to</code>.
     */
    private int from;

    /**
     * The last number in the range. <code>to</code> is always greater than or eqaul to <code>from</code>.
     */
    private int to;

    /**
     * If <code>false</code>, counts up from <code>from</code> to <code>to</code>.  Otherwise, counts down
     * from <code>to</code> to <code>from</code>.
     */
    private boolean reverse;

    /**
     * Creates a new <code>IntRange</code>. If <code>from</code> is greater
     * than <code>to</code>, a reverse range is created with
     * <code>from</code> and <code>to</code> swapped.
     *
     * @param from the first number in the range.
     * @param to   the last number in the range.
     * @throws IllegalArgumentException if the range would contain more than
     *                                  {@link Integer#MAX_VALUE} values.
     */
    public IntRange(int from, int to) {
        if (from > to) {
            this.from = to;
            this.to = from;
            this.reverse = true;
        } else {
            this.from = from;
            this.to = to;
        }

        // size() an integer so ranges can have no more than Integer.MAX_VALUE elements
        if (this.to - this.from >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("range must have no more than " + Integer.MAX_VALUE + " elements");
        }
    }

    /**
     * Creates a new <code>IntRange</code>.
     *
     * @param from    the first value in the range.
     * @param to      the last value in the range.
     * @param reverse <code>true</code> if the range should count from
     *                <code>to</code> to <code>from</code>.
     * @throws IllegalArgumentException if <code>from</code> is greater than <code>to</code>.
     */
    protected IntRange(int from, int to, boolean reverse) {
        if (from > to) {
            throw new IllegalArgumentException("'from' must be less than or equal to 'to'");
        }

        this.from = from;
        this.to = to;
        this.reverse = reverse;
    }

    /**
     * Determines if this object is equal to another object. Delegates to
     * {@link AbstractList#equals(Object)} if <code>that</code> is anthing
     * other than an {@link IntRange}.
     * <p/>
     * <p/>
     * It is not necessary to override <code>hashCode</code>, as
     * {@link AbstractList#hashCode()} provides a suitable hash code.<p>
     * <p/>
     * Note that equals is generally handled by {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#equals(List,List)}
     * instead of this method.
     *
     * @param that the object to compare
     * @return <code>true</code> if the objects are equal
     */
    public boolean equals(Object that) {
        return that instanceof IntRange ? equals((IntRange) that) : super.equals(that);
    }

    /**
     * Compares an {@link IntRange} to another {@link IntRange}.
     *
     * @return <code>true</code> if the ranges are equal
     * @param that the object to compare for equality
     */
    public boolean equals(IntRange that) {
        return that != null && this.reverse == that.reverse && this.from == that.from && this.to == that.to;
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getFrom() {
        return Integer.valueOf(from);
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getTo() {
        return Integer.valueOf(to);
    }

    /**
     * Gets the 'from' value as an integer.
     *
     * @return the 'from' value as an integer.
     */
    public int getFromInt() {
        return from;
    }

    /**
     * Gets the 'to' value as an integer.
     *
     * @return the 'to' value as an integer.
     */
    public int getToInt() {
        return to;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReverse() {
        return reverse;
    }

    public boolean containsWithinBounds(Object o) {
        return contains(o);
    }

    /**
     * {@inheritDoc}
     */
    public Object get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + " too big for range: " + this);
        }
        int value = reverse ? to - index : index + from;
        return Integer.valueOf(value);
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return to - from + 1;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator iterator() {
        return new IntRangeIterator();
    }

    /**
     * {@inheritDoc}
     */
    public List subList(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size()) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }

        if (fromIndex == toIndex) {
            return new EmptyRange(Integer.valueOf(from));
        }

        return new IntRange(fromIndex + this.from, toIndex + this.from - 1, reverse);
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return reverse ? "" + to + ".." + from : "" + from + ".." + to;
    }

    /**
     * {@inheritDoc}
     */
    public String inspect() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(Object value) {
        if (value instanceof Integer) {
            Integer integer = (Integer) value;
            int i = integer.intValue();
            return i >= from && i <= to;
        }
        if (value instanceof BigInteger) {
            BigInteger bigint = (BigInteger) value;
            return bigint.compareTo(BigInteger.valueOf(from)) >= 0 &&
                    bigint.compareTo(BigInteger.valueOf(to)) <= 0;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsAll(Collection other) {
        if (other instanceof IntRange) {
            final IntRange range = (IntRange) other;
            return this.from <= range.from && range.to <= this.to;
        }
        return super.containsAll(other);
    }

    /**
     * {@inheritDoc}
     */
    public void step(int step, Closure closure) {
        if (reverse) {
            step = -step;
        }
        if (step >= 0) {
            int value = from;
            while (value <= to) {
                closure.call(Integer.valueOf(value));
                value = value + step;
            }
        } else {
            int value = to;
            while (value >= from) {
                closure.call(Integer.valueOf(value));
                value = value + step;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List step(int step) {
        IteratorClosureAdapter adapter = new IteratorClosureAdapter(this);
        step(step, adapter);
        return adapter.asList();
    }
}
