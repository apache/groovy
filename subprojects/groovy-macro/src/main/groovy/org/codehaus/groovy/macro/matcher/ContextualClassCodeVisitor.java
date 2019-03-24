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
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.PropertyNode;
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
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.Expression;
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A class code visitor which is capable of remembering the context of the current
 * visit. This makes it easier for subclasses to perform context-dependent transformations,
 * where for example it is necessary to check the parent nodes of an AST node before
 * performing some operations.
 *
 * @since 2.5.0
 */
public abstract class ContextualClassCodeVisitor extends ClassCodeVisitorSupport {
    private final Deque<TreeContext> treeContextStack = new ArrayDeque<TreeContext>();
    private TreeContext lastContext;

    public ContextualClassCodeVisitor() {
        pushContext(new TreeContext(null, null));
    }

    public TreeContext getTreeContext() {
        return treeContextStack.isEmpty()?null:treeContextStack.peek();
    }

    public TreeContext getLastContext() {
        return lastContext;
    }

    protected void pushContext(TreeContext ctx) {
        treeContextStack.push(ctx);
    }

    protected TreeContext popContext() {
        final TreeContext treeContext = treeContextStack.pop();
        List<TreeContextAction> actions = treeContext.getOnPopHandlers();
        for (TreeContextAction contextAction : actions) {
            contextAction.call(treeContext);
        }
        lastContext = treeContext;
        ASTNode parentNode = treeContext.parent!=null?treeContext.parent.node:null;
        if (treeContext.node instanceof Expression && parentNode !=null) {
            ClassCodeExpressionTransformer trn = new ClassCodeExpressionTransformer() {
                @Override
                protected SourceUnit getSourceUnit() {
                    return null;
                }

                @Override
                public Expression transform(final Expression exp) {
                    if (exp==treeContext.node) {
                        Expression replacement = treeContext.getReplacement();
                        if (replacement!=null) {
                            return replacement;
                        }
                    }
                    return super.transform(exp);
                }
            };
            // todo: reliable way to call the transformer
            //parentNode.accept(trn);
        }
        return treeContext;
    }

    protected void pushContext(ASTNode node) {
        pushContext(getTreeContext().fork(node));
    }


    // ----------------------- override visit methods to provide contextual information ---------------------------


    @Override
    public void visitClass(final ClassNode node) {
        pushContext(node);
        super.visitClass(node);
        popContext();
    }

    @Override
    public void visitPackage(final PackageNode node) {
        if (node!=null) {
            pushContext(node);
        }
        super.visitPackage(node);
        if (node!=null) {
            popContext();
        }
    }

    @Override
    public void visitImports(final ModuleNode node) {
        pushContext(node);
        super.visitImports(node);
        popContext();
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        pushContext(node);
        super.visitConstructorOrMethod(node, isConstructor);
        popContext();
    }

    @Override
    public void visitField(final FieldNode node) {
        pushContext(node);
        super.visitField(node);
        popContext();
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        pushContext(node);
        super.visitProperty(node);
        popContext();
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        pushContext(call);
        super.visitMethodCallExpression(call);
        popContext();
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        pushContext(call);
        super.visitStaticMethodCallExpression(call);
        popContext();
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        pushContext(call);
        super.visitConstructorCallExpression(call);
        popContext();
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        pushContext(expression);
        super.visitBinaryExpression(expression);
        popContext();
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        pushContext(expression);
        super.visitTernaryExpression(expression);
        popContext();
    }

    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
        pushContext(expression);
        super.visitShortTernaryExpression(expression);
        popContext();
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        pushContext(expression);
        super.visitPostfixExpression(expression);
        popContext();
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        pushContext(expression);
        super.visitPrefixExpression(expression);
        popContext();
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        pushContext(expression);
        super.visitBooleanExpression(expression);
        popContext();
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        pushContext(expression);
        super.visitNotExpression(expression);
        popContext();
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        pushContext(expression);
        super.visitClosureExpression(expression);
        popContext();
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        pushContext(expression);
        super.visitTupleExpression(expression);
        popContext();
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        pushContext(expression);
        super.visitListExpression(expression);
        popContext();
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        pushContext(expression);
        super.visitArrayExpression(expression);
        popContext();
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        pushContext(expression);
        super.visitMapExpression(expression);
        popContext();
    }

    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        pushContext(expression);
        super.visitMapEntryExpression(expression);
        popContext();
    }

    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        pushContext(expression);
        super.visitRangeExpression(expression);
        popContext();
    }

    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        pushContext(expression);
        super.visitSpreadExpression(expression);
        popContext();
    }

    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        pushContext(expression);
        super.visitSpreadMapExpression(expression);
        popContext();
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        pushContext(expression);
        super.visitMethodPointerExpression(expression);
        popContext();
    }

    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        pushContext(expression);
        super.visitUnaryMinusExpression(expression);
        popContext();
    }

    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        pushContext(expression);
        super.visitUnaryPlusExpression(expression);
        popContext();
    }

    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        pushContext(expression);
        super.visitBitwiseNegationExpression(expression);
        popContext();
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        pushContext(expression);
        super.visitCastExpression(expression);
        popContext();
    }

    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        pushContext(expression);
        super.visitConstantExpression(expression);
        popContext();
    }

    @Override
    public void visitClassExpression(final ClassExpression expression) {
        pushContext(expression);
        super.visitClassExpression(expression);
        popContext();
    }

    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        pushContext(expression);
        super.visitVariableExpression(expression);
        popContext();
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        pushContext(expression);
        super.visitPropertyExpression(expression);
        popContext();
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        pushContext(expression);
        super.visitAttributeExpression(expression);
        popContext();
    }

    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        pushContext(expression);
        super.visitFieldExpression(expression);
        popContext();
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        pushContext(expression);
        super.visitGStringExpression(expression);
        popContext();
    }

    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        pushContext(cle);
        super.visitClosureListExpression(cle);
        popContext();
    }

    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        pushContext(cle);
        super.visitBytecodeExpression(cle);
        popContext();
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        pushContext(statement);
        super.visitAssertStatement(statement);
        popContext();
    }

    @Override
    public void visitBlockStatement(final BlockStatement block) {
        pushContext(block);
        super.visitBlockStatement(block);
        popContext();
    }

    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        pushContext(statement);
        super.visitBreakStatement(statement);
        popContext();
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        pushContext(statement);
        super.visitCaseStatement(statement);
        popContext();
    }

    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        pushContext(statement);
        super.visitCatchStatement(statement);
        popContext();
    }

    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        pushContext(statement);
        super.visitContinueStatement(statement);
        popContext();
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        pushContext(loop);
        super.visitDoWhileLoop(loop);
        popContext();
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        pushContext(statement);
        super.visitExpressionStatement(statement);
        popContext();
    }

    @Override
    public void visitForLoop(final ForStatement forLoop) {
        pushContext(forLoop);
        super.visitForLoop(forLoop);
        popContext();
    }

    @Override
    public void visitIfElse(final IfStatement ifElse) {
        pushContext(ifElse);
        super.visitIfElse(ifElse);
        popContext();
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        pushContext(statement);
        super.visitReturnStatement(statement);
        popContext();
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        pushContext(statement);
        super.visitSwitch(statement);
        popContext();
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        pushContext(statement);
        super.visitSynchronizedStatement(statement);
        popContext();
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        pushContext(statement);
        super.visitThrowStatement(statement);
        popContext();
    }

    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        pushContext(statement);
        super.visitTryCatchFinally(statement);
        popContext();
    }

    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        pushContext(loop);
        super.visitWhileLoop(loop);
        popContext();
    }

    @Override
    protected void visitEmptyStatement(final EmptyStatement statement) {
        pushContext(statement);
        super.visitEmptyStatement(statement);
        popContext();
    }

    public List<TreeContext> getTreePath() {
        List<TreeContext> path = new LinkedList<TreeContext>();
        path.add(lastContext);
        path.addAll(treeContextStack);
        return path;
    }

    public List<TreeContext> pathMatches(List<ASTNodePredicate> predicates) {
        List<TreeContext> path = new LinkedList<TreeContext>();
        TreeContext current = lastContext.parent;
        for (ASTNodePredicate predicate : predicates) {
            path.add(current);
            if (current==null || !predicate.matches(current.node)) {
                return Collections.emptyList();
            }
            current = current.parent;
        }
        if (!path.isEmpty()) {
            path.add(0, lastContext);
        }
        return path;
    }

    public List<TreeContext> pathUpTo(ASTNodePredicate predicate) {
        return pathUpTo(null, predicate);
    }

    public List<TreeContext> pathUpTo(Class<ASTNode> node) {
        return pathUpTo(node, null);
    }

    public List<TreeContext> pathUpTo(Class<ASTNode> node, ASTNodePredicate predicate) {
        List<TreeContext> path = new LinkedList<TreeContext>();
        TreeContext current = lastContext;
        boolean found = false;
        while (current!=null && !found) {
            path.add(current);
            ASTNode currentNode = current.node;
            if (node==null) {
                if (predicate.matches(currentNode)) {
                    found = true;
                }
            } else {
                if (predicate==null) {
                    if (currentNode==null || node==currentNode.getClass()) {
                        found = true;
                    }
                } else {
                    found = currentNode!=null && node==currentNode.getClass() && predicate.matches(currentNode);
                }
            }

            current = current.parent;
        }
        if (found) {
            return path;
        }
        return Collections.emptyList();
    }

    // ----------------------------- inner classes --------------------------------------

    public static List<ASTNodePredicate> matchByClass(Class<ASTNode>... classes) {
        ArrayList<ASTNodePredicate> result = new ArrayList<ASTNodePredicate>(classes.length);
        for (final Class<ASTNode> astNodeClass : classes) {
            result.add(new MatchByClass(astNodeClass));
        }
        return result;
    }

    private static class MatchByClass implements ASTNodePredicate {
        private final Class<ASTNode> astNodeClass;

        MatchByClass(final Class<ASTNode> astNodeClass) {
            this.astNodeClass = astNodeClass;
        }

        @Override
        public boolean matches(final ASTNode node) {
            return astNodeClass ==node.getClass();
        }
    }
}
