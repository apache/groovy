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
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
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
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

public class TransformingCodeVisitor extends CodeVisitorSupport {
    private final ClassCodeExpressionTransformer trn;

    public TransformingCodeVisitor(final ClassCodeExpressionTransformer trn) {
        this.trn = trn;
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        super.visitBlockStatement(block);
        trn.visitBlockStatement(block);
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        trn.visitForLoop(forLoop);
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        super.visitWhileLoop(loop);
        trn.visitWhileLoop(loop);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        super.visitDoWhileLoop(loop);
        trn.visitDoWhileLoop(loop);
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        super.visitIfElse(ifElse);
        trn.visitIfElse(ifElse);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        trn.visitExpressionStatement(statement);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        super.visitReturnStatement(statement);
        trn.visitReturnStatement(statement);
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        super.visitAssertStatement(statement);
        trn.visitAssertStatement(statement);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        super.visitTryCatchFinally(statement);
        trn.visitTryCatchFinally(statement);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        super.visitSwitch(statement);
        trn.visitSwitch(statement);
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        super.visitCaseStatement(statement);
        trn.visitCaseStatement(statement);
    }

    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        super.visitBreakStatement(statement);
        trn.visitBreakStatement(statement);
    }

    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        super.visitContinueStatement(statement);
        trn.visitContinueStatement(statement);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);
        trn.visitSynchronizedStatement(statement);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        super.visitThrowStatement(statement);
        trn.visitThrowStatement(statement);
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        super.visitStaticMethodCallExpression(call);
        trn.visitStaticMethodCallExpression(call);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        trn.visitBinaryExpression(expression);
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        super.visitTernaryExpression(expression);
        trn.visitTernaryExpression(expression);
    }

    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
        super.visitShortTernaryExpression(expression);
        trn.visitShortTernaryExpression(expression);
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        super.visitPostfixExpression(expression);
        trn.visitPostfixExpression(expression);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        super.visitPrefixExpression(expression);
        trn.visitPrefixExpression(expression);
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        super.visitBooleanExpression(expression);
        trn.visitBooleanExpression(expression);
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        super.visitNotExpression(expression);
        trn.visitNotExpression(expression);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        super.visitClosureExpression(expression);
        trn.visitClosureExpression(expression);
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        super.visitTupleExpression(expression);
        trn.visitTupleExpression(expression);
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        super.visitListExpression(expression);
        trn.visitListExpression(expression);
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        super.visitArrayExpression(expression);
        trn.visitArrayExpression(expression);
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        super.visitMapExpression(expression);
        trn.visitMapExpression(expression);
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        super.visitMapEntryExpression(expression);
        trn.visitMapEntryExpression(expression);
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
        trn.visitRangeExpression(expression);
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        super.visitSpreadExpression(expression);
        trn.visitSpreadExpression(expression);
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
        trn.visitSpreadMapExpression(expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        super.visitMethodPointerExpression(expression);
        trn.visitMethodPointerExpression(expression);
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
        trn.visitUnaryMinusExpression(expression);
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
        trn.visitUnaryPlusExpression(expression);
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        super.visitBitwiseNegationExpression(expression);
        trn.visitBitwiseNegationExpression(expression);
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
        trn.visitCastExpression(expression);
    }

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        super.visitConstantExpression(expression);
        trn.visitConstantExpression(expression);
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        super.visitClassExpression(expression);
        trn.visitClassExpression(expression);
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        super.visitVariableExpression(expression);
        trn.visitVariableExpression(expression);
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);
        trn.visitDeclarationExpression(expression);
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        super.visitPropertyExpression(expression);
        trn.visitPropertyExpression(expression);
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        super.visitAttributeExpression(expression);
        trn.visitAttributeExpression(expression);
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        super.visitFieldExpression(expression);
        trn.visitFieldExpression(expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        super.visitGStringExpression(expression);
        trn.visitGStringExpression(expression);
    }

    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        super.visitCatchStatement(statement);
        trn.visitCatchStatement(statement);
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
        trn.visitArgumentlistExpression(ale);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        super.visitClosureListExpression(cle);
        trn.visitClosureListExpression(cle);
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        super.visitBytecodeExpression(cle);
        trn.visitBytecodeExpression(cle);
    }
}
