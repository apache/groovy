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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The original slurper and lexer use this class.
 * This is kept around in case someone needs its exact behavior.
 * Enum listing all the possible JSON tokens that should be recognized by the lexer.
 *
 * @since 1.8.0
 */
public enum JsonTokenType {
    /** Opening curly brace token. */
    OPEN_CURLY      ( "an opening curly brace '{'",        "{"         ),
    /** Closing curly brace token. */
    CLOSE_CURLY     ( "a closing curly brace '}'",          "}"         ),
    /** Opening square bracket token. */
    OPEN_BRACKET    ( "an opening square bracket '['",     "["         ),
    /** Closing square bracket token. */
    CLOSE_BRACKET   ( "a closing square bracket ']'",       "]"         ),
    /** Colon token. */
    COLON           ( "a colon ':'",                        ":"         ),
    /** Comma token. */
    COMMA           ( "a comma ','",                        ","         ),
    /** {@code null} literal token. */
    NULL            ( "the constant 'null'",                "null"      ),
    /** {@code true} literal token. */
    TRUE            ( "the constant 'true'",                "true"      ),
    /** {@code false} literal token. */
    FALSE           ( "the constant 'false'",               "false"     ),
    /** Numeric literal token. */
    NUMBER          ( "a number",                           Pattern.compile("-?\\d+(\\.\\d+)?((e|E)(\\+|-)?\\d+)?")),
    /**
     * String literal token.
     * <p>
     * Original pattern throws the {@link StackOverflowError} for long strings with backslashes.
     * It is therefore replaced by a 2-step approach inspired by json2.js sources:
     *     https://github.com/douglascrockford/JSON-js/blob/master/json2.js#L462
     *
     * See JsonTokenTypeTest#testMatchingLongStringWithBackslashes() for details.
     */
    STRING          ( "a string",                           new Closure(null) {
        private Pattern replacePattern = Pattern.compile("(?:\\\\[\"\\\\bfnrt\\/]|\\\\u[0-9a-fA-F]{4})");
        private Pattern validatePattern = Pattern.compile("\"[^\"\\\\]*\"");

        /**
         * Validates a candidate JSON string token.
         *
         * @param it the candidate token text
         * @return {@code true} if the text is a valid JSON string token
         */
        public boolean doCall(String it) {
            return validatePattern.matcher(replacePattern.matcher(it).replaceAll("@")).matches();
        }
    });

    /**
     * A String constant or a Pattern, serving as a validator for matching tokens.
     */
    private final Object validator;

    /**
     * A label describing the token
     */
    private final String label;

    /**
     * Construct a token type with a label and a validator
     *
     * @param label a label describing the token
     * @param validator a String or Pattern validating input strings as valid tokens
     */
    JsonTokenType(String label, Object validator) {
        this.validator = validator;
        this.label = label;
    }

    /**
     * Tells if an input string matches a token.
     *
     * @param input the input string to match
     *
     * @return a <code>Matching</code> enum value:
     * <code>YES</code> if this is an exact match,
     * <code>POSSIBLE</code> if more characters could turn the input string into a valid token,
     * or <code>NO</code> if the string cannot possibly match the pattern even with more characters to read.
     */
    public boolean matching(String input) {
        if (validator instanceof Pattern) {
            Matcher matcher = ((Pattern) validator).matcher(input);
            return matcher.matches();
        } else if (validator instanceof Closure) {
            return (Boolean) ((Closure) validator).call(input);
        } else if (validator instanceof String) {
            return input.equals(validator);
        } else {
            return false;
        }
    }

    /**
     * Find which JSON value might be starting with a given character
     *
     * @param c the character
     * @return the possible token type found
     */
    public static JsonTokenType startingWith(char c) {
        return switch (c) {
            case '{' -> OPEN_CURLY;
            case '}' -> CLOSE_CURLY;
            case '[' -> OPEN_BRACKET;
            case ']' -> CLOSE_BRACKET;
            case ',' -> COMMA;
            case ':' -> COLON;
            case 't' -> TRUE;
            case 'f' -> FALSE;
            case 'n' -> NULL;
            case '"' -> STRING;
            case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> NUMBER;
            default -> null;
        };
    }

    /**
     * Returns the human-readable description of this token type.
     *
     * @return the token label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Returns the validator used to recognize this token type.
     *
     * @return the token validator
     */
    public Object getValidator() {
        return validator;
    }
}
