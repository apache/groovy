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

import static JsonTokenType.*

/**
 * Class responsible for the actual String serialization of the possible values of a JSON structure.
 * This class can also be used as a category, so as to add <code>toJson()</code> methods to various types.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class JsonOutput {

    /**
     * @return "true" or "false" for a boolean value
     */
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
     * @return a properly encoded string with escape sequences
     */
    static String toJson(String s) {
        if (!s) {
            '""'
        } else {
            def result = new StringBuilder('"')

            s.each { c ->
                switch (c) {
                    case '"':
                        result.append('\\"')
                        break
                    case '\\':
                        result.append('\\\\')
                        break
                    case '/':
                        result.append('\\/')
                        break
                    case '\b':
                        result.append('\\b')
                        break
                    case '\f':
                        result.append('\\f')
                        break
                    case '\n':
                        result.append('\\n')
                        break
                    case '\r':
                        result.append('\\r')
                        break
                    case '\t':
                        result.append('\\t')
                        break
                    default:
                        // control chars below space
                        if (c < ' ') {
                            result.append('\\u' + Integer.toHexString((int)c).padLeft(4, '0'))
                        } else {
                            result.append(c)
                        }
                }
            }

            result.append('"')
            result.toString()
        }
    }

    /**
     * @return an object representation of a closure
     */
    static String toJson(Closure closure) {
        toJson(JsonDelegate.cloneDelegateAndGetContent(closure))
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
        }
    }

    /**
     * @return a JSON object representation for a map
     */
    static String toJson(Map m) {
        "{" + m.collect { k, v -> toJson(k.toString()) + ':' + toJson(v) }.join(',') + "}"
    }

    /**
     * Pretty print a JSON payload
     * 
     * @param jsonPayload
     * @return
     */
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
            } else {
                output.append(token.text)
            }
        }

        return output.toString()
    }
}
