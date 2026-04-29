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

import groovy.lang.Singleton;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Joint-compilation stubber for {@link Singleton}. Emits the public static
 * {@code getInstance()} method (or {@code getXxx()} for a custom property
 * name) so Java consumers can call {@code MyClass.getInstance()} against
 * the joint-compilation stub.
 *
 * <p>The placeholder body returns {@code null}; the full
 * {@link SingletonASTTransformation} at CANONICALIZATION recognises the
 * stubber metadata and replaces the body with the real implementation
 * (which dereferences the singleton field, optionally with double-checked
 * locking when {@code lazy = true}).
 *
 * <p>The corresponding singleton field is intentionally not emitted from
 * the stubber. For the default {@code lazy = false} configuration the
 * runtime field is public static final and Java callers could read it
 * directly; emitting it here would risk a duplicate-add at CANONICALIZATION,
 * and the {@code getInstance()} accessor is the canonical Java idiom.
 * Adding the field could be a follow-up if the spike's experience suggests
 * Java consumers commonly want field-level access.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class SingletonASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Singleton.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        String propertyName = getMemberStringValue(annotation, "property", "instance");
        String getterName = SingletonASTTransformation.getGetterName(propertyName);

        // Honour any user-declared getter — leave alone.
        if (classNode.getDeclaredMethod(getterName, Parameter.EMPTY_ARRAY) != null) return;

        addStubMethod(classNode, getterName,
                ACC_PUBLIC | ACC_STATIC, newClass(classNode),
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                returnS(constX(null)));
    }
}
