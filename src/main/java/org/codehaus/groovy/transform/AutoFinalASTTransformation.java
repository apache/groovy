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

import groovy.transform.AutoFinal;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;

/**
 * Handles generation of code for the {@link AutoFinal} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AutoFinalASTTransformation extends AbstractASTTransformation {

    private static final Class<?> MY_CLASS = AutoFinal.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);

    private AnnotatedNode target;

    @Override
    public  void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        process(nodes, createVisitor());
    }

    private void process(final ASTNode[] nodes, final ClassCodeVisitorSupport visitor) {
        target = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;
        if (memberHasValue(node, "enabled", Boolean.FALSE)) return; // GROOVY-10585

        if (target instanceof ClassNode) {
            processClass((ClassNode) target, visitor);
        } else if (target instanceof FieldNode) {
            processField((FieldNode) target, visitor);
        } else if (target instanceof MethodNode) {
            processConstructorOrMethod((MethodNode) target, visitor);
        } else if (target instanceof DeclarationExpression) {
            processLocalVariable((DeclarationExpression) target, visitor);
        }
    }

    private void processClass(final ClassNode node, final ClassCodeVisitorSupport visitor) {
        if (!isEnabled(node)) return;

        if (node.isInterface()) {
            addError("Error processing interface '" + node.getName() + "'. @" + MY_TYPE.getNameWithoutPackage() + " only allowed for classes.", node);
            return;
        }

        for (ConstructorNode cn : node.getDeclaredConstructors()) {
            if (hasNoExplicitAutoFinal(cn)) {
                processConstructorOrMethod(cn, visitor);
            }
        }

        for (MethodNode mn : node.getAllDeclaredMethods()) {
            if (hasNoExplicitAutoFinal(mn)) {
                processConstructorOrMethod(mn, visitor);
            }
        }

        for (Iterator<? extends ClassNode> it = node.getInnerClasses(); it.hasNext(); ) { ClassNode cn = it.next();
            if (hasNoExplicitAutoFinal(cn) && !cn.isInterface()) { // GROOVY-10585
                processClass(cn, visitor);
            }
        }

        visitor.visitClass(node);
    }

    private void processField(final FieldNode node, final ClassCodeVisitorSupport visitor) {
        if (!isEnabled(node)) return;
        if (node.hasInitialExpression() && node.getInitialExpression() instanceof ClosureExpression) {
            visitor.visitField(node);
        }
    }

    private void processConstructorOrMethod(final MethodNode node, final ClassCodeVisitorSupport visitor) {
        if (!isEnabled(node)) return;
        if (node.isSynthetic()) return;
        for (Parameter p : node.getParameters()) {
            p.setModifiers(p.getModifiers() | Modifier.FINAL);
        }
        visitor.visitMethod(node);
    }

    private void processLocalVariable(final DeclarationExpression expr, final ClassCodeVisitorSupport visitor) {
        if (!isEnabled(expr)) return;
        if (expr.getRightExpression() instanceof ClosureExpression) {
            visitor.visitDeclarationExpression(expr);
        }
    }

    //--------------------------------------------------------------------------

    private ClassCodeVisitorSupport createVisitor() {
        return new ClassCodeVisitorSupport() {
            @Override
            public void visitClosureExpression(final ClosureExpression expression) {
                if (!expression.isSynthetic()) {
                    for (Parameter p : getParametersSafe(expression)) {
                        p.setModifiers(p.getModifiers() | Modifier.FINAL);
                    }
                    super.visitClosureExpression(expression);
                }
            }

            @Override
            protected void visitConstructorOrMethod(final MethodNode node, final boolean isConstructor) {
                if (target == node || hasNoExplicitAutoFinal(node)) {
                    super.visitConstructorOrMethod(node, isConstructor);
                }
            }

            @Override
            public void visitField(final FieldNode node) {
                if (target == node || hasNoExplicitAutoFinal(node)) {
                    super.visitField(node);
                }
            }

            @Override
            public void visitDeclarationExpression(final DeclarationExpression expr) {
                if (target == expr || hasNoExplicitAutoFinal(expr)) {
                    super.visitDeclarationExpression(expr);
                }
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }
        };
    }

    private boolean isEnabled(final AnnotatedNode node) {
        // any explicit false for enabled disables processing
        // this allows, for example, config script to set all
        // classes to true and one class to be explicitly disabled
        return node != null && node.getAnnotations(MY_TYPE).stream()
            .noneMatch(anno -> memberHasValue(anno, "enabled", Boolean.FALSE));
    }

    private static boolean hasNoExplicitAutoFinal(final AnnotatedNode node) {
        return node.getAnnotations(MY_TYPE).isEmpty();
    }
}
