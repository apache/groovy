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

public class EmptyStatement extends Statement {

    /**
     * @see EmptyStatement#INSTANCE
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
    }

    //--------------------------------------------------------------------------

    /**
     * Immutable singleton that is recommended for use when source range or any
     * other occurrence-specific metadata is not needed.
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
