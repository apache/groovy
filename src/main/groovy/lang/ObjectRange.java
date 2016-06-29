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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.runtime.ScriptBytecodeAdapter.*;

/**
 * Represents an inclusive list of objects from a value to a value using
 * comparators.
 * <p>
 * Note: This class is similar to {@link IntRange}. If you make any changes to this
 * class, you might consider making parallel changes to {@link IntRange}.
 */
public class ObjectRange extends AbstractList<Comparable> implements Range<Comparable> {
    /**
     * The first value in the range.
     */
    private final Comparable from;

    /**
     * The last value in the range.
     */
    private final Comparable to;

    /**
     * The cached size, or -1 if not yet computed
     */
    private int size = -1;

    /**
     * <code>true</code> if the range counts backwards from <code>to</code> to <code>from</code>.
     */
    private final boolean reverse;

    /**
     * Creates a new {@link ObjectRange}. Creates a reversed range if
     * <code>from</code> &lt; <code>to</code>.
     *
     * @param from the first value in the range.
     * @param to   the last value in the range.
     */
    public ObjectRange(Comparable from, Comparable to) {
        this(from, to, null);
    }

    /**
     * Creates a new {@link ObjectRange} assumes smaller &lt;&#61; larger, else behavior is undefined.
     * Caution: Prefer the other constructor when in doubt.
     * <p>
     * Optimized Constructor avoiding initial computation of comparison.
     */
    public ObjectRange(Comparable smaller, Comparable larger, boolean reverse) {
        this(smaller, larger, (Boolean) reverse);
    }

    /**
     * Constructs a Range, computing reverse if not provided. When providing reverse,
     * 'smaller' must not be larger than 'larger'.
     *
     * @param smaller start of the range, must no be larger than to when reverse != null
     * @param larger  end of the range, must be larger than from when reverse != null
     * @param reverse direction of the range. If null, causes direction to be computed (can be expensive).
     */
    private ObjectRange(Comparable smaller, Comparable larger, Boolean reverse) {
        if (smaller == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'from' index in a Range");
        }
        if (larger == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'to' index in a Range");
        }
        if (reverse == null) {
            final boolean computedReverse = areReversed(smaller, larger);
            // ensure invariant from <= to
            if (computedReverse) {
                final Comparable temp = larger;
                larger = smaller;
                smaller = temp;
            }
            this.reverse = computedReverse;
        } else {
            this.reverse = reverse;
        }

        if (smaller instanceof Short) {
            smaller = ((Short) smaller).intValue();
        } else if (smaller instanceof Float) {
            smaller = ((Float) smaller).doubleValue();
        }
        if (larger instanceof Short) {
            larger = ((Short) larger).intValue();
        } else if (larger instanceof Float) {
            larger = ((Float) larger).doubleValue();
        }

        if (smaller instanceof Integer && larger instanceof Long) {
            smaller = ((Integer) smaller).longValue();
        } else if (larger instanceof Integer && smaller instanceof Long) {
            larger = ((Integer) larger).longValue();
        }

        /*
            areReversed() already does an implicit type compatibility check
            based on DefaultTypeTransformation.compareToWithEqualityCheck() for mixed classes
            but it is only invoked if reverse == null.
            So Object Range has to perform those type checks for consistency even when not calling
            compareToWithEqualityCheck(), and ObjectRange has
            to use the normalized value used in a successful comparison in
            compareToWithEqualityCheck(). Currently that means Chars and single-char Strings
            are evaluated as the char's charValue (an integer) when compared to numbers.
            So '7'..'9' should produce ['7', '8', '9'], whereas ['7'..9] and [7..'9'] should produce [55, 56, 57].
            if classes match, or both numerical, no checks possible / necessary
        */
        if (smaller.getClass() == larger.getClass() ||
                (smaller instanceof Number && larger instanceof Number)) {
            this.from = smaller;
            this.to = larger;
        } else {
            // Convenience hack: try convert single-char strings to ints
            final Comparable tempfrom = normaliseStringType(smaller);
            final Comparable tempto = normaliseStringType(larger);
            // if after normalizing both are numbers, assume intended range was numbers
            if (tempfrom instanceof Number && tempto instanceof Number) {
                this.from = tempfrom;
                this.to = tempto;
            } else {
                // if convenience hack did not make classes match,
                // throw exception when starting with known class, and thus "from" cannot be advanced over "to".
                // Note if start is an unusual Object, it could have a next() method
                // that yields a Number or String to close the range
                final Comparable start = this.reverse ? larger : smaller;
                if (start instanceof String || start instanceof Number) {
                    // starting with number will never reach a non-number, same for string
                    throw new IllegalArgumentException("Incompatible Argument classes for ObjectRange " + smaller.getClass() + ", " + larger.getClass());
                }
                // Since normalizing did not help, use original values at user's risk
                this.from = smaller;
                this.to = larger;
            }
        }
        checkBoundaryCompatibility();
    }

    /**
     * throws IllegalArgumentException if to and from are incompatible, meaning they e.g. (likely) produce infinite sequences.
     * Called at construction time, subclasses may override cautiously (using only members to and from).
     */
    protected void checkBoundaryCompatibility() {
        if (from instanceof String && to instanceof String) {
            // this test depends deeply on the String.next implementation
            // 009.next is 00:, not 010
            final String start = from.toString();
            final String end = to.toString();
            if (start.length() != end.length()) {
                throw new IllegalArgumentException("Incompatible Strings for Range: different length");
            }
            final int length = start.length();
            int i;
            for (i = 0; i < length; i++) {
                if (start.charAt(i) != end.charAt(i)) {
                    break;
                }
            }
            // strings must be equal except for the last character
            if (i < length - 1) {
                throw new IllegalArgumentException("Incompatible Strings for Range: String#next() will not reach the expected value");
            }
        }
    }

    private static boolean areReversed(Comparable from, Comparable to) {
        try {
            return compareGreaterThan(from, to);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Unable to create range due to incompatible types: " + from.getClass().getSimpleName() + ".." + to.getClass().getSimpleName() + " (possible missing brackets around range?)", cce);
        }
    }

    public boolean equals(Object that) {
        return (that instanceof ObjectRange) ? equals((ObjectRange) that) : super.equals(that);
    }

    /**
     * Compares an {@link ObjectRange} to another {@link ObjectRange}.
     *
     * @param that the object to check equality with
     * @return <code>true</code> if the ranges are equal
     */
    public boolean equals(ObjectRange that) {
        return that != null
                && reverse == that.reverse
                && compareEqual(from, that.from)
                && compareEqual(to, that.to);
    }

    @Override
    public Comparable getFrom() {
        return from;
    }

    @Override
    public Comparable getTo() {
        return to;
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

        int i = 0;
        for (Iterator<Comparable> iter = new LazySteppingIterator(this, 1); ; i++) {
            final Comparable next = iter.next();
            if (i == index) {
                return next;
            } else if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + index + " is too big for range: " + this);
            }
        }
    }

    /**
     * Checks whether a value is between the from and to values of a Range
     *
     * @param value the value of interest
     * @return true if the value is within the bounds
     */
    @Override
    public boolean containsWithinBounds(Object value) {
        if (value instanceof Comparable) {
            final int result = compareTo(from, value);
            return result == 0 || result < 0 && compareGreaterThanEqual(to, value);
        }
        return contains(value);
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
            int tempsize = 0;
            if ((from instanceof Integer || from instanceof Long)
                    && (to instanceof Integer || to instanceof Long)) {
                // let's fast calculate the size
                final BigInteger fromNum = new BigInteger(from.toString());
                final BigInteger toNum = new BigInteger(to.toString());
                final BigInteger sizeNum = toNum.subtract(fromNum).add(new BigInteger("1"));
                tempsize = sizeNum.intValue();
                if (!BigInteger.valueOf(tempsize).equals(sizeNum)) {
                    tempsize = Integer.MAX_VALUE;
                }
            } else if (from instanceof Character && to instanceof Character) {
                // let's fast calculate the size
                final char fromNum = (Character) from;
                final char toNum = (Character) to;
                tempsize = toNum - fromNum + 1;
            } else if (((from instanceof BigDecimal || from instanceof BigInteger) && to instanceof Number) ||
                    ((to instanceof BigDecimal || to instanceof BigInteger) && from instanceof Number)) {
                // let's fast calculate the size
                final BigDecimal fromNum = new BigDecimal(from.toString());
                final BigDecimal toNum = new BigDecimal(to.toString());
                final BigInteger sizeNum = toNum.subtract(fromNum).add(new BigDecimal(1.0)).toBigInteger();
                tempsize = sizeNum.intValue();
                if (!BigInteger.valueOf(tempsize).equals(sizeNum)) {
                    tempsize = Integer.MAX_VALUE;
                }
            } else {
                // let's brute-force calculate the size by iterating start to end
                for (Iterator<Comparable> iter = new LazySteppingIterator(this, 1); iter.hasNext(); ) {
                    tempsize++;
                    // integer overflow
                    if (tempsize < 0) {
                        break;
                    }
                    iter.next();
                }
            }
            // integer overflow
            if (tempsize < 0) {
                tempsize = Integer.MAX_VALUE;
            }
            size = tempsize;
        }
        return size;
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
        final Iterator<Comparable> iter = new LazySteppingIterator(this, 1);

        Comparable toValue = iter.next();
        int i = 0;
        for (; i < fromIndex; i++) {
            if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + i + " is too big for range: " + this);
            }
            toValue = iter.next();
        }
        final Comparable fromValue = toValue;
        for (; i < toIndex - 1; i++) {
            if (!iter.hasNext()) {
                throw new IndexOutOfBoundsException("Index: " + i + " is too big for range: " + this);
            }
            toValue = iter.next();
        }

        return new ObjectRange(fromValue, toValue, reverse);
    }

    public String toString() {
        return reverse ? "" + to + ".." + from : "" + from + ".." + to;
    }

    @Override
    public String inspect() {
        final String toText = InvokerHelper.inspect(to);
        final String fromText = InvokerHelper.inspect(from);
        return reverse ? "" + toText + ".." + fromText : "" + fromText + ".." + toText;
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

        for (Iterator<Comparable> iter = new LazySteppingIterator(this, 1); iter.hasNext(); ) {
            if (compareEqual(value, iter.next())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void step(int step, Closure closure) {
        if (step == 0 && compareEqual(from, to)) {
            return; // from == to and step == 0, nothing to do, so return
        }

        for (Iterator<Comparable> iter = new LazySteppingIterator(this, step); iter.hasNext(); ) {
            closure.call(iter.next());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Comparable> iterator() {
        return new Iterator<Comparable>() {
            private final Iterator<Comparable> delegate = new LazySteppingIterator(ObjectRange.this, 1);

            @Override
            public synchronized boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public synchronized Comparable next() {
                return delegate.next();
            }

            @Override
            public synchronized void remove() {
                delegate.remove();
            }
        };
    }

    /**
     * Iterator with configurable step size
     * It's not thread-safe, and lazily produces the current element (including the first one)
     * only on hasNext() or next() calls.
     */
    class LazySteppingIterator implements Iterator<Comparable> {
        private final Range<?> range;
        private final int step;
        private final boolean isAscending;

        private boolean isNextFetched = false;
        private Comparable next = null;

        LazySteppingIterator(Range<?> range, int desiredStep) {
            if (desiredStep == 0 && compareNotEqual(range.getFrom(), range.getTo())) {
                throw new GroovyRuntimeException("Infinite loop detected due to step size of 0");
            }

            this.range = range;
            if (desiredStep < 0) {
                step = -1 * desiredStep;
                isAscending = range.isReverse();
            } else {
                step = desiredStep;
                isAscending = !range.isReverse();
            }
        }

        @Override
        public boolean hasNext() {
            fetchNextIfNeeded();
            return (next != null) && (isAscending ? compareLessThanEqual(next, range.getTo())
                                                  : compareGreaterThanEqual(next, range.getFrom()));
        }

        @Override
        public Comparable next() {
            if (!hasNext()) {
                return null;
            }

            fetchNextIfNeeded();
            isNextFetched = false;
            return next;
        }

        private void fetchNextIfNeeded() {
            if (!isNextFetched) {
                isNextFetched = true;

                if (next == null) {
                    next = isAscending ? range.getFrom() // make the first fetch lazy too
                                       : range.getTo();
                } else {
                    next = isAscending ? increment(next, step)
                                       : decrement(next, step);
                }
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public List<Comparable> step(int step) {
        final IteratorClosureAdapter<Comparable> adapter = new IteratorClosureAdapter<Comparable>(this);
        step(step, adapter);
        return adapter.asList();
    }

    /**
     * Increments by step size
     *
     * @param value the value to increment
     * @param step  the value to increment by
     * @return the incremented value or null, if there isn't any
     */
    @SuppressWarnings("unchecked")
    private Comparable increment(Comparable value, int step) {
        for (int i = 0; i < step; i++) {
            final Comparable next = (Comparable) InvokerHelper.invokeMethod(value, "next", null);
            if (!compareGreaterThan(next, value)) /* e.g. `next` of the last element */ {
                return null;
            }
            value = next;
        }
        return value;
    }

    /**
     * Decrements by step size
     *
     * @param value the value to decrement
     * @param step  the value to decrement by
     * @return the decremented value or null, if there isn't any
     */
    @SuppressWarnings("unchecked")
    private Comparable decrement(Comparable value, int step) {
        for (int i = 0; i < step; i++) {
            final Comparable previous = (Comparable) InvokerHelper.invokeMethod(value, "previous", null);
            if (!compareLessThan(previous, value)) /* e.g. `previous` of the first element */ {
                return null;
            }
            value = previous;
        }
        return value;
    }

    /**
     * if operand is a Character or a String with one character, return that character's int value.
     */
    private static Comparable normaliseStringType(final Comparable operand) {
        if (operand instanceof Character) {
            return (int) (Character) operand;
        }
        if (operand instanceof String) {
            final String string = (String) operand;

            if (string.length() == 1) {
                return (int) string.charAt(0);
            }
            return string;
        }
        return operand;
    }
}
