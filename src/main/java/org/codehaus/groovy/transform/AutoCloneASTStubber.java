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

import groovy.transform.AutoClone;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link AutoClone}. Adds the
 * {@link Cloneable} interface to the class header and emits a placeholder
 * {@code public Foo clone() throws CloneNotSupportedException} so Java
 * consumers can call the AutoClone-generated covariant {@code clone()}
 * against the joint-compilation stub.
 *
 * <p>Without this, Java sees only {@code Object.clone()} (which is
 * {@code protected}), so {@code foo.clone()} from Java fails to compile
 * even though the runtime exposes a public covariant override.
 *
 * <p>The full {@link AutoCloneASTTransformation} at CANONICALIZATION
 * removes the stubber-tagged placeholder before installing whichever
 * {@code clone()} body the chosen {@code style} produces (CLONE,
 * COPY_CONSTRUCTOR, SERIALIZATION, or SIMPLE).
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class AutoCloneASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(AutoClone.class);
    private static final ClassNode CLONEABLE_TYPE = make(Cloneable.class);
    private static final ClassNode CNSE_TYPE = make(CloneNotSupportedException.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        if (!classNode.implementsInterface(CLONEABLE_TYPE)) {
            classNode.addInterface(CLONEABLE_TYPE);
        }

        // public Foo clone() throws CloneNotSupportedException { return null; }
        if (classNode.getDeclaredMethod("clone", Parameter.EMPTY_ARRAY) == null) {
            addStubMethod(classNode, "clone", ACC_PUBLIC,
                    GenericsUtils.nonGeneric(classNode),
                    Parameter.EMPTY_ARRAY,
                    new ClassNode[]{CNSE_TYPE},
                    returnS(constX(null)));
        }
    }
}
