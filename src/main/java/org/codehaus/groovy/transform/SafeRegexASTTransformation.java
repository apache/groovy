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

import groovy.transform.SafeRegex;
import groovy.util.regex.RegexGuard;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Arrays;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.syntax.Types.FIND_REGEX;
import static org.codehaus.groovy.syntax.Types.MATCH_REGEX;

/**
 * Handles transformation for the @SafeRegex annotation: rewrites the regex
 * operators within the annotated scope to deadline-guarded
 * {@link RegexGuard} calls.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SafeRegexASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation {

    private static final ClassNode MY_TYPE = make(SafeRegex.class);
    private static final ClassNode REGEX_GUARD_TYPE = make(RegexGuard.class);
    private static final String MILLIS_MEMBER = "millis";
    private static final long DEFAULT_MILLIS = 1000L;

    private SourceUnit sourceUnit;
    private long millis = DEFAULT_MILLIS;

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode anno) || !(nodes[1] instanceof AnnotatedNode parent)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        if (!MY_TYPE.equals(anno.getClassNode())) return;

        millis = DEFAULT_MILLIS; // the same instance visits every annotated node of a class
        Expression member = anno.getMember(MILLIS_MEMBER);
        if (member != null) {
            if (member instanceof ConstantExpression ce && ce.getValue() instanceof Number number && number.longValue() > 0) {
                millis = number.longValue();
            } else {
                addError("Expecting a positive integer constant for '" + MILLIS_MEMBER + "' annotation parameter. Found " + member.getText(), member);
                return;
            }
        }

        if (parent instanceof ClassNode) {
            super.visitClass((ClassNode) parent);
        } else if (parent instanceof ConstructorNode) {
            super.visitConstructorOrMethod((MethodNode) parent, true);
        } else if (parent instanceof MethodNode) {
            super.visitConstructorOrMethod((MethodNode) parent, false);
        } else if (parent instanceof DeclarationExpression de) {
            // local variable: guard only the initializer expression
            de.setRightExpression(transform(de.getRightExpression()));
        } else if (parent instanceof FieldNode fn) {
            // the guarded initializer is relocated to <init>/<clinit> by the Verifier as usual
            if (fn.hasInitialExpression()) {
                fn.setInitialValueExpression(transform(fn.getInitialValueExpression()));
            }
        }
    }

    @Override
    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof BinaryExpression be) {
            int type = be.getOperation().getType();
            if (type == MATCH_REGEX || type == FIND_REGEX) {
                Expression input = transform(be.getLeftExpression());
                Expression pattern = transform(be.getRightExpression());
                // the operator-order entry points keep left-to-right operand evaluation
                Expression result = new StaticMethodCallExpression(
                        REGEX_GUARD_TYPE,
                        type == MATCH_REGEX ? "matchRegex" : "findRegex",
                        new ArgumentListExpression(input, pattern, constX(millis, true)));
                result.setSourcePosition(be);
                return result;
            }
        } else if (expr instanceof ClosureExpression ce) {
            ce.getCode().visit(this);
        }
        return expr.transformExpression(this);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }
}
