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
package org.apache.groovy.contracts.ast;

import groovy.contracts.Invariant;
import org.apache.groovy.contracts.LoopInvariantViolation;
import org.apache.groovy.contracts.generation.AssertStatementCreationUtility;
import org.apache.groovy.contracts.generation.TryCatchBlockGenerator;
import org.apache.groovy.contracts.util.ExpressionUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.LoopingStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;

/**
 * Handles {@link Invariant} annotations placed on loop statements ({@code for},
 * {@code while}, {@code do-while}). The invariant closure is evaluated as an
 * assertion at the start of each loop iteration.
 * <p>
 * When {@code @Invariant} is placed on a class (its original usage), this
 * transform returns immediately, letting the existing global contract pipeline
 * handle it.
 * <p>
 * Example:
 * <pre>
 * int sum = 0
 * {@code @Invariant}({ 0 &lt;= i &amp;&amp; i &lt;= 4 })
 * for (int i in 0..4) {
 *     sum += i
 * }
 * </pre>
 *
 * @since 6.0.0
 * @see Invariant
 * @see org.apache.groovy.contracts.LoopInvariantViolation
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LoopInvariantASTTransformation implements ASTTransformation {

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;

        // Only handle loop statements; class-level @Invariant is handled by the
        // existing GContractsASTTransformation global pipeline.
        if (!(nodes[1] instanceof LoopingStatement loopStatement)) return;

        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression closureExpression)) return;

        List<BooleanExpression> booleanExpressions = ExpressionUtils.getBooleanExpression(closureExpression);
        if (booleanExpressions == null || booleanExpressions.isEmpty()) return;

        BlockStatement assertStatements = AssertStatementCreationUtility.getAssertionStatements(booleanExpressions);

        // Wrap in a try-catch that converts PowerAssertionError into
        // LoopInvariantViolation with a helpful message.
        BlockStatement wrapped = TryCatchBlockGenerator.generateTryCatchBlockForInlineMode(
                ClassHelper.makeWithoutCaching(LoopInvariantViolation.class),
                "<" + Invariant.class.getName() + "> loop invariant \n\n",
                assertStatements
        );
        wrapped.setSourcePosition(annotation);

        injectAtLoopBodyStart(loopStatement, wrapped);
    }

    private static void injectAtLoopBodyStart(LoopingStatement loopStatement, Statement check) {
        Statement loopBody = loopStatement.getLoopBlock();
        if (loopBody instanceof BlockStatement block) {
            block.getStatements().addAll(0, ((BlockStatement) check).getStatements());
        } else {
            BlockStatement newBody = new BlockStatement();
            newBody.addStatements(((BlockStatement) check).getStatements());
            newBody.addStatement(loopBody);
            newBody.setSourcePosition(loopBody);
            loopStatement.setLoopBlock(newBody);
        }
    }
}

