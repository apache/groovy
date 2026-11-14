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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyRuntimeException;

import java.io.IOException;
import java.io.Writer;

/**
 * This class is primarily intended for INTERNAL USE. Use at your own risk.
 */
public final class GStringUtil {
    private static final String MKP = "mkp";
    private static final String YIELD = "yield";

    private GStringUtil() {
    }

    /**
     * Returns a new {@link GStringImpl} that concatenates the left and right parts.
     * The input arrays are not modified. Values are copied left then right.
     * When {@code thisStrings} is longer than {@code thisValues}, the last left
     * string is concatenated with the first right string and the remaining right
     * strings follow, so the boundary does not insert an empty value. Otherwise
     * the right strings are copied in full.
     *
     * @param thisValues values from the left GString
     * @param thatValues values from the right GString
     * @param thisStrings strings from the left GString
     * @param thatStrings strings from the right GString
     * @return a GString holding the combined values and strings
     */
    public static GString plusImpl(Object[] thisValues, Object[] thatValues, String[] thisStrings, String[] thatStrings) {
        return new GStringImpl(
                appendValues(thisValues, thatValues),
                appendStrings(thisStrings, thatStrings, thisValues.length));
    }

    private static String[] appendStrings(String[] strings1, String[] strings2, int values1Length) {
        int strings1Length = strings1.length;
        boolean isStringsLonger = strings1Length > values1Length;
        int strings2Length = isStringsLonger ? strings2.length - 1 : strings2.length;

        String[] newStrings = new String[strings1Length + strings2Length];
        System.arraycopy(strings1, 0, newStrings, 0, strings1Length);

        if (isStringsLonger) {
            // merge onto end of previous GString to avoid an empty bridging value
            System.arraycopy(strings2, 1, newStrings, strings1Length, strings2Length);

            int lastIndexOfStrings = strings1Length - 1;
            newStrings[lastIndexOfStrings] = strings1[lastIndexOfStrings] + strings2[0];
        } else {
            System.arraycopy(strings2, 0, newStrings, strings1Length, strings2Length);
        }

        return newStrings;
    }

    private static Object[] appendValues(Object[] values1, Object[] values2) {
        int values1Length = values1.length;
        int values2Length = values2.length;

        Object[] newValues = new Object[values1Length + values2Length];
        System.arraycopy(values1, 0, newValues, 0, values1Length);
        System.arraycopy(values2, 0, newValues, values1Length, values2Length);

        return newValues;
    }

    /**
     * Writes each string in {@code ss}, and the value at the same index when
     * {@code i} is in range of {@code vs}. A {@link Closure} value with no
     * parameters is called and {@link FormatHelper#write(Writer, Object)} writes
     * the call's result. A closure with one parameter is called with {@code out}
     * and its result is not written. Any other value, including {@code null}, is
     * passed to {@link FormatHelper#write(Writer, Object)}. Returns the same writer.
     *
     * @param out the writer that receives the text
     * @param vs the interpolated values
     * @param ss the literal strings
     * @return {@code out}
     * @throws IOException if a write fails
     * @throws GroovyRuntimeException if a closure value takes more than one parameter
     */
    public static Writer writeToImpl(Writer out, Object[] vs, String[] ss) throws IOException {
        int numberOfValues = vs.length;
        for (int i = 0, size = ss.length; i < size; i++) {
            out.write(ss[i]);
            if (i < numberOfValues) {
                final Object value = vs[i];

                if (value instanceof Closure c) {
                    int maximumNumberOfParameters = c.getMaximumNumberOfParameters();

                    if (maximumNumberOfParameters == 0) {
                        FormatHelper.write(out, c.call());
                    } else if (maximumNumberOfParameters == 1) {
                        c.call(out);
                    } else {
                        throw new GroovyRuntimeException("Trying to evaluate a GString containing a Closure taking "
                                + maximumNumberOfParameters + " parameters");
                    }
                } else {
                    FormatHelper.write(out, value);
                }
            }
        }
        return out;
    }

    /**
     * Yields each string, and each following value, through {@code builder}.
     * For every string, and for a value at the same index when one exists,
     * the property {@code mkp} is read and then {@code yield} is invoked with
     * that string or value. The property read's result is discarded.
     *
     * @param builder the builder that receives {@code mkp} and {@code yield}
     * @param vs the interpolated values
     * @param ss the literal strings
     */
    public static void buildImpl(GroovyObject builder, Object[] vs, String[] ss) {
        final int numberOfValues = vs.length;

        for (int i = 0, size = ss.length; i < size; i++) {
            builder.getProperty(MKP);
            builder.invokeMethod(YIELD, new Object[]{ss[i]});
            if (i < numberOfValues) {
                builder.getProperty(MKP);
                builder.invokeMethod(YIELD, new Object[]{vs[i]});
            }
        }
    }

    /**
     * Estimates a buffer size from the literal strings. Sums {@code ss[i].length()}.
     * When {@code ss} is non-empty, adds {@code vs.length} times the greater of
     * that sum divided by {@code ss.length} (integer division) and 8. Returns the
     * greater of 120% of that total, truncated to {@code int}, and 16. Value
     * contents are not inspected. An empty {@code ss} yields 16.
     *
     * @param vs values, used only for {@code vs.length}
     * @param ss literal strings whose lengths are summed
     * @return the estimated capacity, at least 16
     */
    public static int calcInitialCapacityImpl(Object[] vs, String[] ss) {
        int initialCapacity = 0;
        for (String string : ss) {
            initialCapacity += string.length();
        }
        if (ss.length != 0) {
            initialCapacity += vs.length * Math.max(initialCapacity / ss.length, 8);
        }
        return Math.max((int) (initialCapacity * 1.2), 16);
    }
}
