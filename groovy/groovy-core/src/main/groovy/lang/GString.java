/*
 * $Id$
 * 
 * Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.lang;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Represents a String which contains embedded values such as "hello there
 * ${user} how are you?" which can be evaluated lazily. Advanced users can
 * iterate over the text and values to perform special processing, such as for
 * performing SQL operations, the values can be substituted for ? and the
 * actual value objects can be bound to a JDBC statement. The lovely name of
 * this class was suggested by Jules Gosnell and was such a good idea, I
 * couldn't resist :)
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public abstract class GString extends GroovyObjectSupport implements Comparable, CharSequence, Writable {

    private Object[] values;
    /** cached value of the GString */
    private String text;

    public GString(Object values) {
        this.values = (Object[]) values;
    }

    public GString(Object[] values) {
        this.values = values;
    }

    // will be static in an instance
    public abstract String[] getStrings();

    /**
     * Overloaded to implement duck typing for Strings 
     * so that any method that can't be evaluated on this
     * object will be forwarded to the toString() object instead.
     */
    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        }
        catch (MissingMethodException e) {
            // lets try invoke the method on the real String
            return InvokerHelper.invokeMethod(toString(), name, args);
        }
    }

    public Object[] getValues() {
        return values;
    }

    public GString plus(GString that) {
        List stringList = new ArrayList();
        List valueList = new ArrayList();

        stringList.addAll(Arrays.asList(getStrings()));
        valueList.addAll(Arrays.asList(getValues()));

        if (stringList.size() > valueList.size()) {
            valueList.add("");
        }

        stringList.addAll(Arrays.asList(that.getStrings()));
        valueList.addAll(Arrays.asList(that.getValues()));

        final String[] newStrings = new String[stringList.size()];
        stringList.toArray(newStrings);
        Object[] newValues = valueList.toArray();

        return new GString(newValues) {
            public String[] getStrings() {
                return newStrings;
            }
        };
    }

    public GString plus(String that) {
        String[] currentStrings = getStrings();
        String[] newStrings = null;
        if (getValues().length <= getStrings().length) {
            newStrings = new String[currentStrings.length + 1];
            int lastIndex = currentStrings.length;
            System.arraycopy(currentStrings, 0, newStrings, 0, lastIndex);
            newStrings[lastIndex] = that;
            
        }
        else {
            newStrings = new String[currentStrings.length];
            int lastIndex = currentStrings.length - 1;
            System.arraycopy(currentStrings, 0, newStrings, 0, lastIndex);
            newStrings[lastIndex] = currentStrings[lastIndex] + that;
        }

        final String[] finalStrings = newStrings;
        return new GString(getValues()) {
            public String[] getStrings() {
                return finalStrings;
            }
        };
    }

    public int getValueCount() {
        return values.length;
    }

    public Object getValue(int idx) {
        return values[idx];
    }

    public String toString() {
        if (text == null) {
            StringWriter buffer = new StringWriter();
            try {
                writeTo(buffer);
            }
            catch (IOException e) {
                throw new StringWriterIOException(e);
            }
            text = buffer.toString();
        }
        return text;
    }

    public void writeTo(Writer out) throws IOException {
        String[] s = getStrings();
        int numberOfValues = values.length;
        for (int i = 0, size = s.length; i < size; i++) {
            out.write(s[i]);
            if (i < numberOfValues) {
                InvokerHelper.write(out, values[i]);
            }
        }
    }

    public boolean equals(Object that) {
        if (that instanceof GString) {
            return equals((GString) that);
        }
        return false;
    }

    public boolean equals(GString that) {
        return toString().equals(that.toString());
    }

    public int hashCode() {
        return 37 + toString().hashCode();
    }

    public int compareTo(Object that) {
        return toString().compareTo(that.toString());
    }

    public char charAt(int index) {
        return toString().charAt(index);
    }

    public int length() {
        return toString().length();
    }

    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }
}
