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

import groovy.lang.Closure;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

/**
 * Generates JSON from objects.
 *
 * The {@link Options} builder can be used to configure an instance of a JsonGenerator.
 *
 * @see Options#build()
 * @since 2.5.0
 */
public interface JsonGenerator {

    /**
     * Converts an object to its JSON representation.
     *
     * @param object to convert to JSON
     * @return JSON
     */
    String toJson(Object object);

    /**
     * Indicates whether this JsonGenerator is configured to exclude fields by
     * the given name.
     *
     * @param name of the field
     * @return true if that field is being excluded, else false
     */
    boolean isExcludingFieldsNamed(String name);

    /**
     * Indicates whether this JsonGenerator is configured to exclude values
     * of the given object (may be {@code null}).
     *
     * @param value an instance of an object
     * @return true if values like this are being excluded, else false
     */
    boolean isExcludingValues(Object value);

    /**
     * Handles converting a given type.
     *
     * @since 2.5.0
     */
    interface Converter {

        /**
         * Returns {@code true} if this converter can handle conversions
         * of the given type.
         *
         * @param type the type of the object to convert
         * @return {@code true} if this converter can successfully convert values of
         *      the given type, else {@code false}
         */
        boolean handles(Class<?> type);

        /**
         * Converts a given object.
         *
         * @param value the object to convert
         * @param key the key name for the value, may be {@code null}
         * @return the converted object
         */
        Object convert(Object value, String key);

    }

    /**
     * A builder used to construct a {@link JsonGenerator} instance that allows
     * control over the serialized JSON output.  If you do not need to customize the
     * output it is recommended to use the static {@code JsonOutput.toJson} methods.
     *
     * <p>
     * Example:
     * <pre><code class="groovyTestCase">
     *     def generator = new groovy.json.JsonGenerator.Options()
     *                         .excludeNulls()
     *                         .dateFormat('yyyy')
     *                         .excludeFieldsByName('bar', 'baz')
     *                         .excludeFieldsByType(java.sql.Date)
     *                         .build()
     *
     *     def input = [foo: null, lastUpdated: Date.parse('yyyy-MM-dd', '2014-10-24'),
     *                   bar: 'foo', baz: 'foo', systemDate: new java.sql.Date(new Date().getTime())]
     *
     *     assert generator.toJson(input) == '{"lastUpdated":"2014"}'
     * </code></pre>
     *
     * @since 2.5.0
     */
    class Options {

        protected static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
        protected static final Locale JSON_DATE_FORMAT_LOCALE = Locale.US;
        protected static final String DEFAULT_TIMEZONE = "GMT";

        protected boolean excludeNulls;
        protected boolean disableUnicodeEscaping;
        protected String dateFormat = JSON_DATE_FORMAT;
        protected Locale dateLocale = JSON_DATE_FORMAT_LOCALE;
        protected TimeZone timezone = TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        protected final Set<Converter> converters = new LinkedHashSet<>();
        protected final Set<String> excludedFieldNames = new HashSet<>();
        protected final Set<Class<?>> excludedFieldTypes = new HashSet<>();

        public Options() {}

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
            return dateFormat(format, JSON_DATE_FORMAT_LOCALE);
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
         * Registers a converter that will be called when a type it handles is encountered.
         *
         * @param converter to register
         * @return a reference to this {@code Options} instance
         */
        public Options addConverter(Converter converter) {
            if (converter != null) {
                converters.add(converter);
            }
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
         * <p>
         * Example:
         * <pre><code class="groovyTestCase">
         *     def generator = new groovy.json.JsonGenerator.Options()
         *                         .addConverter(URL) { URL u ->
         *                             u.getHost()
         *                         }
         *                         .build()
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
        public <T> Options addConverter(Class<T> type,
                                        @ClosureParams(value=FromString.class, options={"T","T,String"})
                                        Closure<?> closure)
        {
            Converter converter = new DefaultJsonGenerator.ClosureConverter(type, closure);
            converters.remove(converter);
            return addConverter(converter);
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
         * Creates a {@link JsonGenerator} that is based on the current options.
         *
         * @return a fully configured {@link JsonGenerator}
         */
        public JsonGenerator build() {
            return new DefaultJsonGenerator(this);
        }
    }

}
