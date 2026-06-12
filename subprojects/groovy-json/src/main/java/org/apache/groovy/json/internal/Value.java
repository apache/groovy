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
package org.apache.groovy.json.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Represents a lazily decoded JSON token or container managed by the index-overlay parser.
 */
public interface Value {

    /**
     * Converts this token to a byte value.
     *
     * @return the byte representation
     */
    byte byteValue();

    /**
     * Converts this token to a short value.
     *
     * @return the short representation
     */
    short shortValue();

    /**
     * Converts this token to an int value.
     *
     * @return the int representation
     */
    int intValue();

    /**
     * Converts this token to a long value.
     *
     * @return the long representation
     */
    long longValue();

    /**
     * Converts this token to a {@link BigDecimal}.
     *
     * @return the decimal representation
     */
    BigDecimal bigDecimalValue();

    /**
     * Converts this token to a {@link BigInteger}.
     *
     * @return the integer representation
     */
    BigInteger bigIntegerValue();

    /**
     * Converts this token to a float value.
     *
     * @return the float representation
     */
    float floatValue();

    /**
     * Converts this token to a double value.
     *
     * @return the double representation
     */
    double doubleValue();

    /**
     * Converts this token to a boolean value.
     *
     * @return the boolean representation
     */
    boolean booleanValue();

    /**
     * Converts this token to a {@link Date}.
     *
     * @return the date representation
     */
    Date dateValue();

    /**
     * Returns the string form using the value's configured decoding policy.
     *
     * @return the string representation
     */
    String stringValue();

    /**
     * Returns the fully decoded string form.
     *
     * @return the decoded string representation
     */
    String stringValueEncoded();

    /**
     * Materializes this token as a regular Java value.
     *
     * @return the hydrated value
     */
    Object toValue();

    /**
     * Converts this token to an enum constant.
     *
     * @param cls enum type to resolve
     * @param <T> enum type
     * @return the resolved enum constant
     */
    <T extends Enum> T toEnum(Class<T> cls);

    /**
     * Indicates whether this value wraps a map or a collection.
     *
     * @return {@code true} for container values
     */
    boolean isContainer();

    /**
     * Copies any shared backing buffer into a dedicated slice.
     */
    void chop();

    /**
     * Converts this token to a single character.
     *
     * @return the leading character representation
     */
    char charValue();
}
