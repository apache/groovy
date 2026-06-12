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
package org.apache.groovy.ginq.dsl;

import org.codehaus.groovy.ast.expr.Expression;

import java.io.Serial;

/**
 * Represents GINQ syntax error
 *
 * @since 4.0.0
 */
public class GinqSyntaxError extends AssertionError {
    @Serial private static final long serialVersionUID = 1106607493949279933L;
    private final int line;
    private final int column;

    /**
     * Creates a syntax error using the position of the given expression.
     *
     * @param message the error message
     * @param expression the expression supplying line and column information
     */
    public GinqSyntaxError(String message, Expression expression) {
        this(message, expression.getLineNumber(), expression.getColumnNumber());
    }

    /**
     * Creates a syntax error at the supplied source position.
     *
     * @param message the error message
     * @param line the source line
     * @param column the source column
     */
    public GinqSyntaxError(String message, int line, int column) {
        super(message, null);
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the source line of the error.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the source column of the error.
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }
}
