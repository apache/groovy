/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.transform;

import groovy.lang.PackageScope;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.ArrayList;

/**
 * Handles transformation for the @PackageScope annotation.
 * This is experimental, use at your own risk.
 *
 * @author Paul King
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class PackageScopeASTTransformation implements ASTTransformation, Opcodes {

    private static final Class MY_CLASS = PackageScope.class;
    private static final ClassNode MY_TYPE = new ClassNode(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    public void visit(ASTNode[] nodes, SourceUnit source) {
        if (!(nodes[0] instanceof AnnotationNode) || !(nodes[1] instanceof AnnotatedNode)) {
            throw new RuntimeException("Internal error: wrong types: $node.class / $parent.class");
        }

        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof ClassNode) {
            visitClassNode((ClassNode) parent);
        } else if (parent instanceof FieldNode) {
            visitFieldNode((FieldNode) parent);
        }
    }

    private void visitClassNode(ClassNode cNode) {
        String cName = cNode.getName();
        if (cNode.isInterface()) {
            throw new RuntimeException("Error processing interface '" + cName + "'. " + MY_TYPE_NAME + " not allowed for interfaces.");
        }
        final List<PropertyNode> pList = cNode.getProperties();
        List<PropertyNode> foundProps = new ArrayList<PropertyNode>();
        List<String> foundNames = new ArrayList<String>();
        for (PropertyNode pNode : pList) {
            foundProps.add(pNode);
            foundNames.add(pNode.getName());
        }
        for (PropertyNode pNode : foundProps) {
            pList.remove(pNode);
        }
        final List<FieldNode> fList = cNode.getFields();
        for (FieldNode fNode : fList) {
            if (foundNames.contains(fNode.getName())) {
                revertVisibility(fNode);
            }
        }
    }

    private void visitFieldNode(FieldNode fNode) {
        final ClassNode cNode = fNode.getDeclaringClass();
        final List<PropertyNode> pList = cNode.getProperties();
        PropertyNode foundProp = null;
        for (PropertyNode pNode : pList) {
            if (pNode.getName().equals(fNode.getName())) {
                foundProp = pNode;
                break;
            }
        }
        if (foundProp != null) {
            revertVisibility(fNode);
            pList.remove(foundProp);
        }
    }

    private void revertVisibility(FieldNode fNode) {
        fNode.setModifiers(fNode.getModifiers() & (~ACC_PRIVATE));
    }
}