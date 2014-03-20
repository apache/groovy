/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Derived from Boon all rights granted to Groovy project for this fork.
 */
package groovy.json.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author Rick Hightower
 * @author Stephane Landelle (creator of Gatling and JSONPath and first Boon JSON parser adopter.)
 */
public class FastStringUtils {

    public static final Unsafe UNSAFE;
    public static final long STRING_VALUE_FIELD_OFFSET;
    public static final long STRING_OFFSET_FIELD_OFFSET;
    public static final long STRING_COUNT_FIELD_OFFSET;
    public static final boolean ENABLED;
    private static final boolean WRITE_TO_FINAL_FIELDS = Boolean.parseBoolean(System.getProperty("groovy.json.faststringutils.write.to.final.fields", "false"));
    private static final boolean DISABLE = Boolean.parseBoolean(System.getProperty("groovy.json.faststringutils.disable", "false"));

    /**
     * @return Unsafe
     */
    private static Unsafe loadUnsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);

        } catch (Exception e) {
            return null;
        }
    }

    static {
        UNSAFE = DISABLE ? null : loadUnsafe();
        ENABLED = UNSAFE != null;
    }

    /**
     * @param fieldName name of field
     * @return offset
     */
    private static long getFieldOffset(String fieldName) {
        if (ENABLED) {
            try {
                return UNSAFE.objectFieldOffset(String.class.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                // field undefined
            }
        }
        return -1L;
    }

    static {
        STRING_VALUE_FIELD_OFFSET = getFieldOffset("value");
        STRING_OFFSET_FIELD_OFFSET = getFieldOffset("offset");
        STRING_COUNT_FIELD_OFFSET = getFieldOffset("count");
    }

    /**
     * @author Stéphane Landelle
     */
    protected enum StringImplementation {
        /**
         * JDK 7 drops offset and count so there is special handling for later version of JDK 7.
         */
        DIRECT_CHARS {
            @Override
            public char[] toCharArray(String string) {
                return (char[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
            }

            @Override
            public String noCopyStringFromChars(char[] chars) {
                if (WRITE_TO_FINAL_FIELDS) {
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, chars);
                    return string;
                } else {
                    return new String(chars);
                }
            }
        },
        /**
         * JDK 4 and JDK 5 have offset and count fields.
         */
        OFFSET {
            @Override
            public char[] toCharArray(String string) {
                char[] value = (char[]) UNSAFE.getObject(string, STRING_VALUE_FIELD_OFFSET);
                int offset = UNSAFE.getInt(string, STRING_OFFSET_FIELD_OFFSET);
                int count = UNSAFE.getInt(string, STRING_COUNT_FIELD_OFFSET);
                if (offset == 0 && count == value.length) {
                    // no need to copy
                    return value;
                } else {
                    return string.toCharArray();
                }
            }

            @Override
            public String noCopyStringFromChars(char[] chars) {
                if (WRITE_TO_FINAL_FIELDS) {
                    String string = new String();
                    UNSAFE.putObject(string, STRING_VALUE_FIELD_OFFSET, chars);
                    UNSAFE.putInt(string, STRING_COUNT_FIELD_OFFSET, chars.length);
                    return string;
                } else {
                    return new String(chars);
                }
            }
        },
        UNKNOWN {
            @Override
            public char[] toCharArray(String string) {
                return string.toCharArray();
            }

            @Override
            public String noCopyStringFromChars(char[] chars) {
                return new String(chars);
            }
        };

        public abstract char[] toCharArray(String string);

        public abstract String noCopyStringFromChars(char[] chars);
    }

    public static StringImplementation STRING_IMPLEMENTATION = computeStringImplementation();

    /**
     * @return correct string implementation
     */
    private static StringImplementation computeStringImplementation() {

        if (STRING_VALUE_FIELD_OFFSET != -1L) {
            if (STRING_OFFSET_FIELD_OFFSET != -1L && STRING_COUNT_FIELD_OFFSET != -1L) {
                return StringImplementation.OFFSET;

            } else if (STRING_OFFSET_FIELD_OFFSET == -1L && STRING_COUNT_FIELD_OFFSET == -1L) {
                return StringImplementation.DIRECT_CHARS;
            } else {
                // WTF this is a French abbreviation for unknown.
                return StringImplementation.UNKNOWN;
            }
        } else {
            return StringImplementation.UNKNOWN;
        }
    }

    /**
     * @param string string to grab array from.
     * @return char array from string
     */
    public static char[] toCharArray(final String string) {
        return STRING_IMPLEMENTATION.toCharArray(string);

    }

    /**
     * @param charSequence to grab array from.
     * @return char array from char sequence
     */
    public static char[] toCharArray(final CharSequence charSequence) {
        return toCharArray(charSequence.toString());
    }

    /**
     * @param chars to shove array into.
     * @return new string with chars copied into it
     */
    public static String noCopyStringFromChars(final char[] chars) {
        /*
        J'ai écrit JSON parser du Boon. Sans Stéphane, l'analyseur n'existerait pas. Stéphane est la muse de Boon JSON,
         et mon entraîneur pour l'open source, github, et plus encore. Stéphane n'est pas le créateur directe, mais il
         est le maître architecte et je l'appelle mon ami. It is Step-eff-on not Stef-fa-nee.. Ok?
         */
        return STRING_IMPLEMENTATION.noCopyStringFromChars(chars);
    }
}