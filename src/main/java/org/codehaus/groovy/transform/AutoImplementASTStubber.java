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

import groovy.transform.AutoImplement;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.AutoImplementASTTransformation.getAllCorrectedMethodsMap;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;

/**
 * Joint-compilation stubber for {@link AutoImplement}. Emits placeholder
 * implementations of abstract methods reachable through the supertype and
 * interface graph, so Java consumers can call those methods against the
 * joint-compilation stub.
 *
 * <p>The walk mirrors the full transform's
 * {@link AutoImplementASTTransformation#getAllCorrectedMethodsMap}: classpath
 * supertypes/interfaces are fully resolved at CONVERSION; same-unit Groovy
 * supertypes/interfaces have their source-declared abstracts visible too.
 *
 * <p><b>Boundary.</b> Abstract methods <em>contributed by another transform</em>
 * to a same-unit Groovy supertype/interface (most notably trait
 * abstracts, since {@code TraitASTTransformation} runs at CANONICALIZATION
 * after this stubber) are not visible at CONVERSION and therefore not stubbed.
 * The runtime still implements them; only the Java-visible stub surface is
 * reduced. Java callers that reach for those members get a stub-time compile
 * error rather than a stub/runtime mismatch.
 *
 * <p>The placeholder body is a default-value return; the real body is
 * installed by the full transform at CANONICALIZATION, which first discards
 * stubber-tagged placeholders so the walk over {@code isAbstract()} candidates
 * sees the true abstract surface again.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class AutoImplementASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(AutoImplement.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        for (MethodNode candidate : getAllCorrectedMethodsMap(classNode).values()) {
            if (!candidate.isAbstract()) continue;
            // Skip if the user (or a sibling stubber) has already declared this signature.
            if (classNode.getDeclaredMethod(candidate.getName(), candidate.getParameters()) != null) continue;
            MethodNode stub = addStubMethod(classNode,
                    candidate.getName(),
                    candidate.getModifiers() & 0x7, // visibility only
                    candidate.getReturnType(),
                    candidate.getParameters(),
                    candidate.getExceptions(),
                    returnS(defaultValueX(candidate.getReturnType())));
            stub.setGenericsTypes(candidate.getGenericsTypes());
        }
    }
}
