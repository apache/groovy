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

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import java.util.Map;

/**
 * Transforms expressions in a whole class. Transformed expressions are usually not visited.
 */
public abstract class ClassCodeExpressionTransformer extends ClassCodeVisitorSupport implements ExpressionTransformer {

    /**
     * <strong>GOTCHA</strong>: This method does not visit Expressions within Closures, for historical
     * reason.  If you want those Expressions to be visited, you can do this:
     * <pre>
     * {@code
     * public class YourTransformer extends ClassCodeExpressionTransformer {
     *  ...
     *
     *  @Override
     *  public Expression transform(final Expression expr) {
     *    if (expr instanceof ClosureExpression) {
     *      expr.visit(this);
     *
     *      return expr;
     *    }
     *
     *    // ... your custom instanceof + expression transformation
     *    // ...
     *  }
     * }
     * }
     * </pre>
     */
    @Override
    public Expression transform(final Expression expr) {
        if (expr == null) return null;
        return expr.transformExpression(this);
    }

    @Override
    protected void visitAnnotation(final AnnotationNode node) {
        for (Map.Entry<String, Expression> entry : node.getMembers().entrySet()) {
            entry.setValue(transform(entry.getValue()));
        }
    }

    @Override
    protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
        for (Parameter p : node.getParameters()) {
            Expression init = p.getInitialExpression();
            if (init != null) p.setInitialExpression(transform(init));
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitClosureExpression(final ClosureExpression expr) {
        if (expr.isParameterSpecified()) {
            for (Parameter p : expr.getParameters()) {
                Expression init = p.getInitialExpression();
                if (init != null) p.setInitialExpression(transform(init));
            }
        }
        super.visitClosureExpression(expr);
    }

    @Override
    public void visitField(final FieldNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        if (init != null) node.setInitialValueExpression(transform(init));
    }

    @Override
    public void visitProperty(final PropertyNode node) {
        visitAnnotations(node);
        visitClassCodeContainer(node.getGetterBlock());
        visitClassCodeContainer(node.getSetterBlock());
    }

    // statements:

    @Override
    public void visitAssertStatement(final AssertStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        stmt.setMessageExpression(transform(stmt.getMessageExpression()));
    }

    @Override
    public void visitCaseStatement(final CaseStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
        stmt.getCode().visit(this);
    }

    @Override
    public void visitDoWhileLoop(final DoWhileStatement stmt) {
        stmt.getLoopBlock().visit(this);
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
    }

    @Override
    public void visitExpressionStatement(final ExpressionStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitForLoop(final ForStatement stmt) {
        visitAnnotations(stmt.getVariable()); // "for(T x : y)" or "for(x in y)"
        stmt.setCollectionExpression(transform(stmt.getCollectionExpression()));
        stmt.getLoopBlock().visit(this);
    }

    @Override
    public void visitIfElse(final IfStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        stmt.getIfBlock().visit(this);
        stmt.getElseBlock().visit(this);
    }

    @Override
    public void visitReturnStatement(final ReturnStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitSwitch(final SwitchStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
        for (CaseStatement caseStatement : stmt.getCaseStatements()) {
            caseStatement.visit(this);
        }
        stmt.getDefaultStatement().visit(this);
    }

    @Override
    public void visitSynchronizedStatement(final SynchronizedStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
        stmt.getCode().visit(this);
    }

    @Override
    public void visitThrowStatement(final ThrowStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitWhileLoop(final WhileStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        stmt.getLoopBlock().visit(this);
    }

    /**
     * Transfers the source position to target including its property expression if it has one.
     *
     * @param target resulting node
     * @param source original node
     */
    protected static void setSourcePosition(final Expression target, final Expression source) {
        target.setSourcePosition(source);
        if (target instanceof PropertyExpression) {
            ((PropertyExpression) target).getProperty().setSourcePosition(source);
        }
    }
}
