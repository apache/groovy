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
 * <p/>
 * This class is similar to {@link IntRange}. If you make any changes to this
 * class, you might consider making parallel changes to {@link IntRange}.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ObjectRange extends AbstractList implements Range {

    /**
     * The first value in the range.
     */
    private Comparable from;

    /**
     * The last value in the range.
     */
    private Comparable to;

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
     * <code>from</code> < <code>to</code>.
     *
     * @param from the first value in the range.
     * @param to   the last value in the range.
     */
    public ObjectRange(Comparable from, Comparable to) {
        if (from == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'from' index in a Range");
        }
        if (to == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'to' index in a Range");
        }

        this.reverse = ScriptBytecodeAdapter.compareGreaterThan(from, to);
        if (this.reverse) {
            constructorHelper(to, from);
        } else {
            constructorHelper(from, to);
        }
    }

    public ObjectRange(Comparable from, Comparable to, boolean reverse) {
        constructorHelper(from, to);

        this.reverse = reverse;
    }

    private void constructorHelper(Comparable from, Comparable to) {
        if (from instanceof Short) {
            from = new Integer(((Short) from).intValue());
        } else if (from instanceof Float) {
            from = new Double(((Float) from).doubleValue());
        }
        if (to instanceof Short) {
            to = new Integer(((Short) to).intValue());
        } else if (to instanceof Float) {
            to = new Double(((Float) to).doubleValue());
        }

        // TODO: Should we align to like types?
//        if (from instanceof Integer && to instanceof Long) {
//            from = Long.valueOf(((Integer) from).longValue());
//        } else if (to instanceof Integer && from instanceof Long) {
//            to = Long.valueOf(((Integer) to).longValue());
//        }

        // TODO: should we care about different types here?
        if (from.getClass() == to.getClass()) {
            this.from = from;
            this.to = to;
        } else {
            this.from = normaliseStringType(from);
            this.to = normaliseStringType(to);
        }
        if (from instanceof String || to instanceof String) {
            // this test depends deeply on the String.next implementation
            // 009.next is 00:, not 010 
            String start = from.toString();
            String end = to.toString();
            if (start.length() > end.length()) {
                throw new IllegalArgumentException("Incompatible Strings for Range: starting String is longer than ending string");
            }
            int length = Math.min(start.length(), end.length());
            int i;
            for (i = 0; i < length; i++) {
                if (start.charAt(i) != end.charAt(i)) break;
            }
            if (i < length - 1) {
                throw new IllegalArgumentException("Incompatible Strings for Range: String#next() will not reach the expected value");
            }

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
     * {@inheritDoc}
     */
    public int size() {
        if (size == -1) {
            if ((from instanceof Integer || from instanceof Long)
                    && (to instanceof Integer || to instanceof Long)) {
                // let's fast calculate the size
                long fromNum = ((Number) from).longValue();
                long toNum = ((Number) to).longValue();
                size = (int)(toNum - fromNum + 1);
            } else if (from instanceof Character && to instanceof Character) {
                // let's fast calculate the size
                char fromNum = ((Character) from).charValue();
                char toNum = ((Character) to).charValue();
                size = toNum - fromNum + 1;
            } else if (from instanceof BigDecimal || to instanceof BigDecimal) {
                // let's fast calculate the size
                BigDecimal fromNum = new BigDecimal("" + from);
                BigDecimal toNum = new BigDecimal("" + to);
                BigInteger sizeNum = toNum.subtract(fromNum).add(new BigDecimal(1.0)).toBigInteger();
                size = sizeNum.intValue();
            } else {
                // let's lazily calculate the size
                size = 0;
                Comparable first = from;
                Comparable value = from;
                while (compareTo(to, value) >= 0) {
                    value = (Comparable) increment(value);
                    size++;
                    if (compareTo(first, value) >= 0) break; // handle back to beginning due to modulo incrementing
                }
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
        if (reverse) {
            step = -step;
        }
        if (step >= 0) {
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

    private static Comparable normaliseStringType(final Comparable operand) {
        if (operand instanceof Character) {
            return new Integer(((Character) operand).charValue());
        } else if (operand instanceof String) {
            final String string = (String) operand;

            if (string.length() == 1)
                return new Integer(string.charAt(0));
            else
                return string;
        } else {
            return operand;
        }
    }
}
