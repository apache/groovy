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

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ClosureListExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodPointerExpression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

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
 * <p>A member with no sentinel is left as it is, so its nodes and metadata stay
 * put. A member that has one is rewritten by {@link ClassCodeExpressionTransformer}.
 * Composite expressions are rebuilt, and source position and node metadata are
 * copied the same way {@code StaticCompilationTransformer} already expects.
 */
final class SharedVariableExpressionRewriter extends ClassCodeExpressionTransformer {

    static void rewrite(final MethodNode node) {
        if (node == null || !containsShared(node)) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        for (Parameter parameter : node.getParameters()) {
            if (parameter != null && parameter.hasInitialExpression()) {
                parameter.setInitialExpression(rewriter.transform(parameter.getInitialExpression()));
            }
        }
        if (node.getCode() != null) node.getCode().visit(rewriter);
    }

    static void rewrite(final FieldNode node) {
        if (node == null) return;
        Expression init = node.getInitialExpression();
        if (init == null || !containsShared(init)) return;
        node.setInitialValueExpression(new SharedVariableExpressionRewriter().transform(init));
    }

    static void rewrite(final PropertyNode node) {
        if (node == null) return;
        Statement getter = node.getGetterBlock();
        Statement setter = node.getSetterBlock();
        if (!containsShared(getter) && !containsShared(setter)) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        if (getter != null) getter.visit(rewriter);
        if (setter != null) setter.visit(rewriter);
    }

    static void rewriteInitializers(final ClassNode node) {
        if (node == null) return;
        SharedSentinelFinder finder = new SharedSentinelFinder();
        for (Statement statement : node.getObjectInitializerStatements()) {
            if (statement != null) statement.visit(finder);
        }
        if (!finder.found) return;
        SharedVariableExpressionRewriter rewriter = new SharedVariableExpressionRewriter();
        for (Statement statement : node.getObjectInitializerStatements()) {
            if (statement != null) statement.visit(rewriter);
        }
    }

    private SharedVariableExpressionRewriter() {
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null;
    }

    @Override
    public Expression transform(final Expression expr) {
        if (expr == VariableExpression.THIS_EXPRESSION || expr == VariableExpression.SUPER_EXPRESSION) {
            return copyShared((VariableExpression) expr);
        }
        // Closure and switch bodies are statements. transformExpression does not walk them.
        if (expr instanceof ClosureExpression || expr instanceof SwitchExpression) {
            expr.visit(this);
            return expr;
        }
        if (expr instanceof ClosureListExpression list) {
            // Classic for-loops keep their variables in this scope. The stock transform drops it.
            ClosureListExpression transformed = (ClosureListExpression) super.transform(expr);
            transformed.setVariableScope(list.getVariableScope());
            return transformed;
        }
        Expression transformed = super.transform(expr);
        if (transformed instanceof MethodPointerExpression rewritten
                && rewritten.getExpression() == VariableExpression.THIS_EXPRESSION
                && expr instanceof MethodPointerExpression pointer) {
            // A null receiver is not a stored use. transformExpression installs the shared
            // this sentinel without calling transform(); put the null receiver back so an
            // implicit pointer or reference stays implicit.
            return implicitMethodPointer(pointer, rewritten.getMethodName());
        }
        return transformed;
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expr) {
        if (expr.isParameterSpecified()) {
            for (Parameter parameter : expr.getParameters()) {
                if (parameter != null && parameter.hasInitialExpression()) {
                    parameter.setInitialExpression(transform(parameter.getInitialExpression()));
                }
            }
        }
        Statement code = expr.getCode();
        if (code != null) code.visit(this);
    }

    @Override
    public void visitSwitchExpression(final SwitchExpression expression) {
        expression.setExpression(transform(expression.getExpression()));
        for (CaseStatement caseStatement : expression.getCaseStatements()) {
            if (caseStatement != null) caseStatement.visit(this);
        }
        Statement defaultStatement = expression.getDefaultStatement();
        if (defaultStatement != null) defaultStatement.visit(this);
    }

    private static MethodPointerExpression implicitMethodPointer(final MethodPointerExpression original, final Expression name) {
        MethodPointerExpression copy = original instanceof MethodReferenceExpression
                ? new MethodReferenceExpression(null, name)
                : new MethodPointerExpression(null, name);
        copy.setSourcePosition(original);
        copy.copyNodeMetaData(original);
        copy.setSynthetic(original.isSynthetic());
        return copy;
    }

    private static VariableExpression copyShared(final VariableExpression shared) {
        VariableExpression copy = varX(shared.getName());
        copy.setSourcePosition(shared);
        copy.setSynthetic(shared.isSynthetic());
        return copy;
    }

    private static boolean containsShared(final MethodNode node) {
        SharedSentinelFinder finder = new SharedSentinelFinder();
        for (Parameter parameter : node.getParameters()) {
            if (parameter != null && parameter.hasInitialExpression()) {
                parameter.getInitialExpression().visit(finder);
            }
        }
        if (node.getCode() != null) node.getCode().visit(finder);
        return finder.found;
    }

    private static boolean containsShared(final Expression expression) {
        if (expression == null) return false;
        SharedSentinelFinder finder = new SharedSentinelFinder();
        expression.visit(finder);
        return finder.found;
    }

    private static boolean containsShared(final Statement statement) {
        if (statement == null) return false;
        SharedSentinelFinder finder = new SharedSentinelFinder();
        statement.visit(finder);
        return finder.found;
    }

    /**
     * Reports a stored {@code this} or {@code super} sentinel. A null method-pointer
     * receiver is not one: {@code getExpression()} substitutes the shared {@code this}.
     */
    private static final class SharedSentinelFinder extends CodeVisitorSupport {
        private boolean found;

        @Override
        public void visitVariableExpression(final VariableExpression expression) {
            if (expression == VariableExpression.THIS_EXPRESSION || expression == VariableExpression.SUPER_EXPRESSION) {
                found = true;
            }
        }

        @Override
        public void visitMethodPointerExpression(final MethodPointerExpression expression) {
            if (expression.getExpression() == VariableExpression.THIS_EXPRESSION) {
                // Visit only children transformExpression actually passes through.
                expression.transformExpression(child -> {
                    if (child != null) child.visit(this);
                    return child;
                });
            } else {
                super.visitMethodPointerExpression(expression);
            }
        }

        @Override
        public void visitClosureExpression(final ClosureExpression expression) {
            if (expression.isParameterSpecified()) {
                for (Parameter parameter : expression.getParameters()) {
                    if (parameter != null && parameter.hasInitialExpression()) {
                        parameter.getInitialExpression().visit(this);
                    }
                }
            }
            Statement code = expression.getCode();
            if (code != null) code.visit(this);
        }

        @Override
        public void visitSwitchExpression(final SwitchExpression expression) {
            Expression selector = expression.getExpression();
            if (selector != null) selector.visit(this);
            for (CaseStatement caseStatement : expression.getCaseStatements()) {
                if (caseStatement != null) caseStatement.visit(this);
            }
            Statement defaultStatement = expression.getDefaultStatement();
            if (defaultStatement != null) defaultStatement.visit(this);
        }
    }
}
