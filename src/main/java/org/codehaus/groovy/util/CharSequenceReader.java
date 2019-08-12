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
package org.codehaus.groovy.util;

import java.io.Reader;
import java.io.Serializable;

/**
 * {@link Reader} implementation that can read from String, StringBuffer,
 * StringBuilder, CharBuffer or GString.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 * <p>
 * <strong>Note:</strong> This class is mostly a copy from Commons IO and
 * is intended for internal Groovy usage only. It may be deprecated and
 * removed from Groovy at a faster pace than other classes. If you need this
 * functionality in your Groovy programs, we recommend using the Commons IO
 * equivalent directly.
 */
public class CharSequenceReader extends Reader implements Serializable {
    private static final long serialVersionUID = -6661279371843310693L;
    private final CharSequence charSequence;
    private int idx;
    private int mark;
    private static final int EOF = -1;

    /**
     * Construct a new instance with the specified character sequence.
     *
     * @param charSequence The character sequence, may be {@code null}
     */
    public CharSequenceReader(final CharSequence charSequence) {
        this.charSequence = charSequence != null ? charSequence : "";
    }

    /**
     * Close resets the reader back to the start and removes any marked position.
     */
    @Override
    public void close() {
        idx = 0;
        mark = 0;
    }

    /**
     * Mark the current position.
     *
     * @param readAheadLimit ignored
     */
    @Override
    public void mark(final int readAheadLimit) {
        mark = idx;
    }

    /**
     * Mark is supported (returns true).
     *
     * @return {@code true}
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Read a single character.
     *
     * @return the next character from the character sequence
     * or -1 if the end has been reached.
     */
    @Override
    public int read() {
        if (idx >= charSequence.length()) {
            return EOF;
        } else {
            return charSequence.charAt(idx++);
        }
    }

    /**
     * Read the sepcified number of characters into the array.
     *
     * @param array The array to store the characters in
     * @param offset The starting position in the array to store
     * @param length The maximum number of characters to read
     * @return The number of characters read or -1 if there are
     * no more
     */
    @Override
    public int read(final char[] array, final int offset, final int length) {
        if (idx >= charSequence.length()) {
            return EOF;
        }
        if (array == null) {
            throw new NullPointerException("Character array is missing");
        }
        if (length < 0 || offset < 0 || offset + length > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length +
                    ", offset=" + offset + ", length=" + length);
        }
        int count = 0;
        for (int i = 0; i < length; i++) {
            final int c = read();
            if (c == EOF) {
                return count;
            }
            array[offset + i] = (char)c;
            count++;
        }
        return count;
    }

    /**
     * Reset the reader to the last marked position (or the beginning if
     * mark has not been called).
     */
    @Override
    public void reset() {
        idx = mark;
    }

    /**
     * Skip the specified number of characters.
     *
     * @param n The number of characters to skip
     * @return The actual number of characters skipped
     */
    @Override
    public long skip(final long n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Number of characters to skip is less than zero: " + n);
        }
        if (idx >= charSequence.length()) {
            return EOF;
        }
        final int dest = (int)Math.min(charSequence.length(), idx + n);
        final int count = dest - idx;
        idx = dest;
        return count;
    }

    /**
     * Return a String representation of the underlying
     * character sequence.
     *
     * @return The contents of the character sequence
     */
    @Override
    public String toString() {
        return charSequence.toString();
    }
}