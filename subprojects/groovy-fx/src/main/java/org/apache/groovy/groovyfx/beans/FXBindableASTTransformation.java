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
package org.apache.groovy.groovyfx.beans;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.PropertyNodeUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.StubberSupport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.equalsNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifElseS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

/**
 * Handles generation of the JavaFX property, getter, setter and
 * {@code nameProperty()} accessor for {@link FXBindable} properties.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class FXBindableASTTransformation extends AbstractASTTransformation {

    private static final ClassNode MY_TYPE = make(FXBindable.class);
    private static final String PROPERTY_FIELD_SUFFIX = "$property";

    /**
     * Whether the node carries the {@code @FXBindable} annotation.
     */
    public static boolean hasFXBindableAnnotation(AnnotatedNode node) {
        for (AnnotationNode annotation : node.getAnnotations()) {
            if (MY_TYPE.equals(annotation.getClassNode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotationNode annotation = (AnnotationNode) nodes[0];
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        if (!MY_TYPE.equals(annotation.getClassNode())) return;

        if (parent instanceof FieldNode field) {
            transformProperty(annotation, field.getDeclaringClass(), field);
        } else if (parent instanceof ClassNode classNode) {
            transformClass(classNode);
        }
    }

    private void transformProperty(AnnotationNode annotation, ClassNode classNode, FieldNode field) {
        removeStubs(classNode);
        for (PropertyNode property : classNode.getProperties()) {
            if (property.getName().equals(field.getName())) {
                if (field.isStatic()) {
                    addError("@FXBindable cannot annotate a static property.", annotation);
                } else if (field.isFinal()) {
                    addError("@FXBindable cannot annotate a final property.", annotation);
                } else {
                    createProperty(classNode, property, annotation);
                }
                return;
            }
        }
        addError("@FXBindable must be on a property, not a field. Try removing the private, protected, or public modifier.", annotation);
    }

    private void transformClass(ClassNode classNode) {
        if (classNode.isInterface()) return;
        removeStubs(classNode);
        for (PropertyNode property : classNode.getProperties()) {
            FieldNode field = property.getField();
            // explicitly annotated properties are handled by their own visit
            if (hasFXBindableAnnotation(field) || field.isStatic() || field.isFinal()) {
                continue;
            }
            createProperty(classNode, property, classNode);
        }
    }

    private static void removeStubs(ClassNode classNode) {
        List<MethodNode> stubs = new ArrayList<>();
        for (MethodNode method : classNode.getMethods()) {
            if (StubberSupport.isStub(method)) stubs.add(method);
        }
        stubs.forEach(classNode::removeMethod);
    }

    private void createProperty(ClassNode classNode, PropertyNode property, ASTNode errorNode) {
        String name = property.getName();
        ClassNode type = property.getType();
        String accessorName = name + "Property";
        if (FXPropertyTypes.isJavaFXProperty(type)) {
            addError("@FXBindable cannot annotate property '" + name + "': its type is already a JavaFX property.", errorNode);
            return;
        }
        if (!classNode.getMethods(property.getSetterNameOrDefault()).isEmpty()) {
            addError("@FXBindable cannot annotate property '" + name + "': a setter is declared explicitly.", errorNode);
            return;
        }
        if (classNode.getDeclaredMethod(accessorName, Parameter.EMPTY_ARRAY) != null) {
            addError("@FXBindable cannot annotate property '" + name + "': " + accessorName + "() is declared explicitly.", errorNode);
            return;
        }

        FXPropertyTypes.Mapping mapping = FXPropertyTypes.forType(type).resolve(sourceUnit.getClassLoader());
        FieldNode valueField = property.getField();
        FieldNode propertyField = classNode.addField(name + PROPERTY_FIELD_SUFFIX, ACC_PRIVATE, mapping.propertyType, null);
        int modifiers = PropertyNodeUtils.adjustPropertyModifiersForMethod(property);

        // T getName() { return name$property == null ? name : (T) name$property.get(); }
        // a primitive boolean property also gets isName(), as the Verifier would have generated
        Expression currentValue = ternaryX(equalsNullX(fieldX(propertyField)), fieldX(valueField),
                castX(type, callX(fieldX(propertyField), "get")));
        Set<String> getterNames = new LinkedHashSet<>();
        getterNames.add(property.getGetterNameOrDefault());
        getterNames.add("get" + capitalize(name));
        if (ClassHelper.isPrimitiveBoolean(type)) {
            getterNames.add("is" + capitalize(name));
        }
        for (String getterName : getterNames) {
            if (classNode.getDeclaredMethod(getterName, Parameter.EMPTY_ARRAY) == null) {
                addGeneratedMethod(classNode, new MethodNode(getterName, modifiers, type,
                        Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, returnS(currentValue)));
            }
        }

        // void setName(T value) { if (name$property == null) name = value; else name$property.set(value); }
        Statement setterBody = ifElseS(equalsNullX(fieldX(propertyField)),
                assignS(fieldX(valueField), varX("value", type)),
                stmt(callX(fieldX(propertyField), "set", varX("value", type))));
        MethodNode setter = new MethodNode(property.getSetterNameOrDefault(), modifiers, ClassHelper.VOID_TYPE,
                params(param(type, "value")), ClassNode.EMPTY_ARRAY, setterBody);
        setter.setSynthetic(true);
        addGeneratedMethod(classNode, setter);

        // TProperty nameProperty() {
        //     if (name$property == null) name$property = new SimpleTProperty(this, "name", name);
        //     return name$property;
        // }
        Statement accessorBody = block(
                ifS(equalsNullX(fieldX(propertyField)),
                        assignS(fieldX(propertyField),
                                ctorX(mapping.implementationType, args(varX("this"), constX(name), fieldX(valueField))))),
                returnS(fieldX(propertyField)));
        addGeneratedMethod(classNode, new MethodNode(accessorName, modifiers, mapping.propertyType,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, accessorBody));
    }

    private static String capitalize(String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
