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

    /**
     * Creates a visitor with an initial root context.
     */
    public ContextualClassCodeVisitor() {
        pushContext(new TreeContext(null, null));
    }

    /**
     * Returns the current traversal context.
     *
     * @return the current context, or {@code null}
     */
    public TreeContext getTreeContext() {
        return treeContextStack.isEmpty()?null:treeContextStack.peek();
    }

    /**
     * Returns the most recently popped traversal context.
     *
     * @return the last completed context
     */
    public TreeContext getLastContext() {
        return lastContext;
    }

    /**
     * Pushes an explicit traversal context.
     *
     * @param ctx the context to push
     */
    protected void pushContext(TreeContext ctx) {
        treeContextStack.push(ctx);
    }

    /**
     * Pops the current traversal context.
     *
     * @return the popped context
     */
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
                /**
                 * Returns no source unit for deferred replacement.
                 *
                 * @return {@code null}
                 */
                @Override
                protected SourceUnit getSourceUnit() {
                    return null;
                }

                /**
                 * Replaces the current expression when a replacement was registered.
                 *
                 * @param exp the expression to transform
                 * @return the replacement or the transformed expression
                 */
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
            //parentNode.visit(trn);
        }
        return treeContext;
    }

    /**
     * Pushes a child context for the supplied AST node.
     *
     * @param node the node to enter
     */
    protected void pushContext(ASTNode node) {
        pushContext(getTreeContext().fork(node));
    }


    // ----------------------- override visit methods to provide contextual information ---------------------------


    /** Visits a class node while tracking traversal context. */
    @Override
    public void visitClass(final ClassNode node) {
        pushContext(node);
        super.visitClass(node);
        popContext();
    }

    /** Visits a package node while tracking traversal context. */
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

    /** Visits module imports while tracking traversal context. */
    @Override
    public void visitImports(final ModuleNode node) {
        pushContext(node);
        super.visitImports(node);
        popContext();
    }

    /** Visits a constructor or method while tracking traversal context. */
    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        pushContext(node);
        super.visitConstructorOrMethod(node, isConstructor);
        popContext();
    }

    /** Visits a field node while tracking traversal context. */
    @Override
    public void visitField(final FieldNode node) {
        pushContext(node);
        super.visitField(node);
        popContext();
    }

    /** Visits a property node while tracking traversal context. */
    @Override
    public void visitProperty(final PropertyNode node) {
        pushContext(node);
        super.visitProperty(node);
        popContext();
    }

    /** Visits a method call expression while tracking traversal context. */
    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        pushContext(call);
        super.visitMethodCallExpression(call);
        popContext();
    }

    /** Visits a static method call expression while tracking traversal context. */
    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        pushContext(call);
        super.visitStaticMethodCallExpression(call);
        popContext();
    }

    /** Visits a constructor call expression while tracking traversal context. */
    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        pushContext(call);
        super.visitConstructorCallExpression(call);
        popContext();
    }

    /** Visits a binary expression while tracking traversal context. */
    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        pushContext(expression);
        super.visitBinaryExpression(expression);
        popContext();
    }

    /** Visits a ternary expression while tracking traversal context. */
    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        pushContext(expression);
        super.visitTernaryExpression(expression);
        popContext();
    }

    /** Visits an Elvis expression while tracking traversal context. */
    @Override
    public void visitShortTernaryExpression(final ElvisOperatorExpression expression) {
        pushContext(expression);
        super.visitShortTernaryExpression(expression);
        popContext();
    }

    /** Visits a postfix expression while tracking traversal context. */
    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        pushContext(expression);
        super.visitPostfixExpression(expression);
        popContext();
    }

    /** Visits a prefix expression while tracking traversal context. */
    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        pushContext(expression);
        super.visitPrefixExpression(expression);
        popContext();
    }

    /** Visits a boolean expression while tracking traversal context. */
    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        pushContext(expression);
        super.visitBooleanExpression(expression);
        popContext();
    }

    /** Visits a not expression while tracking traversal context. */
    @Override
    public void visitNotExpression(final NotExpression expression) {
        pushContext(expression);
        super.visitNotExpression(expression);
        popContext();
    }

    /** Visits a closure expression while tracking traversal context. */
    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        pushContext(expression);
        super.visitClosureExpression(expression);
        popContext();
    }

    /** Visits a tuple expression while tracking traversal context. */
    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        pushContext(expression);
        super.visitTupleExpression(expression);
        popContext();
    }

    /** Visits a list expression while tracking traversal context. */
    @Override
    public void visitListExpression(final ListExpression expression) {
        pushContext(expression);
        super.visitListExpression(expression);
        popContext();
    }

    /** Visits an array expression while tracking traversal context. */
    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        pushContext(expression);
        super.visitArrayExpression(expression);
        popContext();
    }

    /** Visits a map expression while tracking traversal context. */
    @Override
    public void visitMapExpression(final MapExpression expression) {
        pushContext(expression);
        super.visitMapExpression(expression);
        popContext();
    }

    /** Visits a map-entry expression while tracking traversal context. */
    @Override
    public void visitMapEntryExpression(final MapEntryExpression expression) {
        pushContext(expression);
        super.visitMapEntryExpression(expression);
        popContext();
    }

    /** Visits a range expression while tracking traversal context. */
    @Override
    public void visitRangeExpression(final RangeExpression expression) {
        pushContext(expression);
        super.visitRangeExpression(expression);
        popContext();
    }

    /** Visits a spread expression while tracking traversal context. */
    @Override
    public void visitSpreadExpression(final SpreadExpression expression) {
        pushContext(expression);
        super.visitSpreadExpression(expression);
        popContext();
    }

    /** Visits a spread-map expression while tracking traversal context. */
    @Override
    public void visitSpreadMapExpression(final SpreadMapExpression expression) {
        pushContext(expression);
        super.visitSpreadMapExpression(expression);
        popContext();
    }

    /** Visits a method-pointer expression while tracking traversal context. */
    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        pushContext(expression);
        super.visitMethodPointerExpression(expression);
        popContext();
    }

    /** Visits a unary-minus expression while tracking traversal context. */
    @Override
    public void visitUnaryMinusExpression(final UnaryMinusExpression expression) {
        pushContext(expression);
        super.visitUnaryMinusExpression(expression);
        popContext();
    }

    /** Visits a unary-plus expression while tracking traversal context. */
    @Override
    public void visitUnaryPlusExpression(final UnaryPlusExpression expression) {
        pushContext(expression);
        super.visitUnaryPlusExpression(expression);
        popContext();
    }

    /** Visits a bitwise-negation expression while tracking traversal context. */
    @Override
    public void visitBitwiseNegationExpression(final BitwiseNegationExpression expression) {
        pushContext(expression);
        super.visitBitwiseNegationExpression(expression);
        popContext();
    }

    /** Visits a cast expression while tracking traversal context. */
    @Override
    public void visitCastExpression(final CastExpression expression) {
        pushContext(expression);
        super.visitCastExpression(expression);
        popContext();
    }

    /** Visits a constant expression while tracking traversal context. */
    @Override
    public void visitConstantExpression(final ConstantExpression expression) {
        pushContext(expression);
        super.visitConstantExpression(expression);
        popContext();
    }

    /** Visits a class expression while tracking traversal context. */
    @Override
    public void visitClassExpression(final ClassExpression expression) {
        pushContext(expression);
        super.visitClassExpression(expression);
        popContext();
    }

    /** Visits a variable expression while tracking traversal context. */
    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        pushContext(expression);
        super.visitVariableExpression(expression);
        popContext();
    }

    /** Visits a property expression while tracking traversal context. */
    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        pushContext(expression);
        super.visitPropertyExpression(expression);
        popContext();
    }

    /** Visits an attribute expression while tracking traversal context. */
    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        pushContext(expression);
        super.visitAttributeExpression(expression);
        popContext();
    }

    /** Visits a field expression while tracking traversal context. */
    @Override
    public void visitFieldExpression(final FieldExpression expression) {
        pushContext(expression);
        super.visitFieldExpression(expression);
        popContext();
    }

    /** Visits a GString expression while tracking traversal context. */
    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        pushContext(expression);
        super.visitGStringExpression(expression);
        popContext();
    }

    /** Visits a closure-list expression while tracking traversal context. */
    @Override
    public void visitClosureListExpression(final ClosureListExpression cle) {
        pushContext(cle);
        super.visitClosureListExpression(cle);
        popContext();
    }

    /** Visits a bytecode expression while tracking traversal context. */
    @Override
    public void visitBytecodeExpression(final BytecodeExpression cle) {
        pushContext(cle);
        super.visitBytecodeExpression(cle);
        popContext();
    }

    /** Visits an assert statement while tracking traversal context. */
    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        pushContext(statement);
        super.visitAssertStatement(statement);
        popContext();
    }

    /** Visits a block statement while tracking traversal context. */
    @Override
    public void visitBlockStatement(final BlockStatement block) {
        pushContext(block);
        super.visitBlockStatement(block);
        popContext();
    }

    /** Visits a break statement while tracking traversal context. */
    @Override
    public void visitBreakStatement(final BreakStatement statement) {
        pushContext(statement);
        super.visitBreakStatement(statement);
        popContext();
    }

    /** Visits a case statement while tracking traversal context. */
    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        pushContext(statement);
        super.visitCaseStatement(statement);
        popContext();
    }

    /** Visits a catch statement while tracking traversal context. */
    @Override
    public void visitCatchStatement(final CatchStatement statement) {
        pushContext(statement);
        super.visitCatchStatement(statement);
        popContext();
    }

    /** Visits a continue statement while tracking traversal context. */
    @Override
    public void visitContinueStatement(final ContinueStatement statement) {
        pushContext(statement);
        super.visitContinueStatement(statement);
        popContext();
    }

    /** Visits a do-while loop while tracking traversal context. */
    @Override
    public void visitDoWhileLoop(final DoWhileStatement loop) {
        pushContext(loop);
        super.visitDoWhileLoop(loop);
        popContext();
    }

    /** Visits an expression statement while tracking traversal context. */
    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        pushContext(statement);
        super.visitExpressionStatement(statement);
        popContext();
    }

    /** Visits a for loop while tracking traversal context. */
    @Override
    public void visitForLoop(final ForStatement forLoop) {
        pushContext(forLoop);
        super.visitForLoop(forLoop);
        popContext();
    }

    /** Visits an if/else statement while tracking traversal context. */
    @Override
    public void visitIfElse(final IfStatement ifElse) {
        pushContext(ifElse);
        super.visitIfElse(ifElse);
        popContext();
    }

    /** Visits a return statement while tracking traversal context. */
    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        pushContext(statement);
        super.visitReturnStatement(statement);
        popContext();
    }

    /** Visits a switch statement while tracking traversal context. */
    @Override
    public void visitSwitch(final SwitchStatement statement) {
        pushContext(statement);
        super.visitSwitch(statement);
        popContext();
    }

    /** Visits a synchronized statement while tracking traversal context. */
    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        pushContext(statement);
        super.visitSynchronizedStatement(statement);
        popContext();
    }

    /** Visits a throw statement while tracking traversal context. */
    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        pushContext(statement);
        super.visitThrowStatement(statement);
        popContext();
    }

    /** Visits a try/catch/finally statement while tracking traversal context. */
    @Override
    public void visitTryCatchFinally(final TryCatchStatement statement) {
        pushContext(statement);
        super.visitTryCatchFinally(statement);
        popContext();
    }

    /** Visits a while loop while tracking traversal context. */
    @Override
    public void visitWhileLoop(final WhileStatement loop) {
        pushContext(loop);
        super.visitWhileLoop(loop);
        popContext();
    }

    /** Visits an empty statement while tracking traversal context. */
    @Override
    public void visitEmptyStatement(final EmptyStatement statement) {
        pushContext(statement);
        super.visitEmptyStatement(statement);
        popContext();
    }

    /**
     * Returns the current traversal path, starting with the last completed context.
     *
     * @return the current tree path
     */
    public List<TreeContext> getTreePath() {
        List<TreeContext> path = new LinkedList<TreeContext>();
        path.add(lastContext);
        path.addAll(treeContextStack);
        return path;
    }

    /**
     * Returns the current path when all supplied predicates match successive parents.
     *
     * @param predicates the predicates to match against the path
     * @return the matching path, or an empty list
     */
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

    /**
     * Returns the path up to the first node matching the supplied predicate.
     *
     * @param predicate the predicate that terminates the path
     * @return the matching path, or an empty list
     */
    public List<TreeContext> pathUpTo(ASTNodePredicate predicate) {
        return pathUpTo(null, predicate);
    }

    /**
     * Returns the path up to the first node whose class matches {@code node}.
     *
     * @param node the node class that terminates the path
     * @return the matching path, or an empty list
     */
    public List<TreeContext> pathUpTo(Class<ASTNode> node) {
        return pathUpTo(node, null);
    }

    /**
     * Returns the path up to the first node that matches the supplied class and predicate.
     *
     * @param node the node class that terminates the path, or {@code null}
     * @param predicate the predicate that terminates the path, or {@code null}
     * @return the matching path, or an empty list
     */
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

    /**
     * Creates class-based predicates for the supplied AST node classes.
     *
     * @param classes the classes to match
     * @return the corresponding predicates
     */
    @SuppressWarnings("unchecked")
    public static List<ASTNodePredicate> matchByClass(Class<ASTNode>... classes) {
        List<ASTNodePredicate> result = new ArrayList<>(classes.length);
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

        /**
         * Checks whether the node class exactly matches the configured class.
         *
         * @param node the node to test
         * @return {@code true} when the node class matches
         */
        @Override
        public boolean matches(final ASTNode node) {
            return astNodeClass ==node.getClass();
        }
    }
}
