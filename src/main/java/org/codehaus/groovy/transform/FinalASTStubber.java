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
import static org.objectweb.asm.Opcodes.ACC_FINAL;

/**
 * Joint-compilation stubber for {@link Final}. Flips the {@code ACC_FINAL}
 * modifier on the target class / field / method at CONVERSION so the stub
 * presents the runtime final-ness to {@code javac}.
 *
 * <p>Without this, an {@code @Immutable} (or any {@code @Final}-composed)
 * Groovy class compiles in joint compilation against a stub that lets Java
 * code declare {@code class JavaSubclass extends ImmutableThing}, which
 * the runtime then rejects (or breaks the immutability invariants if it
 * doesn't).
 *
 * <p>No metadata-key handoff is required: the full
 * {@link FinalASTTransformation} ORs {@code ACC_FINAL} into the modifiers
 * unconditionally, which is idempotent if the bit was already set by the
 * stubber.
 *
 * <p>Like {@link LazyASTStubber} this stubber adds no new members — it
 * only adjusts modifiers — so the typical {@code addStub*} +
 * metadata-key dance from {@link StubberSupport} doesn't apply.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class FinalASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(Final.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode target = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (memberHasValue(annotation, "enabled", false)) return;

        if (target instanceof ClassNode cNode) {
            // Skip interfaces (including @interface annotation types).
            // ClassCompletionVerifier rejects final-on-interface as
            // "by nature abstract"; this also covers @AnnotationCollector
            // meta-annotation definitions where @Final reaches the
            // collector target spuriously through annotation composition.
            if (cNode.isInterface()) return;
            cNode.setModifiers(cNode.getModifiers() | ACC_FINAL);
        } else if (target instanceof FieldNode fNode) {
            fNode.setModifiers(fNode.getModifiers() | ACC_FINAL);
        } else if (target instanceof MethodNode mNode) {
            // Skip ConstructorNode (subclass of MethodNode) — the full
            // transform reports an error for constructor targets.
            if (!(mNode instanceof org.codehaus.groovy.ast.ConstructorNode)) {
                mNode.setModifiers(mNode.getModifiers() | ACC_FINAL);
            }
        }
    }
}
