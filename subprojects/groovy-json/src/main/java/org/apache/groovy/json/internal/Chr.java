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

/**
 * Character array helpers used by the JSON internals.
 */
public class Chr {

    /**
     * Returns the supplied characters as an array literal helper.
     *
     * @param array the characters to return
     * @return the supplied array
     */
    public static char[] array(final char... array) {
        return array;
    }

    /**
     * Converts a string to a character array.
     *
     * @param array the string to convert
     * @return the string characters
     */
    public static char[] chars(final String array) {
        return array.toCharArray();
    }

    /**
     * Tests whether the supplied character occurs in the array.
     *
     * @param value the character to find
     * @param array the array to scan
     * @return {@code true} if the character is present
     */
    public static boolean in(char value, char[] array) {
        for (char currentValue : array) {
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the supplied character code occurs in the array.
     *
     * @param value the character code to find
     * @param array the array to scan
     * @return {@code true} if the character is present
     */
    public static boolean in(int value, char[] array) {
        for (int currentValue : array) {
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the supplied character occurs in the array from the given offset.
     *
     * @param value the character to find
     * @param offset the inclusive scan start
     * @param array the array to scan
     * @return {@code true} if the character is present
     */
    public static boolean in(char value, int offset, char[] array) {
        for (int index = offset; index < array.length; index++) {
            char currentValue = array[index];
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether the supplied character occurs in the specified array range.
     *
     * @param value the character to find
     * @param offset the inclusive scan start
     * @param end the exclusive scan end
     * @param array the array to scan
     * @return {@code true} if the character is present
     */
    public static boolean in(char value, int offset, int end, char[] array) {
        for (int index = offset; index < end; index++) {
            char currentValue = array[index];
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Grows an array by the supplied increment.
     *
     * @param array the array to grow
     * @param size the number of extra slots to allocate
     * @return the grown array
     */
    public static char[] grow(char[] array, final int size) {
        char[] newArray = new char[array.length + size];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Doubles the size of an array.
     *
     * @param array the array to grow
     * @return the grown array
     */
    public static char[] grow(char[] array) {
        char[] newArray = new char[array.length * 2];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Copies an entire character array.
     *
     * @param array the array to copy
     * @return the copied array
     */
    public static char[] copy(char[] array) {
        char[] newArray = new char[array.length];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    /**
     * Copies a character range into a new array.
     *
     * @param array the source array
     * @param offset the inclusive start index
     * @param length the number of characters to copy
     * @return the copied range
     */
    public static char[] copy(char[] array, int offset, int length) {
        char[] newArray = new char[length];
        arraycopy(array, offset, newArray, 0, length);
        return newArray;
    }

    /**
     * Appends a character to a new array copy.
     *
     * @param array the source array
     * @param v the character to append
     * @return the new array
     */
    public static char[] add(char[] array, char v) {
        char[] newArray = new char[array.length + 1];
        arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = v;
        return newArray;
    }

    /**
     * Appends a string to a new array copy.
     *
     * @param array the source array
     * @param str the string to append
     * @return the new array
     */
    public static char[] add(char[] array, String str) {
        return add(array, str.toCharArray());
    }

    /**
     * Appends a {@link StringBuilder} to a new array copy.
     *
     * @param array the source array
     * @param stringBuilder the characters to append
     * @return the new array
     */
    public static char[] add(char[] array, StringBuilder stringBuilder) {
        return add(array, getCharsFromStringBuilder(stringBuilder));
    }

    /**
     * Concatenates two character arrays.
     *
     * @param array the first array
     * @param array2 the second array
     * @return the concatenated array
     */
    public static char[] add(char[] array, char[] array2) {
        char[] newArray = new char[array.length + array2.length];
        arraycopy(array, 0, newArray, 0, array.length);
        arraycopy(array2, 0, newArray, array.length, array2.length);
        return newArray;
    }

    /* End universal methods. */

    private static char[] getCharsFromStringBuilder(StringBuilder sbuf) {
        final int length = sbuf.length();
        char[] array2 = new char[length];
        sbuf.getChars(0, length, array2, 0);
        return array2;
    }

    /**
     * Left-pads a character array to the requested size.
     *
     * @param in the input array
     * @param size the target size
     * @param pad the padding character
     * @return the padded array, or the input if no padding is needed
     */
    public static char[] lpad(final char[] in, final int size, char pad) {
        if (in.length >= size) {
            return in;
        }

        int delta = size - in.length;
        int index = 0;
        char[] newArray = new char[size];

        for (; index < delta; index++) {
            newArray[index] = pad;
        }

        for (int index2 = 0; index2 < in.length; index++, index2++) {
            newArray[index] = in[index2];
        }

        return newArray;
    }

    /**
     * Tests whether the supplied range contains a character.
     *
     * @param chars the array to scan
     * @param c the character to find
     * @param start the inclusive scan start
     * @param length the number of characters to scan
     * @return {@code true} if the character is present
     */
    public static boolean contains(char[] chars, char c, int start, final int length) {
        final int to = length + start;
        for (int index = start; index < to; index++) {
            char ch = chars[index];
            if (ch == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies a byte array into a character buffer.
     *
     * @param buffer the destination buffer
     * @param location the destination offset
     * @param chars the bytes to copy
     */
    public static void _idx(char[] buffer, int location, byte[] chars) {
        int index2 = 0;
        int endLocation = (location + chars.length);
        for (int index = location; index < endLocation; index++, index2++) {
            buffer[index] = (char) chars[index2];
        }
    }

    /**
     * Copies a character array into another array.
     *
     * @param array the destination array
     * @param startIndex the destination offset
     * @param input the characters to copy
     */
    public static void _idx(final char[] array, int startIndex, char[] input) {
        try {
            arraycopy(input, 0, array, startIndex, input.length);
        } catch (Exception ex) {
            Exceptions.handle(String.format("array size %d, startIndex %d, input length %d",
                    array.length, startIndex, input.length), ex);
        }
    }

    private static void arraycopy(final char[] src, final int srcPos, final char[] dest, final int destPos, final int length) {
        System.arraycopy(src, srcPos, dest, destPos, length);
    }

    /**
     * Copies a fixed number of characters into another array.
     *
     * @param array the destination array
     * @param startIndex the destination offset
     * @param input the source characters
     * @param inputLength the number of characters to copy
     */
    public static void _idx(final char[] array, int startIndex, char[] input, final int inputLength) {
        try {
            arraycopy(input, 0, array, startIndex, inputLength);
        } catch (Exception ex) {
            Exceptions.handle(String.format("array size %d, startIndex %d, input length %d",
                    array.length, startIndex, input.length), ex);
        }
    }

    /**
     * Copies part of a byte array into a character buffer.
     *
     * @param buffer the destination buffer
     * @param location the destination offset
     * @param chars the source bytes
     * @param start the inclusive source start
     * @param end the exclusive source end
     */
    public static void _idx(char[] buffer, int location, byte[] chars, int start, int end) {
        int index2 = start;
        int endLocation = (location + (end - start));
        for (int index = location; index < endLocation; index++, index2++) {
            buffer[index] = (char) chars[index2];
        }
    }

    /**
     * Concatenates all non-null arrays.
     *
     * @param strings the arrays to concatenate
     * @return the concatenated array
     */
    public static char[] add(char[]... strings) {
        int length = 0;
        for (char[] str : strings) {
            if (str == null) {
                continue;
            }
            length += str.length;
        }
        CharBuf builder = CharBuf.createExact(length);
        for (char[] str : strings) {
            if (str == null) {
                continue;
            }
            builder.add(str);
        }
        return builder.toCharArray();
    }
}
