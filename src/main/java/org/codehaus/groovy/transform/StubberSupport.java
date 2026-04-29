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

import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Utilities for GEP-21 Shape C joint-compilation stubbers.
 *
 * <p>A stubber emits placeholder members at {@code Phases.CONVERSION} so the
 * joint-compilation stub reflects what the full transform will produce at
 * its declared phase. The placeholder is tagged with {@link #STUB_METADATA_KEY};
 * the full transform recognises the tag and either replaces the placeholder
 * body or discards the placeholder before adding its own member.
 *
 * <p>All stubbers share a single metadata key — the runtime mapping between
 * a placeholder and the full transform that should complete it is given by
 * the member's signature, not by per-stubber namespacing.
 */
public final class StubberSupport {

    /**
     * Metadata key tagging a member that was added by a CONVERSION-phase
     * stubber as a placeholder. The paired full transform consults this key
     * to decide whether to replace the body or discard the placeholder.
     */
    public static final String STUB_METADATA_KEY = "groovy.gep21.stubber.placeholder";

    /**
     * Add a placeholder method via {@link ClassNodeUtils#addGeneratedMethod}
     * and tag it with {@link #STUB_METADATA_KEY}.
     */
    public static MethodNode addStubMethod(final ClassNode classNode, final String name,
                                           final int modifiers, final ClassNode returnType,
                                           final Parameter[] parameters,
                                           final ClassNode[] exceptions,
                                           final Statement body) {
        MethodNode method = ClassNodeUtils.addGeneratedMethod(classNode, name, modifiers,
                returnType, parameters, exceptions, body);
        method.putNodeMetaData(STUB_METADATA_KEY, Boolean.TRUE);
        return method;
    }

    /**
     * Add a placeholder constructor via
     * {@link ClassNodeUtils#addGeneratedConstructor(ClassNode, int, Parameter[], ClassNode[], Statement)}
     * and tag it with {@link #STUB_METADATA_KEY}.
     */
    public static ConstructorNode addStubConstructor(final ClassNode classNode, final int modifiers,
                                                     final Parameter[] parameters,
                                                     final ClassNode[] exceptions,
                                                     final Statement body) {
        ConstructorNode ctor = ClassNodeUtils.addGeneratedConstructor(classNode, modifiers,
                parameters, exceptions, body);
        ctor.putNodeMetaData(STUB_METADATA_KEY, Boolean.TRUE);
        return ctor;
    }

    /**
     * Tag an existing node as a stubber placeholder. Useful when the addition
     * itself goes through a shared helper (e.g. {@code addComparableSurface})
     * so the stubber receives the resulting node and only needs to tag it.
     */
    public static <T extends AnnotatedNode> T tagAsStub(final T node) {
        if (node != null) node.putNodeMetaData(STUB_METADATA_KEY, Boolean.TRUE);
        return node;
    }

    /** Returns true when {@code node} carries the stubber-placeholder tag. */
    public static boolean isStub(final AnnotatedNode node) {
        return node != null && Boolean.TRUE.equals(node.getNodeMetaData(STUB_METADATA_KEY));
    }

    /** Removes the stubber-placeholder tag from {@code node}, if present. */
    public static void clearStub(final AnnotatedNode node) {
        if (node != null) node.removeNodeMetaData(STUB_METADATA_KEY);
    }

    private StubberSupport() {}
}
