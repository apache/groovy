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

import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class responsible for the actual String serialization of the possible values of a JSON structure.
 * This class can also be used as a category, so as to add <code>toJson()</code> methods to various types.
 * <p>
 * This class does not provide the ability to customize the resulting output.  A {@link JsonGenerator}
 * can be used if the ability to alter the resulting output is required.
 *
 * @see JsonGenerator
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

    static final char[] EMPTY_STRING_CHARS = Chr.array(QUOTE, QUOTE);
    static final char[] EMPTY_MAP_CHARS = {OPEN_BRACE, CLOSE_BRACE};
    static final char[] EMPTY_LIST_CHARS = {OPEN_BRACKET, CLOSE_BRACKET};

    /* package-private for use in builders */
    static final JsonGenerator DEFAULT_GENERATOR = new DefaultJsonGenerator(new JsonGenerator.Options());

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
        // Just a guess that the pretty view will take 20 percent more than original.
        final CharBuf output = CharBuf.create((int) (jsonPayload.length() * 1.2));

        JsonLexer lexer = new JsonLexer(new StringReader(jsonPayload));
        // Will store already created indents.
        Map<Integer, char[]> indentCache = new HashMap<>();
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
