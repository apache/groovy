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
import org.codehaus.groovy.ast.VariableScope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a compound statement consisting of an ordered sequence of {@link Statement}s
 * within a specific {@link VariableScope}. A block statement is the fundamental grouping
 * mechanism in the Groovy AST and is used to represent the body of methods, classes, loops, conditionals,
 * try-catch blocks, and any other scoped code region.
 *
 * @see Statement
 * @see VariableScope
 */
public class BlockStatement extends Statement {

    private List<Statement> statements;
    private VariableScope scope;

    /**
     * Constructs an empty BlockStatement with a new default scope.
     */
    public BlockStatement() {
        this(new ArrayList<>(), new VariableScope());
    }

    /**
     * Constructs a BlockStatement with an array of statements and a variable scope.
     * The provided array is copied internally; subsequent modifications to the array
     * will not affect this BlockStatement.
     *
     * @param statements
     *      an array of {@link Statement}s to include in this block; must not be null
     *      or a NullPointerException will be raised
     * @param scope
     *      the {@link VariableScope} for this block, typically containing local variable
     *      declarations and type information
     */
    public BlockStatement(final Statement[] statements, final VariableScope scope) {
        this(new ArrayList<>(Arrays.asList(statements)), scope);
    }

    /**
     * Constructs a BlockStatement with a list of statements and a variable scope.
     * A reference to the provided list is maintained; modifications to the list after
     * construction will be reflected in this BlockStatement.
     *
     * @param statements
     *      a {@link List} of {@link Statement}s to include in this block; pass an empty list,
     *      not null, to avoid NullPointerException later
     * @param scope
     *      the {@link VariableScope} for this block, containing variable binding and type metadata
     */
    public BlockStatement(final List<Statement> statements, final VariableScope scope) {
        this.statements = statements;
        this.scope = scope;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitBlockStatement(this);
    }

    /**
     * Returns the list of {@link Statement}s in this block.
     *
     * @return an unmodifiable view or direct reference to the statements list
     */
    public List<Statement> getStatements() {
        return statements;
    }

    /**
     * Appends a {@link Statement} to this block.
     *
     * @param statement
     *      the {@link Statement} to add; must not be null
     */
    public void addStatement(final Statement statement) {
        statements.add(statement);
    }

    /**
     * Appends all {@link Statement}s from the provided list to this block.
     *
     * @param listOfStatements
     *      a {@link List} of {@link Statement}s to append; must not be null
     */
    public void addStatements(final List<Statement> listOfStatements) {
        statements.addAll(listOfStatements);
    }

    @Override
    public String getText() {
        StringJoiner text = new StringJoiner("; ", "{ ", " }");
        for (Statement statement : statements) {
            text.add(statement.getText());
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return super.toString() + statements;
    }

    /**
     * Checks whether this block contains no statements.
     *
     * @return true if the statement list is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return statements.isEmpty();
    }

    /**
     * Returns the {@link VariableScope} associated with this block, which contains
     * variable declarations, type information, and scope metadata.
     *
     * @return the {@link VariableScope} for this block
     */
    public VariableScope getVariableScope() {
        return scope;
    }

    /**
     * Sets the {@link VariableScope} for this block.
     *
     * @param scope
     *      the {@link VariableScope} to associate with this block
     */
    public void setVariableScope(final VariableScope scope) {
        this.scope = scope;
    }
}
