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
 * Represents a for loop in Groovy.
 */
public class ForStatement extends Statement implements LoopingStatement {

    private static final Parameter DUMMY_VALUE_VARIABLE = new Parameter(ClassHelper.OBJECT_TYPE, "forLoopDummyParameter");
    @Deprecated(since = "6.0.0")
    public static final Parameter FOR_LOOP_DUMMY = DUMMY_VALUE_VARIABLE;

    private final Parameter indexVariable, valueVariable;
    private Expression collectionExpression;
    private Statement loopBlock;

    /**
     * A constructor of for-in loops (possibly with an optional index variable).
     * An example with an index variable:
     * <pre>
     * for (int idx, int i in 10..12) {
     *   println "$idx $i"
     * }
     * </pre>
     *
     * @since 5.0.0
     */
    public ForStatement(final Parameter indexVariable, final Parameter valueVariable, final Expression collectionExpression, final Statement loopBlock) {
        this.indexVariable = indexVariable; // null implies no index variable
        this.valueVariable = requireNonNull(valueVariable);
        setCollectionExpression(collectionExpression);
        setLoopBlock(loopBlock);
    }

    /**
     * A constructor of for-in loops without an index variable.
     * Variants using Java-style ":" or the Groovy reserved word "in" are equivalent:
     * <pre>
     * for (int i : 0..2) {
     *   println i
     * }
     * for (int j in 5..7) {
     *   println j
     * }
     * </pre>
     *
     * @since 5.0.0
     */
    public ForStatement(final Parameter valueVariable, final Expression collectionExpression, final Statement loopBlock) {
        this(null, valueVariable, collectionExpression, loopBlock);
    }

    /**
     * A constructor of classic (C-style) for loops.
     * <pre>
     * for (int i = 20; i &lt; 23; i++) {
     *   println i
     * }
     * </pre>
     * Also handles multi-assignment for loops.
     * <pre>
     * for (def (String i, int j) = ['a', 30]; j &lt; 33; i++, j++) {
     *   println "$i $j"
     * }
     * </pre>
     *
     * @since 6.0.0
     */
    public ForStatement(final ClosureListExpression closureListExpression, final Statement loopBlock) {
        this(DUMMY_VALUE_VARIABLE, closureListExpression, loopBlock);
    }

    public void setCollectionExpression(final Expression collectionExpression) {
        this.collectionExpression = requireNonNull(collectionExpression);
    }

    @Override
    public void setLoopBlock(final Statement loopBlock) {
        this.loopBlock = requireNonNull(loopBlock);
    }

    //--------------------------------------------------------------------------

    /**
     * @since 5.0.0
     */
    public Parameter getIndexVariable() {
        return indexVariable;
    }

    /**
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

    public Expression getCollectionExpression() {
        return collectionExpression;
    }

    @Override
    public Statement getLoopBlock() {
        return loopBlock;
    }

    //--------------------------------------------------------------------------

    private VariableScope scope;

    public VariableScope getVariableScope() {
        return this.scope;
    }

    public void setVariableScope(final VariableScope scope) {
        this.scope = scope;
    }

    //--------------------------------------------------------------------------

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitForLoop(this);
    }
}
