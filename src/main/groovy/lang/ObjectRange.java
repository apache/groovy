/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represents an inclusive list of objects from a value to a value using
 * comparators
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ObjectRange extends AbstractList implements Range {

    private Comparable from;
    private Comparable to;
    private int size;
    private final boolean reverse;

    public ObjectRange(Comparable from, Comparable to) {
        this.size = -1;
        this.reverse = ScriptBytecodeAdapter.compareGreaterThan(from, to);
        if (this.reverse) {
            constructorHelper(to, from);
        } else {
            constructorHelper(from, to);
        }
    }

    public ObjectRange(Comparable from, Comparable to, boolean reverse) {
        this.size = -1;
        constructorHelper(from, to);

        this.reverse = reverse;
    }

    private void constructorHelper(Comparable from, Comparable to) {
        if (from == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'from' index in a Range");
        }
        if (to == null) {
            throw new IllegalArgumentException("Must specify a non-null value for the 'to' index in a Range");
        }
        if (from.getClass() == to.getClass()) {
            this.from = from;
            this.to = to;
        } else {
            this.from = normaliseType(from);
            this.to = normaliseType(to);
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
            int i = 0;
            for (i = 0; i < length; i++) {
                if (start.charAt(i) != end.charAt(i)) break;
            }
            if (i < length - 1) {
                throw new IllegalArgumentException("Incompatible Strings for Range: String#next() will not reach the expected value");
            }

        }
    }

    public int hashCode() {
        /** @todo should code this the Josh Bloch way */
        return from.hashCode() ^ to.hashCode() + (reverse ? 1 : 0);
    }

    public boolean equals(Object that) {
        if (that instanceof ObjectRange) {
            return equals((ObjectRange) that);
        } else if (that instanceof List) {
            return equals((List) that);
        }
        return false;
    }

    public boolean equals(ObjectRange that) {
        return this.reverse == that.reverse
                && DefaultTypeTransformation.compareEqual(this.from, that.from)
                && DefaultTypeTransformation.compareEqual(this.to, that.to);
    }

    public boolean equals(List that) {
        int size = size();
        if (that.size() == size) {
            for (int i = 0; i < size; i++) {
                if (!DefaultTypeTransformation.compareEqual(get(i), that.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public Comparable getFrom() {
        return from;
    }

    public Comparable getTo() {
        return to;
    }

    public boolean isReverse() {
        return reverse;
    }

    public Object get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + " is too big for range: " + this);
        }
        Object value = null;
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

    public Iterator iterator() {
        return new Iterator() {
            int index = 0;
            Object value = (reverse) ? to : from;

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

    public int size() {
        if (size == -1) {
            if (from instanceof Integer && to instanceof Integer) {
                // lets fast calculate the size
                size = 0;
                int fromNum = ((Integer) from).intValue();
                int toNum = ((Integer) to).intValue();
                size = toNum - fromNum + 1;
            } else if (from instanceof BigDecimal || to instanceof BigDecimal) {
                // lets fast calculate the size
                size = 0;
                BigDecimal fromNum = new BigDecimal("" + from);
                BigDecimal toNum = new BigDecimal("" + to);
                BigInteger sizeNum = toNum.subtract(fromNum).add(new BigDecimal(1.0)).toBigInteger();
                size = sizeNum.intValue();
            } else {
                // lets lazily calculate the size
                size = 0;
                Object value = from;
                while (to.compareTo(value) >= 0) {
                    value = increment(value);
                    size++;
                }
            }
        }
        return size;
    }

    public List subList(int fromIndex, int toIndex) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        int size = size();
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (--toIndex >= size) {
            return new ObjectRange((Comparable) get(fromIndex), getTo(), reverse);
        } else {
            return new ObjectRange((Comparable) get(fromIndex), (Comparable) get(toIndex), reverse);
        }
    }

    public String toString() {
        return (reverse) ? "" + to + ".." + from : "" + from + ".." + to;
    }

    public String inspect() {
        String toText = InvokerHelper.inspect(to);
        String fromText = InvokerHelper.inspect(from);
        return (reverse) ? "" + toText + ".." + fromText : "" + fromText + ".." + toText;
    }

    public boolean contains(Object value) {
        if (value instanceof Comparable) {
            return contains((Comparable) value);
        } else {
            return super.contains(value);
        }
    }

    public boolean contains(Comparable value) {
        int result = from.compareTo(value);
        return result == 0 || result < 0 && to.compareTo(value) >= 0;
    }

    public void step(int step, Closure closure) {
        if (reverse) {
            step = -step;
        }
        if (step >= 0) {
            Comparable value = from;
            while (value.compareTo(to) <= 0) {
                closure.call(value);
                for (int i = 0; i < step; i++) {
                    value = (Comparable) increment(value);
                }
            }
        } else {
            step = -step;
            Comparable value = to;
            while (value.compareTo(from) >= 0) {
                closure.call(value);
                for (int i = 0; i < step; i++) {
                    value = (Comparable) decrement(value);
                }
            }
        }
    }

    public List step(int step) {
        IteratorClosureAdapter adapter = new IteratorClosureAdapter(this);
        step(step, adapter);
        return adapter.asList();
    }

    protected Object increment(Object value) {
        return InvokerHelper.invokeMethod(value, "next", null);
    }

    protected Object decrement(Object value) {
        return InvokerHelper.invokeMethod(value, "previous", null);
    }

    private static Comparable normaliseType(final Comparable operand) {
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
