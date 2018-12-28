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

import groovy.io.LineColumnReader;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static groovy.json.JsonTokenType.CLOSE_BRACKET;
import static groovy.json.JsonTokenType.CLOSE_CURLY;
import static groovy.json.JsonTokenType.COLON;
import static groovy.json.JsonTokenType.COMMA;
import static groovy.json.JsonTokenType.NULL;
import static groovy.json.JsonTokenType.OPEN_BRACKET;
import static groovy.json.JsonTokenType.OPEN_CURLY;
import static groovy.json.JsonTokenType.STRING;

/**
 * This is the original slurper included in case someone relies on its exact behavior.
 *
 * JSON slurper which parses text or reader content into a data structure of lists and maps.
 * <p>
 * Example usage:
 * <code><pre class="groovyTestCase">
 * def slurper = new groovy.json.JsonSlurperClassic()
 * def result = slurper.parseText('{"person":{"name":"Guillaume","age":33,"pets":["dog","cat"]}}')
 *
 * assert result.person.name == "Guillaume"
 * assert result.person.age == 33
 * assert result.person.pets.size() == 2
 * assert result.person.pets[0] == "dog"
 * assert result.person.pets[1] == "cat"
 * </pre></code>
 *
 * @since 1.8.0
 */
public class JsonSlurperClassic {

    /**
     * Parse a text representation of a JSON data structure
     *
     * @param text JSON text to parse
     * @return a data structure of lists and maps
     */
    public Object parseText(String text) {
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("The JSON input text should neither be null nor empty.");
        }

        return parse(new LineColumnReader(new StringReader(text)));
    }

    /**
     * Parse a JSON data structure from content from a reader
     *
     * @param reader reader over a JSON content
     * @return a data structure of lists and maps
     */
    public Object parse(Reader reader) {
        Object content;

        JsonLexer lexer = new JsonLexer(reader);

        JsonToken token = lexer.nextToken();
        if (token.getType() == OPEN_CURLY) {
            content = parseObject(lexer);
        } else if (token.getType() == OPEN_BRACKET) {
            content = parseArray(lexer);
        } else {
            throw new JsonException(
                    "A JSON payload should start with " + OPEN_CURLY.getLabel() +
                            " or " + OPEN_BRACKET.getLabel() + ".\n" +
                            "Instead, '" + token.getText() + "' was found " +
                            "on line: " + token.getStartLine() + ", " +
                            "column: " + token.getStartColumn()
            );
        }

        return content;
    }

    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file) {
        return parseFile(file, null);
    }

    /**
     * Parse a JSON data structure from content within a given File.
     *
     * @param file File containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(File file, String charset) {
        return parseFile(file, charset);
    }

    private Object parseFile(File file, String charset) {
        Reader reader = null;
        try {
            if (charset == null || charset.length() == 0) {
                reader = ResourceGroovyMethods.newReader(file);
            } else {
                reader = ResourceGroovyMethods.newReader(file, charset);
            }
            return parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process file: " + file.getPath(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url) {
        return parseURL(url, null);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params) {
        return parseURL(url, params);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url) {
        return parseURL(url, params);
    }

    private Object parseURL(URL url, Map params) {
        Reader reader = null;
        try {
            if (params == null || params.isEmpty()) {
                reader = ResourceGroovyMethods.newReader(url);
            } else {
                reader = ResourceGroovyMethods.newReader(url, params);
            }
            return parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process url: " + url.toString(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, String charset) {
        return parseURL(url, null, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL.
     *
     * @param url URL containing JSON content
     * @param params connection parameters
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(URL url, Map params, String charset) {
        return parseURL(url, params, charset);
    }

    /**
     * Parse a JSON data structure from content at a given URL. Convenience variant when using Groovy named parameters for the connection params.
     *
     * @param params connection parameters
     * @param url URL containing JSON content
     * @param charset the charset for this File
     * @return a data structure of lists and maps
     * @since 2.2.0
     */
    public Object parse(Map params, URL url, String charset) {
        return parseURL(url, params, charset);
    }

    private Object parseURL(URL url, Map params, String charset) {
        Reader reader = null;
        try {
            if (params == null || params.isEmpty()) {
                reader = ResourceGroovyMethods.newReader(url, charset);
            } else {
                reader = ResourceGroovyMethods.newReader(url, params, charset);
            }
            return parse(reader);
        } catch (IOException ioe) {
            throw new JsonException("Unable to process url: " + url.toString(), ioe);
        } finally {
            if (reader != null) {
                DefaultGroovyMethodsSupport.closeWithWarning(reader);
            }
        }
    }

    /**
     * Parse an array from the lexer
     *
     * @param lexer the lexer
     * @return a list of JSON values
     */
    private List parseArray(JsonLexer lexer) {
        List content = new ArrayList();

        JsonToken currentToken;

        for(;;) {
            currentToken = lexer.nextToken();

            if (currentToken == null) {
                throw new JsonException(
                        "Expected a value on line: " + lexer.getReader().getLine() + ", " +
                                "column: " + lexer.getReader().getColumn() + ".\n" +
                                "But got an unterminated array."
                );
            }

            if (currentToken.getType() == OPEN_CURLY) {
                content.add(parseObject(lexer));
            } else if (currentToken.getType() == OPEN_BRACKET) {
                content.add(parseArray(lexer));
            } else if (currentToken.getType().ordinal() >= NULL.ordinal()) {
                content.add(currentToken.getValue());
            } else if (currentToken.getType() == CLOSE_BRACKET) {
                return content;
            } else {
                throw new JsonException(
                        "Expected a value, an array, or an object " +
                                "on line: " + currentToken.getStartLine() + ", " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }

            currentToken = lexer.nextToken();

            if (currentToken == null) {
                throw new JsonException(
                        "Expected " + CLOSE_BRACKET.getLabel() + " " +
                                "or " + COMMA.getLabel() + " " +
                                "on line: " + lexer.getReader().getLine() + ", " +
                                "column: " + lexer.getReader().getColumn() + ".\n" +
                                "But got an unterminated array."
                );
            }

            // Expect a comma for an upcoming value
            // or a closing bracket for the end of the array
            if (currentToken.getType() == CLOSE_BRACKET) {
                break;
            } else if (currentToken.getType() != COMMA) {
                throw new JsonException(
                        "Expected a value or " + CLOSE_BRACKET.getLabel() + " " +
                                "on line: " + currentToken.getStartLine() + " " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }
        }

        return content;
    }

    /**
     * Parses an object from the lexer
     *
     * @param lexer the lexer
     * @return a Map representing a JSON object
     */
    private Map parseObject(JsonLexer lexer) {
        Map content = new HashMap();

        JsonToken previousToken = null;
        JsonToken currentToken = null;

        for(;;) {
            currentToken = lexer.nextToken();

            if (currentToken == null) {
                throw new JsonException(
                        "Expected a String key on line: " + lexer.getReader().getLine() + ", " +
                                "column: " + lexer.getReader().getColumn() + ".\n" +
                                "But got an unterminated object."
                );
            }

            // expect a string key, or already a closing curly brace

            if (currentToken.getType() == CLOSE_CURLY) {
                return content;
            } else if (currentToken.getType() != STRING) {
                throw new JsonException(
                        "Expected " + STRING.getLabel() + " key " +
                                "on line: " + currentToken.getStartLine() + ", " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }

            String mapKey = (String) currentToken.getValue();

            currentToken = lexer.nextToken();

            if (currentToken == null) {
                throw new JsonException(
                        "Expected a " + COLON.getLabel() + " " +
                                "on line: " + lexer.getReader().getLine() + ", " +
                                "column: " + lexer.getReader().getColumn() + ".\n" +
                                "But got an unterminated object."
                );
            }

            // expect a colon between the key and value pair

            if (currentToken.getType() != COLON) {
                throw new JsonException(
                        "Expected " + COLON.getLabel() + " " +
                                "on line: " + currentToken.getStartLine() + ", " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }

            currentToken = lexer.nextToken();

            if (currentToken == null) {
                throw new JsonException(
                        "Expected a value " +
                                "on line: " + lexer.getReader().getLine() + ", " +
                                "column: " + lexer.getReader().getColumn() + ".\n" +
                                "But got an unterminated object."
                );
            }

            // value can be an object, an array, a number, string, boolean or null values

            if (currentToken.getType() == OPEN_CURLY) {
                content.put(mapKey, parseObject(lexer));
            } else if (currentToken.getType() == OPEN_BRACKET) {
                content.put(mapKey, parseArray(lexer));
            } else if (currentToken.getType().ordinal() >= NULL.ordinal()) {
                content.put(mapKey, currentToken.getValue());
            } else {
                throw new JsonException(
                        "Expected a value, an array, or an object " +
                                "on line: " + currentToken.getStartLine() + ", " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }

            previousToken = currentToken;
            currentToken = lexer.nextToken();

            // premature end of the object

            if (currentToken == null) {
                throw new JsonException(
                        "Expected " + CLOSE_CURLY.getLabel() + " or " + COMMA.getLabel() + " " +
                                "on line: " + previousToken.getEndLine() + ", " +
                                "column: " + previousToken.getEndColumn() + ".\n" +
                                "But got an unterminated object."
                );
            }

            // Expect a comma for an upcoming key/value pair
            // or a closing curly brace for the end of the object
            if (currentToken.getType() == CLOSE_CURLY) {
                break;
            } else if (currentToken.getType() != COMMA) {
                throw new JsonException(
                        "Expected a value or " + CLOSE_CURLY.getLabel() + " " +
                                "on line: " + currentToken.getStartLine() + ", " +
                                "column: " + currentToken.getStartColumn() + ".\n" +
                                "But got '" + currentToken.getText() + "' instead."
                );
            }
        }

        return content;
    }
}
