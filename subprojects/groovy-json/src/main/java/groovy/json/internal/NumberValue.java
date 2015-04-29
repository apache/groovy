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
package groovy.json.internal;

import groovy.json.JsonException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static groovy.json.internal.CharScanner.*;
import static groovy.json.internal.Exceptions.die;
import static groovy.json.internal.Exceptions.sputs;

/**
 * @author Rick Hightower
 */
public class NumberValue extends java.lang.Number implements Value {

    private char[] buffer;
    private boolean chopped;
    private int startIndex;
    private int endIndex;
    private Type type;
    private Object value;

    public NumberValue(Type type) {
        this.type = type;
    }

    public NumberValue() {

    }

    public NumberValue(boolean chop, Type type, int startIndex, int endIndex, char[] buffer) {
        this.type = type;

        try {
            if (chop) {
                this.buffer = ArrayUtils.copyRange(buffer, startIndex, endIndex);
                this.startIndex = 0;
                this.endIndex = this.buffer.length;
                chopped = true;
            } else {
                this.startIndex = startIndex;
                this.endIndex = endIndex;
                this.buffer = buffer;
            }
        } catch (Exception ex) {
            Exceptions.handle(sputs("exception", ex, "start", startIndex, "end", endIndex), ex);
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
        return toEnum(cls, intValue());
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

    private final Object doToValue() {
        switch (type) {
            case DOUBLE:
                return bigDecimalValue();
            case INTEGER:
                int sign = 1;
                boolean negative = false;
                if (buffer[startIndex] == '-') {
                    startIndex++;
                    sign = -1;
                    negative = true;

                }

                if (isInteger(buffer, startIndex, endIndex - startIndex)) {
                    return intValue() * sign;
                } else {
                    return longValue() * sign;
                }
        }
        die();
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Value)) return false;

        NumberValue value1 = (NumberValue) o;

        if (endIndex != value1.endIndex) return false;
        if (startIndex != value1.startIndex) return false;
        if (!Arrays.equals(buffer, value1.buffer)) return false;
        if (type != value1.type) return false;
        if (value != null ? !value.equals(value1.value) : value1.value != null) return false;

        return true;
    }

    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (buffer != null ? Arrays.hashCode(buffer) : 0);
        result = 31 * result + startIndex;
        result = 31 * result + endIndex;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public BigDecimal bigDecimalValue() {
        try {
            return new BigDecimal(buffer, startIndex, endIndex - startIndex);
        } catch (NumberFormatException e) {
            throw new JsonException("unable to parse " + new String(buffer, startIndex, endIndex - startIndex), e);
        }
    }

    public BigInteger bigIntegerValue() {
        return new BigInteger(toString());
    }

    public String stringValue() {
        return toString();
    }

    public String stringValueEncoded() {
        return toString();
    }

    public Date dateValue() {
        return new Date(Dates.utc(longValue()));
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

    private static float fpowersOf10[] = {
            1.0f,
            10.0f,
            100.0f,
            1000.0f,
            10000.0f,
            100000.0f,
            1000000.0f,
            10000000.0f,
            100000000.0f,
            1000000000.0f,
    };

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
