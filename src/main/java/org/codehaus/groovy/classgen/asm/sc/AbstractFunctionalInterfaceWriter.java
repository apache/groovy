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
import org.codehaus.groovy.syntax.RuntimeParserException;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.getUnwrapper;
import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.isDynamicTyped;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getClassInternalName;
import static org.codehaus.groovy.classgen.asm.BytecodeHelper.getMethodDescriptor;

/**
 * Represents functional interface writer which contains some common methods to complete generating bytecode
 * @since 3.0.0
 */
public interface AbstractFunctionalInterfaceWriter {

    default String createMethodDescriptor(final MethodNode method) {
        Class<?> returnType = method.getReturnType().getTypeClass();
        Class<?>[] parameterTypes = Arrays.stream(method.getParameters())
                .map(p -> p.getType().getTypeClass()).toArray(Class[]::new);

        return getMethodDescriptor(returnType, parameterTypes);
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

    default Object[] createBootstrapMethodArguments(final String abstractMethodDesc, final int insn, final ClassNode methodOwner, final MethodNode methodNode, final Parameter[] parameters, final boolean serializable) {
        ClassNode returnType = !abstractMethodDesc.endsWith(")V") ? methodNode.getReturnType() : ClassHelper.VOID_TYPE; // GROOVY-10933
        Object[] arguments = !serializable ? new Object[3] : new Object[]{null, null, null, 5, 0};

        arguments[0] = Type.getType(abstractMethodDesc);

        arguments[1] = new Handle(
                insn, // H_INVOKESTATIC or H_INVOKEVIRTUAL or H_INVOKEINTERFACE (GROOVY-9853)
                getClassInternalName(methodOwner.getName()),
                methodNode.getName(),
                getMethodDescriptor(methodNode),
                methodOwner.isInterface());

        arguments[2] = Type.getType(getMethodDescriptor(returnType, parameters));

        return arguments;
    }

    default ClassNode convertParameterType(final ClassNode targetType, final ClassNode parameterType, final ClassNode inferredType) {
        if (!getWrapper(inferredType).isDerivedFrom(getWrapper(parameterType))) {
            throw new RuntimeParserException("The inferred type[" + inferredType.redirect() + "] is not compatible with the parameter type[" + parameterType.redirect() + "]", parameterType);
        }

        ClassNode type;
        boolean isParameterTypePrimitive = isPrimitiveType(parameterType);
        boolean isInferredTypePrimitive = isPrimitiveType(inferredType);
        if (!isParameterTypePrimitive && isInferredTypePrimitive) {
            if (isDynamicTyped(parameterType) && isPrimitiveType(targetType) // (1)
                    || !parameterType.equals(getUnwrapper(parameterType)) && !inferredType.equals(getWrapper(inferredType)) // (2)
            ) {
                // GROOVY-9790: bootstrap method initialization exception raised when lambda parameter type is wrong
                // (1) java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
                // (2) java.lang.BootstrapMethodError: bootstrap method initialization exception
                type = inferredType;
            } else {
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: int is not a subtype of class java.lang.Object
                type = getWrapper(inferredType);
            }
        } else if (isParameterTypePrimitive && !isInferredTypePrimitive) {
            // The non-primitive type and primitive type are not allowed to mix since Java 9+
            // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
            type = getUnwrapper(inferredType);
        } else {
            type = inferredType;
        }
        return type;
    }

    /**
     * @deprecated use {@link #convertParameterType(ClassNode, ClassNode, ClassNode)} instead
     */
    @Deprecated
    default ClassNode convertParameterType(final ClassNode parameterType, final ClassNode inferredType) {
        if (!getWrapper(inferredType.redirect()).isDerivedFrom(getWrapper(parameterType.redirect()))) {
            throw new RuntimeParserException("The inferred type[" + inferredType.redirect() + "] is not compatible with the parameter type[" + parameterType.redirect() + "]", parameterType);
        } else {
            boolean isParameterTypePrimitive = isPrimitiveType(parameterType);
            boolean isInferredTypePrimitive = isPrimitiveType(inferredType);
            ClassNode type;
            if (!isParameterTypePrimitive && isInferredTypePrimitive) {
                if (parameterType != getUnwrapper(parameterType) && inferredType != getWrapper(inferredType)) {
                    type = inferredType;
                } else {
                    type = getWrapper(inferredType);
                }
            } else if (isParameterTypePrimitive && !isInferredTypePrimitive) {
                type = getUnwrapper(inferredType);
            } else {
                type = inferredType;
            }
            return type;
        }
    }

    default Parameter prependParameter(final List<Parameter> parameterList, final String parameterName, final ClassNode parameterType) {
        Parameter parameter = new Parameter(parameterType, parameterName);
        parameter.setClosureSharedVariable(false);
        parameter.setOriginType(parameterType);
        parameterList.add(0, parameter);
        return parameter;
    }
}
