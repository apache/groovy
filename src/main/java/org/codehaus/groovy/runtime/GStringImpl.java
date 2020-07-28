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
import org.apache.groovy.ast.tools.ImmutablePropertyUtils;

import java.io.IOException;
import java.io.Writer;

/**
 * Default implementation of a GString used by the compiler. A GString consists
 * of a list of values and strings which can be combined to create a new String.
 *
 * @see groovy.lang.GString
 */
public class GStringImpl extends GString {
    private static final long serialVersionUID = 3581289038662723858L;
    private static final String MKP = "mkp";
    private static final String YIELD = "yield";

    private final String[] strings;
    private boolean cacheable;
    private final boolean frozen;
    private String cachedStringLiteral;

    /**
     * Create a new GString with values and strings.
     * <p>
     * Each value is prefixed by a string, after the last value
     * an additional String might be used, hence the following constraint is expected to hold:
     * <code>
     * strings.length == values.length  ||  strings.length == values.length + 1
     * </code>.
     * <p>
     * <strong>NOTE:</strong> The lengths are <strong>not</strong> checked but using arrays with
     * lengths which violate the above constraint could result in unpredictable behaviour.
     *
     * @param values  the value parts
     * @param strings the string parts
     */
    public GStringImpl(Object[] values, String[] strings) {
        this(values, strings, checkValuesImmutable(values), null, false);
    }

    private GStringImpl(Object[] values, String[] strings, boolean cachable, String cachedStringLiteral, boolean frozen) {
        super(values);
        this.strings = strings;
        this.cacheable = cachable;
        this.frozen = frozen;
        this.cachedStringLiteral = cachedStringLiteral;
    }

    /**
     * Get the strings of this GString.
     * <p>
     * This methods returns the same array as used in the constructor.
     * Changing the values will result in changes of the GString.
     * It is generally not recommended to do so.
     */
    @Override
    public String[] getStrings() {
        if (!frozen) {
            cacheable = false;
            cachedStringLiteral = null;
        }
        return strings;
    }

    @Override
    public Object[] getValues() {
        if (!frozen) {
            cacheable = false;
            cachedStringLiteral = null;
        }
        return super.getValues();
    }

    @Override
    public String toString() {
        if (null != cachedStringLiteral) {
            return cachedStringLiteral;
        }
        String str = super.toString();
        if (cacheable || frozen) {
            cachedStringLiteral = str;
        }
        return str;
    }

    private static boolean checkValuesImmutable(Object[] values) {
        for (Object value : values) {
            if (null == value) continue;
            if (!(ImmutablePropertyUtils.isBuiltinImmutable(value.getClass().getName())
                    || (value instanceof GStringImpl && ((GStringImpl) value).cacheable))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public GString frozen() {
        return new GStringImpl(super.getValues(), strings, cacheable, cachedStringLiteral, frozen);
    }

    public GString plus(GString other) {
        GString that = other.frozen();
        Object[] thisValues = super.getValues();
        return new GStringImpl(
                appendValues(thisValues, that.getValues()),
                appendStrings(this.getStrings(), that.getStrings(), thisValues.length));
    }

    private static Object[] appendValues(Object[] values1, Object[] values2) {
        int values1Length = values1.length;
        int values2Length = values2.length;

        Object[] newValues = new Object[values1Length + values2Length];
        System.arraycopy(values1, 0, newValues, 0, values1Length);
        System.arraycopy(values2, 0, newValues, values1Length, values2Length);

        return newValues;
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

    @Override
    public Writer writeTo(Writer out) throws IOException {
        final String[] ss = this.getStrings();
        Object[] thisValues = super.getValues();
        int numberOfValues = thisValues.length;
        for (int i = 0, size = ss.length; i < size; i++) {
            out.write(ss[i]);
            if (i < numberOfValues) {
                final Object value = thisValues[i];

                if (value instanceof Closure) {
                    final Closure<?> c = (Closure<?>) value;
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

    @Override
    public void build(final GroovyObject builder) {
        final String[] ss = this.getStrings();
        Object[] thisValues = super.getValues();
        final int numberOfValues = thisValues.length;

        for (int i = 0, size = ss.length; i < size; i++) {
            builder.getProperty(MKP);
            builder.invokeMethod(YIELD, new Object[]{ss[i]});
            if (i < numberOfValues) {
                builder.getProperty(MKP);
                builder.invokeMethod(YIELD, new Object[]{thisValues[i]});
            }
        }
    }
}
