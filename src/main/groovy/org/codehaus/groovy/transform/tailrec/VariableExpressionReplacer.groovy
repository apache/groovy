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
package org.codehaus.groovy.transform.tailrec

import groovy.transform.AutoFinal
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.AssertStatement
import org.codehaus.groovy.ast.stmt.CaseStatement
import org.codehaus.groovy.ast.stmt.DoWhileStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.SwitchStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codehaus.groovy.ast.stmt.ThrowStatement
import org.codehaus.groovy.ast.stmt.WhileStatement
import org.codehaus.groovy.ast.tools.GeneralUtils

import java.lang.reflect.Method

/**
 * Tool for replacing VariableExpression instances in an AST by other VariableExpression instances.
 * Regardless of a real change taking place in nested expressions, all considered expression (trees) will be replaced.
 * This could be optimized to accelerate compilation.
 *
 * Within @TailRecursive it is used
 * - to swap the access of method args with the access to iteration variables
 * - to swap the access of iteration variables with the access of temp vars
 */
@AutoFinal @CompileStatic
class VariableExpressionReplacer extends CodeVisitorSupport {

    Closure<Boolean> when = { VariableExpression node -> false }
    Closure<VariableExpression> replaceWith = { VariableExpression variableExpression -> variableExpression }

    private ExpressionTransformer transformer

    synchronized void replaceIn(ASTNode root) {
        transformer = new VariableExpressionTransformer(when: when, replaceWith: replaceWith)
        root.visit(this)
    }

    void visitReturnStatement(ReturnStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitReturnStatement(statement)
    }

    void visitIfElse(IfStatement ifElse) {
        replaceExpressionPropertyWhenNecessary(ifElse, 'booleanExpression', BooleanExpression)
        super.visitIfElse(ifElse)
    }

    void visitForLoop(ForStatement forLoop) {
        replaceExpressionPropertyWhenNecessary(forLoop, 'collectionExpression')
        super.visitForLoop(forLoop)
    }

    /**
     * It's the only Expression type in which replacing is considered.
     * That's an abuse of the class, but I couldn't think of a better way.
     */
    void visitBinaryExpression(BinaryExpression expression) {
        //A hack: Only replace right expression b/c ReturnStatementToIterationConverter needs it that way :-/
        replaceExpressionPropertyWhenNecessary(expression, 'rightExpression')
        expression.rightExpression.visit(this)
        super.visitBinaryExpression(expression)
    }

    void visitWhileLoop(WhileStatement loop) {
        replaceExpressionPropertyWhenNecessary(loop, 'booleanExpression', BooleanExpression)
        super.visitWhileLoop(loop)
    }

    void visitDoWhileLoop(DoWhileStatement loop) {
        replaceExpressionPropertyWhenNecessary(loop, 'booleanExpression', BooleanExpression)
        super.visitDoWhileLoop(loop)
    }

    void visitSwitch(SwitchStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitSwitch(statement)
    }

    void visitCaseStatement(CaseStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitCaseStatement(statement)
    }

    void visitExpressionStatement(ExpressionStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitExpressionStatement(statement)
    }

    void visitThrowStatement(ThrowStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitThrowStatement(statement)
    }

    void visitAssertStatement(AssertStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement, 'booleanExpression', BooleanExpression)
        replaceExpressionPropertyWhenNecessary(statement, 'messageExpression')
        super.visitAssertStatement(statement)
    }

    void visitSynchronizedStatement(SynchronizedStatement statement) {
        replaceExpressionPropertyWhenNecessary(statement)
        super.visitSynchronizedStatement(statement)
    }

    private void replaceExpressionPropertyWhenNecessary(ASTNode node, String propName = 'expression', Class propClass = Expression) {
        Expression expr = getExpression(node, propName)

        if (expr instanceof VariableExpression) {
            if (when(expr)) {
                VariableExpression newExpr = replaceWith(expr)
                replaceExpression(node, propName, propClass, expr, newExpr)
            }
        } else {
            Expression newExpr = transformer.transform(expr)
            replaceExpression(node, propName, propClass, expr, newExpr)
        }
    }

    private void replaceExpression(ASTNode node, String propName, Class propClass, Expression oldExpr, Expression newExpr) {
        //Use reflection to enable CompileStatic
        String setterName = GeneralUtils.getSetterName(propName)
        Method setExpressionMethod = node.class.getMethod(setterName, propClass)
        newExpr.copyNodeMetaData(oldExpr)
        newExpr.setSourcePosition(oldExpr)
        setExpressionMethod.invoke(node, newExpr)
    }

    private Expression getExpression(ASTNode node, String propName) {
        //Use reflection to enable CompileStatic
        String getterName = GeneralUtils.getGetterName(propName)
        Method getExpressionMethod = node.class.getMethod(getterName)
        getExpressionMethod.invoke(node) as Expression
    }
}
