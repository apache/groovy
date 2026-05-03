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
 * Exception thrown when a parser encounters a token of an unexpected type.
 * Contains information about both the unexpected token found and the expected token type.
 * Extends {@link TokenException} to inherit token-based error reporting.
 */
public class TokenMismatchException extends TokenException {
    @Serial private static final long serialVersionUID = -6321206176010272124L;
    /** The token that was encountered but was not expected. */
    private final Token unexpectedToken;
    /** The token type that was expected instead. */
    private final int expectedType;

    /**
     * Constructs a TokenMismatchException.
     *
     * @param token the unexpected {@link Token} that was encountered
     * @param expectedType the expected token type from {@link Types}
     */
    public TokenMismatchException(Token token, int expectedType) {
        super("Expected token: " + expectedType + " but found: " + token, token);
        this.unexpectedToken = token;
        this.expectedType = expectedType;
    }

    /**
     * Returns the unexpected token that triggered this exception.
     *
     * @return the unexpected {@link Token}
     */
    public Token getUnexpectedToken() {
        return this.unexpectedToken;
    }

    /**
     * Returns the token type that was expected.
     *
     * @return the expected token type from {@link Types}
     */
    public int getExpectedType() {
        return this.expectedType;
    }
}
