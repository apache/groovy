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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;

/**
 * Represents functional interface writer which contains some common methods to complete generating bytecode
 * @since 3.0.0
 */
public interface AbstractFunctionalInterfaceWriter {
    @Deprecated
    String ORIGINAL_PARAMETERS_WITH_EXACT_TYPE = "__ORIGINAL_PARAMETERS_WITH_EXACT_TYPE";

    default ClassNode getFunctionalInterfaceType(final Expression expression) {
        ClassNode type = expression.getNodeMetaData(StaticTypesMarker.PARAMETER_TYPE);
        if (type == null) {
            type = expression.getNodeMetaData(StaticTypesMarker.INFERRED_FUNCTIONAL_INTERFACE_TYPE);
        }
        return type;
    }

    default String createMethodDescriptor(final MethodNode methodNode) {
        return BytecodeHelper.getMethodDescriptor(methodNode);
    }

    default Handle createBootstrapMethod(final boolean isInterface, final boolean serializable) {
        if (serializable) {
            return new Handle(
                    Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "altMetafactory",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                    isInterface
            );
        }

        return new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                isInterface
        );
    }

    @Deprecated
    default Object[] createBootstrapMethodArguments(final String abstractMethodDesc, final int insn, final ClassNode methodOwner, final MethodNode methodNode, final boolean serializable) {
        return createBootstrapMethodArguments(abstractMethodDesc, insn, methodOwner, methodNode, methodNode.getNodeMetaData(ORIGINAL_PARAMETERS_WITH_EXACT_TYPE), serializable);
    }

    default Object[] createBootstrapMethodArguments(final String abstractMethodDesc, final int insn, final ClassNode methodOwner, final MethodNode methodNode, final Parameter[] parameters, final boolean serializable) {
        ClassNode returnType = methodNode.getReturnType();
        switch (Type.getReturnType(abstractMethodDesc).getSort()) {
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

        Object[] arguments = !serializable ? new Object[3] : new Object[]{null, null, null, 5, 0};

        arguments[0] = Type.getMethodType(abstractMethodDesc);

        arguments[1] = new Handle(
                insn, // H_INVOKESTATIC or H_INVOKEVIRTUAL or H_INVOKEINTERFACE (GROOVY-9853)
                BytecodeHelper.getClassInternalName(methodOwner.getName()),
                methodNode.getName(),
                BytecodeHelper.getMethodDescriptor(methodNode),
                methodOwner.isInterface());

        arguments[2] = Type.getMethodType(BytecodeHelper.getMethodDescriptor(returnType, parameters));

        return arguments;
    }

    default ClassNode convertParameterType(final ClassNode parameterType, final ClassNode inferredType) {
        return convertParameterType(parameterType, parameterType, inferredType);
    }

    default ClassNode convertParameterType(final ClassNode targetType, final ClassNode parameterType, final ClassNode inferredType) {
        if (!ClassHelper.getWrapper(inferredType).isDerivedFrom(ClassHelper.getWrapper(parameterType))) {
            throw new RuntimeParserException("The inferred type[" + inferredType.redirect() + "] is not compatible with the parameter type[" + parameterType.redirect() + "]", parameterType);
        }

        ClassNode type = inferredType;
        if (ClassHelper.isPrimitiveType(parameterType)) {
            if (!ClassHelper.isPrimitiveType(inferredType)) {
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
                type = ClassHelper.getUnwrapper(inferredType);
            }
        } else if (ClassHelper.isPrimitiveType(inferredType)) {
            // GROOVY-9790: bootstrap method initialization exception raised when lambda parameter type is wrong
            // (1) java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
            // (2) java.lang.BootstrapMethodError: bootstrap method initialization exception
            if (!(ClassHelper.DYNAMIC_TYPE.equals(parameterType) && ClassHelper.isPrimitiveType(targetType)) // (1)
                    && (parameterType.equals(ClassHelper.getUnwrapper(parameterType)) || inferredType.equals(ClassHelper.getWrapper(inferredType)))) { // (2)
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: int is not a subtype of class java.lang.Object
                type = ClassHelper.getWrapper(inferredType);
            }
        }
        if (type.isGenericsPlaceHolder()) {
            type = type.redirect();
        }
        return type;
    }

    default Parameter prependParameter(final List<Parameter> parameterList, final String parameterName, final ClassNode parameterType) {
        Parameter parameter = new Parameter(parameterType, parameterName);
        parameter.setClosureSharedVariable(false);
        parameterList.add(0, parameter);
        return parameter;
    }
}
