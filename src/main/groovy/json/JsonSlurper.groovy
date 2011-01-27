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
import groovy.io.LineColumnReader

/**
 * JSON slurper which parses text or reader content into a data structure of lists and maps.
 * <p>
 * Example usage:
 * <code><pre>
 * def slurper = new JsonSlurper()
 * def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 * 
 * assert result.person.name == "Guillaume"
 * assert result.person.age == 33
 * assert result.person.pets.size() == 2
 * assert result.person.pets[0] == "dog"
 * assert result.person.pets[1] == "cat"
 * </pre></code>
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class JsonSlurper {

    /**
     * Parse a text representation of a JSON data structure
     *
     * @param text JSON text to parse
     * @return a data structure of lists and maps
     */
    def parseText(String text) {
        parse(new LineColumnReader(new StringReader(text)))
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    def parse(Reader reader) {
        def content

        def lexer = new JsonLexer(reader)

        def token = lexer.nextToken()
        if (token.type == OPEN_CURLY) {
            content = parseObject(lexer)
        } else if (token.type == OPEN_BRACKET) {
            content = parseArray(lexer)
        } else {
            throw new JsonException(
                    "A JSON payload should start with ${OPEN_CURLY.label} or ${OPEN_BRACKET.label}.\n" +
                    "Instead, '${token.text}' was found on line: ${token.startLine}, column: ${token.startColumn}"
            )
        }
        
        return content
    }

    /**
     * Parse an array from the lexer
     *
     * @param lexer the lexer
     * @return a list of JSON values
     */
    private List parseArray(JsonLexer lexer) {
        def content = []

        def currentToken

        for(;;) {
            currentToken = lexer.nextToken()

            if (currentToken == null) {
                throw new JsonException(
                        "Expected a value on line: ${lexer.reader.line}, column: ${lexer.reader.column}.\n" +
                        "But got an unterminated array."
                )
            }

            if (currentToken.type == OPEN_CURLY) {
                content << parseObject(lexer)
            } else if (currentToken.type == OPEN_BRACKET) {
                content << parseArray(lexer)
            } else if (currentToken.type in [NUMBER, STRING, BOOL_TRUE, BOOL_FALSE, NULL]) {
                content << currentToken.value
            } else if (currentToken.type == CLOSE_BRACKET) {
                return content
            } else {
                throw new JsonException(
                        "Expected a value, an array, or an object on line: ${currentToken.startLine}, column: ${currentToken.startColumn}.\n" +
                        "But got '${currentToken.text}' instead."
                )
            }

            currentToken = lexer.nextToken()

            if (currentToken == null) {
                throw new JsonException(
                        "Expected ${CLOSE_BRACKET.label} or ${COMMA.label} on line: ${lexer.reader.line}, column: ${lexer.reader.column}.\n" +
                        "But got an unterminated array."
                )
            }

            // Expect a comma for an upcoming value
            // or a closing bracket for the end of the array
            if (currentToken.type == CLOSE_BRACKET) {
                break
            } else if (currentToken.type != COMMA) {
                throw new JsonException(
                        "Expected a value or ${CLOSE_BRACKET.label} on line: ${currentToken.startLine} column: ${currentToken.startColumn}.\n" +
                        "But got '${currentToken.text}' instead."
                )
            }
        }

        return content
    }

    /**
     * Parses an object from the lexer
     *
     * @param lexer the lexer
     * @return a Map representing a JSON object
     */
    private Map parseObject(JsonLexer lexer) {
        def content = [:]

        def previousToken
        def currentToken

        for(;;) {
            previousToken = currentToken
            currentToken = lexer.nextToken()

            // expect a string key, or already a closing curly brace

            if (currentToken.type == CLOSE_CURLY) {
                return content
            } else if (currentToken.type != STRING) {
                throw new JsonException(
                        "Expected ${STRING.label} key on line: ${currentToken.startLine}, column: ${currentToken.startColumn}.\n" +
                        "Bug got '${currentToken.text}' instead."
                )
            }

            String mapKey = currentToken.value

            previousToken = currentToken
            currentToken = lexer.nextToken()

            // expect a colon between the key and value pair

            if (currentToken.type != COLON) {
                throw new JsonException(
                        "Expected ${COLON.label} on line: ${currentToken.startLine}, column: ${currentToken.startColumn}.\n" +
                        "Bug got '${currentToken.text}' instead."
                )
            }

            previousToken = currentToken
            currentToken = lexer.nextToken()

            // value can be an object, an array, a number, string, boolean or null values

            if (currentToken.type == OPEN_CURLY) {
                content[mapKey] = parseObject(lexer)
            } else if (currentToken.type == OPEN_BRACKET) {
                content[mapKey] = parseArray(lexer)
            } else if (currentToken.type in [NUMBER, STRING, BOOL_TRUE, BOOL_FALSE, NULL]) {
                content[mapKey] = currentToken.value
            } else {
                throw new JsonException(
                        "Expected a value, an array, or an object on line: ${currentToken.startLine}, column: ${currentToken.startColumn}.\n" +
                        "But got '${currentToken.text}' instead."
                )
            }

            previousToken = currentToken
            currentToken = lexer.nextToken()

            // premature end of the object

            if (currentToken == null) {
                throw new JsonException(
                        "Expected ${CLOSE_CURLY.label} or ${COMMA.label} on line: ${previousToken.endLine}, column: ${previousToken.endColumn}.\n" +
                        "But got an unterminated object."
                )
            }

            // Expect a comma for an upcoming key/value pair
            // or a closing curly brace for the end of the object
            if (currentToken.type == CLOSE_CURLY) {
                break
            } else if (currentToken.type != COMMA) {
                throw new JsonException(
                        "Expected a value or ${CLOSE_CURLY.label} on line: ${currentToken.startLine}, column: ${currentToken.startColumn}.\n" +
                        "But got '${currentToken.text}' instead."
                )
            }
        }

        return content
    }

}
