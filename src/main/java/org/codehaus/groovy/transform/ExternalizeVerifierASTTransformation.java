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

import groovy.transform.ExternalizeVerifier;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.hasNoArgConstructor;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstanceNonPropertyFields;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getInstancePropertyFields;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_TRANSIENT;

@org.codehaus.groovy.transform.GroovyASTTransformation(phase = CompilePhase.CLASS_GENERATION)
public class ExternalizeVerifierASTTransformation extends org.codehaus.groovy.transform.AbstractASTTransformation {
    static final Class MY_CLASS = ExternalizeVerifier.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode EXTERNALIZABLE_TYPE = make(Externalizable.class);
    private static final ClassNode SERIALIZABLE_TYPE = make(Serializable.class);

    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!hasNoArgConstructor(cNode)) {
                addError(MY_TYPE_NAME + ": An Externalizable class requires a no-arg constructor but none found", cNode);
            }
            if (!implementsExternalizable(cNode)) {
                addError(MY_TYPE_NAME + ": An Externalizable class must implement the Externalizable interface", cNode);
            }
            boolean includeFields = memberHasValue(anno, "includeFields", true);
            boolean checkPropertyTypes = memberHasValue(anno, "checkPropertyTypes", true);
            List<String> excludes = getMemberStringList(anno, "excludes");
            if (!checkPropertyList(cNode, excludes, "excludes", anno, MY_TYPE_NAME, includeFields)) return;
            List<FieldNode> list = getInstancePropertyFields(cNode);
            if (includeFields) {
                list.addAll(getInstanceNonPropertyFields(cNode));
            }
            checkProps(list, excludes, checkPropertyTypes);
        }
    }

    private void checkProps(List<FieldNode> list, List<String> excludes, boolean checkPropertyTypes) {
        for (FieldNode fNode : list) {
            if (excludes != null && excludes.contains(fNode.getName())) continue;
            if ((fNode.getModifiers() & ACC_TRANSIENT) != 0) continue;
            if ((fNode.getModifiers() & ACC_FINAL) != 0) {
                addError(MY_TYPE_NAME + ": The Externalizable property (or field) '" + fNode.getName() + "' cannot be final", fNode);
            }
            ClassNode propType = fNode.getType();
            if (checkPropertyTypes && !isPrimitiveType(propType) && !implementsExternalizable(propType) && !implementsSerializable(propType)) {
                addError(MY_TYPE_NAME + ": strict type checking is enabled and the non-primitive property (or field) '" + fNode.getName() +
                        "' in an Externalizable class has the type '" + propType.getName() + "' which isn't Externalizable or Serializable", fNode);
            }
        }
    }

    private static boolean implementsExternalizable(ClassNode cNode) {
        return cNode.implementsInterface(EXTERNALIZABLE_TYPE);
    }

    private static boolean implementsSerializable(ClassNode cNode) {
        return cNode.implementsInterface(SERIALIZABLE_TYPE);
    }

}
