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

class ASTFinder extends ContextualClassCodeVisitor {

    private final ASTNode initial;
    private final List<TreeContext> matches = new LinkedList<TreeContext>();

    ASTFinder(final ASTNode initial) {
        this.initial = initial;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    public List<TreeContext> getMatches() {
        return matches;
    }

    private void tryFind(final Class<?> clazz, final ASTNode node) {
        if (clazz.isAssignableFrom(initial.getClass()) && ASTMatcher.matches(node, initial)) {
            matches.add(getLastContext());
        }
    }

    @Override
    public void visitClass(final ClassNode node) {
        super.visitClass(node);
        tryFind(ClassNode.class, node);
    }

    @Override
    public void visitPackage(final PackageNode node) {
        super.visitPackage(node);
        tryFind(PackageNode.class, node);
    }

    @Override
    public void visitImports(final ModuleNode node) {
        super.visitImports(node);
        tryFind(ModuleNode.class, node);
    }

    @Override
    public void visitDeclarationExpression(final DeclarationExpression expression) {
        super.visitDeclarationExpression(expression);
        tryFind(DeclarationExpression.class, expression);
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);
        tryFind(MethodNode.class, node);
    }

    @Override
    public void visitField(final FieldNode node) {
        super.visitField(node);
        tryFind(FieldNode.class, node);
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        super.visitProperty(node);
        tryFind(PropertyNode.class, node);
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        super.visitAssertStatement(statement);
        tryFind(AssertStatement.class, statement);
    }

    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        super.visitBreakStatement(statement);
        tryFind(BreakStatement.class, statement);
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        super.visitCaseStatement(statement);
        tryFind(CaseStatement.class, statement);
    }

    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        super.visitCatchStatement(statement);
        tryFind(CatchStatement.class, statement);
    }

    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        super.visitContinueStatement(statement);
        tryFind(ContinueStatement.class, statement);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        super.visitDoWhileLoop(loop);
        tryFind(DoWhileStatement.class, loop);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        tryFind(ExpressionStatement.class, statement);
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        super.visitForLoop(forLoop);
        tryFind(ForStatement.class, forLoop);
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        super.visitIfElse(ifElse);
        tryFind(IfStatement.class, ifElse);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        super.visitReturnStatement(statement);
        tryFind(ReturnStatement.class, statement);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        super.visitSwitch(statement);
        tryFind(SwitchStatement.class, statement);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);
        tryFind(SynchronizedStatement.class, statement);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        super.visitThrowStatement(statement);
        tryFind(ThrowStatement.class, statement);
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        super.visitTryCatchFinally(statement);
        tryFind(TryCatchStatement.class, statement);
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        super.visitWhileLoop(loop);
        tryFind(WhileStatement.class, loop);
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        super.visitBlockStatement(block);
        tryFind(BlockStatement.class, block);
    }

    @Override
    public void visitEmptyStatement(final EmptyStatement statement) {
        super.visitEmptyStatement(statement);
        tryFind(EmptyStatement.class, statement);
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);
        tryFind(MethodCallExpression.class, call);
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        super.visitStaticMethodCallExpression(call);
        tryFind(StaticMethodCallExpression.class, call);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        tryFind(ConstructorCallExpression.class, call);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        super.visitBinaryExpression(expression);
        tryFind(BinaryExpression.class, expression);
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        super.visitTernaryExpression(expression);
        tryFind(TernaryExpression.class, expression);
    }

    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
        super.visitShortTernaryExpression(expression);
        tryFind(ElvisOperatorExpression.class, expression);
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        super.visitPostfixExpression(expression);
        tryFind(PostfixExpression.class, expression);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        super.visitPrefixExpression(expression);
        tryFind(PrefixExpression.class, expression);
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        super.visitBooleanExpression(expression);
        tryFind(BooleanExpression.class, expression);
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        super.visitNotExpression(expression);
        tryFind(NotExpression.class, expression);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        super.visitClosureExpression(expression);
        tryFind(ClosureExpression.class, expression);
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        super.visitTupleExpression(expression);
        tryFind(TupleExpression.class, expression);
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        super.visitListExpression(expression);
        tryFind(ListExpression.class, expression);
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        super.visitArrayExpression(expression);
        tryFind(ArrayExpression.class, expression);
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        super.visitMapExpression(expression);
        tryFind(MapExpression.class, expression);
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        super.visitMapEntryExpression(expression);
        tryFind(MapEntryExpression.class, expression);
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        super.visitRangeExpression(expression);
        tryFind(RangeExpression.class, expression);
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        super.visitSpreadExpression(expression);
        tryFind(SpreadExpression.class, expression);
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        super.visitSpreadMapExpression(expression);
        tryFind(SpreadMapExpression.class, expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        super.visitMethodPointerExpression(expression);
        tryFind(MethodPointerExpression.class, expression);
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        super.visitUnaryMinusExpression(expression);
        tryFind(UnaryMinusExpression.class, expression);
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        super.visitUnaryPlusExpression(expression);
        tryFind(UnaryPlusExpression.class, expression);
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        super.visitBitwiseNegationExpression(expression);
        tryFind(BitwiseNegationExpression.class, expression);
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        super.visitCastExpression(expression);
        tryFind(CastExpression.class, expression);
    }

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        super.visitConstantExpression(expression);
        tryFind(ConstantExpression.class, expression);
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        super.visitClassExpression(expression);
        tryFind(ClassExpression.class, expression);
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        super.visitVariableExpression(expression);
        tryFind(VariableExpression.class, expression);
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        super.visitPropertyExpression(expression);
        tryFind(PropertyExpression.class, expression);
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        super.visitAttributeExpression(expression);
        tryFind(AttributeExpression.class, expression);
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        super.visitFieldExpression(expression);
        tryFind(FieldExpression.class, expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        super.visitGStringExpression(expression);
        tryFind(GStringExpression.class, expression);
    }

    @Override
    public void visitArgumentlistExpression(final ArgumentListExpression ale) {
        super.visitArgumentlistExpression(ale);
        tryFind(ArgumentListExpression.class, ale);
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        super.visitClosureListExpression(cle);
        tryFind(ClosureListExpression.class, cle);
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        super.visitBytecodeExpression(cle);
        tryFind(BytecodeExpression.class, cle);
    }
}
