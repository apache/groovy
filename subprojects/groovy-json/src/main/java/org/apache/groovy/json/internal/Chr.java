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

public class Chr {

    public static char[] array(final char... array) {
        return array;
    }

    public static char[] chars(final String array) {
        return array.toCharArray();
    }

    public static boolean in(char value, char[] array) {
        for (char currentValue : array) {
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean in(int value, char[] array) {
        for (int currentValue : array) {
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean in(char value, int offset, char[] array) {
        for (int index = offset; index < array.length; index++) {
            char currentValue = array[index];
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean in(char value, int offset, int end, char[] array) {
        for (int index = offset; index < end; index++) {
            char currentValue = array[index];
            if (currentValue == value) {
                return true;
            }
        }
        return false;
    }

    public static char[] grow(char[] array, final int size) {
        char[] newArray = new char[array.length + size];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static char[] grow(char[] array) {
        char[] newArray = new char[array.length * 2];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static char[] copy(char[] array) {
        char[] newArray = new char[array.length];
        arraycopy(array, 0, newArray, 0, array.length);
        return newArray;
    }

    public static char[] copy(char[] array, int offset, int length) {
        char[] newArray = new char[length];
        arraycopy(array, offset, newArray, 0, length);
        return newArray;
    }

    public static char[] add(char[] array, char v) {
        char[] newArray = new char[array.length + 1];
        arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = v;
        return newArray;
    }

    public static char[] add(char[] array, String str) {
        return add(array, str.toCharArray());
    }

    public static char[] add(char[] array, StringBuilder stringBuilder) {
        return add(array, getCharsFromStringBuilder(stringBuilder));
    }

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

    public static void _idx(char[] buffer, int location, byte[] chars) {
        int index2 = 0;
        int endLocation = (location + chars.length);
        for (int index = location; index < endLocation; index++, index2++) {
            buffer[index] = (char) chars[index2];
        }
    }

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

    public static void _idx(final char[] array, int startIndex, char[] input, final int inputLength) {
        try {
            arraycopy(input, 0, array, startIndex, inputLength);
        } catch (Exception ex) {
            Exceptions.handle(String.format("array size %d, startIndex %d, input length %d",
                    array.length, startIndex, input.length), ex);
        }
    }

    public static void _idx(char[] buffer, int location, byte[] chars, int start, int end) {
        int index2 = start;
        int endLocation = (location + (end - start));
        for (int index = location; index < endLocation; index++, index2++) {
            buffer[index] = (char) chars[index2];
        }
    }

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
