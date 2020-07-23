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
import org.apache.groovy.ast.tools.ImmutablePropertyUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of a GString used by the compiler. A GString consists
 * of a list of values and strings which can be combined to create a new String.
 *
 * @see groovy.lang.GString
 */
public class GStringImpl extends GString {
    private static final long serialVersionUID = 3581289038662723858L;
    private final String[] strings;
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
        super(values);
        this.strings = strings;
        cacheable = checkValuesImmutable();
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
        cacheable = false;
        cachedStringLiteral = null;
        return strings;
    }

    @Override
    public Object[] getValues() {
        cacheable = false;
        cachedStringLiteral = null;
        return super.getValues();
    }

    /**
     * Get the strings of this GString as an unmodifiable list.
     * This method is preferred over {@code getStrings()} in performance critical scenarios.
     */
    public List<String> getStringList() {
        return Collections.unmodifiableList(Arrays.asList(strings));
    }

    /**
     * Get the values of this GString as an unmodifiable list.
     * This method is preferred over {@code getValues()} in performance critical scenarios.
     */
    public List<?> getValueList() {
        return Collections.unmodifiableList(Arrays.asList(super.getValues()));
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

    private boolean checkValuesImmutable() {
        for (Object value : super.getValues()) {
            if (null == value) continue;
            if (!(ImmutablePropertyUtils.isBuiltinImmutable(value.getClass().getName())
                    || (value instanceof GStringImpl && ((GStringImpl) value).cacheable))) {
                return false;
            }
        }

        return true;
    }
}
