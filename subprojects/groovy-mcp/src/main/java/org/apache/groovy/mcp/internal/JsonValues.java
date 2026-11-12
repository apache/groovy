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
package org.apache.groovy.mcp.internal;

import java.math.BigDecimal;

/**
 * Conversions from {@code JsonSlurper} values. Numbers may be
 * {@code Integer}, {@code Long} or {@code BigDecimal}.
 */
public final class JsonValues {

    private static final int BRIEF_LIMIT = 64;

    private JsonValues() {
    }

    /**
     * @param value a JSON number
     * @return the int value, or {@code null} when it is fractional or out of range
     */
    public static Integer asInt(final Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        if (number instanceof Double || number instanceof Float) {
            double wide = number.doubleValue();
            if (Double.isNaN(wide) || Double.isInfinite(wide) || wide != Math.rint(wide)) {
                return null;
            }
        }
        long exact;
        if (number instanceof BigDecimal decimal) {
            if (decimal.stripTrailingZeros().scale() > 0) {
                return null;
            }
            try {
                exact = decimal.longValueExact();
            } catch (ArithmeticException ex) {
                return null;
            }
        } else {
            exact = number.longValue();
        }
        if (exact < Integer.MIN_VALUE || exact > Integer.MAX_VALUE) {
            return null;
        }
        return (int) exact;
    }

    /**
     * Echoes a JSON-RPC id as a string or an integral number.
     *
     * @param id raw id
     * @return a string, {@code Integer} or {@code Long}
     */
    public static Object canonicalId(final Object id) {
        if (id instanceof String text) {
            if (text.length() > 1024) {
                throw new ProtocolException(-32600, "id is too long");
            }
            return text;
        }
        Integer integral = asInt(id);
        if (integral != null) {
            return integral;
        }
        if (id instanceof Long || id instanceof BigDecimal) {
            try {
                long wide = id instanceof Long ? (Long) id : ((BigDecimal) id).longValueExact();
                return wide;
            } catch (ArithmeticException ex) {
                throw new ProtocolException(-32600, "id must be a string or integer");
            }
        }
        throw new ProtocolException(-32600, "id must be a string or integer");
    }

    /**
     * One short line for an error message. Control characters are replaced
     * so a client string cannot break a stderr line.
     *
     * @param value raw text, possibly {@code null}
     * @return at most 64 code points
     */
    public static String brief(final String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (int i = 0; i < value.length() && count < BRIEF_LIMIT; ) {
            int point = value.codePointAt(i);
            builder.appendCodePoint(point < 0x20 || point == 0x7f ? '?' : point);
            i += Character.charCount(point);
            count++;
        }
        return builder.toString();
    }
}
