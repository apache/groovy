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
package org.codehaus.groovy.syntax;

import antlr.collections.AST;
import org.codehaus.groovy.antlr.ASTRuntimeException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Helper class for processing Groovy numeric literals.
 */
public class Numbers {

    //---------------------------------------------------------------------------
    // LEXING SUPPORT

    /**
     * Returns true if the specified character is a base-10 digit.
     */
    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * Returns true if the specific character is a base-8 digit.
     */
    public static boolean isOctalDigit(char c) {
        return c >= '0' && c <= '7';
    }

    /**
     * Returns true if the specified character is a base-16 digit.
     */
    public static boolean isHexDigit(char c) {
        return isDigit(c) || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    /**
     * Returns true if the specified character is a valid type specifier
     * for a numeric value.
     */
    public static boolean isNumericTypeSpecifier(char c, boolean isDecimal) {
        if (isDecimal) {
            switch (c) {
                case 'G':
                case 'g':
                case 'D':
                case 'd':
                case 'F':
                case 'f':
                    return true;
            }
        } else {
            switch (c) {
                case 'G':
                case 'g':
                case 'I':
                case 'i':
                case 'L':
                case 'l':
                    return true;
            }
        }

        return false;
    }

    //---------------------------------------------------------------------------
    // PARSING SUPPORT

    private static final BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN_LONG = BigInteger.valueOf(Long.MIN_VALUE);

    private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE);

    private static final BigDecimal MAX_DOUBLE = new BigDecimal(String.valueOf(Double.MAX_VALUE));
    private static final BigDecimal MIN_DOUBLE = MAX_DOUBLE.negate();

    private static final BigDecimal MAX_FLOAT = new BigDecimal(String.valueOf(Float.MAX_VALUE));
    private static final BigDecimal MIN_FLOAT = MAX_FLOAT.negate();

    /**
     * Builds a Number from the given integer descriptor.  Creates the narrowest
     * type possible, or a specific type, if specified.
     *
     * @param text literal text to parse
     * @return instantiated Number object
     * @throws NumberFormatException if the number does not fit within the type requested by the type
     *                               specifier suffix (invalid numbers don't make it here)
     */
    public static Number parseInteger(String text) {
        String text1 = text;
        // remove place holder underscore before starting
        text1 = text1.replace("_", "");

        char c = ' ';
        int length = text1.length();

        //
        // Strip off the sign, if present

        boolean negative = false;
        if ((c = text1.charAt(0)) == '-' || c == '+') {
            negative = (c == '-');
            text1 = text1.substring(1, length);
            length -= 1;
        }

        //
        // Determine radix (default is 10).

        int radix = 10;
        if (text1.charAt(0) == '0' && length > 1) {
            c = text1.charAt(1);
            if (c == 'X' || c == 'x') {
                radix = 16;
                text1 = text1.substring(2, length);
                length -= 2;
            } else if (c == 'B' || c == 'b') {
                radix = 2;
                text1 = text1.substring(2, length);
                length -= 2;
            } else {
                radix = 8;
            }
        }

        //
        // Strip off any type specifier and convert it to lower
        // case, if present.

        char type = 'x';  // pick best fit
        if (isNumericTypeSpecifier(text1.charAt(length - 1), false)) {
            type = Character.toLowerCase(text1.charAt(length - 1));
            text1 = text1.substring(0, length - 1);

            length -= 1;
        }

        //
        // Add the sign back, if necessary

        if (negative) {
            text1 = "-" + text1;
        }

        //
        // Build the specified type or, if no type was specified, the
        // smallest type in which the number will fit.

        BigInteger value = new BigInteger(text1, radix);

        switch (type) {
            case 'i':
                if (radix == 10 && (value.compareTo(MAX_INTEGER) > 0 || value.compareTo(MIN_INTEGER) < 0)) {
                    throw new NumberFormatException("Number of value " + value + " does not fit in the range of int, but int was enforced.");
                } else {
                    return value.intValue();
                }
            case 'l':
                if (radix == 10 && (value.compareTo(MAX_LONG) > 0 || value.compareTo(MIN_LONG) < 0)) {
                    throw new NumberFormatException("Number of value " + value + " does not fit in the range of long, but long was enforced.");
                } else {
                    return value.longValue();
                }
            case 'g':
                return value;
            default:
                // If not specified, we will return the narrowest possible
                // of Integer, Long, and BigInteger.
                if (value.compareTo(MAX_INTEGER) <= 0 && value.compareTo(MIN_INTEGER) >= 0) {
                    return value.intValue();
                } else if (value.compareTo(MAX_LONG) <= 0 && value.compareTo(MIN_LONG) >= 0) {
                    return value.longValue();
                }
                return value;
        }
    }

    /**
     * Builds a Number from the given integer descriptor.  Creates the narrowest
     * type possible, or a specific type, if specified.
     *
     * @param reportNode at node for error reporting in the parser
     * @param text       literal text to parse
     * @return instantiated Number object
     * @throws NumberFormatException if the number does not fit within the type requested by the type
     *                               specifier suffix (invalid numbers don't make it here)
     */
    @Deprecated
    public static Number parseInteger(AST reportNode, String text) {
        // remove place holder underscore before starting
        text = text.replace("_", "");

        char c = ' ';
        int length = text.length();

        //
        // Strip off the sign, if present

        boolean negative = false;
        if ((c = text.charAt(0)) == '-' || c == '+') {
            negative = (c == '-');
            text = text.substring(1, length);
            length -= 1;
        }

        //
        // Determine radix (default is 10).

        int radix = 10;
        if (text.charAt(0) == '0' && length > 1) {
            c = text.charAt(1);
            if (c == 'X' || c == 'x') {
                radix = 16;
                text = text.substring(2, length);
                length -= 2;
            } else if (c == 'B' || c == 'b') {
                radix = 2;
                text = text.substring(2, length);
                length -= 2;
            } else {
                radix = 8;
            }
        }

        //
        // Strip off any type specifier and convert it to lower
        // case, if present.

        char type = 'x';  // pick best fit
        if (isNumericTypeSpecifier(text.charAt(length - 1), false)) {
            type = Character.toLowerCase(text.charAt(length - 1));
            text = text.substring(0, length - 1);

            length -= 1;
        }

        //
        // Add the sign back, if necessary

        if (negative) {
            text = "-" + text;
        }

        //
        // Build the specified type or, if no type was specified, the
        // smallest type in which the number will fit.

        BigInteger value = new BigInteger(text, radix);

        switch (type) {
            case 'i':
                if (radix == 10 && (value.compareTo(MAX_INTEGER) > 0 || value.compareTo(MIN_INTEGER) < 0)) {
                    throw new ASTRuntimeException(reportNode, "Number of value " + value + " does not fit in the range of int, but int was enforced.");
                } else {
                    return value.intValue();
                }
            case 'l':
                if (radix == 10 && (value.compareTo(MAX_LONG) > 0 || value.compareTo(MIN_LONG) < 0)) {
                    throw new ASTRuntimeException(reportNode, "Number of value " + value + " does not fit in the range of long, but long was enforced.");
                } else {
                    return value.longValue();
                }
            case 'g':
                return value;
            default:
                // If not specified, we will return the narrowest possible
                // of Integer, Long, and BigInteger.
                if (value.compareTo(MAX_INTEGER) <= 0 && value.compareTo(MIN_INTEGER) >= 0) {
                    return value.intValue();
                } else if (value.compareTo(MAX_LONG) <= 0 && value.compareTo(MIN_LONG) >= 0) {
                    return value.longValue();
                }
                return value;
        }
    }

    /**
     * Builds a Number from the given decimal descriptor.  Uses BigDecimal,
     * unless, Double or Float is requested.
     *
     * @param text literal text to parse
     * @return instantiated Number object
     * @throws NumberFormatException if the number does not fit within the type
     *                               requested by the type specifier suffix (invalid numbers don't make
     *                               it here)
     */
    public static Number parseDecimal(String text) {
        text = text.replace("_", "");
        int length = text.length();

        //
        // Strip off any type specifier and convert it to lower
        // case, if present.

        char type = 'x';
        if (isNumericTypeSpecifier(text.charAt(length - 1), true)) {
            type = Character.toLowerCase(text.charAt(length - 1));
            text = text.substring(0, length - 1);
        }

        //
        // Build the specified type or default to BigDecimal

        BigDecimal value = new BigDecimal(text);
        switch (type) {
            case 'f':
                if (value.compareTo(MAX_FLOAT) <= 0 && value.compareTo(MIN_FLOAT) >= 0) {
                    return Float.parseFloat(text);
                }
                throw new NumberFormatException("out of range");

            case 'd':
                if (value.compareTo(MAX_DOUBLE) <= 0 && value.compareTo(MIN_DOUBLE) >= 0) {
                    return Double.parseDouble(text);
                }
                throw new NumberFormatException("out of range");

            case 'g':
            default:
                return value;
        }
    }

}
