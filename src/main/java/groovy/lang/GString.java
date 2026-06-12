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

import org.apache.groovy.io.StringBuilderWriter;
import org.codehaus.groovy.runtime.GStringImpl;
import org.codehaus.groovy.runtime.GStringUtil;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.io.*;
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

    @Serial private static final long serialVersionUID = -2638020355892246323L;

    /**
     * Shared empty string array.
     */
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Shared empty object array.
     */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * A GString containing a single empty String and no values.
     */
    public static final GString EMPTY = new GString(EMPTY_OBJECT_ARRAY) {
        @Serial private static final long serialVersionUID = -7676746462783374250L;
        private static final String EMPTY_STRING = "";

        /**
         * {@inheritDoc}
         */
        @Override
        public String[] getStrings() {
            return new String[]{EMPTY_STRING};
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return EMPTY_STRING;
        }
    };

    private final Object[] values;

    /**
     * Creates a GString from the supplied values array reference.
     *
     * @param values the interpolated values
     */
    public GString(Object values) {
        this.values = (Object[]) values;
    }

    /**
     * Creates a GString from the supplied values.
     *
     * @param values the interpolated values
     */
    public GString(Object[] values) {
        this.values = values;
    }

    // will be static in an instance

    /**
     * Returns the string segments surrounding the interpolated values.
     *
     * @return the string segments
     */
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
            // let's try to invoke the method on the real String
            return InvokerHelper.invokeMethod(toString(), name, args);
        }
    }

    /**
     * Returns the interpolated values.
     *
     * @return the interpolated values
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * Concatenates this GString with another GString.
     *
     * @param that the other GString
     * @return the concatenated GString
     */
    public GString plus(GString that) {
        return GStringUtil.plusImpl(values, that.values, getStrings(), that.getStrings());
    }

    /**
     * Concatenates this GString with a String.
     *
     * @param that the string to append
     * @return the concatenated GString
     */
    public GString plus(String that) {
        return plus(new GStringImpl(EMPTY_OBJECT_ARRAY, new String[]{that}));
    }

    /**
     * Returns the number of interpolated values.
     *
     * @return the value count
     */
    public int getValueCount() {
        return values.length;
    }

    /**
     * Returns the interpolated value at the supplied index.
     *
     * @param idx the value index
     * @return the interpolated value
     */
    public Object getValue(int idx) {
        return values[idx];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        Writer buffer = new StringBuilderWriter(calcInitialCapacity());
        try {
            writeTo(buffer);
        } catch (IOException e) {
            throw new StringWriterIOException(e);
        }

        return buffer.toString();
    }

    /**
     * Calculates the initial buffer capacity for rendering this GString.
     *
     * @return the initial capacity
     */
    protected int calcInitialCapacity() {
        return GStringUtil.calcInitialCapacityImpl(values, getStrings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Writer writeTo(Writer out) throws IOException {
        return GStringUtil.writeToImpl(out, values, getStrings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void build(final GroovyObject builder) {
        GStringUtil.buildImpl(builder, values, getStrings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 37 + toString().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof GString)) return false;

        return equals((GString) that);
    }

    /**
     * Compares this GString with another GString by rendered content.
     *
     * @param that the other GString
     * @return {@code true} if the rendered strings are equal
     */
    public boolean equals(GString that) {
        return toString().equals(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Object that) {
        return toString().compareTo(that.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return toString().length();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns the rendered string as a byte array using the platform default charset.
     *
     * @return the rendered bytes
     */
    public byte[] getBytes() {
        return toString().getBytes();
    }

    /**
     * Returns the rendered string as a byte array using the supplied charset.
     *
     * @param charset the charset name
     * @return the rendered bytes
     * @throws UnsupportedEncodingException if the charset is unsupported
     */
    public byte[] getBytes(String charset) throws UnsupportedEncodingException {
        return toString().getBytes(charset);
    }
}
