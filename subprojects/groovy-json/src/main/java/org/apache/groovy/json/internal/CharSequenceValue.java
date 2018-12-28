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

import groovy.json.JsonException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static org.apache.groovy.json.internal.CharScanner.isInteger;
import static org.apache.groovy.json.internal.CharScanner.parseIntFromTo;
import static org.apache.groovy.json.internal.CharScanner.parseLongFromTo;
import static org.apache.groovy.json.internal.Exceptions.die;

public class CharSequenceValue implements Value, CharSequence {

    private final Type type;
    private final boolean checkDate;
    private final boolean decodeStrings;

    private char[] buffer;
    private boolean chopped;
    private int startIndex;
    private int endIndex;
    private Object value;

    public CharSequenceValue(boolean chop, Type type, int startIndex, int endIndex, char[] buffer,
                             boolean encoded, boolean checkDate) {
        this.type = type;
        this.checkDate = checkDate;
        this.decodeStrings = encoded;

        if (chop) {
            try {
                this.buffer = ArrayUtils.copyRange(buffer, startIndex, endIndex);
            } catch (Exception ex) {
                Exceptions.handle(ex);
            }
            this.startIndex = 0;
            this.endIndex = this.buffer.length;
            this.chopped = true;
        } else {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.buffer = buffer;
        }
    }

    public String toString() {
        if (startIndex == 0 && endIndex == buffer.length) {
            return FastStringUtils.noCopyStringFromChars(buffer);
        } else {
            return new String(buffer, startIndex, (endIndex - startIndex));
        }
    }

    public final Object toValue() {
        return value != null ? value : (value = doToValue());
    }

    public <T extends Enum> T toEnum(Class<T> cls) {
        switch (type) {
            case STRING:
                return toEnum(cls, stringValue());
            case INTEGER:
                return toEnum(cls, intValue());
            case NULL:
                return null;
        }
        die("toEnum " + cls + " value was " + stringValue());
        return null;
    }

    public static <T extends Enum> T toEnum(Class<T> cls, String value) {
        try {
            return (T) Enum.valueOf(cls, value);
        } catch (Exception ex) {
            return (T) Enum.valueOf(cls, value.toUpperCase().replace('-', '_'));
        }
    }

    public static <T extends Enum> T toEnum(Class<T> cls, int value) {
        T[] enumConstants = cls.getEnumConstants();
        for (T e : enumConstants) {
            if (e.ordinal() == value) {
                return e;
            }
        }
        die("Can't convert ordinal value " + value + " into enum of type " + cls);
        return null;
    }

    public boolean isContainer() {
        return false;
    }

    private Object doToValue() {
        switch (type) {
            case DOUBLE:
                return doubleValue();
            case INTEGER:
                if (isInteger(buffer, startIndex, endIndex - startIndex)) {
                    return intValue();
                } else {
                    return longValue();
                }
            case STRING:
                if (checkDate) {
                    Date date = null;
                    if (Dates.isISO8601QuickCheck(buffer, startIndex, endIndex)) {
                        if (Dates.isJsonDate(buffer, startIndex, endIndex)) {
                            date = Dates.fromJsonDate(buffer, startIndex, endIndex);
                        } else if (Dates.isISO8601(buffer, startIndex, endIndex)) {
                            date = Dates.fromISO8601(buffer, startIndex, endIndex);
                        } else {
                            return stringValue();
                        }

                        if (date == null) {
                            return stringValue();
                        } else {
                            return date;
                        }
                    }
                }
                return stringValue();
        }
        die();
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;

        CharSequenceValue value1 = (CharSequenceValue) o;

        if (endIndex != value1.endIndex) return false;
        if (startIndex != value1.startIndex) return false;
        if (!Arrays.equals(buffer, value1.buffer)) return false;
        if (type != value1.type) return false;
        return value != null ? value.equals(value1.value) : value1.value == null;

    }

    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (buffer != null ? Arrays.hashCode(buffer) : 0);
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public final int length() {
        return buffer.length;
    }

    public final char charAt(int index) {
        return buffer[index];
    }

    public final CharSequence subSequence(int start, int end) {
        return new CharSequenceValue(false, type, start, end, buffer, decodeStrings, checkDate);
    }

    public BigDecimal bigDecimalValue() {
        return new BigDecimal(buffer, startIndex, endIndex - startIndex);
    }

    public BigInteger bigIntegerValue() {
        return new BigInteger(toString());
    }

    public String stringValue() {
        if (this.decodeStrings) {
            return JsonStringDecoder.decodeForSure(buffer, startIndex, endIndex);
        } else {
            return toString();
        }
    }

    public String stringValueEncoded() {
        return JsonStringDecoder.decode(buffer, startIndex, endIndex);
    }

    public Date dateValue() {
        if (type == Type.STRING) {

            if (Dates.isISO8601QuickCheck(buffer, startIndex, endIndex)) {

                if (Dates.isJsonDate(buffer, startIndex, endIndex)) {
                    return Dates.fromJsonDate(buffer, startIndex, endIndex);

                } else if (Dates.isISO8601(buffer, startIndex, endIndex)) {
                    return Dates.fromISO8601(buffer, startIndex, endIndex);
                } else {
                    throw new JsonException("Unable to convert " + stringValue() + " to date ");
                }
            } else {
                throw new JsonException("Unable to convert " + stringValue() + " to date ");
            }
        } else {
            return new Date(Dates.utc(longValue()));
        }
    }

    public int intValue() {
        int sign = 1;
        if (buffer[startIndex] == '-') {
            startIndex++;
            sign = -1;
        }
        return parseIntFromTo(buffer, startIndex, endIndex) * sign;
    }

    public long longValue() {
        if (isInteger(buffer, startIndex, endIndex - startIndex)) {
            return parseIntFromTo(buffer, startIndex, endIndex);
        } else {
            return parseLongFromTo(buffer, startIndex, endIndex);
        }
    }

    public byte byteValue() {
        return (byte) intValue();
    }

    public short shortValue() {
        return (short) intValue();
    }

    public double doubleValue() {
        return CharScanner.parseDouble(this.buffer, startIndex, endIndex);
    }

    public boolean booleanValue() {
        return Boolean.parseBoolean(toString());
    }

    public float floatValue() {
        return CharScanner.parseFloat(this.buffer, startIndex, endIndex);
    }

    public final void chop() {
        if (!chopped) {
            this.chopped = true;
            this.buffer = ArrayUtils.copyRange(buffer, startIndex, endIndex);
            this.startIndex = 0;
            this.endIndex = this.buffer.length;
        }
    }

    public char charValue() {
        return buffer[startIndex];
    }
}
