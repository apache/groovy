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

    public static Writer writeToImpl(Writer out, Object[] vs, String[] ss) throws IOException {
        int numberOfValues = vs.length;
        for (int i = 0, size = ss.length; i < size; i++) {
            out.write(ss[i]);
            if (i < numberOfValues) {
                final Object value = vs[i];

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
