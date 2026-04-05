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

import groovy.contracts.Decreases;
import org.apache.groovy.contracts.LoopVariantViolation;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.LoopingStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.geX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ltX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles {@link Decreases} annotations placed on loop statements ({@code for},
 * {@code while}, {@code do-while}). The closure must return a value that
 * strictly decreases on every iteration and remains non-negative.
 * <p>
 * The transformation injects code to:
 * <ol>
 *   <li>Save the expression value at the start of each iteration.</li>
 *   <li>Re-evaluate it at the end of the iteration.</li>
 *   <li>Assert the value has strictly decreased.</li>
 *   <li>Assert the value is non-negative.</li>
 * </ol>
 * <p>
 * Example:
 * <pre>
 * int n = 10
 * {@code @Decreases}({ n })
 * while (n &gt; 0) {
 *     n--
 * }
 * </pre>
 *
 * @since 6.0.0
 * @see Decreases
 * @see LoopVariantViolation
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LoopVariantASTTransformation implements ASTTransformation {

    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        if (!(nodes[1] instanceof LoopingStatement loopStatement)) return;

        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression closureExpression)) return;

        Expression variantExpression = extractExpression(closureExpression);
        if (variantExpression == null) return;

        String suffix = Long.toString(COUNTER.getAndIncrement());
        String prevVarName = "$_gc_decreases_prev_" + suffix;
        String currVarName = "$_gc_decreases_curr_" + suffix;

        // At start of iteration: def prevVar = <expression>
        Statement savePrev = declS(localVarX(prevVarName, ClassHelper.dynamicType()), variantExpression);
        savePrev.setSourcePosition(annotation);

        // At end of iteration: def currVar = <expression copy>
        // We need a fresh copy of the expression for re-evaluation
        Expression variantCopy = copyExpression(closureExpression);
        Statement saveCurr = declS(localVarX(currVarName, ClassHelper.dynamicType()), variantCopy);
        saveCurr.setSourcePosition(annotation);

        // Assert: currVar < prevVar (must strictly decrease)
        Statement decreaseCheck = stmt(
                callX(
                        ClassHelper.makeWithoutCaching(LoopVariantASTTransformation.class),
                        "checkDecreased",
                        args(varX(prevVarName), varX(currVarName))
                )
        );
        decreaseCheck.setSourcePosition(annotation);

        // Inject: save at start, check at end
        injectAtLoopBodyStartAndEnd(loopStatement, savePrev, block(saveCurr, decreaseCheck));
    }

    /**
     * Runtime check called from generated code. Throws {@link LoopVariantViolation}
     * if the variant did not strictly decrease or became negative.
     * <p>
     * If both values are {@link List}s, they are compared lexicographically:
     * the first position where values differ must show a strict decrease;
     * all earlier positions must be equal. If all positions are equal, the
     * variant has not decreased and a violation is thrown.
     */
    public static void checkDecreased(Object prev, Object curr) {
        if (prev instanceof List<?> prevList && curr instanceof List<?> currList) {
            checkDecreasedLexicographic(prevList, currList);
        } else if (prev instanceof Comparable && curr instanceof Comparable) {
            checkDecreasedScalar(prev, curr);
        } else {
            throw new LoopVariantViolation(
                    "<groovy.contracts.Decreases> loop variant is not Comparable: prev=" + prev + ", curr=" + curr);
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkDecreasedScalar(Object prev, Object curr) {
        Comparable<Object> prevComp = (Comparable<Object>) prev;
        if (prevComp.compareTo(curr) <= 0) {
            throw new LoopVariantViolation(
                    "<groovy.contracts.Decreases> loop variant did not decrease: was " + prev + ", now " + curr);
        }
        if (curr instanceof Number && ((Number) curr).doubleValue() < 0) {
            throw new LoopVariantViolation(
                    "<groovy.contracts.Decreases> loop variant became negative: " + curr);
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkDecreasedLexicographic(List<?> prev, List<?> curr) {
        int size = Math.min(prev.size(), curr.size());
        for (int i = 0; i < size; i++) {
            Object p = prev.get(i);
            Object c = curr.get(i);
            if (!(p instanceof Comparable) || !(c instanceof Comparable)) {
                throw new LoopVariantViolation(
                        "<groovy.contracts.Decreases> loop variant element at position " + i
                                + " is not Comparable: prev=" + p + ", curr=" + c);
            }
            int cmp = ((Comparable<Object>) p).compareTo(c);
            if (cmp > 0) {
                // This element decreased — lexicographic comparison satisfied
                return;
            }
            if (cmp < 0) {
                throw new LoopVariantViolation(
                        "<groovy.contracts.Decreases> loop variant increased at position " + i
                                + ": was " + prev + ", now " + curr);
            }
            // cmp == 0: equal at this position, check next
        }
        // All compared positions are equal — no progress
        throw new LoopVariantViolation(
                "<groovy.contracts.Decreases> loop variant did not decrease: was " + prev + ", now " + curr);
    }

    private static Expression extractExpression(ClosureExpression closureExpression) {
        BlockStatement block = (BlockStatement) closureExpression.getCode();
        List<Statement> statements = block.getStatements();
        if (statements.size() != 1) return null;
        Statement stmt = statements.get(0);
        if (stmt instanceof ExpressionStatement) {
            return ((ExpressionStatement) stmt).getExpression();
        }
        return null;
    }

    private static Expression copyExpression(ClosureExpression closureExpression) {
        // Re-extract from the closure to get a fresh AST node
        // (the original is consumed by the first injection point)
        BlockStatement block = (BlockStatement) closureExpression.getCode();
        List<Statement> statements = block.getStatements();
        if (statements.size() != 1) return null;
        Statement stmt = statements.get(0);
        if (stmt instanceof ExpressionStatement exprStmt) {
            // Use transformExpression to get a deep copy
            return exprStmt.getExpression().transformExpression(expr -> expr);
        }
        return null;
    }

    private static void injectAtLoopBodyStartAndEnd(LoopingStatement loopStatement,
                                                     Statement startCheck, Statement endCheck) {
        Statement loopBody = loopStatement.getLoopBlock();
        BlockStatement newBody;
        if (loopBody instanceof BlockStatement block) {
            // Prepend save at start
            block.getStatements().add(0, startCheck);
            // Append checks at end
            block.getStatements().addAll(((BlockStatement) endCheck).getStatements());
            newBody = block;
        } else {
            newBody = new BlockStatement();
            newBody.addStatement(startCheck);
            newBody.addStatement(loopBody);
            newBody.addStatements(((BlockStatement) endCheck).getStatements());
            newBody.setSourcePosition(loopBody);
            loopStatement.setLoopBlock(newBody);
        }
    }
}
