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
package org.codehaus.groovy.macro.matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.PropertyNode;
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
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.LinkedList;
import java.util.List;

/**
 * Searches an AST for subtrees matching a given pattern.
 *
 * @since 2.5.0
 */
class ASTFinder extends ContextualClassCodeVisitor {

    private final ASTNode initial;
    private final List<TreeContext> matches = new LinkedList<TreeContext>();

    /**
     * Creates a finder for the supplied pattern node.
     *
     * @param initial the pattern to locate
     */
    ASTFinder(final ASTNode initial) {
        this.initial = initial;
    }

    /**
     * Returns no source unit because pattern finding is source-independent.
     *
     * @return {@code null}
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    /**
     * Returns the contexts of all matches found so far.
     *
     * @return the matching tree contexts
     */
    public List<TreeContext> getMatches() {
        return matches;
    }

    private void tryFind(final Class<?> clazz, final ASTNode node) {
        if (clazz.isAssignableFrom(initial.getClass()) && ASTMatcher.matches(node, initial)) {
            matches.add(getLastContext());
        }
    }

    /** Visits a class node and records it when it matches the pattern. */
    @Override
    public void visitClass(final ClassNode node) {
        super.visitClass(node);
        tryFind(ClassNode.class, node);
    }

    /** Visits a package node and records it when it matches the pattern. */
    @Override
    public void visitPackage(final PackageNode node) {
        super.visitPackage(node);
        tryFind(PackageNode.class, node);
    }

    /** Visits module imports and records them when they match the pattern. */
    @Override
    public void visitImports(final ModuleNode node) {
        super.visitImports(node);
        tryFind(ModuleNode.class, node);
    }

    /** Visits a declaration expression and records it when it matches the pattern. */
    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);
        tryFind(DeclarationExpression.class, expression);
    }

    /** Visits a constructor or method and records it when it matches the pattern. */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);
        tryFind(MethodNode.class, node);
    }

    /** Visits a field node and records it when it matches the pattern. */
    @Override
    public void visitField(final FieldNode node) {
        super.visitField(node);
        tryFind(FieldNode.class, node);
    }

    /** Visits a property node and records it when it matches the pattern. */
    @Override
    public void visitProperty(final PropertyNode node) {
        super.visitProperty(node);
        tryFind(PropertyNode.class, node);
    }

    /** Visits an assert statement and records it when it matches the pattern. */
    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        super.visitAssertStatement(statement);
        tryFind(AssertStatement.class, statement);
    }

    /** Visits a break statement and records it when it matches the pattern. */
    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        super.visitBreakStatement(statement);
        tryFind(BreakStatement.class, statement);
    }

    /** Visits a case statement and records it when it matches the pattern. */
    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        super.visitCaseStatement(statement);
        tryFind(CaseStatement.class, statement);
    }

    /** Visits a catch statement and records it when it matches the pattern. */
    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        super.visitCatchStatement(statement);
        tryFind(CatchStatement.class, statement);
    }

    /** Visits a continue statement and records it when it matches the pattern. */
    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        super.visitContinueStatement(statement);
        tryFind(ContinueStatement.class, statement);
    }

    /** Visits a do-while loop and records it when it matches the pattern. */
    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        super.visitDoWhileLoop(loop);
        tryFind(DoWhileStatement.class, loop);
    }

    /** Visits an expression statement and records it when it matches the pattern. */
    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        tryFind(ExpressionStatement.class, statement);
    }

    /** Visits a for loop and records it when it matches the pattern. */
    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        tryFind(ForStatement.class, forLoop);
    }

    /** Visits an if/else statement and records it when it matches the pattern. */
    @Override
    public void visitIfElse(final IfStatement ifElse) {
        super.visitIfElse(ifElse);
        tryFind(IfStatement.class, ifElse);
    }

    /** Visits a return statement and records it when it matches the pattern. */
    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        super.visitReturnStatement(statement);
        tryFind(ReturnStatement.class, statement);
    }

    /** Visits a switch statement and records it when it matches the pattern. */
    @Override
    public void visitSwitch(final SwitchStatement statement) {
        super.visitSwitch(statement);
        tryFind(SwitchStatement.class, statement);
    }

    /** Visits a synchronized statement and records it when it matches the pattern. */
    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);
        tryFind(SynchronizedStatement.class, statement);
    }

    /** Visits a throw statement and records it when it matches the pattern. */
    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        super.visitThrowStatement(statement);
        tryFind(ThrowStatement.class, statement);
    }

    /** Visits a try/catch/finally statement and records it when it matches the pattern. */
    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        super.visitTryCatchFinally(statement);
        tryFind(TryCatchStatement.class, statement);
    }

    /** Visits a while loop and records it when it matches the pattern. */
    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        super.visitWhileLoop(loop);
        tryFind(WhileStatement.class, loop);
    }

    /** Visits a block statement and records it when it matches the pattern. */
    @Override
    public void visitBlockStatement(final BlockStatement block) {
        super.visitBlockStatement(block);
        tryFind(BlockStatement.class, block);
    }

    /** Visits an empty statement and records it when it matches the pattern. */
    @Override
    public void visitEmptyStatement(final EmptyStatement statement) {
        super.visitEmptyStatement(statement);
        tryFind(EmptyStatement.class, statement);
    }

    /** Visits a method call expression and records it when it matches the pattern. */
    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);
        tryFind(MethodCallExpression.class, call);
    }

    /** Visits a static method call expression and records it when it matches the pattern. */
    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        super.visitStaticMethodCallExpression(call);
        tryFind(StaticMethodCallExpression.class, call);
    }

    /** Visits a constructor call expression and records it when it matches the pattern. */
    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        tryFind(ConstructorCallExpression.class, call);
    }

    /** Visits a binary expression and records it when it matches the pattern. */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        tryFind(BinaryExpression.class, expression);
    }

    /** Visits a ternary expression and records it when it matches the pattern. */
    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        super.visitTernaryExpression(expression);
        tryFind(TernaryExpression.class, expression);
    }

    /** Visits an Elvis expression and records it when it matches the pattern. */
    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
        super.visitShortTernaryExpression(expression);
        tryFind(ElvisOperatorExpression.class, expression);
    }

    /** Visits a postfix expression and records it when it matches the pattern. */
    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        super.visitPostfixExpression(expression);
        tryFind(PostfixExpression.class, expression);
    }

    /** Visits a prefix expression and records it when it matches the pattern. */
    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        super.visitPrefixExpression(expression);
        tryFind(PrefixExpression.class, expression);
    }

    /** Visits a boolean expression and records it when it matches the pattern. */
    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        super.visitBooleanExpression(expression);
        tryFind(BooleanExpression.class, expression);
    }

    /** Visits a not expression and records it when it matches the pattern. */
    @Override
    public void visitNotExpression(final NotExpression expression) {
        super.visitNotExpression(expression);
        tryFind(NotExpression.class, expression);
    }

    /** Visits a closure expression and records it when it matches the pattern. */
    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        super.visitClosureExpression(expression);
        tryFind(ClosureExpression.class, expression);
    }

    /** Visits a tuple expression and records it when it matches the pattern. */
    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        super.visitTupleExpression(expression);
        tryFind(TupleExpression.class, expression);
    }

    /** Visits a list expression and records it when it matches the pattern. */
    @Override
    public void visitListExpression(final ListExpression expression) {
        super.visitListExpression(expression);
        tryFind(ListExpression.class, expression);
    }

    /** Visits an array expression and records it when it matches the pattern. */
    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        super.visitArrayExpression(expression);
        tryFind(ArrayExpression.class, expression);
    }

    /** Visits a map expression and records it when it matches the pattern. */
    @Override
    public void visitMapExpression(final MapExpression expression) {
        super.visitMapExpression(expression);
        tryFind(MapExpression.class, expression);
    }

    /** Visits a map entry expression and records it when it matches the pattern. */
    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        super.visitMapEntryExpression(expression);
        tryFind(MapEntryExpression.class, expression);
    }

    /** Visits a range expression and records it when it matches the pattern. */
    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
        tryFind(RangeExpression.class, expression);
    }

    /** Visits a spread expression and records it when it matches the pattern. */
    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        super.visitSpreadExpression(expression);
        tryFind(SpreadExpression.class, expression);
    }

    /** Visits a spread-map expression and records it when it matches the pattern. */
    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
        tryFind(SpreadMapExpression.class, expression);
    }

    /** Visits a method-pointer expression and records it when it matches the pattern. */
    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        super.visitMethodPointerExpression(expression);
        tryFind(MethodPointerExpression.class, expression);
    }

    /** Visits a unary-minus expression and records it when it matches the pattern. */
    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
        tryFind(UnaryMinusExpression.class, expression);
    }

    /** Visits a unary-plus expression and records it when it matches the pattern. */
    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
        tryFind(UnaryPlusExpression.class, expression);
    }

    /** Visits a bitwise-negation expression and records it when it matches the pattern. */
    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        super.visitBitwiseNegationExpression(expression);
        tryFind(BitwiseNegationExpression.class, expression);
    }

    /** Visits a cast expression and records it when it matches the pattern. */
    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
        tryFind(CastExpression.class, expression);
    }

    /** Visits a constant expression and records it when it matches the pattern. */
    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        super.visitConstantExpression(expression);
        tryFind(ConstantExpression.class, expression);
    }

    /** Visits a class expression and records it when it matches the pattern. */
    @Override
    public void visitClassExpression(final ClassExpression expression) {
        super.visitClassExpression(expression);
        tryFind(ClassExpression.class, expression);
    }

    /** Visits a variable expression and records it when it matches the pattern. */
    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        super.visitVariableExpression(expression);
        tryFind(VariableExpression.class, expression);
    }

    /** Visits a property expression and records it when it matches the pattern. */
    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        super.visitPropertyExpression(expression);
        tryFind(PropertyExpression.class, expression);
    }

    /** Visits an attribute expression and records it when it matches the pattern. */
    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        super.visitAttributeExpression(expression);
        tryFind(AttributeExpression.class, expression);
    }

    /** Visits a field expression and records it when it matches the pattern. */
    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        super.visitFieldExpression(expression);
        tryFind(FieldExpression.class, expression);
    }

    /** Visits a GString expression and records it when it matches the pattern. */
    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        super.visitGStringExpression(expression);
        tryFind(GStringExpression.class, expression);
    }

    /** Visits an argument-list expression and records it when it matches the pattern. */
    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
        tryFind(ArgumentListExpression.class, ale);
    }

    /** Visits a closure-list expression and records it when it matches the pattern. */
    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        super.visitClosureListExpression(cle);
        tryFind(ClosureListExpression.class, cle);
    }

    /** Visits a bytecode expression and records it when it matches the pattern. */
    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        super.visitBytecodeExpression(cle);
        tryFind(BytecodeExpression.class, cle);
    }
}
