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
import org.codehaus.groovy.ast.expr.EmptyExpression;
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
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

/**
 * @since 4.0.0
 */
public class GroovyCodeVisitorAdapter implements GroovyCodeVisitor {

    // statements:

    @Override
    public void visitAssertStatement(AssertStatement statement) {
    }

    @Override
    public void visitBlockStatement(BlockStatement statement) {
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
    }

    public void visitEmptyStatement(EmptyStatement statement) {
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
    }

    @Override
    public void visitForLoop(ForStatement statement) {
    }

    @Override
    public void visitIfElse(IfStatement statement) {
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
    }

    @Override
    public void visitWhileLoop(WhileStatement statement) {
    }

    // expressions:

    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    @Override
    public void visitArrayExpression(ArrayExpression expression) {
    }

    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        visitPropertyExpression(expression);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
    }

    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
    }

    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
    }

    @Override
    public void visitCastExpression(CastExpression expression) {
    }

    @Override
    public void visitClassExpression(ClassExpression expression) {
    }

    @Override
    public void visitClosureExpression(ClosureExpression expression) {
    }

    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        visitListExpression(expression);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
    }

    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    @Override
    public void visitEmptyExpression(EmptyExpression expression) {
    }

    @Override
    public void visitFieldExpression(FieldExpression expression) {
    }

    @Override
    public void visitGStringExpression(GStringExpression expression) {
    }

    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        visitClosureExpression(expression);
    }

    @Override
    public void visitListExpression(ListExpression expression) {
    }

    @Override
    public void visitMapExpression(MapExpression expression) {
    }

    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
    }

    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
    }

    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        visitMethodPointerExpression(expression);
    }

    @Override
    public void visitNotExpression(NotExpression expression) {
        visitBooleanExpression(expression);
    }

    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
    }

    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
    }

    @Override
    public void visitRangeExpression(RangeExpression expression) {
    }

    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
    }

    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
    }

    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
    }

    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
    }

    @Override
    public void visitTupleExpression(TupleExpression expression) {
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
    }

    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
    }

    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
    }
}
