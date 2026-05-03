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
 * Exception thrown when a parse error is encountered by the parser.
 * Extends {@link TokenException} to provide token-aware error reporting
 * with position information from the parse stream.
 */
public class ParserException extends TokenException {
    @Serial
    private static final long serialVersionUID = -3772086239731735693L;

    /**
     * Constructs a ParserException from a token and error message.
     *
     * @param message the error message
     * @param token   the {@link Token} where the error occurred
     */
    public ParserException(String message, Token token) {
        super(message, token);
    }

    /**
     * Constructs a ParserException with explicit position and a cause.
     *
     * @param message      the error message
     * @param cause        the underlying {@link Throwable} that caused this exception
     * @param lineNumber   the line number where the error occurs (1-based)
     * @param columnNumber the column number where the error occurs (1-based)
     */
    public ParserException(String message, Throwable cause, int lineNumber, int columnNumber) {
        super(message, cause, lineNumber, columnNumber);
    }

    /**
     * Constructs a ParserException with explicit position range and a cause.
     *
     * @param message         the error message
     * @param cause           the underlying {@link Throwable} that caused this exception
     * @param lineNumber      the starting line number (1-based)
     * @param columnNumber    the starting column number (1-based)
     * @param endLineNumber   the ending line number (1-based)
     * @param endColumnNumber the ending column number (1-based)
     */
    public ParserException(String message, Throwable cause, int lineNumber, int columnNumber, int endLineNumber, int endColumnNumber) {
        super(message, cause, lineNumber, columnNumber, endLineNumber, endColumnNumber);
    }
}
