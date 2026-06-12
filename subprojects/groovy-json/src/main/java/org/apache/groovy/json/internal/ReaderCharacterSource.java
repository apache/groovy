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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Buffered {@link CharacterSource} implementation backed by a {@link Reader}.
 */
public class ReaderCharacterSource implements CharacterSource {

    private static final int MAX_TOKEN_SIZE = 5;

    private final Reader reader;
    private final int readAheadSize;
    private int ch = -2;

    private boolean foundEscape;

    private final char[] readBuf;

    private int index;

    private int length;

    /**
     * Whether additional characters may still be read.
     */
    boolean more = true;
    private boolean done = false;

    /**
     * Creates a buffered character source.
     *
     * @param reader reader supplying characters
     * @param readAheadSize number of characters to read per buffer refill
     */
    public ReaderCharacterSource(final Reader reader, final int readAheadSize) {
        this.reader = reader;
        this.readBuf = new char[readAheadSize + MAX_TOKEN_SIZE];
        this.readAheadSize = readAheadSize;
    }

    /**
     * Creates a buffered character source with the default read-ahead size.
     *
     * @param reader reader supplying characters
     */
    public ReaderCharacterSource(final Reader reader) {
        this.reader = reader;
        this.readAheadSize = 10000;
        this.readBuf = new char[readAheadSize + MAX_TOKEN_SIZE];
    }

    /**
     * Creates a character source over an in-memory string.
     *
     * @param string JSON content to expose as characters
     */
    public ReaderCharacterSource(final String string) {
        this(new StringReader(string));
    }

    private void readForToken() {
        try {
            length += reader.read(readBuf, readBuf.length - MAX_TOKEN_SIZE, MAX_TOKEN_SIZE);
        } catch (IOException e) {
            Exceptions.handle(e);
        }
    }

    private void ensureBuffer() {
        try {
            if (index >= length && !done) {
                readNextBuffer();
            } else {
                more = !(done && index >= length);
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("ensureBuffer issue", readBuf, index, ch);
            Exceptions.handle(str, ex);
        }
    }

    private void readNextBuffer() throws IOException {
        length = reader.read(readBuf, 0, readAheadSize);

        index = 0;
        if (length == -1) {
            ch = -1;
            length = 0;
            more = false;
            done = true;
        } else {
            more = true;
        }
    }

    /**
     * Advances to the next character.
     *
     * @return next character code
     */
    @Override
    public final int nextChar() {
        ensureBuffer();
        return ch = readBuf[index++];
    }

    /**
     * Returns the current character without advancing.
     *
     * @return current character code
     */
    @Override
    public final int currentChar() {
        ensureBuffer();
        return readBuf[index];
    }

    /**
     * Checks whether another character is available.
     *
     * @return {@code true} when more input remains
     */
    @Override
    public final boolean hasChar() {
        ensureBuffer();
        return more;
    }

    /**
     * Consumes the supplied character sequence when it matches the current position.
     *
     * @param match character sequence to test
     * @return {@code true} when the sequence was consumed
     */
    @Override
    public final boolean consumeIfMatch(char[] match) {
        try {
            char[] _chars = readBuf;
            int i = 0;
            int idx = index;
            boolean ok = true;

            if (idx + match.length > length) {
                readForToken();
            }

            for (; i < match.length; i++, idx++) {
                ok &= (match[i] == _chars[idx]);
                if (!ok) break;
            }

            if (ok) {
                index = idx;
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("consumeIfMatch issue", readBuf, index, ch);
            return Exceptions.handle(boolean.class, str, ex);
        }
    }

    /**
     * Returns the current buffer index for diagnostics.
     *
     * @return current character index
     */
    @Override
    public final int location() {
        return index;
    }

    /**
     * Advances when possible and returns {@code -1} at end of input.
     *
     * @return next character code or {@code -1} when no more input remains
     */
    @Override
    public final int safeNextChar() {
        try {
            ensureBuffer();
            return index + 1 < readBuf.length ? readBuf[index++] : -1;
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("safeNextChar issue", readBuf, index, ch);
            return Exceptions.handle(int.class, str, ex);
        }
    }

    private static final char[] EMPTY_CHARS = new char[0];

    /**
     * Collects characters until the next unescaped match character.
     *
     * @param match terminating character to search for
     * @param esc escape character that shields the following character
     * @return collected characters between the current position and the terminator
     */
    @Override
    public char[] findNextChar(int match, int esc) {
        try {
            ensureBuffer();

            foundEscape = false;
            if (readBuf[index] == '"') {
                index++;
                return EMPTY_CHARS;
            }

            int start = index;

            char[] results = null;
            boolean foundEnd = false;
            boolean wasEscaped = false;
            while (!foundEnd) {
                for (; index < length; index++) {
                    ch = readBuf[index];
                    if (wasEscaped) {
                        wasEscaped = false;
                    } else if (ch == match) {
                        foundEnd = true;
                        break;
                    } else if (ch == esc) {
                        foundEscape = true;
                        wasEscaped = true;
                    }
                }

                if (results != null) {
                    results = Chr.add(results, ArrayUtils.copyRange(readBuf, start, index));
                }
                else {
                    results = ArrayUtils.copyRange(readBuf, start, index);
                }

                ensureBuffer();

                // Reset start if new buffer
                if (index == 0) {
                    start = 0;
                }

                // Exit early if we run out of data
                if (done) {
                    break;
                }
            }

            // done will only be true if we ran out of data without seeing the match character
            if (done) {
                return Exceptions.die(char[].class, "Unable to find close char " + (char)match + ": " + new String(results));
            } else {
                index++;
                return results;
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("findNextChar issue", readBuf, index, ch);
            return Exceptions.handle(char[].class, str, ex);
        }
    }

    /**
     * Reports whether the last string scan encountered an escape sequence.
     *
     * @return {@code true} when an escape was seen
     */
    @Override
    public boolean hadEscape() {
        return foundEscape;
    }

    /**
     * Advances past whitespace characters.
     */
    @Override
    public void skipWhiteSpace() {
        try {
            index = CharScanner.skipWhiteSpace(readBuf, index, length);
            if (index >= length && more) {
                ensureBuffer();

                skipWhiteSpace();
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("skipWhiteSpace issue", readBuf, index, ch);
            Exceptions.handle(str, ex);
        }
    }

    /**
     * Reads a numeric token from the current position.
     *
     * @return characters that form the numeric token
     */
    @Override
    public char[] readNumber() {
        try {
            ensureBuffer();

            char[] results = CharScanner.readNumber(readBuf, index, length);
            index += results.length;

            if (index >= length && more) {
                ensureBuffer();
                if (length != 0) {
                    char[] results2 = readNumber();
                    return Chr.add(results, results2);
                } else {
                    return results;
                }
            } else {
                return results;
            }
        } catch (Exception ex) {
            String str = CharScanner.errorDetails("readNumber issue", readBuf, index, ch);
            return Exceptions.handle(char[].class, str, ex);
        }
    }

    /**
     * Builds an error message using the current reader context.
     *
     * @param message parser-specific message
     * @return formatted error details
     */
    @Override
    public String errorDetails(String message) {
        return CharScanner.errorDetails(message, readBuf, index, ch);
    }
}
