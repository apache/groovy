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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.tools.GeneralUtils;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.SERIALIZEDLAMBDA_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.isDynamicTyped;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.codehaus.groovy.ast.tools.GeneralUtils.params;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.ast.tools.GenericsUtils.hasUnresolvedGenerics;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getClassInternalName;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getMethodDescriptor;
import static org.codehaus.groovy.classgen.asm.sc.StaticTypesFunctionalInterfaceMetadataKey.DESERIALIZE_LAMBDA_DISPATCHER;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;
import static org.objectweb.asm.Opcodes.CHECKCAST;

/**
 * Shared bytecode and deserialization support for statically-compiled functional interface implementations,
 * including both lambdas and method references.
 *
 * @since 3.0.0
 */
public interface AbstractFunctionalInterfaceWriter {

    default void writeFunctionalInterfaceIndy(final MethodVisitor methodVisitor,
                                              final String samMethodName, final String invokedTypeDescriptor,
                                              final String samMethodDescriptor, final int implMethodKind,
                                              final ClassNode implClassNode, final MethodNode implMethodNode,
                                              final Parameter[] implMethodParameters, final boolean serializable) {
        methodVisitor.visitInvokeDynamicInsn(
                samMethodName,
                invokedTypeDescriptor,
                createBootstrapMethod(serializable),
                createBootstrapMethodArguments(samMethodDescriptor, implMethodKind, implClassNode, implMethodNode, implMethodParameters, serializable)
        );
        if (serializable) {
            methodVisitor.visitTypeInsn(CHECKCAST, BytecodeHelper.getClassInternalName(ClassHelper.SERIALIZABLE_TYPE));
        }
    }

    default String createMethodDescriptor(final MethodNode method) {
        return createMethodDescriptor(method.getReturnType(), method.getParameters());
    }

    default String createMethodDescriptor(final ClassNode returnType, final Parameter[] parameters) {
        return getMethodDescriptor(returnType, parameters);
    }

    default String createFunctionalInterfaceFactoryDescriptor(final ClassNode functionalType, final Parameter[] capturedParameters) {
        return createMethodDescriptor(functionalType.redirect(), capturedParameters);
    }

    default Parameter createCapturedReceiverParameter(final ClassNode receiverType, final String parameterName) {
        Parameter parameter = new Parameter(receiverType, parameterName);
        parameter.setClosureSharedVariable(false);
        return parameter;
    }

    default ClassNode convertParameterType(final ClassNode targetType, final ClassNode parameterType, final ClassNode inferredType) {
        if (!getWrapper(inferredType).isDerivedFrom(getWrapper(parameterType))) {
            throw new RuntimeParserException("The inferred type[" + inferredType.redirect() + "] is not compatible with the parameter type[" + parameterType.redirect() + "]", parameterType);
        }

        ClassNode type;
        if (isPrimitiveType(parameterType)) {
            if (!isPrimitiveType(inferredType)) {
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
                type = getUnwrapper(inferredType).getPlainNodeReference(false);
            } else {
                type = inferredType.getPlainNodeReference(false);
            }
        } else if (isPrimitiveType(inferredType)) {
            // GROOVY-9790: bootstrap method initialization exception raised when lambda parameter type is wrong
            // (1) java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
            // (2) java.lang.BootstrapMethodError: bootstrap method initialization exception
            if (!(isDynamicTyped(parameterType) && isPrimitiveType(targetType)) // (1)
                && (parameterType.equals(getUnwrapper(parameterType)) || inferredType.equals(getWrapper(inferredType)))) { // (2)
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: int is not a subtype of class java.lang.Object
                type = getWrapper(inferredType).getPlainNodeReference();
            } else {
                type = inferredType.getPlainNodeReference(false);
            }
        } else {
            type = inferredType;
            // GROOVY-11304: no placeholders
            if (hasUnresolvedGenerics(type)) type = type.redirect();
            // GROOVY-11479: mutable for node metadata or type annotations
            if (type.toString(false).equals(parameterType.toString(false))) {
                type = parameterType;
            } else {
                // TODO: deep copy if type args set
                type = type.getPlainNodeReference();
            }
        }
        return type;
    }

    default SerializedLambdaFingerprint createSerializedLambdaFingerprint(final String samMethodDescriptor, final ClassNode capturingClass,
                                                                          final int implMethodKind, final ClassNode implClassNode,
                                                                          final MethodNode implMethodNode, final Parameter[] implMethodParameters,
                                                                          final ClassNode functionalType, final MethodNode abstractMethod,
                                                                          final int capturedArgumentCount) {
        return new SerializedLambdaFingerprint(
            getClassInternalName(capturingClass),
            implMethodKind,
            getClassInternalName(implClassNode),
            implMethodNode.getName(),
            getMethodDescriptor(implMethodNode),
            getClassInternalName(functionalType.redirect()),
            abstractMethod.getName(),
            samMethodDescriptor,
            createInstantiatedMethodType(samMethodDescriptor, implMethodNode, implMethodParameters).getDescriptor(),
            capturedArgumentCount
        );
    }

    default void addDeserializeDispatcherEntry(final WriterController controller, final Parameter[] deserializeMethodParameters,
                                               final SerializedLambdaFingerprint serializedLambdaFingerprint,
                                               final MethodNode helperMethod) {
        BlockStatement dispatcherGuards = getOrAddDeserializeDispatcherGuards(controller, deserializeMethodParameters);
        MethodCallExpression helperCall = callX(classX(controller.getClassNode()), helperMethod.getName(), args(varX(deserializeMethodParameters[0])));
        helperCall.setImplicitThis(false);
        helperCall.setMethodTarget(helperMethod);

        dispatcherGuards.addStatement(
            // Keep this guard strict: deserialization must route to exactly one synthetic helper
            // whose serialized-lambda fingerprint fully matches the incoming SerializedLambda.
            ifS(boolX(matchesSerializedFunctionalInterface(varX(deserializeMethodParameters[0]), serializedLambdaFingerprint)),
                returnS(helperCall)
            )
        );
    }

    default Parameter[] createDeserializeMethodParameters() {
        return new Parameter[] { new Parameter(SERIALIZEDLAMBDA_TYPE, "serializedLambda") };
    }

    private Handle createBootstrapMethod(final boolean serializable) {
        return new Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            serializable ? "altMetafactory" : "metafactory",
            serializable ? "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
                : "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
            false // GROOVY-8299, GROOVY-8989, GROOVY-11265
        );
    }

    private Object[] createBootstrapMethodArguments(final String samMethodDescriptor, final int implMethodKind,
                                                    final ClassNode implClassNode, final MethodNode implMethodNode,
                                                    final Parameter[] implMethodParameters, final boolean serializable) {
        Object[] arguments = !serializable ? new Object[3] : new Object[]{null, null, null, 5, 0};

        arguments[0] = Type.getMethodType(samMethodDescriptor);

        arguments[1] = new Handle(
            implMethodKind, // H_INVOKESTATIC or H_INVOKEVIRTUAL or H_INVOKEINTERFACE (GROOVY-9853)
            getClassInternalName(implClassNode.getName()),
            implMethodNode.getName(),
            getMethodDescriptor(implMethodNode),
            implClassNode.isInterface());

        arguments[2] = createInstantiatedMethodType(samMethodDescriptor, implMethodNode, implMethodParameters);

        return arguments;
    }

    private Type createInstantiatedMethodType(final String samMethodDescriptor, final MethodNode implMethodNode, final Parameter[] implMethodParameters) {
        ClassNode returnType = implMethodNode.getReturnType();
        switch (Type.getReturnType(samMethodDescriptor).getSort()) {
          case Type.BOOLEAN:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Boolean_TYPE; // GROOVY-10975
            break;
          case Type.BYTE:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Byte_TYPE;
            break;
          case Type.CHAR:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Character_TYPE;
            break;
          case Type.DOUBLE:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Double_TYPE;
            break;
          case Type.FLOAT:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Float_TYPE;
            break;
          case Type.INT:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Integer_TYPE;
            break;
          case Type.LONG:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Long_TYPE;
            break;
          case Type.SHORT:
            if (returnType.isGenericsPlaceHolder()) returnType = ClassHelper.Short_TYPE;
            break;
          case Type.VOID:
            returnType = ClassHelper.VOID_TYPE; // GROOVY-10933
        }

        return Type.getMethodType(createMethodDescriptor(returnType, implMethodParameters));
    }

    private BlockStatement getOrAddDeserializeDispatcherGuards(final WriterController controller, final Parameter[] deserializeMethodParameters) {
        ClassNode enclosingClass = controller.getClassNode();
        BlockStatement dispatcherGuards = enclosingClass.getNodeMetaData(DESERIALIZE_LAMBDA_DISPATCHER);
        if (dispatcherGuards != null) {
            return dispatcherGuards;
        }

        dispatcherGuards = new BlockStatement();
        BlockStatement dispatcher = new BlockStatement();
        dispatcher.addStatement(dispatcherGuards);
        dispatcher.addStatement(createInvalidDeserializationStatement());

        MethodNode deserializeLambda = enclosingClass.addSyntheticMethod(
            "$deserializeLambda$",
                Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC,
                OBJECT_TYPE,
                deserializeMethodParameters,
                ClassNode.EMPTY_ARRAY,
                dispatcher);
        deserializeLambda.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        enclosingClass.putNodeMetaData(DESERIALIZE_LAMBDA_DISPATCHER, dispatcherGuards);
        return dispatcherGuards;
    }

    private Statement createInvalidDeserializationStatement() {
        final ClassNode cn = ClassHelper.make(IllegalArgumentException.class);
        ConstructorCallExpression exception = ctorX(cn, constX("Invalid serialized functional interface"));
        exception.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, cn.getDeclaredConstructor(
                params(param(ClassHelper.STRING_TYPE, "message"))
        ));
        return throwS(exception);
    }

    /**
     * Builds the identity check used by {@code $deserializeLambda$} to select the
     * correct synthetic helper for a serialized lambda/method reference.
     * <p>
     * The generated expression is a conjunction over all stable
     * {@link java.lang.invoke.SerializedLambda} identity fields we emit via
     * {@link #createSerializedLambdaFingerprint(String, ClassNode, int, ClassNode, MethodNode, Parameter[], ClassNode, MethodNode, int)}:
     * capturing class, implementation kind/class/name/signature, functional interface class/SAM method/signature,
     * instantiated method type, and captured argument count.
     * <p>
     * Do not weaken this predicate (for example, by checking only method name/class or
     * by returning a constant). Doing so can misroute deserialization to the wrong helper,
     * while overly strict constant-false behavior breaks valid deserialization.
     *
     * @param serializedForm the deserialized {@code SerializedLambda} expression
     * @param serializedLambdaFingerprint compile-time fingerprint of one serialized lambda target
     * @return expression that evaluates to {@code true} only for this exact target
     */
    private Expression matchesSerializedFunctionalInterface(final Expression serializedForm, final SerializedLambdaFingerprint serializedLambdaFingerprint) {
        return allMatch(
            matchesSerializedFormInt(serializedForm, "getCapturedArgCount", serializedLambdaFingerprint.capturedArgCount()),
            matchesSerializedFormString(serializedForm, "getCapturingClass", serializedLambdaFingerprint.capturingClass()),
            matchesSerializedFormInt(serializedForm, "getImplMethodKind", serializedLambdaFingerprint.implMethodKind()),
            matchesSerializedFormString(serializedForm, "getImplClass", serializedLambdaFingerprint.implClass()),
            matchesSerializedFormString(serializedForm, "getImplMethodName", serializedLambdaFingerprint.implMethodName()),
            matchesSerializedFormString(serializedForm, "getImplMethodSignature", serializedLambdaFingerprint.implMethodSignature()),
            matchesSerializedFormString(serializedForm, "getFunctionalInterfaceClass", serializedLambdaFingerprint.functionalInterfaceClass()),
            matchesSerializedFormString(serializedForm, "getFunctionalInterfaceMethodName", serializedLambdaFingerprint.functionalInterfaceMethodName()),
            matchesSerializedFormString(serializedForm, "getFunctionalInterfaceMethodSignature", serializedLambdaFingerprint.functionalInterfaceMethodSignature()),
            matchesSerializedFormString(serializedForm, "getInstantiatedMethodType", serializedLambdaFingerprint.instantiatedMethodType())
        );
    }

    /**
     * Combines predicates with logical AND.
     *
     * @param expressions match predicates to combine
     * @return conjunction of all predicates
     * @throws IllegalArgumentException if no predicates are supplied
     */
    private Expression allMatch(final Expression... expressions) {
        return Arrays.stream(expressions)
                .reduce(GeneralUtils::andX)
                .orElseThrow(() -> new IllegalArgumentException("expressions must not be empty"));
    }

    private Expression matchesSerializedFormInt(final Expression serializedForm, final String accessorName, final int expectedValue) {
        return eqX(serializedLambdaAccessorCall(serializedForm, accessorName), constX(expectedValue, true));
    }

    private Expression matchesSerializedFormString(final Expression serializedForm, final String accessorName, final String expectedValue) {
        return eqX(serializedLambdaAccessorCall(serializedForm, accessorName), constX(expectedValue));
    }

    /**
     * Creates a direct {@link java.lang.invoke.SerializedLambda} accessor call for the generated
     * deserialization dispatcher.
     * <p>
     * The dispatcher is emitted during bytecode generation rather than type checking, so it must
     * resolve accessor targets explicitly instead of relying on a later static-type-checking pass.
     * This keeps the generated bytecode on the direct invocation path and prevents accidental
     * fallback to Groovy's dynamic method dispatch for JDK accessors.
     *
     * @param serializedForm the deserialized {@code SerializedLambda} expression
     * @param accessorName the zero-argument accessor to invoke
     * @return method call expression with a resolved direct-call target
     * @throws IllegalArgumentException if the accessor does not exist on {@code SerializedLambda}
     */
    private MethodCallExpression serializedLambdaAccessorCall(final Expression serializedForm, final String accessorName) {
        MethodNode accessor = SERIALIZEDLAMBDA_TYPE.getMethod(accessorName, Parameter.EMPTY_ARRAY);
        if (accessor == null) {
            throw new IllegalArgumentException("Unknown SerializedLambda accessor: " + accessorName);
        }

        MethodCallExpression accessorCall = callX(serializedForm, accessorName);
        accessorCall.setMethodTarget(accessor);
        accessorCall.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, accessor);
        return accessorCall;
    }

    /**
     * Compile-time identity of one serialized functional-interface target used by
     * {@code $deserializeLambda$} dispatch.
     */
    record SerializedLambdaFingerprint(String capturingClass, int implMethodKind, String implClass, String implMethodName,
                                       String implMethodSignature, String functionalInterfaceClass,
                                       String functionalInterfaceMethodName, String functionalInterfaceMethodSignature,
                                       String instantiatedMethodType, int capturedArgCount) {
    }
}
