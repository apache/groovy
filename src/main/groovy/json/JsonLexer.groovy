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
        skipWhitespace()

        JsonToken token = null
        StringBuilder tokenContent = new StringBuilder()
        Collection<JsonTokenType> possibleTokenTypes = JsonTokenType.values()

        int startColumn = reader.column
        int startLine = reader.line

        for(;;) {
            reader.mark(1)
            int read = reader.read()
            if (read == -1) return token

            char c = (char)read
            tokenContent.append(c)
            String currentContent = tokenContent.toString()

            // reduce the possible tokens
            def currentTokenTypes = possibleTokenTypes.findAll { it.matching(currentContent) != NO }

            if (currentTokenTypes.size() == 0) {
                if (possibleTokenTypes.size() == 1 && possibleTokenTypes[0] == NUMBER) {
                    reader.reset()
                    return token
                } else {
                    throw new JsonException(
                            "Lexing failed on line: ${reader.line}, column: ${reader.column}, while reading '${currentContent}', " +
                            "was trying to match ${possibleTokenTypes.collect { it.label }.join(', ')}"
                    )
                }
            } else if (currentTokenTypes.size() == 1) {
                def matching = currentTokenTypes[0].matching(currentContent)
                if (matching == YES) {
                    token = new JsonToken()
                    token.startColumn = startColumn
                    token.startLine = startLine
                    token.endColumn = reader.column
                    token.endLine = reader.line
                    token.type = currentTokenTypes[0]
                    token.text = currentContent

                    // we return the token, unless we're trying to match a number
                    // as numbers should be matched eagerly, as more input can be part of the number
                    // (ie. 12.34 is a valid number, but if there's another digit 5 after that,
                    // then 12.345 is also a valid number, so read as much as possible)
                    if (currentTokenTypes[0] != NUMBER) {
                        return token
                    }
                }
            }
            
            // reduce the possibilities for the next loop
            possibleTokenTypes = currentTokenTypes
        }
    }

    /**
     * Skips all the whitespace characters and moves the cursor to the next non-space character.
     */
    void skipWhitespace() {
        Character c = SPACE

        while(c.isWhitespace()) {
            reader.mark(1)
            c = new Character((char)reader.read())
        }

        reader.reset()
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