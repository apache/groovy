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
package org.codehaus.groovy.transform;

import groovy.transform.Parallel;
import org.apache.groovy.lang.annotation.Incubating;
import org.apache.groovy.util.concurrent.ThreadHelper;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;

/**
 * Simple AST transform for {@link Parallel}: each {@code for-in} iteration body is
 * wrapped in a new thread and started immediately.
 */
@Incubating
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ParallelASTTransformation implements ASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes == null || nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        if (!Parallel.class.getName().equals(annotation.getClassNode().getName())) return;

        if (!(nodes[1] instanceof ForStatement forStatement)) {
            source.getErrorCollector().addError(new SyntaxErrorMessage(
                    new SyntaxException("@Parallel may only be applied to a for loop statement",
                            annotation.getLineNumber(), annotation.getColumnNumber()), source));
            return;
        }

        if (forStatement.getValueVariable() == null) {
            source.getErrorCollector().addError(new SyntaxErrorMessage(
                    new SyntaxException("@Parallel currently supports only for-in loops",
                            annotation.getLineNumber(), annotation.getColumnNumber()), source));
            return;
        }

        injectParallelThreadStart(forStatement, annotation);
    }

    private static void injectParallelThreadStart(final ForStatement forStatement, final AnnotationNode annotation) {
        Statement originalBody = forStatement.getLoopBlock();

        // Use a closure parameter with the same loop variable name and curry(currentValue)
        // so each launched thread receives its own iteration value.
        Parameter loopParameter = forStatement.getValueVariable();
        Parameter workerParameter = new Parameter(loopParameter.getOriginType(), loopParameter.getName());

        BlockStatement workerCode = block();
        if (originalBody instanceof BlockStatement bodyBlock) {
            bodyBlock.getStatements().forEach(workerCode::addStatement);
        } else {
            workerCode.addStatement(originalBody);
        }

        ClosureExpression worker = new ClosureExpression(new Parameter[]{workerParameter}, workerCode);
        worker.setVariableScope(new VariableScope());
        worker.setSourcePosition(annotation);

        Expression currentLoopValue = new VariableExpression(loopParameter);
        Expression boundWorker = callX(worker, "curry", args(currentLoopValue));
        Expression runnable = castX(ClassHelper.make(Runnable.class), boundWorker);

        Statement startThread = stmt(callX(
                ClassHelper.make(ThreadHelper.class),
                "startThread",
                args(runnable)));
        startThread.setSourcePosition(annotation);

        BlockStatement loopBody = block(startThread);
        loopBody.setSourcePosition(originalBody);
        forStatement.setLoopBlock(loopBody);
    }
}


