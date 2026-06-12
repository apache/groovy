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

import org.codehaus.groovy.ast.GroovyCodeVisitor;


/**
 * Represents a continue statement that skips the remainder of the current loop iteration.
 * When a continue statement is encountered, control flow jumps to the next iteration of
 * the innermost enclosing loop. Labeled continue statements can skip to the next iteration
 * of an outer loop.
 *
 * @see {@link LoopingStatement}
 * @see {@link BreakStatement}
 * @see {@link Statement}
 */
public class ContinueStatement extends Statement {

    private String label;

    /**
     * Constructs an unlabeled continue statement that continues the innermost enclosing loop.
     */
    public ContinueStatement() {
        this(null);
    }

    /**
     * Constructs a labeled continue statement that continues the enclosing loop with the given label.
     *
     * @param label the name of the label to continue to, or null for an unlabeled continue
     */
    public ContinueStatement(String label) {
        this.label = label;
    }

    /**
     * Returns the label associated with this continue statement.
     *
     * @return the label name, or null if this is an unlabeled continue statement
     */
    public String getLabel() {
        return label;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitContinueStatement(this);
    }
}
