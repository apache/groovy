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

/**
 * Represents a syntax error of a Groovy program, raised by the lexer or parser.
 * <p>
 * The message is the diagnostic as produced by the recogniser (for example
 * {@code Unclosed string literal} or {@code Unexpected character: '\u005cu200b'}).
 * Line and column are 1-based caret coordinates. On the compiler's normal lexer
 * path ({@link SyntaxErrorReportable#require} with {@code toAttachPositionInfo}
 * false), the location is not part of the message string —
 * {@link org.codehaus.groovy.syntax.SyntaxException} appends
 * {@code @ line N, column M} when wrapping the error. The public
 * {@link SyntaxErrorReportable#throwSyntaxError} path can still append
 * {@link PositionInfo} when {@code toAttachPositionInfo} is true (IDE /
 * highlighter callers).
 * </p>
 */
public class GroovySyntaxError extends AssertionError {
    public static final int LEXER = 0;
    public static final int PARSER = 1;
    private final int source;
    private final int line;
    private final int column;

    public GroovySyntaxError(String message, int source, int line, int column) {
        super(message, null);

        if (source != LEXER && source != PARSER) {
            throw new IllegalArgumentException("Invalid syntax error source: " + source);
        }

        this.source = source;
        this.line = line;
        this.column = column;
    }

    public int getSource() {
        return source;
    }
    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
