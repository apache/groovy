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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

import java.util.List;

/**
 * Defines the visitor interface for traversing and processing Groovy AST nodes using the visitor pattern.
 * Implementations can perform compilation, transformation, code generation, or analysis by implementing
 * visit methods for specific statement and expression types. The interface provides both required abstract
 * methods and default convenience methods for handling generic collections and null values.
 *
 * @see Statement
 * @see Expression
 * @see org.codehaus.groovy.ast.ClassCodeVisitorSupport
 * @see org.codehaus.groovy.ast.CodeVisitorSupport
 */
public interface GroovyCodeVisitor {

    //--------------------------------------------------------------------------
    // statements

    /**
     * Visits a block statement (list of statements enclosed in braces).
     *
     * @param statement the {@link BlockStatement} to process
     */
    void visitBlockStatement(BlockStatement statement);

    /**
     * Visits a for loop statement.
     *
     * @param statement the {@link ForStatement} to process
     */
    void visitForLoop(ForStatement statement);

    /**
     * Visits a while loop statement.
     *
     * @param statement the {@link WhileStatement} to process
     */
    void visitWhileLoop(WhileStatement statement);

    /**
     * Visits a do-while loop statement.
     *
     * @param statement the {@link DoWhileStatement} to process
     */
    void visitDoWhileLoop(DoWhileStatement statement);

    /**
     * Visits an if-else statement.
     *
     * @param statement the {@link IfStatement} to process
     */
    void visitIfElse(IfStatement statement);

    /**
     * Visits an expression statement (an expression used as a statement).
     *
     * @param statement the {@link ExpressionStatement} to process
     */
    void visitExpressionStatement(ExpressionStatement statement);

    /**
     * Visits a return statement.
     *
     * @param statement the {@link ReturnStatement} to process
     */
    void visitReturnStatement(ReturnStatement statement);

    /**
     * Visits an assert statement.
     *
     * @param statement the {@link AssertStatement} to process
     */
    void visitAssertStatement(AssertStatement statement);

    /**
     * Visits a try-catch-finally statement.
     *
     * @param statement the {@link TryCatchStatement} to process
     */
    void visitTryCatchFinally(TryCatchStatement statement);

    /**
     * Visits a switch statement.
     *
     * @param statement the {@link SwitchStatement} to process
     */
    void visitSwitch(SwitchStatement statement);

    /**
     * Visits a case clause within a switch statement.
     *
     * @param statement the {@link CaseStatement} to process
     */
    void visitCaseStatement(CaseStatement statement);

    /**
     * Visits a break statement.
     *
     * @param statement the {@link BreakStatement} to process
     */
    void visitBreakStatement(BreakStatement statement);

    /**
     * Visits a continue statement.
     *
     * @param statement the {@link ContinueStatement} to process
     */
    void visitContinueStatement(ContinueStatement statement);

    /**
     * Visits a throw statement.
     *
     * @param statement the {@link ThrowStatement} to process
     */
    void visitThrowStatement(ThrowStatement statement);

    /**
     * Visits a synchronized statement.
     *
     * @param statement the {@link SynchronizedStatement} to process
     */
    void visitSynchronizedStatement(SynchronizedStatement statement);

    /**
     * Visits a catch clause within a try-catch statement.
     *
     * @param statement the {@link CatchStatement} to process
     */
    void visitCatchStatement(CatchStatement statement);

    /**
     * Visits an empty statement. Default implementation does nothing.
     *
     * @param statement the {@link EmptyStatement} to process
     */
    default void visitEmptyStatement(EmptyStatement statement) {
    }

    /**
     * Convenience method to visit a statement if it is not null.
     * Delegates to the statement's visit method.
     *
     * @param statement the {@link Statement} to process, or null to do nothing
     */
    default void visit(final Statement statement) {
        if (statement != null) {
            statement.visit(this);
        }
    }

    //--------------------------------------------------------------------------
    // expressions

    /**
     * Visits a method call expression.
     *
     * @param expression the {@link MethodCallExpression} to process
     */
    void visitMethodCallExpression(MethodCallExpression expression);

    /**
     * Visits a static method call expression.
     *
     * @param expression the {@link StaticMethodCallExpression} to process
     */
    void visitStaticMethodCallExpression(StaticMethodCallExpression expression);

    /**
     * Visits a constructor call expression.
     *
     * @param expression the {@link ConstructorCallExpression} to process
     */
    void visitConstructorCallExpression(ConstructorCallExpression expression);

    /**
     * Visits a ternary expression (condition ? trueExpr : falseExpr).
     *
     * @param expression the {@link TernaryExpression} to process
     */
    void visitTernaryExpression(TernaryExpression expression);

    /**
     * Visits an Elvis operator expression (shorthand ternary with implicit condition check).
     *
     * @param expression the {@link ElvisOperatorExpression} to process
     */
    void visitShortTernaryExpression(ElvisOperatorExpression expression);

    /**
     * Visits a binary expression (left op right).
     *
     * @param expression the {@link BinaryExpression} to process
     */
    void visitBinaryExpression(BinaryExpression expression);

    /**
     * Visits a prefix expression (op expr).
     *
     * @param expression the {@link PrefixExpression} to process
     */
    void visitPrefixExpression(PrefixExpression expression);

    /**
     * Visits a postfix expression (expr op).
     *
     * @param expression the {@link PostfixExpression} to process
     */
    void visitPostfixExpression(PostfixExpression expression);

    /**
     * Visits a boolean expression.
     *
     * @param expression the {@link BooleanExpression} to process
     */
    void visitBooleanExpression(BooleanExpression expression);

    /**
     * Visits a closure expression.
     *
     * @param expression the {@link ClosureExpression} to process
     */
    void visitClosureExpression(ClosureExpression expression);

    /**
     * Visits a lambda expression.
     *
     * @param expression the {@link LambdaExpression} to process
     */
    void visitLambdaExpression(LambdaExpression expression);

    /**
     * Visits a tuple expression (comma-separated expressions grouped in parentheses).
     *
     * @param expression the {@link TupleExpression} to process
     */
    void visitTupleExpression(TupleExpression expression);

    /**
     * Visits a map expression.
     *
     * @param expression the {@link MapExpression} to process
     */
    void visitMapExpression(MapExpression expression);

    /**
     * Visits a map entry expression (key: value pair within a map).
     *
     * @param expression the {@link MapEntryExpression} to process
     */
    void visitMapEntryExpression(MapEntryExpression expression);

    /**
     * Visits a list expression.
     *
     * @param expression the {@link ListExpression} to process
     */
    void visitListExpression(ListExpression expression);

    /**
     * Visits a range expression.
     *
     * @param expression the {@link RangeExpression} to process
     */
    void visitRangeExpression(RangeExpression expression);

    /**
     * Visits a property expression (object.property).
     *
     * @param expression the {@link PropertyExpression} to process
     */
    void visitPropertyExpression(PropertyExpression expression);

    /**
     * Visits an attribute expression (object@property, accessing direct field without getters).
     *
     * @param expression the {@link AttributeExpression} to process
     */
    void visitAttributeExpression(AttributeExpression expression);

    /**
     * Visits a field expression.
     *
     * @param expression the {@link FieldExpression} to process
     */
    void visitFieldExpression(FieldExpression expression);

    /**
     * Visits a method pointer expression (object.&methodName).
     *
     * @param expression the {@link MethodPointerExpression} to process
     */
    void visitMethodPointerExpression(MethodPointerExpression expression);

    /**
     * Visits a method reference expression (obj::method).
     *
     * @param expression the {@link MethodReferenceExpression} to process
     */
    void visitMethodReferenceExpression(MethodReferenceExpression expression);

    /**
     * Visits a constant expression (literal value).
     *
     * @param expression the {@link ConstantExpression} to process
     */
    void visitConstantExpression(ConstantExpression expression);

    /**
     * Visits a class expression.
     *
     * @param expression the {@link ClassExpression} to process
     */
    void visitClassExpression(ClassExpression expression);

    /**
     * Visits a variable expression.
     *
     * @param expression the {@link VariableExpression} to process
     */
    void visitVariableExpression(VariableExpression expression);

    /**
     * Visits a declaration expression (variable declaration with initialization).
     *
     * @param expression the {@link DeclarationExpression} to process
     */
    void visitDeclarationExpression(DeclarationExpression expression);

    /**
     * Visits a GString expression (string with interpolations).
     *
     * @param expression the {@link GStringExpression} to process
     */
    void visitGStringExpression(GStringExpression expression);

    /**
     * Visits an array expression.
     *
     * @param expression the {@link ArrayExpression} to process
     */
    void visitArrayExpression(ArrayExpression expression);

    /**
     * Visits a spread expression (*expr, spreading collection elements).
     *
     * @param expression the {@link SpreadExpression} to process
     */
    void visitSpreadExpression(SpreadExpression expression);

    /**
     * Visits a spread map expression (*:map, spreading map entries).
     *
     * @param expression the {@link SpreadMapExpression} to process
     */
    void visitSpreadMapExpression(SpreadMapExpression expression);

    /**
     * Visits a not expression (!expr).
     *
     * @param expression the {@link NotExpression} to process
     */
    void visitNotExpression(NotExpression expression);

    /**
     * Visits a unary minus expression (-expr).
     *
     * @param expression the {@link UnaryMinusExpression} to process
     */
    void visitUnaryMinusExpression(UnaryMinusExpression expression);

    /**
     * Visits a unary plus expression (+expr).
     *
     * @param expression the {@link UnaryPlusExpression} to process
     */
    void visitUnaryPlusExpression(UnaryPlusExpression expression);

    /**
     * Visits a bitwise negation expression (~expr).
     *
     * @param expression the {@link BitwiseNegationExpression} to process
     */
    void visitBitwiseNegationExpression(BitwiseNegationExpression expression);

    /**
     * Visits a cast expression ((Type) expr).
     *
     * @param expression the {@link CastExpression} to process
     */
    void visitCastExpression(CastExpression expression);

    /**
     * Visits an argument list expression (method arguments).
     *
     * @param expression the {@link ArgumentListExpression} to process
     */
    void visitArgumentlistExpression(ArgumentListExpression expression);

    /**
     * Visits a closure list expression.
     *
     * @param expression the {@link ClosureListExpression} to process
     */
    void visitClosureListExpression(ClosureListExpression expression);

    /**
     * Visits a bytecode expression (direct JVM bytecode instructions).
     *
     * @param expression the {@link BytecodeExpression} to process
     */
    void visitBytecodeExpression(BytecodeExpression expression);

    /**
     * Visits an empty expression. Default implementation does nothing.
     *
     * @param expression the {@link EmptyExpression} to process
     */
    default void visitEmptyExpression(EmptyExpression expression) {
    }

    /**
     * Convenience method to visit a list of expressions.
     * Iterates through the list and calls the visit method on each non-null expression.
     *
     * @param list the list of {@link Expression}s to process, or null to do nothing
     */
    default void visitListOfExpressions(final List<? extends Expression> list) {
        if (list != null) {
            for (Expression expr: list) {
                expr.visit(this);
            }
        }
    }

    /**
     * Convenience method to visit an expression if it is not null.
     * Delegates to the expression's visit method.
     *
     * @param expression the {@link Expression} to process, or null to do nothing
     */
    default void visit(final Expression expression) {
        if (expression != null) {
            expression.visit(this);
        }
    }
}
