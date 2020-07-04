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

    protected static final int COLON = ':';
    protected static final int COMMA = ',';
    protected static final int CLOSED_CURLY = '}';
    protected static final int CLOSED_BRACKET = ']';

    protected static final int LETTER_E = 'e';
    protected static final int LETTER_BIG_E = 'E';

    protected static final int MINUS = '-';
    protected static final int PLUS = '+';

    protected static final int DECIMAL_POINT = '.';

    protected static final int ALPHA_0 = '0';
    protected static final int ALPHA_1 = '1';
    protected static final int ALPHA_2 = '2';
    protected static final int ALPHA_3 = '3';
    protected static final int ALPHA_4 = '4';
    protected static final int ALPHA_5 = '5';
    protected static final int ALPHA_6 = '6';
    protected static final int ALPHA_7 = '7';
    protected static final int ALPHA_8 = '8';
    protected static final int ALPHA_9 = '9';

    protected static final int DOUBLE_QUOTE = '"';

    protected static final int ESCAPE = '\\';

    protected static final boolean internKeys = Boolean.parseBoolean(System.getProperty("groovy.json.internKeys", "false"));
    protected static final ConcurrentHashMap<String, String> internedKeysCache;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    protected String charset = UTF_8.name();

    private CharBuf fileInputBuf;

    protected int bufSize = 256;

    static {
        if (internKeys) {
            internedKeysCache = new ConcurrentHashMap<String, String>();
        } else {
            internedKeysCache = null;
        }
    }

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

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public Object parse(String jsonString) {
        return parse(FastStringUtils.toCharArray(jsonString));
    }

    public Object parse(byte[] bytes) {
        return parse(bytes, charset);
    }

    public Object parse(byte[] bytes, String charset) {
        try {
            return parse(new String(bytes, charset));
        } catch (UnsupportedEncodingException e) {
            return Exceptions.handle(Object.class, e);
        }
    }

    public Object parse(CharSequence charSequence) {
        return parse(FastStringUtils.toCharArray(charSequence));
    }

    public Object parse(Reader reader) {
        fileInputBuf = IO.read(reader, fileInputBuf, bufSize);
        return parse(fileInputBuf.readForRecycle());
    }

    public Object parse(InputStream input) {
        return parse(input, charset);
    }

    public Object parse(InputStream input, String charset) {
        try {
            return parse(new InputStreamReader(input, charset));
        } catch (UnsupportedEncodingException e) {
            return Exceptions.handle(Object.class, e);
        }
    }

    public Object parse(File file, String charset) {
        Reader reader = null;
        try {
            if (charset == null || charset.length() == 0) {
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

    protected static boolean isDecimalChar(int currentChar) {
        switch (currentChar) {
            case MINUS:
            case PLUS:
            case LETTER_E:
            case LETTER_BIG_E:
            case DECIMAL_POINT:
                return true;
        }
        return false;
    }

    protected static boolean isDelimiter(int c) {
        return c == COMMA || c == CLOSED_CURLY || c == CLOSED_BRACKET;
    }

    protected static final boolean isNumberDigit(int c) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }

    protected static final boolean isDoubleQuote(int c) {
        return c == DOUBLE_QUOTE;
    }

    protected static final boolean isEscape(int c) {
        return c == ESCAPE;
    }

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

    int[] indexHolder = new int[1];

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
