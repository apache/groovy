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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.groovy.util.BeanUtils.decapitalize;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveBoolean;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

/**
 * Utility class for working with MethodNodes
 */
public class MethodNodeUtils {

    private MethodNodeUtils() { }

    /**
     * Return the method node's descriptor including its
     * name and parameter types without generics.
     *
     * @param mNode the method node
     * @return the method node's abbreviated descriptor excluding the return type
     */
    public static String methodDescriptorWithoutReturnType(final MethodNode mNode) {
        StringBuilder sb = new StringBuilder();
        sb.append(mNode.getName()).append(':');
        for (Parameter p : mNode.getParameters()) {
            sb.append(ClassNodeUtils.formatTypeName(p.getType())).append(',');
        }
        return sb.toString();
    }

    /**
     * Return the method node's descriptor which includes its return type,
     * name and parameter types without generics.
     *
     * @param mNode the method node
     * @return the method node's descriptor
     */
    public static String methodDescriptor(final MethodNode mNode) {
        return methodDescriptor(mNode, false);
    }

    /**
     * Return the method node's descriptor which includes its return type,
     * name and parameter types without generics.
     *
     * @param mNode the method node
     * @param pretty whether to quote a name with spaces
     * @return the method node's descriptor
     */
    public static String methodDescriptor(final MethodNode mNode, boolean pretty) {
        String name = mNode.getName();
        if (pretty) pretty = name.contains(" ");
        Parameter[] parameters = mNode.getParameters();
        int nParameters = parameters == null ? 0 : parameters.length;

        StringBuilder sb = new StringBuilder(name.length() * 2 + nParameters * 10);
        sb.append(ClassNodeUtils.formatTypeName(mNode.getReturnType()));
        sb.append(' ');
        if (pretty) sb.append('"');
        sb.append(name);
        if (pretty) sb.append('"');
        sb.append('(');
        for (int i = 0; i < nParameters; i += 1) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(ClassNodeUtils.formatTypeName(parameters[i].getType()));
        }
        sb.append(')');
        return sb.toString();
    }

    /**
     * For a method node potentially representing a property, returns the name of the property.
     *
     * @param mNode a MethodNode
     * @return the property name without the get/set/is prefix if a property or null
     */
    public static String getPropertyName(final MethodNode mNode) {
        final String name = mNode.getName();
        final int nameLength = name.length();
        if (nameLength > 2) {
            switch (name.charAt(0)) {
              case 'g':
                if (nameLength > 3 && name.charAt(1) == 'e' && name.charAt(2) == 't' && mNode.getParameters().length == 0 && !mNode.isVoidMethod()) {
                    return decapitalize(name.substring(3));
                }
                break;
              case 's':
                if (nameLength > 3 && name.charAt(1) == 'e' && name.charAt(2) == 't' && mNode.getParameters().length == 1 /*&& mNode.isVoidMethod()*/) {
                    return decapitalize(name.substring(3));
                }
                break;
              case 'i':
                if (name.charAt(1) == 's' && mNode.getParameters().length == 0 && (isPrimitiveBoolean(mNode.getReturnType()) /*|| isWrapperBoolean(mNode.getReturnType())*/)) {
                    return decapitalize(name.substring(2));
                }
                break;
            }
        }
        return null;
    }

    /**
     * Gets the code for a method (or constructor) as a block.
     * If no code is found, an empty block will be returned.
     * If a single non-block statement is found, a block containing that statement will be returned.
     * Otherwise the existing block statement will be returned.
     * The original {@code node} is not modified.
     *
     * @param mNode the method (or constructor) node
     * @return the found or created block statement
     */
    public static BlockStatement getCodeAsBlock(final MethodNode mNode) {
        Statement code = mNode.getCode();
        BlockStatement block;
        if (code == null) {
            block = new BlockStatement();
        } else if (!(code instanceof BlockStatement)) {
            block = new BlockStatement();
            block.addStatement(code);
        } else {
            block = (BlockStatement) code;
        }
        return block;
    }

    /**
     * Determines if given method is a getter candidate.
     *
     * @since 4.0.0
     */
    public static boolean isGetterCandidate(final MethodNode mNode) {
        Parameter[] parameters = mNode.getParameters();
        return (parameters == null || parameters.length == 0)
                && mNode.isPublic() && !mNode.isStatic() && !mNode.isAbstract() && !mNode.isVoidMethod();
    }

    /**
     * Returns new list that includes methods that will be generated for default
     * argument expressions.
     *
     * @since 5.0.0
     */
    public static List<MethodNode> withDefaultArgumentMethods(final List<? extends MethodNode> methods) {
        List<MethodNode> result = new ArrayList<>(methods.size());

        for (MethodNode method : methods) {
            result.add(method);

            if (!method.hasDefaultValue()) continue;

            Parameter[] parameters = method.getParameters();
            var n = Arrays.stream(parameters).filter(Parameter::hasInitialExpression).count();
            for (int i = 1; i <= n; i += 1) { // drop parameters with value from right to left
                Parameter[] newParams = new Parameter[parameters.length - i];
                int j = 1, index = 0;
                for (Parameter parameter : parameters) {
                    if (j > n - i && parameter.hasInitialExpression()) {
                        ;
                    } else {
                        newParams[index++] = parameter;
                    }
                    if (parameter.hasInitialExpression()) j += 1;
                }

                MethodNode stub;
                if (method.isConstructor()) {
                    stub = new ConstructorNode(method.getModifiers(), newParams, method.getExceptions(), EmptyStatement.INSTANCE);
                } else {
                    stub = new MethodNode(method.getName(), method.getModifiers() & ~ACC_ABSTRACT, method.getReturnType(), newParams, method.getExceptions(), EmptyStatement.INSTANCE);
                }
                stub.setDeclaringClass(method.getDeclaringClass());
                stub.setGenericsTypes(method.getGenericsTypes());
                stub.setSynthetic(true);
                result.add(stub);
            }
        }

        return result;
    }
}
