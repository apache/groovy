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

    /**
     * Creates a verifier for label usage in one source unit.
     *
     * @param src the source unit to report errors against
     */
    public LabelVerifier(SourceUnit src) {
        source = src;
    }

    /**
     * Returns the source unit currently being verified.
     *
     * @return the active source unit
     */
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Reports any labeled break or continue statements that could not be matched.
     */
    protected void assertNoLabelsMissed() {
        for (Map.Entry<String, Statement> entry : undefinedLabels.entrySet()) {
            String kind = (entry.getValue() instanceof BreakStatement ? "break" : "continue");
            addError(String.format("cannot %s to label '%s' from here", kind, entry.getKey()), entry.getValue());
        }
    }

    /**
     * Registers any labels introduced by the current statement.
     *
     * @param s the statement being entered
     */
    @Override
    protected void visitStatement(Statement s) {
        List<String> labels = s.getStatementLabels();
        if (labels != null) {
            availableLabels.addAll(labels);
        }
    }

    /**
     * Removes any labels that go out of scope after visiting the current statement.
     *
     * @param s the statement being exited
     */
    @Override
    protected void postVisitStatement(Statement s) {
        List<String> labels = s.getStatementLabels();
        if (labels != null) {
            availableLabels.removeAll(labels); // GROOVY-7617
        }
    }

    /**
     * Resets label-tracking state before verifying a class code block.
     *
     * @param code the statement container to verify
     */
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

    /**
     * Verifies labels inside an {@code if}/{@code else} statement.
     *
     * @param cond the conditional statement to inspect
     */
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

    /**
     * Verifies labels inside a {@code for} loop.
     *
     * @param loop the loop to inspect
     */
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

    /**
     * Verifies labels inside a {@code while} loop.
     *
     * @param loop the loop to inspect
     */
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

    /**
     * Verifies labels inside a {@code do}/{@code while} loop.
     *
     * @param loop the loop to inspect
     */
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

    /**
     * Verifies labels inside a {@code switch} statement.
     *
     * @param switchStatement the switch statement to inspect
     */
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

    /**
     * Verifies that a {@code break} statement appears in a valid context.
     *
     * @param breakStatement the break statement to inspect
     */
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

    /**
     * Verifies that a {@code continue} statement appears in a valid loop context.
     *
     * @param continueStatement the continue statement to inspect
     */
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

/**
 * Statement visitor that invokes hooks before and after each statement visit.
 */
abstract class PrePostStatementVisitor extends ClassCodeVisitorSupport {

    /**
     * Handles statement-entry bookkeeping.
     *
     * @param statement the statement being entered
     */
    @Override
    protected abstract void visitStatement(Statement statement);

    /**
     * Handles statement-exit bookkeeping.
     *
     * @param statement the statement being exited
     */
    protected abstract void postVisitStatement(Statement statement);

    /**
     * Visits an assert statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitAssertStatement(AssertStatement statement) {
        super.visitAssertStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a block statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitBlockStatement(BlockStatement statement) {
        super.visitBlockStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a break statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitBreakStatement(BreakStatement statement) {
        super.visitBreakStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a case statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitCaseStatement(CaseStatement statement) {
        super.visitCaseStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a catch statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        super.visitCatchStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a continue statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitContinueStatement(ContinueStatement statement) {
        super.visitContinueStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a do-while loop and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitDoWhileLoop(DoWhileStatement statement) {
        super.visitDoWhileLoop(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits an empty statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitEmptyStatement(EmptyStatement statement) {
        visitStatement(statement); // TODO: Move to super?
        super.visitEmptyStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits an expression statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitExpressionStatement(ExpressionStatement statement) {
        super.visitExpressionStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a for loop and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitForLoop(ForStatement statement) {
        super.visitForLoop(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits an if-else statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitIfElse(IfStatement statement) {
        super.visitIfElse(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a return statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        super.visitReturnStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a switch statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitSwitch(SwitchStatement statement) {
        super.visitSwitch(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a synchronized statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitSynchronizedStatement(SynchronizedStatement statement) {
        super.visitSynchronizedStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a throw statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitThrowStatement(ThrowStatement statement) {
        super.visitThrowStatement(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a try-catch-finally statement and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitTryCatchFinally(TryCatchStatement statement) {
        super.visitTryCatchFinally(statement);
        postVisitStatement(statement);
    }

    /**
     * Visits a while loop and then runs post-visit bookkeeping.
     *
     * @param statement the statement to visit
     */
    @Override
    public void visitWhileLoop(WhileStatement statement) {
        super.visitWhileLoop(statement);
        postVisitStatement(statement);
    }
}
