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

import static org.apache.groovy.json.internal.Exceptions.die;
import static org.apache.groovy.json.internal.Exceptions.handle;

public class CharScanner {

    protected static final int COMMA = ',';
    protected static final int CLOSED_CURLY = '}';
    protected static final int CLOSED_BRACKET = ']';
    protected static final int LETTER_E = 'e';
    protected static final int LETTER_BIG_E = 'E';
    protected static final int DECIMAL_POINT = '.';
    protected static final int ALPHA_0 = '0';
    protected static final int ALPHA_9 = '9';
    protected static final int MINUS = '-';
    protected static final int PLUS = '+';

    static final String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE);
    static final String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);
    static final String MIN_INT_STR_NO_SIGN = String.valueOf(Integer.MIN_VALUE);
    static final String MAX_INT_STR = String.valueOf(Integer.MAX_VALUE);

    private static double powersOf10[] = {
            1.0,
            10.0,
            100.0,
            1000.0,
            10000.0,
            100000.0,
            1000000.0,
            10000000.0,
            100000000.0,
            1000000000.0,
            10000000000.0,
            100000000000.0,
            1000000000000.0,
            10000000000000.0,
            100000000000000.0,
            1000000000000000.0,
            10000000000000000.0,
            100000000000000000.0,
            1000000000000000000.0,
    };

    public static boolean isDigit(int c) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }

    public static boolean isDecimalDigit(int c) {
        return isDigit(c) || isDecimalChar(c);
    }

    public static boolean isDecimalChar(int currentChar) {
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

    public static boolean hasDecimalChar(char[] chars, boolean negative) {
        int index = 0;

        if (negative) index++;

        for (; index < chars.length; index++) {
            switch (chars[index]) {
                case MINUS:
                case PLUS:
                case LETTER_E:
                case LETTER_BIG_E:
                case DECIMAL_POINT:
                    return true;
            }
        }
        return false;
    }

    public static boolean isLong(char[] digitChars) {
        return isLong(digitChars, 0, digitChars.length);
    }

    public static boolean isLong(char[] digitChars, int offset, int len) {
        String cmpStr = digitChars[offset] == '-' ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int cmpLen = cmpStr.length();
        if (len < cmpLen) return true;
        if (len > cmpLen) return false;

        for (int i = 0; i < cmpLen; ++i) {
            int diff = digitChars[offset + i] - cmpStr.charAt(i);
            if (diff != 0) {
                return (diff < 0);
            }
        }
        return true;
    }

    public static boolean isInteger(char[] digitChars) {
        return isInteger(digitChars, 0, digitChars.length);
    }

    public static boolean isInteger(char[] digitChars, int offset, int len) {
        String cmpStr = (digitChars[offset] == '-') ? MIN_INT_STR_NO_SIGN : MAX_INT_STR;
        int cmpLen = cmpStr.length();
        if (len < cmpLen) return true;
        if (len > cmpLen) return false;

        for (int i = 0; i < cmpLen; ++i) {
            int diff = digitChars[offset + i] - cmpStr.charAt(i);
            if (diff != 0) {
                return (diff < 0);
            }
        }
        return true;
    }

    public static int parseInt(char[] digitChars) {
        return parseIntFromTo(digitChars, 0, digitChars.length);
    }

    public static int parseIntFromTo(char[] digitChars, int offset, int to) {
        try {
            int num;
            boolean negative = false;
            char c = digitChars[offset];
            if (c == '-') {
                offset++;
                negative = true;
            }
            if (offset >= to) {
                die();
            }
            num = (digitChars[offset] - '0');
            if (++offset < to) {
                num = (num * 10) + (digitChars[offset] - '0');
                if (++offset < to) {
                    num = (num * 10) + (digitChars[offset] - '0');
                    if (++offset < to) {
                        num = (num * 10) + (digitChars[offset] - '0');
                        if (++offset < to) {
                            num = (num * 10) + (digitChars[offset] - '0');
                            if (++offset < to) {
                                num = (num * 10) + (digitChars[offset] - '0');
                                if (++offset < to) {
                                    num = (num * 10) + (digitChars[offset] - '0');
                                    if (++offset < to) {
                                        num = (num * 10) + (digitChars[offset] - '0');
                                        if (++offset < to) {
                                            num = (num * 10) + (digitChars[offset] - '0');
                                            if (++offset < to) {
                                                num = (num * 10) + (digitChars[offset] - '0');
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return negative ? num * -1 : num;
        } catch (Exception ex) {
            return handle(int.class, ex);
        }
    }

    public static int parseIntFromToIgnoreDot(char[] digitChars, int offset, int to) {
        int num;
        boolean negative = false;
        char c = digitChars[offset];
        if (c == '-') {
            offset++;
            negative = true;
        }
        if (offset >= to) {
            die();
        }
        c = digitChars[offset];
        num = (c - '0');
        offset++;

        for (; offset < to; offset++) {
            c = digitChars[offset];
            if (c != '.') {
                num = (num * 10) + (c - '0');
            }
        }

        return negative ? num * -1 : num;
    }

    public static long parseLongFromToIgnoreDot(char[] digitChars, int offset, int to) {
        long num;
        boolean negative = false;
        char c = digitChars[offset];
        if (c == '-') {
            offset++;
            negative = true;
        }
        if (offset >= to) {
            die();
        }
        c = digitChars[offset];
        num = (c - '0');
        offset++;

        for (; offset < to; offset++) {
            c = digitChars[offset];
            if (c != '.') {
                num = (num * 10) + (c - '0');
            }
        }

        return negative ? num * -1 : num;
    }

    public static long parseLongFromTo(char[] digitChars, int offset, int to) {
        long num;
        boolean negative = false;
        char c = digitChars[offset];
        if (c == '-') {
            offset++;
            negative = true;
        }
        if (offset >= to) {
            die();
        }
        c = digitChars[offset];
        num = (c - '0');
        offset++;

        long digit;

        for (; offset < to; offset++) {
            c = digitChars[offset];
            digit = (c - '0');
            num = (num * 10) + digit;
        }

        return negative ? num * -1 : num;
    }

    public static long parseLong(char[] digitChars) {
        return parseLongFromTo(digitChars, 0, digitChars.length);
    }

    public static Number parseJsonNumber(char[] buffer) {
        return parseJsonNumber(buffer, 0, buffer.length);
    }

    public static Number parseJsonNumber(char[] buffer, int from, int to) {
        return parseJsonNumber(buffer, from, to, null);
    }

    public static boolean isNumberDigit(int c) {
        return c >= ALPHA_0 && c <= ALPHA_9;
    }

    protected static boolean isDelimiter(int c) {
        return c == COMMA || c == CLOSED_CURLY || c == CLOSED_BRACKET;
    }

    public static Number parseJsonNumber(char[] buffer, int from, int max, int size[]) {
        Number value = null;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;

        if (buffer[index] == '-') {
            index++;
        }
        if (index >= max) {
            die();
        }

        boolean foundDot = false;
        for (; index < max; index++) {
            char ch = buffer[index];
            if (isNumberDigit(ch)) {
                if (foundDot) {
                    digitsPastPoint++;
                }
            } else if (ch <= 32 || isDelimiter(ch)) {
                break;
            } else if (ch == '.') {
                if (foundDot) {
                    die("unexpected character " + ch);
                }
                foundDot = true;
            } else if (ch == 'E' || ch == 'e' || ch == '-' || ch == '+') {
                simple = false;
            } else {
                die("unexpected character " + ch);
            }
        }

        if (digitsPastPoint >= powersOf10.length - 1) {
            simple = false;
        }

        final int length = index - from;

        if (!foundDot && simple) {
            if (isInteger(buffer, from, length)) {
                value = parseIntFromTo(buffer, from, index);
            } else {
                value = parseLongFromTo(buffer, from, index);
            }
        } else {
            value = new BigDecimal(buffer, from, length);
        }

        if (size != null) {
            size[0] = index;
        }

        return value;
    }

    public static BigDecimal parseBigDecimal(char[] buffer) {
        return new BigDecimal(buffer);
    }

    public static float parseFloat(char[] buffer, int from, int to) {
        return (float) parseDouble(buffer, from, to);
    }

    public static double parseDouble(char[] buffer, int from, int to) {
        double value;
        boolean simple = true;
        int digitsPastPoint = 0;

        int index = from;

        if (buffer[index] == '-') {
            index++;
        }

        boolean foundDot = false;
        for (; index < to; index++) {
            char ch = buffer[index];
            if (isNumberDigit(ch)) {
                if (foundDot) {
                    digitsPastPoint++;
                }
            } else if (ch == '.') {
                if (foundDot) {
                    die("unexpected character " + ch);
                }
                foundDot = true;
            } else if (ch == 'E' || ch == 'e' || ch == '-' || ch == '+') {
                simple = false;
            } else {
                die("unexpected character " + ch);
            }
        }

        if (digitsPastPoint >= powersOf10.length - 1) {
            simple = false;
        }

        final int length = index - from;

        if (!foundDot && simple) {
            if (isInteger(buffer, from, length)) {
                value = parseIntFromTo(buffer, from, index);
            } else {
                value = parseLongFromTo(buffer, from, index);
            }
        } else if (foundDot && simple) {
            long lvalue;

            if (length < powersOf10.length) {
                if (isInteger(buffer, from, length)) {
                    lvalue = parseIntFromToIgnoreDot(buffer, from, index);
                } else {
                    lvalue = parseLongFromToIgnoreDot(buffer, from, index);
                }

                double power = powersOf10[digitsPastPoint];
                value = lvalue / power;
            } else {
                value = Double.parseDouble(new String(buffer, from, length));
            }
        } else {
            value = Double.parseDouble(new String(buffer, from, index - from));
        }

        return value;
    }

    public static int skipWhiteSpace(char[] array, int index, final int length) {
        int c;
        for (; index < length; index++) {
            c = array[index];
            if (c > 32) {
                return index;
            }
        }
        return index;
    }

    public static char[] readNumber(char[] array, int idx, final int len) {
        final int startIndex = idx;

        while (true) {
            if (!CharScanner.isDecimalDigit(array[idx])) {
                break;
            } else {
                idx++;
                if (idx >= len) break;
            }
        }

        return ArrayUtils.copyRange(array, startIndex, idx);
    }

    public static String errorDetails(String message, char[] array, int index, int ch) {
        CharBuf buf = CharBuf.create(255);

        buf.addLine(message);

        buf.addLine("");
        buf.addLine("The current character read is " + debugCharDescription(ch));

        buf.addLine(message);

        int line = 0;
        int lastLineIndex = 0;

        for (int i = 0; i < index && i < array.length; i++) {
            if (array[i] == '\n') {
                line++;
                lastLineIndex = i + 1;
            }
        }

        int count = 0;

        for (int i = lastLineIndex; i < array.length; i++, count++) {
            if (array[i] == '\n') {
                break;
            }
        }

        buf.addLine("line number " + (line + 1));
        buf.addLine("index number " + index);

        try {
            buf.addLine(new String(array, lastLineIndex, count));
        } catch (Exception ex) {
            try {
                int start = index = Math.max(index - 10, 0);

                buf.addLine(new String(array, start, index));
            } catch (Exception ex2) {
                buf.addLine(new String(array, 0, array.length));
            }
        }
        for (int i = 0; i < (index - lastLineIndex); i++) {
            buf.add('.');
        }
        buf.add('^');

        return buf.toString();
    }

    public static String debugCharDescription(int c) {
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
}
