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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.Expression;

import static java.util.Objects.requireNonNull;

/**
 * Represents a for loop in Groovy, supporting both for-in loops (with values and optional indices)
 * and classic (C-style) for loops with initialization, condition, and update expressions.
 * For-in loops iterate over a collection or iterable expression, optionally capturing the index.
 * Classic for loops use a {@link ClosureListExpression} to represent initialization, condition,
 * and update expressions and may include multi-assignment declarations.
 *
 * @see LoopingStatement
 * @see Statement
 * @see Parameter
 * @see Expression
 * @see ClosureListExpression
 */
public class ForStatement extends Statement implements LoopingStatement {

    private static final Parameter DUMMY_VALUE_VARIABLE = new Parameter(ClassHelper.OBJECT_TYPE, "forLoopDummyParameter");
    @Deprecated(since = "6.0.0")
    public static final Parameter FOR_LOOP_DUMMY = DUMMY_VALUE_VARIABLE;

    private final Parameter indexVariable, valueVariable;
    private Expression collectionExpression;
    private Statement loopBlock;

    /**
     * Constructs a for-in loop with an optional index variable and a value variable.
     * This constructor supports both indexed and non-indexed iteration patterns:
     * <pre>
     * for (int i in 10..12) { ... }                    // no index
     * for (int idx, int i in 10..12) { ... }           // with index
     * </pre>
     *
     * @param indexVariable
     *      the optional loop index {@link Parameter}, or null if no index is needed
     * @param valueVariable
     *      the {@link Parameter} representing the loop value; must not be null
     * @param collectionExpression
     *      the {@link Expression} that produces the iterable or collection to loop over; must not be null
     * @param loopBlock
     *      the {@link Statement} to execute for each iteration; must not be null
     * @throws NullPointerException if valueVariable, collectionExpression, or loopBlock is null
     * @since 5.0.0
     */
    public ForStatement(final Parameter indexVariable, final Parameter valueVariable, final Expression collectionExpression, final Statement loopBlock) {
        this.indexVariable = indexVariable; // null implies no index variable
        this.valueVariable = requireNonNull(valueVariable);
        setCollectionExpression(collectionExpression);
        setLoopBlock(loopBlock);
    }

    /**
     * Constructs a for-in loop with a value variable but no index.
     * This is the most common for-in loop pattern, equivalent to Java's enhanced for loop:
     * <pre>
     * for (int i : 0..2) { ... }     // Java-style colon syntax
     * for (int j in 5..7) { ... }    // Groovy-style 'in' keyword
     * </pre>
     *
     * @param valueVariable
     *      the {@link Parameter} representing the loop value; must not be null
     * @param collectionExpression
     *      the {@link Expression} that produces the iterable or collection to loop over; must not be null
     * @param loopBlock
     *      the {@link Statement} to execute for each iteration; must not be null
     * @throws NullPointerException if valueVariable, collectionExpression, or loopBlock is null
     * @since 5.0.0
     */
    public ForStatement(final Parameter valueVariable, final Expression collectionExpression, final Statement loopBlock) {
        this(null, valueVariable, collectionExpression, loopBlock);
    }

    /**
     * Constructs a classic (C-style) for loop with optional multi-assignment support.
     * The {@link ClosureListExpression} encapsulates initialization, condition, and update expressions:
     * <pre>
     * for (int i = 20; i &lt; 23; i++) { ... }
     * for (def (String i, int j) = ['a', 30]; j &lt; 33; i++, j++) { ... }
     * </pre>
     *
     * @param closureListExpression
     *      the {@link ClosureListExpression} containing loop initialization, condition, and update; must not be null
     * @param loopBlock
     *      the {@link Statement} to execute for each iteration; must not be null
     * @throws NullPointerException if closureListExpression or loopBlock is null
     * @since 6.0.0
     */
    public ForStatement(final ClosureListExpression closureListExpression, final Statement loopBlock) {
        this(DUMMY_VALUE_VARIABLE, closureListExpression, loopBlock);
    }

    /**
     * Sets the {@link Expression} that produces the collection or iterable to loop over.
     *
     * @param collectionExpression
     *      the {@link Expression} that evaluates to an iterable; must not be null
     * @throws NullPointerException if collectionExpression is null
     */
    public void setCollectionExpression(final Expression collectionExpression) {
        this.collectionExpression = requireNonNull(collectionExpression);
    }

    /**
     * Sets the {@link Statement} to execute for each loop iteration.
     *
     * @param loopBlock
     *      the loop body {@link Statement}; must not be null
     * @throws NullPointerException if loopBlock is null
     */
    @Override
    public void setLoopBlock(final Statement loopBlock) {
        this.loopBlock = requireNonNull(loopBlock);
    }

    //--------------------------------------------------------------------------

    /**
     * Returns the optional index {@link Parameter} for for-in loops with an index variable,
     * or null if this is a for-in loop without an index or a classic for loop.
     *
     * @return the index {@link Parameter}, or null
     * @since 5.0.0
     */
    public Parameter getIndexVariable() {
        return indexVariable;
    }

    /**
     * Returns the value {@link Parameter} for for-in loops,
     * or null if this is a classic for loop (represented internally with a dummy parameter).
     *
     * @return the value {@link Parameter}, or null for classic for loops
     * @since 5.0.0
     */
    public Parameter getValueVariable() {
        return valueVariable != DUMMY_VALUE_VARIABLE ? valueVariable : null;
    }

    @Deprecated(since = "5.0.0")
    public Parameter getVariable() {
        return valueVariable;
    }

    @Deprecated(since = "5.0.0")
    public ClassNode getVariableType() {
        return valueVariable.getType();
    }

    /**
     * Returns the {@link Expression} that produces the iterable to loop over.
     * For classic for loops, this is a {@link ClosureListExpression} containing
     * initialization, condition, and update expressions.
     *
     * @return the collection or control {@link Expression}
     */
    public Expression getCollectionExpression() {
        return collectionExpression;
    }

    /**
     * Returns the loop body {@link Statement}.
     *
     * @return the loop block {@link Statement}
     */
    @Override
    public Statement getLoopBlock() {
        return loopBlock;
    }

    //--------------------------------------------------------------------------

    private VariableScope scope;

    /**
     * Returns the {@link VariableScope} associated with this for loop,
     * containing loop variable bindings and type information.
     *
     * @return the {@link VariableScope}}
     */
    public VariableScope getVariableScope() {
        return this.scope;
    }

    /**
     * Sets the {@link VariableScope} for this loop.
     *
     * @param scope
     *      the {@link VariableScope} to associate with this loop
     */
    public void setVariableScope(final VariableScope scope) {
        this.scope = scope;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitForLoop(this);
    }
}
