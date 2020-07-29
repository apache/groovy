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

import groovy.lang.GString;
import groovy.lang.GroovyObject;
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
    private final String[] strings;
    private final boolean frozen;
    private boolean cacheable;
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
     * @param frozen  creates a GStringImpl which is not subject to mutation and hence more amenable to caching
     */
    protected GStringImpl(Object[] values, String[] strings, boolean cacheable, String cachedStringLiteral, boolean frozen) {
        super(frozen ? values.clone() : values);
        this.strings = frozen ? strings.clone() : strings;
        this.frozen = frozen;
        this.cacheable = cacheable;
    }

    @Override
    public GString plus(GString that) {
        GString thatFrozen = that instanceof GStringImpl ? ((GStringImpl) that).freeze() : that;
        return GStringUtil.plusImpl(super.getValues(), thatFrozen.getValues(), strings, thatFrozen.getStrings());
    }

    @Override
    public Writer writeTo(Writer out) throws IOException {
        return GStringUtil.writeToImpl(out, super.getValues(), strings);
    }

    /* (non-Javadoc)
     * @see groovy.lang.Buildable#build(groovy.lang.GroovyObject)
     */
    @Override
    public void build(final GroovyObject builder) {
        GStringUtil.buildImpl(builder, super.getValues(), strings);
    }

    @Override
    protected int calcInitialCapacity() {
        return GStringUtil.calcInitialCapacityImpl(super.getValues(), strings);
    }

    /**
     * @return returns an equivalent optimised but less mutable version of this GString
     */
    public GString freeze() {
        return new GStringImpl(super.getValues(), strings, cacheable, cachedStringLiteral, true);
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
        if (frozen) {
            return strings.clone();
        }
        cacheable = false;
        cachedStringLiteral = null;
        return strings;
    }

    @Override
    public Object[] getValues() {
        if (frozen) {
            return super.getValues().clone();
        }
        cacheable = false;
        cachedStringLiteral = null;
        return super.getValues();
    }

    @Override
    public String toString() {
        if (null != cachedStringLiteral) {
            return cachedStringLiteral;
        }
        String str = super.toString();
        if (cacheable) {
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
}
