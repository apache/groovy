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
import groovy.transform.CompilationUnitAware;
import org.apache.groovy.contracts.VariantSupport;
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
import org.codehaus.groovy.control.CompilationUnit;
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
 * @see org.apache.groovy.contracts.VariantSupport
 * @see org.apache.groovy.contracts.LoopVariantViolation
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class LoopVariantASTTransformation implements ASTTransformation, CompilationUnitAware {

    private static final AtomicLong COUNTER = new AtomicLong();

    private CompilationUnit compilationUnit;

    @Override
    public void setCompilationUnit(final CompilationUnit unit) {
        this.compilationUnit = unit;
    }

    /**
     * Rewrites a loop-level {@link Decreases} annotation into variant bookkeeping and runtime checks.
     *
     * @param nodes the annotated AST nodes supplied by the compiler
     * @param source the current source unit
     */
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

        // Assert (if enabled): currVar < prevVar and non-negative — delegated to the
        // shared VariantSupport, gated by the enclosing class's -ea/-da configuration.
        String className = LoopContractSupport.enclosingClassName(source, (ASTNode) loopStatement);
        Statement decreaseCheck = stmt(
                callX(
                        ClassHelper.make(VariantSupport.class),
                        "checkLoopVariant",
                        args(varX(prevVarName), varX(currVarName), constX(className))
                )
        );
        decreaseCheck.setSourcePosition(annotation);

        // Inject: save at start, check at end
        injectAtLoopBodyStartAndEnd(loopStatement, savePrev, block(saveCurr, decreaseCheck));

        // The variant closure lived inside a statement annotation, so the compiler's resolution
        // passes never reached it; re-resolve types, static imports and variable scopes now that the
        // expressions are real loop-body statements.
        LoopContractSupport.resolveInlinedContractCode(source, (ASTNode) loopStatement, compilationUnit);
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
