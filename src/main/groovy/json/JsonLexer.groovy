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
import static Matching.*
import groovy.io.LineColumnReader

/**
 * The lexer reads JSON tokens in a streaming fashion from the underlying reader.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class JsonLexer implements Iterator<JsonToken> {
    private static final Character SPACE = new Character(' ' as char)

    /**
     * Underlying reader from which to read the JSON tokens.
     * This reader is an instance of <code>LineColumnReader</code>,
     * to keep track of line and column positions.
     */
    LineColumnReader reader

    private JsonToken currentToken = null

    /**
     * Instanciates a lexer with a reader from which to read JSON tokens.
     * Under the hood, the reader is wrapped in a <code>LineColumnReader</code>,
     * for line and column information, unless it's already an instance of that class.
     *
     * @param reader underlying reader
     */
    JsonLexer(Reader reader) {
        this.reader = reader instanceof LineColumnReader ? reader : new LineColumnReader(reader)
    }

    /**
     * @return the next token from the stream
     */
    JsonToken nextToken() {
        int firstIntRead = skipWhitespace()
        if (firstIntRead == -1) return null

        char firstChar = (char)firstIntRead

        JsonTokenType possibleToken = tokenStartingWith((char)firstIntRead)

        if (possibleToken == null) {
            throw new JsonException(
                    "Lexing failed on line: ${reader.line}, column: ${reader.column}, while reading '${firstChar}', " +
                    "no possible valid JSON value or punctuation could be recognized."
            )
        }

        reader.reset()
        int startLine = reader.line
        int startColumn = reader.column

        JsonToken token = new JsonToken(
                startLine:  startLine,      startColumn:    startColumn,
                endLine:    startLine,      endColumn:      startColumn + 1,
                type:       possibleToken,  text:           firstChar
        )

        if (possibleToken in [OPEN_CURLY, CLOSE_CURLY, OPEN_BRACKET, CLOSE_BRACKET, COLON, COMMA, TRUE, FALSE, NULL]) {
            return readingConstant(possibleToken, token)
        } else if (possibleToken in [STRING, NUMBER]) {
            StringBuilder currentContent = new StringBuilder()
            for(;;) {
                reader.mark(1)
                int read = reader.read()
                if (read == -1) {
                    return null
                }
                currentContent.append((char)read)
                def matching = possibleToken.matching(currentContent.toString())

                if (matching == NO) {
                    if (possibleToken == NUMBER) {
                        reader.reset()
                        return token
                    } else {
                        throw new JsonException(
                                "Lexing failed on line: ${reader.line}, column: ${reader.column}, while reading '${currentContent}', " +
                                "was trying to match ${possibleToken.label}"
                        )
                    }
                } else if (matching == POSSIBLE) {
                    token.endLine = reader.line
                    token.endColumn = reader.column
                    token.text = currentContent.toString()
                }

                if (matching == YES) {
                    token.endLine = reader.line
                    token.endColumn = reader.column
                    token.text = currentContent.toString()

                    if (possibleToken == STRING) {
                        return token
                    }
                }
            }
        }
    }

    JsonToken readingConstant(JsonTokenType type, JsonToken token) {
        int numCharsToRead = type.validator.size()
        char[] chars = new char[numCharsToRead]
        reader.read(chars)
        def stringRead = new String(chars)

        if (stringRead == type.validator) {
            token.endColumn = token.startColumn + numCharsToRead
            token.text = stringRead
            return token
        } else {
            throw new JsonException(
                    "Lexing failed on line: ${reader.line}, column: ${reader.column}, while reading '${stringRead}', " +
                    "was trying to match ${type.label}"
            )
        }
    }

    JsonTokenType tokenStartingWith(char c) {
        switch (c) {
            case '{': return OPEN_CURLY
            case '}': return CLOSE_CURLY
            case '[': return OPEN_BRACKET
            case ']': return CLOSE_BRACKET
            case ',': return COMMA
            case ':': return COLON

            case 't': return TRUE
            case 'f': return FALSE
            case 'n': return NULL

            case '"': return STRING

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
                return NUMBER
        }
    }

    /**
     * Skips all the whitespace characters and moves the cursor to the next non-space character.
     */
    int skipWhitespace() {
        int readChar = 20
        Character c = SPACE
        while(c.isWhitespace()) {
            reader.mark(1)
            readChar = reader.read()
            c = new Character((char)readChar)
        }
        reader.reset()
        return readChar
    }

    /**
     * Iterator method to know if another token follows,
     * or if we've reached the end of the stream.
     *
     * @return true if there are more tokens
     */
    boolean hasNext() {
        currentToken = nextToken()
        return currentToken != null
    }

    /**
     * Iterator method to get the next token of the stream.
     *
     * @return the next token
     */
    JsonToken next() {
        return currentToken
    }

    /**
     * Method not implemented.
     *
     * @throws UnsupportedOperationException
     */
    void remove() {
        throw new UnsupportedOperationException("The method remove() is not supported on this lexer.")
    }

}