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
package org.apache.groovy.parser.antlr4;

import org.codehaus.groovy.syntax.SyntaxException;

/**
 * Represents a syntax error of a Groovy program, raised by the lexer or parser.
 * <p>
 * The message is the diagnostic as produced by the recogniser (for example
 * {@code Unclosed string literal} or {@code Unexpected character: '\u005cu200b'}).
 * Line and column are 1-based caret coordinates. On the compiler's normal lexer
 * path ({@link SyntaxErrorReportable#require} with {@code toAttachPositionInfo}
 * false), the location is not part of the message string —
 * {@link SyntaxException} appends
 * {@code @ line N, column M} when wrapping the error. The public
 * {@link SyntaxErrorReportable#throwSyntaxError} path can still append
 * {@link PositionInfo} when {@code toAttachPositionInfo} is true (IDE /
 * highlighter callers).
 * </p>
 */
public class GroovySyntaxError extends AssertionError {
    /** {@code source} value for an error raised by the lexer. */
    public static final int LEXER = 0;
    /** {@code source} value for an error raised by the parser. */
    public static final int PARSER = 1;
    private final int source;
    private final int line;
    private final int column;

    /**
     * Creates an error with no cause. {@code source} must be {@link #LEXER} or {@link #PARSER}.
     * {@code line} and {@code column} are stored as given; callers supply 1-based caret coordinates.
     *
     * @param message the diagnostic text
     * @param source {@link #LEXER} or {@link #PARSER}
     * @param line the line stored for this error
     * @param column the column stored for this error
     * @throws IllegalArgumentException if {@code source} is neither {@link #LEXER} nor {@link #PARSER}
     */
    public GroovySyntaxError(String message, int source, int line, int column) {
        super(message, null);

        if (source != LEXER && source != PARSER) {
            throw new IllegalArgumentException("Invalid syntax error source: " + source);
        }

        this.source = source;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the recogniser that raised this error.
     *
     * @return {@link #LEXER} or {@link #PARSER}
     */
    public int getSource() {
        return source;
    }

    /**
     * Returns the line passed to the constructor.
     *
     * @return the stored line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the column passed to the constructor.
     *
     * @return the stored column
     */
    public int getColumn() {
        return column;
    }
}
