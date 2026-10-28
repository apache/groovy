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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BitwiseNegationExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.ElvisOperatorExpression;
import org.codehaus.groovy.ast.expr.EmptyExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.GStringExpression;
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
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.UnaryMinusExpression;
import org.codehaus.groovy.ast.expr.UnaryPlusExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.ast.stmt.YieldStatement;
import org.codehaus.groovy.classgen.BytecodeExpression;

import java.util.List;

/**
 * Gives each use of {@link VariableExpression#THIS_EXPRESSION} and
 * {@link VariableExpression#SUPER_EXPRESSION} its own node.
 *
 * <p>Those two fields are process-wide singletons. Static type checking stores
 * inferred types on the expression it is visiting, and one shared node cannot
 * hold a type for every use. Leaving the singleton untyped makes static
 * compilation fall back to the class currently being generated, which inside a
 * closure is the closure class rather than the enclosing instance.
 *
 * <p>The walk mutates the tree in place and allocates a node only when a
 * sentinel is actually present. Nodes that do not contain one keep their
 * identity, so metadata already recorded on them is preserved.
 */
final class SharedVariableExpressionRewriter extends CodeVisitorSupport {

    static void rewrite(final MethodNode node) {
        if (node == null) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        for (Parameter parameter : node.getParameters()) {
            if (parameter != null && parameter.hasInitialExpression()) {
                parameter.setInitialExpression(rewriter.rewrite(parameter.getInitialExpression()));
            }
        }
        if (node.getCode() != null) node.getCode().visit(rewriter);
    }

    static void rewrite(final FieldNode node) {
        if (node == null) return;
        Expression init = node.getInitialExpression();
        if (init == null) return;
        Expression rewritten = new SharedVariableExpressionRewriter().rewrite(init);
        if (rewritten != init) node.setInitialValueExpression(rewritten);
    }

    static void rewrite(final PropertyNode node) {
        if (node == null) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        if (node.getGetterBlock() != null) node.getGetterBlock().visit(rewriter);
        if (node.getSetterBlock() != null) node.getSetterBlock().visit(rewriter);
    }

    static void rewriteInitializers(final ClassNode node) {
        if (node == null) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        for (Statement statement : node.getObjectInitializerStatements()) {
            if (statement != null) statement.visit(rewriter);
        }
    }

    private SharedVariableExpressionRewriter() {
    }

    private Expression rewrite(final Expression expr) {
        if (expr == null || expr instanceof ConstantExpression || expr instanceof ClassExpression
                || expr instanceof FieldExpression || expr instanceof EmptyExpression
                || expr instanceof BytecodeExpression) {
            return expr;
        }
        if (expr instanceof VariableExpression variable) {
            if (variable == VariableExpression.THIS_EXPRESSION || variable == VariableExpression.SUPER_EXPRESSION) {
                return copyShared(variable);
            }
            return variable;
        }
        if (expr instanceof ClosureExpression closure) {
            if (closure.isParameterSpecified()) {
                for (Parameter parameter : closure.getParameters()) {
                    if (parameter != null && parameter.hasInitialExpression()) {
                        parameter.setInitialExpression(rewrite(parameter.getInitialExpression()));
                    }
                }
            }
            if (closure.getCode() != null) closure.getCode().visit(this);
            return closure;
        }
        if (expr instanceof MethodCallExpression call) {
            Expression object = rewrite(call.getObjectExpression());
            if (object != call.getObjectExpression()) call.setObjectExpression(object);
            Expression method = rewrite(call.getMethod());
            if (method != call.getMethod()) call.setMethod(method);
            rewrite(call.getArguments());
            return call;
        }
        if (expr instanceof ConstructorCallExpression call) {
            rewrite(call.getArguments());
            return call;
        }
        if (expr instanceof StaticMethodCallExpression call) {
            rewrite(call.getArguments());
            return call;
        }
        if (expr instanceof MethodPointerExpression pointer) {
            // A null receiver is reported as the this sentinel. That is not a
            // stored use, and replacing it would turn "&m" / "::m" into "this.&m".
            Expression reported = pointer.getExpression();
            String text = pointer.getText();
            boolean implicitReceiver = reported == VariableExpression.THIS_EXPRESSION
                    && (text.startsWith("&") || text.startsWith("::"));
            Expression receiver = implicitReceiver ? null : rewrite(reported);
            Expression name = rewrite(pointer.getMethodName());
            if ((implicitReceiver || receiver == reported) && name == pointer.getMethodName()) return pointer;
            MethodPointerExpression copy = pointer instanceof MethodReferenceExpression
                    ? new MethodReferenceExpression(implicitReceiver ? null : receiver, name)
                    : new MethodPointerExpression(implicitReceiver ? null : receiver, name);
            adopt(copy, pointer);
            return copy;
        }
        if (expr instanceof BinaryExpression binary) {
            Expression left = rewrite(binary.getLeftExpression());
            if (left != binary.getLeftExpression()) binary.setLeftExpression(left);
            Expression right = rewrite(binary.getRightExpression());
            if (right != binary.getRightExpression()) binary.setRightExpression(right);
            return binary;
        }
        if (expr instanceof PropertyExpression property) {
            Expression object = rewrite(property.getObjectExpression());
            Expression name = rewrite(property.getProperty());
            if (name != property.getProperty()) {
                PropertyExpression copy = property instanceof AttributeExpression
                        ? new AttributeExpression(object, name, property.isSafe())
                        : new PropertyExpression(object, name, property.isSafe());
                copy.setImplicitThis(property.isImplicitThis());
                copy.setSpreadSafe(property.isSpreadSafe());
                copy.setStatic(property.isStatic());
                adopt(copy, property);
                return copy;
            }
            if (object != property.getObjectExpression()) property.setObjectExpression(object);
            return property;
        }
        if (expr instanceof ElvisOperatorExpression elvis) {
            Expression trueExpression = rewrite(elvis.getTrueExpression());
            Expression falseExpression = rewrite(elvis.getFalseExpression());
            if (trueExpression == elvis.getTrueExpression() && falseExpression == elvis.getFalseExpression()) return elvis;
            Expression copy = new ElvisOperatorExpression(trueExpression, falseExpression);
            adopt(copy, elvis);
            return copy;
        }
        if (expr instanceof TernaryExpression ternary) {
            Expression condition = rewrite(ternary.getBooleanExpression());
            Expression trueExpression = rewrite(ternary.getTrueExpression());
            Expression falseExpression = rewrite(ternary.getFalseExpression());
            if (condition == ternary.getBooleanExpression()
                    && trueExpression == ternary.getTrueExpression()
                    && falseExpression == ternary.getFalseExpression()) return ternary;
            Expression copy = new TernaryExpression((BooleanExpression) condition, trueExpression, falseExpression);
            adopt(copy, ternary);
            return copy;
        }
        if (expr instanceof NotExpression not) {
            return replaceUnary(not, not.getExpression(), inner -> new NotExpression(inner));
        }
        if (expr instanceof BooleanExpression bool) {
            return replaceUnary(bool, bool.getExpression(), BooleanExpression::new);
        }
        if (expr instanceof CastExpression cast) {
            Expression inner = rewrite(cast.getExpression());
            if (inner != cast.getExpression()) cast.setExpression(inner);
            return cast;
        }
        if (expr instanceof PrefixExpression prefix) {
            Expression inner = rewrite(prefix.getExpression());
            if (inner != prefix.getExpression()) prefix.setExpression(inner);
            return prefix;
        }
        if (expr instanceof PostfixExpression postfix) {
            Expression inner = rewrite(postfix.getExpression());
            if (inner != postfix.getExpression()) postfix.setExpression(inner);
            return postfix;
        }
        if (expr instanceof UnaryMinusExpression unary) {
            return replaceUnary(unary, unary.getExpression(), UnaryMinusExpression::new);
        }
        if (expr instanceof UnaryPlusExpression unary) {
            return replaceUnary(unary, unary.getExpression(), UnaryPlusExpression::new);
        }
        if (expr instanceof BitwiseNegationExpression unary) {
            return replaceUnary(unary, unary.getExpression(), BitwiseNegationExpression::new);
        }
        if (expr instanceof SpreadExpression spread) {
            return replaceUnary(spread, spread.getExpression(), SpreadExpression::new);
        }
        if (expr instanceof SpreadMapExpression spread) {
            return replaceUnary(spread, spread.getExpression(), SpreadMapExpression::new);
        }
        if (expr instanceof RangeExpression range) {
            Expression from = rewrite(range.getFrom());
            Expression to = rewrite(range.getTo());
            if (from == range.getFrom() && to == range.getTo()) return range;
            Expression copy = new RangeExpression(from, to, range.isExclusiveLeft(), range.isExclusiveRight());
            adopt(copy, range);
            return copy;
        }
        if (expr instanceof SwitchExpression sw) {
            sw.setExpression(rewrite(sw.getExpression()));
            for (CaseStatement caseStatement : sw.getCaseStatements()) {
                caseStatement.visit(this);
            }
            if (sw.getDefaultStatement() != null) sw.getDefaultStatement().visit(this);
            return sw;
        }
        if (expr instanceof GStringExpression gstring) {
            rewriteList(gstring.getValues());
            return gstring;
        }
        if (expr instanceof TupleExpression tuple) {
            rewriteList(tuple.getExpressions());
            return tuple;
        }
        if (expr instanceof MapExpression map) {
            for (MapEntryExpression entry : map.getMapEntryExpressions()) {
                if (entry == null) continue;
                Expression key = rewrite(entry.getKeyExpression());
                if (key != entry.getKeyExpression()) entry.setKeyExpression(key);
                Expression value = rewrite(entry.getValueExpression());
                if (value != entry.getValueExpression()) entry.setValueExpression(value);
            }
            return map;
        }
        if (expr instanceof ListExpression list) {
            rewriteList(list.getExpressions());
            return list;
        }
        if (expr instanceof ArrayExpression array) {
            rewriteList(array.getExpressions());
            rewriteList(array.getSizeExpression());
            return array;
        }
        return expr;
    }

    private Expression replaceUnary(final Expression original, final Expression inner, final UnaryFactory factory) {
        Expression rewritten = rewrite(inner);
        if (rewritten == inner) return original;
        Expression copy = factory.create(rewritten);
        adopt(copy, original);
        return copy;
    }

    private void rewriteList(final List<Expression> list) {
        if (list == null || list.isEmpty()) return;
        for (int i = 0, n = list.size(); i < n; i++) {
            Expression original = list.get(i);
            if (original == null) continue;
            Expression rewritten = rewrite(original);
            if (rewritten != original) list.set(i, rewritten);
        }
    }

    private static VariableExpression copyShared(final VariableExpression shared) {
        VariableExpression copy = new VariableExpression(shared.getName());
        copy.setSourcePosition(shared);
        copy.setSynthetic(shared.isSynthetic());
        return copy;
    }

    private static void adopt(final Expression target, final Expression source) {
        target.setSourcePosition(source);
        target.copyNodeMetaData(source);
        target.setSynthetic(source.isSynthetic());
        target.setType(source.getType());
    }

    @FunctionalInterface
    private interface UnaryFactory {
        Expression create(Expression inner);
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
    }

    @Override
    public void visitReturnStatement(final ReturnStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
    }

    @Override
    public void visitThrowStatement(final ThrowStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
    }

    @Override
    public void visitYieldStatement(final YieldStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
    }

    @Override
    public void visitAssertStatement(final AssertStatement statement) {
        statement.setBooleanExpression((BooleanExpression) rewrite(statement.getBooleanExpression()));
        if (statement.getMessageExpression() != null) {
            statement.setMessageExpression(rewrite(statement.getMessageExpression()));
        }
    }

    @Override
    public void visitIfElse(final IfStatement statement) {
        statement.setBooleanExpression((BooleanExpression) rewrite(statement.getBooleanExpression()));
        statement.getIfBlock().visit(this);
        statement.getElseBlock().visit(this);
    }

    @Override
    public void visitWhileLoop(final WhileStatement statement) {
        statement.setBooleanExpression((BooleanExpression) rewrite(statement.getBooleanExpression()));
        statement.getLoopBlock().visit(this);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement statement) {
        statement.getLoopBlock().visit(this);
        statement.setBooleanExpression((BooleanExpression) rewrite(statement.getBooleanExpression()));
    }

    @Override
    public void visitForLoop(final ForStatement statement) {
        statement.setCollectionExpression(rewrite(statement.getCollectionExpression()));
        statement.getLoopBlock().visit(this);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
        statement.getCode().visit(this);
    }

    @Override
    public void visitSwitch(final SwitchStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
        for (CaseStatement caseStatement : statement.getCaseStatements()) {
            caseStatement.visit(this);
        }
        statement.getDefaultStatement().visit(this);
    }

    @Override
    public void visitCaseStatement(final CaseStatement statement) {
        statement.setExpression(rewrite(statement.getExpression()));
        statement.getCode().visit(this);
    }

    // Expression.visit (for example from a bytecode sequence) cannot replace a
    // node whose child field is final. In-place containers are rewritten here;
    // statement visitors above install the replacement when a new node is required.

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        rewrite(call);
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        rewrite(call);
    }

    @Override
    public void visitStaticMethodCallExpression(final StaticMethodCallExpression call) {
        rewrite(call);
    }

    @Override
    public void visitBinaryExpression(final BinaryExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitTernaryExpression(final TernaryExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitPropertyExpression(final PropertyExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitAttributeExpression(final AttributeExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitCastExpression(final CastExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitTupleExpression(final TupleExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitListExpression(final ListExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitArrayExpression(final ArrayExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitMapExpression(final MapExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitGStringExpression(final GStringExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitBooleanExpression(final BooleanExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitNotExpression(final NotExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitPrefixExpression(final PrefixExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitPostfixExpression(final PostfixExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitMethodPointerExpression(final MethodPointerExpression expression) {
        rewrite(expression);
    }

    @Override
    public void visitSwitchExpression(final SwitchExpression expression) {
        rewrite(expression);
    }
}
