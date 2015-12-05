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
package groovy.json;

import groovy.json.internal.CharBuf;
import groovy.json.internal.Chr;
import groovy.lang.Closure;
import groovy.util.Expando;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Class responsible for the actual String serialization of the possible values of a JSON structure.
 * This class can also be used as a category, so as to add <code>toJson()</code> methods to various types.
 *
 * @author Guillaume Laforge
 * @author Roshan Dawrani
 * @author Andrey Bloschetsov
 * @author Rick Hightower
 * @author Graeme Rocher
 *
 * @since 1.8.0
 */
public class JsonOutput {

    static final char OPEN_BRACKET = '[';
    static final char CLOSE_BRACKET = ']';
    static final char OPEN_BRACE = '{';
    static final char CLOSE_BRACE = '}';
    static final char COLON = ':';
    static final char COMMA = ',';
    static final char SPACE = ' ';
    static final char NEW_LINE = '\n';
    static final char QUOTE = '"';

    private static final char[] EMPTY_STRING_CHARS = Chr.array(QUOTE, QUOTE);

    private static final String NULL_VALUE = "null";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String DEFAULT_TIMEZONE = "GMT";

    /**
     * @return "true" or "false" for a boolean value
     */
    public static String toJson(Boolean bool) {
        CharBuf buffer = CharBuf.create(4);
        writeObject(bool, buffer); // checking null inside

        return buffer.toString();
    }

    /**
     * @return a string representation for a number
     * @throws JsonException if the number is infinite or not a number.
     */
    public static String toJson(Number n) {
        if (n == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(3);
        Class<?> numberClass = n.getClass();
        writeNumber(numberClass, n, buffer);

        return buffer.toString();
    }

    /**
     * @return a JSON string representation of the character
     */
    public static String toJson(Character c) {
        CharBuf buffer = CharBuf.create(3);
        writeObject(c, buffer); // checking null inside

        return buffer.toString();
    }

    /**
     * @return a properly encoded string with escape sequences
     */
    public static String toJson(String s) {
        if (s == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(s.length() + 2);
        writeCharSequence(s, buffer);

        return buffer.toString();
    }

    /**
     * Format a date that is parseable from JavaScript, according to ISO-8601.
     *
     * @param date the date to format to a JSON string
     * @return a formatted date in the form of a string
     */
    public static String toJson(Date date) {
        if (date == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(26);
        writeDate(date, buffer);

        return buffer.toString();
    }

    /**
     * Format a calendar instance that is parseable from JavaScript, according to ISO-8601.
     *
     * @param cal the calendar to format to a JSON string
     * @return a formatted date in the form of a string
     */
    public static String toJson(Calendar cal) {
        if (cal == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(26);
        writeDate(cal.getTime(), buffer);

        return buffer.toString();
    }

    /**
     * @return the string representation of an uuid
     */
    public static String toJson(UUID uuid) {
        CharBuf buffer = CharBuf.create(64);
        writeObject(uuid, buffer); // checking null inside

        return buffer.toString();
    }

    /**
     * @return the string representation of the URL
     */
    public static String toJson(URL url) {
        CharBuf buffer = CharBuf.create(64);
        writeObject(url, buffer); // checking null inside

        return buffer.toString();
    }

    /**
     * @return an object representation of a closure
     */
    public static String toJson(Closure closure) {
        if (closure == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(255);
        writeMap(JsonDelegate.cloneDelegateAndGetContent(closure), buffer);

        return buffer.toString();
    }

    /**
     * @return an object representation of an Expando
     */
    public static String toJson(Expando expando) {
        if (expando == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(255);
        writeMap(expando.getProperties(), buffer);

        return buffer.toString();
    }

    /**
     * @return "null" for a null value, or a JSON array representation for a collection, array, iterator or enumeration,
     * or representation for other object.
     */
    public static String toJson(Object object) {
        CharBuf buffer = CharBuf.create(255);
        writeObject(object, buffer); // checking null inside

        return buffer.toString();
    }

    /**
     * @return a JSON object representation for a map
     */
    public static String toJson(Map m) {
        if (m == null) {
            return NULL_VALUE;
        }

        CharBuf buffer = CharBuf.create(255);
        writeMap(m, buffer);

        return buffer.toString();
    }

    /**
     * Serializes Number value and writes it into specified buffer.
     */
    private static void writeNumber(Class<?> numberClass, Number value, CharBuf buffer) {
        if (numberClass == Integer.class) {
            buffer.addInt((Integer) value);
        } else if (numberClass == Long.class) {
            buffer.addLong((Long) value);
        } else if (numberClass == BigInteger.class) {
            buffer.addBigInteger((BigInteger) value);
        } else if (numberClass == BigDecimal.class) {
            buffer.addBigDecimal((BigDecimal) value);
        } else if (numberClass == Double.class) {
            Double doubleValue = (Double) value;
            if (doubleValue.isInfinite()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: infinite are not allowed in JSON.");
            }
            if (doubleValue.isNaN()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: NaN are not allowed in JSON.");
            }

            buffer.addDouble(doubleValue);
        } else if (numberClass == Float.class) {
            Float floatValue = (Float) value;
            if (floatValue.isInfinite()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: infinite are not allowed in JSON.");
            }
            if (floatValue.isNaN()) {
                throw new JsonException("Number " + value + " can't be serialized as JSON: NaN are not allowed in JSON.");
            }

            buffer.addFloat(floatValue);
        } else if (numberClass == Byte.class) {
            buffer.addByte((Byte) value);
        } else if (numberClass == Short.class) {
            buffer.addShort((Short) value);
        } else { // Handle other Number implementations
            buffer.addString(value.toString());
        }
    }

    /**
     * Serializes object and writes it into specified buffer.
     */
    private static void writeObject(Object object, CharBuf buffer) {
        if (object == null) {
            buffer.addNull();
        } else {
            Class<?> objectClass = object.getClass();

            if (CharSequence.class.isAssignableFrom(objectClass)) { // Handle String, StringBuilder, GString and other CharSequence implementations
                writeCharSequence((CharSequence) object, buffer);
            } else if (objectClass == Boolean.class) {
                buffer.addBoolean((Boolean) object);
            } else if (Number.class.isAssignableFrom(objectClass)) {
                writeNumber(objectClass, (Number) object, buffer);
            } else if (Date.class.isAssignableFrom(objectClass)) {
                writeDate((Date) object, buffer);
            } else if (Calendar.class.isAssignableFrom(objectClass)) {
                writeDate(((Calendar) object).getTime(), buffer);
            } else if (Map.class.isAssignableFrom(objectClass)) {
                writeMap((Map) object, buffer);
            } else if (Iterable.class.isAssignableFrom(objectClass)) {
                writeIterator(((Iterable<?>) object).iterator(), buffer);
            } else if (Iterator.class.isAssignableFrom(objectClass)) {
                writeIterator((Iterator) object, buffer);
            } else if (objectClass == Character.class) {
                buffer.addJsonEscapedString(Chr.array((Character) object));
            } else if (objectClass == URL.class) {
                buffer.addJsonEscapedString(object.toString());
            } else if (objectClass == UUID.class) {
                buffer.addQuoted(object.toString());
            } else if (objectClass == JsonUnescaped.class) {
                buffer.add(object.toString());
            } else if (Closure.class.isAssignableFrom(objectClass)) {
                writeMap(JsonDelegate.cloneDelegateAndGetContent((Closure<?>) object), buffer);
            } else if (Expando.class.isAssignableFrom(objectClass)) {
                writeMap(((Expando) object).getProperties(), buffer);
            } else if (Enumeration.class.isAssignableFrom(objectClass)) {
                List<?> list = Collections.list((Enumeration<?>) object);
                writeIterator(list.iterator(), buffer);
            } else if (objectClass.isArray()) {
                writeArray(objectClass, object, buffer);
            } else if (Enum.class.isAssignableFrom(objectClass)) {
                buffer.addQuoted(((Enum<?>) object).name());
            }else if (File.class.isAssignableFrom(objectClass)){
                Map<?, ?> properties = getObjectProperties(object);
                //Clean up all recursive references to File objects
                Iterator<? extends Map.Entry<?, ?>> iterator = properties.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<?,?> entry = iterator.next();
                    if(entry.getValue() instanceof File){
                        iterator.remove();
                    }
                }

                writeMap(properties, buffer);
            } else {
                Map<?, ?> properties = getObjectProperties(object);
                writeMap(properties, buffer);
            }
        }
    }

    private static Map<?, ?> getObjectProperties(Object object) {
        Map<?, ?> properties = DefaultGroovyMethods.getProperties(object);
        properties.remove("class");
        properties.remove("declaringClass");
        properties.remove("metaClass");
        return properties;
    }


    /**
     * Serializes any char sequence and writes it into specified buffer.
     */
    private static void writeCharSequence(CharSequence seq, CharBuf buffer) {
        if (seq.length() > 0) {
            buffer.addJsonEscapedString(seq.toString());
        } else {
            buffer.addChars(EMPTY_STRING_CHARS);
        }
    }

    /**
     * Serializes date and writes it into specified buffer.
     */
    private static void writeDate(Date date, CharBuf buffer) {
        SimpleDateFormat formatter = new SimpleDateFormat(JSON_DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone(DEFAULT_TIMEZONE));
        buffer.addQuoted(formatter.format(date));
    }

    /**
     * Serializes array and writes it into specified buffer.
     */
    private static void writeArray(Class<?> arrayClass, Object array, CharBuf buffer) {
        buffer.addChar(OPEN_BRACKET);
        if (Object[].class.isAssignableFrom(arrayClass)) {
            Object[] objArray = (Object[]) array;
            if (objArray.length > 0) {
                writeObject(objArray[0], buffer);
                for (int i = 1; i < objArray.length; i++) {
                    buffer.addChar(COMMA);
                    writeObject(objArray[i], buffer);
                }
            }
        } else if (int[].class.isAssignableFrom(arrayClass)) {
            int[] intArray = (int[]) array;
            if (intArray.length > 0) {
                buffer.addInt(intArray[0]);
                for (int i = 1; i < intArray.length; i++) {
                    buffer.addChar(COMMA).addInt(intArray[i]);
                }
            }
        } else if (long[].class.isAssignableFrom(arrayClass)) {
            long[] longArray = (long[]) array;
            if (longArray.length > 0) {
                buffer.addLong(longArray[0]);
                for (int i = 1; i < longArray.length; i++) {
                    buffer.addChar(COMMA).addLong(longArray[i]);
                }
            }
        } else if (boolean[].class.isAssignableFrom(arrayClass)) {
            boolean[] booleanArray = (boolean[]) array;
            if (booleanArray.length > 0) {
                buffer.addBoolean(booleanArray[0]);
                for (int i = 1; i < booleanArray.length; i++) {
                    buffer.addChar(COMMA).addBoolean(booleanArray[i]);
                }
            }
        } else if (char[].class.isAssignableFrom(arrayClass)) {
            char[] charArray = (char[]) array;
            if (charArray.length > 0) {
                buffer.addJsonEscapedString(Chr.array(charArray[0]));
                for (int i = 1; i < charArray.length; i++) {
                    buffer.addChar(COMMA).addJsonEscapedString(Chr.array(charArray[i]));
                }
            }
        } else if (double[].class.isAssignableFrom(arrayClass)) {
            double[] doubleArray = (double[]) array;
            if (doubleArray.length > 0) {
                buffer.addDouble(doubleArray[0]);
                for (int i = 1; i < doubleArray.length; i++) {
                    buffer.addChar(COMMA).addDouble(doubleArray[i]);
                }
            }
        } else if (float[].class.isAssignableFrom(arrayClass)) {
            float[] floatArray = (float[]) array;
            if (floatArray.length > 0) {
                buffer.addFloat(floatArray[0]);
                for (int i = 1; i < floatArray.length; i++) {
                    buffer.addChar(COMMA).addFloat(floatArray[i]);
                }
            }
        } else if (byte[].class.isAssignableFrom(arrayClass)) {
            byte[] byteArray = (byte[]) array;
            if (byteArray.length > 0) {
                buffer.addByte(byteArray[0]);
                for (int i = 1; i < byteArray.length; i++) {
                    buffer.addChar(COMMA).addByte(byteArray[i]);
                }
            }
        } else if (short[].class.isAssignableFrom(arrayClass)) {
            short[] shortArray = (short[]) array;
            if (shortArray.length > 0) {
                buffer.addShort(shortArray[0]);
                for (int i = 1; i < shortArray.length; i++) {
                    buffer.addChar(COMMA).addShort(shortArray[i]);
                }
            }
        }
        buffer.addChar(CLOSE_BRACKET);
    }

    private static final char[] EMPTY_MAP_CHARS = {OPEN_BRACE, CLOSE_BRACE};

    /**
     * Serializes map and writes it into specified buffer.
     */
    private static void writeMap(Map<?, ?> map, CharBuf buffer) {
        if (!map.isEmpty()) {
            buffer.addChar(OPEN_BRACE);
            boolean firstItem = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON");
                }

                if (!firstItem) {
                    buffer.addChar(COMMA);
                } else {
                    firstItem = false;
                }

                buffer.addJsonFieldName(entry.getKey().toString());
                writeObject(entry.getValue(), buffer);
            }
            buffer.addChar(CLOSE_BRACE);
        } else {
            buffer.addChars(EMPTY_MAP_CHARS);
        }
    }

    private static final char[] EMPTY_LIST_CHARS = {OPEN_BRACKET, CLOSE_BRACKET};

    /**
     * Serializes iterator and writes it into specified buffer.
     */
    private static void writeIterator(Iterator<?> iterator, CharBuf buffer) {
        if (iterator.hasNext()) {
            buffer.addChar(OPEN_BRACKET);
            Object it = iterator.next();
            writeObject(it, buffer);
            while (iterator.hasNext()) {
                it = iterator.next();
                buffer.addChar(COMMA);
                writeObject(it, buffer);
            }
            buffer.addChar(CLOSE_BRACKET);
        } else {
            buffer.addChars(EMPTY_LIST_CHARS);
        }
    }

    /**
     * Pretty print a JSON payload.
     *
     * @param jsonPayload
     * @return a pretty representation of JSON payload.
     */
    public static String prettyPrint(String jsonPayload) {
        int indentSize = 0;
        // Just a guess that the pretty view will take a 20 percent more than original.
        final CharBuf output = CharBuf.create((int) (jsonPayload.length() * 0.2));

        JsonLexer lexer = new JsonLexer(new StringReader(jsonPayload));
        // Will store already created indents.
        Map<Integer, char[]> indentCache = new HashMap<Integer, char[]>();
        while (lexer.hasNext()) {
            JsonToken token = lexer.next();
            switch (token.getType()) {
                case OPEN_CURLY:
                    indentSize += 4;
                    output.addChars(Chr.array(OPEN_BRACE, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case CLOSE_CURLY:
                    indentSize -= 4;
                    output.addChar(NEW_LINE);
                    if (indentSize > 0) {
                        output.addChars(getIndent(indentSize, indentCache));
                    }
                    output.addChar(CLOSE_BRACE);

                    break;
                case OPEN_BRACKET:
                    indentSize += 4;
                    output.addChars(Chr.array(OPEN_BRACKET, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case CLOSE_BRACKET:
                    indentSize -= 4;
                    output.addChar(NEW_LINE);
                    if (indentSize > 0) {
                        output.addChars(getIndent(indentSize, indentCache));
                    }
                    output.addChar(CLOSE_BRACKET);

                    break;
                case COMMA:
                    output.addChars(Chr.array(COMMA, NEW_LINE)).addChars(getIndent(indentSize, indentCache));

                    break;
                case COLON:
                    output.addChars(Chr.array(COLON, SPACE));

                    break;
                case STRING:
                    String textStr = token.getText();
                    String textWithoutQuotes = textStr.substring(1, textStr.length() - 1);
                    if (textWithoutQuotes.length() > 0) {
                        output.addJsonEscapedString(textWithoutQuotes);
                    } else {
                        output.addQuoted(Chr.array());
                    }

                    break;
                default:
                    output.addString(token.getText());
            }
        }

        return output.toString();
    }

    /**
     * Creates new indent if it not exists in the indent cache.
     *
     * @return indent with the specified size.
     */
    private static char[] getIndent(int indentSize, Map<Integer, char[]> indentCache) {
        char[] indent = indentCache.get(indentSize);
        if (indent == null) {
            indent = new char[indentSize];
            Arrays.fill(indent, SPACE);
            indentCache.put(indentSize, indent);
        }

        return indent;
    }

    /**
     * Obtains JSON unescaped text for the given text
     *
     * @param text The text
     * @return The unescaped text
     */
    public static JsonUnescaped unescaped(CharSequence text) {
        return new JsonUnescaped(text);
    }

    /**
     * Represents unescaped JSON
     */
    public static class JsonUnescaped {
        private CharSequence text;

        public JsonUnescaped(CharSequence text) {
            this.text = text;
        }

        public CharSequence getText() {
            return text;
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }

}
