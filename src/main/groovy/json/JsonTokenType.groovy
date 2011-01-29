/*
 * Copyright 2003-2011 the original author or authors.
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

import java.util.regex.Pattern
import static Matching.*

/**
 * Enum listing all the possible JSON tokens that should be recognized by the lexer.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
enum JsonTokenType {
    OPEN_CURLY      ( "an openning curly brace '{'",        '{'         ),
    CLOSE_CURLY     ( "a closing curly brace '}'",          '}'         ),
    OPEN_BRACKET    ( "an openning square bracket '['",     '['         ),
    CLOSE_BRACKET   ( "a closing square bracket ']'",       ']'         ),
    COLON           ( "a colon ':'",                        ':'         ),
    COMMA           ( "a comma ','",                        ','         ),
    NULL            ( "the constant 'null'",                'null'      ),
    TRUE            ( "the constant 'true'",                'true'      ),
    FALSE           ( "the constant 'false'",               'false'     ),
    NUMBER          ( "a number",                           ~/-?\d+(\.\d+)?((e|E)(\+|-)?\d+)?/  ),
    STRING          ( "a string",                           ~/"([^"\\]|\\(["\\\/bfnrt]|u[0-9a-fA-F]{4}))*"/ )

    /**
     * A String constant or a Pattern, serving as a validator for matching tokens.
     */
    Object validator

    /**
     * A label describing the token
     */
    String label

    /**
     * Construct a token type with a label and a validator
     *
     * @param label a label describing the token
     * @param validator a String or Pattern validating input strings as valid tokens
     */
    JsonTokenType(String label, Object validator) {
        this.validator = validator
        this.label = label
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
    Matching matching(String input) {
        if (validator instanceof Pattern) {
            def matcher = validator.matcher(input)
            if (matcher.matches()) {
                return YES
            } else if (matcher.hitEnd()) {
                return POSSIBLE
            } else {
                return NO
            }
        } else if (validator instanceof String) {
            if (input == validator) {
                return YES
            } else if (validator.startsWith(input)) {
                return POSSIBLE
            } else {
                return NO
            }
        }
    }
}
