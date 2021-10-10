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
package org.codehaus.groovy.transform.tailrec;

import groovy.lang.Closure;
import org.apache.groovy.internal.util.UncheckedThrow;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.lang.reflect.Method;

/**
 * Tool for replacing VariableExpression instances in an AST by other VariableExpression instances.
 * Regardless of a real change taking place in nested expressions, all considered expression (trees) will be replaced.
 * This could be optimized to accelerate compilation.
 * <p>
 * Within @TailRecursive it is used
 * - to swap the access of method args with the access to iteration variables
 * - to swap the access of iteration variables with the access of temp vars
 */
class VariableExpressionReplacer extends CodeVisitorSupport {
    VariableExpressionReplacer(Closure<Boolean> when, Closure<VariableExpression> replaceWith) {
        this.when = when;
        this.replaceWith = replaceWith;
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        replaceExpressionPropertyWhenNecessary(ifElse, "booleanExpression", BooleanExpression.class);
        super.visitIfElse(ifElse);
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        replaceExpressionPropertyWhenNecessary(forLoop, "collectionExpression");
        super.visitForLoop(forLoop);
    }

    /**
     * It's the only Expression type in which replacing is considered.
     * That's an abuse of the class, but I couldn't think of a better way.
     */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        //A hack: Only replace right expression b/c ReturnStatementToIterationConverter needs it that way :-/
        replaceExpressionPropertyWhenNecessary(expression, "rightExpression");
        expression.getRightExpression().visit(this);
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        replaceExpressionPropertyWhenNecessary(loop, "booleanExpression", BooleanExpression.class);
        super.visitWhileLoop(loop);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        replaceExpressionPropertyWhenNecessary(loop, "booleanExpression", BooleanExpression.class);
        super.visitDoWhileLoop(loop);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitSwitch(statement);
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitCaseStatement(statement);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitExpressionStatement(statement);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitThrowStatement(statement);
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement, "booleanExpression", BooleanExpression.class);
        replaceExpressionPropertyWhenNecessary(statement, "messageExpression");
        super.visitAssertStatement(statement);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement);
        super.visitSynchronizedStatement(statement);
    }

    public synchronized void replaceIn(final ASTNode root) {
        transformer = new VariableExpressionTransformer(when, replaceWith);
        root.visit(this);
    }

    private void replaceExpressionPropertyWhenNecessary(final ASTNode node, final String propName, final Class propClass) {
        Expression expr = getExpression(node, propName);

        if (expr instanceof VariableExpression) {
            if (when.call(expr)) {
                VariableExpression newExpr = replaceWith.call(expr);
                replaceExpression(node, propName, propClass, expr, newExpr);
            }
        } else {
            Expression newExpr = transformer.transform(expr);
            replaceExpression(node, propName, propClass, expr, newExpr);
        }
    }

    private void replaceExpressionPropertyWhenNecessary(final ASTNode node, final String propName) {
        replaceExpressionPropertyWhenNecessary(node, propName, Expression.class);
    }

    private void replaceExpressionPropertyWhenNecessary(final ASTNode node) {
        replaceExpressionPropertyWhenNecessary(node, "expression", Expression.class);
    }

    private void replaceExpression(final ASTNode node, final String propName, final Class propClass, final Expression oldExpr, final Expression newExpr) {
        try {
            //Use reflection to enable CompileStatic
            String setterName = GeneralUtils.getSetterName(propName);
            Method setExpressionMethod = node.getClass().getMethod(setterName, propClass);
            newExpr.copyNodeMetaData(oldExpr);
            newExpr.setSourcePosition(oldExpr);
            setExpressionMethod.invoke(node, newExpr);
        } catch (Throwable t) {
            UncheckedThrow.rethrow(t);
        }
    }

    private Expression getExpression(final ASTNode node, final String propName) {
        try {
            //Use reflection to enable CompileStatic
            String getterName = GeneralUtils.getGetterName(propName);
            Method getExpressionMethod = node.getClass().getMethod(getterName);
            return DefaultGroovyMethods.asType(getExpressionMethod.invoke(node), Expression.class);
        } catch (Throwable t) {
            UncheckedThrow.rethrow(t);
            return null;
        }
    }

    public Closure<Boolean> getWhen() {
        return when;
    }

    public void setWhen(Closure<Boolean> when) {
        this.when = when;
    }

    public Closure<VariableExpression> getReplaceWith() {
        return replaceWith;
    }

    public void setReplaceWith(Closure<VariableExpression> replaceWith) {
        this.replaceWith = replaceWith;
    }

    private Closure<Boolean> when;
    private Closure<VariableExpression> replaceWith;
    private ExpressionTransformer transformer;
}
