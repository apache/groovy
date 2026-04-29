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

import groovy.transform.EqualsAndHashCode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link EqualsAndHashCode}. Emits placeholder
 * {@code hashCode()} and {@code equals(Object)} methods so the
 * {@code @EqualsAndHashCode}-generated overrides are visible in the
 * joint-compilation stub even though Object already provides them.
 *
 * <p>As with {@link ToStringASTStubber}, simple call sites compile either
 * way (Object-inherited dispatch is enough), but exposing the declared
 * overrides in the stub matters for Java code that does {@code super.equals()}
 * / {@code super.hashCode()} chaining or for tooling that distinguishes
 * declared from inherited methods.
 *
 * <p>The placeholder bodies return safe defaults; the full
 * {@link EqualsAndHashCodeASTTransformation} at CANONICALIZATION recognises
 * the stubber metadata and replaces the bodies with the real implementations.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class EqualsAndHashCodeASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(EqualsAndHashCode.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        // hashCode() — only emit if user hasn't already declared one.
        if (classNode.getDeclaredMethod("hashCode", Parameter.EMPTY_ARRAY) == null) {
            addStubMethod(classNode, "hashCode", ACC_PUBLIC,
                    ClassHelper.int_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                    returnS(constX(0)));
        }

        // equals(Object) — only emit if user hasn't already declared one.
        Parameter[] equalsParams = params(param(OBJECT_TYPE, "other"));
        if (classNode.getDeclaredMethod("equals", equalsParams) == null) {
            addStubMethod(classNode, "equals", ACC_PUBLIC,
                    ClassHelper.boolean_TYPE, equalsParams, ClassNode.EMPTY_ARRAY,
                    returnS(constX(Boolean.FALSE, true)));
        }
    }
}
