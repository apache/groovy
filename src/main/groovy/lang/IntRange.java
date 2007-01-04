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

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.math.BigInteger;

import org.codehaus.groovy.runtime.IteratorClosureAdapter;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

/**
 * Represents a list of Integer objects from a specified int up to and including
 * a given and to.
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class IntRange extends AbstractList implements Range {

    private int from;
    private int to;
    private boolean reverse;

    public IntRange(int from, int to) {
        if (from > to) {
            this.from = to;
            this.to = from;
            this.reverse = true;
        }
        else {
            this.from = from;
            this.to = to;
        }
    }

    protected IntRange(int from, int to, boolean reverse) {
        this.from = from;
        this.to = to;
        this.reverse = reverse;
    }

    public boolean equals(Object that) {
        if (that instanceof IntRange) {
            return equals((IntRange) that);
        }
        else if (that instanceof List) {
            return equals((List) that);
        }
        return false;
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

    public boolean equals(IntRange that) {
        return this.reverse == that.reverse && this.from == that.from && this.to == that.to;
    }

    public Comparable getFrom() {
        return new Integer(from);
    }

    public Comparable getTo() {
        return new Integer(to);
    }

    public int getFromInt() {
        return from;
    }

    public int getToInt() {
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
            throw new IndexOutOfBoundsException("Index: " + index + " too big for range: " + this);
        }
        int value = (reverse) ? to - index : index + from;
        return new Integer(value);
    }

    public int size() {
        return to - from + 1;
    }

    public int hashCode() {
        return from ^ to + (reverse ? 1 : 0);
    }

    public Iterator iterator() {
        return new Iterator() {
            int index = 0;
            int size = size();
            int value = (reverse) ? to : from;

            public boolean hasNext() {
                return index < size;
            }

            public Object next() {
                if (index++ > 0) {
                    if (index > size) {
                        return null;
                    }
                    else {
                        if (reverse) {
                            --value;
                        }
                        else {
                            ++value;
                        }
                    }
                }
                return new Integer(value);
            }

            public void remove() {
                IntRange.this.remove(index);
            }
        };
    }

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
        return new IntRange(fromIndex + this.from, toIndex + this.from - 1, reverse);
    }

    public String toString() {
        return (reverse) ? "" + to + ".." + from : "" + from + ".." + to;
    }
    
    public String inspect() {
        return toString();
    }
    
    public boolean contains(Object value) {
        if (value instanceof Integer) {
            Integer integer = (Integer) value;
            int i = integer.intValue();
            return i >= from && i <= to;
        }
        if (value instanceof IntRange) {
            IntRange range = (IntRange) value;
            return from<=range.from && range.to<=to;
        }
        if (value instanceof BigInteger) {
            BigInteger bigint = (BigInteger) value;
            return bigint.compareTo(BigInteger.valueOf(from)) >= 0 &&
                    bigint.compareTo(BigInteger.valueOf(to)) <= 0;
        }
        return false;
    }

    public void step(int step, Closure closure) {
        if (reverse) {
            step = -step;
        }
        if (step >= 0) {
            int value = from;
            while (value <= to) {
                closure.call(new Integer(value));
                value = value + step;
            }
        }
        else {
            int value = to;
            while (value >= from) {
                closure.call(new Integer(value));
                value = value + step;
            }
        }
    }

    public List step(int step) {
        IteratorClosureAdapter adapter = new IteratorClosureAdapter(this);
        step(step, adapter);
        return adapter.asList();
    }
}
