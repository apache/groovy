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

import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.ast.ASTNode;

import java.io.Serial;

/**
 * Base exception class indicating a syntax error detected during parsing.
 * Captures source location information (line and column numbers) where the
 * error occurred to facilitate debugging and error reporting.
 */
public class SyntaxException extends GroovyException {

    @Serial private static final long serialVersionUID = 7447641806794047013L;
    /** The starting line number where the error occurred. */
    private final int startLine;
    /** The ending line number of the error range. */
    private final int endLine;

    /** The starting column number where the error occurred. */
    private final int startColumn;
    /** The ending column number of the error range. */
    private final int endColumn;

    /** Optional identifier for the source (file path, URL, etc.). */
    private String sourceLocator;

    /**
     * Constructs a SyntaxException from an AST node's position information.
     *
     * @param message the error message
     * @param node the {@link ASTNode} where the error occurred
     */
    public SyntaxException(String message, ASTNode node) {
        this(message, node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber());
    }

    /**
     * Constructs a SyntaxException with line and column information.
     * The end position defaults to one column after the start position.
     *
     * @param message the error message
     * @param startLine the line number where the error starts (1-based)
     * @param startColumn the column number where the error starts (1-based)
     */
    public SyntaxException(String message, int startLine, int startColumn) {
        this(message, startLine, startColumn, startLine, startColumn+1);
    }

    /**
     * Constructs a SyntaxException with complete position range information.
     *
     * @param message the error message
     * @param startLine the line number where the error starts (1-based)
     * @param startColumn the column number where the error starts (1-based)
     * @param endLine the line number where the error ends (1-based)
     * @param endColumn the column number where the error ends (1-based)
     */
    public SyntaxException(String message, int startLine, int startColumn, int endLine, int endColumn) {
        super(message, false);
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    /**
     * Constructs a SyntaxException with a cause exception.
     * The end position defaults to one column after the start position.
     *
     * @param message the error message
     * @param cause the underlying {@link Throwable} that caused this exception
     * @param startLine the line number where the error starts (1-based)
     * @param startColumn the column number where the error starts (1-based)
     */
    public SyntaxException(String message, Throwable cause, int startLine, int startColumn) {
        this(message, cause, startLine, startColumn, startLine, startColumn+1);
    }

    /**
     * Constructs a SyntaxException with a cause exception and complete position information.
     *
     * @param message the error message
     * @param cause the underlying {@link Throwable} that caused this exception
     * @param startLine the line number where the error starts (1-based)
     * @param startColumn the column number where the error starts (1-based)
     * @param endLine the line number where the error ends (1-based)
     * @param endColumn the column number where the error ends (1-based)
     */
    public SyntaxException(String message, Throwable cause, int startLine, int startColumn, int endLine, int endColumn) {
        super(message, cause);
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    /**
     * Sets an optional source locator string (e.g., file path, URL).
     *
     * @param sourceLocator the source location identifier
     */
    public void setSourceLocator(String sourceLocator) {
        this.sourceLocator = sourceLocator;
    }

    /**
     * Returns the optional source locator string.
     *
     * @return the source location identifier, or {@code null} if not set
     */
    public String getSourceLocator() {
        return this.sourceLocator;
    }

    /**
     * Returns the starting line number of the error.
     * Provided for backward compatibility; equivalent to {@link #getStartLine()}.
     *
     * @return the line number (1-based)
     */
    public int getLine() {
        return getStartLine();
    }

    /**
     * Returns the starting line number of the error.
     *
     * @return the line number (1-based)
     */
    public int getStartLine() {
        return startLine;
    }

    /**
     * Returns the starting column number of the error.
     *
     * @return the column number (1-based)
     */
    public int getStartColumn() {
        return startColumn;
    }

    /**
     * Returns the ending line number of the error.
     *
     * @return the line number (1-based)
     */
    public int getEndLine() {
        return endLine;
    }

    /**
     * Returns the ending column number of the error.
     *
     * @return the column number (1-based)
     */
    public int getEndColumn() {
        return endColumn;
    }

    /**
     * Returns the original error message without location information.
     *
     * @return the base error message
     */
    public String getOriginalMessage() {
        return super.getMessage();
    }

    /**
     * Returns the error message with location information appended.
     *
     * @return the formatted message including line and column information
     */
    @Override
    public String getMessage() {
        return super.getMessage() + " @ line " + startLine + ", column " + startColumn + ".";
    }
}
