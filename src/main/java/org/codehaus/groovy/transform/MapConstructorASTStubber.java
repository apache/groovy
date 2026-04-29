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

import groovy.transform.MapConstructor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.transform.StubberSupport.addStubConstructor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link MapConstructor}.
 *
 * <p>Emits the {@code Foo(Map)} constructor signature so Java consumers can
 * call {@code new Foo(someMap)} against the joint-compilation stub. The
 * signature is invariant with respect to property visibility — it's always
 * {@code Foo(Map)} regardless of which fields, super properties, or
 * include/exclude lists end up driving the body — so the stub is a faithful
 * representation of the runtime API.
 *
 * <p>If {@code noArg = true} is set on the annotation, an empty no-arg
 * constructor is also emitted; this mirrors the second constructor the
 * full transform produces at CANONICALIZATION.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class MapConstructorASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(MapConstructor.class);
    private static final ClassNode MAP_TYPE = ClassHelper.MAP_TYPE.getPlainNodeReference();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        // Honour any user-declared constructor — the full transform's dedupe
        // logic also relies on this assumption.
        //
        // Filter out stubber-tagged constructors: when @TupleConstructor and
        // @MapConstructor are composed (e.g. via @Immutable), each stubber
        // would otherwise see the OTHER stubber's placeholder and silently
        // bail out. Only genuine user-declared constructors should suppress
        // emission.
        if (classNode.getDeclaredConstructors().stream().anyMatch(c -> !StubberSupport.isStub(c))) return;

        addStubConstructor(classNode, ACC_PUBLIC,
                params(param(MAP_TYPE, "args")),
                ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);

        // Mirror the full transform's guard: the no-arg is emitted only when
        // there are properties to populate. Using directly-declared properties
        // here (instead of the full transform's super-aware view) keeps the
        // stub a strict subset of the runtime — an empty class still doesn't
        // get a no-arg in the stub, matching the {@code @Immutable Foo {}}
        // case.
        if (memberHasValue(annotation, "noArg", Boolean.TRUE)
                && !classNode.getProperties().isEmpty()) {
            addStubConstructor(classNode, ACC_PUBLIC,
                    Parameter.EMPTY_ARRAY,
                    ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
        }
    }
}
