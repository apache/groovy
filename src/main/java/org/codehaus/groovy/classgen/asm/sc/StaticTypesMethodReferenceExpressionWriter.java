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
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodReferenceExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.ast.tools.ParameterUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.BytecodeVariable;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.MethodReferenceExpressionWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.ArrayTypeUtils;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.filterMethodsByVisibility;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS;

/**
 * Writer responsible for generating method reference in statically compiled mode.
 * @since 3.0.0
 */
public class StaticTypesMethodReferenceExpressionWriter extends MethodReferenceExpressionWriter implements AbstractFunctionInterfaceWriter {
    private static final String MR_EXPR_INSTANCE = "__MR_EXPR_INSTANCE";

    public StaticTypesMethodReferenceExpressionWriter(WriterController controller) {
        super(controller);
    }

    @Override
    public void writeMethodReferenceExpression(MethodReferenceExpression methodReferenceExpression) {
        ClassNode functionalInterfaceType = getFunctionalInterfaceType(methodReferenceExpression);
        ClassNode redirect = functionalInterfaceType.redirect();

        MethodNode abstractMethodNode = ClassHelper.findSAM(redirect);

        String abstractMethodDesc = createMethodDescriptor(abstractMethodNode);

        ClassNode classNode = controller.getClassNode();
        boolean isInterface = classNode.isInterface();

        Expression mrExpr = methodReferenceExpression.getExpression();
        ClassNode mrExprType = mrExpr.getType();
        String mrMethodName = methodReferenceExpression.getMethodName().getText();

        ClassNode[] methodReferenceParamTypes = methodReferenceExpression.getNodeMetaData(CLOSURE_ARGUMENTS);
        Parameter[] parametersWithExactType = createParametersWithExactType(abstractMethodNode, methodReferenceParamTypes);

        boolean isConstructorReference = isConstructorReference(mrMethodName);
        if (isConstructorReference) {
            mrMethodName = createSyntheticMethodForConstructorReference();
            addSyntheticMethodForConstructorReference(mrMethodName, mrExprType, parametersWithExactType);
        }

        MethodNode mrMethodNode = findMrMethodNode(mrMethodName, parametersWithExactType, mrExpr, isConstructorReference);

        if (null == mrMethodNode) {
            throw new GroovyRuntimeException("Failed to find the expected method["
                    + mrMethodName + "(" + Arrays.asList(parametersWithExactType) + ")] in type[" + mrExprType.getName() + "]");
        }

        mrMethodNode.putNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE, parametersWithExactType);
        MethodVisitor mv = controller.getMethodVisitor();

        boolean isClassExpr = isClassExpr(mrExpr);

        if (!isClassExpr) {
            if (isConstructorReference) {
                // TODO move the checking code to the Parrot parser
                throw new GroovyRuntimeException("Constructor reference must be className::new");
            }

            if (mrMethodNode.isStatic()) {
                ClassExpression classExpression = new ClassExpression(mrExprType);
                classExpression.setSourcePosition(mrExpr);
                mrExpr = classExpression;
            }

            if (mrExpr instanceof VariableExpression) {
                VariableExpression variableExpression = (VariableExpression) mrExpr;

                OperandStack operandStack = controller.getOperandStack();
                CompileStack compileStack = controller.getCompileStack();
                BytecodeVariable variable = compileStack.getVariable(variableExpression.getName(), true);

                operandStack.loadOrStoreVariable(variable, variableExpression.isUseReferenceDirectly());
            } else if (mrExpr instanceof ClassExpression) {
                // DO NOTHING
            } else {
                throw new GroovyBugError("TODO: " + mrExpr.getClass());
            }
        }

        mv.visitInvokeDynamicInsn(
                abstractMethodNode.getName(),
                createAbstractMethodDesc(functionalInterfaceType, mrExpr),
                createBootstrapMethod(isInterface),
                createBootstrapMethodArguments(
                        abstractMethodDesc,
                        mrMethodNode.isStatic() || isConstructorReference ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKEVIRTUAL,
                        isConstructorReference ? controller.getClassNode() : mrExprType,
                        mrMethodNode)
        );

        if (isClassExpr) {
            controller.getOperandStack().push(redirect);
        } else {
            controller.getOperandStack().replace(redirect, 1);
        }
    }

    private void addSyntheticMethodForConstructorReference(String syntheticMethodName, ClassNode returnType, Parameter[] parametersWithExactType) {
        ArgumentListExpression ctorArgs = args(parametersWithExactType);

        controller.getClassNode().addSyntheticMethod(
                syntheticMethodName,
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                returnType,
                parametersWithExactType,
                ClassNode.EMPTY_ARRAY,
                block(
                        returnS(
                                returnType.isArray()
                                        ?   new ArrayExpression(
                                                ClassHelper.make(ArrayTypeUtils.elementType(returnType.getTypeClass())),
                                                null,
                                                ctorArgs.getExpressions()
                                            )
                                        :   ctorX(returnType, ctorArgs)
                        )
                )
        );

    }

    private String createSyntheticMethodForConstructorReference() {
        return controller.getContext().getNextConstructorReferenceSyntheticMethodName(controller.getMethodNode());
    }

    private boolean isConstructorReference(String mrMethodName) {
        return "new".equals(mrMethodName);
    }

    private boolean isClassExpr(Expression mrExpr) {
        return mrExpr instanceof ClassExpression;
    }

    private String createAbstractMethodDesc(ClassNode functionalInterfaceType, Expression mrExpr) {
        List<Parameter> methodReferenceSharedVariableList = new LinkedList<>();

        if (!(isClassExpr(mrExpr))) {
            ClassNode mrExprInstanceType = mrExpr.getType();
            prependParameter(methodReferenceSharedVariableList, MR_EXPR_INSTANCE, mrExprInstanceType);
        }

        return BytecodeHelper.getMethodDescriptor(functionalInterfaceType.redirect(), methodReferenceSharedVariableList.toArray(Parameter.EMPTY_ARRAY));
    }

    private Parameter[] createParametersWithExactType(MethodNode abstractMethodNode, ClassNode[] inferredParameterTypes) {
        Parameter[] originalParameters = abstractMethodNode.getParameters();

        // We MUST clone the parameters to avoid impacting the original parameter type of SAM
        Parameter[] parameters = GeneralUtils.cloneParams(originalParameters);
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

    private MethodNode findMrMethodNode(String mrMethodName, Parameter[] abstractMethodParameters, Expression mrExpr, boolean isConstructorReference) {
        if (isConstructorReference) {
            return controller.getClassNode().getMethod(mrMethodName, abstractMethodParameters);
        }

        ClassNode mrExprType = mrExpr.getType();
        List<MethodNode> methodNodeList = mrExprType.getMethods(mrMethodName);
        ClassNode classNode = controller.getClassNode();

        MethodNode mrMethodNode = null;
        for (MethodNode mn : filterMethodsByVisibility(methodNodeList, classNode)) {

            if (mn.isStatic()) {
                if (ParameterUtils.parametersEqualWithWrapperType(mn.getParameters(), abstractMethodParameters)) {
                    mrMethodNode = mn;
                    break;
                }
            } else {
                if (0 == abstractMethodParameters.length) {
                    break;
                }

                Parameter[] parameters;
                if (isClassExpr(mrExpr)) {
                    parameters =
                            new ArrayList<>(Arrays.asList(abstractMethodParameters))
                                    .subList(1, abstractMethodParameters.length)
                                    .toArray(Parameter.EMPTY_ARRAY);
                } else {
                    parameters = abstractMethodParameters;
                }

                if (ParameterUtils.parametersEqualWithWrapperType(mn.getParameters(), parameters)) {
                    mrMethodNode = mn;
                    break;
                }
            }
        }

        return mrMethodNode;
    }
}