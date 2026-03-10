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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class checks the handling of labels in the AST
 */
public class LabelVerifier extends PrePostStatementVisitor {

    boolean inIf, inLoop, inSwitch;
    private final SourceUnit source;
    private Set<String> availableLabels;
    private Map<String, Statement> undefinedLabels;

    public LabelVerifier(SourceUnit src) {
        source = src;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    protected void assertNoLabelsMissed() {
        for (Map.Entry<String, Statement> entry : undefinedLabels.entrySet()) {
            String kind = (entry.getValue() instanceof BreakStatement ? "break" : "continue");
            addError(String.format("cannot %s to label '%s' from here", kind, entry.getKey()), entry.getValue());
        }
    }

    @Override
    protected void visitStatement(Statement s) {
        List<String> labels = s.getStatementLabels();
        if (labels != null) {
            availableLabels.addAll(labels);
        }
    }

    @Override
    protected void postVisitStatement(Statement s) {
        List<String> labels = s.getStatementLabels();
        if (labels != null) {
            availableLabels.removeAll(labels); // GROOVY-7617
        }
    }

    @Override
    protected void visitClassCodeContainer(Statement code) {
        inIf     = false;
        inLoop   = false;
        inSwitch = false;

        availableLabels = new HashSet<>();
        undefinedLabels = new HashMap<>();

        super.visitClassCodeContainer(code);

        assertNoLabelsMissed();
    }

    //--------------------------------------------------------------------------

    @Override
    public void visitIfElse(IfStatement cond) {
        visitStatement(cond);
        cond.getBooleanExpression().visit(this);
        boolean oldInIf = inIf;
        inIf = true;
        cond.getIfBlock().visit(this);
        cond.getElseBlock().visit(this);
        inIf = oldInIf;
        postVisitStatement(cond);
    }

    @Override
    public void visitForLoop(ForStatement loop) {
        visitStatement(loop);
        loop.getCollectionExpression().visit(this);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
        postVisitStatement(loop);
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        visitStatement(loop);
        loop.getBooleanExpression().visit(this);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
        postVisitStatement(loop);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        visitStatement(loop);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
        loop.getBooleanExpression().visit(this);
        postVisitStatement(loop);
    }

    @Override
    public void visitSwitch(SwitchStatement switchStatement) {
        visitStatement(switchStatement);
        switchStatement.getExpression().visit(this);
        boolean oldInSwitch = inSwitch;
        inSwitch = true;
        switchStatement.getCaseStatements().forEach(this::visit);
        switchStatement.getDefaultStatement().visit(this);
        inSwitch = oldInSwitch;
        postVisitStatement(switchStatement);
    }

    @Override
    public void visitBreakStatement(BreakStatement breakStatement) {
        String label = breakStatement.getLabel();
        if (label == null) {
            if (!inLoop && !inSwitch) {
                addError("the break statement is only allowed inside loops or switches", breakStatement);
            }
        } else {
            if (!inLoop && !inIf) { // GROOVY-7463
                addError("the break statement with named label is only allowed inside control statements", breakStatement);
            }
            if (!availableLabels.contains(label)) {
                undefinedLabels.put(label, breakStatement);
            }
        }
    }

    @Override
    public void visitContinueStatement(ContinueStatement continueStatement) {
        if (!inLoop) { // GROOVY-3908
            addError("the continue statement is only allowed inside loops", continueStatement);
        }
        String label = continueStatement.getLabel();
        if (label != null) {
            if (!availableLabels.contains(label)) {
                undefinedLabels.put(label, continueStatement);
            }
        }
    }
}

//------------------------------------------------------------------------------

abstract class PrePostStatementVisitor extends ClassCodeVisitorSupport {

    @Override
    protected abstract void visitStatement(Statement statement);

    protected abstract void postVisitStatement(Statement statement);

    @Override
    public void visitAssertStatement(AssertStatement statement) {
        super.visitAssertStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitBlockStatement(BlockStatement statement) {
        super.visitBlockStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitBreakStatement(BreakStatement statement) {
        super.visitBreakStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitCaseStatement(CaseStatement statement) {
        super.visitCaseStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitCatchStatement(CatchStatement statement) {
        super.visitCatchStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        super.visitContinueStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
        super.visitDoWhileLoop(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitEmptyStatement(EmptyStatement statement) {
        visitStatement(statement); // TODO: Move to super?
        super.visitEmptyStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitForLoop(ForStatement statement) {
        super.visitForLoop(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitIfElse(IfStatement statement) {
        super.visitIfElse(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        super.visitReturnStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitSwitch(SwitchStatement statement) {
        super.visitSwitch(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        super.visitThrowStatement(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        super.visitTryCatchFinally(statement);
        postVisitStatement(statement);
    }

    @Override
    public void visitWhileLoop(WhileStatement statement) {
        super.visitWhileLoop(statement);
        postVisitStatement(statement);
    }
}
