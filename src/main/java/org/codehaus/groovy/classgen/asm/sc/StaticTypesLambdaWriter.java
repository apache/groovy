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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.LambdaWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.WriterControllerFactory;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.classgen.asm.sc.StaticInvocationWriter.PARAMETER_TYPE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 *
 */
public class StaticTypesLambdaWriter extends LambdaWriter {
    public static final String DO_CALL = "doCall";
    private StaticTypesClosureWriter staticTypesClosureWriter;
    private WriterController controller;
    private WriterControllerFactory factory;
    private final Map<Expression,ClassNode> lambdaClassMap = new HashMap<>();

    public StaticTypesLambdaWriter(WriterController wc) {
        super(wc);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(wc);
        this.controller = wc;
        this.factory = new WriterControllerFactory() {
            public WriterController makeController(final WriterController normalController) {
                return controller;
            }
        };
    }

    @Override
    public void writeLambda(LambdaExpression expression) {
        ClassNode parameterType = expression.getNodeMetaData(PARAMETER_TYPE);

        List<MethodNode> abstractMethodNodeList =
                parameterType.redirect().getMethods().stream()
                        .filter(MethodNode::isAbstract)
                        .collect(Collectors.toList());

        if (!(isFunctionInterface(parameterType) && abstractMethodNodeList.size() == 1)) {
            super.writeLambda(expression);
            return;
        }

        MethodNode abstractMethodNode = abstractMethodNodeList.get(0);
        String abstractMethodName = abstractMethodNode.getName();
        String abstractMethodDesc = "()L" + parameterType.redirect().getPackageName().replace('.', '/') + "/" + parameterType.redirect().getNameWithoutPackage() + ";";


        MethodVisitor mv = controller.getMethodVisitor();
        ClassNode lambdaEnclosingClassNode = getOrAddLambdaClass(expression, ACC_PUBLIC, abstractMethodNode);
        MethodNode syntheticLambdaMethodNode = lambdaEnclosingClassNode.getMethods(DO_CALL).get(0);
        String syntheticLambdaMethodWithExactTypeDesc = BytecodeHelper.getMethodDescriptor(syntheticLambdaMethodNode);
        String syntheticLambdaMethodDesc =
                BytecodeHelper.getMethodDescriptor(
                        abstractMethodNode.getReturnType().getTypeClass(),
                        Arrays.stream(abstractMethodNode.getParameters())
                                .map(e -> e.getType().getTypeClass())
                                .toArray(Class[]::new)
                );

        controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);

        mv.visitInvokeDynamicInsn(
                abstractMethodName,
                abstractMethodDesc,
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        "java/lang/invoke/LambdaMetafactory",
                        "metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                        false
                ),
                new Object[]{
                        Type.getType(syntheticLambdaMethodDesc),
                        new Handle(
                                Opcodes.H_INVOKESTATIC,
                                lambdaEnclosingClassNode.getName(),
                                syntheticLambdaMethodNode.getName(),
                                syntheticLambdaMethodWithExactTypeDesc,
                                false
                        ),
                        Type.getType(syntheticLambdaMethodWithExactTypeDesc)
                }
        );
    }

    private boolean isFunctionInterface(ClassNode parameterType) {
        return parameterType.redirect().isInterface() && !parameterType.redirect().getAnnotations(ClassHelper.FunctionalInterface_Type).isEmpty();
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
        return lambdaClass;
    }

    protected ClassNode createLambdaClass(LambdaExpression expression, int mods, MethodNode abstractMethodNode) {
        ClassNode outerClass = controller.getOutermostClass();
        ClassNode classNode = controller.getClassNode();
        String name = genLambdaClassName();
        boolean staticMethodOrInStaticClass = controller.isStaticMethod() || classNode.isStaticClass();

        InnerClassNode answer = new InnerClassNode(classNode, name, mods, ClassHelper.LAMBDA_TYPE.getPlainNodeReference());
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

        Parameter[] parametersWithExactType = createParametersWithExactType(expression); // expression.getParameters();

        MethodNode methodNode =
                answer.addMethod(DO_CALL, Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC, abstractMethodNode.getReturnType(), parametersWithExactType, ClassNode.EMPTY_ARRAY, expression.getCode());
        methodNode.setSourcePosition(expression);

        return answer;
    }

    private Parameter[] createParametersWithExactType(LambdaExpression expression) {
        Parameter[] parameters = expression.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        ClassNode[] blockParameterTypes = expression.getNodeMetaData(org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS);
//        Parameter[] parametersWithExactType = new Parameter[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            parameters[i].setType(blockParameterTypes[i]);
            parameters[i].setOriginType(blockParameterTypes[i]);
        }
        return parameters;
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        return staticTypesClosureWriter.createClosureClass(expression, mods);
    }

    private String genLambdaClassName() {
        ClassNode classNode = controller.getClassNode();
        ClassNode outerClass = controller.getOutermostClass();
        MethodNode methodNode = controller.getMethodNode();

        return classNode.getName() + "$"
                + controller.getContext().getNextLambdaInnerName(outerClass, classNode, methodNode);
    }
}
