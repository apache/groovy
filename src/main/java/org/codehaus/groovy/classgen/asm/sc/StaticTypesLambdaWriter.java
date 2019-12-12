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

import org.apache.groovy.util.ObjectHolder;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.builder.AstStringCompiler;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.BytecodeInstruction;
import org.codehaus.groovy.classgen.BytecodeSequence;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.ast.ClassHelper.SERIALIZABLE_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 */
public class StaticTypesLambdaWriter extends LambdaWriter implements AbstractFunctionalInterfaceWriter {
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
        if (null == functionalInterfaceType) {
            // if the parameter type failed to be inferred, generate the default bytecode, which is actually a closure
            super.writeLambda(expression);
            return;
        }

        ClassNode redirect = functionalInterfaceType.redirect();
        if (!ClassHelper.isFunctionalInterface(redirect)) {
            // if the parameter type is not real FunctionalInterface, generate the default bytecode, which is actually a closure
            super.writeLambda(expression);
            return;
        }

        boolean implementsSerializable = functionalInterfaceType.implementsInterface(SERIALIZABLE_TYPE);
        expression.setSerializable(expression.isSerializable() || implementsSerializable);

        MethodNode abstractMethodNode = ClassHelper.findSAM(redirect);
        String abstractMethodDesc = createMethodDescriptor(abstractMethodNode);

        ClassNode classNode = controller.getClassNode();

        boolean isInterface = classNode.isInterface();
        ClassNode lambdaWrapperClassNode = getOrAddLambdaClass(expression, ACC_PUBLIC | ACC_FINAL | (isInterface ? ACC_STATIC : 0) | ACC_SYNTHETIC, abstractMethodNode);
        MethodNode syntheticLambdaMethodNode = lambdaWrapperClassNode.getMethods(DO_CALL).get(0);

        boolean canDeserialize = classNode.hasMethod(createDeserializeLambdaMethodName(lambdaWrapperClassNode), createDeserializeLambdaMethodParams());

        if (!canDeserialize) {
            if (expression.isSerializable()) {
                addDeserializeLambdaMethodForEachLambdaExpression(expression, lambdaWrapperClassNode);
                addDeserializeLambdaMethod();
            }

            boolean accessingInstanceMembers = isAccessingInstanceMembersOfEnclosingClass(syntheticLambdaMethodNode);
            newGroovyLambdaWrapperAndLoad(lambdaWrapperClassNode, expression, accessingInstanceMembers);
            loadEnclosingClassInstance(accessingInstanceMembers);
        }

        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();

        mv.visitInvokeDynamicInsn(
                abstractMethodNode.getName(),
                createAbstractMethodDesc(functionalInterfaceType, lambdaWrapperClassNode),
                createBootstrapMethod(isInterface, expression.isSerializable()),
                createBootstrapMethodArguments(abstractMethodDesc, H_INVOKEVIRTUAL, lambdaWrapperClassNode, syntheticLambdaMethodNode, expression.isSerializable())
        );

        if (expression.isSerializable()) {
            mv.visitTypeInsn(CHECKCAST, "java/io/Serializable");
        }

        operandStack.replace(redirect, 2);
    }

    private Parameter[] createDeserializeLambdaMethodParams() {
        return new Parameter[]{new Parameter(ClassHelper.SERIALIZEDLAMBDA_TYPE, SERIALIZED_LAMBDA_PARAM_NAME)};
    }

    private void loadEnclosingClassInstance(boolean accessingInstanceMembers) {
        MethodVisitor mv = controller.getMethodVisitor();
        OperandStack operandStack = controller.getOperandStack();
        CompileStack compileStack = controller.getCompileStack();

        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall() || !accessingInstanceMembers) {
            operandStack.pushConstant(ConstantExpression.NULL);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            operandStack.push(controller.getClassNode());
        }
    }

    private boolean isAccessingInstanceMembersOfEnclosingClass(MethodNode syntheticLambdaMethodNode) {
        ObjectHolder<Boolean> objectHolder = new ObjectHolder<>(false);
        ClassCodeVisitorSupport classCodeVisitorSupport = new ClassCodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                if (expression.isThisExpression()) {
                    objectHolder.setObject(true);
                }
            }

            @Override
            protected SourceUnit getSourceUnit() {
                return null;
            }
        };

        classCodeVisitorSupport.visitMethod(syntheticLambdaMethodNode);

        return objectHolder.getObject();
    }

    private void newGroovyLambdaWrapperAndLoad(ClassNode lambdaWrapperClassNode, LambdaExpression expression, boolean accessingInstanceMembers) {
        MethodVisitor mv = controller.getMethodVisitor();
        String lambdaWrapperClassInternalName = BytecodeHelper.getClassInternalName(lambdaWrapperClassNode);
        mv.visitTypeInsn(NEW, lambdaWrapperClassInternalName);
        mv.visitInsn(DUP);

        loadEnclosingClassInstance(accessingInstanceMembers);
        controller.getOperandStack().dup();

        loadSharedVariables(expression);

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

    private Parameter[] loadSharedVariables(LambdaExpression expression) {
        Parameter[] lambdaSharedVariableParameters = expression.getNodeMetaData(LAMBDA_SHARED_VARIABLES);
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

        if (expression.isSerializable()) {
            addSerialVersionUIDField(answer);
        }

        if (staticMethodOrInStaticClass) {
            answer.setStaticClass(true);
        }
        if (controller.isInScriptBody()) {
            answer.setScriptBody(true);
        }

        MethodNode syntheticLambdaMethodNode = addSyntheticLambdaMethodNode(expression, answer, abstractMethodNode);
        Parameter[] localVariableParameters = expression.getNodeMetaData(LAMBDA_SHARED_VARIABLES);

        addFieldsAndGettersForLocalVariables(answer, localVariableParameters);
        ConstructorNode constructorNode = addConstructor(expression, localVariableParameters, answer, createBlockStatementForConstructor(expression, outerClass, classNode));
        constructorNode.putNodeMetaData(IS_GENERATED_CONSTRUCTOR, Boolean.TRUE);

        new LambdaBodyTransformationVisitor(answer).visitMethod(syntheticLambdaMethodNode);

        return answer;
    }

    private void addSerialVersionUIDField(InnerClassNode answer) {
        answer.addFieldFirst("serialVersionUID", ACC_PRIVATE | ACC_STATIC | ACC_FINAL, ClassHelper.long_TYPE, new ConstantExpression(-1L, true));
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
                        ACC_PUBLIC,
                        abstractMethodNode.getReturnType() /*ClassHelper.OBJECT_TYPE*/ /*returnType*/,
                        methodParameterList.toArray(Parameter.EMPTY_ARRAY),
                        ClassNode.EMPTY_ARRAY,
                        expression.getCode()
                );
        methodNode.putNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE, parametersWithExactType);
        expression.putNodeMetaData(LAMBDA_SHARED_VARIABLES, localVariableParameters);
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

    private static final String SERIALIZED_LAMBDA_PARAM_NAME = "serializedLambda";
    private static final String DESERIALIZE_LAMBDA_METHOD_NAME = "$deserializeLambda$";
    private void addDeserializeLambdaMethod() {
        ClassNode classNode = controller.getClassNode();
        Parameter[] parameters = createDeserializeLambdaMethodParams();
        if (classNode.hasMethod(DESERIALIZE_LAMBDA_METHOD_NAME, parameters)) {
            return;
        }
        Statement code = block(
                declS(localVarX("enclosingClass", ClassHelper.DYNAMIC_TYPE), new ClassExpression(classNode)),
                ((BlockStatement) new AstStringCompiler().compile(
                        "return enclosingClass" +
                                ".getDeclaredMethod(\"\\$deserializeLambda_${serializedLambda.getImplClass().replace('/', '$')}\\$\", serializedLambda.getClass())" +
                                ".invoke(null, serializedLambda)"
                ).get(0)).getStatements().get(0)
        );

        classNode.addSyntheticMethod(
                DESERIALIZE_LAMBDA_METHOD_NAME,
                ACC_PRIVATE | ACC_STATIC,
                ClassHelper.OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                code);
    }

    private void addDeserializeLambdaMethodForEachLambdaExpression(LambdaExpression lambdaExpression, ClassNode lambdaWrapperClassNode) {
        ClassNode classNode = controller.getClassNode();
        Statement code = block(
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(MethodVisitor mv) {
                        callGetCapturedArg(mv, ICONST_0, lambdaWrapperClassNode);
                        callGetCapturedArg(mv, ICONST_1, classNode);
                    }

                    private void callGetCapturedArg(MethodVisitor mv, int capturedArgIndex, ClassNode resultType) {
                        OperandStack operandStack = controller.getOperandStack();

                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(capturedArgIndex);
                        mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "java/lang/invoke/SerializedLambda",
                                "getCapturedArg",
                                "(I)Ljava/lang/Object;",
                                false);
                        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(resultType));
                        operandStack.push(resultType);
                    }
                }),
                returnS(lambdaExpression)
        );

        classNode.addSyntheticMethod(
                createDeserializeLambdaMethodName(lambdaWrapperClassNode),
                ACC_PUBLIC | ACC_STATIC,
                ClassHelper.OBJECT_TYPE,
                createDeserializeLambdaMethodParams(),
                ClassNode.EMPTY_ARRAY,
                code);
    }

    private String createDeserializeLambdaMethodName(ClassNode lambdaWrapperClassNode) {
        return "$deserializeLambda_" + lambdaWrapperClassNode.getName().replace('.', '$') + "$";
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        return staticTypesClosureWriter.createClosureClass(expression, mods);
    }

    private static final class LambdaBodyTransformationVisitor extends ClassCodeVisitorSupport {
        private final CorrectAccessedVariableVisitor correctAccessedVariableVisitor;

        public LambdaBodyTransformationVisitor(InnerClassNode icn) {
            this.correctAccessedVariableVisitor = new CorrectAccessedVariableVisitor(icn);
        }

        @Override
        public void visitVariableExpression(VariableExpression expression) {
            correctAccessedVariableVisitor.visitVariableExpression(expression);
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }
    }
}
