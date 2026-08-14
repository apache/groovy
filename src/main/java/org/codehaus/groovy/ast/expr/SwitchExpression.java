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

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
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

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        List<CaseStatement> transformedCases = new ArrayList<>(caseStatements.size());
        for (CaseStatement caseStatement : caseStatements) {
            transformedCases.add(copyCase(caseStatement, transformer));
        }
        SwitchExpression ret = new SwitchExpression(
                transformer.transform(expression),
                transformedCases,
                copyAndTransform(defaultStatement, transformer));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        ret.setType(getType());
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitSwitchExpression(this);
    }

    private static CaseStatement copyCase(final CaseStatement caseStatement, final ExpressionTransformer transformer) {
        CaseStatement copy = new CaseStatement(
                transformer.transform(caseStatement.getExpression()),
                copyAndTransform(caseStatement.getCode(), transformer));
        copy.setArrow(caseStatement.isArrow());
        copy.setSourcePosition(caseStatement);
        copy.copyNodeMetaData(caseStatement);
        copy.copyStatementLabels(caseStatement);
        return copy;
    }

    /**
     * Returns a structural copy of {@code statement} whose nested expressions
     * have been passed through {@code transformer}. The original tree is not
     * mutated. Unknown statement types are returned as-is.
     */
    private static Statement copyAndTransform(final Statement statement, final ExpressionTransformer transformer) {
        if (statement == null || statement.isEmpty()) {
            return statement;
        }
        Statement copy;
        if (statement instanceof YieldStatement yieldStatement) {
            copy = new YieldStatement(transformer.transform(yieldStatement.getExpression()));
        } else if (statement instanceof ThrowStatement throwStatement) {
            copy = new ThrowStatement(transformer.transform(throwStatement.getExpression()));
        } else if (statement instanceof ExpressionStatement expressionStatement) {
            copy = new ExpressionStatement(transformer.transform(expressionStatement.getExpression()));
        } else if (statement instanceof ReturnStatement returnStatement) {
            copy = new ReturnStatement(transformer.transform(returnStatement.getExpression()));
        } else if (statement instanceof AssertStatement assertStatement) {
            copy = new AssertStatement(
                    transformBoolean(assertStatement.getBooleanExpression(), transformer),
                    transformer.transform(assertStatement.getMessageExpression()));
        } else if (statement instanceof BlockStatement blockStatement) {
            List<Statement> copied = new ArrayList<>(blockStatement.getStatements().size());
            for (Statement child : blockStatement.getStatements()) {
                copied.add(copyAndTransform(child, transformer));
            }
            copy = new BlockStatement(copied, blockStatement.getVariableScope());
        } else if (statement instanceof IfStatement ifStatement) {
            copy = new IfStatement(
                    transformBoolean(ifStatement.getBooleanExpression(), transformer),
                    copyAndTransform(ifStatement.getIfBlock(), transformer),
                    copyAndTransform(ifStatement.getElseBlock(), transformer));
        } else if (statement instanceof WhileStatement whileStatement) {
            copy = new WhileStatement(
                    transformBoolean(whileStatement.getBooleanExpression(), transformer),
                    copyAndTransform(whileStatement.getLoopBlock(), transformer));
        } else if (statement instanceof DoWhileStatement doWhileStatement) {
            copy = new DoWhileStatement(
                    transformBoolean(doWhileStatement.getBooleanExpression(), transformer),
                    copyAndTransform(doWhileStatement.getLoopBlock(), transformer));
        } else if (statement instanceof ForStatement forStatement) {
            Expression collection = transformer.transform(forStatement.getCollectionExpression());
            ForStatement copiedFor;
            if (collection instanceof ClosureListExpression classic) {
                // C-style `for (;;)` stores a dummy value parameter that
                // getValueVariable() hides; use the dedicated constructor.
                copiedFor = new ForStatement(classic, copyAndTransform(forStatement.getLoopBlock(), transformer));
            } else {
                copiedFor = new ForStatement(
                        forStatement.getIndexVariable(),
                        forStatement.getValueVariable(),
                        collection,
                        copyAndTransform(forStatement.getLoopBlock(), transformer));
            }
            copiedFor.setVariableScope(forStatement.getVariableScope());
            copy = copiedFor;
        } else if (statement instanceof SynchronizedStatement synchronizedStatement) {
            copy = new SynchronizedStatement(
                    transformer.transform(synchronizedStatement.getExpression()),
                    copyAndTransform(synchronizedStatement.getCode(), transformer));
        } else if (statement instanceof TryCatchStatement tryCatchStatement) {
            TryCatchStatement copied = new TryCatchStatement(
                    copyAndTransform(tryCatchStatement.getTryStatement(), transformer),
                    copyAndTransform(tryCatchStatement.getFinallyStatement(), transformer));
            for (CatchStatement catchStatement : tryCatchStatement.getCatchStatements()) {
                copied.addCatch(new CatchStatement(
                        catchStatement.getVariable(),
                        copyAndTransform(catchStatement.getCode(), transformer)));
            }
            for (ExpressionStatement resource : tryCatchStatement.getResourceStatements()) {
                copied.addResource((ExpressionStatement) copyAndTransform(resource, transformer));
            }
            copy = copied;
        } else if (statement instanceof SwitchStatement switchStatement) {
            List<CaseStatement> cases = new ArrayList<>(switchStatement.getCaseStatements().size());
            for (CaseStatement caseStatement : switchStatement.getCaseStatements()) {
                cases.add(copyCase(caseStatement, transformer));
            }
            copy = new SwitchStatement(
                    transformer.transform(switchStatement.getExpression()),
                    cases,
                    copyAndTransform(switchStatement.getDefaultStatement(), transformer));
        } else if (statement instanceof BreakStatement breakStatement) {
            copy = new BreakStatement(breakStatement.getLabel());
        } else if (statement instanceof ContinueStatement continueStatement) {
            copy = new ContinueStatement(continueStatement.getLabel());
        } else {
            return statement;
        }
        copy.setSourcePosition(statement);
        copy.copyNodeMetaData(statement);
        copy.copyStatementLabels(statement);
        return copy;
    }

    private static BooleanExpression transformBoolean(final BooleanExpression expression, final ExpressionTransformer transformer) {
        Expression transformed = transformer.transform(expression);
        return transformed instanceof BooleanExpression bool ? bool : new BooleanExpression(transformed);
    }
}
