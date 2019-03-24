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
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
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

    public void visitBlockStatement(BlockStatement block) {
        for (Statement statement : block.getStatements()) {
            statement.accept(this);
        }
    }

    public void visitForLoop(ForStatement forLoop) {
        forLoop.getCollectionExpression().accept(this);
        forLoop.getLoopBlock().accept(this);
    }

    public void visitWhileLoop(WhileStatement loop) {
        loop.getBooleanExpression().accept(this);
        loop.getLoopBlock().accept(this);
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        loop.getLoopBlock().accept(this);
        loop.getBooleanExpression().accept(this);
    }

    public void visitIfElse(IfStatement ifElse) {
        ifElse.getBooleanExpression().accept(this);
        ifElse.getIfBlock().accept(this);

        Statement elseBlock = ifElse.getElseBlock();
        if (elseBlock instanceof EmptyStatement) {
            // dispatching to EmptyStatement will not call back visitor, 
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) elseBlock);
        } else {
            elseBlock.accept(this);
        }
    }

    public void visitExpressionStatement(ExpressionStatement statement) {
        statement.getExpression().accept(this);
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().accept(this);
    }

    public void visitAssertStatement(AssertStatement statement) {
        statement.getBooleanExpression().accept(this);
        statement.getMessageExpression().accept(this);
    }

    public void visitTryCatchFinally(TryCatchStatement statement) {
        statement.getTryStatement().accept(this);
        for (CatchStatement catchStatement : statement.getCatchStatements()) {
            catchStatement.accept(this);
        }
        Statement finallyStatement = statement.getFinallyStatement();
        if (finallyStatement instanceof EmptyStatement) {
            // dispatching to EmptyStatement will not call back visitor, 
            // must call our visitEmptyStatement explicitly
            visitEmptyStatement((EmptyStatement) finallyStatement);
        } else {
            finallyStatement.accept(this);
        }
    }

    protected void visitEmptyStatement(EmptyStatement statement) {
        // noop
    }

    public void visitSwitch(SwitchStatement statement) {
        statement.getExpression().accept(this);
        for (CaseStatement caseStatement : statement.getCaseStatements()) {
            caseStatement.accept(this);
        }
        statement.getDefaultStatement().accept(this);
    }

    public void visitCaseStatement(CaseStatement statement) {
        statement.getExpression().accept(this);
        statement.getCode().accept(this);
    }

    public void visitBreakStatement(BreakStatement statement) {
    }

    public void visitContinueStatement(ContinueStatement statement) {
    }

    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        statement.getExpression().accept(this);
        statement.getCode().accept(this);
    }

    public void visitThrowStatement(ThrowStatement statement) {
        statement.getExpression().accept(this);
    }

    public void visitMethodCallExpression(MethodCallExpression call) {
        call.getObjectExpression().accept(this);
        call.getMethod().accept(this);
        call.getArguments().accept(this);
    }

    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        call.getArguments().accept(this);
    }

    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        call.getArguments().accept(this);
    }

    public void visitBinaryExpression(BinaryExpression expression) {
        expression.getLeftExpression().accept(this);
        expression.getRightExpression().accept(this);
    }

    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().accept(this);
        expression.getTrueExpression().accept(this);
        expression.getFalseExpression().accept(this);
    }

    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    public void visitPostfixExpression(PostfixExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitPrefixExpression(PrefixExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitNotExpression(NotExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitClosureExpression(ClosureExpression expression) {
        expression.getCode().accept(this);
    }

    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }

    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());

    }

    public void visitMapEntryExpression(MapEntryExpression expression) {
        expression.getKeyExpression().accept(this);
        expression.getValueExpression().accept(this);

    }

    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().accept(this);
        expression.getTo().accept(this);
    }

    public void visitSpreadExpression(SpreadExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        expression.getExpression().accept(this);
        expression.getMethodName().accept(this);
    }

    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitCastExpression(CastExpression expression) {
        expression.getExpression().accept(this);
    }

    public void visitConstantExpression(ConstantExpression expression) {
    }

    public void visitClassExpression(ClassExpression expression) {
    }

    public void visitVariableExpression(VariableExpression expression) {
    }

    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().accept(this);
        expression.getProperty().accept(this);
    }

    public void visitAttributeExpression(AttributeExpression expression) {
        expression.getObjectExpression().accept(this);
        expression.getProperty().accept(this);
    }

    public void visitFieldExpression(FieldExpression expression) {
    }

    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    public void visitCatchStatement(CatchStatement statement) {
        statement.getCode().accept(this);
    }

    public void visitArgumentlistExpression(ArgumentListExpression ale) {
        visitTupleExpression(ale);
    }

    public void visitClosureListExpression(ClosureListExpression cle) {
        visitListOfExpressions(cle.getExpressions());
    }

    public void visitBytecodeExpression(BytecodeExpression cle) {
    }
}
