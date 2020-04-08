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
package org.codehaus.groovy.antlr;

import antlr.CharScanner;
import antlr.Token;
import antlr.TokenStreamException;

import java.io.IOException;
import java.io.Reader;

/**
 * Translates GLS-defined unicode escapes into characters. Throws an exception
 * in the event of an invalid unicode escape being detected.
 * <p>
 * No attempt has been made to optimize this class for speed or space.
 */
@Deprecated
public class UnicodeEscapingReader extends Reader {

    private final Reader reader;
    private CharScanner lexer;
    private boolean hasNextChar = false;
    private int nextChar;
    private final SourceBuffer sourceBuffer;
    private int previousLine;
    private int numUnicodeEscapesFound = 0;
    private int numUnicodeEscapesFoundOnCurrentLine = 0;

    private static class DummyLexer extends CharScanner {
        private final Token t = new Token();
        public Token nextToken() throws TokenStreamException {
            return t;
        }
        @Override
        public int getColumn() {
            return 0;
        }
        @Override
        public int getLine() {
            return 0;
        }
    }
    
    /**
     * Constructor.
     * @param reader The reader that this reader will filter over.
     */
    public UnicodeEscapingReader(Reader reader,SourceBuffer sourceBuffer) {
        this.reader = reader;
        this.sourceBuffer = sourceBuffer;
        this.lexer = new DummyLexer();
    }

    /**
     * Sets the lexer that is using this reader. Must be called before the
     * lexer is used.
     */
    public void setLexer(CharScanner lexer) {
        this.lexer = lexer;
    }

    /**
     * Reads characters from the underlying reader.
     * @see java.io.Reader#read(char[],int,int)
     */
    public int read(char cbuf[], int off, int len) throws IOException {
        int c = 0;
        int count = 0;
        while (count < len && (c = read())!= -1) {
            cbuf[off + count] = (char) c;
            count++;
        }
        return (count == 0 && c == -1) ? -1 : count;
    }

    /**
     * Gets the next character from the underlying reader,
     * translating escapes as required.
     * @see java.io.Reader#close()
     */
    public int read() throws IOException {
        if (hasNextChar) {
            hasNextChar = false;
            write(nextChar);
            return nextChar;
        }

        if (previousLine != lexer.getLine()) {
            // new line, so reset unicode escapes
            numUnicodeEscapesFoundOnCurrentLine = 0;
            previousLine = lexer.getLine();
        }
        
        int c = reader.read();
        if (c != '\\') {
            write(c);
            return c;
        }

        // Have one backslash, continue if next char is 'u'
        c = reader.read();
        if (c != 'u') {
            hasNextChar = true;
            nextChar = c;
            write('\\');
            return '\\';
        }

        // Swallow multiple 'u's
        int numberOfUChars = 0;
        do {
            numberOfUChars++;
            c = reader.read();
        } while (c == 'u');

        // Get first hex digit
        checkHexDigit(c);
        StringBuilder charNum = new StringBuilder();
        charNum.append((char) c);

        // Must now be three more hex digits
        for (int i = 0; i < 3; i++) {
            c = reader.read();
            checkHexDigit(c);
            charNum.append((char) c);
        }
        int rv = Integer.parseInt(charNum.toString(), 16);
        write(rv);
        
        numUnicodeEscapesFound += 4 + numberOfUChars;
        numUnicodeEscapesFoundOnCurrentLine += 4 + numberOfUChars;

        return rv;
    }
    private void write(int c) {
        if (sourceBuffer != null) {sourceBuffer.write(c);}
    }
    /**
     * Checks that the given character is indeed a hex digit.
     */
    private void checkHexDigit(int c) throws IOException {
        if (c >= '0' && c <= '9') {
            return;
        }
        if (c >= 'a' && c <= 'f') {
            return;
        }
        if (c >= 'A' && c <= 'F') {
            return;
        }
        // Causes the invalid escape to be skipped
        hasNextChar = true;
        nextChar = c;
        throw new IOException("Did not find four digit hex character code."
                + " line: " + lexer.getLine() + " col:" + lexer.getColumn());
    }

    public int getUnescapedUnicodeColumnCount() {
        return numUnicodeEscapesFoundOnCurrentLine;
    }

    public int getUnescapedUnicodeOffsetCount() {
        return numUnicodeEscapesFound;
    }

    /**
     * Closes this reader by calling close on the underlying reader.
     *
     * @see java.io.Reader#close()
     */
    public void close() throws IOException {
        reader.close();
    }
}
