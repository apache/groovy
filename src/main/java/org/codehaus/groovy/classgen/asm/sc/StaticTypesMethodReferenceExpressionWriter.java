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
package org.codehaus.groovy.classgen.asm.sc;

import groovy.lang.GroovyRuntimeException;
import groovy.lang.Tuple2;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.getWrapper;

/**
 * Writer responsible for generating method reference in statically compiled mode.
 * @since 3.0.0
 */
public class StaticTypesMethodReferenceExpressionWriter extends MethodReferenceExpressionWriter implements AbstractFunctionInterfaceWriter {
    public StaticTypesMethodReferenceExpressionWriter(WriterController controller) {
        super(controller);
    }

    @Override
    public void writeMethodReferenceExpression(MethodReferenceExpression methodReferenceExpression) {
        // TODO generate native method reference bytecode here
        ClassNode functionInterfaceType = getFunctionInterfaceType(methodReferenceExpression);
        ClassNode redirect = functionInterfaceType.redirect();

        MethodNode abstractMethodNode = ClassHelper.findSAM(redirect);

        String abstractMethodDesc = createMethodDescriptor(abstractMethodNode);

        ClassNode classNode = controller.getClassNode();
        boolean isInterface = classNode.isInterface();

        ClassNode mrExpressionType = methodReferenceExpression.getExpression().getType();
        String mrMethodName = methodReferenceExpression.getMethodName().getText();


        MethodNode mrMethodNode = findMrMethodNode(mrMethodName, createParametersWithExactType(abstractMethodNode, functionInterfaceType), mrExpressionType);

        if (null == mrMethodNode) {
            throw new GroovyRuntimeException("Failed to find the expected method[" + mrMethodName + "] in type[" + mrExpressionType.getName() + "]");
        }

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitInvokeDynamicInsn(
                abstractMethodNode.getName(),
                BytecodeHelper.getMethodDescriptor(redirect, Parameter.EMPTY_ARRAY),
                createBootstrapMethod(isInterface),
                createBootstrapMethodArguments(abstractMethodDesc, mrExpressionType, mrMethodNode, abstractMethodNode));

        controller.getOperandStack().push(redirect);
    }

    private Parameter[] createParametersWithExactType(MethodNode abstractMethodNode, ClassNode functionInterfaceType) {
        Tuple2<ClassNode[], ClassNode> abstractMethodNodeTypeInfo = GenericsUtils.parameterizeSAM(functionInterfaceType);
        ClassNode[] inferredParameterTypes = abstractMethodNodeTypeInfo.getV1();
        Parameter[] parameters = abstractMethodNode.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ClassNode inferredType = inferredParameterTypes[i];

            if (null == inferredType) {
                continue;
            }

            // Java 11 does not allow primitive type, we should use the wrapper type
            // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: int is not a subtype of class java.lang.Object
            ClassNode wrappedType = getWrapper(inferredType);

            parameter.setType(wrappedType);
            parameter.setOriginType(wrappedType);
        }

        return parameters;
    }

    private MethodNode findMrMethodNode(String mrMethodName, Parameter[] abstractMethodParameters, ClassNode mrExpressionType) {
        List<MethodNode> methodNodeList = mrExpressionType.getMethods(mrMethodName);
        ClassNode classNode = controller.getClassNode();

        MethodNode mrMethodNode = null;
        for (MethodNode mn : methodNodeList) {
            if (mn.isPrivate() && !mrExpressionType.getName().equals(classNode.getName())) {
                continue;
            }
            if ((mn.isPackageScope() || mn.isProtected()) && !mrExpressionType.getPackageName().equals(classNode.getPackageName())) {
                continue;
            }
            if (mn.isProtected() && !classNode.isDerivedFrom(mrExpressionType)) {
                continue;
            }

            if (mn.isStatic()) {
                if (ParameterUtils.parametersEqual(mn.getParameters(), abstractMethodParameters)) {
                    mrMethodNode = mn;
                    break;
                }
            } else {
                if (0 == abstractMethodParameters.length) {
                    break;
                }
                if (ParameterUtils.parametersEqual(mn.getParameters(), new ArrayList<>(Arrays.asList(abstractMethodParameters)).subList(1, abstractMethodParameters.length).toArray(Parameter.EMPTY_ARRAY))) {
                    mrMethodNode = mn;
                    break;
                }
            }
        }

        return mrMethodNode;
    }

    private Object[] createBootstrapMethodArguments(String abstractMethodDesc, ClassNode expressionType, MethodNode mrMethodNode, MethodNode abstractMethodNode) {
        return new Object[]{
                Type.getType(abstractMethodDesc),
                new Handle(
                        Opcodes.H_INVOKEVIRTUAL,
                        BytecodeHelper.getClassInternalName(expressionType.getTypeClass()),
                        mrMethodNode.getName(),
                        BytecodeHelper.getMethodDescriptor(mrMethodNode),
                        expressionType.isInterface()
                ),
                Type.getType(BytecodeHelper.getMethodDescriptor(abstractMethodNode.getReturnType(), abstractMethodNode.getParameters()))
        };
    }
}
