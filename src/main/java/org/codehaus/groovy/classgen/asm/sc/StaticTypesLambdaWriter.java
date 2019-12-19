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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.builder.AstStringCompiler;
import org.codehaus.groovy.ast.expr.ClosureExpression;
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
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.MethodVisitor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GENERATED_LAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZEDLAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.findSAM;
import static org.codehaus.groovy.ast.ClassHelper.isGeneratedFunction;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
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
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 */
public class StaticTypesLambdaWriter extends LambdaWriter implements AbstractFunctionalInterfaceWriter {

    private static final String IS_GENERATED_CONSTRUCTOR = "__IS_GENERATED_CONSTRUCTOR";
    private static final String LAMBDA_SHARED_VARIABLES = "__LAMBDA_SHARED_VARIABLES";

    private final StaticTypesClosureWriter staticTypesClosureWriter;
    private final Map<Expression, ClassNode> lambdaClassNodes = new HashMap<>();

    public StaticTypesLambdaWriter(final WriterController controller) {
        super(controller);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(controller);
    }

    @Override
    public void writeLambda(final LambdaExpression expression) {
        ClassNode functionalInterface = getFunctionalInterfaceType(expression);
        if (functionalInterface == null || !functionalInterface.isInterface()) {
            super.writeLambda(expression);
            return;
        }

        MethodNode abstractMethod = findSAM(functionalInterface.redirect());
        if (abstractMethod == null) {
            super.writeLambda(expression);
            return;
        }

        if (!expression.isSerializable() && functionalInterface.implementsInterface(SERIALIZABLE_TYPE)) {
            expression.setSerializable(true);
        }

        ClassNode enclosingClass = controller.getClassNode();
        int modifiers = ACC_FINAL | ACC_PUBLIC | ACC_SYNTHETIC;
        if (enclosingClass.isInterface()) modifiers |= ACC_STATIC;
        ClassNode lambdaClass = getOrAddLambdaClass(expression, modifiers, abstractMethod);
        MethodNode lambdaMethod = lambdaClass.getMethods("doCall").get(0);

        boolean canDeserialize = enclosingClass.hasMethod(createDeserializeLambdaMethodName(lambdaClass), createDeserializeLambdaMethodParams());
        if (!canDeserialize) {
            if (expression.isSerializable()) {
                addDeserializeLambdaMethodForEachLambdaExpression(expression, lambdaClass);
                addDeserializeLambdaMethod();
            }
            newGroovyLambdaWrapperAndLoad(lambdaClass, expression, isAccessingInstanceMembersOfEnclosingClass(lambdaMethod));
        }

        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitInvokeDynamicInsn(
                abstractMethod.getName(),
                createAbstractMethodDesc(functionalInterface.redirect(), lambdaClass),
                createBootstrapMethod(enclosingClass.isInterface(), expression.isSerializable()),
                createBootstrapMethodArguments(createMethodDescriptor(abstractMethod), H_INVOKEVIRTUAL, lambdaClass, lambdaMethod, expression.isSerializable())
        );
        if (expression.isSerializable()) {
            mv.visitTypeInsn(CHECKCAST, "java/io/Serializable");
        }

        OperandStack operandStack = controller.getOperandStack();
        operandStack.replace(functionalInterface.redirect(), 1);
    }

    private static Parameter[] createDeserializeLambdaMethodParams() {
        return new Parameter[]{new Parameter(SERIALIZEDLAMBDA_TYPE, "serializedLambda")};
    }

    private static boolean isAccessingInstanceMembersOfEnclosingClass(final MethodNode syntheticLambdaMethodNode) {
        boolean[] result = new boolean[1];

        GroovyCodeVisitor visitor = new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(final VariableExpression expression) {
                if (expression.isThisExpression()) {
                    result[0] = true;
                }
            }
        };
        syntheticLambdaMethodNode.getCode().visit(visitor);

        return result[0];
    }

    private void newGroovyLambdaWrapperAndLoad(final ClassNode lambdaClass, final LambdaExpression expression, final boolean accessingInstanceMembers) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        String lambdaClassInternalName = BytecodeHelper.getClassInternalName(lambdaClass);
        mv.visitTypeInsn(NEW, lambdaClassInternalName);
        mv.visitInsn(DUP);

        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall() || !accessingInstanceMembers) {
            ClassNode classNode = controller.getClassNode();
            while (isGeneratedFunction(classNode)) {
                classNode = classNode.getOuterClass();
            }
            classX(classNode).visit(controller.getAcg());
        } else {
            loadThis();
        }

        operandStack.dup();

        loadSharedVariables(expression);

        Optional<ConstructorNode> generatedConstructor = lambdaClass.getDeclaredConstructors().stream()
                .filter(ctor -> Boolean.TRUE.equals(ctor.getNodeMetaData(IS_GENERATED_CONSTRUCTOR))).findFirst();
        if (!generatedConstructor.isPresent()) {
            throw new GroovyBugError("Failed to find the generated constructor");
        }

        Parameter[] lambdaClassConstructorParameters = generatedConstructor.get().getParameters();
        mv.visitMethodInsn(INVOKESPECIAL, lambdaClassInternalName, "<init>", BytecodeHelper.getMethodDescriptor(VOID_TYPE, lambdaClassConstructorParameters), lambdaClass.isInterface());

        operandStack.replace(CLOSURE_TYPE, lambdaClassConstructorParameters.length);
    }

    private Parameter[] loadSharedVariables(final LambdaExpression expression) {
        Parameter[] lambdaSharedVariableParameters = expression.getNodeMetaData(LAMBDA_SHARED_VARIABLES);

        for (Parameter parameter : lambdaSharedVariableParameters) {
            loadReference(parameter.getName(), controller);
            if (parameter.getNodeMetaData(UseExistingReference.class) == null) {
                parameter.setNodeMetaData(UseExistingReference.class, Boolean.TRUE);
            }
        }

        return lambdaSharedVariableParameters;
    }

    private String createAbstractMethodDesc(final ClassNode functionalInterface, final ClassNode lambdaClass) {
        List<Parameter> lambdaSharedVariables = new LinkedList<>();
        prependParameter(lambdaSharedVariables, "__lambda_this", lambdaClass);
        return BytecodeHelper.getMethodDescriptor(functionalInterface, lambdaSharedVariables.toArray(Parameter.EMPTY_ARRAY));
    }

    private ClassNode getOrAddLambdaClass(final LambdaExpression expression, final int modifiers, final MethodNode abstractMethod) {
        return lambdaClassNodes.computeIfAbsent(expression, key -> {
            ClassNode lambdaClass = createLambdaClass(expression, modifiers, abstractMethod);
            controller.getAcg().addInnerClass(lambdaClass);
            lambdaClass.addInterface(GENERATED_LAMBDA_TYPE);
            lambdaClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
            lambdaClass.putNodeMetaData(WriterControllerFactory.class, (WriterControllerFactory) x -> controller);
            return lambdaClass;
        });
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int modifiers) {
        return staticTypesClosureWriter.createClosureClass(expression, modifiers);
    }

    protected ClassNode createLambdaClass(final LambdaExpression expression, final int modifiers, final MethodNode abstractMethod) {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();
        boolean staticMethodOrInStaticClass = (controller.isStaticMethod() || enclosingClass.isStaticClass());

        InnerClassNode lambdaClass = new InnerClassNode(enclosingClass, nextLambdaClassName(), modifiers, CLOSURE_TYPE.getPlainNodeReference());
      //lambdaClass.setUsingGenerics(outermostClass.isUsingGenerics());
        lambdaClass.setEnclosingMethod(controller.getMethodNode());
        lambdaClass.setSourcePosition(expression);
        lambdaClass.setSynthetic(true);

        if (controller.isInScriptBody()) {
            lambdaClass.setScriptBody(true);
        }
        if (staticMethodOrInStaticClass) {
            lambdaClass.setStaticClass(true);
        }
        if (expression.isSerializable()) {
            addSerialVersionUIDField(lambdaClass);
        }

        MethodNode syntheticLambdaMethodNode = addSyntheticLambdaMethodNode(expression, lambdaClass, abstractMethod);
        Parameter[] localVariableParameters = expression.getNodeMetaData(LAMBDA_SHARED_VARIABLES);

        addFieldsAndGettersForLocalVariables(lambdaClass, localVariableParameters);
        ConstructorNode constructorNode = addConstructor(expression, localVariableParameters, lambdaClass, createBlockStatementForConstructor(expression, outermostClass, enclosingClass));
        constructorNode.putNodeMetaData(IS_GENERATED_CONSTRUCTOR, Boolean.TRUE);

        syntheticLambdaMethodNode.getCode().visit(new CorrectAccessedVariableVisitor(lambdaClass));

        return lambdaClass;
    }

    private String nextLambdaClassName() {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();
        return enclosingClass.getName() + "$" + controller.getContext().getNextLambdaInnerName(outermostClass, enclosingClass, controller.getMethodNode());
    }

    private static void addSerialVersionUIDField(final ClassNode lambdaClass) {
        lambdaClass.addFieldFirst("serialVersionUID", ACC_PRIVATE | ACC_STATIC | ACC_FINAL, long_TYPE, constX(-1L, true));
    }

    private MethodNode addSyntheticLambdaMethodNode(final LambdaExpression expression, final ClassNode lambdaClass, final MethodNode abstractMethod) {
        Parameter[] parametersWithExactType = createParametersWithExactType(expression);
        Parameter[] localVariableParameters = getLambdaSharedVariables(expression);
        removeInitialValues(localVariableParameters);

        MethodNode doCallMethod = lambdaClass.addMethod(
                "doCall",
                ACC_PUBLIC,
                abstractMethod.getReturnType(),
                Arrays.copyOf(parametersWithExactType, parametersWithExactType.length),
                ClassNode.EMPTY_ARRAY,
                expression.getCode()
        );
        doCallMethod.putNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE, parametersWithExactType);
        expression.putNodeMetaData(LAMBDA_SHARED_VARIABLES, localVariableParameters);
        doCallMethod.setSourcePosition(expression);

        return doCallMethod;
    }

    private Parameter[] createParametersWithExactType(final LambdaExpression expression) {
        Parameter[] parameters = expression.getParameters();
        if (parameters == null) {
            parameters = Parameter.EMPTY_ARRAY;
        }

        for (Parameter parameter : parameters) {
            ClassNode inferredType = parameter.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
            if (inferredType == null) {
                continue;
            }

            ClassNode type = convertParameterType(parameter.getType(), inferredType);

            parameter.setType(type);
            parameter.setOriginType(type);
        }

        return parameters;
    }

    private void addDeserializeLambdaMethod() {
        ClassNode enclosingClass = controller.getClassNode();
        Parameter[] parameters = createDeserializeLambdaMethodParams();
        if (enclosingClass.hasMethod("$deserializeLambda$", parameters)) {
            return;
        }

        Statement code = block(
                declS(localVarX("enclosingClass", OBJECT_TYPE), classX(enclosingClass)),
                ((BlockStatement) new AstStringCompiler().compile(
                        "return enclosingClass" +
                                ".getDeclaredMethod(\"\\$deserializeLambda_${serializedLambda.getImplClass().replace('/', '$')}\\$\", serializedLambda.getClass())" +
                                ".invoke(null, serializedLambda)"
                ).get(0)).getStatements().get(0)
        );

        enclosingClass.addSyntheticMethod(
                "$deserializeLambda$",
                ACC_PRIVATE | ACC_STATIC,
                OBJECT_TYPE,
                parameters,
                ClassNode.EMPTY_ARRAY,
                code);
    }

    private void addDeserializeLambdaMethodForEachLambdaExpression(final LambdaExpression expression, final ClassNode lambdaClass) {
        ClassNode enclosingClass = controller.getClassNode();
        Statement code = block(
                new BytecodeSequence(new BytecodeInstruction() {
                    @Override
                    public void visit(final MethodVisitor mv) {
                        mv.visitVarInsn(ALOAD, 0);
                        mv.visitInsn(ICONST_0);
                        mv.visitMethodInsn(
                                INVOKEVIRTUAL,
                                "java/lang/invoke/SerializedLambda",
                                "getCapturedArg",
                                "(I)Ljava/lang/Object;",
                                false);
                        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(lambdaClass));
                        OperandStack operandStack = controller.getOperandStack();
                        operandStack.push(lambdaClass);
                    }
                }),
                returnS(expression)
        );

        enclosingClass.addSyntheticMethod(
                createDeserializeLambdaMethodName(lambdaClass),
                ACC_PUBLIC | ACC_STATIC,
                OBJECT_TYPE,
                createDeserializeLambdaMethodParams(),
                ClassNode.EMPTY_ARRAY,
                code);
    }

    private static String createDeserializeLambdaMethodName(final ClassNode lambdaClass) {
        return "$deserializeLambda_" + lambdaClass.getName().replace('.', '$') + "$";
    }
}
