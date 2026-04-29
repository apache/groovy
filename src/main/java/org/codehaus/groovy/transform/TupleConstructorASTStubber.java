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

import groovy.transform.DefaultsMode;
import groovy.transform.TupleConstructor;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GenericsUtils.newClass;
import static org.codehaus.groovy.transform.StubberSupport.addStubConstructor;
import static org.codehaus.groovy.transform.TupleConstructorASTTransformation.SelectedTupleProperties;
import static org.codehaus.groovy.transform.TupleConstructorASTTransformation.resolveDefaultsMode;
import static org.codehaus.groovy.transform.TupleConstructorASTTransformation.selectTupleProperties;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Adds a placeholder tuple-style constructor surface for {@link TupleConstructor}
 * classes during joint compilation. The set of parameters honoured —
 * {@code includes}, {@code excludes}, {@code allNames},
 * {@code includeProperties}, {@code includeFields},
 * {@code includeSuperProperties}, {@code includeSuperFields},
 * {@code allProperties} — comes from
 * {@link TupleConstructorASTTransformation#selectTupleProperties}, the same
 * helper the full transform uses, so the joint-compilation stub's
 * constructor signature is a strict subset of the runtime's.
 *
 * <p>When {@code defaults != false} (the default), the runtime exposes the
 * full prefix-overload chain via {@code Verifier}. The stubber mirrors that
 * chain so Java consumers can drop trailing arguments. When {@code defaults
 * = false} only the maximal-arg constructor is emitted, matching the runtime.
 *
 * <p>Trait-injected and same-unit super-class members added by another
 * transform are not visible at CONVERSION; the stubber may therefore see
 * a smaller surface than the runtime does for those configurations. The
 * resulting stub remains a strict subset.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class TupleConstructorASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(TupleConstructor.class);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode) || classNode.isInterface()) return;

        // Honour user-declared parameterless construction intent only when no other
        // constructor was hand-written. Skip silently in any ambiguous case so the
        // full transform at CANONICALIZATION remains authoritative.
        //
        // Filter out stubber-tagged constructors: when @TupleConstructor and
        // @MapConstructor are composed (e.g. via @Immutable), each stubber
        // would otherwise see the OTHER stubber's placeholder and silently
        // bail out. Only genuine user-declared constructors should suppress
        // emission.
        if (classNode.getDeclaredConstructors().stream().anyMatch(c -> !StubberSupport.isStub(c))) return;

        SelectedTupleProperties selected = selectTupleProperties(this, classNode, annotation);
        List<Parameter> params = new ArrayList<>(selected.ordered().size());
        for (PropertyNode p : selected.ordered()) {
            params.add(param(newClass(p.getType()), p.getName()));
        }
        // Skip when there is nothing to expose — emitting a no-arg constructor
        // would shadow the implicit default and break configurations like
        // {@code @TupleConstructor(defaults=false)} where the full transform
        // intentionally produces no public no-arg.
        if (params.isEmpty()) return;

        DefaultsMode mode = resolveDefaultsMode(annotation, this);

        if (mode == DefaultsMode.ON) {
            // Mirror the prefix-overload chain Verifier will emit at runtime —
            // no-arg, one-arg, ..., full.
            for (int i = 0; i <= params.size(); i++) {
                emit(classNode, params.subList(0, i).toArray(Parameter.EMPTY_ARRAY));
            }
        } else {
            // OFF or AUTO: emit only the maximal form. AUTO produces a partial
            // chain at runtime based on per-property initialisers; the stubber
            // can't predict which, so it stays a strict subset by emitting the
            // single guaranteed signature.
            emit(classNode, params.toArray(Parameter.EMPTY_ARRAY));
        }

        // namedVariant=true: the full transform delegates to
        // NamedVariantASTTransformation.createMapVariant to add a Foo(Map)
        // constructor at SEMANTIC_ANALYSIS. Mirror that here so Java consumers
        // can call new Foo(myMap) against the joint-compilation stub.
        // (TupleConstructor doesn't add a @NamedVariant annotation to anything;
        // it leans on the same generation logic, so the stubber follows the
        // same pattern.)
        if (memberHasValue(annotation, "namedVariant", Boolean.TRUE)) {
            Parameter[] mapParams = {param(
                    MAP_TYPE.getPlainNodeReference(),
                    "namedArgs")};
            if (classNode.getDeclaredConstructor(mapParams) == null) {
                emit(classNode, mapParams);
            }
        }
    }

    private static void emit(final ClassNode classNode, final Parameter[] signature) {
        addStubConstructor(classNode, ACC_PUBLIC, signature, ClassNode.EMPTY_ARRAY, EmptyStatement.INSTANCE);
    }
}
