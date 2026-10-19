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
package org.apache.groovy.groovyfx.beans;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Declares placeholder {@code nameProperty()} methods for {@link FXBindable}
 * properties early enough for joint-compilation stubs to see them. The real
 * methods are installed by {@link FXBindableASTTransformation}, which replaces
 * these placeholders.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class FXBindableASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(FXBindable.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;

        if (parent instanceof ClassNode classNode) {
            if (classNode.isInterface()) return;
            for (PropertyNode property : classNode.getProperties()) {
                if (!property.isStatic() && !property.getField().isFinal()) {
                    addStub(classNode, property);
                }
            }
        } else if (parent instanceof FieldNode field) {
            ClassNode classNode = field.getDeclaringClass();
            for (PropertyNode property : classNode.getProperties()) {
                if (property.getName().equals(field.getName())) {
                    addStub(classNode, property);
                }
            }
        }
    }

    private static void addStub(ClassNode classNode, PropertyNode property) {
        String accessor = property.getName() + "Property";
        if (classNode.getDeclaredMethod(accessor, Parameter.EMPTY_ARRAY) != null) return;
        ClassNode returnType = FXPropertyTypes.forType(property.getType()).propertyType;
        addStubMethod(classNode, accessor, ACC_PUBLIC, returnType,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(constX(null)));
    }
}
