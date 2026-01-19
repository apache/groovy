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
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * This class checks the handling of labels in the AST
 */
public class LabelVerifier extends ClassCodeVisitorSupport {

    boolean inIf, inLoop, inSwitch;
    private final SourceUnit source;
    private Set<String> visitedLabels;
    private List<BreakStatement> breakLabels;
    private List<ContinueStatement> continueLabels;

    public LabelVerifier(SourceUnit src) {
        source = src;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }

    protected void assertNoLabelsMissed() {
        // TODO: Report multiple missing labels of the same name only once?
        for (ContinueStatement element : continueLabels) {
            addError("continue to missing label", element);
        }
        for (BreakStatement element : breakLabels) {
            addError("break to missing label", element);
        }
    }

    @Override
    public void visitStatement(Statement s) {
        List<String> labels = s.getStatementLabels();
        if (labels != null && visitedLabels != null) {
            for (String label : labels) {
                visitedLabels.add(label);

                breakLabels.removeIf(breakStatement -> breakStatement.getLabel().equals(label));
                continueLabels.removeIf(continueStatement -> continueStatement.getLabel().equals(label));
            }
        }
    }

    @Override
    protected void visitClassCodeContainer(Statement code) {
        inIf     = false;
        inLoop   = false;
        inSwitch = false;

        visitedLabels  = new HashSet<>();
        breakLabels    = new LinkedList<>();
        continueLabels = new LinkedList<>();

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
    }

    @Override
    public void visitForLoop(ForStatement loop) {
        visitStatement(loop);
        loop.getCollectionExpression().visit(this);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
    }

    @Override
    public void visitWhileLoop(WhileStatement loop) {
        visitStatement(loop);
        loop.getBooleanExpression().visit(this);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
    }

    @Override
    public void visitDoWhileLoop(DoWhileStatement loop) {
        visitStatement(loop);
        boolean oldInLoop = inLoop;
        inLoop = true;
        loop.getLoopBlock().visit(this);
        inLoop = oldInLoop;
        loop.getBooleanExpression().visit(this);
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
            if (!visitedLabels.contains(label)) {
                breakLabels.add(breakStatement);
            }
        }
        super.visitBreakStatement(breakStatement);
    }

    @Override
    public void visitContinueStatement(ContinueStatement continueStatement) {
        if (!inLoop) { // GROOVY-3908
            addError("the continue statement is only allowed inside loops", continueStatement);
        }
        String label = continueStatement.getLabel();
        if (label != null) {
            if (!visitedLabels.contains(label)) {
                continueLabels.add(continueStatement);
            }
        }
        super.visitContinueStatement(continueStatement);
    }
}
