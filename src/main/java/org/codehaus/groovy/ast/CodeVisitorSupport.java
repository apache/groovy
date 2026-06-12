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
 * Abstract base class for implementing the {@link GroovyCodeVisitor} interface, providing
 * default implementations for all visit methods that perform depth-first traversal of the AST.
 *
 * <p>This class implements the visitor design pattern for Groovy AST nodes. Subclasses may
 * override specific visit methods to perform custom processing, while other visit methods
 * automatically traverse child nodes. Default implementations delegate to child nodes,
 * allowing for simple tree traversal without accumulating logic in each subclass.
 *
 * <p>The traversal pattern follows the structure of the AST:
 * <ul>
 *   <li>Block and control flow statements recursively visit their contained statements</li>
 *   <li>Expressions recursively visit their operand expressions</li>
 *   <li>Terminal expressions (constants, variables) perform no traversal</li>
 * </ul>
 *
 * <p>Subclasses typically override {@code visit*} methods of interest and call
 * {@code super.visit*(node)} to maintain the default traversal behavior or to
 * perform post-processing after traversing children.
 *
 * @see GroovyCodeVisitor
 * @see ClassCodeVisitorSupport for class-level visitor support
 * @see TransformingCodeVisitor for transforming visitor implementation
 */
public abstract class CodeVisitorSupport implements GroovyCodeVisitor {

    /**
     * Visits a {@link BlockStatement}, traversing each contained statement in order.
     *
     * @param block the block statement to visit, may contain multiple statements
     * @see #visitForLoop(ForStatement)
     * @see #visitWhileLoop(WhileStatement)
     */
    @Override
    public void visitBlockStatement(final BlockStatement block) {
        for (Statement statement : block.getStatements()) {
            statement.visit(this);
        }
    }

    /**
     * Visits a {@link ForStatement}, traversing the collection expression and loop body.
     *
     * @param statement the for loop statement
     */
    @Override
    public void visitForLoop(final ForStatement statement) {
        statement.getCollectionExpression().visit(this);
        statement.getLoopBlock().visit(this);
    }

    /**
     * Visits a {@link WhileStatement}, traversing the boolean condition and loop body.
     *
     * @param statement the while loop statement
     */
    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getLoopBlock().visit(this);
    }

    /**
     * Visits a {@link DoWhileStatement}, traversing the loop body before the boolean condition.
     *
     * @param statement the do-while loop statement
     */
    @Override
    public void visitDoWhileLoop(final DoWhileStatement statement) {
        statement.getLoopBlock().visit(this);
        statement.getBooleanExpression().visit(this);
    }

    /**
     * Visits an {@link IfStatement}, traversing the condition, if-block, and optional else-block.
     *
     * @param statement the if-else statement
     */
    @Override
    public void visitIfElse(final IfStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getIfBlock().visit(this);
        statement.getElseBlock().visit(this);
    }

    /**
     * Visits an {@link ExpressionStatement}, traversing its contained expression.
     *
     * @param statement the expression statement
     */
    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        statement.getExpression().visit(this);
    }

    /**
     * Visits a {@link ReturnStatement}, traversing its return expression.
     *
     * @param statement the return statement
     */
    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    /**
     * Visits an {@link AssertStatement}, traversing the assertion condition and optional message expression.
     *
     * @param statement the assert statement
     */
    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        statement.getBooleanExpression().visit(this);
        statement.getMessageExpression().visit(this);
    }

    /**
     * Visits a {@link TryCatchStatement}, traversing resource statements, try block, catch statements, and finally block.
     *
     * @param statement the try-catch-finally statement
     */
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

    /**
     * Visits a {@link CatchStatement}, traversing the catch block code.
     *
     * @param statement the catch statement
     */
    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        statement.getCode().visit(this);
    }

    /**
     * Visits a {@link SwitchStatement}, traversing the switch expression, case statements, and default statement.
     *
     * @param statement the switch statement
     */
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
     * Hook method called after the switch condition expression is visited, but before case statements.
     * Subclasses may override to perform processing between condition and cases.
     *
     * @param statement the switch statement being visited
     * @since 3.0.0
     */
    protected void afterSwitchConditionExpressionVisited(final SwitchStatement statement) {
        // hook for subclass to do something after switch condition, but before case(s)
    }

    /**
     * Hook method called after all case statements are visited, but before the default statement.
     * Subclasses may override to perform processing between cases and default.
     *
     * @param statement the switch statement being visited
     * @since 5.0.0
     */
    protected void afterSwitchCaseStatementsVisited(final SwitchStatement statement) {
    }

    /**
     * Visits a {@link CaseStatement}, traversing the case expression and code block.
     *
     * @param statement the case statement
     */
    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    /**
     * Visits a {@link BreakStatement}. No traversal is performed as break statements contain no child nodes.
     *
     * @param statement the break statement
     */
    @Override
    public void visitBreakStatement(final BreakStatement statement) {
    }

    /**
     * Visits a {@link ContinueStatement}. No traversal is performed as continue statements contain no child nodes.
     *
     * @param statement the continue statement
     */
    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
    }

    /**
     * Visits a {@link SynchronizedStatement}, traversing the synchronization expression and code block.
     *
     * @param statement the synchronized statement
     */
    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        statement.getExpression().visit(this);
        statement.getCode().visit(this);
    }

    /**
     * Visits a {@link ThrowStatement}, traversing the exception expression.
     *
     * @param statement the throw statement
     */
    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        statement.getExpression().visit(this);
    }

    /**
     * Visits an {@link EmptyStatement}. No traversal is performed as empty statements contain no child nodes.
     *
     * @param statement the empty statement
     */
    @Override
    public void visitEmptyStatement(final EmptyStatement statement) {
    }

    /**
     * Visits a {@link org.codehaus.groovy.ast.expr.MethodCallExpression}, traversing the object expression,
     * method expression, and argument list.
     *
     * @param call the method call expression
     */
    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        call.getObjectExpression().visit(this);
        call.getMethod().visit(this);
        call.getArguments().visit(this);
    }

    /**
     * Visits a {@link StaticMethodCallExpression}, traversing its argument list.
     *
     * @param call the static method call expression
     */
    @Override
    public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
        call.getArguments().visit(this);
    }

    /**
     * Visits a {@link ConstructorCallExpression}, traversing its argument list.
     *
     * @param call the constructor call expression
     */
    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        call.getArguments().visit(this);
    }

    /**
     * Visits a {@link BinaryExpression}, traversing left and right operand expressions.
     *
     * @param expression the binary expression
     */
    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        expression.getLeftExpression().visit(this);
        expression.getRightExpression().visit(this);
    }

    /**
     * Visits a {@link TernaryExpression}, traversing the condition, true-branch, and false-branch expressions.
     *
     * @param expression the ternary expression
     */
    @Override
    public void visitTernaryExpression(TernaryExpression expression) {
        expression.getBooleanExpression().visit(this);
        expression.getTrueExpression().visit(this);
        expression.getFalseExpression().visit(this);
    }

    /**
     * Visits a {@link ElvisOperatorExpression}, treating it as a ternary expression.
     *
     * @param expression the elvis operator expression
     */
    @Override
    public void visitShortTernaryExpression(ElvisOperatorExpression expression) {
        visitTernaryExpression(expression);
    }

    /**
     * Visits a {@link PostfixExpression}, traversing the operand expression.
     *
     * @param expression the postfix expression
     */
    @Override
    public void visitPostfixExpression(PostfixExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link PrefixExpression}, traversing the operand expression.
     *
     * @param expression the prefix expression
     */
    @Override
    public void visitPrefixExpression(PrefixExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link BooleanExpression}, traversing the contained expression.
     *
     * @param expression the boolean expression
     */
    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link NotExpression}, traversing the operand expression.
     *
     * @param expression the not expression
     */
    @Override
    public void visitNotExpression(NotExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link ClosureExpression}, traversing parameter initializers and the closure code block.
     *
     * @param expression the closure expression
     */
    @Override
    public void visitClosureExpression(ClosureExpression expression) {
        if (expression.isParameterSpecified()) {
            for (Parameter parameter : expression.getParameters()) {
                if (parameter.hasInitialExpression())
                    parameter.getInitialExpression().visit(this);
            }
        }
        expression.getCode().visit(this);
    }

    /**
     * Visits a {@link LambdaExpression}, treating it as a closure expression.
     *
     * @param expression the lambda expression
     */
    @Override
    public void visitLambdaExpression(LambdaExpression expression) {
        visitClosureExpression(expression);
    }

    /**
     * Visits a {@link TupleExpression}, traversing all contained expressions.
     *
     * @param expression the tuple expression
     */
    @Override
    public void visitTupleExpression(TupleExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    /**
     * Visits a {@link ListExpression}, traversing all contained expressions.
     *
     * @param expression the list expression
     */
    @Override
    public void visitListExpression(ListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    /**
     * Visits an {@link ArrayExpression}, traversing element expressions and size expressions.
     *
     * @param expression the array expression
     */
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        visitListOfExpressions(expression.getExpressions());
        visitListOfExpressions(expression.getSizeExpression());
    }

    /**
     * Visits a {@link MapExpression}, traversing all map entry expressions.
     *
     * @param expression the map expression
     */
    @Override
    public void visitMapExpression(MapExpression expression) {
        visitListOfExpressions(expression.getMapEntryExpressions());
    }

    /**
     * Visits a {@link MapEntryExpression}, traversing key and value expressions.
     *
     * @param expression the map entry expression
     */
    @Override
    public void visitMapEntryExpression(MapEntryExpression expression) {
        expression.getKeyExpression().visit(this);
        expression.getValueExpression().visit(this);
    }

    /**
     * Visits a {@link RangeExpression}, traversing from and to boundary expressions.
     *
     * @param expression the range expression
     */
    @Override
    public void visitRangeExpression(RangeExpression expression) {
        expression.getFrom().visit(this);
        expression.getTo().visit(this);
    }

    /**
     * Visits a {@link SpreadExpression}, traversing the operand expression.
     *
     * @param expression the spread expression
     */
    @Override
    public void visitSpreadExpression(SpreadExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link SpreadMapExpression}, traversing the operand expression.
     *
     * @param expression the spread map expression
     */
    @Override
    public void visitSpreadMapExpression(SpreadMapExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link MethodPointerExpression}, traversing the object and method name expressions.
     *
     * @param expression the method pointer expression
     */
    @Override
    public void visitMethodPointerExpression(MethodPointerExpression expression) {
        expression.getExpression().visit(this);
        expression.getMethodName().visit(this);
    }

    /**
     * Visits a {@link org.codehaus.groovy.ast.expr.MethodReferenceExpression}, treating it as a method pointer.
     *
     * @param expression the method reference expression
     */
    @Override
    public void visitMethodReferenceExpression(MethodReferenceExpression expression) {
        visitMethodPointerExpression(expression);
    }

    /**
     * Visits a {@link UnaryMinusExpression}, traversing the operand expression.
     *
     * @param expression the unary minus expression
     */
    @Override
    public void visitUnaryMinusExpression(UnaryMinusExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link UnaryPlusExpression}, traversing the operand expression.
     *
     * @param expression the unary plus expression
     */
    @Override
    public void visitUnaryPlusExpression(UnaryPlusExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link BitwiseNegationExpression}, traversing the operand expression.
     *
     * @param expression the bitwise negation expression
     */
    @Override
    public void visitBitwiseNegationExpression(BitwiseNegationExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link CastExpression}, traversing the operand expression.
     *
     * @param expression the cast expression
     */
    @Override
    public void visitCastExpression(CastExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Visits a {@link ConstantExpression}. No traversal is performed as constants contain no child expressions.
     *
     * @param expression the constant expression
     */
    @Override
    public void visitConstantExpression(ConstantExpression expression) {
    }

    /**
     * Visits a {@link ClassExpression}. No traversal is performed as class expressions contain no child expressions.
     *
     * @param expression the class expression
     */
    @Override
    public void visitClassExpression(ClassExpression expression) {
    }

    /**
     * Visits a {@link VariableExpression}. No traversal is performed as variable expressions contain no child expressions.
     *
     * @param expression the variable expression
     */
    @Override
    public void visitVariableExpression(VariableExpression expression) {
    }

    /**
     * Visits a {@link DeclarationExpression}, treating it as a binary expression.
     *
     * @param expression the declaration expression
     */
    @Override
    public void visitDeclarationExpression(DeclarationExpression expression) {
        visitBinaryExpression(expression);
    }

    /**
     * Visits a {@link PropertyExpression}, traversing the object and property name expressions.
     *
     * @param expression the property expression
     */
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    /**
     * Visits an {@link AttributeExpression}, traversing the object and attribute name expressions.
     *
     * @param expression the attribute expression
     */
    @Override
    public void visitAttributeExpression(AttributeExpression expression) {
        expression.getObjectExpression().visit(this);
        expression.getProperty().visit(this);
    }

    /**
     * Visits a {@link FieldExpression}. No traversal is performed as field expressions contain no child expressions.
     *
     * @param expression the field expression
     */
    @Override
    public void visitFieldExpression(FieldExpression expression) {
    }

    /**
     * Visits a {@link GStringExpression}, traversing string parts and interpolated value expressions.
     *
     * @param expression the GString expression
     */
    @Override
    public void visitGStringExpression(GStringExpression expression) {
        visitListOfExpressions(expression.getStrings());
        visitListOfExpressions(expression.getValues());
    }

    /**
     * Visits an {@link ArgumentListExpression}, treating it as a tuple expression.
     *
     * @param expression the argument list expression
     */
    @Override
    public void visitArgumentlistExpression(ArgumentListExpression expression) {
        visitTupleExpression(expression);
    }

    /**
     * Visits a {@link ClosureListExpression}, traversing all contained expressions.
     *
     * @param expression the closure list expression
     */
    @Override
    public void visitClosureListExpression(ClosureListExpression expression) {
        visitListOfExpressions(expression.getExpressions());
    }

    /**
     * Visits a {@link BytecodeExpression}. No traversal is performed as bytecode expressions are terminal.
     *
     * @param expression the bytecode expression
     */
    @Override
    public void visitBytecodeExpression(BytecodeExpression expression) {
    }
}
