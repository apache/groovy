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
import org.apache.groovy.contracts.MethodVariantSupport;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles a {@link Decreases} annotation placed on a <em>method</em> (the loop
 * case is handled by {@link LoopVariantASTTransformation}). The closure is a
 * termination measure over the method's parameters that must strictly decrease,
 * and stay non-negative, on every recursive re-entry.
 * <p>
 * The method body is wrapped so that, conceptually:
 * <pre>
 * def $m   = &lt;measure&gt;
 * def $prev = MethodVariantSupport.enter("&lt;key&gt;", $m)   // checks decrease vs the enclosing frame
 * try { &lt;original body&gt; } finally { MethodVariantSupport.exit("&lt;key&gt;", $prev) }
 * </pre>
 * The check is pure entry/exit bookkeeping keyed by method signature, so no
 * static analysis of recursive call sites is needed.
 *
 * @since 6.0.0
 * @see Decreases
 * @see MethodVariantSupport
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MethodVariantASTTransformation implements ASTTransformation {

    private static final AtomicLong COUNTER = new AtomicLong();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        // Only the method case here; the loop case is LoopVariantASTTransformation.
        if (!(nodes[1] instanceof MethodNode method)) return;

        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression closureExpression)) return;

        weaveMeasure(method, extractExpression(closureExpression), source);
    }

    /**
     * Weave the recursion-measure entry/exit bookkeeping into {@code method}'s body. Shared by the
     * direct-annotation case ({@link #visit}) and by inherited measures
     * ({@link MethodVariantInheritanceASTTransformation}). The measure is deep-copied so the
     * caller's node (e.g. an annotation closure) is never shared into the body.
     *
     * @param method  the method to instrument
     * @param measure the termination-measure expression (over the method's parameters)
     * @param source  the current source unit (for scope re-resolution)
     */
    static void weaveMeasure(final MethodNode method, final Expression measure, final SourceUnit source) {
        if (method == null || measure == null) return;
        if (method.isAbstract() || method.getCode() == null) return;

        Expression measureExpr = measure.transformExpression(e -> e); // deep copy
        String key = keyFor(method);
        String suffix = Long.toString(COUNTER.getAndIncrement());
        String measureVar = "$_gc_measure_" + suffix;
        String prevVar = "$_gc_measure_prev_" + suffix;
        String className = method.getDeclaringClass() != null ? method.getDeclaringClass().getName() : null;

        // def $m = <measure>
        Statement saveMeasure = declS(localVarX(measureVar, ClassHelper.dynamicType()), measureExpr);
        // def $prev = MethodVariantSupport.enter("<key>", "<className>", $m)
        Statement enter = declS(
                localVarX(prevVar, ClassHelper.dynamicType()),
                callX(ClassHelper.make(MethodVariantSupport.class), "enter",
                        args(constX(key), constX(className), varX(measureVar))));
        // finally { MethodVariantSupport.exit("<key>", $prev) }
        Statement exit = stmt(
                callX(ClassHelper.make(MethodVariantSupport.class), "exit",
                        args(constX(key), varX(prevVar))));

        Statement originalBody = method.getCode();
        TryCatchStatement tryFinally = new TryCatchStatement(originalBody, block(exit));
        BlockStatement newBody = block(saveMeasure, enter, tryFinally);
        newBody.setSourcePosition(originalBody);
        method.setCode(newBody);

        // The measure expression's variable references may not have been resolved (it came from an
        // annotation closure); re-run scope analysis now that it is a real method-body statement so
        // @TypeChecked/@CompileStatic can see declared types.
        LoopContractSupport.resolveVariableScopes(source);
    }

    /** A stable, unique-per-overload key: {@code declaringClass#name(paramType,...)}. */
    private static String keyFor(final MethodNode method) {
        StringBuilder sb = new StringBuilder();
        if (method.getDeclaringClass() != null) {
            sb.append(method.getDeclaringClass().getName());
        }
        sb.append('#').append(method.getName()).append('(');
        Parameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(params[i].getType().getName());
        }
        sb.append(')');
        return sb.toString();
    }

    static Expression extractExpression(final ClosureExpression closureExpression) {
        if (!(closureExpression.getCode() instanceof BlockStatement block)) return null;
        List<Statement> statements = block.getStatements();
        if (statements.size() != 1) return null;
        Statement stmt = statements.get(0);
        if (stmt instanceof ExpressionStatement exprStmt) {
            return exprStmt.getExpression();
        }
        return null;
    }
}