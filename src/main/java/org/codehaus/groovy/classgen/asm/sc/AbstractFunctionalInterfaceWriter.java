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
        return getMethodDescriptor(method.getReturnType(), method.getParameters());
    }

    default Handle createBootstrapMethod(final boolean isInterface, final boolean serializable) {
        return new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                serializable ? "altMetafactory" : "metafactory",
                serializable ? "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;"
                             : "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false // GROOVY-8299, GROOVY-8989, GROOVY-11265
        );
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
                getClassInternalName(methodOwner.getName()),
                methodNode.getName(),
                getMethodDescriptor(methodNode),
                methodOwner.isInterface());

        arguments[2] = Type.getMethodType(getMethodDescriptor(returnType, parameters));

        return arguments;
    }

    default ClassNode convertParameterType(final ClassNode parameterType, final ClassNode inferredType) {
        return convertParameterType(parameterType, parameterType, inferredType);
    }

    default ClassNode convertParameterType(final ClassNode targetType, final ClassNode parameterType, final ClassNode inferredType) {
        if (!getWrapper(inferredType).isDerivedFrom(getWrapper(parameterType))) {
            throw new RuntimeParserException("The inferred type[" + inferredType.redirect() + "] is not compatible with the parameter type[" + parameterType.redirect() + "]", parameterType);
        }

        ClassNode type = inferredType;
        if (isPrimitiveType(parameterType)) {
            if (!isPrimitiveType(inferredType)) {
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
                type = getUnwrapper(inferredType);
            }
        } else if (isPrimitiveType(inferredType)) {
            // GROOVY-9790: bootstrap method initialization exception raised when lambda parameter type is wrong
            // (1) java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: class java.lang.Integer is not a subtype of int
            // (2) java.lang.BootstrapMethodError: bootstrap method initialization exception
            if (!(isDynamicTyped(parameterType) && isPrimitiveType(targetType)) // (1)
                    && (parameterType.equals(getUnwrapper(parameterType)) || inferredType.equals(getWrapper(inferredType)))) { // (2)
                // The non-primitive type and primitive type are not allowed to mix since Java 9+
                // java.lang.invoke.LambdaConversionException: Type mismatch for instantiated parameter 0: int is not a subtype of class java.lang.Object
                type = getWrapper(inferredType);
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
