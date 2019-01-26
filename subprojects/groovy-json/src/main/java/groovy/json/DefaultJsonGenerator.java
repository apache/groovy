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

import org.apache.groovy.json.internal.CharBuf;
import org.apache.groovy.json.internal.Chr;
import groovy.lang.Closure;
import groovy.util.Expando;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import static groovy.json.JsonOutput.CLOSE_BRACE;
import static groovy.json.JsonOutput.CLOSE_BRACKET;
import static groovy.json.JsonOutput.COMMA;
import static groovy.json.JsonOutput.EMPTY_LIST_CHARS;
import static groovy.json.JsonOutput.EMPTY_MAP_CHARS;
import static groovy.json.JsonOutput.EMPTY_STRING_CHARS;
import static groovy.json.JsonOutput.OPEN_BRACE;
import static groovy.json.JsonOutput.OPEN_BRACKET;

/**
 * A JsonGenerator that can be configured with various {@link JsonGenerator.Options}.
 * If the default options are sufficient consider using the static {@code JsonOutput.toJson}
 * methods.
 *
 * @see JsonGenerator.Options#build()
 * @since 2.5.0
 */
public class DefaultJsonGenerator implements JsonGenerator {

    protected final boolean excludeNulls;
    protected final boolean disableUnicodeEscaping;
    protected final String dateFormat;
    protected final Locale dateLocale;
    protected final TimeZone timezone;
    protected final Set<Converter> converters = new LinkedHashSet<>();
    protected final Set<String> excludedFieldNames = new HashSet<>();
    protected final Set<Class<?>> excludedFieldTypes = new HashSet<>();

    protected DefaultJsonGenerator(Options options) {
        excludeNulls = options.excludeNulls;
        disableUnicodeEscaping = options.disableUnicodeEscaping;
        dateFormat = options.dateFormat;
        dateLocale = options.dateLocale;
        timezone = options.timezone;
        if (!options.converters.isEmpty()) {
            converters.addAll(options.converters);
        }
        if (!options.excludedFieldNames.isEmpty()) {
            excludedFieldNames.addAll(options.excludedFieldNames);
        }
        if (!options.excludedFieldTypes.isEmpty()) {
            excludedFieldTypes.addAll(options.excludedFieldTypes);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson(Object object) {
        CharBuf buffer = CharBuf.create(255);
        writeObject(object, buffer);
        return buffer.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExcludingFieldsNamed(String name) {
        return excludedFieldNames.contains(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExcludingValues(Object value) {
        if (value == null) {
            return excludeNulls;
        } else {
            return shouldExcludeType(value.getClass());
        }
    }

    /**
     * Serializes Number value and writes it into specified buffer.
     */
    protected void writeNumber(Class<?> numberClass, Number value, CharBuf buffer) {
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

    protected void writeObject(Object object, CharBuf buffer) {
        writeObject(null, object, buffer);
    }

    /**
     * Serializes object and writes it into specified buffer.
     */
    protected void writeObject(String key, Object object, CharBuf buffer) {

        if (isExcludingValues(object)) {
            return;
        }

        if (object == null) {
            buffer.addNull();
            return;
        }

        Class<?> objectClass = object.getClass();

        Converter converter = findConverter(objectClass);
        if (converter != null) {
            object = converter.convert(object, key);
            objectClass = object.getClass();
        }

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
            buffer.addJsonEscapedString(Chr.array((Character) object), disableUnicodeEscaping);
        } else if (objectClass == URL.class) {
            buffer.addJsonEscapedString(object.toString(), disableUnicodeEscaping);
        } else if (objectClass == UUID.class) {
            buffer.addQuoted(object.toString());
        } else if (objectClass == JsonOutput.JsonUnescaped.class) {
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
        } else if (File.class.isAssignableFrom(objectClass)) {
            Map<?, ?> properties = getObjectProperties(object);
            //Clean up all recursive references to File objects
            Iterator<? extends Map.Entry<?, ?>> iterator = properties.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<?,?> entry = iterator.next();
                if(entry.getValue() instanceof File) {
                    iterator.remove();
                }
            }
            writeMap(properties, buffer);
        } else {
            Map<?, ?> properties = getObjectProperties(object);
            writeMap(properties, buffer);
        }
    }

    protected Map<?, ?> getObjectProperties(Object object) {
        Map<?, ?> properties = DefaultGroovyMethods.getProperties(object);
        properties.remove("class");
        properties.remove("declaringClass");
        properties.remove("metaClass");
        return properties;
    }

    /**
     * Serializes any char sequence and writes it into specified buffer.
     */
    protected void writeCharSequence(CharSequence seq, CharBuf buffer) {
        if (seq.length() > 0) {
            buffer.addJsonEscapedString(seq.toString(), disableUnicodeEscaping);
        } else {
            buffer.addChars(EMPTY_STRING_CHARS);
        }
    }

    /**
     * Serializes any char sequence and writes it into specified buffer
     * without performing any manipulation of the given text.
     */
    protected void writeRaw(CharSequence seq, CharBuf buffer) {
        if (seq != null) {
            buffer.add(seq.toString());
        }
    }

    /**
     * Serializes date and writes it into specified buffer.
     */
    protected void writeDate(Date date, CharBuf buffer) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, dateLocale);
        formatter.setTimeZone(timezone);
        buffer.addQuoted(formatter.format(date));
    }

    /**
     * Serializes array and writes it into specified buffer.
     */
    protected void writeArray(Class<?> arrayClass, Object array, CharBuf buffer) {
        if (Object[].class.isAssignableFrom(arrayClass)) {
            Object[] objArray = (Object[]) array;
            writeIterator(Arrays.asList(objArray).iterator(), buffer);
            return;
        }
        buffer.addChar(OPEN_BRACKET);
        if (int[].class.isAssignableFrom(arrayClass)) {
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
                buffer.addJsonEscapedString(Chr.array(charArray[0]), disableUnicodeEscaping);
                for (int i = 1; i < charArray.length; i++) {
                    buffer.addChar(COMMA).addJsonEscapedString(Chr.array(charArray[i]), disableUnicodeEscaping);
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

    /**
     * Serializes map and writes it into specified buffer.
     */
    protected void writeMap(Map<?, ?> map, CharBuf buffer) {
        if (map.isEmpty()) {
            buffer.addChars(EMPTY_MAP_CHARS);
            return;
        }
        buffer.addChar(OPEN_BRACE);
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON");
            }
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            if (isExcludingValues(value) || isExcludingFieldsNamed(key)) {
                continue;
            }
            writeMapEntry(key, value, buffer);
            buffer.addChar(COMMA);
        }
        buffer.removeLastChar(COMMA); // dangling comma
        buffer.addChar(CLOSE_BRACE);
    }

    /**
     * Serializes a map entry and writes it into specified buffer.
     */
    protected void writeMapEntry(String key, Object value, CharBuf buffer) {
        buffer.addJsonFieldName(key, disableUnicodeEscaping);
        writeObject(key, value, buffer);
    }

    /**
     * Serializes iterator and writes it into specified buffer.
     */
    protected void writeIterator(Iterator<?> iterator, CharBuf buffer) {
        if (!iterator.hasNext()) {
            buffer.addChars(EMPTY_LIST_CHARS);
            return;
        }
        buffer.addChar(OPEN_BRACKET);
        while (iterator.hasNext()) {
            Object it = iterator.next();
            if (!isExcludingValues(it)) {
                writeObject(it, buffer);
                buffer.addChar(COMMA);
            }
        }
        buffer.removeLastChar(COMMA); // dangling comma
        buffer.addChar(CLOSE_BRACKET);
    }

    /**
     * Finds a converter that can handle the given type.  The first converter
     * that reports it can handle the type is returned, based on the order in
     * which the converters were specified.  A {@code null} value will be returned
     * if no suitable converter can be found for the given type.
     *
     * @param type that this converter can handle
     * @return first converter that can handle the given type; else {@code null}
     *         if no compatible converters are found for the given type.
     */
    protected Converter findConverter(Class<?> type) {
        for (Converter c : converters) {
            if (c.handles(type)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Indicates whether the given type should be excluded from the generated output.
     *
     * @param type the type to check
     * @return {@code true} if the given type should not be output, else {@code false}
     */
    protected boolean shouldExcludeType(Class<?> type) {
        for (Class<?> t : excludedFieldTypes) {
            if (t.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A converter that handles converting a given type using a closure.
     *
     * @since 2.5.0
     */
    protected static class ClosureConverter implements Converter {

        protected final Class<?> type;
        protected final Closure<?> closure;
        protected final int paramCount;

        protected ClosureConverter(Class<?> type, Closure<?> closure) {
            if (type == null) {
                throw new NullPointerException("Type parameter must not be null");
            }
            if (closure == null) {
                throw new NullPointerException("Closure parameter must not be null");
            }

            int paramCount = closure.getMaximumNumberOfParameters();
            if (paramCount < 1) {
                throw new IllegalArgumentException("Closure must accept at least one parameter");
            }
            Class<?> param1 = closure.getParameterTypes()[0];
            if (!param1.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Expected first parameter to be of type: " + type.toString());
            }
            if (paramCount > 1) {
                Class<?> param2 = closure.getParameterTypes()[1];
                if (!param2.isAssignableFrom(String.class)) {
                    throw new IllegalArgumentException("Expected second parameter to be of type: " + String.class.toString());
                }
            }
            this.type = type;
            this.closure = closure;
            this.paramCount = paramCount;
        }

        /**
         * Returns {@code true} if this converter can handle conversions
         * of the given type.
         *
         * @param type the type of the object to convert
         * @return true if this converter can successfully convert values of
         *      the given type
         */
        @Override
        public boolean handles(Class<?> type) {
            return this.type.isAssignableFrom(type);
        }

        /**
         * Converts a given value.
         *
         * @param value the object to convert
         * @param key the key name for the value, may be {@code null}
         * @return the converted object
         */
        @Override
        public Object convert(Object value, String key) {
            return (paramCount == 1) ?
                    closure.call(value) :
                    closure.call(value, key);
        }

        /**
         * Any two Converter instances registered for the same type are considered
         * to be equal.  This comparison makes managing instances in a Set easier;
         * since there is no chaining of Converters it makes sense to only allow
         * one per type.
         *
         * @param o the object with which to compare.
         * @return {@code true} if this object contains the same class; {@code false} otherwise.
         */
        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ClosureConverter)) {
                return false;
            }
            return this.type == ((ClosureConverter)o).type;
        }

        @Override
        public int hashCode() {
            return this.type.hashCode();
        }

        @Override
        public String toString() {
            return super.toString() + "<" + this.type.toString() + ">";
        }
    }

}
