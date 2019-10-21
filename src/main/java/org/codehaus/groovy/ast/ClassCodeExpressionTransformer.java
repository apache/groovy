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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import java.util.Map;

/**
 * Transforms expressions in a whole class. Transformed expressions are usually not visited.
 */
public abstract class ClassCodeExpressionTransformer extends ClassCodeVisitorSupport implements ExpressionTransformer {

    @Override
    public Expression transform(Expression expr) {
        if (expr == null) return null;
        return expr.transformExpression(this);
    }

    @Override
    protected void visitAnnotation(AnnotationNode node) {
        for (Map.Entry<String, Expression> entry : node.getMembers().entrySet()) {
            entry.setValue(transform(entry.getValue()));
        }
    }

    @Override
    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        for (Parameter p : node.getParameters()) {
            if (p.hasInitialExpression()) {
                Expression init = p.getInitialExpression();
                p.setInitialExpression(transform(init));
            }
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    @Override
    public void visitField(FieldNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        node.setInitialValueExpression(transform(init));
    }

    @Override
    public void visitProperty(PropertyNode node) {
        visitAnnotations(node);
        Statement statement = node.getGetterBlock();
        visitClassCodeContainer(statement);

        statement = node.getSetterBlock();
        visitClassCodeContainer(statement);
    }

    // statements:

    @Override
    public void visitAssertStatement(AssertStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        stmt.setMessageExpression(transform(stmt.getMessageExpression()));
    }

    @Override
    public void visitCaseStatement(CaseStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
        stmt.getCode().visit(this);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        super.visitDoWhileLoop(stmt);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitForLoop(ForStatement stmt) {
        stmt.setCollectionExpression(transform(stmt.getCollectionExpression()));
        super.visitForLoop(stmt);
    }

    @Override
    public void visitIfElse(IfStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        stmt.getIfBlock().visit(this);
        stmt.getElseBlock().visit(this);
    }

    @Override
    public void visitReturnStatement(ReturnStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitSwitch(SwitchStatement stmt) {
        Expression exp = stmt.getExpression();
        stmt.setExpression(transform(exp));
        for (CaseStatement caseStatement : stmt.getCaseStatements()) {
            caseStatement.visit(this);
        }
        stmt.getDefaultStatement().visit(this);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
        super.visitSynchronizedStatement(stmt);
    }

    @Override
    public void visitThrowStatement(ThrowStatement stmt) {
        stmt.setExpression(transform(stmt.getExpression()));
    }

    @Override
    public void visitWhileLoop(WhileStatement stmt) {
        stmt.setBooleanExpression((BooleanExpression) transform(stmt.getBooleanExpression()));
        super.visitWhileLoop(stmt);
    }

    /**
     * Set the source position of toSet including its property expression if it has one.
     *
     * @param toSet resulting node
     * @param origNode original node
     */
    protected static void setSourcePosition(Expression toSet, Expression origNode) {
        toSet.setSourcePosition(origNode);
        if (toSet instanceof PropertyExpression) {
            ((PropertyExpression) toSet).getProperty().setSourcePosition(origNode);
        }
    }
}
