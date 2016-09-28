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
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;
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
    private static final char[] EMPTY_MAP_CHARS = {OPEN_BRACE, CLOSE_BRACE};
    private static final char[] EMPTY_LIST_CHARS = {OPEN_BRACKET, CLOSE_BRACKET};

    private static final String NULL_VALUE = "null";
    private static final String EMPTY_VALUE = "";
    private static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final Locale JSON_DATE_FORMAT_LOCALE = Locale.US;
    private static final String DEFAULT_TIMEZONE = "GMT";

    /* package-private for use in builders */
    static final Generator DEFAULT_GENERATOR = new Generator(new Options());

    /**
     * @return "true" or "false" for a boolean value
     */
    public static String toJson(Boolean bool) {
        return DEFAULT_GENERATOR.toJson(bool);
    }

    /**
     * @return a string representation for a number
     * @throws JsonException if the number is infinite or not a number.
     */
    public static String toJson(Number n) {
        return DEFAULT_GENERATOR.toJson(n);
    }

    /**
     * @return a JSON string representation of the character
     */
    public static String toJson(Character c) {
        return DEFAULT_GENERATOR.toJson(c);
    }

    /**
     * @return a properly encoded string with escape sequences
     */
    public static String toJson(String s) {
        return DEFAULT_GENERATOR.toJson(s);
    }

    /**
     * Format a date that is parseable from JavaScript, according to ISO-8601.
     *
     * @param date the date to format to a JSON string
     * @return a formatted date in the form of a string
     */
    public static String toJson(Date date) {
        return DEFAULT_GENERATOR.toJson(date);
    }

    /**
     * Format a calendar instance that is parseable from JavaScript, according to ISO-8601.
     *
     * @param cal the calendar to format to a JSON string
     * @return a formatted date in the form of a string
     */
    public static String toJson(Calendar cal) {
        return DEFAULT_GENERATOR.toJson(cal);
    }

    /**
     * @return the string representation of an uuid
     */
    public static String toJson(UUID uuid) {
        return DEFAULT_GENERATOR.toJson(uuid);
    }

    /**
     * @return the string representation of the URL
     */
    public static String toJson(URL url) {
        return DEFAULT_GENERATOR.toJson(url);
    }

    /**
     * @return an object representation of a closure
     */
    public static String toJson(Closure closure) {
        return DEFAULT_GENERATOR.toJson(closure);
    }

    /**
     * @return an object representation of an Expando
     */
    public static String toJson(Expando expando) {
        return DEFAULT_GENERATOR.toJson(expando);
    }

    /**
     * @return "null" for a null value, or a JSON array representation for a collection, array, iterator or enumeration,
     * or representation for other object.
     */
    public static String toJson(Object object) {
        return DEFAULT_GENERATOR.toJson(object);
    }

    /**
     * @return a JSON object representation for a map
     */
    public static String toJson(Map m) {
        return DEFAULT_GENERATOR.toJson(m);
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
        final CharBuf output = CharBuf.create((int) (jsonPayload.length() * 1.2));

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

    /**
     * Creates a builder for various options that can be set to alter the
     * generated JSON.  After setting the options a call to
     * {@link Options#createGenerator()} will return a fully configured
     * {@link JsonOutput.Generator} object and the {@code toJson} methods
     * can be used.
     *
     * @return a builder for building a JsonOutput.Generator
     *         with the specified options set.
     * @since 2.5
     */
    public static Options options() {
        return new Options();
    }

    /**
     * A builder used to construct a {@link JsonOutput.Generator} instance that allows
     * control over the serialized JSON output.  If you do not need to customize the
     * output it is recommended to use the static {@code JsonOutput.toJson} methods.
     *
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     *     def generator = groovy.json.JsonOutput.options()
     *                         .excludeNulls()
     *                         .dateFormat('yyyy')
     *                         .excludeFieldsByName('bar', 'baz')
     *                         .excludeFieldsByType(java.sql.Date)
     *                         .createGenerator()
     *
     *     def input = [foo: null, lastUpdated: Date.parse('yyyy-MM-dd', '2014-10-24'),
     *                   bar: 'foo', baz: 'foo', systemDate: new java.sql.Date(new Date().getTime())]
     *
     *     assert generator.toJson(input) == '{"lastUpdated":"2014"}'
     * </code></pre>
     *
     * @since 2.5
     */
    public static class Options {

        private boolean excludeNulls;

        private boolean disableUnicodeEscaping;

        private String dateFormat = JsonOutput.JSON_DATE_FORMAT;

        private Locale dateLocale = JsonOutput.JSON_DATE_FORMAT_LOCALE;

        private TimeZone timezone = TimeZone.getTimeZone(JsonOutput.DEFAULT_TIMEZONE);

        private final Set<Converter> converters = new LinkedHashSet<Converter>();

        private final Set<String> excludedFieldNames = new HashSet<String>();

        private final Set<Class<?>> excludedFieldTypes = new HashSet<Class<?>>();

        private Options() {}

        /**
         * Do not serialize {@code null} values.
         *
         * @return a reference to this {@code Options} instance
         */
        public Options excludeNulls() {
            excludeNulls = true;
            return this;
        }

        /**
         * Disables the escaping of Unicode characters in JSON String values.
         *
         * @return a reference to this {@code Options} instance
         */
        public Options disableUnicodeEscaping() {
            disableUnicodeEscaping = true;
            return this;
        }

        /**
         * Sets the date format that will be used to serialize {@code Date} objects.
         * This must be a valid pattern for {@link java.text.SimpleDateFormat} and the
         * date formatter will be constructed with the default locale of {@link Locale#US}.
         *
         * @param format date format pattern used to serialize dates
         * @return a reference to this {@code Options} instance
         * @exception NullPointerException if the given pattern is null
         * @exception IllegalArgumentException if the given pattern is invalid
         */
        public Options dateFormat(String format) {
            return dateFormat(format, JsonOutput.JSON_DATE_FORMAT_LOCALE);
        }

        /**
         * Sets the date format that will be used to serialize {@code Date} objects.
         * This must be a valid pattern for {@link java.text.SimpleDateFormat}.
         *
         * @param format date format pattern used to serialize dates
         * @param locale the locale whose date format symbols will be used
         * @return a reference to this {@code Options} instance
         * @exception IllegalArgumentException if the given pattern is invalid
         */
        public Options dateFormat(String format, Locale locale) {
            // validate date format pattern
            new SimpleDateFormat(format, locale);
            dateFormat = format;
            dateLocale = locale;
            return this;
        }

        /**
         * Sets the time zone that will be used to serialize dates.
         *
         * @param timezone used to serialize dates
         * @return a reference to this {@code Options} instance
         * @exception NullPointerException if the given timezone is null
         */
        public Options timezone(String timezone) {
            this.timezone = TimeZone.getTimeZone(timezone);
            return this;
        }

        /**
         * Registers a closure that will be called when the specified type or subtype
         * is serialized.
         *
         * <p>The closure must accept either 1 or 2 parameters.  The first parameter
         * is required and will be instance of the {@code type} for which the closure
         * is registered.  The second optional parameter should be of type {@code String}
         * and, if available, will be passed the name of the key associated with this
         * value if serializing a JSON Object.  This parameter will be {@code null} when
         * serializing a JSON Array or when there is no way to determine the name of the key.
         *
         * <p>The return value from the closure must be a valid JSON value. The result
         * of the closure will be written to the internal buffer directly and no quoting,
         * escaping or other manipulation will be done to the resulting output.
         *
         * <p>
         * Example:
         * <pre><code class="groovyTestCase">
         *     def generator = groovy.json.JsonOutput.options()
         *                         .addConverter(URL) { URL u ->
         *                             "\"${u.getHost()}\""
         *                         }
         *                         .createGenerator()
         *
         *     def input = [domain: new URL('http://groovy-lang.org/json.html#_parser_variants')]
         *
         *     assert generator.toJson(input) == '{"domain":"groovy-lang.org"}'
         * </code></pre>
         *
         * <p>If two or more closures are registered for the exact same type the last
         * closure based on the order they were specified will be used.  When serializing an
         * object its type is compared to the list of registered types in the order the were
         * given and the closure for the first suitable type will be called.  Therefore, it is
         * important to register more specific types first.
         *
         * @param type the type to convert
         * @param closure called when the registered type or any type assignable to the given
         *                type is encountered
         * @param <T> the type this converter is registered to handle
         * @return a reference to this {@code Options} instance
         * @exception NullPointerException if the given type or closure is null
         * @exception IllegalArgumentException if the given closure does not accept
         *                  a parameter of the given type
         */
        public <T> Options addConverter(Class<T> type, @ClosureParams(value=FromString.class, options={"T","T,String"}) Closure<? extends CharSequence> closure) {
            Converter converter = Converter.of(type, closure);
            if (converters.contains(converter)) {
                converters.remove(converter);
            }
            converters.add(converter);
            return this;
        }

        /**
         * Excludes from the output any fields that match the specified names.
         *
         * @param fieldNames name of the field to exclude from the output
         * @return a reference to this {@code Options} instance
         */
        public Options excludeFieldsByName(CharSequence... fieldNames) {
            return excludeFieldsByName(Arrays.asList(fieldNames));
        }

        /**
         * Excludes from the output any fields that match the specified names.
         *
         * @param fieldNames collection of names to exclude from the output
         * @return a reference to this {@code Options} instance
         */
        public Options excludeFieldsByName(Iterable<? extends CharSequence> fieldNames) {
            for (CharSequence cs : fieldNames) {
                if (cs != null) {
                    excludedFieldNames.add(cs.toString());
                }
            }
            return this;
        }

        /**
         * Excludes from the output any fields whose type is the same or is
         * assignable to any of the given types.
         *
         * @param types excluded from the output
         * @return a reference to this {@code Options} instance
         */
        public Options excludeFieldsByType(Class<?>... types) {
            return excludeFieldsByType(Arrays.asList(types));
        }

        /**
         * Excludes from the output any fields whose type is the same or is
         * assignable to any of the given types.
         *
         * @param types collection of types to exclude from the output
         * @return a reference to this {@code Options} instance
         */
        public Options excludeFieldsByType(Iterable<Class<?>> types) {
            for (Class<?> c : types) {
                if (c != null) {
                    excludedFieldTypes.add(c);
                }
            }
            return this;
        }

        /**
         * Creates a {@link JsonOutput.Generator} that is based on the current options.
         *
         * @return a fully configured {@link JsonOutput.Generator}
         */
        public Generator createGenerator() {
            return new Generator(this);
        }
    }

    /**
     * A JsonOutput Generator that can be configured with various {@link JsonOutput.Options}.
     * If the default options are sufficient consider using the static {@code JsonOutput.toJson}
     * methods.
     *
     * @see JsonOutput#options()
     * @see Options#createGenerator()
     * @since 2.5
     */
    public static class Generator {

        private final boolean excludeNulls;
        private final boolean disableUnicodeEscaping;
        private final String dateFormat;
        private final Locale dateLocale;
        private final TimeZone timezone;

        private final Set<Converter> converters = new LinkedHashSet<Converter>();

        private final Set<String> excludedFieldNames = new HashSet<String>();

        private final Set<Class<?>> excludedFieldTypes = new HashSet<Class<?>>();

        private final String nullValue;

        private final boolean hasConverters;
        private final boolean hasExcludedFieldNames;
        private final boolean hasExcludedFieldTypes;

        private Generator(Options options) {
            excludeNulls = options.excludeNulls;
            disableUnicodeEscaping = options.disableUnicodeEscaping;
            nullValue = (excludeNulls) ? JsonOutput.EMPTY_VALUE : JsonOutput.NULL_VALUE;
            dateFormat = options.dateFormat;
            dateLocale = options.dateLocale;
            timezone = options.timezone;
            if (!options.converters.isEmpty()) {
                converters.addAll(options.converters);
                hasConverters = true;
            } else {
                hasConverters = false;
            }
            if (!options.excludedFieldNames.isEmpty()) {
                excludedFieldNames.addAll(options.excludedFieldNames);
                hasExcludedFieldNames = true;
            } else {
                hasExcludedFieldNames = false;
            }
            if (!options.excludedFieldTypes.isEmpty()) {
                excludedFieldTypes.addAll(options.excludedFieldTypes);
                hasExcludedFieldTypes = true;
            } else {
                hasExcludedFieldTypes = false;
            }
        }

        /**
         * @see JsonOutput#toJson(Boolean)
         */
        public String toJson(Boolean bool) {
            CharBuf buffer = CharBuf.create(4);
            writeObject(bool, buffer); // checking null inside

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Number)
         */
        public String toJson(Number n) {
            if (n == null) {
                return nullValue;
            }

            CharBuf buffer = CharBuf.create(3);
            Class<?> numberClass = n.getClass();

            if (shouldExcludeType(numberClass)) {
                return EMPTY_VALUE;
            }

            Converter converter = findConverter(numberClass);
            if (converter != null) {
                writeRaw(converter.convert(n), buffer);
            } else {
                writeNumber(numberClass, n, buffer);
            }

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Character)
         */
        public String toJson(Character c) {
            CharBuf buffer = CharBuf.create(3);
            writeObject(c, buffer); // checking null inside

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(String)
         */
        public String toJson(String s) {
            if (s == null) {
                return nullValue;
            }

            CharBuf buffer = CharBuf.create(s.length() + 2);
            writeCharSequence(s, buffer);

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Date)
         */
        public String toJson(Date date) {
            if (date == null) {
                return nullValue;
            }

            if (shouldExcludeType(date.getClass())) {
                return EMPTY_VALUE;
            }

            CharBuf buffer = CharBuf.create(26);

            Converter converter = findConverter(date.getClass());
            if (converter != null) {
                writeRaw(converter.convert(date), buffer);
            } else {
                writeDate(date, buffer);
            }

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Calendar)
         */
        public String toJson(Calendar cal) {
            if (cal == null) {
                return nullValue;
            }

            if (shouldExcludeType(cal.getClass())) {
                return EMPTY_VALUE;
            }

            CharBuf buffer = CharBuf.create(26);

            Converter converter = findConverter(cal.getClass());
            if (converter != null) {
                writeRaw(converter.convert(cal), buffer);
            } else {
                writeDate(cal.getTime(), buffer);
            }

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(UUID)
         */
        public String toJson(UUID uuid) {
            CharBuf buffer = CharBuf.create(64);
            writeObject(uuid, buffer); // checking null inside

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(URL)
         */
        public String toJson(URL url) {
            CharBuf buffer = CharBuf.create(64);
            writeObject(url, buffer); // checking null inside

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Closure)
         */
        public String toJson(Closure closure) {
            if (closure == null) {
                return nullValue;
            }

            if (shouldExcludeType(closure.getClass())) {
                return EMPTY_VALUE;
            }

            CharBuf buffer = CharBuf.create(255);
            writeMap(JsonDelegate.cloneDelegateAndGetContent(closure), buffer);

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Expando)
         */
        public String toJson(Expando expando) {
            if (expando == null) {
                return nullValue;
            }

            if (shouldExcludeType(expando.getClass())) {
                return EMPTY_VALUE;
            }

            CharBuf buffer = CharBuf.create(255);
            writeMap(expando.getProperties(), buffer);

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Object)
         */
        public String toJson(Object object) {
            CharBuf buffer = CharBuf.create(255);
            writeObject(object, buffer); // checking null inside

            return buffer.toString();
        }

        /**
         * @see JsonOutput#toJson(Map)
         */
        public String toJson(Map m) {
            if (m == null) {
                return nullValue;
            }

            if (shouldExcludeType(m.getClass())) {
                return EMPTY_VALUE;
            }

            CharBuf buffer = CharBuf.create(255);
            writeMap(m, buffer);

            return buffer.toString();
        }

        /**
         * Serializes Number value and writes it into specified buffer.
         */
        private void writeNumber(Class<?> numberClass, Number value, CharBuf buffer) {
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

        private void writeObject(Object object, CharBuf buffer) {
            writeObject(null, object, buffer);
        }

        /**
         * Serializes object and writes it into specified buffer.
         */
        private void writeObject(String key, Object object, CharBuf buffer) {
            if (object == null) {
                if (!excludeNulls) {
                    buffer.addNull();
                }
                return;
            }

            Class<?> objectClass = object.getClass();

            if (shouldExcludeType(objectClass)) {
                return;
            }

            Converter converter = findConverter(objectClass);
            if (converter != null) {
                writeRaw(converter.convert(object, key), buffer);
                return;
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
        private void writeCharSequence(CharSequence seq, CharBuf buffer) {
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
        private void writeRaw(CharSequence seq, CharBuf buffer) {
            if (seq != null) {
                buffer.add(seq.toString());
            }
        }

        /**
         * Serializes date and writes it into specified buffer.
         */
        private void writeDate(Date date, CharBuf buffer) {
            SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, dateLocale);
            formatter.setTimeZone(timezone);
            buffer.addQuoted(formatter.format(date));
        }

        /**
         * Serializes array and writes it into specified buffer.
         */
        private void writeArray(Class<?> arrayClass, Object array, CharBuf buffer) {
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
        private void writeMap(Map<?, ?> map, CharBuf buffer) {
            if (!map.isEmpty()) {
                buffer.addChar(OPEN_BRACE);
                boolean firstItem = true;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() == null) {
                        throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON");
                    }

                    String key = entry.getKey().toString();
                    Object value = entry.getValue();

                    if (excludeNulls && value == null) {
                        continue;
                    }
                    if (hasExcludedFieldNames && excludedFieldNames.contains(key)) {
                        continue;
                    }
                    if (value != null && shouldExcludeType(value.getClass())) {
                        continue;
                    }

                    if (!firstItem) {
                        buffer.addChar(COMMA);
                    } else {
                        firstItem = false;
                    }

                    buffer.addJsonFieldName(key, disableUnicodeEscaping);
                    writeObject(key, value, buffer);
                }
                buffer.addChar(CLOSE_BRACE);
            } else {
                buffer.addChars(EMPTY_MAP_CHARS);
            }
        }

        /**
         * Serializes iterator and writes it into specified buffer.
         */
        private void writeIterator(Iterator<?> iterator, CharBuf buffer) {
            if (iterator.hasNext()) {
                buffer.addChar(OPEN_BRACKET);
                boolean needComma = false;
                while (iterator.hasNext()) {
                    Object it = iterator.next();
                    if (excludeNulls && it == null) {
                        continue;
                    }
                    if (it != null && shouldExcludeType(it.getClass())) {
                        continue;
                    }
                    if (needComma) buffer.addChar(COMMA);
                    writeObject(it, buffer);
                    needComma = true;
                }
                buffer.addChar(CLOSE_BRACKET);
            } else {
                buffer.addChars(EMPTY_LIST_CHARS);
            }
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
        private Converter findConverter(Class<?> type) {
            if (!hasConverters) {
                return null;
            }
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
        private boolean shouldExcludeType(Class<?> type) {
            if (hasExcludedFieldTypes) {
                for (Class<?> t : excludedFieldTypes) {
                    if (t.isAssignableFrom(type)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /**
         * Package-private helper method used by StreamingJsonBuilder.
         *
         * @param name of the field
         * @return true if that field is being excluded, else false
         */
        boolean isExcludingFieldsNamed(String name) {
            return hasExcludedFieldNames && excludedFieldNames.contains(name);
        }

        /**
         * Package-private helper method used by StreamingJsonBuilder.
         *
         * @param value an instance of an object
         * @return true if values like this are being excluded, else false
         */
        boolean isExcludingValues(Object value) {
            if (value == null) {
                if (excludeNulls) {
                    return true;
                }
            } else {
                if (shouldExcludeType(value.getClass())) {
                    return true;
                }
            }
            return false;
        }

    }

    /**
     * A converter that handles converting a given type to a JSON value
     * using a closure.
     */
    private static class Converter {

        private final Class<?> type;
        private final Closure<? extends CharSequence> closure;
        private final int paramCount;

        static Converter of(Class<?> type, Closure<? extends CharSequence> closure) {
            return new Converter(type, closure);
        }

        private Converter(Class<?> type, Closure<? extends CharSequence> closure) {
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
         *      the given type to a JSON value
         */
        boolean handles(Class<?> type) {
            return this.type.isAssignableFrom(type);
        }

        /**
         * Converts a given value to a JSON value.
         *
         * @param value the object to convert
         * @return a JSON value representing the value
         */
        CharSequence convert(Object value) {
            return convert(value, null);
        }

        /**
         * Converts a given value to a JSON value.
         *
         * @param value the object to convert
         * @param key the key name for the value, may be {@code null}
         * @return a JSON value representing the value
         */
        CharSequence convert(Object value, String key) {
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
            if (!(o instanceof Converter)) {
                return false;
            }
            return this.type == ((Converter)o).type;
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
