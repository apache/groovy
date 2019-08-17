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

import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.RangeInfo;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Represents a list of Integer objects starting at a specified {@code from} value up (or down)
 * to and potentially including a given {@code to} value.
 * <p>
 * Instances of this class may be either inclusive aware or non-inclusive aware. See the
 * relevant constructors for creating each type. Inclusive aware IntRange instances are
 * suitable for use with Groovy's range indexing - in particular if the from or to values
 * might be negative. This normally happens underneath the covers but is worth keeping
 * in mind if creating these ranges yourself explicitly.
 * <p>
 * Note: the design of this class might seem a little strange at first. It contains a Boolean
 * field, {@code inclusive}, which can be {@code true}, {@code false} or {@code null}. This
 * design is for backwards compatibility reasons. Groovy uses this class under the covers
 * to represent range indexing, e.g. {@code someList[x..y]} and {@code someString[x..<y]}.
 * In early versions of Groovy the ranges in these expressions were represented under the
 * covers by the {@code new IntRange(x, y)} and {@code new IntRange(x, y-1)}. This turns
 * out to be a lossy abstraction when x and/or y are negative values. Now the latter case
 * is represented by {@code new IntRange(false, x, y)}.
 * <p>
 * Note: This class is a copy of {@link ObjectRange} optimized for <code>int</code>. If you make any
 * changes to this class, you might consider making parallel changes to {@link ObjectRange}.
 */
public class IntRange extends AbstractList<Integer> implements Range<Integer>, Serializable {

    private static final long serialVersionUID = -7827097587793510780L;

    /**
     * Iterates through each number in an <code>IntRange</code>.
     */
    private class IntRangeIterator implements Iterator<Integer> {
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
        private int value = isReverse() ? getTo() : getFrom();

        @Override
        public boolean hasNext() {
            return index < size;
        }

        @Override
        public Integer next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (index++ > 0) {
                if (isReverse()) {
                    --value;
                } else {
                    ++value;
                }
            }
            return value;
        }

        /**
         * Not supported.
         *
         * @throws java.lang.UnsupportedOperationException always
         */
        @Override
        public void remove() {
            IntRange.this.remove(index);
        }
    }

    /**
     * For non-inclusive aware ranges, the first number in the range; <code>from</code> is always less than or equal to <code>to</code>.
     * For inclusive aware ranges, the <code>from</code> argument supplied to the constructor.
     */
    private final int from;

    /**
     * For non-inclusive aware ranges, the last number in the range; <code>to</code> is always greater than or equal to <code>from</code>.
     * For inclusive aware ranges, the <code>from</code> argument supplied to the constructor.
     */
    private final int to;

    /**
     * If <code>false</code>, counts up from <code>from</code> to <code>to</code>.  Otherwise, counts down
     * from <code>to</code> to <code>from</code>. Not used for inclusive-aware ranges (inclusive = true|false).
     */
    private final boolean reverse;

    /**
     * If <code>true</code> or null, <code>to</code> is included in the range.
     * If <code>false</code>, the range stops before the <code>to</code> value.
     * <p>
     * Null for non-inclusive-aware ranges (which are inclusive).
     * <p>
     * If true or false, the reverse flag is discarded.
     */
    private final Boolean inclusive;

    /**
     * Creates a new non-inclusive aware <code>IntRange</code>. If <code>from</code> is greater than
     * <code>to</code>, a reverse range is created with <code>from</code> and <code>to</code> swapped.
     *
     * @param from the first number in the range.
     * @param to   the last number in the range.
     * @throws IllegalArgumentException if the range would contain more than {@link Integer#MAX_VALUE} values.
     */
    public IntRange(int from, int to) {
        this.inclusive = null;
        if (from > to) {
            this.from = to;
            this.to = from;
            this.reverse = true;
        } else {
            this.from = from;
            this.to = to;
            this.reverse = false;
        }
        checkSize();
    }

    /**
     * Creates a new non-inclusive aware <code>IntRange</code>.
     *
     * @param from    the first value in the range.
     * @param to      the last value in the range.
     * @param reverse <code>true</code> if the range should count from
     *                <code>to</code> to <code>from</code>.
     * @throws IllegalArgumentException if <code>from</code> is greater than <code>to</code>.
     */
    protected IntRange(int from, int to, boolean reverse) {
        this.inclusive = null;
        if (from > to) {
            throw new IllegalArgumentException("'from' must be less than or equal to 'to'");
        }

        this.from = from;
        this.to = to;
        this.reverse = reverse;
        checkSize();
    }

    /**
     * Creates a new inclusive aware <code>IntRange</code>.
     *
     * @param from      the first value in the range.
     * @param to        the last value in the range.
     * @param inclusive <code>true</code> if the to value is included in the range.
     */
    public IntRange(boolean inclusive, int from, int to) {
        this.from = from;
        this.to = to;
        this.inclusive = inclusive;
        this.reverse = false; // range may still be reversed, this value is ignored for inclusive-aware ranges
        checkSize();
    }

    /**
     * Creates a new NumberRange with the same <code>from</code> and <code>to</code> as this
     * IntRange but with a step size of <code>stepSize</code>.
     *
     * @param stepSize the desired step size
     * @return a new NumberRange
     * @since 2.5.0
     */
    public <T extends Number & Comparable> NumberRange by(T stepSize) {
        return new NumberRange(NumberRange.comparableNumber((Number)from), NumberRange.comparableNumber((Number)to), stepSize, inclusive);
    }

    private void checkSize() {
        // size() in the Collection interface returns an integer, so ranges can have no more than Integer.MAX_VALUE elements
        final long size = (long) to - from + 1;
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("A range must have no more than " + Integer.MAX_VALUE + " elements but attempted " + size + " elements");
        }
    }

    /**
     * A method for determining from and to information when using this IntRange to index an aggregate object of the specified size.
     * Normally only used internally within Groovy but useful if adding range indexing support for your own aggregates.
     *
     * @param size the size of the aggregate being indexed
     * @return the calculated range information (with 1 added to the to value, ready for providing to subList
     */
    public RangeInfo subListBorders(int size) {
        if (inclusive == null) {
            throw new IllegalStateException("Should not call subListBorders on a non-inclusive aware IntRange");
        }
        int tempFrom = from;
        if (tempFrom < 0) {
            tempFrom += size;
        }
        int tempTo = to;
        if (tempTo < 0) {
            tempTo += size;
        }
        if (tempFrom > tempTo) {
            return new RangeInfo(inclusive ? tempTo : tempTo + 1, tempFrom + 1, true);
        }
        return new RangeInfo(tempFrom, inclusive ? tempTo + 1 : tempTo, false);
    }

    /**
     * Determines if this object is equal to another object. Delegates to
     * {@link AbstractList#equals(Object)} if <code>that</code> is anything
     * other than an {@link IntRange}.
     * <p>
     * It is not necessary to override <code>hashCode</code>, as
     * {@link AbstractList#hashCode()} provides a suitable hash code.<p>
     * <p>
     * Note that equals is generally handled by {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#equals(List, List)}
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
     * @param that the object to compare for equality
     * @return <code>true</code> if the ranges are equal
     */
    public boolean equals(IntRange that) {
        return that != null && ((inclusive == null && reverse == that.reverse && from == that.from && to == that.to)
                || (inclusive != null && Objects.equals(inclusive, that.inclusive) && from == that.from && to == that.to));
    }

    @Override
    public Integer getFrom() {
        if (inclusive == null || from <= to) {
            return from;
        }
        return inclusive ? to : to + 1;
    }

    @Override
    public Integer getTo() {
        if (inclusive == null) {
            return to;
        }
        if (from <= to) {
            return inclusive ? to : to - 1;
        }
        return from;
    }

    /**
     * Returns the inclusive flag. Null for non-inclusive aware ranges or non-null for inclusive aware ranges.
     */
    public Boolean getInclusive() {
        return inclusive;
    }

    /**
     * Gets the 'from' value as a primitive integer.
     *
     * @return the 'from' value as a primitive integer.
     */
    public int getFromInt() {
        return getFrom();
    }

    /**
     * Gets the 'to' value as a primitive integer.
     *
     * @return the 'to' value as a primitive integer.
     */
    public int getToInt() {
        return getTo();
    }

    @Override
    public boolean isReverse() {
        return inclusive == null ? reverse : (from > to);
    }

    @Override
    public boolean containsWithinBounds(Object o) {
        return contains(o);
    }

    @Override
    public Integer get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + " too big for range: " + this);
        }
        return isReverse() ? getTo() - index : index + getFrom();
    }

    @Override
    public int size() {
        return getTo() - getFrom() + 1;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new IntRangeIterator();
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
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
            return new EmptyRange<Integer>(getFrom());
        }

        return new IntRange(fromIndex + getFrom(), toIndex + getFrom() - 1, isReverse());
    }

    public String toString() {
        return inclusive != null ? ("" + from + ".." + (inclusive ? "" : "<") + to)
                : (reverse ? "" + to + ".." + from : "" + from + ".." + to);
    }

    @Override
    public String inspect() {
        return toString();
    }

    @Override
    public boolean contains(Object value) {
        if (value instanceof Integer) {
            return (Integer) value >= getFrom() && (Integer) value <= getTo();
        }
        if (value instanceof BigInteger) {
            final BigInteger bigint = (BigInteger) value;
            return bigint.compareTo(BigInteger.valueOf(getFrom())) >= 0 &&
                    bigint.compareTo(BigInteger.valueOf(getTo())) <= 0;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection other) {
        if (other instanceof IntRange) {
            final IntRange range = (IntRange) other;
            return getFrom() <= range.getFrom() && range.getTo() <= getTo();
        }
        return super.containsAll(other);
    }

    @Override
    public void step(int step, Closure closure) {
        if (step == 0) {
            if (!getFrom().equals(getTo())) {
                throw new GroovyRuntimeException("Infinite loop detected due to step size of 0");
            }
            return; // from == to and step == 0, nothing to do, so return
        }

        if (isReverse()) {
            step = -step;
        }
        if (step > 0) {
            int value = getFrom();
            while (value <= getTo()) {
                closure.call(value);
                if (((long) value + step) >= Integer.MAX_VALUE) {
                    break;
                }
                value = value + step;
            }
        } else {
            int value = getTo();
            while (value >= getFrom()) {
                closure.call(value);
                if (((long) value + step) <= Integer.MIN_VALUE) {
                    break;
                }
                value = value + step;
            }
        }
    }

    @Override
    public List<Integer> step(int step) {
        final IteratorClosureAdapter<Integer> adapter = new IteratorClosureAdapter<Integer>(this);
        step(step, adapter);
        return adapter.asList();
    }

    @Override
    public int hashCode(){
        int hashCode;
        final int from = this.getFrom();
        final int to = this.getTo();

        hashCode = ((from+to+1)*(from+to))/2+to;
        return hashCode;
    }
}
