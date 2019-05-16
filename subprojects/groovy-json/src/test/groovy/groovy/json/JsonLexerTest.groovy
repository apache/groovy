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
package groovy.json

import groovy.io.LineColumnReader
import groovy.test.GroovyTestCase

import static groovy.json.JsonTokenType.CLOSE_BRACKET
import static groovy.json.JsonTokenType.CLOSE_CURLY
import static groovy.json.JsonTokenType.COLON
import static groovy.json.JsonTokenType.COMMA
import static groovy.json.JsonTokenType.FALSE
import static groovy.json.JsonTokenType.NULL
import static groovy.json.JsonTokenType.NUMBER
import static groovy.json.JsonTokenType.OPEN_BRACKET
import static groovy.json.JsonTokenType.OPEN_CURLY
import static groovy.json.JsonTokenType.TRUE

class JsonLexerTest extends GroovyTestCase {

    void testSkipWhitespace() {
        def content = "    true"
        def reader = new LineColumnReader(new StringReader(content))

        def lexer = new JsonLexer(reader)
        lexer.skipWhitespace()

        def charsLeftRead = new char[4]
        reader.read(charsLeftRead)

        assert new String(charsLeftRead) == "true"
    }

    void testNextToken() {
        def content = [" true ", " false ", " null ", " -123.456e78 ", " [ ", " ] ", " { ", " } ", " : ", " , "]

        assert content.collect {
            def lexer = new JsonLexer(new StringReader(it))
            lexer.nextToken().type
        } == [
                TRUE, FALSE, NULL, NUMBER,
                OPEN_BRACKET, CLOSE_BRACKET, OPEN_CURLY, CLOSE_CURLY, COLON, COMMA
        ]
    }

    void testSuiteOfTokens() {
        def content = ' [ true, null, false, { "a" : 1, "b": "hi"}, 12.34 ] '
        def lexer = new JsonLexer(new StringReader(content))

        def output = lexer.collect { it.toString() }

        assert output == [
                "[ (OPEN_BRACKET) [1:2-1:3]",
                "true (TRUE) [1:4-1:8]",
                ", (COMMA) [1:8-1:9]",
                "null (NULL) [1:10-1:14]",
                ", (COMMA) [1:14-1:15]",
                "false (FALSE) [1:16-1:21]",
                ", (COMMA) [1:21-1:22]",
                "{ (OPEN_CURLY) [1:23-1:24]",
                '"a" (STRING) [1:25-1:28]',
                ": (COLON) [1:29-1:30]",
                "1 (NUMBER) [1:31-1:32]",
                ", (COMMA) [1:32-1:33]",
                '"b" (STRING) [1:34-1:37]',
                ": (COLON) [1:37-1:38]",
                '"hi" (STRING) [1:39-1:43]',
                "} (CLOSE_CURLY) [1:43-1:44]",
                ", (COMMA) [1:44-1:45]",
                "12.34 (NUMBER) [1:46-1:51]",
                "] (CLOSE_BRACKET) [1:52-1:53]"
        ]
    }

    void testBeginningOfAValidToken() {
        def content = "  truaaa "
        def lexer = new JsonLexer(new StringReader(content))

        def msg = shouldFail(JsonException) {
            lexer.nextToken()
        }

        assert msg.contains("trua")
        assert msg.contains("constant 'true'")
        assert msg.contains("column: 7")
    }

    void testBeginningOfValidNumber() {
        def content = " 2134.432a"
        def lexer = new JsonLexer(new StringReader(content))

        assert lexer.nextToken().text == "2134.432"
        shouldFail(JsonException) {
            lexer.nextToken()
        }
    }

    void testIteratorRemoveUnimplemented() {
        def lexer = new JsonLexer(new StringReader(""))

        shouldFail(UnsupportedOperationException) {
            lexer.remove()
        }
    }

    void testUnescapingWithLexer() {
        // use string concatenation so that the unicode escape characters are not decoded 
        // by Groovy's lexer but by the JsonLexer
        def lexer = new JsonLexer(new StringReader('"\\' + 'u004A\\' + 'u0053\\' + 'u004F\\' + 'u004E"'))

        assert lexer.nextToken().value == 'JSON'
    }

    void testUnescaping() {
        assert JsonLexer.unescape('\\b') == '\b'
        assert JsonLexer.unescape('\\f') == '\f'
        assert JsonLexer.unescape('\\n') == '\n'
        assert JsonLexer.unescape('\\r') == '\r'
        assert JsonLexer.unescape('\\t') == '\t'
        assert JsonLexer.unescape('\\\\') == '\\'
        assert JsonLexer.unescape('\\/') == '/'
        assert JsonLexer.unescape('\\"') == '"'

        // use string concatenation so that the unicode escape characters are not decoded
        // by Groovy's lexer but by the JsonLexer
        assert JsonLexer.unescape('\\' + 'u004A\\' + 'u0053\\' + 'u004F\\' + 'u004E') == 'JSON'
    }

    void testBackSlashEscaping() {
        def lexer = new JsonLexer(new StringReader('["Guill\\\\aume"]'))

        assert lexer.nextToken().type == JsonTokenType.OPEN_BRACKET
        assert lexer.nextToken().value == "Guill\\aume"
        assert lexer.nextToken().type == JsonTokenType.CLOSE_BRACKET
        assert lexer.nextToken() == null

        lexer = new JsonLexer(new StringReader('["c:\\\\"]'))

        assert lexer.nextToken().type == JsonTokenType.OPEN_BRACKET
        assert lexer.nextToken().value == "c:\\"
        assert lexer.nextToken().type == JsonTokenType.CLOSE_BRACKET
        assert lexer.nextToken() == null
    }
}
