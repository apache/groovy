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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a try { ... } catch { ... } finally { ... } statement in Groovy.
 * A try-catch statement combines exception handling with optional resource management (try-with-resources).
 * It contains a main try block, zero or more {@link CatchStatement}s for exception handlers,
 * an optional finally block, and optional resource declarations that implement AutoCloseable.
 *
 * @see Statement
 * @see CatchStatement
 * @see ExpressionStatement
 */
public class TryCatchStatement extends Statement {

    private Statement tryStatement;
    private Statement finallyStatement;
    private final List<CatchStatement> catchStatements = new ArrayList<>();
    private final List<ExpressionStatement> resourceStatements = new ArrayList<>();

    /**
     * Constructs a TryCatchStatement with a try block and optional finally block.
     * Catch statements and resource statements should be added separately via
     * {@link #addCatch(CatchStatement)} and {@link #addResource(ExpressionStatement)}.
     *
     * @param tryStatement
     *      the {@link Statement} to execute as the try block
     * @param finallyStatement
     *      the {@link Statement} to execute in the finally block, or null if no finally clause
     */
    public TryCatchStatement(Statement tryStatement, Statement finallyStatement) {
        this.tryStatement = tryStatement;
        this.finallyStatement = finallyStatement;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitTryCatchFinally(this);
    }

    //

    /**
     * Returns the try block {@link Statement}, which is executed when the try-catch is entered.
     *
     * @return the try block {@link Statement}}
     */
    public Statement getTryStatement() {
        return tryStatement;
    }

    /**
     * Returns the finally block {@link Statement}, executed after the try and catch blocks complete.
     * May be null if no finally clause is present.
     *
     * @return the finally block {@link Statement}}, or null
     */
    public Statement getFinallyStatement() {
        return finallyStatement;
    }

    /**
     * Returns the {@link CatchStatement} at the specified index.
     *
     * @param idx
     *      the index of the catch statement
     * @return the {@link CatchStatement} at the index, or null if the index is out of bounds
     */
    public CatchStatement getCatchStatement(int idx) {
        if (idx >= 0 && idx < catchStatements.size()) {
            return catchStatements.get(idx);
        }
        return null;
    }

    /**
     * Returns the list of all {@link CatchStatement}s in order of declaration.
     *
     * @return a {@link List} of {@link CatchStatement}s
     */
    public List<CatchStatement> getCatchStatements() {
        return catchStatements;
    }

    /**
     * Returns the resource {@link ExpressionStatement} at the specified index,
     * representing a try-with-resources variable declaration.
     *
     * @param idx
     *      the index of the resource statement
     * @return the resource {@link ExpressionStatement}} at the index, or null if the index is out of bounds
     */
    public ExpressionStatement getResourceStatement(int idx) {
        if (idx >= 0 && idx < resourceStatements.size()) {
            return resourceStatements.get(idx);
        }
        return null;
    }

    /**
     * Returns the list of all resource {@link ExpressionStatement}s declared in try-with-resources.
     *
     * @return a {@link List} of resource {@link ExpressionStatement}s
     */
    public List<ExpressionStatement> getResourceStatements() {
        return resourceStatements;
    }

    /**
     * Checks if an {@link Expression} is marked as a resource in a try-with-resources block.
     *
     * @param expression
     *      the {@link Expression} to check
     * @return true if the expression is a resource declaration, false otherwise
     */
    public static boolean isResource(final Expression expression) {
        return Boolean.TRUE.equals(expression.getNodeMetaData("_IS_RESOURCE"));
    }

    //--------------------------------------------------------------------------

    /**
     * Sets the try block {@link Statement}}.
     *
     * @param tryStatement
     *      the try block {@link Statement}}
     */
    public void setTryStatement(Statement tryStatement) {
        this.tryStatement = tryStatement;
    }

    /**
     * Sets the finally block {@link Statement}}.
     *
     * @param finallyStatement
     *      the finally block {@link Statement}}, or null for no finally clause
     */
    public void setFinallyStatement(Statement finallyStatement) {
        this.finallyStatement = finallyStatement;
    }

    /**
     * Replaces the {@link CatchStatement}} at the specified index.
     *
     * @param idx
     *      the index of the catch statement to replace
     * @param catchStatement
     *      the new {@link CatchStatement}}
     */
    public void setCatchStatement(int idx, CatchStatement catchStatement) {
        catchStatements.set(idx, catchStatement);
    }

    //

    /**
     * Adds a {@link CatchStatement}} to this try-catch block and returns this for method chaining.
     *
     * @param catchStatement
     *      the {@link CatchStatement}} to add
     * @return this {@link TryCatchStatement}} for method chaining
     */
    public TryCatchStatement addCatch(CatchStatement catchStatement) {
        catchStatements.add(catchStatement);
        return this;
    }

    /**
     * Adds a resource {@link ExpressionStatement}} representing a try-with-resources declaration.
     * The expression must be a {@link DeclarationExpression} or {@link VariableExpression}.
     *
     * @param resourceStatement
     *      the resource {@link ExpressionStatement}} to add
     * @return this {@link TryCatchStatement}} for method chaining
     * @throws GroovyBugError if the resource expression is not a DeclarationExpression or VariableExpression
     */
    public TryCatchStatement addResource(ExpressionStatement resourceStatement) {
        Expression resourceExpression = resourceStatement.getExpression();
        if (!(resourceExpression instanceof DeclarationExpression || resourceExpression instanceof VariableExpression)) {
            throw new GroovyBugError("resourceStatement should be a variable declaration statement or a variable");
        }
        resourceExpression.putNodeMetaData("_IS_RESOURCE", Boolean.TRUE);
        resourceStatements.add(resourceStatement);
        return this;
    }
}
