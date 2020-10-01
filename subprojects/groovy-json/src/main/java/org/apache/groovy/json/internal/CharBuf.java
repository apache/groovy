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

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class CharBuf extends Writer implements CharSequence {

    protected int capacity = 16;
    protected int location = 0;

    protected char[] buffer;

    public CharBuf(char[] buffer) {
        __init__(buffer);
    }

    private void __init__(char[] buffer) {
        this.buffer = buffer;
        this.capacity = buffer.length;
    }

    public CharBuf(byte[] bytes) {
        this.buffer = null;
        String str = new String(bytes, StandardCharsets.UTF_8);
        __init__(FastStringUtils.toCharArray(str));
    }

    public static CharBuf createExact(final int capacity) {
        return new CharBuf(capacity) {
            @Override
            public CharBuf add(char[] chars) {
                Chr._idx(buffer, location, chars);
                location += chars.length;
                return this;
            }
        };
    }

    public static CharBuf create(int capacity) {
        return new CharBuf(capacity);
    }

    public static CharBuf create(char[] buffer) {
        return new CharBuf(buffer);
    }

    protected CharBuf(int capacity) {
        this.capacity = capacity;
        init();
    }

    protected CharBuf() {
        init();
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if (off == 0 && cbuf.length == len) {
            this.add(cbuf);
        } else {
            char[] buffer = ArrayUtils.copyRange(cbuf, off, off + len);
            this.add(buffer);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

    public void init() {
        buffer = new char[capacity];
    }

    public final CharBuf add(String str) {
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    public final CharBuf addString(String str) {
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    public final CharBuf add(int i) {
        add(Integer.toString(i));
        return this;
    }

    private Cache<Integer, char[]> icache;

    public final CharBuf addInt(int i) {
        switch (i) {
            case 0:
                addChar('0');
                return this;
            case 1:
                addChar('1');
                return this;
            case -1:
                addChar('-');
                addChar('1');
                return this;
        }

        addInt(Integer.valueOf(i));
        return this;
    }

    public final CharBuf addInt(Integer key) {
        if (icache == null) {
            icache = new SimpleCache<Integer, char[]>(20);
        }
        char[] chars = icache.get(key);

        if (chars == null) {
            String str = Integer.toString(key);
            chars = FastStringUtils.toCharArray(str);
            icache.put(key, chars);
        }

        addChars(chars);
        return this;
    }

    final char[] trueChars = "true".toCharArray();
    final char[] falseChars = "false".toCharArray();

    public final CharBuf add(boolean b) {
        addChars(b ? trueChars : falseChars);
        return this;
    }

    public final CharBuf addBoolean(boolean b) {
        add(Boolean.toString(b));
        return this;
    }

    public final CharBuf add(byte i) {
        add(Byte.toString(i));
        return this;
    }

    public final CharBuf addByte(byte i) {
        addInt(i);
        return this;
    }

    public final CharBuf add(short i) {
        add(Short.toString(i));
        return this;
    }

    public final CharBuf addShort(short i) {
        addInt(i);
        return this;
    }

    public final CharBuf add(long l) {
        add(Long.toString(l));
        return this;
    }

    public final CharBuf add(double d) {
        add(Double.toString(d));
        return this;
    }

    private Cache<Double, char[]> dcache;

    public final CharBuf addDouble(double d) {
        addDouble(Double.valueOf(d));
        return this;
    }

    public final CharBuf addDouble(Double key) {
        if (dcache == null) {
            dcache = new SimpleCache<Double, char[]>(20);
        }
        char[] chars = dcache.get(key);

        if (chars == null) {
            String str = Double.toString(key);
            chars = FastStringUtils.toCharArray(str);
            dcache.put(key, chars);
        }

        add(chars);
        return this;
    }

    public final CharBuf add(float d) {
        add(Float.toString(d));
        return this;
    }

    private Cache<Float, char[]> fcache;

    public final CharBuf addFloat(float d) {
        addFloat(Float.valueOf(d));
        return this;
    }

    public final CharBuf addFloat(Float key) {
        if (fcache == null) {
            fcache = new SimpleCache<Float, char[]>(20);
        }
        char[] chars = fcache.get(key);

        if (chars == null) {
            String str = Float.toString(key);
            chars = FastStringUtils.toCharArray(str);
            fcache.put(key, chars);
        }

        add(chars);

        return this;
    }

    public final CharBuf addChar(byte i) {
        add((char) i);
        return this;
    }

    public final CharBuf addChar(int i) {
        add((char) i);
        return this;
    }

    public final CharBuf addChar(short i) {
        add((char) i);
        return this;
    }

    public final CharBuf addChar(final char ch) {
        int _location = location;
        char[] _buffer = buffer;
        int _capacity = capacity;

        if (1 + _location > _capacity) {
            _buffer = Chr.grow(_buffer);
            _capacity = _buffer.length;
        }

        _buffer[_location] = ch;
        _location++;

        location = _location;
        buffer = _buffer;
        capacity = _capacity;
        return this;
    }

    public CharBuf addLine(String str) {
        add(str.toCharArray());
        add('\n');
        return this;
    }

    public CharBuf addLine(CharSequence str) {
        add(str.toString());
        add('\n');
        return this;
    }

    public CharBuf add(char[] chars) {
        if (chars.length + location > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + chars.length);
            capacity = buffer.length;
        }

        Chr._idx(buffer, location, chars);
        location += chars.length;
        return this;
    }

    public final CharBuf addChars(char[] chars) {
        if (chars.length + location > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + chars.length);
            capacity = buffer.length;
        }

        System.arraycopy(chars, 0, buffer, location, chars.length);
        location += chars.length;
        return this;
    }

    public final CharBuf addQuoted(char[] chars) {
        int _location = location;
        char[] _buffer = buffer;
        int _capacity = capacity;

        int sizeNeeded = chars.length + 2 + _location;
        if (sizeNeeded > _capacity) {
            _buffer = Chr.grow(_buffer, sizeNeeded * 2);
            _capacity = _buffer.length;
        }
        _buffer[_location] = '"';
        _location++;

        System.arraycopy(chars, 0, _buffer, _location, chars.length);

        _location += (chars.length);
        _buffer[_location] = '"';
        _location++;

        location = _location;
        buffer = _buffer;
        capacity = _capacity;
        return this;
    }

    public final CharBuf addJsonEscapedString(String jsonString) {
        return addJsonEscapedString(jsonString, false);
    }

    public final CharBuf addJsonEscapedString(String jsonString, boolean disableUnicodeEscaping) {
        char[] charArray = FastStringUtils.toCharArray(jsonString);
        return addJsonEscapedString(charArray, disableUnicodeEscaping);
    }

    private static boolean shouldEscape(int c, boolean disableUnicodeEscaping) {
        if (c < 32) { /* less than space is a control char */
            return true;
        } else if (c == 34) {  /* double quote */
            return true;
        } else if (c == 92) {  /* backslash */
            return true;
        } else if (!disableUnicodeEscaping && c > 126) {  /* non-ascii char range */
            return true;
        }

        return false;
    }

    private static boolean hasAnyJSONControlChars(final char[] charArray, boolean disableUnicodeEscaping) {
        int index = 0;
        char c;
        while (true) {
            c = charArray[index];
            if (shouldEscape(c, disableUnicodeEscaping)) {
                return true;
            }
            if (++index >= charArray.length) return false;
        }
    }

    public final CharBuf addJsonEscapedString(final char[] charArray) {
        return addJsonEscapedString(charArray, false);
    }

    public final CharBuf addJsonEscapedString(final char[] charArray, boolean disableUnicodeEscaping) {
        if (charArray.length == 0) return this;
        if (hasAnyJSONControlChars(charArray, disableUnicodeEscaping)) {
            return doAddJsonEscapedString(charArray, disableUnicodeEscaping);
        } else {
            return this.addQuoted(charArray);
        }
    }

    final byte[] encoded = new byte[2];

    final byte[] charTo = new byte[2];

    private CharBuf doAddJsonEscapedString(char[] charArray, boolean disableUnicodeEscaping) {
        char[] _buffer = buffer;
        int _location = this.location;

        final byte[] _encoded = encoded;

        final byte[] _charTo = charTo;
        /* We are making a bet that not all chars will be unicode. */
        int ensureThisMuch = charArray.length * 6 + 2;

        int sizeNeeded = (ensureThisMuch) + _location;
        if (sizeNeeded > capacity) {
            int growBy = Math.max((_buffer.length * 2), sizeNeeded);
            _buffer = Chr.grow(buffer, growBy);
            capacity = _buffer.length;
        }

        _buffer[_location] = '"';
        _location++;

        int index = 0;
        while (true) {
            char c = charArray[index];

            if (shouldEscape(c, disableUnicodeEscaping)) {
                   /* We are covering our bet with a safety net.
                      otherwise we would have to have 5x buffer
                      allocated for control chars */
                if (_location + 5 > _buffer.length) {
                    _buffer = Chr.grow(_buffer, 20);
                }

                switch (c) {
                    case '\"':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = '"';
                        _location++;
                        break;
                    case '\\':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = '\\';
                        _location++;
                        break;
                    //There is not requirement to escape solidus so we will not.
//                        case '/':
//                            _buffer[_location] = '\\';
//                            _location ++;
//                            _buffer[_location] =  '/';
//                            _location ++;
//                            break;

                    case '\b':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 'b';
                        _location++;
                        break;
                    case '\f':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 'f';
                        _location++;
                        break;
                    case '\n':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 'n';
                        _location++;
                        break;
                    case '\r':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 'r';
                        _location++;
                        break;
                    case '\t':
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 't';
                        _location++;
                        break;
                    default:
                        _buffer[_location] = '\\';
                        _location++;
                        _buffer[_location] = 'u';
                        _location++;
                        if (c <= 255) {
                            _buffer[_location] = '0';
                            _location++;
                            _buffer[_location] = '0';
                            _location++;
                            ByteScanner.encodeByteIntoTwoAsciiCharBytes(c, _encoded);
                            for (int b : _encoded) {
                                _buffer[_location] = (char) b;
                                _location++;
                            }
                        } else {
                            _charTo[1] = (byte) (c);
                            _charTo[0] = (byte) (c >>> 8);

                            for (int charByte : _charTo) {
                                ByteScanner.encodeByteIntoTwoAsciiCharBytes(charByte, _encoded);
                                for (int b : _encoded) {
                                    _buffer[_location] = (char) b;
                                    _location++;
                                }
                            }
                        }
                }
            } else {
                _buffer[_location] = c;
                _location++;
            }

            if (++index >= charArray.length) break;
        }
        _buffer[_location] = '"';
        _location++;

        buffer = _buffer;
        location = _location;

        return this;
    }

    public final CharBuf addJsonFieldName(String str) {
        return addJsonFieldName(str, false);
    }

    public final CharBuf addJsonFieldName(String str, boolean disableUnicodeEscaping) {
        return addJsonFieldName(FastStringUtils.toCharArray(str), disableUnicodeEscaping);
    }

    private static final char[] EMPTY_STRING_CHARS = Chr.array('"', '"');

    public final CharBuf addJsonFieldName(char[] chars) {
        return addJsonFieldName(chars, false);
    }

    public final CharBuf addJsonFieldName(char[] chars, boolean disableUnicodeEscaping) {
        if (chars.length > 0) {
            addJsonEscapedString(chars, disableUnicodeEscaping);
        } else {
            addChars(EMPTY_STRING_CHARS);
        }
        addChar(':');
        return this;
    }

    public final CharBuf addQuoted(String str) {
        final char[] chars = FastStringUtils.toCharArray(str);
        addQuoted(chars);
        return this;
    }

    public CharBuf add(char[] chars, final int length) {
        if (length + location < capacity) {
            Chr._idx(buffer, location, chars, length);
        } else {
            buffer = Chr.grow(buffer, buffer.length * 2 + length);
            Chr._idx(buffer, location, chars);
            capacity = buffer.length;
        }
        location += length;
        return this;
    }

    public CharBuf add(byte[] chars) {
        if (chars.length + location < capacity) {
            Chr._idx(buffer, location, chars);
        } else {
            buffer = Chr.grow(buffer, buffer.length * 2 + chars.length);
            Chr._idx(buffer, location, chars);
            capacity = buffer.length;
        }
        location += chars.length;
        return this;
    }

    public CharBuf add(byte[] bytes, int start, int end) {
        int charsLength = end - start;
        if (charsLength + location > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + charsLength);
        }
        Chr._idx(buffer, location, bytes, start, end);
        capacity = buffer.length;
        location += charsLength;
        return this;
    }

    public final CharBuf add(char ch) {
        if (1 + location < capacity) {
            buffer[location] = ch;
        } else {
            buffer = Chr.grow(buffer);
            buffer[location] = ch;
            capacity = buffer.length;
        }
        location += 1;
        return this;
    }

    @Override
    public int length() {
        return len();
    }

    @Override
    public char charAt(int index) {
        return buffer[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(buffer, start, end - start);
    }

    @Override
    public String toString() {
        return new String(buffer, 0, location);
    }

    public String toDebugString() {
        return "CharBuf{" +
                "capacity=" + capacity +
                ", location=" + location +
                '}';
    }

    public String toStringAndRecycle() {
        String str = new String(buffer, 0, location);
        location = 0;
        return str;
    }

    public int len() {
        return location;
    }

    public char[] toCharArray() {
        return this.buffer;
    }

    public void _len(int location) {
        this.location = location;
    }

    public char[] readForRecycle() {
        this.location = 0;
        return this.buffer;
    }

    public void recycle() {
        this.location = 0;
    }

    public double doubleValue() {
        return CharScanner.parseDouble(this.buffer, 0, location);
    }

    public float floatValue() {
        return CharScanner.parseFloat(this.buffer, 0, location);
    }

    public int intValue() {
        return CharScanner.parseIntFromTo(buffer, 0, location);
    }

    public long longValue() {
        return CharScanner.parseLongFromTo(buffer, 0, location);
    }

    public byte byteValue() {
        return (byte) intValue();
    }

    public short shortValue() {
        return (short) intValue();
    }

    public Number toIntegerWrapper() {
        if (CharScanner.isInteger(buffer, 0, location)) {
            return intValue();
        } else {
            return longValue();
        }
    }

    static final char[] nullChars = "null".toCharArray();

    public final void addNull() {
        this.add(nullChars);
    }

    public void removeLastChar() {
        if (location > 0) {
            location--;
        }
    }

    public void removeLastChar(char expect) {
        if (location == 0 || buffer[location-1] != expect) {
            return;
        }
        removeLastChar();
    }

    private Cache<BigDecimal, char[]> bigDCache;

    public CharBuf addBigDecimal(BigDecimal key) {
        if (bigDCache == null) {
            bigDCache = new SimpleCache<BigDecimal, char[]>(20);
        }
        char[] chars = bigDCache.get(key);

        if (chars == null) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray(str);
            bigDCache.put(key, chars);
        }

        add(chars);

        return this;
    }

    private Cache<BigInteger, char[]> bigICache;

    public CharBuf addBigInteger(BigInteger key) {
        if (bigICache == null) {
            bigICache = new SimpleCache<BigInteger, char[]>(20);
        }
        char[] chars = bigICache.get(key);

        if (chars == null) {
            String str = key.toString();
            chars = FastStringUtils.toCharArray(str);
            bigICache.put(key, chars);
        }

        add(chars);

        return this;
    }

    private Cache<Long, char[]> lcache;

    public final CharBuf addLong(long l) {
        addLong(Long.valueOf(l));
        return this;
    }

    public final CharBuf addLong(Long key) {
        if (lcache == null) {
            lcache = new SimpleCache<Long, char[]>(20);
        }
        char[] chars = lcache.get(key);

        if (chars == null) {
            String str = Long.toString(key);
            chars = FastStringUtils.toCharArray(str);
            lcache.put(key, chars);
        }

        add(chars);

        return this;
    }

    public final CharBuf decodeJsonString(char[] chars) {
        return decodeJsonString(chars, 0, chars.length);
    }

    public final CharBuf decodeJsonString(char[] chars, int start, int to) {
        int len = to - start;

        char[] buffer = this.buffer;
        int location = this.location;

        if (len > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + len);
            capacity = buffer.length;
        }

        for (int index = start; index < to; index++) {
            char c = chars[index];
            if (c == '\\') {
                if (index < to) {
                    index++;
                    c = chars[index];
                    switch (c) {

                        case 'n':
                            buffer[location++] = '\n';
                            break;

                        case '/':
                            buffer[location++] = '/';
                            break;

                        case '"':
                            buffer[location++] = '"';
                            break;

                        case 'f':
                            buffer[location++] = '\f';
                            break;

                        case 't':
                            buffer[location++] = '\t';
                            break;

                        case '\\':
                            buffer[location++] = '\\';
                            break;

                        case 'b':
                            buffer[location++] = '\b';
                            break;

                        case 'r':
                            buffer[location++] = '\r';
                            break;

                        case 'u':
                            if (index + 4 < to) {
                                String hex = new String(chars, index + 1, 4);
                                char unicode = (char) Integer.parseInt(hex, 16);
                                buffer[location++] = unicode;
                                index += 4;
                            }
                            break;

                        default:
                            throw new JsonException("Unable to decode string");
                    }
                }
            } else {
                buffer[location++] = c;
            }
        }

        this.buffer = buffer;
        this.location = location;

        return this;
    }
}


