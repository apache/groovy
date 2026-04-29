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

import groovy.transform.IndexedProperty;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.stmt.EmptyStatement.INSTANCE;
import static org.codehaus.groovy.transform.IndexedPropertyASTTransformation.getComponentTypeForList;
import static org.codehaus.groovy.transform.IndexedPropertyASTTransformation.getModifiers;
import static org.codehaus.groovy.transform.IndexedPropertyASTTransformation.makeName;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;

/**
 * Joint-compilation stubber for {@link IndexedProperty}. Emits placeholder
 * indexed accessors {@code getXxx(int)} and (for non-immutable fields)
 * {@code setXxx(int, T)} so Java consumers can call them against the
 * joint-compilation stub.
 *
 * <p>Component type detection mirrors the full transform:
 * <ul>
 *   <li>Array fields: component type is {@code T} from {@code T[]}.</li>
 *   <li>List fields: component type is the first generic parameter, or
 *       {@code Object} if the list is raw.</li>
 *   <li>Other field types: skipped (the full transform reports an error).</li>
 * </ul>
 *
 * <p>Setter emission is suppressed when the enclosing class carries an
 * Immutable-family annotation ({@code @Immutable},
 * {@code groovy.transform.ImmutableBase}, or
 * {@code groovy.transform.KnownImmutable}). The full transform's
 * runtime guard uses field-level {@code IMMUTABLE_BREADCRUMB} metadata
 * which isn't set until CANONICALIZATION; the class-level annotation
 * check is a CONVERSION-time approximation that matches the typical
 * usage pattern. Less common configurations may produce a stub that
 * over-claims the setter — flagged in the GEP-21 spike notes.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class IndexedPropertyASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(IndexedProperty.class);
    private static final ClassNode LIST_TYPE = makeWithoutCaching(List.class, false);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof FieldNode fNode)) return;

        ClassNode cNode = fNode.getDeclaringClass();
        if (cNode == null || cNode.getProperty(fNode.getName()) == null) return;

        ClassNode fType = fNode.getType();
        ClassNode componentType;
        if (fType.isArray()) {
            componentType = fType.getComponentType();
        } else if (fType.isDerivedFrom(LIST_TYPE)) {
            componentType = getComponentTypeForList(fType);
        } else {
            // Full transform would report an error here; nothing to stub.
            return;
        }

        int modifiers = getModifiers(fNode);

        // Getter — always emitted (the full transform always emits it too).
        String getterName = makeName(fNode, "get");
        Parameter[] getterParams = params(param(ClassHelper.int_TYPE, "index"));
        if (cNode.getDeclaredMethod(getterName, getterParams) == null) {
            addStubMethod(cNode, getterName, modifiers,
                    componentType, getterParams, ClassNode.EMPTY_ARRAY,
                    returnS(constX(null)));
        }

        // Setter — skipped when the enclosing class is Immutable-family.
        if (isClassImmutable(cNode)) return;

        String setterName = getSetterName(fNode.getName());
        Parameter[] setterParams = params(
                param(ClassHelper.int_TYPE, "index"),
                param(componentType, "value"));
        if (cNode.getDeclaredMethod(setterName, setterParams) == null) {
            addStubMethod(cNode, setterName, modifiers,
                    ClassHelper.VOID_TYPE, setterParams, ClassNode.EMPTY_ARRAY, INSTANCE);
        }
    }

    private static boolean isClassImmutable(ClassNode cNode) {
        for (AnnotationNode a : cNode.getAnnotations()) {
            String name = a.getClassNode().getName();
            if ("groovy.transform.Immutable".equals(name)
                    || "groovy.transform.ImmutableBase".equals(name)
                    || "groovy.transform.KnownImmutable".equals(name)) {
                return true;
            }
        }
        return false;
    }
}
