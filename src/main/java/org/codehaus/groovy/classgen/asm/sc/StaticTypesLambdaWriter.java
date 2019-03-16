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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.CompileStack;
import org.codehaus.groovy.classgen.asm.LambdaWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 */
public class StaticTypesLambdaWriter extends LambdaWriter implements AbstractFunctionInterfaceWriter {
    private static final String DO_CALL = "doCall";
    private static final String LAMBDA_SHARED_VARIABLES = "__LAMBDA_SHARED_VARIABLES";
    private static final String ENCLOSING_THIS = "__enclosing_this";
    private static final String LAMBDA_THIS = "__lambda_this";
    private static final String INIT = "<init>";
    private static final String IS_GENERATED_CONSTRUCTOR = "__IS_GENERATED_CONSTRUCTOR";
    private final StaticTypesClosureWriter staticTypesClosureWriter;
    private final WriterController controller;
    private final WriterControllerFactory factory;
    private final Map<Expression,ClassNode> lambdaClassMap = new HashMap<>();

    public StaticTypesLambdaWriter(WriterController wc) {
        super(wc);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(wc);
        this.controller = wc;
        this.factory = normalController -> controller;
    }

    @Override
    public void writeLambda(LambdaExpression expression) {
        ClassNode functionalInterfaceType = getFunctionalInterfaceType(expression);
        ClassNode redirect = functionalInterfaceType.redirect();

        if (null == functionalInterfaceType || !ClassHelper.isFunctionalInterface(redirect)) {
            // if the parameter type is not real FunctionInterface or failed to be inferred, generate the default bytecode, which is actually a closure
            super.writeLambda(expression);
            return;
        }

        MethodNode abstractMethodNode = ClassHelper.findSAM(redirect);
        String abstractMethodDesc = createMethodDescriptor(abstractMethodNode);

        ClassNode classNode = controller.getClassNode();
        boolean isInterface = classNode.isInterface();
        ClassNode lambdaWrapperClassNode = getOrAddLambdaClass(expression, ACC_PUBLIC | ACC_FINAL | (isInterface ? ACC_STATIC : 0) | ACC_SYNTHETIC, abstractMethodNode);
        MethodNode syntheticLambdaMethodNode = lambdaWrapperClassNode.getMethods(DO_CALL).get(0);

        newGroovyLambdaWrapperAndLoad(lambdaWrapperClassNode, syntheticLambdaMethodNode);

        loadEnclosingClassInstance();


        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        mv.visitInvokeDynamicInsn(
                abstractMethodNode.getName(),
                createAbstractMethodDesc(functionalInterfaceType, lambdaWrapperClassNode),
                createBootstrapMethod(isInterface),
                createBootstrapMethodArguments(abstractMethodDesc, Opcodes.H_INVOKEVIRTUAL, lambdaWrapperClassNode, syntheticLambdaMethodNode)
        );
        operandStack.replace(redirect, 2);
    }

    private void loadEnclosingClassInstance() {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall()) {
            operandStack.pushConstant(ConstantExpression.NULL);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
        }
    }

    private void newGroovyLambdaWrapperAndLoad(ClassNode lambdaWrapperClassNode, MethodNode syntheticLambdaMethodNode) {
        MethodVisitor mv = controller.getMethodVisitor();
        String lambdaWrapperClassInternalName = BytecodeHelper.getClassInternalName(lambdaWrapperClassNode);
        mv.visitTypeInsn(NEW, lambdaWrapperClassInternalName);
        mv.visitInsn(DUP);

        loadEnclosingClassInstance();
        loadEnclosingClassInstance();

        loadSharedVariables(syntheticLambdaMethodNode);

        List<ConstructorNode> constructorNodeList =
                lambdaWrapperClassNode.getDeclaredConstructors().stream()
                        .filter(e -> Boolean.TRUE.equals(e.getNodeMetaData(IS_GENERATED_CONSTRUCTOR)))
                        .collect(Collectors.toList());

        if (constructorNodeList.size() == 0) {
            throw new GroovyBugError("Failed to find the generated constructor");
        }

        ConstructorNode constructorNode = constructorNodeList.get(0);
        Parameter[] lambdaWrapperClassConstructorParameters = constructorNode.getParameters();
        mv.visitMethodInsn(INVOKESPECIAL, lambdaWrapperClassInternalName, INIT, BytecodeHelper.getMethodDescriptor(ClassHelper.VOID_TYPE, lambdaWrapperClassConstructorParameters), lambdaWrapperClassNode.isInterface());
        OperandStack operandStack = controller.getOperandStack();
        operandStack.replace(ClassHelper.CLOSURE_TYPE, lambdaWrapperClassConstructorParameters.length);
    }

    private Parameter[] loadSharedVariables(MethodNode syntheticLambdaMethodNode) {
        Parameter[] lambdaSharedVariableParameters = syntheticLambdaMethodNode.getNodeMetaData(LAMBDA_SHARED_VARIABLES);
        for (Parameter parameter : lambdaSharedVariableParameters) {
            String parameterName = parameter.getName();
            loadReference(parameterName, controller);
            if (parameter.getNodeMetaData(LambdaWriter.UseExistingReference.class) == null) {
                parameter.setNodeMetaData(LambdaWriter.UseExistingReference.class, Boolean.TRUE);
            }
        }

        return lambdaSharedVariableParameters;
    }

    private String createAbstractMethodDesc(ClassNode functionalInterfaceType, ClassNode lambdaClassNode) {
        List<Parameter> lambdaSharedVariableList = new LinkedList<>();

        prependEnclosingThis(lambdaSharedVariableList);
        prependParameter(lambdaSharedVariableList, LAMBDA_THIS, lambdaClassNode);

        return BytecodeHelper.getMethodDescriptor(functionalInterfaceType.redirect(), lambdaSharedVariableList.toArray(Parameter.EMPTY_ARRAY));
    }

    public ClassNode getOrAddLambdaClass(LambdaExpression expression, int mods, MethodNode abstractMethodNode) {
        ClassNode lambdaClass = lambdaClassMap.get(expression);
        if (lambdaClass == null) {
            lambdaClass = createLambdaClass(expression, mods, abstractMethodNode);
            lambdaClassMap.put(expression, lambdaClass);
            controller.getAcg().addInnerClass(lambdaClass);
            lambdaClass.addInterface(ClassHelper.GENERATED_LAMBDA_TYPE);
            lambdaClass.putNodeMetaData(WriterControllerFactory.class, factory);
        }
        lambdaClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        return lambdaClass;
    }

    protected ClassNode createLambdaClass(LambdaExpression expression, int mods, MethodNode abstractMethodNode) {
        ClassNode outerClass = controller.getOutermostClass();
        ClassNode classNode = controller.getClassNode();
        String name = genLambdaClassName();
        boolean staticMethodOrInStaticClass = controller.isStaticMethod() || classNode.isStaticClass();

        InnerClassNode answer = new InnerClassNode(classNode, name, mods, ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
        answer.setEnclosingMethod(controller.getMethodNode());
        answer.setSynthetic(true);
        answer.setUsingGenerics(outerClass.isUsingGenerics());
        answer.setSourcePosition(expression);

        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (controller.isInScriptBody()) {
            answer.setScriptBody(true);
        }

        MethodNode syntheticLambdaMethodNode = addSyntheticLambdaMethodNode(expression, answer, abstractMethodNode);
        Parameter[] localVariableParameters = syntheticLambdaMethodNode.getNodeMetaData(LAMBDA_SHARED_VARIABLES);

        addFieldsAndGettersForLocalVariables(answer, localVariableParameters);
        ConstructorNode constructorNode = addConstructor(expression, localVariableParameters, answer, createBlockStatementForConstructor(expression, outerClass, classNode));
        constructorNode.putNodeMetaData(IS_GENERATED_CONSTRUCTOR, Boolean.TRUE);

        Parameter enclosingThisParameter = syntheticLambdaMethodNode.getParameters()[0];
        new TransformationVisitor(answer, enclosingThisParameter).visitMethod(syntheticLambdaMethodNode);

        return answer;
    }

    private String genLambdaClassName() {
        ClassNode classNode = controller.getClassNode();
        ClassNode outerClass = controller.getOutermostClass();
        MethodNode methodNode = controller.getMethodNode();

        return classNode.getName() + "$"
                + controller.getContext().getNextLambdaInnerName(outerClass, classNode, methodNode);
    }

    private MethodNode addSyntheticLambdaMethodNode(LambdaExpression expression, InnerClassNode answer, MethodNode abstractMethodNode) {
        Parameter[] parametersWithExactType = createParametersWithExactType(expression); // expression.getParameters();
//        ClassNode returnType = expression.getNodeMetaData(StaticTypesMarker.INFERRED_RETURN_TYPE); //abstractMethodNode.getReturnType();
        Parameter[] localVariableParameters = getLambdaSharedVariables(expression);
        removeInitialValues(localVariableParameters);

        List<Parameter> methodParameterList = new LinkedList<>(Arrays.asList(parametersWithExactType));
        prependEnclosingThis(methodParameterList);

        MethodNode methodNode =
                answer.addMethod(
                        DO_CALL,
                        Opcodes.ACC_PUBLIC,
                        abstractMethodNode.getReturnType() /*ClassHelper.OBJECT_TYPE*/ /*returnType*/,
                        methodParameterList.toArray(Parameter.EMPTY_ARRAY),
                        ClassNode.EMPTY_ARRAY,
                        expression.getCode()
                );
        methodNode.putNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE, parametersWithExactType);
        methodNode.putNodeMetaData(LAMBDA_SHARED_VARIABLES, localVariableParameters);
        methodNode.setSourcePosition(expression);

        return methodNode;
    }

    private Parameter prependEnclosingThis(List<Parameter> methodParameterList) {
        return prependParameter(methodParameterList, ENCLOSING_THIS, controller.getClassNode().getPlainNodeReference());
    }

    private Parameter[] createParametersWithExactType(LambdaExpression expression) {
        Parameter[] parameters = expression.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        for (Parameter parameter : parameters) {
            ClassNode parameterType = parameter.getType();
            ClassNode inferredType = parameter.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);

            if (null == inferredType) {
                continue;
            }

            ClassNode type = convertParameterType(parameterType, inferredType);

            parameter.setType(type);
            parameter.setOriginType(type);
        }

        return parameters;
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        return staticTypesClosureWriter.createClosureClass(expression, mods);
    }

    private static final class TransformationVisitor extends ClassCodeVisitorSupport {
        private final CorrectAccessedVariableVisitor correctAccessedVariableVisitor;
        private final Parameter enclosingThisParameter;

        public TransformationVisitor(InnerClassNode icn, Parameter enclosingThisParameter) {
            this.correctAccessedVariableVisitor = new CorrectAccessedVariableVisitor(icn);
            this.enclosingThisParameter = enclosingThisParameter;
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            correctAccessedVariableVisitor.visitVariableExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(MethodCallExpression call) {
            if (!call.getMethodTarget().isStatic()) {
                Expression objectExpression = call.getObjectExpression();

                if (objectExpression instanceof VariableExpression) {
                    VariableExpression originalObjectExpression = (VariableExpression) objectExpression;
                    if (null == originalObjectExpression.getAccessedVariable()) {
                        VariableExpression thisVariable = new VariableExpression(enclosingThisParameter);
                        thisVariable.setSourcePosition(originalObjectExpression);

                        call.setObjectExpression(thisVariable);
                        call.setImplicitThis(false);
                    }
                }
            }

            super.visitMethodCallExpression(call);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }
}
