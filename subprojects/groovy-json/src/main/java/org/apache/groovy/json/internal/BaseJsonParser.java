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
import groovy.json.JsonParser;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base JSON parser.
 * Scaled down version of Boon JsonParser with features
 * removed that are JDK 1.7 dependent or Groovy duplicated functionality.
 */
public abstract class BaseJsonParser implements JsonParser {

    /**
     * Character code for the {@code :} name/value separator.
     */
    protected static final int COLON = ':';
    /**
     * Character code for the {@code ,} element separator.
     */
    protected static final int COMMA = ',';
    /**
     * Character code for the closing object delimiter {@code }}.
     */
    protected static final int CLOSED_CURLY = '}';
    /**
     * Character code for the closing array delimiter {@code ]}.
     */
    protected static final int CLOSED_BRACKET = ']';

    /**
     * Lowercase exponent marker used in JSON numbers.
     */
    protected static final int LETTER_E = 'e';
    /**
     * Uppercase exponent marker used in JSON numbers.
     */
    protected static final int LETTER_BIG_E = 'E';

    /**
     * Character code for the minus sign in number literals.
     */
    protected static final int MINUS = '-';
    /**
     * Character code for the plus sign in exponent literals.
     */
    protected static final int PLUS = '+';

    /**
     * Character code for the decimal point in number literals.
     */
    protected static final int DECIMAL_POINT = '.';

    /**
     * Character code for digit {@code 0}.
     */
    protected static final int ALPHA_0 = '0';
    /**
     * Character code for digit {@code 1}.
     */
    protected static final int ALPHA_1 = '1';
    /**
     * Character code for digit {@code 2}.
     */
    protected static final int ALPHA_2 = '2';
    /**
     * Character code for digit {@code 3}.
     */
    protected static final int ALPHA_3 = '3';
    /**
     * Character code for digit {@code 4}.
     */
    protected static final int ALPHA_4 = '4';
    /**
     * Character code for digit {@code 5}.
     */
    protected static final int ALPHA_5 = '5';
    /**
     * Character code for digit {@code 6}.
     */
    protected static final int ALPHA_6 = '6';
    /**
     * Character code for digit {@code 7}.
     */
    protected static final int ALPHA_7 = '7';
    /**
     * Character code for digit {@code 8}.
     */
    protected static final int ALPHA_8 = '8';
    /**
     * Character code for digit {@code 9}.
     */
    protected static final int ALPHA_9 = '9';

    /**
     * Character code for the JSON string delimiter {@code "}.
     */
    protected static final int DOUBLE_QUOTE = '"';

    /**
     * Character code for the JSON escape marker {@code \}.
     */
    protected static final int ESCAPE = '\\';

    /**
     * Whether parsed object keys should be interned.
     */
    protected static final boolean internKeys = Boolean.parseBoolean(System.getProperty("groovy.json.internKeys", "false"));
    /**
     * Cache used when key interning is enabled.
     */
    protected static final ConcurrentHashMap<String, String> internedKeysCache;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * Charset used for byte-based input when no override is supplied.
     */
    protected String charset = UTF_8.name();

    private CharBuf fileInputBuf;

    /**
     * Initial buffer size used when reading character streams.
     */
    protected int bufSize = 256;

    static {
        if (internKeys) {
            internedKeysCache = new ConcurrentHashMap<String, String>();
        } else {
            internedKeysCache = null;
        }
    }

    /**
     * Builds a readable description of a character code for parser errors.
     *
     * @param c character code to describe
     * @return human-readable description of {@code c}
     */
    protected String charDescription(int c) {
        String charString;
        if (c == ' ') {
            charString = "[SPACE]";
        } else if (c == '\t') {
            charString = "[TAB]";

        } else if (c == '\n') {
            charString = "[NEWLINE]";

        } else {
            charString = "'" + (char) c + "'";
        }

        charString = charString + " with an int value of " + ((int) c);
        return charString;
    }

    /**
     * Sets the default charset used for byte and stream input.
     *
     * @param charset charset name to use for subsequent parsing
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Parses a JSON string.
     *
     * @param jsonString JSON text to parse
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(String jsonString) {
        return parse(FastStringUtils.toCharArray(jsonString));
    }

    /**
     * Parses JSON bytes using the parser's configured charset.
     *
     * @param bytes JSON bytes to parse
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(byte[] bytes) {
        return parse(bytes, charset);
    }

    /**
     * Parses JSON bytes using the supplied charset.
     *
     * @param bytes JSON bytes to parse
     * @param charset charset name to use
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(byte[] bytes, String charset) {
        try {
            return parse(new String(bytes, charset));
        } catch (UnsupportedEncodingException e) {
            return Exceptions.handle(Object.class, e);
        }
    }

    /**
     * Parses JSON from a character sequence.
     *
     * @param charSequence JSON text to parse
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(CharSequence charSequence) {
        return parse(FastStringUtils.toCharArray(charSequence));
    }

    /**
     * Parses JSON from a reader.
     *
     * @param reader reader supplying JSON content
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(Reader reader) {
        fileInputBuf = IO.read(reader, fileInputBuf, bufSize);
        return parse(fileInputBuf.readForRecycle());
    }

    /**
     * Parses JSON from a byte stream using the configured charset.
     *
     * @param input input stream supplying JSON bytes
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(InputStream input) {
        return parse(input, charset);
    }

    /**
     * Parses JSON from a byte stream using the supplied charset.
     *
     * @param input input stream supplying JSON bytes
     * @param charset charset name to use
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(InputStream input, String charset) {
        try {
            return parse(new InputStreamReader(input, charset));
        } catch (UnsupportedEncodingException e) {
            return Exceptions.handle(Object.class, e);
        }
    }

    /**
     * Parses JSON from a file.
     *
     * @param file file containing JSON content
     * @param charset charset name to use, or the platform default when blank
     * @return parsed Groovy JSON value
     */
    @Override
    public Object parse(File file, String charset) {
        Reader reader = null;
        try {
            if (charset == null || charset.isEmpty()) {
                reader = ResourceGroovyMethods.newReader(file);
            } else {
                reader = ResourceGroovyMethods.newReader(file, charset);
            }
            return parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process file: " + file.getPath(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }

    /**
     * Checks whether a character can appear in a decimal JSON number.
     *
     * @param currentChar character code to test
     * @return {@code true} for decimal-number markers
     */
    protected static boolean isDecimalChar(int currentChar) {
        return switch (currentChar) {
            case MINUS, PLUS, LETTER_E, LETTER_BIG_E, DECIMAL_POINT -> true;
            default -> false;
        };
    }

    /**
     * Checks whether a character terminates the current JSON value.
     *
     * @param c character code to test
     * @return {@code true} when {@code c} is a value delimiter
     */
    protected static boolean isDelimiter(int c) {
        return c == COMMA || c == CLOSED_CURLY || c == CLOSED_BRACKET;
    }

    /**
     * Checks whether a character code is an ASCII digit.
     *
     * @param c character code to test
     * @return {@code true} when {@code c} is between {@code 0} and {@code 9}
     */
    protected static final boolean isNumberDigit(int c) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }

    /**
     * Checks whether a character code is a double quote.
     *
     * @param c character code to test
     * @return {@code true} when {@code c} is {@code "}
     */
    protected static final boolean isDoubleQuote(int c) {
        return c == DOUBLE_QUOTE;
    }

    /**
     * Checks whether a character code is the JSON escape marker.
     *
     * @param c character code to test
     * @return {@code true} when {@code c} is {@code \}
     */
    protected static final boolean isEscape(int c) {
        return c == ESCAPE;
    }

    /**
     * Scans a string body until it finds an escape or the closing quote.
     *
     * @param array source buffer
     * @param index index at which to start scanning
     * @param indexHolder single-item holder updated with the stop index
     * @return {@code true} when an escape character is encountered before the closing quote
     */
    protected static boolean hasEscapeChar(char[] array, int index, int[] indexHolder) {
        char currentChar;
        for (; index < array.length; index++) {
            currentChar = array[index];
            if (isDoubleQuote(currentChar)) {
                indexHolder[0] = index;
                return false;
            } else if (isEscape(currentChar)) {
                indexHolder[0] = index;
                return true;
            }
        }

        indexHolder[0] = index;
        return false;
    }

    /**
     * Reusable holder for string-scan indexes.
     */
    int[] indexHolder = new int[1];

    /**
     * Finds the closing quote of a JSON string while honoring escapes.
     *
     * @param array source buffer
     * @param index index at which to start scanning
     * @return index of the terminating quote or the buffer end
     */
    protected static int findEndQuote(final char[] array, int index) {
        char currentChar;
        boolean escape = false;

        for (; index < array.length; index++) {
            currentChar = array[index];
            if (isDoubleQuote(currentChar)) {
                if (!escape) {
                    break;
                }
            }
            escape = isEscape(currentChar) && !escape;
        }
        return index;
    }
}
