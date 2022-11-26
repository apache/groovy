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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.SpreadExpression;
import org.codehaus.groovy.ast.expr.SpreadMapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
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
import org.codehaus.groovy.classgen.BytecodeExpression;

/**
 * Abstract base class for any GroovyCodeVisitor which by default
 * just walks the code and expression tree
 */
public abstract class CodeVisitorSupport implements GroovyCodeVisitor {

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        for (Statement statement : block.getStatements()) {
            statement.visit(this);
        }
    }

    @Override
    public void visitForLoop(final ForStatement statement) {
        statement.getCollectionExpression().visit(this);
        statement.getLoopBlock().visit(this);
    }

    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getLoopBlock().visit(this);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement statement) {
        statement.getLoopBlock().visit(this);
        statement.getBooleanExpression().visit(this);
    }

    @Override
    public void visitIfElse(final IfStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getIfBlock().visit(this);
        statement.getElseBlock().visit(this);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getMessageExpression().visit(this);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        for (Statement resource : statement.getResourceStatements()) {
            resource.visit(this);
        }
        statement.getTryStatement().visit(this);
        for (Statement catchStatement : statement.getCatchStatements()) {
            catchStatement.visit(this);
        }
        statement.getFinallyStatement().visit(this);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        statement.getCode().visit(this);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        statement.getExpression().visit(this);
        afterSwitchConditionExpressionVisited(statement);
        for (Statement caseStatement : statement.getCaseStatements()) {
            caseStatement.visit(this);
        }
        afterSwitchCaseStatementsVisited(statement);
        statement.getDefaultStatement().visit(this);
    }

    /**
     * @since 3.0.0
     */
    protected void afterSwitchConditionExpressionVisited(final SwitchStatement statement) {
        // hook for subclass to do something after switch condition, but before case(s)
    }

    /**
     * @since 5.0.0
     */
    protected void afterSwitchCaseStatementsVisited(final SwitchStatement statement) {
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    @Override
    public void visitBreakStatement(final BreakStatement statement) {
    }

    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visitEmptyStatement(final EmptyStatement statement) {
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        call.getObjectExpression().visit(this);
        call.getMethod().visit(this);
        call.getArguments().visit(this);
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        call.getArguments().visit(this);
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        call.getArguments().visit(this);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        expression.getCode().visit(this);
    }

    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        visitClosureExpression(expression);
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        expression.getExpression().visit(this);
        expression.getMethodName().visit(this);
    }

    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        visitMethodPointerExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
    }
}
