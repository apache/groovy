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
package groovy.text;

import java.io.Serial;

/**
 * A custom exception class to flag template parsing errors
 */
public class TemplateParseException extends RuntimeException {
    @Serial private static final long serialVersionUID = 1607958968337123274L;
    private final int lineNumber;
    private final int column;

    /**
     * Creates an exception for a parse failure at the supplied template position.
     *
     * @param lineNumber one-based line number in the template source
     * @param column one-based column number in the template source
     */
    public TemplateParseException(int lineNumber, int column) {
        super();
        this.lineNumber = lineNumber;
        this.column = column;
    }

    /**
     * Creates an exception for a parse failure at the supplied template position.
     *
     * @param message a description of the parse failure
     * @param lineNumber one-based line number in the template source
     * @param column one-based column number in the template source
     */
    public TemplateParseException(String message, int lineNumber, int column) {
        super(message);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    /**
     * Creates an exception for a parse failure at the supplied template position.
     *
     * @param message a description of the parse failure
     * @param cause the underlying parse failure
     * @param lineNumber one-based line number in the template source
     * @param column one-based column number in the template source
     */
    public TemplateParseException(String message, Throwable cause, int lineNumber, int column) {
        super(message, cause);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    /**
     * Creates an exception for a parse failure at the supplied template position.
     *
     * @param t the underlying parse failure
     * @param lineNumber one-based line number in the template source
     * @param column one-based column number in the template source
     */
    public TemplateParseException(Throwable t, int lineNumber, int column) {
        super(t);
        this.lineNumber = lineNumber;
        this.column = column;
    }

    /**
     * Returns the line where parsing failed.
     *
     * @return the one-based line number of the parse failure
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the column where parsing failed.
     *
     * @return the one-based column number of the parse failure
     */
    public int getColumn() {
        return column;
    }
}
