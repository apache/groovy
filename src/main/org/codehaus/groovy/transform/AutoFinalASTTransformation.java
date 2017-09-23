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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.reflect.Modifier;

import static org.codehaus.groovy.ast.ClassHelper.make;

/**
 * Handles generation of code for the {@link AutoFinal} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class AutoFinalASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = AutoFinal.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
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
