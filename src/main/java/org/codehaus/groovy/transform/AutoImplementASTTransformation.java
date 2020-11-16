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

import groovy.transform.AutoImplement;
import groovy.transform.Undefined;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getPropertyName;
import static org.apache.groovy.ast.tools.MethodNodeUtils.methodDescriptorWithoutReturnType;
import static org.apache.groovy.util.BeanUtils.capitalize;
import static org.codehaus.groovy.antlr.PrimitiveHelper.getDefaultValueForPrimitive;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getGetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.getSetterName;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersEqual;

/**
 * Generates code for the {@code @AutoImplement} annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AutoImplementASTTransformation extends AbstractASTTransformation {

    private static final Class<?> MY_CLASS = AutoImplement.class;
    private static final ClassNode MY_TYPE = ClassHelper.make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    @Override
    public void visit(final ASTNode[] nodes, final SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;

            String message = getMemberStringValue(anno, "message");
            ClassNode exception = getMemberClassValue(anno, "exception");
            if (exception != null && Undefined.isUndefinedException(exception)) {
                exception = null;
            }

            Expression code = anno.getMember("code");
            if (code != null && !(code instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'code'. Found " + code, cNode);
            } else {
                createMethods(cNode, exception, message, (ClosureExpression) code);
                if (code != null) {
                    anno.setMember("code", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
                }
            }
        }
    }

    private void createMethods(final ClassNode cNode, final ClassNode exception, final String message, final ClosureExpression code) {
        for (MethodNode candidate : getAllCorrectedMethodsMap(cNode).values()) {
            if (candidate.isAbstract()) {
                addGeneratedMethod(cNode,
                        candidate.getName(),
                        candidate.getModifiers() & 0x7, // visibility only
                        candidate.getReturnType(),
                        candidate.getParameters(),
                        candidate.getExceptions(),
                        createMethodBody(cNode, candidate, exception, message, code)
                );
            }
        }
    }

    private static Statement createMethodBody(final ClassNode cNode, final MethodNode mNode, final ClassNode exception, final String message, final ClosureExpression code) {
        if (mNode.getParameters().length == 0) {
            String propertyName = getPropertyName(mNode);
            if (propertyName != null) {
                String accessorName = mNode.getName().startsWith("is")
                        ? "get" + capitalize(propertyName) : "is" + capitalize(propertyName);
                if (cNode.hasMethod(accessorName, Parameter.EMPTY_ARRAY)) {
                    // delegate to existing accessor to reduce the surprise
                    return returnS(callX(varX("this"), accessorName));
                }
            }
        }

        if (code != null) {
            return code.getCode();
        }

        if (exception != null) {
            if (message == null) {
                return throwS(ctorX(exception));
            } else {
                return throwS(ctorX(exception, constX(message)));
            }
        }

        Expression retval = getDefaultValueForPrimitive(mNode.getReturnType());
        if (retval == null) retval = nullX();
        return returnS(retval);
    }

    /**
     * Returns all methods including abstract super/interface methods but only
     * if not overridden by a concrete declared/inherited method.
     */
    private static Map<String, MethodNode> getAllCorrectedMethodsMap(final ClassNode cNode) {
        Map<String, MethodNode> result = new HashMap<>();
        for (MethodNode mn : cNode.getMethods()) {
            result.put(methodDescriptorWithoutReturnType(mn), mn);
        }
        ClassNode next = cNode;
        while (true) {
            Map<String, ClassNode> genericsSpec = createGenericsSpec(next);
            if (next != cNode) {
                for (MethodNode mn : next.getMethods()) {
                    MethodNode correctedMethod = correctToGenericsSpec(genericsSpec, mn);
                    ClassNode correctedClass = correctToGenericsSpecRecurse(genericsSpec, next);
                    MethodNode found = getDeclaredMethodCorrected(genericsSpec, correctedMethod, correctedClass);
                    if (found != null) {
                        String td = methodDescriptorWithoutReturnType(found);
                        if (result.containsKey(td) && !result.get(td).isAbstract()) {
                            continue;
                        }
                        result.put(td, found);
                    }
                }
            }
            List<ClassNode> interfaces = new ArrayList<>();
            Collections.addAll(interfaces, next.getInterfaces());
            Map<String, ClassNode> updatedGenericsSpec = new HashMap<>(genericsSpec);
            while (!interfaces.isEmpty()) {
                ClassNode origInterface = interfaces.remove(0);
                // ignore java.lang.Object; also methods added by Verifier for GroovyObject are already good enough
                if (!origInterface.equals(ClassHelper.OBJECT_TYPE) && !origInterface.equals(ClassHelper.GROOVY_OBJECT_TYPE)) {
                    updatedGenericsSpec = createGenericsSpec(origInterface, updatedGenericsSpec);
                    ClassNode correctedInterface = correctToGenericsSpecRecurse(updatedGenericsSpec, origInterface);
                    for (MethodNode nextMethod : correctedInterface.getMethods()) {
                        MethodNode correctedMethod = correctToGenericsSpec(updatedGenericsSpec, nextMethod);
                        MethodNode found = getDeclaredMethodCorrected(updatedGenericsSpec, correctedMethod, correctedInterface);
                        if (found != null) {
                            String td = methodDescriptorWithoutReturnType(found);
                            if (result.containsKey(td) && !result.get(td).isAbstract()) {
                                continue;
                            }
                            result.put(td, found);
                        }
                    }
                    Collections.addAll(interfaces, correctedInterface.getInterfaces());
                }
            }
            ClassNode superClass = next.getUnresolvedSuperClass();
            if (superClass == null) {
                break;
            }
            next = correctToGenericsSpecRecurse(updatedGenericsSpec, superClass);
        }

        // GROOVY-9816: remove entries for to-be-generated property access and mutate methods
        for (ClassNode cn = cNode; cn != null && !cn.equals(ClassHelper.OBJECT_TYPE); cn = cn.getSuperClass()) {
            for (PropertyNode pn : cn.getProperties()) {
                if (!pn.getField().isFinal()) {
                    result.remove(getSetterName(pn.getName()) + ":" + pn.getType().getText() + ",");
                }
                if (!pn.getType().equals(ClassHelper.boolean_TYPE)) {
                    result.remove(getGetterName(pn) + ":");
                } else {
                    // getter generated only if no explicit isser and vice versa
                    String isserName  = "is" + capitalize(pn.getName());
                    String getterName = getGetterName(pn);
                    if (!cNode.hasMethod(isserName, Parameter.EMPTY_ARRAY)) {
                        result.remove(getterName + ":");
                    }
                    if (!cNode.hasMethod(getterName, Parameter.EMPTY_ARRAY)) {
                        result.remove(isserName + ":");
                    }
                }
            }
        }

        return result;
    }

    private static MethodNode getDeclaredMethodCorrected(final Map<String, ClassNode> genericsSpec, final MethodNode origMethod, final ClassNode correctedClass) {
        for (MethodNode nameMatch : correctedClass.getDeclaredMethods(origMethod.getName())) {
            MethodNode correctedMethod = correctToGenericsSpec(genericsSpec, nameMatch);
            if (parametersEqual(correctedMethod.getParameters(), origMethod.getParameters())) {
                return correctedMethod;
            }
        }
        return null;
    }
}
