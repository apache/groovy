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

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import static groovy.json.JsonTokenType.FALSE;
import static groovy.json.JsonTokenType.NUMBER;
import static groovy.json.JsonTokenType.OPEN_CURLY;
import static groovy.json.JsonTokenType.STRING;
import static groovy.json.JsonTokenType.startingWith;

/**
 * The lexer reads JSON tokens in a streaming fashion from the underlying reader.
 *
 * @since 1.8.0
 */
public class JsonLexer implements Iterator<JsonToken> {

    private static final char SPACE    = ' ';
    private static final char DOT      = '.';
    private static final char MINUS    = '-';
    private static final char PLUS     = '+';
    private static final char LOWER_E  = 'e';
    private static final char UPPER_E  = 'E';
    private static final char ZERO     = '0';
    private static final char NINE     = '9';

    private final LineColumnReader reader;

    /**
     * Underlying reader from which to read the JSON tokens.
     * This reader is an instance of <code>LineColumnReader</code>,
     * to keep track of line and column positions.
     */
    public LineColumnReader getReader() {
        return reader;
    }

    private JsonToken currentToken = null;

    /**
     * Instantiates a lexer with a reader from which to read JSON tokens.
     * Under the hood, the reader is wrapped in a <code>LineColumnReader</code>,
     * for line and column information, unless it's already an instance of that class.
     *
     * @param reader underlying reader
     */
    public JsonLexer(Reader reader) {
        this.reader = reader instanceof LineColumnReader ? (LineColumnReader) reader : new LineColumnReader(reader);
    }

    /**
     * @return the next token from the stream
     */
    public JsonToken nextToken() {
        try {
            int firstIntRead = skipWhitespace();
            if (firstIntRead == -1) return null;

            char firstChar = (char) firstIntRead;
            JsonTokenType possibleTokenType = startingWith((char) firstIntRead);

            if (possibleTokenType == null) {
                throw new JsonException(
                        "Lexing failed on line: " + reader.getLine() + ", column: " + reader.getColumn() +
                                ", while reading '" + firstChar + "', " +
                                "no possible valid JSON value or punctuation could be recognized."
                );
            }

            reader.reset();
            long startLine = reader.getLine();
            long startColumn = reader.getColumn();

            JsonToken token = new JsonToken();
            token.setStartLine(startLine);
            token.setStartColumn(startColumn);
            token.setEndLine(startLine);
            token.setEndColumn(startColumn + 1);
            token.setType(possibleTokenType);
            token.setText("" + firstChar);

            if (possibleTokenType.ordinal() >= OPEN_CURLY.ordinal() && possibleTokenType.ordinal() <= FALSE.ordinal()) {
                return readingConstant(possibleTokenType, token);
            } else if (possibleTokenType == STRING) {
                StringBuilder currentContent = new StringBuilder("\"");
                // consume the first double quote starting the string
                reader.read();
                boolean isEscaped = false;
                for (;;) {
                    int read = reader.read();
                    if (read == -1) return null;

                    isEscaped = (!isEscaped && currentContent.charAt(currentContent.length() - 1) == '\\');

                    char charRead = (char) read;
                    currentContent.append(charRead);

                    if (charRead == '"' && !isEscaped &&
                            possibleTokenType.matching(currentContent.toString())) {
                        token.setEndLine(reader.getLine());
                        token.setEndColumn(reader.getColumn());
                        token.setText(unescape(currentContent.toString()));
                        return token;
                    }
                }
            } else if (possibleTokenType == NUMBER) {
                StringBuilder currentContent = new StringBuilder();
                for (;;) {
                    reader.mark(1);
                    int read = reader.read();
                    if (read == -1) return null;
                    char lastCharRead = (char) read;

                    if (lastCharRead >= ZERO && lastCharRead <= NINE ||
                            lastCharRead == DOT || lastCharRead == MINUS || lastCharRead == PLUS ||
                            lastCharRead == LOWER_E || lastCharRead == UPPER_E) {
                        currentContent.append(lastCharRead);
                    } else {
                        reader.reset();
                        break;
                    }
                }

                String content = currentContent.toString();
                if (possibleTokenType.matching(content)) {
                    token.setEndLine(reader.getLine());
                    token.setEndColumn(reader.getColumn());
                    token.setText(currentContent.toString());

                    return token;
                } else {
                    throwJsonException(currentContent.toString(), possibleTokenType);
                }
            }
            return null;
        } catch (IOException ioe) {
            throw new JsonException("An IO exception occurred while reading the JSON payload", ioe);
        }
    }

    private void throwJsonException(String content, JsonTokenType type) {
        throw new JsonException(
                "Lexing failed on line: " +
                reader.getLine() + ", column: " + reader.getColumn() +
                ", while reading '" + content + "', " +
                "was trying to match " + type.getLabel()
        );
    }

    /**
     * Replace unicode escape and other control characters with real characters
     *
     * @param input text
     * @return input text without the escaping
     */
    public static String unescape(String input) {
        return StringEscapeUtils.unescapeJavaScript(input);
    }

    /**
     * When a constant token type is expected, check that the expected constant is read,
     * and update the content of the token accordingly.
     *
     * @param type the token type
     * @param token the token
     * @return the token updated with end column and text updated
     */
    private JsonToken readingConstant(JsonTokenType type, JsonToken token) {
        try {
            int numCharsToRead = ((String) type.getValidator()).length();
            char[] chars = new char[numCharsToRead];
            reader.read(chars);
            String stringRead = new String(chars);

            if (stringRead.equals(type.getValidator())) {
                token.setEndColumn(token.getStartColumn() + numCharsToRead);
                token.setText(stringRead);
                return token;
            } else {
                throwJsonException(stringRead, type);
            }
        } catch (IOException ioe) {
            throw new JsonException("An IO exception occurred while reading the JSON payload", ioe);
        }
        return null;
    }

    /**
     * Skips all the whitespace characters and moves the cursor to the next non-space character.
     */
    public int skipWhitespace() {
        try {
            int readChar = 20;
            char c = SPACE;
            while (Character.isWhitespace(c)) {
                reader.mark(1);
                readChar = reader.read();
                c = (char) readChar;
            }
            reader.reset();
            return readChar;
        } catch (IOException ioe) {
            throw new JsonException("An IO exception occurred while reading the JSON payload", ioe);
        }
    }

    /**
     * Iterator method to know if another token follows,
     * or if we've reached the end of the stream.
     *
     * @return true if there are more tokens
     */
    @Override
    public boolean hasNext() {
        currentToken = nextToken();
        return currentToken != null;
    }

    /**
     * Iterator method to get the next token of the stream.
     *
     * @return the next token
     */
    @Override
    public JsonToken next() {
        return currentToken;
    }

    /**
     * Method not implemented.
     *
     * @throws UnsupportedOperationException
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("The method remove() is not supported on this lexer.");
    }
}
