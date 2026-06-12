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
import java.util.List;
import java.util.Map;

import static org.apache.groovy.json.internal.Exceptions.die;
import static org.apache.groovy.json.internal.Exceptions.sputs;

/**
 * Simple {@link Value} wrapper for literals and already-hydrated JSON containers.
 */
public class ValueContainer implements CharSequence, Value {

    /**
     * Shared wrapper for the JSON literal {@code true}.
     */
    public static final Value TRUE = new ValueContainer(Type.TRUE);
    /**
     * Shared wrapper for the JSON literal {@code false}.
     */
    public static final Value FALSE = new ValueContainer(Type.FALSE);
    /**
     * Shared wrapper for the JSON literal {@code null}.
     */
    public static final Value NULL = new ValueContainer(Type.NULL);

    /**
     * Cached hydrated value, when one already exists.
     */
    public Object value;

    /**
     * Token type represented by this wrapper.
     */
    public Type type;
    private boolean container;

    /**
     * Indicates whether string values should decode escape sequences when materialized.
     */
    public boolean decodeStrings;

    /**
     * Creates a wrapper around an already known value and type.
     *
     * @param value cached value
     * @param type token type
     * @param decodeStrings whether string decoding should be applied
     */
    public ValueContainer(Object value, Type type, boolean decodeStrings) {
        this.value = value;
        this.type = type;
        this.decodeStrings = decodeStrings;
    }

    /**
     * Creates a wrapper for a scalar token that can be materialized later.
     *
     * @param type token type
     */
    public ValueContainer(Type type) {
        this.type = type;
    }

    /**
     * Creates a wrapper for a parsed object container.
     *
     * @param map hydrated object value
     */
    public ValueContainer(Map<String, Object> map) {
        this.value = map;
        this.type = Type.MAP;
        this.container = true;
    }

    /**
     * Creates a wrapper for a parsed array container.
     *
     * @param list hydrated array value
     */
    public ValueContainer(List<Object> list) {
        this.value = list;
        this.type = Type.LIST;
        this.container = true;
    }

    /**
     * Unsupported for these literal and container wrappers.
     *
     * @return never returns normally
     */
    @Override
    public int intValue() {
        return die(int.class, sputs("intValue not supported for type ", type));
    }

    /**
     * Unsupported for these literal and container wrappers.
     *
     * @return never returns normally
     */
    @Override
    public long longValue() {
        return die(int.class, sputs("intValue not supported for type ", type));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean booleanValue() {
        switch (type) {
            case FALSE:
                return false;
            case TRUE:
                return true;
        }
        die();
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValue() {
        if (type == Type.NULL) {
            return null;
        } else {
            return type.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String stringValueEncoded() {
        return toString();
    }

    /**
     * Returns the token name for this wrapper.
     *
     * @return the type name
     */
    @Override
    public String toString() {
        return type.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object toValue() {
        if (value != null) {
            return value;
        }
        switch (type) {
            case FALSE:
                return (value = false);

            case TRUE:
                return (value = true);
            case NULL:
                return null;
        }
        die();
        return null;
    }

    /**
     * Returns the cached enum instance stored in this wrapper.
     *
     * @param cls enum type
     * @param <T> enum type
     * @return the cached enum value
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Enum> T toEnum(Class<T> cls) {
        return (T) value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isContainer() {
        return container;
    }

    /**
     * No-op because this wrapper does not retain a shared parser buffer.
     */
    @Override
    public void chop() {
    }

    /**
     * Returns {@code 0} because this wrapper is not backed by character data.
     *
     * @return {@code 0}
     */
    @Override
    public char charValue() {
        return 0;
    }

    /**
     * Returns {@code 0} because this wrapper does not expose a character sequence.
     *
     * @return {@code 0}
     */
    @Override
    public int length() {
        return 0;
    }

    /**
     * Returns the placeholder character {@code '0'} because no character data is available.
     *
     * @param index ignored
     * @return {@code '0'}
     */
    @Override
    public char charAt(int index) {
        return '0';
    }

    /**
     * Returns an empty character sequence because this wrapper has no character backing.
     *
     * @param start ignored
     * @param end ignored
     * @return an empty sequence
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return "";
    }

    /**
     * Returns {@code null} because this wrapper does not provide date conversion here.
     *
     * @return {@code null}
     */
    @Override
    public Date dateValue() {
        return null;
    }

    /**
     * Returns {@code 0} because this wrapper is not numeric.
     *
     * @return {@code 0}
     */
    @Override
    public byte byteValue() {
        return 0;
    }

    /**
     * Returns {@code 0} because this wrapper is not numeric.
     *
     * @return {@code 0}
     */
    @Override
    public short shortValue() {
        return 0;
    }

    /**
     * Returns {@code null} because this wrapper is not numeric.
     *
     * @return {@code null}
     */
    @Override
    public BigDecimal bigDecimalValue() {
        return null;
    }

    /**
     * Returns {@code null} because this wrapper is not numeric.
     *
     * @return {@code null}
     */
    @Override
    public BigInteger bigIntegerValue() {
        return null;
    }

    /**
     * Returns {@code 0} because this wrapper is not numeric.
     *
     * @return {@code 0}
     */
    @Override
    public double doubleValue() {
        return 0;
    }

    /**
     * Returns {@code 0} because this wrapper is not numeric.
     *
     * @return {@code 0}
     */
    @Override
    public float floatValue() {
        return 0;
    }
}
