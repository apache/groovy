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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.YieldStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a {@code switch} used as an expression, as specified by
 * JEP 361 (Switch Expressions). The selector is evaluated once and matched
 * against the {@link CaseStatement} list using Groovy's {@code isCase}
 * semantics (or a tableswitch / lookupswitch when the compiler can prove
 * that is equivalent). Each completing arm yields a value via
 * {@link YieldStatement}; the expression's result is that value.
 * <p>
 * Arms stay as {@link CaseStatement}s, the same way a
 * {@link ClosureExpression} holds a statement body: the case label is an
 * expression, the arm is a statement. A plain {@link ExpressionTransformer}
 * rewrites only the selector and case labels. A
 * {@link ClassCodeExpressionTransformer} walks the tree in place via
 * {@link ClassCodeExpressionTransformer#visitSwitchExpression}.
 *
 * @see SwitchStatement
 * @see YieldStatement
 * @see CaseStatement
 * @since 6.0.0
 */
public class SwitchExpression extends Expression {

    private Expression expression;
    private List<CaseStatement> caseStatements;
    private Statement defaultStatement;

    /**
     * Constructs a switch expression with the given selector.
     * The default statement is initialized to {@link EmptyStatement#INSTANCE}.
     *
     * @param expression the selector expression
     */
    public SwitchExpression(final Expression expression) {
        this(expression, EmptyStatement.INSTANCE);
    }

    /**
     * Constructs a switch expression with the given selector and default arm.
     *
     * @param expression the selector expression
     * @param defaultStatement the arm executed when no case matches; may be {@link EmptyStatement#INSTANCE}
     */
    public SwitchExpression(final Expression expression, final Statement defaultStatement) {
        this(expression, new ArrayList<>(), defaultStatement);
    }

    /**
     * Constructs a switch expression with the given selector, case arms, and default arm.
     *
     * @param expression the selector expression
     * @param caseStatements the case arms
     * @param defaultStatement the arm executed when no case matches
     */
    public SwitchExpression(final Expression expression, final List<CaseStatement> caseStatements, final Statement defaultStatement) {
        this.expression = expression;
        this.caseStatements = caseStatements;
        this.defaultStatement = defaultStatement;
    }

    /**
     * Returns the selector expression matched against case values.
     *
     * @return the selector {@link Expression}
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * Sets the selector expression matched against case values.
     *
     * @param expression the selector {@link Expression}
     */
    public void setExpression(final Expression expression) {
        this.expression = expression;
    }

    /**
     * Returns the case arms of this switch expression.
     *
     * @return a list of {@link CaseStatement} objects; never null
     */
    public List<CaseStatement> getCaseStatements() {
        return caseStatements;
    }

    /**
     * Returns the arm executed when no case matches.
     *
     * @return the default {@link Statement}, or {@link EmptyStatement#INSTANCE} if not set
     */
    public Statement getDefaultStatement() {
        return defaultStatement;
    }

    /**
     * Sets the arm executed when no case matches.
     *
     * @param defaultStatement the default {@link Statement}
     */
    public void setDefaultStatement(final Statement defaultStatement) {
        this.defaultStatement = defaultStatement;
    }

    /**
     * Adds a case arm to this switch expression.
     *
     * @param caseStatement the {@link CaseStatement} to add
     */
    public void addCase(final CaseStatement caseStatement) {
        caseStatements.add(caseStatement);
    }

    @Override
    public String getText() {
        return "switch (" + expression.getText() + ") { ... }";
    }

    @Override
    public String toString() {
        return super.toString() + "[expression: " + expression + "; cases: " + caseStatements + "; default: " + defaultStatement + "]";
    }

    /**
     * A {@link ClassCodeExpressionTransformer} walks this node in place through
     * {@link ClassCodeExpressionTransformer#visitSwitchExpression}, the same
     * pattern {@link ClosureExpression} uses so resolve, static-import and
     * similar rewrites still see nested arm expressions. Any other transformer
     * gets a copy of the selector and case labels; arm statements are shared.
     */
    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        if (transformer instanceof ClassCodeExpressionTransformer visitor) {
            visitor.visitSwitchExpression(this);
            return this;
        }
        List<CaseStatement> transformedCases = new ArrayList<>(caseStatements.size());
        for (CaseStatement caseStatement : caseStatements) {
            CaseStatement copy = new CaseStatement(
                    transformer.transform(caseStatement.getExpression()),
                    caseStatement.getCode());
            copy.setArrow(caseStatement.isArrow());
            copy.setSourcePosition(caseStatement);
            copy.copyNodeMetaData(caseStatement);
            copy.copyStatementLabels(caseStatement);
            transformedCases.add(copy);
        }
        SwitchExpression ret = new SwitchExpression(
                transformer.transform(expression),
                transformedCases,
                defaultStatement);
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        ret.setType(getType());
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitSwitchExpression(this);
    }
}
