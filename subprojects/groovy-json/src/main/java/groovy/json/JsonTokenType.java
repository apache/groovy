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
    OPEN_CURLY      ( "an openning curly brace '{'",        "{"         ),
    CLOSE_CURLY     ( "a closing curly brace '}'",          "}"         ),
    OPEN_BRACKET    ( "an openning square bracket '['",     "["         ),
    CLOSE_BRACKET   ( "a closing square bracket ']'",       "]"         ),
    COLON           ( "a colon ':'",                        ":"         ),
    COMMA           ( "a comma ','",                        ","         ),
    NULL            ( "the constant 'null'",                "null"      ),
    TRUE            ( "the constant 'true'",                "true"      ),
    FALSE           ( "the constant 'false'",               "false"     ),
    NUMBER          ( "a number",                           Pattern.compile("-?\\d+(\\.\\d+)?((e|E)(\\+|-)?\\d+)?")),
    //STRING          ( "a string",                           Pattern.compile("\"([^\"\\\\]*|\\\\[\"\\\\bfnrt\\/]|\\\\u[0-9a-fA-F]{4})*\"", Pattern.DOTALL));
    /**
     * Original pattern throws the StackOverflowError for long strings with backslashes.
     * So it is replaced by a 2-step approach inspired from json2.js sources:
     *     https://github.com/douglascrockford/JSON-js/blob/master/json2.js#L462
     *
     * See JsonTokenTypeTest#testMatchingLongStringWithBackslashes() for details.
     */
    STRING          ( "a string",                           new Closure(null) {
        private Pattern replacePattern = Pattern.compile("(?:\\\\[\"\\\\bfnrt\\/]|\\\\u[0-9a-fA-F]{4})");
        private Pattern validatePattern = Pattern.compile("\"[^\"\\\\]*\"");
        boolean doCall(String it) {
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
        switch (c) {
            case '{': return OPEN_CURLY;
            case '}': return CLOSE_CURLY;
            case '[': return OPEN_BRACKET;
            case ']': return CLOSE_BRACKET;
            case ',': return COMMA;
            case ':': return COLON;

            case 't': return TRUE;
            case 'f': return FALSE;
            case 'n': return NULL;

            case '"': return STRING;

            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return NUMBER;
        }
        return null;
    }

    public String getLabel() {
        return label;
    }

    public Object getValidator() {
        return validator;
    }
}
