/*
 * Copyright 2003-2013 the original author or authors.
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
 */
package groovy.json

import groovy.transform.CompileStatic

import static JsonTokenType.*
import java.text.SimpleDateFormat
import org.codehaus.groovy.runtime.DefaultGroovyMethods

/**
 * Class responsible for the actual String serialization of the possible values of a JSON structure.
 * This class can also be used as a category, so as to add <code>toJson()</code> methods to various types.
 *
 * @author Guillaume Laforge
 * @author Roshan Dawrani
 * @since 1.8.0
 */
class JsonOutput {

    /**
     * Date formatter for outputting dates to a string
     * that can be parsed back from JavaScript with:
     * <code>Date.parse(stringRepresentation)</code>
     */
    private static final ThreadLocal<SimpleDateFormat> dateFormatter = new DateFormatThreadLocal()

    /**
     * @return "true" or "false" for a boolean value
     */
    @CompileStatic
    static String toJson(Boolean bool) {
        bool.toString()
    }

    /**
     * @return a string representation for a number
     * @throws JsonException if the number is infinite or not a number.
     */
    static String toJson(Number n) {
        if (n.class in [Double, Float] && (n.isInfinite() || n.isNaN())) {
            throw new JsonException("Number ${n} can't be serialized as JSON: infinite or NaN are not allowed in JSON.")
        }
        n.toString()
    }

    /**
     * @return a JSON string representation of the character
     */
    @CompileStatic
    static String toJson(Character c) {
        "\"$c\""
    }

    /**
     * @return a properly encoded string with escape sequences
     */
    @CompileStatic
    static String toJson(String s) {
        "\"${StringEscapeUtils.escapeJava(s)}\""
    }

    /**
     * Format a date that is parseable from JavaScript, according to ISO-8601.
     *
     * @param date the date to format to a JSON string
     * @return a formatted date in the form of a string
     */
    @CompileStatic
    static String toJson(Date date) {
        "\"${dateFormatter.get().format(date)}\""
    }

    /**
     * Format a calendar instance that is parseable from JavaScript, according to ISO-8601.
     *
     * @param cal the calendar to format to a JSON string
     * @return a formatted date in the form of a string
     */
    @CompileStatic
    static String toJson(Calendar cal) {
        "\"${dateFormatter.get().format(cal.time)}\""
    }

    /**
     * @return the string representation of an uuid
     */
    @CompileStatic
    static String toJson(UUID uuid) {
        "\"${uuid.toString()}\""
    }

    /**
     * @return the string representation of the URL
     */
    @CompileStatic
    static String toJson(URL url) {
        "\"${url.toString()}\""
    }

    /**
     * @return an object representation of a closure
     */
    static String toJson(Closure closure) {
        toJson(JsonDelegate.cloneDelegateAndGetContent(closure))
    }

    /**
     * @return an object representation of an Expando
     */
    static String toJson(Expando expando) {
        toJson(expando.properties)
    }

    /**
     * @return "null" for a null value, or a JSON array representation for a collection, array, iterator or enumeration.
     */
    static String toJson(object) {
        if (object == null) {
            "null"
        } else if (object instanceof Collection ||
                object.class.isArray() ||
                object instanceof Iterator ||
                object instanceof Enumeration) {
            "[" + object.collect { toJson(it) }.join(',') + "]"
        } else if (object instanceof Enum) {
            '"' + object.name() + '"'
        } else {
            def properties = DefaultGroovyMethods.getProperties(object)
            properties.remove('class')
            properties.remove('declaringClass')
            properties.remove('metaClass')
            toJson(properties)
        }
    }

    /**
     * @return a JSON object representation for a map
     */
    static String toJson(Map m) {
        "{" + m.collect { k, v ->
                if (k == null) {
                    throw new IllegalArgumentException('Maps with null keys can\'t be converted to JSON')
                }
                toJson(k.toString()) + ':' + toJson(v)
        }.join(',') + "}"
    }

    /**
     * Pretty print a JSON payload
     * 
     * @param jsonPayload
     * @return
     */
    @CompileStatic
    static String prettyPrint(String jsonPayload) {
        int indent = 0
        def output = new StringBuilder()
        def lexer = new JsonLexer(new StringReader(jsonPayload))

        while (lexer.hasNext()) {
            JsonToken token = lexer.next()
            if (token.type == OPEN_CURLY) {
                indent += 4
                output.append('{\n')
                output.append(' ' * indent)
            } else if (token.type == CLOSE_CURLY) {
                indent -= 4
                output.append('\n')
                output.append(' ' * indent)
                output.append('}')
            } else if(token.type == OPEN_BRACKET) {
                indent += 4
                output.append('[\n')
                output.append(' ' * indent)
            } else if(token.type == CLOSE_BRACKET) {
                indent -= 4
                output.append('\n')
                output.append(' ' * indent)
                output.append(']')
            } else if (token.type == COMMA) {
                output.append(',\n')
                output.append(' ' * indent)
            } else if (token.type == COLON) {
                output.append(': ')
            } else if (token.type == STRING) {
                // Cannot use a range (1..-2) here as it will reverse for a string of
                // length 2 (i.e. textStr=/""/ ) and will not strip the leading/trailing
                // quotes (just reverses them).
                String textStr = token.text
                String textWithoutQuotes = textStr.substring( 1, textStr.size()-1 )
                output.append('"' + StringEscapeUtils.escapeJava( textWithoutQuotes ) + '"')
            } else {
                output.append(token.text)
            }
        }

        return output.toString()
    }
}
