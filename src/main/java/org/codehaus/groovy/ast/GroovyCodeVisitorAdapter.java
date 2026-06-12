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
 * Adapter class providing default no-op implementations of all methods in the {@link GroovyCodeVisitor} interface.
 * Subclasses can override only the visitor methods relevant to their use case, simplifying implementation
 * of custom AST visitors that do not need to handle all node types.
 *
 * <p>The adapter includes delegation strategies for related expression types:
 * {@code visitDeclarationExpression()} delegates to {@code visitBinaryExpression()};
 * {@code visitNotExpression()} delegates to {@code visitBooleanExpression()};
 * {@code visitLambdaExpression()} delegates to {@code visitClosureExpression()};
 * {@code visitAttributeExpression()} delegates to {@code visitPropertyExpression()};
 * {@code visitArgumentlistExpression()} delegates to {@code visitTupleExpression()};
 * {@code visitClosureListExpression()} delegates to {@code visitListExpression()};
 * {@code visitMethodReferenceExpression()} delegates to {@code visitMethodPointerExpression()};
 * {@code visitShortTernaryExpression()} (elvis operator) delegates to {@code visitTernaryExpression()}.
 *
 * @see GroovyCodeVisitor
 * @since 4.0.0
 */
public class GroovyCodeVisitorAdapter implements GroovyCodeVisitor {

    // Statements:

    /**
     * Visits an {@link AssertStatement}. Default implementation is no-op.
     *
     * @param statement the assert statement
     */
    @Override
    public void visitAssertStatement(AssertStatement statement) {
    }

    /**
     * Visits a {@link BlockStatement}. Default implementation is no-op.
     *
     * @param statement the block statement
     */
    @Override
    public void visitBlockStatement(BlockStatement statement) {
    }

    /**
     * Visits a {@link BreakStatement}. Default implementation is no-op.
     *
     * @param statement the break statement
     */
    @Override
    public void visitBreakStatement(BreakStatement statement) {
    }

    /**
     * Visits a {@link CaseStatement}. Default implementation is no-op.
     *
     * @param statement the case statement
     */
    @Override
    public void visitCaseStatement(CaseStatement statement) {
    }

    /**
     * Visits a {@link CatchStatement}. Default implementation is no-op.
     *
     * @param statement the catch statement
     */
    @Override
    public void visitCatchStatement(CatchStatement statement) {
    }

    /**
     * Visits a {@link ContinueStatement}. Default implementation is no-op.
     *
     * @param statement the continue statement
     */
    @Override
    public void visitContinueStatement(ContinueStatement statement) {
    }

    /**
     * Visits a {@link DoWhileStatement}. Default implementation is no-op.
     *
     * @param statement the do-while loop statement
     */
    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
    }

    /**
     * Visits an {@link EmptyStatement}. Default implementation is no-op.
     *
     * @param statement the empty statement
     */
    @Override
    public void visitEmptyStatement(EmptyStatement statement) {
    }

    /**
     * Visits an {@link ExpressionStatement}. Default implementation is no-op.
     *
     * @param statement the expression statement
     */
    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
    }

    /**
     * Visits a {@link ForStatement}. Default implementation is no-op.
     *
     * @param statement the for loop statement
     */
    @Override
    public void visitForLoop(ForStatement statement) {
    }

    /**
     * Visits an {@link IfStatement}. Default implementation is no-op.
     *
     * @param statement the if-else statement
     */
    @Override
    public void visitIfElse(IfStatement statement) {
    }

    /**
     * Visits a {@link ReturnStatement}. Default implementation is no-op.
     *
     * @param statement the return statement
     */
    @Override
    public void visitReturnStatement(ReturnStatement statement) {
    }

    /**
     * Visits a {@link SwitchStatement}. Default implementation is no-op.
     *
     * @param statement the switch statement
     */
    @Override
    public void visitSwitch(SwitchStatement statement) {
    }

    /**
     * Visits a {@link SynchronizedStatement}. Default implementation is no-op.
     *
     * @param statement the synchronized statement
     */
    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
    }

    /**
     * Visits a {@link ThrowStatement}. Default implementation is no-op.
     *
     * @param statement the throw statement
     */
    @Override
    public void visitThrowStatement(ThrowStatement statement) {
    }

    /**
     * Visits a {@link TryCatchStatement}. Default implementation is no-op.
     *
     * @param statement the try-catch-finally statement
     */
    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
    }

    /**
     * Visits a {@link WhileStatement}. Default implementation is no-op.
     *
     * @param statement the while loop statement
     */
    @Override
    public void visitWhileLoop(WhileStatement statement) {
    }

    // Expressions:

    /**
     * Visits an {@link ArgumentListExpression}.
     * Default implementation delegates to {@link #visitTupleExpression(TupleExpression)}.
     *
     * @param expression the argument list expression
     */
    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    /**
     * Visits an {@link ArrayExpression}. Default implementation is no-op.
     *
     * @param expression the array expression
     */
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
    }

    /**
     * Visits an {@link AttributeExpression}.
     * Default implementation delegates to {@link #visitPropertyExpression(PropertyExpression)}.
     *
     * @param expression the attribute expression
     */
    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        visitPropertyExpression(expression);
    }

    /**
     * Visits a {@link BinaryExpression}. Default implementation is no-op.
     *
     * @param expression the binary expression
     */
    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
    }

    /**
     * Visits a {@link BitwiseNegationExpression}. Default implementation is no-op.
     *
     * @param expression the bitwise negation expression
     */
    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
    }

    /**
     * Visits a {@link BooleanExpression}. Default implementation is no-op.
     *
     * @param expression the boolean expression
     */
    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
    }

    /**
     * Visits a {@link BytecodeExpression}. Default implementation is no-op.
     *
     * @param expression the bytecode expression
     */
    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
    }

    /**
     * Visits a {@link CastExpression}. Default implementation is no-op.
     *
     * @param expression the cast expression
     */
    @Override
    public void visitCastExpression(CastExpression expression) {
    }

    /**
     * Visits a {@link ClassExpression}. Default implementation is no-op.
     *
     * @param expression the class expression
     */
    @Override
    public void visitClassExpression(ClassExpression expression) {
    }

    /**
     * Visits a {@link ClosureExpression}. Default implementation is no-op.
     *
     * @param expression the closure expression
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
    }

    /**
     * Visits a {@link ClosureListExpression}.
     * Default implementation delegates to {@link #visitListExpression(ListExpression)}.
     *
     * @param expression the closure list expression
     */
    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        visitListExpression(expression);
    }

    /**
     * Visits a {@link ConstantExpression}. Default implementation is no-op.
     *
     * @param expression the constant expression
     */
    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    }

    /**
     * Visits a {@link ConstructorCallExpression}. Default implementation is no-op.
     *
     * @param expression the constructor call expression
     */
    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression expression) {
    }

    /**
     * Visits a {@link DeclarationExpression}.
     * Default implementation delegates to {@link #visitBinaryExpression(BinaryExpression)}.
     *
     * @param expression the declaration expression
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    /**
     * Visits an {@link EmptyExpression}. Default implementation is no-op.
     *
     * @param expression the empty expression
     */
    @Override
    public void visitEmptyExpression(EmptyExpression expression) {
    }

    /**
     * Visits a {@link FieldExpression}. Default implementation is no-op.
     *
     * @param expression the field expression
     */
    @Override
    public void visitFieldExpression(FieldExpression expression) {
    }

    /**
     * Visits a {@link GStringExpression}. Default implementation is no-op.
     *
     * @param expression the GString expression
     */
    @Override
    public void visitGStringExpression(GStringExpression expression) {
    }

    /**
     * Visits a {@link LambdaExpression}.
     * Default implementation delegates to {@link #visitClosureExpression(ClosureExpression)}.
     *
     * @param expression the lambda expression
     */
    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        visitClosureExpression(expression);
    }

    /**
     * Visits a {@link ListExpression}. Default implementation is no-op.
     *
     * @param expression the list expression
     */
    @Override
    public void visitListExpression(ListExpression expression) {
    }

    /**
     * Visits a {@link MapExpression}. Default implementation is no-op.
     *
     * @param expression the map expression
     */
    @Override
    public void visitMapExpression(MapExpression expression) {
    }

    /**
     * Visits a {@link MapEntryExpression}. Default implementation is no-op.
     *
     * @param expression the map entry expression
     */
    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
    }

    /**
     * Visits a {@link MethodCallExpression}. Default implementation is no-op.
     *
     * @param expression the method call expression
     */
    @Override
    public void visitMethodCallExpression(MethodCallExpression expression) {
    }

    /**
     * Visits a {@link MethodPointerExpression}. Default implementation is no-op.
     *
     * @param expression the method pointer expression
     */
    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
    }

    /**
     * Visits a {@link MethodReferenceExpression}.
     * Default implementation delegates to {@link #visitMethodPointerExpression(MethodPointerExpression)}.
     *
     * @param expression the method reference expression
     */
    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        visitMethodPointerExpression(expression);
    }

    /**
     * Visits a {@link NotExpression}.
     * Default implementation delegates to {@link #visitBooleanExpression(BooleanExpression)}.
     *
     * @param expression the not expression
     */
    @Override
    public void visitNotExpression(NotExpression expression) {
        visitBooleanExpression(expression);
    }

    /**
     * Visits a {@link PostfixExpression}. Default implementation is no-op.
     *
     * @param expression the postfix expression
     */
    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
    }

    /**
     * Visits a {@link PrefixExpression}. Default implementation is no-op.
     *
     * @param expression the prefix expression
     */
    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
    }

    /**
     * Visits a {@link PropertyExpression}. Default implementation is no-op.
     *
     * @param expression the property expression
     */
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
    }

    /**
     * Visits a {@link RangeExpression}. Default implementation is no-op.
     *
     * @param expression the range expression
     */
    @Override
    public void visitRangeExpression(RangeExpression expression) {
    }

    /**
     * Visits an {@link ElvisOperatorExpression} (short ternary operator).
     * Default implementation delegates to {@link #visitTernaryExpression(TernaryExpression)}.
     *
     * @param expression the elvis operator expression
     */
    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    /**
     * Visits a {@link SpreadExpression}. Default implementation is no-op.
     *
     * @param expression the spread expression
     */
    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
    }

    /**
     * Visits a {@link SpreadMapExpression}. Default implementation is no-op.
     *
     * @param expression the spread map expression
     */
    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
    }

    /**
     * Visits a {@link StaticMethodCallExpression}. Default implementation is no-op.
     *
     * @param expression the static method call expression
     */
    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression expression) {
    }

    /**
     * Visits a {@link TernaryExpression}. Default implementation is no-op.
     *
     * @param expression the ternary expression
     */
    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
    }

    /**
     * Visits a {@link TupleExpression}. Default implementation is no-op.
     *
     * @param expression the tuple expression
     */
    @Override
    public void visitTupleExpression(TupleExpression expression) {
    }

    /**
     * Visits a {@link VariableExpression}. Default implementation is no-op.
     *
     * @param expression the variable expression
     */
    @Override
    public void visitVariableExpression(VariableExpression expression) {
    }

    /**
     * Visits a {@link UnaryMinusExpression}. Default implementation is no-op.
     *
     * @param expression the unary minus expression
     */
    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
    }

    /**
     * Visits a {@link UnaryPlusExpression}. Default implementation is no-op.
     *
     * @param expression the unary plus expression
     */
    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
    }
}
