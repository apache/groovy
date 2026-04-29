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

import groovy.lang.Lazy;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.objectweb.asm.Opcodes.ACC_FINAL;

/**
 * Joint-compilation stubber for {@link Lazy}. Aligns the stub's accessor
 * surface with what the full {@code LazyASTTransformation} produces at
 * SEMANTIC_ANALYSIS: a getter only, no setter.
 *
 * <p>How: mark the {@code @Lazy} property as final at CONVERSION. The stub
 * generator runs through {@code Verifier.visitProperty}, which auto-generates
 * a setter only when the property is neither private nor final. Marking it
 * final therefore suppresses setter emission in the stub. The full transform
 * later removes the property entirely (replacing it with a private backing
 * field plus an explicit getter), so the final flag does not survive into
 * the runtime class.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class LazyASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Lazy.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof FieldNode fieldNode)) return;

        ClassNode declaringClass = fieldNode.getDeclaringClass();
        if (declaringClass == null) return;
        for (PropertyNode property : declaringClass.getProperties()) {
            if (property.getField() == fieldNode) {
                property.setModifiers(property.getModifiers() | ACC_FINAL);
                break;
            }
        }
    }
}
