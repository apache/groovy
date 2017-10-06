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
import groovy.transform.AutoFinalClosure;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.lang.reflect.Modifier;

import static org.codehaus.groovy.ast.ClassHelper.make;

/**
 * Handles {@link AutoFinal} annotation code generation for arguments of closures.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
//class AutoFinalClosureASTTransformation implements ASTTransformation {
class AutoFinalClosureASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = AutoFinalClosure.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

//    public void visit(ASTNode[] nodes, SourceUnit unit) {
//        ClassNode annotatedClass = (ClassNode) nodes[1];
//
//        final ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
//            @Override
//            public void visitClosureExpression(ClosureExpression expression) {
//                if(expression.isSynthetic()) { return; }
//                Parameter[] origParams = expression.getParameters();
//                for (Parameter p : origParams) {
//                    p.setModifiers(p.getModifiers() | Modifier.FINAL);
//                }
//                super.visitClosureExpression(expression);
//            }
//
//            protected SourceUnit getSourceUnit() {
//                return unit;
//            }
//        };
//
//        visitor.visitClass(annotatedClass);
//    }


    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        //if(true) { throw new RuntimeException("!!!!!!!!!!!!!!!!!!!!!!! AutoFinalClosureASTTransformation TEST !!!!!!!!!!!!!!!!!!!!!!!"); }
        processClassesConstructorsMethods(nodes, source);
        processClosures(nodes, source);
    }

    private void processClassesConstructorsMethods(ASTNode[] nodes, final SourceUnit unit) {
        AnnotatedNode candidate = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (candidate instanceof ClassNode) {
            processClass((ClassNode) candidate);
        } else if (candidate instanceof MethodNode) {
            // handles constructors and methods
            processConstructorOrMethod((MethodNode) candidate);
        }
    }


    private void processClosures(ASTNode[] nodes, final SourceUnit source) {
        final ASTNode node = nodes[1];
        ClassNode annotatedClass = (ClassNode) node;

        final ClassCodeVisitorSupport visitor = new ClassCodeVisitorSupport() {
            @Override
            public void visitClosureExpression(ClosureExpression expression) {

                //if(true) { throw new RuntimeException("TEST!!!!!!!!!!!!!!!!!!!!!!!"); }

                if(expression.isSynthetic()) { return; }
                Parameter[] origParams = expression.getParameters();
                for (Parameter p : origParams) {
                    p.setModifiers(p.getModifiers() | Modifier.FINAL);
                }
                super.visitClosureExpression(expression);
            }

            protected SourceUnit getSourceUnit() {
                return source;
            }
        };

        visitor.visitClass(annotatedClass);
    }



    private void processClass(ClassNode cNode) {
        if (cNode.isInterface()) {
            addError("Error processing interface '" + cNode.getName() +
                    "'. " + MY_TYPE_NAME + " only allowed for classes.", cNode);
            return;
        }
        for (ConstructorNode cn : cNode.getDeclaredConstructors()) {
            processConstructorOrMethod(cn);
        }
        for (MethodNode mn : cNode.getAllDeclaredMethods()) {
            processConstructorOrMethod(mn);
        }
    }

    private void processConstructorOrMethod(MethodNode node) {
        if (node.isSynthetic()) return;
        Parameter[] origParams = node.getParameters();
        for (Parameter p : origParams) {
            p.setModifiers(p.getModifiers() | Modifier.FINAL);
        }
    }

}