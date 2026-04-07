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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles {@link groovy.contracts.Modifies} annotations placed on methods.
 * Extracts the declared modification targets (fields and parameters) from
 * the annotation closure and stores them as node metadata on the {@link MethodNode}.
 * <p>
 * The metadata key is {@value #MODIFIES_FIELDS_KEY} and the value is a
 * {@code Set<String>} of field/parameter names. Downstream processors
 * (such as {@code @Ensures} validation) can read this metadata.
 *
 * @since 6.0.0
 * @see groovy.contracts.Modifies
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ModifiesASTTransformation implements ASTTransformation {

    /** Node metadata key for the set of modifiable field/parameter names. */
    public static final String MODIFIES_FIELDS_KEY = "groovy.contracts.modifiesFields";

    @Override
    @SuppressWarnings("unchecked")
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        if (nodes.length != 2) return;
        if (!(nodes[0] instanceof AnnotationNode annotation)) return;
        if (!(nodes[1] instanceof MethodNode methodNode)) return;

        // For @Repeatable, each annotation triggers this transform separately,
        // so merge with any existing metadata from prior invocations.
        Set<String> modifiesSet = (Set<String>) methodNode.getNodeMetaData(MODIFIES_FIELDS_KEY);
        if (modifiesSet == null) {
            modifiesSet = new LinkedHashSet<>();
        }

        extractFromAnnotation(annotation, methodNode, modifiesSet, source);

        if (!modifiesSet.isEmpty()) {
            methodNode.putNodeMetaData(MODIFIES_FIELDS_KEY, modifiesSet);
        }
    }

    private static void extractFromAnnotation(AnnotationNode annotation, MethodNode methodNode, Set<String> modifiesSet, SourceUnit source) {
        Expression value = annotation.getMember("value");
        if (!(value instanceof ClosureExpression closureExpr)) return;

        Expression expr = extractExpression(closureExpr);
        if (expr == null) {
            source.addError(new SyntaxException(
                    "@Modifies closure must contain a field reference, parameter reference, or list of references",
                    annotation.getLineNumber(), annotation.getColumnNumber()));
            return;
        }

        if (expr instanceof ListExpression listExpr) {
            for (Expression element : listExpr.getExpressions()) {
                addValidatedName(element, methodNode, modifiesSet, source);
            }
        } else {
            addValidatedName(expr, methodNode, modifiesSet, source);
        }
    }

    private static void addValidatedName(Expression expr, MethodNode methodNode, Set<String> modifiesSet, SourceUnit source) {
        if (expr instanceof PropertyExpression propExpr) {
            Expression objExpr = propExpr.getObjectExpression();
            if (objExpr instanceof VariableExpression varExpr && "this".equals(varExpr.getName())) {
                String fieldName = propExpr.getPropertyAsString();
                ClassNode declaringClass = methodNode.getDeclaringClass();
                if (declaringClass != null && declaringClass.getField(fieldName) == null) {
                    source.addError(new SyntaxException(
                            "@Modifies references field '" + fieldName + "' which does not exist in " + declaringClass.getName(),
                            expr.getLineNumber(), expr.getColumnNumber()));
                } else {
                    modifiesSet.add(fieldName);
                }
                return;
            }
        }
        if (expr instanceof VariableExpression varExpr) {
            String name = varExpr.getName();
            if (!"this".equals(name)) {
                boolean isParam = false;
                for (Parameter param : methodNode.getParameters()) {
                    if (param.getName().equals(name)) {
                        isParam = true;
                        break;
                    }
                }
                if (!isParam) {
                    source.addError(new SyntaxException(
                            "@Modifies references '" + name + "' which is not a parameter of " + methodNode.getName() + "()",
                            expr.getLineNumber(), expr.getColumnNumber()));
                } else {
                    modifiesSet.add(name);
                }
                return;
            }
        }
        source.addError(new SyntaxException(
                "@Modifies elements must be field references (this.field) or parameter references",
                expr.getLineNumber(), expr.getColumnNumber()));
    }

    private static Expression extractExpression(ClosureExpression closureExpression) {
        BlockStatement block = (BlockStatement) closureExpression.getCode();
        List<Statement> statements = block.getStatements();
        if (statements.size() != 1) return null;
        Statement stmt = statements.get(0);
        if (stmt instanceof ExpressionStatement exprStmt) {
            return exprStmt.getExpression();
        }
        return null;
    }
}
