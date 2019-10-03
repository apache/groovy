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
import org.codehaus.groovy.ast.expr.Expression;
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

import java.util.List;

/**
 * An implementation of the visitor pattern for working with ASTNodes.
 */
public interface GroovyCodeVisitor {

    //--------------------------------------------------------------------------
    // statements

    void visitBlockStatement(BlockStatement statement);

    void visitForLoop(ForStatement statement);

    void visitWhileLoop(WhileStatement statement);

    void visitDoWhileLoop(DoWhileStatement statement);

    void visitIfElse(IfStatement statement);

    void visitExpressionStatement(ExpressionStatement statement);

    void visitReturnStatement(ReturnStatement statement);

    void visitAssertStatement(AssertStatement statement);

    void visitTryCatchFinally(TryCatchStatement statement);

    void visitSwitch(SwitchStatement statement);

    void visitCaseStatement(CaseStatement statement);

    void visitBreakStatement(BreakStatement statement);

    void visitContinueStatement(ContinueStatement statement);

    void visitThrowStatement(ThrowStatement statement);

    void visitSynchronizedStatement(SynchronizedStatement statement);

    void visitCatchStatement(CatchStatement statement);

    default void visitEmptyStatement(EmptyStatement statement) {}

    //--------------------------------------------------------------------------
    // expressions

    void visitMethodCallExpression(MethodCallExpression expression);

    void visitStaticMethodCallExpression(StaticMethodCallExpression expression);

    void visitConstructorCallExpression(ConstructorCallExpression expression);

    void visitTernaryExpression(TernaryExpression expression);

    void visitShortTernaryExpression(ElvisOperatorExpression expression);

    void visitBinaryExpression(BinaryExpression expression);

    void visitPrefixExpression(PrefixExpression expression);

    void visitPostfixExpression(PostfixExpression expression);

    void visitBooleanExpression(BooleanExpression expression);

    void visitClosureExpression(ClosureExpression expression);

    void visitLambdaExpression(LambdaExpression expression);

    void visitTupleExpression(TupleExpression expression);

    void visitMapExpression(MapExpression expression);

    void visitMapEntryExpression(MapEntryExpression expression);

    void visitListExpression(ListExpression expression);

    void visitRangeExpression(RangeExpression expression);

    void visitPropertyExpression(PropertyExpression expression);

    void visitAttributeExpression(AttributeExpression expression);

    void visitFieldExpression(FieldExpression expression);

    void visitMethodPointerExpression(MethodPointerExpression expression);

    void visitMethodReferenceExpression(MethodReferenceExpression expression);

    void visitConstantExpression(ConstantExpression expression);

    void visitClassExpression(ClassExpression expression);

    void visitVariableExpression(VariableExpression expression);

    void visitDeclarationExpression(DeclarationExpression expression);

    void visitGStringExpression(GStringExpression expression);

    void visitArrayExpression(ArrayExpression expression);

    void visitSpreadExpression(SpreadExpression expression);

    void visitSpreadMapExpression(SpreadMapExpression expression);

    void visitNotExpression(NotExpression expression);

    void visitUnaryMinusExpression(UnaryMinusExpression expression);

    void visitUnaryPlusExpression(UnaryPlusExpression expression);

    void visitBitwiseNegationExpression(BitwiseNegationExpression expression);

    void visitCastExpression(CastExpression expression);

    void visitArgumentlistExpression(ArgumentListExpression expression);

    void visitClosureListExpression(ClosureListExpression expression);

    void visitBytecodeExpression(BytecodeExpression expression);

    default void visitEmptyExpression(EmptyExpression expression) {}

    default void visitListOfExpressions(List<? extends Expression> list) {
        if (list == null) return;
        for (Expression expression : list) {
            if (expression instanceof SpreadExpression) {
                Expression spread = ((SpreadExpression) expression).getExpression();
                spread.visit(this);
            } else {
                expression.visit(this);
            }
        }
    }
}
