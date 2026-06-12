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
package org.codehaus.groovy.syntax;

import java.io.Serial;

/**
 * Exception thrown when a token-related error is encountered during parsing.
 * Extends {@link SyntaxException} to provide lexical-level error information
 * using a {@link Token} as the error location reference.
 */
public class TokenException extends SyntaxException {
    @Serial private static final long serialVersionUID = 6850594285972085144L;

    /**
     * Constructs a TokenException from a token and error message.
     * The token's position information is used to determine error location.
     *
     * @param message the error message
     * @param token the {@link Token} where the error occurred (may be null)
     */
    public TokenException(String message, Token token) {
        super(
                (token == null)
                        ? message + ". No token"
                        : message,
                getLine(token),
                getColumn(token));
    }

    /**
     * Constructs a TokenException with explicit position and a cause.
     *
     * @param message the error message
     * @param cause the underlying {@link Throwable} that caused this exception
     * @param line the line number where the error occurs (1-based)
     * @param column the column number where the error occurs (1-based)
     */
    public TokenException(String message, Throwable cause, int line, int column) {
        super(message, cause, line, column);
    }

    /**
     * Constructs a TokenException with explicit position range and a cause.
     *
     * @param message the error message
     * @param cause the underlying {@link Throwable} that caused this exception
     * @param line the starting line number (1-based)
     * @param column the starting column number (1-based)
     * @param endLine the ending line number (1-based)
     * @param endColumn the ending column number (1-based)
     */
    public TokenException(String message, Throwable cause, int line, int column, int endLine, int endColumn) {
        super(message, cause, line, column, endLine, endColumn);
    }

    /**
     * Extracts the column number from a token, or -1 if the token is null.
     *
     * @param token the token (may be null)
     * @return the column number (1-based) or -1 if not available
     */
    private static int getColumn(Token token) {
        return (token != null) ? token.getStartColumn() : -1;
    }

    /**
     * Extracts the line number from a token, or -1 if the token is null.
     *
     * @param token the token (may be null)
     * @return the line number (1-based) or -1 if not available
     */
    private static int getLine(Token token) {
        return (token != null) ? token.getStartLine() : -1;
    }

}
