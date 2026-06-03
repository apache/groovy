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
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Inherits a method-level {@link Decreases} termination measure onto an override that does not
 * redeclare it — mirroring how groovy-contracts inherits {@code @Requires}/{@code @Ensures} (Liskov
 * substitution). Because such an override carries no annotation of its own, the per-annotation
 * {@link MethodVariantASTTransformation} never fires on it; this global transform fills that gap.
 * <p>
 * For each concrete method that overrides a super-type method carrying {@code @Decreases} (and does
 * not declare its own), the ancestor's measure — re-mapped from the ancestor's parameter names to
 * the override's, since an override may rename parameters — is woven onto the override via
 * {@link MethodVariantASTTransformation#weaveMeasure}. An override is free to declare its own
 * {@code @Decreases} to refine the measure; in that case the local transform handles it and this one
 * skips it.
 * <p>
 * Limitation: the ancestor's measure must be available as source AST (same compilation unit). When
 * the ancestor is a precompiled (binary) super-type, its measure closure survives only as a generated
 * class, so the override degrades to no recursion check — sound (conservative), and a documented
 * follow-up.
 *
 * @since 6.0.0
 * @see Decreases
 * @see MethodVariantASTTransformation
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class MethodVariantInheritanceASTTransformation implements ASTTransformation {

    private static final ClassNode DECREASES_TYPE = ClassHelper.make(Decreases.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (source == null || source.getAST() == null) return;
        for (ClassNode classNode : source.getAST().getClasses()) {
            for (MethodNode method : new ArrayList<>(classNode.getMethods())) {
                if (method.getDeclaringClass() != classNode) continue;          // only methods declared here
                if (method.isAbstract() || method.getCode() == null) continue;
                if (!method.getAnnotations(DECREASES_TYPE).isEmpty()) continue; // own measure → local transform
                MethodNode ancestor = ancestorWithDecreases(classNode, method);
                if (ancestor == null) continue;
                Expression measure = inheritedMeasure(ancestor, method);
                if (measure != null) {
                    MethodVariantASTTransformation.weaveMeasure(method, measure, source);
                }
            }
        }
    }

    /** The nearest super-type method overridden by {@code method} that carries {@code @Decreases}. */
    private static MethodNode ancestorWithDecreases(final ClassNode type, final MethodNode method) {
        for (ClassNode sc = type.getSuperClass(); sc != null; sc = sc.getSuperClass()) {
            MethodNode m = sc.getDeclaredMethod(method.getName(), method.getParameters());
            if (m != null && !m.getAnnotations(DECREASES_TYPE).isEmpty()) return m;
        }
        for (ClassNode iface : type.getAllInterfaces()) {
            MethodNode m = iface.getDeclaredMethod(method.getName(), method.getParameters());
            if (m != null && !m.getAnnotations(DECREASES_TYPE).isEmpty()) return m;
        }
        return null;
    }

    /** The ancestor's measure expression, re-mapped to the override's parameter names, or null if unavailable. */
    private static Expression inheritedMeasure(final MethodNode ancestor, final MethodNode override) {
        AnnotationNode annotation = ancestor.getAnnotations(DECREASES_TYPE).get(0);
        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression closure)) return null; // binary super: no source AST
        Expression expr = MethodVariantASTTransformation.extractExpression(closure);
        if (expr == null) return null;

        Map<String, String> rename = new HashMap<>();
        Parameter[] ap = ancestor.getParameters();
        Parameter[] op = override.getParameters();
        for (int i = 0; i < Math.min(ap.length, op.length); i++) {
            if (!ap[i].getName().equals(op[i].getName())) {
                rename.put(ap[i].getName(), op[i].getName());
            }
        }
        return rename.isEmpty() ? expr : renameVariables(expr, rename);
    }

    /** Deep-copy {@code expr}, renaming any variable reference found in {@code rename}. */
    private static Expression renameVariables(final Expression expr, final Map<String, String> rename) {
        ExpressionTransformer transformer = new ExpressionTransformer() {
            @Override
            public Expression transform(final Expression e) {
                if (e instanceof VariableExpression ve && rename.containsKey(ve.getName())) {
                    VariableExpression renamed = new VariableExpression(rename.get(ve.getName()));
                    renamed.setSourcePosition(ve);
                    return renamed;
                }
                return e == null ? null : e.transformExpression(this);
            }
        };
        return transformer.transform(expr);
    }
}
