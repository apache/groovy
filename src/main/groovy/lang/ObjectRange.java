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
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents an inclusive list of objects from a value to a value using
 * comparators.
 * <p>
 * This class is similar to {@link IntRange}. If you make any changes to this
 * class, you might consider making parallel changes to {@link IntRange}.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class ObjectRange extends AbstractList implements Range {

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
     * Creates a new {@link ObjectRange,} assumes smaller <= larger, else behavior is undefined.
     * Caution: Prefer the other constructor when in doubt.
     *
     * Optimized Constructor avoiding initial computation of comparison.
     */
    public ObjectRange(Comparable smaller, Comparable larger, boolean reverse) {
        this(smaller, larger, Boolean.valueOf(reverse));
    }

    /**
     * Constructs a Range, computing reverse if not provided. When providing reverse,
     * 'smaller' must not be larger than 'larger'.
     *
     * @param smaller start of the range, must no be larger than to when reverse != null
     * @param larger end of the range, must be larger than from when reverse != null
     * @param reverse direction of the range. If null, causes direction to be computed (can be expensive).
     */
    private ObjectRange(Comparable smaller, Comparable larger, Boolean reverse) {
        if (reverse == null) {
            boolean computedReverse = areReversed(smaller, larger);
            // ensure invariant from <= to
            if (computedReverse) {
                Comparable temp = larger;
                larger = smaller;
                smaller = temp;
            }
            this.reverse = computedReverse;
        } else {
            this.reverse = reverse.booleanValue();
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
            smaller = Long.valueOf(((Integer) smaller).longValue());
        } else if (larger instanceof Integer && smaller instanceof Long) {
            larger = Long.valueOf(((Integer) larger).longValue());
        }

        // TODO: should we care about different types here?
        if (smaller.getClass() == larger.getClass()) {
            this.from = smaller;
            this.to = larger;
        } else {
            // Convenience hack: convert single-char strings to ints
            Comparable tempSmaller = normaliseCharAndSingleStringToInt(smaller);
            Comparable tempLarger = normaliseCharAndSingleStringToInt(larger);
            if (tempSmaller.getClass() != tempLarger.getClass()) {
                // if convenience hack did not make classes match,
                // and from cannot be advanced over to, throw exception.
                // Note if from as an unusual Object, it could have a next() method
                // that yields a Number or String to close the range
                if ((tempSmaller instanceof Number && !(tempLarger instanceof Number))
                        || (tempSmaller instanceof String && !(tempLarger instanceof String))) {
                    throw new IllegalArgumentException("Incompatible Argument classes for ObjectRange " + smaller.getClass() + ", " + larger.getClass());
                }
                // do not use normalized arguments when dealing with edge case
                this.from = smaller;
                this.to = larger;
            } else {
                this.from = tempSmaller;
                this.to = tempLarger;
            }
        }
        if (from instanceof String && to instanceof String) {
            // this test depends deeply on the String.next implementation
            // 009.next is 00:, not 010
            String start = smaller.toString();
            String end = larger.toString();
            if (start.length() != end.length()) {
                throw new IllegalArgumentException("Incompatible Strings for Range: different length");
            }
            int length = start.length();
            int i;
            for (i = 0; i < length; i++) {
                if (start.charAt(i) != end.charAt(i)) break;
            }
            // strings must be equal except for the last character
            if (i < length - 1) {
                throw new IllegalArgumentException("Incompatible Strings for Range: String#next() will not reach the expected value");
            }
        }
    }

    private static boolean areReversed(Comparable from, Comparable to) {
        if (from == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'from' index in a Range");
        }
        if (to == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'to' index in a Range");
        }

        try {
            return ScriptBytecodeAdapter.compareGreaterThan(from, to);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("Unable to create range due to incompatible types: " + from.getClass().getSimpleName() + ".." + to.getClass().getSimpleName() + " (possible missing brackets around range?)", cce);
        }
    }

    /**
     * {@inheritDoc}
     */
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
                && this.reverse == that.reverse
                && DefaultTypeTransformation.compareEqual(this.from, that.from)
                && DefaultTypeTransformation.compareEqual(this.to, that.to);
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getFrom() {
        return from;
    }

    /**
     * {@inheritDoc}
     */
    public Comparable getTo() {
        return to;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReverse() {
        return reverse;
    }

    /**
     * {@inheritDoc}
     */
    public Object get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + " is too big for range: " + this);
        }
        Object value;
        if (reverse) {
            value = to;

            for (int i = 0; i < index; i++) {
                value = decrement(value);
            }
        } else {
            value = from;
            for (int i = 0; i < index; i++) {
                value = increment(value);
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator iterator() {
        return new Iterator() {
            private int index;
            private Object value = reverse ? to : from;

            public boolean hasNext() {
                return index < size();
            }

            public Object next() {
                if (index++ > 0) {
                    if (index > size()) {
                        value = null;
                    } else {
                        if (reverse) {
                            value = decrement(value);
                        } else {
                            value = increment(value);
                        }
                    }
                }
                return value;
            }

            public void remove() {
                ObjectRange.this.remove(index);
            }
        };
    }

    /**
     * Checks whether a value is between the from and to values of a Range
     *
     * @param value the value of interest
     * @return true if the value is within the bounds
     */
    public boolean containsWithinBounds(Object value) {
        if (value instanceof Comparable) {
            int result = compareTo(from, (Comparable) value);
            return result == 0 || result < 0 && compareTo(to, (Comparable) value) >= 0;
        }
        return contains(value);
    }

    private int compareTo(Comparable first, Comparable second) {
        return DefaultGroovyMethods.numberAwareCompareTo(first, second);
    }

    /**
     * protection against calls from Groovy
     */
    private void setSize(int x) {
        throw new UnsupportedOperationException("size must not be changed");
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        if (size == -1) {
            if ((from instanceof Integer || from instanceof Long)
                    && (to instanceof Integer || to instanceof Long)) {
                // let's fast calculate the size
                long fromNum = ((Number) from).longValue();
                long toNum = ((Number) to).longValue();
                size = (int) (toNum - fromNum + 1);
            } else if (from instanceof Character && to instanceof Character) {
                // let's fast calculate the size
                char fromNum = (Character) from;
                char toNum = (Character) to;
                size = toNum - fromNum + 1;
            } else if (from instanceof BigDecimal || to instanceof BigDecimal ||
                       from instanceof BigInteger || to instanceof BigInteger) {
                // let's fast calculate the size
                BigDecimal fromNum = new BigDecimal(from.toString());
                BigDecimal toNum = new BigDecimal(to.toString());
                BigInteger sizeNum = toNum.subtract(fromNum).add(new BigDecimal(1.0)).toBigInteger();
                size = sizeNum.intValue();
            } else {
                // let's brute-force calculate the size by iterating start to end
                // use temp variable for thread-safety
                int tempsize = 0;
                Comparable first = from;
                Comparable value = from;
                while (compareTo(to, value) >= 0) {
                    value = (Comparable) increment(value);
                    tempsize++;
                    // handle back to beginning due to modulo incrementing
                    if (compareTo(first, value) >= 0) break;
                }
                size = tempsize;
            }
        }
        return size;
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
            return new EmptyRange(from);
        }

        return new ObjectRange((Comparable) get(fromIndex), (Comparable) get(--toIndex), reverse);
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
        String toText = InvokerHelper.inspect(to);
        String fromText = InvokerHelper.inspect(from);
        return reverse ? "" + toText + ".." + fromText : "" + fromText + ".." + toText;
    }

    /**
     * iterates over all values and returns true if one value matches.
     * Also see containsWithinBounds.
     */
    public boolean contains(Object value) {
        Iterator it = iterator();
        if (value == null) return false;
        while (it.hasNext()) {
            try {
                if (DefaultTypeTransformation.compareEqual(value, it.next())) return true;
            } catch (ClassCastException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void step(int step, Closure closure) {
        if (step == 0) {
            if (compareTo(from, to) != 0) {
                throw new GroovyRuntimeException("Infinite loop detected due to step size of 0");
            } else {
                return; // from == to and step == 0, nothing to do, so return
            }
        }

        if (reverse) {
            step = -step;
        }
        if (step > 0) {
            Comparable first = from;
            Comparable value = from;
            while (compareTo(value, to) <= 0) {
                closure.call(value);
                for (int i = 0; i < step; i++) {
                    value = (Comparable) increment(value);
                    if (compareTo(value, first) <= 0) return;
                }
            }
        } else {
            step = -step;
            Comparable first = to;
            Comparable value = to;
            while (compareTo(value, from) >= 0) {
                closure.call(value);
                for (int i = 0; i < step; i++) {
                    value = (Comparable) decrement(value);
                    if (compareTo(value, first) >= 0) return;
                }
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

    /**
     * Increments by one
     *
     * @param value the value to increment
     * @return the incremented value
     */
    protected Object increment(Object value) {
        return InvokerHelper.invokeMethod(value, "next", null);
    }

    /**
     * Decrements by one
     *
     * @param value the value to decrement
     * @return the decremented value
     */
    protected Object decrement(Object value) {
        return InvokerHelper.invokeMethod(value, "previous", null);
    }

    /**
     * if operand is a Character or a String with one character, return that characters int value.
     * @param operand
     * @return
     */
    private static Comparable normaliseCharAndSingleStringToInt(final Comparable operand) {
        if (operand instanceof Character) {
            return (int) ((Character) operand).charValue();
        } else if (operand instanceof String) {
            final String string = (String) operand;

            if (string.length() == 1)
                return (int) string.charAt(0);
            else
                return string;
        } else {
            return operand;
        }
    }
}
