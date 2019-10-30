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
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class checks the handling of labels in the AST
 */
public class LabelVerifier extends ClassCodeVisitorSupport {

    private final SourceUnit source;
    private LinkedList<String> visitedLabels;
    private LinkedList<ContinueStatement> continueLabels;
    private LinkedList<BreakStatement> breakLabels;
    boolean inLoop = false;
    boolean inSwitch = false;

    public LabelVerifier(SourceUnit src) {
        source = src;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    private void init() {
        visitedLabels = new LinkedList<String>();
        continueLabels = new LinkedList<ContinueStatement>();
        breakLabels = new LinkedList<BreakStatement>();
        inLoop = false;
        inSwitch = false;
    }

    protected void visitClassCodeContainer(Statement code) {
        init();
        super.visitClassCodeContainer(code);
        assertNoLabelsMissed();
    }

    public void visitStatement(Statement statement) {
        List<String> labels = statement.getStatementLabels();

        if (labels != null) {
            for (String label : labels) {
                if (breakLabels != null) {
                    breakLabels.removeIf(breakStatement -> breakStatement.getLabel().equals(label));
                }
                if (continueLabels != null) {
                    continueLabels.removeIf(continueStatement -> continueStatement.getLabel().equals(label));
                }
                if (visitedLabels != null) {
                    visitedLabels.add(label);
                }
            }
        }

        super.visitStatement(statement);
    }

    public void visitForLoop(ForStatement forLoop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitForLoop(forLoop);
        inLoop = oldInLoop;
    }

    public void visitDoWhileLoop(DoWhileStatement loop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitDoWhileLoop(loop);
        inLoop = oldInLoop;
    }

    public void visitWhileLoop(WhileStatement loop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitWhileLoop(loop);
        inLoop = oldInLoop;
    }

    public void visitBreakStatement(BreakStatement statement) {
        String label = statement.getLabel();
        boolean hasNamedLabel = label != null;
        if (!hasNamedLabel && !inLoop && !inSwitch) {
            addError("the break statement is only allowed inside loops or switches", statement);
        } else if (hasNamedLabel && !inLoop) {
            addError("the break statement with named label is only allowed inside loops", statement);
        }
        if (label != null) {
            boolean found = false;
            for (String element : visitedLabels) {
                if (element.equals(label)) {
                    found = true;
                    break;
                }
            }
            if (!found) breakLabels.add(statement);
        }

        super.visitBreakStatement(statement);
    }

    public void visitContinueStatement(ContinueStatement statement) {
        String label = statement.getLabel();
        boolean hasNamedLabel = label != null;
        if (!hasNamedLabel && !inLoop) {
            addError("the continue statement is only allowed inside loops", statement);
        }
        if (label != null) {
            boolean found = false;
            for (String element : visitedLabels) {
                if (element.equals(label)) {
                    found = true;
                    break;
                }
            }
            if (!found) continueLabels.add(statement);
        }

        super.visitContinueStatement(statement);
    }

    protected void assertNoLabelsMissed() {
        //TODO: report multiple missing labels of the same name only once?
        for (ContinueStatement element : continueLabels) {
            addError("continue to missing label", element);
        }
        for (BreakStatement element : breakLabels) {
            addError("break to missing label", element);
        }
    }

    public void visitSwitch(SwitchStatement statement) {
        boolean oldInSwitch = inSwitch;
        inSwitch = true;
        super.visitSwitch(statement);
        inSwitch = oldInSwitch;
    }

}
