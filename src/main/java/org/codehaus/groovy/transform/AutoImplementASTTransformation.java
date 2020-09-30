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
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedMethod;
import static org.apache.groovy.ast.tools.MethodNodeUtils.methodDescriptorWithoutReturnType;
import static org.codehaus.groovy.antlr.PrimitiveHelper.getDefaultValueForPrimitive;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.expr.ArgumentListExpression.EMPTY_ARGUMENTS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpec;
import static org.codehaus.groovy.ast.tools.GenericsUtils.correctToGenericsSpecRecurse;
import static org.codehaus.groovy.ast.tools.GenericsUtils.createGenericsSpec;

/**
 * Handles generation of code for the @AutoImplement annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AutoImplementASTTransformation extends AbstractASTTransformation {
    static final Class MY_CLASS = AutoImplement.class;
    static final ClassNode MY_TYPE = make(MY_CLASS);
    static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!MY_TYPE.equals(anno.getClassNode())) return;

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, MY_TYPE_NAME)) return;
            ClassNode exception = getMemberClassValue(anno, "exception");
            if (exception != null && Undefined.isUndefinedException(exception)) {
                exception = null;
            }
            String message = getMemberStringValue(anno, "message");
            Expression code = anno.getMember("code");
            if (code != null && !(code instanceof ClosureExpression)) {
                addError("Expected closure value for annotation parameter 'code'. Found " + code, cNode);
                return;
            }
            createMethods(cNode, exception, message, (ClosureExpression) code);
            if (code != null) {
                anno.setMember("code", new ClosureExpression(Parameter.EMPTY_ARRAY, EmptyStatement.INSTANCE));
            }
        }
    }

    private void createMethods(ClassNode cNode, ClassNode exception, String message, ClosureExpression code) {
        for (MethodNode candidate : getAllCorrectedMethodsMap(cNode).values()) {
            if (candidate.isAbstract()) {
                addGeneratedMethod(cNode, candidate.getName(), Opcodes.ACC_PUBLIC, candidate.getReturnType(),
                        candidate.getParameters(), candidate.getExceptions(),
                        methodBody(exception, message, code, candidate.getReturnType()));
            }
        }
    }

    /**
     * Return all methods including abstract super/interface methods but only if not overridden
     * by a concrete declared/inherited method.
     */
    private static Map<String, MethodNode> getAllCorrectedMethodsMap(ClassNode cNode) {
        Map<String, MethodNode> result = new HashMap<String, MethodNode>();
        for (MethodNode mn : cNode.getMethods()) {
            result.put(methodDescriptorWithoutReturnType(mn), mn);
        }
        ClassNode next = cNode;
        while (true) {
            Map<String, ClassNode> genericsSpec = createGenericsSpec(next);
            for (MethodNode mn : next.getMethods()) {
                MethodNode correctedMethod = correctToGenericsSpec(genericsSpec, mn);
                if (next != cNode) {
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
            List<ClassNode> interfaces = new ArrayList<ClassNode>(Arrays.asList(next.getInterfaces()));
            Map<String, ClassNode> updatedGenericsSpec = new HashMap<String, ClassNode>(genericsSpec);
            while (!interfaces.isEmpty()) {
                ClassNode origInterface = interfaces.remove(0);
                // ignore java.lang.Object; also methods added by Verifier for GroovyObject are already good enough
                if (!origInterface.equals(ClassHelper.OBJECT_TYPE) && !origInterface.equals(ClassHelper.GROOVY_OBJECT_TYPE)) {
                    updatedGenericsSpec = createGenericsSpec(origInterface, updatedGenericsSpec);
                    ClassNode correctedInterface = correctToGenericsSpecRecurse(updatedGenericsSpec, origInterface);
                    for (MethodNode nextMethod : correctedInterface.getMethods()) {
                        MethodNode correctedMethod = correctToGenericsSpec(genericsSpec, nextMethod);
                        MethodNode found = getDeclaredMethodCorrected(updatedGenericsSpec, correctedMethod, correctedInterface);
                        if (found != null) {
                            String td = methodDescriptorWithoutReturnType(found);
                            if (result.containsKey(td) && !result.get(td).isAbstract()) {
                                continue;
                            }
                            result.put(td, found);
                        }
                    }
                    interfaces.addAll(Arrays.asList(correctedInterface.getInterfaces()));
                }
            }
            ClassNode superClass = next.getUnresolvedSuperClass();
            if (superClass == null) {
                break;
            }
            next = correctToGenericsSpecRecurse(updatedGenericsSpec, superClass);
        }
        return result;
    }

    private static MethodNode getDeclaredMethodCorrected(Map<String, ClassNode> genericsSpec, MethodNode origMethod, ClassNode correctedClass) {
        for (MethodNode nameMatch : correctedClass.getDeclaredMethods(origMethod.getName())) {
            MethodNode correctedMethod = correctToGenericsSpec(genericsSpec, nameMatch);
            if (ParameterUtils.parametersEqual(correctedMethod.getParameters(), origMethod.getParameters())) {
                return correctedMethod;
            }
        }
        return null;
    }

    private BlockStatement methodBody(ClassNode exception, String message, ClosureExpression code, ClassNode returnType) {
        BlockStatement body = new BlockStatement();
        if (code != null) {
            body.addStatement(code.getCode());
        } else if (exception != null) {
            body.addStatement(throwS(ctorX(exception, message == null ? EMPTY_ARGUMENTS : constX(message))));
        } else {
            Expression result = getDefaultValueForPrimitive(returnType);
            if (result != null) {
                body.addStatement(returnS(result));
            }
        }
        return body;
    }
}
