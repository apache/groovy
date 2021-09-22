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

import groovy.transform.Final;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

/**
 * Handles generation of code for the {@link Final} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class FinalASTTransformation extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Final.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode candidate = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;
        if (memberHasValue(node, "enabled", false)) return;

        if (candidate instanceof ClassNode) {
            ClassNode cNode = (ClassNode) candidate;
            checkModifiers(this, cNode.getModifiers(), cNode, "type " + cNode.getName());
            cNode.setModifiers(cNode.getModifiers() | ACC_FINAL);
        } else if (candidate instanceof MethodNode) {
            // includes constructors
            MethodNode mNode = (MethodNode) candidate;
            checkModifiers(this, mNode.getModifiers(), mNode, "method or constructor " + mNode.getName());
            mNode.setModifiers(mNode.getModifiers() | ACC_FINAL);
        } else if (candidate instanceof FieldNode) {
            FieldNode fNode = (FieldNode) candidate;
            checkModifiers(this, fNode.getModifiers(), fNode, "field " + fNode.getName());
            fNode.setModifiers(fNode.getModifiers() | ACC_FINAL);
        }
    }

    static void checkModifiers(final AbstractASTTransformation xform, final int modifiers, final AnnotatedNode node, final String place) {
        if ((modifiers & ACC_FINAL) == 0 && (modifiers & (ACC_ABSTRACT | ACC_SYNTHETIC)) == (ACC_ABSTRACT | ACC_SYNTHETIC)) {
            xform.addError("Error during " + MY_TYPE.getNameWithoutPackage() +
                    " processing: annotation found on " + place + " with innapropriate modifiers", node);
        }
    }

}
