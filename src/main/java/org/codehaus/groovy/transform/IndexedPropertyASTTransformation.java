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
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.indexX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Handles generation of code for the {@code @}IndexedProperty annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class IndexedPropertyASTTransformation extends AbstractASTTransformation {

    private static final Class MY_CLASS = IndexedProperty.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private static final ClassNode LIST_TYPE = makeWithoutCaching(List.class, false);

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode node = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(node.getClassNode())) return;

        if (parent instanceof FieldNode) {
            FieldNode fNode = (FieldNode) parent;
            ClassNode cNode = fNode.getDeclaringClass();
            if (cNode.getProperty(fNode.getName()) == null) {
                addError("Error during " + MY_TYPE_NAME + " processing. Field '" + fNode.getName() +
                        "' doesn't appear to be a property; incorrect visibility?", fNode);
                return;
            }
            ClassNode fType = fNode.getType();
            if (fType.isArray()) {
                addArraySetter(fNode);
                addArrayGetter(fNode);
            } else if (fType.isDerivedFrom(LIST_TYPE)) {
                addListSetter(fNode);
                addListGetter(fNode);
            } else {
                addError("Error during " + MY_TYPE_NAME + " processing. Non-Indexable property '" + fNode.getName() +
                        "' found. Type must be array or list but found " + fType.getName(), fNode);
            }
        }
    }

    private static void addListGetter(FieldNode fNode) {
        addGetter(fNode, getComponentTypeForList(fNode.getType()));
    }

    private static void addListSetter(FieldNode fNode) {
        addSetter(fNode, getComponentTypeForList(fNode.getType()));
    }

    private static void addArrayGetter(FieldNode fNode) {
        addGetter(fNode, fNode.getType().getComponentType());
    }

    private static void addArraySetter(FieldNode fNode) {
        addSetter(fNode, fNode.getType().getComponentType());
    }

    private static void addGetter(FieldNode fNode, ClassNode componentType) {
        ClassNode cNode = fNode.getDeclaringClass();
        BlockStatement body = new BlockStatement();
        Parameter[] params = new Parameter[1];
        params[0] = new Parameter(ClassHelper.int_TYPE, "index");
        body.addStatement(stmt(indexX(varX(fNode), varX(params[0]))));
        addGeneratedMethod(cNode, makeName(fNode, "get"), getModifiers(fNode), componentType, params, null, body);
    }

    private static void addSetter(FieldNode fNode, ClassNode componentType) {
        ClassNode cNode = fNode.getDeclaringClass();
        BlockStatement body = new BlockStatement();
        Parameter[] theParams = params(
                new Parameter(ClassHelper.int_TYPE, "index"),
                new Parameter(componentType, "value"));
        body.addStatement(assignS(indexX(varX(fNode), varX(theParams[0])), varX(theParams[1])));
        addGeneratedMethod(cNode, getSetterName(fNode.getName()), getModifiers(fNode), ClassHelper.VOID_TYPE, theParams, null, body);
    }

    private static ClassNode getComponentTypeForList(ClassNode fType) {
        if (fType.isUsingGenerics() && fType.getGenericsTypes().length == 1) {
            return fType.getGenericsTypes()[0].getType();
        } else {
            return ClassHelper.OBJECT_TYPE;
        }
    }

    private static int getModifiers(FieldNode fNode) {
        int mods = ACC_PUBLIC;
        if (fNode.isStatic()) mods |= ACC_STATIC;
        return mods;
    }

    private static String makeName(FieldNode fNode, String prefix) {
        return prefix + capitalize(fNode.getName());
    }
}
