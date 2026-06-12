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

/**
 * Growable character buffer used by the JSON internals.
 */
public class CharBuf extends Writer implements CharSequence {

    /** Current backing-array capacity. */
    protected int capacity = 16;
    /** Next write position in {@link #buffer}. */
    protected int location = 0;

    /** Backing storage for buffered characters. */
    protected char[] buffer;

    /** Wraps an existing character buffer. */
    public CharBuf(char[] buffer) {
        __init__(buffer);
    }

    private void __init__(char[] buffer) {
        this.buffer = buffer;
        this.capacity = buffer.length;
    }

    /** Decodes UTF-8 bytes into the backing character buffer. */
    public CharBuf(byte[] bytes) {
        this.buffer = null;
        String str = new String(bytes, StandardCharsets.UTF_8);
        __init__(FastStringUtils.toCharArray(str));
    }

    /** Creates a buffer sized exactly for the requested capacity. */
    public static CharBuf createExact(final int capacity) {
        return new CharBuf(capacity) {
            /** {@inheritDoc} */
            @Override
            public CharBuf add(char[] chars) {
                Chr._idx(buffer, location, chars);
                location += chars.length;
                return this;
            }
        };
    }

    /** Creates a growable buffer with the supplied capacity. */
    public static CharBuf create(int capacity) {
        return new CharBuf(capacity);
    }

    /** Wraps the supplied character buffer. */
    public static CharBuf create(char[] buffer) {
        return new CharBuf(buffer);
    }

    /** Creates a growable buffer with the supplied initial capacity. */
    protected CharBuf(int capacity) {
        this.capacity = capacity;
        init();
    }

    /** Creates a growable buffer with the default capacity. */
    protected CharBuf() {
        init();
    }

    /** {@inheritDoc} */
    @Override
    public void write(char[] cbuf, int off, int len) {
        if (off == 0 && cbuf.length == len) {
            this.add(cbuf);
        } else {
            char[] buffer = ArrayUtils.copyRange(cbuf, off, off + len);
            this.add(buffer);
        }
    }

    /** No-op for this in-memory buffer. */
    @Override
    public void flush() throws IOException {
    }

    /** No-op for this in-memory buffer. */
    @Override
    public void close() throws IOException {
    }

    /** Allocates the backing array from the current capacity. */
    public void init() {
        buffer = new char[capacity];
    }

    /** Appends a string. */
    public final CharBuf add(String str) {
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    /** Appends a string. */
    public final CharBuf addString(String str) {
        add(FastStringUtils.toCharArray(str));
        return this;
    }

    /** Appends an int value. */
    public final CharBuf add(int i) {
        add(Integer.toString(i));
        return this;
    }

    private Cache<Integer, char[]> icache;

    /** Appends an int value using cached character data when available. */
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

    /** Appends an {@link Integer} using cached character data. */
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

    /** Cached characters for the JSON literal {@code true}. */
    final char[] trueChars = "true".toCharArray();
    /** Cached characters for the JSON literal {@code false}. */
    final char[] falseChars = "false".toCharArray();

    /** Appends a boolean value using cached JSON literals. */
    public final CharBuf add(boolean b) {
        addChars(b ? trueChars : falseChars);
        return this;
    }

    /** Appends a boolean value. */
    public final CharBuf addBoolean(boolean b) {
        add(Boolean.toString(b));
        return this;
    }

    /** Appends a byte value. */
    public final CharBuf add(byte i) {
        add(Byte.toString(i));
        return this;
    }

    /** Appends a byte value as a character digit sequence. */
    public final CharBuf addByte(byte i) {
        addInt(i);
        return this;
    }

    /** Appends a short value. */
    public final CharBuf add(short i) {
        add(Short.toString(i));
        return this;
    }

    /** Appends a short value as a character digit sequence. */
    public final CharBuf addShort(short i) {
        addInt(i);
        return this;
    }

    /** Appends a long value. */
    public final CharBuf add(long l) {
        add(Long.toString(l));
        return this;
    }

    /** Appends a double value. */
    public final CharBuf add(double d) {
        add(Double.toString(d));
        return this;
    }

    private Cache<Double, char[]> dcache;

    /** Appends a double value using cached character data when available. */
    public final CharBuf addDouble(double d) {
        addDouble(Double.valueOf(d));
        return this;
    }

    /** Appends a {@link Double} using cached character data. */
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

    /** Appends a float value. */
    public final CharBuf add(float d) {
        add(Float.toString(d));
        return this;
    }

    private Cache<Float, char[]> fcache;

    /** Appends a float value using cached character data when available. */
    public final CharBuf addFloat(float d) {
        addFloat(Float.valueOf(d));
        return this;
    }

    /** Appends a {@link Float} using cached character data. */
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

    /** Appends a byte as a single character. */
    public final CharBuf addChar(byte i) {
        add((char) i);
        return this;
    }

    /** Appends an int as a single character. */
    public final CharBuf addChar(int i) {
        add((char) i);
        return this;
    }

    /** Appends a short as a single character. */
    public final CharBuf addChar(short i) {
        add((char) i);
        return this;
    }

    /** Appends a character. */
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

    /** Appends a line followed by a newline character. */
    public CharBuf addLine(String str) {
        add(str.toCharArray());
        add('\n');
        return this;
    }

    /** Appends a character sequence followed by a newline character. */
    public CharBuf addLine(CharSequence str) {
        add(str.toString());
        add('\n');
        return this;
    }

    /** Appends a character array. */
    public CharBuf add(char[] chars) {
        if (chars.length + location > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + chars.length);
            capacity = buffer.length;
        }

        Chr._idx(buffer, location, chars);
        location += chars.length;
        return this;
    }

    /** Appends a character array using {@link System#arraycopy(Object, int, Object, int, int)}. */
    public final CharBuf addChars(char[] chars) {
        if (chars.length + location > capacity) {
            buffer = Chr.grow(buffer, buffer.length * 2 + chars.length);
            capacity = buffer.length;
        }

        System.arraycopy(chars, 0, buffer, location, chars.length);
        location += chars.length;
        return this;
    }

    /** Appends a quoted character array without escaping. */
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

    /** Appends a quoted JSON string with escaping enabled. */
    public final CharBuf addJsonEscapedString(String jsonString) {
        return addJsonEscapedString(jsonString, false);
    }

    /** Appends a quoted JSON string. */
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

    /** Appends a quoted JSON string with escaping enabled. */
    public final CharBuf addJsonEscapedString(final char[] charArray) {
        return addJsonEscapedString(charArray, false);
    }

    /** Appends a quoted JSON string from a character array. */
    public final CharBuf addJsonEscapedString(final char[] charArray, boolean disableUnicodeEscaping) {
        if (charArray.length == 0) return this;
        if (hasAnyJSONControlChars(charArray, disableUnicodeEscaping)) {
            return doAddJsonEscapedString(charArray, disableUnicodeEscaping);
        } else {
            return this.addQuoted(charArray);
        }
    }

    /** Scratch buffer used while encoding hexadecimal escape bytes. */
    final byte[] encoded = new byte[2];

    /** Scratch buffer used to split a UTF-16 character into bytes. */
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
                    //There is no requirement to escape solidus so we will not.
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

    /** Appends a quoted JSON field name followed by a colon. */
    public final CharBuf addJsonFieldName(String str) {
        return addJsonFieldName(str, false);
    }

    /** Appends a quoted JSON field name followed by a colon. */
    public final CharBuf addJsonFieldName(String str, boolean disableUnicodeEscaping) {
        return addJsonFieldName(FastStringUtils.toCharArray(str), disableUnicodeEscaping);
    }

    private static final char[] EMPTY_STRING_CHARS = Chr.array('"', '"');

    /** Appends a quoted JSON field name followed by a colon. */
    public final CharBuf addJsonFieldName(char[] chars) {
        return addJsonFieldName(chars, false);
    }

    /** Appends a quoted JSON field name followed by a colon. */
    public final CharBuf addJsonFieldName(char[] chars, boolean disableUnicodeEscaping) {
        if (chars.length > 0) {
            addJsonEscapedString(chars, disableUnicodeEscaping);
        } else {
            addChars(EMPTY_STRING_CHARS);
        }
        addChar(':');
        return this;
    }

    /** Appends a quoted string without escaping. */
    public final CharBuf addQuoted(String str) {
        final char[] chars = FastStringUtils.toCharArray(str);
        addQuoted(chars);
        return this;
    }

    /** Appends a fixed-length prefix of a character array. */
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

    /** Appends a byte array as characters. */
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

    /** Appends a byte subrange as characters. */
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

    /** Appends a single character. */
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

    /** {@inheritDoc} */
    @Override
    public int length() {
        return len();
    }

    /** {@inheritDoc} */
    @Override
    public char charAt(int index) {
        return buffer[index];
    }

    /** {@inheritDoc} */
    @Override
    public CharSequence subSequence(int start, int end) {
        return new String(buffer, start, end - start);
    }

    /** Returns the buffered characters as a string. */
    @Override
    public String toString() {
        return new String(buffer, 0, location);
    }

    /** Returns a debug view of the current buffer state. */
    public String toDebugString() {
        return "CharBuf{" +
                "capacity=" + capacity +
                ", location=" + location +
                '}';
    }

    /** Returns the buffered text and resets the write position. */
    public String toStringAndRecycle() {
        String str = new String(buffer, 0, location);
        location = 0;
        return str;
    }

    /** Returns the current buffer length. */
    public int len() {
        return location;
    }

    /** Returns the backing character array. */
    public char[] toCharArray() {
        return this.buffer;
    }

    /** Sets the current logical buffer length. */
    public void _len(int location) {
        this.location = location;
    }

    /** Returns the backing array and resets the write position. */
    public char[] readForRecycle() {
        this.location = 0;
        return this.buffer;
    }

    /** Resets the write position without clearing the backing array. */
    public void recycle() {
        this.location = 0;
    }

    /** Parses the buffered characters as a {@code double}. */
    public double doubleValue() {
        return CharScanner.parseDouble(this.buffer, 0, location);
    }

    /** Parses the buffered characters as a {@code float}. */
    public float floatValue() {
        return CharScanner.parseFloat(this.buffer, 0, location);
    }

    /** Parses the buffered characters as an {@code int}. */
    public int intValue() {
        return CharScanner.parseIntFromTo(buffer, 0, location);
    }

    /** Parses the buffered characters as a {@code long}. */
    public long longValue() {
        return CharScanner.parseLongFromTo(buffer, 0, location);
    }

    /** Parses the buffered characters as a {@code byte}. */
    public byte byteValue() {
        return (byte) intValue();
    }

    /** Parses the buffered characters as a {@code short}. */
    public short shortValue() {
        return (short) intValue();
    }

    /** Parses the buffered characters as either an {@code Integer} or {@code Long}. */
    public Number toIntegerWrapper() {
        if (CharScanner.isInteger(buffer, 0, location)) {
            return intValue();
        } else {
            return longValue();
        }
    }

    /** Cached characters for the JSON literal {@code null}. */
    static final char[] nullChars = "null".toCharArray();

    /** Appends the JSON literal {@code null}. */
    public final void addNull() {
        this.add(nullChars);
    }

    /** Removes the last character when the buffer is not empty. */
    public void removeLastChar() {
        if (location > 0) {
            location--;
        }
    }

    /** Removes the last character when it matches the expected value. */
    public void removeLastChar(char expect) {
        if (location == 0 || buffer[location-1] != expect) {
            return;
        }
        removeLastChar();
    }

    private Cache<BigDecimal, char[]> bigDCache;

    /** Appends a {@link BigDecimal} using cached character data. */
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

    /** Appends a {@link BigInteger} using cached character data. */
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

    /** Appends a long value using cached character data when available. */
    public final CharBuf addLong(long l) {
        addLong(Long.valueOf(l));
        return this;
    }

    /** Appends a {@link Long} using cached character data. */
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

    /** Decodes a JSON string fragment into this buffer. */
    public final CharBuf decodeJsonString(char[] chars) {
        return decodeJsonString(chars, 0, chars.length);
    }

    /** Decodes a JSON string subrange into this buffer. */
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
