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

import groovy.transform.RecordBase;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.LIST_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.MAP_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.TUPLE_CLASSES;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.defaultValueX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceProperties;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GenericsUtils.nonGeneric;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.COMPONENTS;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.COPY_WITH;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.GET_AT;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.NAMED_ARGS;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.SIZE;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.TO_LIST;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.TO_MAP;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.getRecordOptions;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddComponents;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddCopyWith;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddGetAt;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddSize;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddToList;
import static org.codehaus.groovy.transform.RecordTypeASTTransformation.shouldAddToMap;
import static org.codehaus.groovy.transform.StubberSupport.addStubMethod;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * Joint-compilation stubber for {@link RecordBase}. For emulated records
 * (target {@code < JDK16} or explicit {@code mode = EMULATE}) it produces
 * the same Java-callable surface that the runtime transform produces at
 * SEMANTIC_ANALYSIS:
 *
 * <ul>
 *   <li>Property modifier flips so the stub generator's {@code Verifier}
 *       sub-pass emits {@code componentName()} accessors instead of
 *       {@code getComponentName()} and skips setter synthesis on the
 *       (now-final) properties.</li>
 *   <li>Stub placeholders for the Groovy-specific record convenience
 *       methods — {@code getAt(int)}, {@code toList()}, {@code toMap()},
 *       {@code size()}, plus the opt-in {@code copyWith(Map)} and
 *       {@code components()} when enabled via {@code @RecordOptions}.</li>
 * </ul>
 *
 * <p>The "should we add this?" predicates are shared with the full
 * transform via package-private statics on
 * {@link RecordTypeASTTransformation}, so the stub never exposes a method
 * the runtime won't add (matching the same subset invariant as
 * {@code @Delegate}'s shared filter helpers).
 *
 * <p>The canonical constructor on the stub comes transitively from the
 * existing {@code @TupleConstructor} stubber, which {@code @RecordType}'s
 * {@code @AnnotationCollector} pulls in.
 *
 * <p><b>Native records.</b> When the class would compile as a native JVM
 * record (target {@code >= JDK16} and {@code mode != EMULATE}), the stub
 * generator already renders {@code record Foo(...)} syntax via the
 * back-channel introduced by GROOVY-11974, and {@code javac} synthesises
 * the canonical constructor and component accessors itself. This stubber
 * detects that case via
 * {@link RecordTypeASTTransformation#wouldBeNativeRecord} and bails out;
 * applying property modifier flips or stubbing convenience methods on a
 * native-record AST would interfere with subsequent record-component
 * processing.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class RecordBaseASTStubber extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(RecordBase.class);

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;
        if (!(parent instanceof ClassNode classNode)) return;

        // Native records are handled by the stub generator's back-channel
        // (GROOVY-11974). Leave the AST untouched in that case.
        String targetBytecode = source.getConfiguration().getTargetBytecode();
        if (RecordTypeASTTransformation.wouldBeNativeRecord(classNode, targetBytecode)) return;

        List<PropertyNode> pList = getInstanceProperties(classNode);

        for (PropertyNode pNode : pList) {
            FieldNode fNode = pNode.getField();
            if (fNode != null) {
                fNode.setModifiers((fNode.getModifiers() & ~ACC_PUBLIC) | ACC_PRIVATE | ACC_FINAL);
            }
            // Drives Verifier to emit `T name()` instead of `T getName()`.
            pNode.setGetterName(pNode.getName());
            // Final property → Verifier skips setter synthesis.
            pNode.setModifiers(pNode.getModifiers() | ACC_FINAL);
        }

        AnnotationNode options = getRecordOptions(classNode);
        int modifiers = ACC_PUBLIC | ACC_FINAL;

        if (shouldAddGetAt(classNode, options)) {
            addStubMethod(classNode, GET_AT, modifiers, OBJECT_TYPE,
                    params(param(int_TYPE, "i")), ClassNode.EMPTY_ARRAY,
                    returnS(defaultValueX(OBJECT_TYPE)));
        }
        if (shouldAddToList(classNode, options)) {
            addStubMethod(classNode, TO_LIST, modifiers, LIST_TYPE.getPlainNodeReference(),
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                    returnS(defaultValueX(LIST_TYPE)));
        }
        if (shouldAddToMap(classNode, options)) {
            addStubMethod(classNode, TO_MAP, modifiers, MAP_TYPE.getPlainNodeReference(),
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                    returnS(defaultValueX(MAP_TYPE)));
        }
        if (shouldAddSize(classNode, options)) {
            addStubMethod(classNode, SIZE, modifiers, int_TYPE,
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                    returnS(constX(pList.size(), true)));
        }
        if (shouldAddCopyWith(classNode, options)) {
            Parameter mapParam = param(nonGeneric(MAP_TYPE), NAMED_ARGS);
            addStubMethod(classNode, COPY_WITH, modifiers, classNode.getPlainNodeReference(),
                    params(mapParam), ClassNode.EMPTY_ARRAY,
                    returnS(defaultValueX(classNode)));
        }
        if (shouldAddComponents(classNode, options)) {
            // Match the runtime's TUPLE_CLASSES[size] return type — only
            // valid for size <= 16; the full transform reports the error
            // for size > 16 at SEMANTIC_ANALYSIS.
            int size = pList.size();
            if (size <= 16 && size < TUPLE_CLASSES.length) {
                ClassNode tupleType = makeWithoutCaching(TUPLE_CLASSES[size], false);
                addStubMethod(classNode, COMPONENTS, modifiers, tupleType,
                        Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY,
                        returnS(defaultValueX(tupleType)));
            }
        }
    }
}
