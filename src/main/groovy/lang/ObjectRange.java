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
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.IteratorClosureAdapter;

/**
 * Represents a list of objects from a value to a value using
 * comparators
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class ObjectRange extends AbstractList implements Range {

    private Comparable from;
    private Comparable to;
    private int size = -1;

    public ObjectRange(Comparable from, Comparable to) {
        this.from = from;
        this.to = to;
    }

    public int hashCode() {
        /** @todo should code this the Josh Bloch way */
        return from.hashCode() ^ to.hashCode();
    }

    public boolean equals(Object that) {
        if (that instanceof ObjectRange) {
            return equals((ObjectRange) that);
        }
        else if (that instanceof List) {
            return equals((List) that);
        }
        return false;
    }

    public boolean equals(ObjectRange that) {
        return InvokerHelper.compareEqual(this.from, that.from) && InvokerHelper.compareEqual(this.to, that.to);
    }

    public boolean equals(List that) {
        int size = size();
        if (that.size() == size) {
            for (int i = 0; i < size; i++ ) {
                if (! InvokerHelper.compareEqual(get(i), that.get(i))) {
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

    public Object get(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " should not be negative");
        }
        Object value = from;
        for (int i = 0; i < index; i++) {
            value = increment(value);
        }
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + " is too big for range: " + this);
        }
        return value;
    }

    public int size() {
        if (size == -1) {
            // lets lazily calculate the size
            size = 0;
            Object value = from;
            while (to.compareTo(value) > 0) {
                value = increment(value);
                size++;
            }
        }
        return size;
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
        return new ObjectRange((Comparable) get(fromIndex), (Comparable) get(toIndex));
    }

    public String toString() {
        return "" + from + ".." + to;
    }

    public boolean contains(Comparable value) {
        int result = from.compareTo(value);
        if (result == 0) {
            return true;
        }
        return result < 0 && to.compareTo(value) > 0;
    }

    public void step(int step, Closure closure) {
        if (step >= 0) {
            Comparable value = from;
            while (value.compareTo(to) <= 0) {
                closure.call(value);
                for (int i = 0; i < step; i++) {
                    value = (Comparable) increment(value);
                }
            }
        }
        else {
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
        return InvokerHelper.invokeMethod(value, "increment", null);
    }

    protected Object decrement(Object value) {
        return InvokerHelper.invokeMethod(value, "decrement", null);
    }
   }
