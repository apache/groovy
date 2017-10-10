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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import static org.codehaus.groovy.ast.ClassHelper.make;

/**
 * Handles generation of code for the {@link AutoFinal} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AutoFinalASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = AutoFinal.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private AnnotatedNode candidate;


    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        final ClassCodeVisitorSupport visitor = createVisitor();
        process(nodes, visitor);
    }

    private ClassCodeVisitorSupport createVisitor() {
        return new ClassCodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression expression) {
                if (expression.isSynthetic()) {
                    return;
                }
                Parameter[] origParams = expression.getParameters();
                for (Parameter p : origParams) {
                    p.setModifiers(p.getModifiers() | Modifier.FINAL);
                }
                super.visitClosureExpression(expression);
            }

            @Override
            protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
                if (hasNoExplicitAutoFinal(node) || candidate == node) {
                    super.visitConstructorOrMethod(node, isConstructor);
                }
            }

            @Override
            public void visitField(FieldNode node) {
                if (hasNoExplicitAutoFinal(node) || candidate == node) {
                    super.visitField(node);
                }
            }

            @Override
            public void visitDeclarationExpression(DeclarationExpression expr) {
                if (hasNoExplicitAutoFinal(expr) || candidate == expr) {
                    super.visitDeclarationExpression(expr);
                }
            }

            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }
        };
    }

    private void process(ASTNode[] nodes, final ClassCodeVisitorSupport visitor) {
        candidate = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (candidate instanceof ClassNode) {
            ClassNode cNode = (ClassNode) candidate;
            processClass(cNode, visitor);
        } else if (candidate instanceof MethodNode) {
            // handles constructors and methods
            MethodNode mNode = (MethodNode) candidate;
            processConstructorOrMethod(mNode, visitor);
        } else if (candidate instanceof FieldNode) {
            FieldNode fNode = (FieldNode) candidate;
            processField(fNode, visitor);
        } else if (candidate instanceof DeclarationExpression) {
            DeclarationExpression de = (DeclarationExpression) candidate;
            processLocalVariable(de, visitor);
        }
    }

    private void processLocalVariable(DeclarationExpression de, ClassCodeVisitorSupport visitor) {
        if (de.getRightExpression() instanceof ClosureExpression) {
            visitor.visitDeclarationExpression(de);
        }
    }

    private void processField(FieldNode fNode, ClassCodeVisitorSupport visitor) {
        if (fNode.hasInitialExpression() && fNode.getInitialExpression() instanceof ClosureExpression) {
            visitor.visitField(fNode);
        }
    }

    private void processClass(ClassNode cNode, final ClassCodeVisitorSupport visitor) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() +
                    "'. " + MY_TYPE_NAME + " only allowed for classes.", cNode);
            return;
        }

        for (ConstructorNode cn : cNode.getDeclaredConstructors()) {
            if (hasNoExplicitAutoFinal(cn)) {
                processConstructorOrMethod(cn, visitor);
            }
        }

        for (MethodNode mn : cNode.getAllDeclaredMethods()) {
            if (hasNoExplicitAutoFinal(mn)) {
                processConstructorOrMethod(mn, visitor);
            }
        }

        Iterator<InnerClassNode> it = cNode.getInnerClasses();
        while (it.hasNext()) {
            InnerClassNode in = it.next();
            if (in.getAnnotations(MY_TYPE).isEmpty()) {
                processClass(in, visitor);
            }
        }

        visitor.visitClass(cNode);
    }

    private boolean hasNoExplicitAutoFinal(AnnotatedNode node) {
        return node.getAnnotations(MY_TYPE).isEmpty();
    }

    private void processConstructorOrMethod(MethodNode mNode, ClassCodeVisitorSupport visitor) {
        if (mNode.isSynthetic()) return;
        Parameter[] origParams = mNode.getParameters();
        for (Parameter p : origParams) {
            p.setModifiers(p.getModifiers() | Modifier.FINAL);
        }
        visitor.visitMethod(mNode);
    }
}
