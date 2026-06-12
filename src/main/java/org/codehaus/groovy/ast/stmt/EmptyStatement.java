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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

import java.util.Map;

/**
 * Represents an empty statement that performs no operation.
 * An empty statement is a valid statement that can appear wherever a statement is expected
 * but has no executable code. Empty statements are often used as default cases or as placeholders
 * in control flow structures.
 *
 * <p>An immutable singleton {@link #INSTANCE} is provided for use when source position or
 * other occurrence-specific metadata is not needed. Using the singleton reduces memory overhead.</p>
 *
 * @see {@link Statement}
 * @see {@link SwitchStatement}
 */
public class EmptyStatement extends Statement {

    /**
     * Constructs a new empty statement instance.
     * For most use cases, consider using {@link #INSTANCE} instead to avoid unnecessary object allocation.
     *
     * @see #INSTANCE
     */
    public EmptyStatement() {
        super();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitEmptyStatement(this);
    }

    //--------------------------------------------------------------------------

    /**
     * Immutable singleton that is recommended for use when source range or any
     * other occurrence-specific metadata is not needed. Using this singleton
     * conserves memory by avoiding unnecessary object allocations.
     *
     * @see EmptyStatement#EmptyStatement()
     */
    public static final EmptyStatement INSTANCE = new EmptyStatement() {

        private void throwUnsupportedOperationException() {
            throw new UnsupportedOperationException("EmptyStatement.INSTANCE is immutable");
        }

        // ASTNode overrides:

        @Override
        public void setColumnNumber(int n) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setLastColumnNumber(int n) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setLastLineNumber(int n) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setLineNumber(int n) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setMetaDataMap(Map<?, ?> meta) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setSourcePosition(ASTNode node) {
            throwUnsupportedOperationException();
        }

        // Statement overrides:

        @Override
        public void addStatementLabel(String label) {
            throwUnsupportedOperationException();
        }

        @Override
        public void setStatementLabel(String label) {
            throwUnsupportedOperationException();
        }
    };
}
