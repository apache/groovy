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

import org.apache.groovy.ast.tools.ImmutablePropertyUtils;
import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.runtime.GStringImpl;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.regex.Pattern;

/**
 * Represents a String which contains embedded values such as "hello there
 * ${user} how are you?" which can be evaluated lazily. Advanced users can
 * iterate over the text and values to perform special processing, such as for
 * performing SQL operations, the values can be substituted for ? and the
 * actual value objects can be bound to a JDBC statement.
 * <p>
 * James Strachan: The lovely name of this class was suggested by Jules Gosnell
 * and was such a good idea, I couldn't resist :)
 */
public abstract class GString extends GroovyObjectSupport implements Comparable, CharSequence, Writable, Buildable, Serializable {

    private static final long serialVersionUID = -2638020355892246323L;
    private static final String MKP = "mkp";
    private static final String YIELD = "yield";

    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * A GString containing a single empty String and no values.
     */
    public static final GString EMPTY = new GString(EMPTY_OBJECT_ARRAY) {
        private static final long serialVersionUID = -7676746462783374250L;
        private static final String EMPTY_STRING = "";

        @Override
        public String[] getStrings() {
            return new String[] { EMPTY_STRING };
        }

        @Override
        public String toString() {
            return EMPTY_STRING;
        }
    };


    private final Object[] values;
    private final boolean immutable;

    public GString(Object values) {
        this.values = (Object[]) values;
        this.immutable = checkImmutable(this.values);
    }

    public GString(Object[] values) {
        this.values = values;
        this.immutable = checkImmutable(this.values);
    }

    // will be static in an instance

    public abstract String[] getStrings();

    /**
     * Overloaded to implement duck typing for Strings
     * so that any method that can't be evaluated on this
     * object will be forwarded to the toString() object instead.
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        try {
            return super.invokeMethod(name, args);
        } catch (MissingMethodException e) {
            // lets try invoke the method on the real String
            return InvokerHelper.invokeMethod(toString(), name, args);
        }
    }

    public Object[] getValues() {
        return values.clone();
    }

    public GString plus(GString that) {
        Object[] values = this.values;

        return new GStringImpl(appendValues(values, that.values), appendStrings(getStrings(), that.getStrings(), values.length));
    }

    private String[] appendStrings(String[] strings, String[] thatStrings, int valuesLength) {
        int stringsLength = strings.length;
        boolean isStringsLonger = stringsLength > valuesLength;
        int thatStringsLength = isStringsLonger ? thatStrings.length - 1 : thatStrings.length;

        String[] newStrings = new String[stringsLength + thatStringsLength];
        System.arraycopy(strings, 0, newStrings, 0, stringsLength);

        if (isStringsLonger) {
            // merge onto end of previous GString to avoid an empty bridging value
            System.arraycopy(thatStrings, 1, newStrings, stringsLength, thatStringsLength);

            int lastIndexOfStrings = stringsLength - 1;
            newStrings[lastIndexOfStrings] = strings[lastIndexOfStrings] + thatStrings[0];
        } else {
            System.arraycopy(thatStrings, 0, newStrings, stringsLength, thatStringsLength);
        }

        return newStrings;
    }

    private Object[] appendValues(Object[] values, Object[] thatValues) {
        int valuesLength = values.length;
        int thatValuesLength = thatValues.length;

        Object[] newValues = new Object[valuesLength + thatValuesLength];
        System.arraycopy(values, 0, newValues, 0, valuesLength);
        System.arraycopy(thatValues, 0, newValues, valuesLength, thatValuesLength);

        return newValues;
    }

    public GString plus(String that) {
        return plus(new GStringImpl(EMPTY_OBJECT_ARRAY, new String[]{that}));
    }

    public int getValueCount() {
        return values.length;
    }

    public Object getValue(int idx) {
        return values[idx];
    }


    private String cachedStringLiteral;

    @Override
    public String toString() {
        if (null != cachedStringLiteral) {
            return cachedStringLiteral;
        }

        Writer buffer = new StringBuilderWriter(calcInitialCapacity());
        try {
            writeTo(buffer);
        } catch (IOException e) {
            throw new StringWriterIOException(e);
        }

        String str = buffer.toString();

        return immutable ? (cachedStringLiteral = str) : str;
    }

    private int calcInitialCapacity() {
        String[] strings = getStrings();

        int initialCapacity = 0;
        for (String string : strings) {
            initialCapacity += string.length();
        }

        initialCapacity += values.length * Math.max(initialCapacity / strings.length, 8);

        return Math.max((int) (initialCapacity * 1.2), 16);
    }

    @Override
    public Writer writeTo(Writer out) throws IOException {
        String[] s = getStrings();
        int numberOfValues = values.length;
        for (int i = 0, size = s.length; i < size; i++) {
            out.write(s[i]);
            if (i < numberOfValues) {
                final Object value = values[i];

                if (value instanceof Closure) {
                    final Closure c = (Closure) value;
                    int maximumNumberOfParameters = c.getMaximumNumberOfParameters();

                    if (maximumNumberOfParameters == 0) {
                        InvokerHelper.write(out, c.call());
                    } else if (maximumNumberOfParameters == 1) {
                        c.call(out);
                    } else {
                        throw new GroovyRuntimeException("Trying to evaluate a GString containing a Closure taking "
                                + maximumNumberOfParameters + " parameters");
                    }
                } else {
                    InvokerHelper.write(out, value);
                }
            }
        }
        return out;
    }

    /* (non-Javadoc)
     * @see groovy.lang.Buildable#build(groovy.lang.GroovyObject)
     */

    @Override
    public void build(final GroovyObject builder) {
        final String[] s = getStrings();
        final int numberOfValues = values.length;

        for (int i = 0, size = s.length; i < size; i++) {
            builder.getProperty(MKP);
            builder.invokeMethod(YIELD, new Object[]{s[i]});
            if (i < numberOfValues) {
                builder.getProperty(MKP);
                builder.invokeMethod(YIELD, new Object[]{values[i]});
            }
        }
    }

    @Override
    public int hashCode() {
        return 37 + toString().hashCode();
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof GString)) return false;

        return equals((GString) that);
    }

    public boolean equals(GString that) {
        return toString().equals(that.toString());
    }

    @Override
    public int compareTo(Object that) {
        return toString().compareTo(that.toString());
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /**
     * Turns a String into a regular expression pattern
     *
     * @return the regular expression pattern
     */
    public Pattern negate() {
        return StringGroovyMethods.bitwiseNegate(toString());
    }

    public byte[] getBytes() {
        return toString().getBytes();
    }

    public byte[] getBytes(String charset) throws UnsupportedEncodingException {
        return toString().getBytes(charset);
    }

    private static boolean checkImmutable(Object[] values) {
        for (Object value : values) {
            if (null == value) continue;
            if (!(ImmutablePropertyUtils.isBuiltinImmutable(value.getClass().getName())
                    || (value instanceof GString && ((GString) value).immutable))) {
                return false;
            }
        }

        return true;
    }
}
