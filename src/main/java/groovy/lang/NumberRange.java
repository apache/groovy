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
import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.RangeInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareGreaterThan;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareGreaterThanEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareLessThan;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareLessThanEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareNotEqual;
import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.compareTo;
import static org.codehaus.groovy.runtime.dgmimpl.NumberNumberMinus.minus;
import static org.codehaus.groovy.runtime.dgmimpl.NumberNumberMultiply.multiply;
import static org.codehaus.groovy.runtime.dgmimpl.NumberNumberPlus.plus;

/**
 * Represents an immutable list of Numbers from a value to a value with a particular step size.
 *
 * In general, it isn't recommended using a NumberRange as a key to a map. The range
 * 0..3 is deemed to be equal to 0.0..3.0 but they have different hashCode values,
 * so storing a value using one of these ranges couldn't be retrieved using the other.
 *
 * @since 2.5.0
 */
public class NumberRange extends AbstractList<Comparable> implements Range<Comparable>, Serializable {

    private static final long serialVersionUID = 5107424833653948484L;
    /**
     * The first value in the range.
     */
    private final Comparable from;

    /**
     * The last value in the range.
     */
    private final Comparable to;

    /**
     * The step size in the range.
     */
    private final Number stepSize;

    /**
     * The cached size, or -1 if not yet computed
     */
    private int size = -1;

    /**
     * The cached hashCode (once calculated)
     */
    private Integer hashCodeCache = null;

    /**
     * <code>true</code> if the range counts backwards from <code>to</code> to <code>from</code>.
     */
    private final boolean reverse;

    /**
     * <code>true</code> if the range includes the upper bound.
     */
    private final boolean inclusive;

    /**
     * Creates an inclusive {@link NumberRange} with step size 1.
     * Creates a reversed range if <code>from</code> &lt; <code>to</code>.
     *
     * @param from the first value in the range
     * @param to   the last value in the range
     */
    public <T extends Number & Comparable, U extends Number & Comparable>
    NumberRange(T from, U to) {
        this(from, to, null, true);
    }

    /**
     * Creates a new {@link NumberRange} with step size 1.
     * Creates a reversed range if <code>from</code> &lt; <code>to</code>.
     *
     * @param from start of the range
     * @param to   end of the range
     * @param inclusive whether the range is inclusive
     */
    public <T extends Number & Comparable, U extends Number & Comparable>
    NumberRange(T from, U to, boolean inclusive) {
        this(from, to, null, inclusive);
    }

    /**
     * Creates an inclusive {@link NumberRange}.
     * Creates a reversed range if <code>from</code> &lt; <code>to</code>.
     *
     * @param from start of the range
     * @param to   end of the range
     * @param stepSize the gap between discrete elements in the range
     */
    public <T extends Number & Comparable, U extends Number & Comparable, V extends
            Number & Comparable<? super Number>>
    NumberRange(T from, U to, V stepSize) {
        this(from, to, stepSize, true);
    }

    /**
     * Creates a {@link NumberRange}.
     * Creates a reversed range if <code>from</code> &lt; <code>to</code>.
     *
     * @param from start of the range
     * @param to   end of the range
     * @param stepSize the gap between discrete elements in the range
     * @param inclusive whether the range is inclusive
     */
    public <T extends Number & Comparable, U extends Number & Comparable, V extends
            Number & Comparable>
    NumberRange(T from, U to, V stepSize, boolean inclusive) {
        if (from == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'from' index in a Range");
        }
        if (to == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'to' index in a Range");
        }
        reverse = areReversed(from, to);
        Number tempFrom;
        Number tempTo;
        if (reverse) {
            tempFrom = to;
            tempTo = from;
        } else {
            tempFrom = from;
            tempTo = to;
        }
        if (tempFrom instanceof Short) {
            tempFrom = tempFrom.intValue();
        } else if (tempFrom instanceof Float) {
            tempFrom = tempFrom.doubleValue();
        }
        if (tempTo instanceof Short) {
            tempTo = tempTo.intValue();
        } else if (tempTo instanceof Float) {
            tempTo = tempTo.doubleValue();
        }

        if (tempFrom instanceof Integer && tempTo instanceof Long) {
            tempFrom = tempFrom.longValue();
        } else if (tempTo instanceof Integer && tempFrom instanceof Long) {
            tempTo = tempTo.longValue();
        }

        this.from = (Comparable) tempFrom;
        this.to = (Comparable) tempTo;
        this.stepSize = stepSize == null ? 1 : stepSize;
        this.inclusive = inclusive;
    }

    /**
     * A method for determining from and to information when using this IntRange to index an aggregate object of the specified size.
     * Normally only used internally within Groovy but useful if adding range indexing support for your own aggregates.
     *
     * @param size the size of the aggregate being indexed
     * @return the calculated range information (with 1 added to the to value, ready for providing to subList
     */
    public RangeInfo subListBorders(int size) {
        if (stepSize.intValue() != 1) {
            throw new IllegalStateException("Step must be 1 when used by subList!");
        }
        return IntRange.subListBorders(((Number) from).intValue(), ((Number) to).intValue(), inclusive, size);
    }

    /**
     * For a NumberRange with step size 1, creates a new NumberRange with the same
     * <code>from</code> and <code>to</code> as this NumberRange
     * but with a step size of <code>stepSize</code>.
     *
     * @param stepSize the desired step size
     * @return a new NumberRange
     */
    public <T extends Number & Comparable> NumberRange by(T stepSize) {
        if (!Integer.valueOf(1).equals(this.stepSize)) {
            throw new IllegalStateException("by only allowed on ranges with original stepSize = 1 but found " + this.stepSize);
        }
        return new NumberRange(comparableNumber(from), comparableNumber(to), stepSize, inclusive);
    }

    @SuppressWarnings("unchecked")
    /* package private */ static <T extends Number & Comparable> T comparableNumber(Comparable c) {
        return (T) c;
    }

    @SuppressWarnings("unchecked")
    /* package private */ static <T extends Number & Comparable> T comparableNumber(Number n) {
        return (T) n;
    }

    private static boolean areReversed(Number from, Number to) {
        try {
            return compareGreaterThan(from, to);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Unable to create range due to incompatible types: " + from.getClass().getSimpleName() + ".." + to.getClass().getSimpleName() + " (possible missing brackets around range?)", cce);
        }
    }

    /**
     * An object is deemed equal to this NumberRange if it represents a List of items and
     * those items equal the list of discrete items represented by this NumberRange.
     *
     * @param that the object to be compared for equality with this NumberRange
     * @return {@code true} if the specified object is equal to this NumberRange
     * @see #fastEquals(NumberRange)
     */
    @Override
    public boolean equals(Object that) {
        return super.equals(that);
    }

    /**
     * A NumberRange's hashCode is based on hashCode values of the discrete items it represents.
     *
     * @return the hashCode value
     */
    @Override
    public int hashCode() {
        if (hashCodeCache == null) {
            hashCodeCache = super.hashCode();
        }
        return hashCodeCache;
    }

    /*
     * NOTE: as per the class javadoc, this class doesn't obey the normal equals/hashCode contract.
     * The following field and method could assist some scenarios which required a similar sort of contract
     * (but between equals and the custom canonicalHashCode). Currently commented out since we haven't
     * found a real need. We will likely remove this commented out code if no usage is identified soon.
     */

    /*
     * The cached canonical hashCode (once calculated)
     */
//    private Integer canonicalHashCodeCache = null;

    /*
     * A NumberRange's canonicalHashCode is based on hashCode values of the discrete items it represents.
     * When two NumberRange's are equal they will have the same canonicalHashCode value.
     * Numerical values which Groovy deems equal have the same hashCode during this calculation.
     * So currently (0..3).equals(0.0..3.0) yet they have different hashCode values. This breaks
     * the normal equals/hashCode contract which is a weakness in Groovy's '==' operator. However
     * the contract isn't broken between equals and canonicalHashCode.
     *
     * @return the hashCode value
     */
//    public int canonicalHashCode() {
//        if (canonicalHashCodeCache == null) {
//            int hashCode = 1;
//            for (Comparable e : this) {
//                int value;
//                if (e == null) {
//                    value = 0;
//                } else {
//                    BigDecimal next = new BigDecimal(e.toString());
//                    if (next.compareTo(BigDecimal.ZERO) == 0) {
//                        // workaround on pre-Java8 for http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6480539
//                        value = BigDecimal.ZERO.hashCode();
//                    } else {
//                        value = next.stripTrailingZeros().hashCode();
//                    }
//                }
//                hashCode = 31 * hashCode + value;
//            }
//            canonicalHashCodeCache = hashCode;
//        }
//        return canonicalHashCodeCache;
//    }

    /**
     * Compares a {@link NumberRange} to another {@link NumberRange} using only a strict comparison
     * of the NumberRange properties. This won't return true for some ranges which represent the same
     * discrete items, use equals instead for that but will be much faster for large lists.
     *
     * @param that the NumberRange to check equality with
     * @return <code>true</code> if the ranges are equal
     */
    public boolean fastEquals(NumberRange that) {
        return that != null
                && reverse == that.reverse
                && inclusive == that.inclusive
                && compareEqual(from, that.from)
                && compareEqual(to, that.to)
                && compareEqual(stepSize, that.stepSize);
    }

    /*
     * NOTE: as per the class javadoc, this class doesn't obey the normal equals/hashCode contract.
     * The following field and method could assist some scenarios which required a similar sort of contract
     * (but between fastEquals and the custom fastHashCode). Currently commented out since we haven't
     * found a real need. We will likely remove this commented out code if no usage is identified soon.
     */

    /*
     * The cached fast hashCode (once calculated)
     */
//    private Integer fastHashCodeCache = null;

    /*
     * A hashCode function that pairs with fastEquals, following the normal equals/hashCode contract.
     *
     * @return the calculated hash code
     */
//    public int fastHashCode() {
//        if (fastHashCodeCache == null) {
//            int result = 17;
//            result = result * 31 + (reverse ? 1 : 0);
//            result = result * 31 + (inclusive ? 1 : 0);
//            result = result * 31 + new BigDecimal(from.toString()).stripTrailingZeros().hashCode();
//            result = result * 31 + new BigDecimal(to.toString()).stripTrailingZeros().hashCode();
//            result = result * 31 + new BigDecimal(stepSize.toString()).stripTrailingZeros().hashCode();
//            fastHashCodeCache = result;
//        }
//        return fastHashCodeCache;
//    }

    @Override
    public Comparable getFrom() {
        return from;
    }

    @Override
    public Comparable getTo() {
        return to;
    }

    public Comparable getStepSize() {
        return (Comparable) stepSize;
    }

    @Override
    public boolean isReverse() {
        return reverse;
    }

    @Override
    public Comparable get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        final Iterator<Comparable> iter = new StepIterator(this, stepSize);

        Comparable value = iter.next();
        for (int i = 0; i < index; i++) {
            if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + index + " is too big for range: " + this);
            }
            value = iter.next();
        }
        return value;
    }

    /**
     * Checks whether a value is between the from and to values of a Range
     *
     * @param value the value of interest
     * @return true if the value is within the bounds
     */
    @Override
    public boolean containsWithinBounds(Object value) {
        final int result = compareTo(from, value);
        return result == 0 || result < 0 && compareTo(to, value) >= 0;
    }

    /**
     * protection against calls from Groovy
     */
    @SuppressWarnings("unused")
    private void setSize(int size) {
        throw new UnsupportedOperationException("size must not be changed");
    }

    @Override
    public int size() {
        if (size == -1) {
            calcSize(from, to, stepSize);
        }
        return size;
    }

    void calcSize(Comparable from, Comparable to, Number stepSize) {
        int tempsize = 0;
        boolean shortcut = false;
        if (isIntegral(stepSize)) {
            if ((from instanceof Integer || from instanceof Long)
                    && (to instanceof Integer || to instanceof Long)) {
                // let's fast calculate the size
                final BigInteger fromNum = new BigInteger(from.toString());
                final BigInteger toTemp = new BigInteger(to.toString());
                final BigInteger toNum = inclusive ? toTemp : toTemp.subtract(BigInteger.ONE);
                final BigInteger sizeNum = new BigDecimal(toNum.subtract(fromNum)).divide(new BigDecimal(stepSize.longValue()), BigDecimal.ROUND_DOWN).toBigInteger().add(BigInteger.ONE);
                tempsize = sizeNum.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0 ? sizeNum.intValue() : Integer.MAX_VALUE;
                shortcut = true;
            } else if (((from instanceof BigDecimal || from instanceof BigInteger) && to instanceof Number) ||
                    ((to instanceof BigDecimal || to instanceof BigInteger) && from instanceof Number)) {
                // let's fast calculate the size
                final BigDecimal fromNum = new BigDecimal(from.toString());
                final BigDecimal toTemp = new BigDecimal(to.toString());
                final BigDecimal toNum = inclusive ? toTemp : toTemp.subtract(new BigDecimal("1.0"));
                final BigInteger sizeNum = toNum.subtract(fromNum).divide(new BigDecimal(stepSize.longValue()), BigDecimal.ROUND_DOWN).toBigInteger().add(BigInteger.ONE);
                tempsize = sizeNum.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0 ? sizeNum.intValue() : Integer.MAX_VALUE;
                shortcut = true;
            }
        }
        if (!shortcut) {
            // let's brute-force calculate the size by iterating start to end
            final Iterator iter = new StepIterator(this, stepSize);
            while (iter.hasNext()) {
                tempsize++;
                // integer overflow
                if (tempsize < 0) {
                    break;
                }
                iter.next();
            }
            // integer overflow
            if (tempsize < 0) {
                tempsize = Integer.MAX_VALUE;
            }
        }
        size = tempsize;
    }

    private boolean isIntegral(Number stepSize) {
        BigDecimal tempStepSize = new BigDecimal(stepSize.toString());
        return tempStepSize.equals(new BigDecimal(tempStepSize.toBigInteger()));
    }

    @Override
    public List<Comparable> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex == toIndex) {
            return new EmptyRange<Comparable>(from);
        }

        // Performance detail:
        // not using get(fromIndex), get(toIndex) in the following to avoid stepping over elements twice
        final Iterator<Comparable> iter = new StepIterator(this, stepSize);

        Comparable value = iter.next();
        int i = 0;
        for (; i < fromIndex; i++) {
            if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + i + " is too big for range: " + this);
            }
            value = iter.next();
        }
        final Comparable fromValue = value;
        for (; i < toIndex - 1; i++) {
            if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + i + " is too big for range: " + this);
            }
            value = iter.next();
        }
        final Comparable toValue = value;

        return new NumberRange(comparableNumber(fromValue), comparableNumber(toValue), comparableNumber(stepSize), true);
    }

    @Override
    public String toString() {
        return getToString(to.toString(), from.toString());
    }

    @Override
    public String inspect() {
        return getToString(InvokerHelper.inspect(to), InvokerHelper.inspect(from));
    }

    private String getToString(String toText, String fromText) {
        String sep = inclusive ? ".." : "..<";
        String base = reverse ? "" + toText + sep + fromText : "" + fromText + sep + toText;
        return Integer.valueOf(1).equals(stepSize) ? base : base + ".by(" + stepSize + ")";
    }

    /**
     * iterates over all values and returns true if one value matches.
     * Also see containsWithinBounds.
     */
    @Override
    public boolean contains(Object value) {
        if (value == null) {
            return false;
        }
        final Iterator it = new StepIterator(this, stepSize);
        while (it.hasNext()) {
            if (compareEqual(value, it.next())) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void step(int numSteps, Closure closure) {
        if (numSteps == 0 && compareTo(from, to) == 0) {
            return; // from == to and step == 0, nothing to do, so return
        }
        final StepIterator iter = new StepIterator(this, multiply(numSteps, stepSize));
        while (iter.hasNext()) {
            closure.call(iter.next());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Comparable> iterator() {
        return new StepIterator(this, stepSize);
    }

    /**
     * convenience class to serve in other methods.
     * It's not thread-safe, and lazily produces the next element only on calls of hasNext() or next()
     */
    private class StepIterator implements Iterator<Comparable> {
        private final NumberRange range;
        private final Number step;
        private final boolean isAscending;

        private boolean isNextFetched = false;
        private Comparable next = null;

        StepIterator(NumberRange range, Number step) {
            if (compareEqual(step, 0) && compareNotEqual(range.getFrom(), range.getTo())) {
                throw new GroovyRuntimeException("Infinite loop detected due to step size of 0");
            }

            this.range = range;
            if (compareLessThan(step, 0)) {
                this.step = multiply(step, -1);
                isAscending = range.isReverse();
            } else {
                this.step = step;
                isAscending = !range.isReverse();
            }
        }

        @Override
        public boolean hasNext() {
            fetchNextIfNeeded();
            return (next != null) && (isAscending
                    ? (range.inclusive ? compareLessThanEqual(next, range.getTo()) : compareLessThan(next, range.getTo()))
                    : (range.inclusive ? compareGreaterThanEqual(next, range.getFrom()) : compareGreaterThan(next, range.getFrom())));
        }

        @Override
        public Comparable next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            fetchNextIfNeeded();
            isNextFetched = false;
            return next;
        }

        private void fetchNextIfNeeded() {
            if (!isNextFetched) {
                isNextFetched = true;

                if (next == null) {
                    // make the first fetch lazy too
                    next = isAscending ? range.getFrom() : range.getTo();
                } else {
                    next = isAscending ? increment(next, step) : decrement(next, step);
                }
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Comparable> step(int numSteps) {
        final IteratorClosureAdapter<Comparable> adapter = new IteratorClosureAdapter<Comparable>(this);
        step(numSteps, adapter);
        return adapter.asList();
    }

    /**
     * Increments by given step
     *
     * @param value the value to increment
     * @param step the amount to increment
     * @return the incremented value
     */
    @SuppressWarnings("unchecked")
    private Comparable increment(Object value, Number step) {
        return (Comparable) plus((Number) value, step);
    }

    /**
     * Decrements by given step
     *
     * @param value the value to decrement
     * @param step the amount to decrement
     * @return the decremented value
     */
    @SuppressWarnings("unchecked")
    private Comparable decrement(Object value, Number step) {
        return (Comparable) minus((Number) value, step);
    }
}
