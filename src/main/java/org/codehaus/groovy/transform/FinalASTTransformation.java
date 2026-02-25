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
import org.codehaus.groovy.ast.ConstructorNode;
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
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        var aNode = (AnnotationNode) nodes[0];
        var target = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(aNode.getClassNode())) return;
        if (memberHasValue(aNode, "enabled", false)) return;

        if (target instanceof ClassNode) {
            ClassNode cNode = (ClassNode) target;
            checkModifiers(cNode.getModifiers(), "type " + cNode.getName(), cNode);
            cNode.setModifiers(cNode.getModifiers() | ACC_FINAL);
        } else if (target instanceof FieldNode) {
            FieldNode fNode = (FieldNode) target;
            checkModifiers(fNode.getModifiers(), "field " + fNode.getName(), fNode);
            fNode.setModifiers(fNode.getModifiers() | ACC_FINAL);
        } else if (target instanceof ConstructorNode) {
            xformError("cannot modify a constructor", target); // GROOVY-11860
        } else if (target instanceof MethodNode) {
            MethodNode mNode = (MethodNode) target;
            checkModifiers(mNode.getModifiers(), "method " + mNode.getName(), mNode);
            mNode.setModifiers(mNode.getModifiers() | ACC_FINAL);
        }
    }

    private void checkModifiers(final int mods, final String spec, final ASTNode where) {
        if ((mods & ACC_FINAL) == 0 && (mods & (ACC_ABSTRACT | ACC_SYNTHETIC)) == (ACC_ABSTRACT | ACC_SYNTHETIC)) {
            xformError("annotation found on " + spec + " with innapropriate modifiers", where);
        }
    }

    private void xformError(final String error, final ASTNode where) {
        addError("Error during " + MY_TYPE.getNameWithoutPackage() + " processing: " + error, where);
    }
}
