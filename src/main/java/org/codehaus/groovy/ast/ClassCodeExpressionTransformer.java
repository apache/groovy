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

import java.util.List;
import java.util.Map;

/**
 * Visitor to transform expressions in a whole class.
 * Transformed Expressions are usually not visited.
 */
public abstract class ClassCodeExpressionTransformer extends ClassCodeVisitorSupport implements ExpressionTransformer {

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        for (Parameter p : node.getParameters()) {
            if (p.hasInitialExpression()) {
                Expression init = p.getInitialExpression();
                p.setInitialExpression(transform(init));
            }
        }
        super.visitConstructorOrMethod(node, isConstructor);
    }

    public void visitSwitch(SwitchStatement statement) {
        Expression exp = statement.getExpression();
        statement.setExpression(transform(exp));
        for (CaseStatement caseStatement : statement.getCaseStatements()) {
            caseStatement.accept(this);
        }
        statement.getDefaultStatement().accept(this);
    }

    public void visitField(FieldNode node) {
        visitAnnotations(node);
        Expression init = node.getInitialExpression();
        node.setInitialValueExpression(transform(init));
    }

    public void visitProperty(PropertyNode node) {
        visitAnnotations(node);
        Statement statement = node.getGetterBlock();
        visitClassCodeContainer(statement);

        statement = node.getSetterBlock();
        visitClassCodeContainer(statement);
    }

    public void visitIfElse(IfStatement ifElse) {
        ifElse.setBooleanExpression((BooleanExpression) (transform(ifElse.getBooleanExpression())));
        ifElse.getIfBlock().accept(this);
        ifElse.getElseBlock().accept(this);
    }

    public Expression transform(Expression exp) {
        if (exp == null) return null;
        return exp.transformExpression(this);
    }

    public void visitAnnotations(AnnotatedNode node) {
        List<AnnotationNode> annotations = node.getAnnotations();
        if (annotations.isEmpty()) return;
        for (AnnotationNode an : annotations) {
            // skip built-in properties
            if (an.isBuiltIn()) continue;
            for (Map.Entry<String, Expression> member : an.getMembers().entrySet()) {
                member.setValue(transform(member.getValue()));
            }
        }
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.setExpression(transform(statement.getExpression()));
    }

    public void visitAssertStatement(AssertStatement as) {
        as.setBooleanExpression((BooleanExpression) (transform(as.getBooleanExpression())));
        as.setMessageExpression(transform(as.getMessageExpression()));
    }

    public void visitCaseStatement(CaseStatement statement) {
        statement.setExpression(transform(statement.getExpression()));
        statement.getCode().accept(this);
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        loop.setBooleanExpression((BooleanExpression) (transform(loop.getBooleanExpression())));
        super.visitDoWhileLoop(loop);
    }

    public void visitForLoop(ForStatement forLoop) {
        forLoop.setCollectionExpression(transform(forLoop.getCollectionExpression()));
        super.visitForLoop(forLoop);
    }

    public void visitSynchronizedStatement(SynchronizedStatement sync) {
        sync.setExpression(transform(sync.getExpression()));
        super.visitSynchronizedStatement(sync);
    }

    public void visitThrowStatement(ThrowStatement ts) {
        ts.setExpression(transform(ts.getExpression()));
    }

    public void visitWhileLoop(WhileStatement loop) {
        loop.setBooleanExpression((BooleanExpression) transform(loop.getBooleanExpression()));
        super.visitWhileLoop(loop);
    }

    public void visitExpressionStatement(ExpressionStatement es) {
        es.setExpression(transform(es.getExpression()));
    }
}
