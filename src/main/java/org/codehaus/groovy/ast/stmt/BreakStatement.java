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
 * Represents a break statement that terminates execution of a loop or switch statement.
 * When a break statement is encountered, control flow exits the current loop or switch,
 * continuing at the statement following the loop or switch. Labeled break statements
 * can exit outer loops or switch statements.
 *
 * @see {@link SwitchStatement}
 * @see {@link LoopingStatement}
 * @see {@link ContinueStatement}
 * @see {@link Statement}
 */
public class BreakStatement extends Statement {

    private String label;

    /**
     * Constructs an unlabeled break statement that exits the innermost enclosing loop or switch.
     */
    public BreakStatement() {
        this(null);
    }

    /**
     * Constructs a labeled break statement that exits the enclosing loop or switch with the given label.
     *
     * @param label the name of the label to break to, or null for an unlabeled break
     */
    public BreakStatement(String label) {
        this.label = label;
    }

    /**
     * Returns the label associated with this break statement.
     *
     * @return the label name, or null if this is an unlabeled break statement
     */
    public String getLabel() {
        return label;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitBreakStatement(this);
    }
}
