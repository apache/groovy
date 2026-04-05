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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.builder.AstStringCompiler;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
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
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.CLOSURE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GENERATED_LAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZABLE_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZEDLAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.CLOSURE_ARGUMENTS;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.PARAMETER_TYPE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;
import static org.objectweb.asm.Opcodes.H_INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 */
public class StaticTypesLambdaWriter extends LambdaWriter implements AbstractFunctionalInterfaceWriter {

    public StaticTypesLambdaWriter(final WriterController controller) {
        super(controller);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(controller);
        this.lambdaAnalyzer = new StaticTypesLambdaAnalyzer(controller.getSourceUnit());
    }

    @Override
    public void writeLambda(final LambdaExpression expression) {
        // functional interface target is required for native lambda generation
        ClassNode functionalType = expression.getNodeMetaData(PARAMETER_TYPE);
        MethodNode abstractMethod = resolveFunctionalInterfaceMethod(functionalType);
        if (abstractMethod == null) {
            // generate bytecode for closure
            super.writeLambda(expression);
            return;
        }

        boolean serializable = makeSerializableIfNeeded(expression, functionalType);
        GeneratedLambda generatedLambda = getOrAddGeneratedLambda(expression, abstractMethod);

        ensureDeserializeLambdaSupport(expression, generatedLambda, serializable);
        if (generatedLambda.isCapturing() && !isPreloadedLambdaReceiver(generatedLambda)) {
            loadLambdaReceiver(generatedLambda);
        }

        writeLambdaFactoryInvocation(functionalType.redirect(), abstractMethod, generatedLambda, serializable);
    }

    private static Parameter[] createDeserializeLambdaMethodParams() {
        return new Parameter[]{new Parameter(SERIALIZEDLAMBDA_TYPE, "serializedLambda")};
    }

    private static MethodNode resolveFunctionalInterfaceMethod(final ClassNode functionalType) {
        if (functionalType == null || !functionalType.isInterface()) {
            return null;
        }
        return ClassHelper.findSAM(functionalType);
    }

    private boolean makeSerializableIfNeeded(final LambdaExpression expression, final ClassNode functionalType) {
        if (!expression.isSerializable() && functionalType.implementsInterface(SERIALIZABLE_TYPE)) {
            expression.setSerializable(true);
        }
        return expression.isSerializable();
    }

    private void ensureDeserializeLambdaSupport(final LambdaExpression expression, final GeneratedLambda generatedLambda, final boolean serializable) {
        if (!serializable || hasDeserializeLambdaMethod(generatedLambda.lambdaClass)) {
            return;
        }

        addDeserializeLambdaMethodForLambdaExpression(expression, generatedLambda);
        addDeserializeLambdaMethod();
    }

    private void writeLambdaFactoryInvocation(final ClassNode functionalType, final MethodNode abstractMethod, final GeneratedLambda generatedLambda, final boolean serializable) {
        MethodVisitor mv = controller.getMethodVisitor();
        mv.visitInvokeDynamicInsn(
            abstractMethod.getName(),
            createLambdaFactoryMethodDescriptor(functionalType, generatedLambda),
            createBootstrapMethod(controller.getClassNode().isInterface(), serializable),
            createBootstrapMethodArguments(createMethodDescriptor(abstractMethod),
                generatedLambda.getMethodHandleKind(),
                generatedLambda.lambdaClass, generatedLambda.lambdaMethod, generatedLambda.lambdaMethod.getParameters(), serializable)
        );
        if (serializable) {
            mv.visitTypeInsn(CHECKCAST, "java/io/Serializable");
        }

        if (generatedLambda.nonCapturing()) {
            controller.getOperandStack().push(functionalType);
        } else {
            controller.getOperandStack().replace(functionalType, 1);
        }
    }

    private boolean hasDeserializeLambdaMethod(final ClassNode lambdaClass) {
        return controller.getClassNode().hasMethod(createDeserializeLambdaMethodName(lambdaClass), createDeserializeLambdaMethodParams());
    }

    private static MethodNode getLambdaMethod(final ClassNode lambdaClass) {
        List<MethodNode> lambdaMethods = lambdaClass.getMethods(DO_CALL);
        if (lambdaMethods.isEmpty()) {
            throw new GroovyBugError("Failed to find the synthetic lambda method in " + lambdaClass.getName());
        }
        return lambdaMethods.get(0);
    }

    private static ConstructorNode getGeneratedConstructor(final ClassNode lambdaClass) {
        for (ConstructorNode constructorNode : lambdaClass.getDeclaredConstructors()) {
            if (Boolean.TRUE.equals(constructorNode.getNodeMetaData(MetaDataKey.GENERATED_CONSTRUCTOR))) {
                return constructorNode;
            }
        }
        throw new GroovyBugError("Failed to find the generated constructor in " + lambdaClass.getName());
    }

    private boolean isPreloadedLambdaReceiver(final GeneratedLambda generatedLambda) {
        MethodNode enclosingMethod = controller.getMethodNode();
        return enclosingMethod != null
            && enclosingMethod.getNodeMetaData(MetaDataKey.PRELOADED_LAMBDA_RECEIVER) == generatedLambda.lambdaClass;
    }

    private void loadLambdaReceiver(final GeneratedLambda generatedLambda) {
        CompileStack compileStack = controller.getCompileStack();
        OperandStack operandStack = controller.getOperandStack();
        MethodVisitor mv = controller.getMethodVisitor();

        String lambdaClassInternalName = BytecodeHelper.getClassInternalName(generatedLambda.lambdaClass);
        mv.visitTypeInsn(NEW, lambdaClassInternalName);
        mv.visitInsn(DUP);

        if (controller.isStaticMethod() || compileStack.isInSpecialConstructorCall() || !generatedLambda.accessingInstanceMembers) {
            classX(controller.getThisType()).visit(controller.getAcg());
        } else {
            loadThis();
        }

        operandStack.dup();

        loadSharedVariables(generatedLambda.sharedVariables);

        Parameter[] lambdaClassConstructorParameters = generatedLambda.constructor.getParameters();
        mv.visitMethodInsn(INVOKESPECIAL, lambdaClassInternalName, "<init>", BytecodeHelper.getMethodDescriptor(VOID_TYPE, lambdaClassConstructorParameters), generatedLambda.lambdaClass.isInterface());

        operandStack.replace(CLOSURE_TYPE, lambdaClassConstructorParameters.length);
    }

    private void loadSharedVariables(final Parameter[] lambdaSharedVariableParameters) {
        for (Parameter parameter : lambdaSharedVariableParameters) {
            loadReference(parameter.getName(), controller);
            if (parameter.getNodeMetaData(UseExistingReference.class) == null) {
                parameter.setNodeMetaData(UseExistingReference.class, Boolean.TRUE);
            }
        }
    }

    private String createLambdaFactoryMethodDescriptor(final ClassNode functionalInterface, final GeneratedLambda generatedLambda) {
        if (generatedLambda.nonCapturing()) {
            return BytecodeHelper.getMethodDescriptor(functionalInterface, Parameter.EMPTY_ARRAY);
        }
        return BytecodeHelper.getMethodDescriptor(functionalInterface, new Parameter[]{createLambdaReceiverParameter(generatedLambda.lambdaClass)});
    }

    private static Parameter createLambdaReceiverParameter(final ClassNode lambdaClass) {
        Parameter parameter = new Parameter(lambdaClass, "__lambda_this");
        parameter.setClosureSharedVariable(false);
        return parameter;
    }

    private GeneratedLambda getOrAddGeneratedLambda(final LambdaExpression expression, final MethodNode abstractMethod) {
        return generatedLambdas.computeIfAbsent(expression, expr -> {
            ClassNode lambdaClass = createLambdaClass(expr, ACC_FINAL | ACC_PUBLIC | ACC_STATIC, abstractMethod);
            controller.getAcg().addInnerClass(lambdaClass);
            lambdaClass.addInterface(GENERATED_LAMBDA_TYPE);
            lambdaClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
            lambdaClass.putNodeMetaData(WriterControllerFactory.class, (WriterControllerFactory) x -> controller);
            MethodNode lambdaMethod = getLambdaMethod(lambdaClass);
            return new GeneratedLambda(
                lambdaClass,
                lambdaMethod,
                getGeneratedConstructor(lambdaClass),
                getStoredLambdaSharedVariables(expr),
                !requiresLambdaInstance(lambdaMethod),
                lambdaAnalyzer.accessesInstanceMembers(lambdaMethod)
            );
        });
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int modifiers) {
        return staticTypesClosureWriter.createClosureClass(expression, modifiers);
    }

    protected ClassNode createLambdaClass(final LambdaExpression expression, final int modifiers, final MethodNode abstractMethod) {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();

        InnerClassNode lambdaClass = new InnerClassNode(enclosingClass, nextLambdaClassName(), modifiers, CLOSURE_TYPE.getPlainNodeReference());
        lambdaClass.setEnclosingMethod(controller.getMethodNode());
        lambdaClass.setSourcePosition(expression);
        lambdaClass.setSynthetic(true);

        if (controller.isInScriptBody()) {
            lambdaClass.setScriptBody(true);
        }
        if (controller.isStaticMethod()
            || enclosingClass.isStaticClass()) {
            lambdaClass.setStaticClass(true);
        }
        if (expression.isSerializable()) {
            addSerialVersionUIDField(lambdaClass);
        }

        MethodNode syntheticLambdaMethodNode = addSyntheticLambdaMethodNode(expression, lambdaClass, abstractMethod);

        Parameter[] localVariableParameters = getStoredLambdaSharedVariables(expression);
        addFieldsForLocalVariables(lambdaClass, localVariableParameters);

        ConstructorNode constructorNode = addConstructor(expression, localVariableParameters, lambdaClass, createBlockStatementForConstructor(expression, outermostClass, enclosingClass));
        constructorNode.putNodeMetaData(MetaDataKey.GENERATED_CONSTRUCTOR, Boolean.TRUE);

        syntheticLambdaMethodNode.getCode().visit(new CorrectAccessedVariableVisitor(lambdaClass));

        return lambdaClass;
    }

    private String nextLambdaClassName() {
        ClassNode enclosingClass = controller.getClassNode();
        ClassNode outermostClass = controller.getOutermostClass();
        return enclosingClass.getName() + "$" + controller.getContext().getNextLambdaInnerName(outermostClass, enclosingClass, controller.getMethodNode());
    }

    private MethodNode addSyntheticLambdaMethodNode(final LambdaExpression expression, final ClassNode lambdaClass, final MethodNode abstractMethod) {
        Parameter[] parametersWithExactType = createParametersWithExactType(expression, abstractMethod);
        Parameter[] localVariableParameters = getLambdaSharedVariables(expression);
        removeInitialValues(localVariableParameters);

        expression.putNodeMetaData(MetaDataKey.STORED_LAMBDA_SHARED_VARIABLES, localVariableParameters);

        MethodNode doCallMethod = lambdaClass.addMethod(
            DO_CALL,
            ACC_PUBLIC,
            abstractMethod.getReturnType(),
            parametersWithExactType.clone(),
            ClassNode.EMPTY_ARRAY,
            expression.getCode()
        );
        doCallMethod.setSourcePosition(expression);
        if (lambdaAnalyzer.isNonCapturing(doCallMethod, localVariableParameters)) {
            lambdaAnalyzer.qualifyOuterStaticMemberReferences(doCallMethod);
            doCallMethod.setModifiers(doCallMethod.getModifiers() | ACC_STATIC);
        }
        return doCallMethod;
    }

    private Parameter[] createParametersWithExactType(final LambdaExpression expression, final MethodNode abstractMethod) {
        Parameter[] targetParameters = abstractMethod.getParameters();
        Parameter[] lambdaParameters = getParametersSafe(expression);
        ClassNode[] lambdaParamTypes = expression.getNodeMetaData(CLOSURE_ARGUMENTS);
        for (int i = 0, n = lambdaParameters.length; i < n; i += 1) {
            final Parameter lambdaParameter = lambdaParameters[i];
            ClassNode resolvedType = convertParameterType(targetParameters[i].getType(), lambdaParameter.getType(), lambdaParamTypes[i]);
            lambdaParameter.setType(resolvedType);
        }
        return lambdaParameters;
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

    private static boolean requiresLambdaInstance(final MethodNode lambdaMethod) {
        return 0 == (lambdaMethod.getModifiers() & ACC_STATIC);
    }

    private void addDeserializeLambdaMethodForLambdaExpression(final LambdaExpression expression, final GeneratedLambda generatedLambda) {
        ClassNode enclosingClass = controller.getClassNode();
        Statement code;
        if (generatedLambda.nonCapturing()) {
            code = block(returnS(expression));
        } else {
            code = block(
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
                        mv.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(generatedLambda.lambdaClass));
                        OperandStack operandStack = controller.getOperandStack();
                        operandStack.push(generatedLambda.lambdaClass);
                    }
                }),
                returnS(expression)
            );
        }

        MethodNode deserializeLambdaMethod = enclosingClass.addSyntheticMethod(
            createDeserializeLambdaMethodName(generatedLambda.lambdaClass),
            ACC_PUBLIC | ACC_STATIC,
            OBJECT_TYPE,
            createDeserializeLambdaMethodParams(),
            ClassNode.EMPTY_ARRAY,
            code);
        if (generatedLambda.isCapturing()) {
            // The deserialize helper preloads the captured receiver before it reuses the original lambda expression.
            deserializeLambdaMethod.putNodeMetaData(MetaDataKey.PRELOADED_LAMBDA_RECEIVER, generatedLambda.lambdaClass);
        }
    }

    private static String createDeserializeLambdaMethodName(final ClassNode lambdaClass) {
        return "$deserializeLambda_" + lambdaClass.getName().replace('.', '$') + "$";
    }

    private static Parameter[] getStoredLambdaSharedVariables(final LambdaExpression expression) {
        Parameter[] sharedVariables = expression.getNodeMetaData(MetaDataKey.STORED_LAMBDA_SHARED_VARIABLES);
        if (sharedVariables == null) {
            throw new GroovyBugError("Failed to find shared variables for lambda expression");
        }
        return sharedVariables;
    }

    private enum MetaDataKey {
        GENERATED_CONSTRUCTOR,
        STORED_LAMBDA_SHARED_VARIABLES,
        PRELOADED_LAMBDA_RECEIVER
    }

    private record GeneratedLambda(ClassNode lambdaClass, MethodNode lambdaMethod, ConstructorNode constructor,
                                   Parameter[] sharedVariables, boolean nonCapturing,
                                   boolean accessingInstanceMembers) {

        private boolean isCapturing() {
            return !nonCapturing;
        }

        private int getMethodHandleKind() {
            return nonCapturing ? H_INVOKESTATIC : H_INVOKEVIRTUAL;
        }
    }

    private static final String DO_CALL = "doCall";
    private final Map<LambdaExpression, GeneratedLambda> generatedLambdas = new HashMap<>();
    private final StaticTypesClosureWriter staticTypesClosureWriter;
    private final StaticTypesLambdaAnalyzer lambdaAnalyzer;
}
