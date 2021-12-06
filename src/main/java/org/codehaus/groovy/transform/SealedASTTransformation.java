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

import groovy.transform.Sealed;
import groovy.transform.SealedMode;
import groovy.transform.SealedOptions;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.runtime.StringGroovyMethods.isAtLeast;

/**
 * Handles generation of code for the @Sealed annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class SealedASTTransformation extends AbstractASTTransformation {

    private static final Class<?> SEALED_CLASS = Sealed.class;
    private static final ClassNode SEALED_TYPE = make(SEALED_CLASS);
    private static final ClassNode SEALED_OPTIONS_TYPE = make(SealedOptions.class);
    public static final String SEALED_ALWAYS_ANNOTATE = "groovy.transform.SealedOptions.alwaysAnnotate";

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!SEALED_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (cNode.isEnum()) {
                addError("@" + SEALED_CLASS.getSimpleName() + " not allowed for enum", cNode);
                return;
            }
            if (cNode.isAnnotationDefinition()) {
                addError("@" + SEALED_CLASS.getSimpleName() + " not allowed for annotation definition", cNode);
                return;
            }
            cNode.putNodeMetaData(SEALED_CLASS, Boolean.TRUE);
            boolean isPostJDK17 = false;
            String message = "Expecting JDK17+ but unable to determine target bytecode";
            if (sourceUnit != null) {
                CompilerConfiguration config = sourceUnit.getConfiguration();
                String targetBytecode = config.getTargetBytecode();
                isPostJDK17 = isAtLeast(targetBytecode, CompilerConfiguration.JDK17);
                message = "Expecting JDK17+ but found " + targetBytecode;
            }
            List<AnnotationNode> annotations = cNode.getAnnotations(SEALED_OPTIONS_TYPE);
            AnnotationNode options = annotations.isEmpty() ? null : annotations.get(0);
            SealedMode mode = getMode(options, "mode");
            boolean doNotAnnotate = options != null && memberHasValue(options, "alwaysAnnotate", Boolean.FALSE);

            boolean isNative = isPostJDK17 && mode != SealedMode.EMULATE;
            if (doNotAnnotate) {
                cNode.putNodeMetaData(SEALED_ALWAYS_ANNOTATE, Boolean.FALSE);
            }
            if (isNative) {
                cNode.putNodeMetaData(SealedMode.class, SealedMode.NATIVE);
            } else if (mode == SealedMode.NATIVE) {
                addError(message + " when attempting to create a native sealed class", cNode);
            }
            List<ClassNode> newSubclasses = getMemberClassList(anno, "permittedSubclasses");
            if (newSubclasses != null) {
                cNode.getPermittedSubclasses().addAll(newSubclasses);
            }
        }
    }

    private static SealedMode getMode(AnnotationNode node, String name) {
        if (node != null) {
            final Expression member = node.getMember(name);
            if (member instanceof PropertyExpression) {
                PropertyExpression prop = (PropertyExpression) member;
                Expression oe = prop.getObjectExpression();
                if (oe instanceof ClassExpression) {
                    ClassExpression ce = (ClassExpression) oe;
                    if (ce.getType().getName().equals("groovy.transform.SealedMode")) {
                        return SealedMode.valueOf(prop.getPropertyAsString());
                    }
                }
            }
        }
        return null;
    }
}
