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

import groovy.transform.NamedVariant;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.addStubConstructor;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;

/**
 * Joint-compilation stubber for {@link NamedVariant}. Emits a placeholder
 * Map-arg variant alongside the user-declared method or constructor so
 * Java consumers can call the named-arg form against the joint-compilation
 * stub.
 *
 * <p>The signature emitted is the simple "all params named" shape:
 * {@code returnType methodName(Map namedArgs)} for methods, or
 * {@code ClassName(Map namedArgs)} for constructors. Mixed cases —
 * where some parameters carry {@code @NamedParam} / {@code @NamedDelegate}
 * and some remain positional — produce a richer signature at runtime that
 * the stubber does not yet reproduce; Java consumers in those cases see
 * only the single Map-arg variant in the stub. That is a strict subset of
 * the runtime, so call sites that match it work either way; call sites
 * needing positional-plus-map don't compile against the stub.
 *
 * <p>Originally listed in Tier 3 because a {@code @NamedVariant} target
 * "added by another transform" wouldn't be visible at CONVERSION. In
 * practice transforms compose this kind of variant via {@code @NamedParam}
 * on parameters of an existing method, and direct user-written
 * {@code @NamedVariant} on a hand-written method has the target visible
 * at CONVERSION. The Tier 3 deferral was overly cautious; this stubber
 * covers the common case.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class NamedVariantASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(NamedVariant.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof MethodNode mNode)) return;

        // Full transform errors on no-arg targets; nothing to stub.
        if (mNode.getParameters().length == 0) return;

        ClassNode cNode = mNode.getDeclaringClass();
        if (cNode == null) return;

        Parameter[] mapParams = params(param(GenericsUtils.nonGeneric(MAP_TYPE), "namedArgs"));

        if (mNode instanceof ConstructorNode) {
            // Skip if a user already wrote a Map constructor of this shape.
            if (cNode.getDeclaredConstructor(mapParams) != null) return;
            addStubConstructor(cNode, mNode.getModifiers(),
                    mapParams, mNode.getExceptions(), EmptyStatement.INSTANCE);
        } else {
            // Skip if a user already wrote a Map-arg method with the same name.
            if (cNode.getDeclaredMethod(mNode.getName(), mapParams) != null) return;
            // Body returns a default value matching the declared return type.
            addStubMethod(cNode, mNode.getName(), mNode.getModifiers(),
                    mNode.getReturnType(), mapParams, mNode.getExceptions(),
                    ClassHelper.isPrimitiveVoid(mNode.getReturnType())
                            ? EmptyStatement.INSTANCE
                            : returnS(constX(null)));
        }
    }
}
