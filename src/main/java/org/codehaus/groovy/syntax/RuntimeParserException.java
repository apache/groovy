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

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.ASTNode;

import java.io.Serial;

/**
 * A runtime exception wrapper for parser errors, allowing parser exceptions to be thrown
 * from contexts that cannot throw checked exceptions. Can be converted back to a
 * {@link SyntaxException} when needed for proper error handling.
 */
public class RuntimeParserException extends GroovyRuntimeException {

    @Serial private static final long serialVersionUID = -6612860527133856587L;

    /**
     * Constructs a RuntimeParserException from an error message and AST node.
     * The node's position information is included in the error message.
     *
     * @param message the error message
     * @param node the {@link ASTNode} where the error occurred
     */
    public RuntimeParserException(String message, ASTNode node) {
        super(message + "\n", node);
    }

    /**
     * Converts this runtime exception to a checked {@link SyntaxException}.
     * Extracts position information from the associated AST node.
     *
     * @throws SyntaxException containing the error details from this runtime exception
     */
    public void throwParserException() throws SyntaxException {
        final ASTNode node = getNode();
        throw new SyntaxException(getMessage(), node.getLineNumber(), node.getColumnNumber(), node.getLastLineNumber(), node.getLastColumnNumber());
    }

}
