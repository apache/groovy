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
package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.tools.GenericsUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.List;
import java.util.Map;

/**
 * Parses Java generic signatures from compiled method and field bytecode according to JVMS &sect;4.7.9.1.
 * <p>
 * This utility class provides factory methods to create MethodNode and FieldNode instances from
 * ASM stubs, extracting generic type information from the class member's signature attribute.
 * <p>
 * For fields, it extracts:
 * <ul>
 *   <li>Field type with full generic information (e.g., {@code List<String>})</li>
 * </ul>
 * <p>
 * For methods, it extracts:
 * <ul>
 *   <li>Generic type parameters with their bounds (e.g., {@code <T extends Comparable<T>>})</li>
 *   <li>Parameter types with generic information (e.g., {@code List<T>})</li>
 *   <li>Return type with generic information (e.g., {@code T})</li>
 *   <li>Exception types with generic information (rare)</li>
 * </ul>
 * <p>
 * Method signature format example (JVMS notation):
 * {@code <T:Ljava/lang/Comparable<TT;>;>(TT;)TT;}
 * represents: {@code <T extends Comparable<T>> T apply(T value)}
 *
 * @see GenericsType for generic type representation
 * @see MethodNode for method AST nodes
 * @see FieldNode for field AST nodes
 * @see FormalParameterParser for parsing type parameters
 * @see TypeSignatureParser for parsing type expressions
 * @see ClassSignatureParser for parsing class-level types
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se19/html/jvms-4.html#jvms-MethodTypeSignature">
 *      JVMS §4.7.9.1 MethodTypeSignature and FieldTypeSignature Productions</a>
 */
class MemberSignatureParser {

    /**
     * Creates a FieldNode from a field stub with resolved generic type information.
     * <p>
     * Parses the field's generic signature (if present) to extract the field's parameterized type.
     * If no generic signature is available, uses the field's descriptor and applies type erasure.
     * <p>
     * Examples:
     * <ul>
     *   <li>Generic field: {@code items: List<String>} from signature {@code Ljava/util/List<Ljava/lang/String;>;}</li>
     *   <li>Raw field: {@code items: List} when generic information is not available</li>
     *   <li>Primitive field: {@code count: int} from descriptor {@code I}</li>
     * </ul>
     *
     * @param field the field stub containing name, descriptor, signature, and modifiers
     * @param resolver used to resolve class types from descriptors and internal names
     * @param owner the class that declares this field
     * @return a FieldNode with the field's type resolved, including generic information if available
     *
     * @see TypeSignatureParser for signature parsing
     * @see FieldStub for field metadata
     */
    static FieldNode createFieldNode(final FieldStub field, final AsmReferenceResolver resolver, final DecompiledClassNode owner) {
        ClassNode[] type = resolve(resolver, Type.getType(field.desc));
        if (field.signature != null) {
            new SignatureReader(field.signature).accept(new TypeSignatureParser(resolver) {
                @Override
                void finished(final ClassNode result) {
                    type[0] = applyErasure(result, type[0]);
                }
            });
        } else {
            // ex: java.util.Collections#EMPTY_LIST/EMPTY_MAP/EMPTY_SET
            type[0] = GenericsUtils.nonGeneric(type[0]);
        }
        return new FieldNode(field.fieldName, field.accessModifiers, type[0], owner, field.value != null ? new ConstantExpression(field.value, true) : null);
    }

    /**
     * Creates a MethodNode or ConstructorNode from a method stub with resolved generic type information.
     * <p>
     * Parses the method's generic signature (if present) to extract:
     * <ul>
     *   <li>Generic type parameters with their bounds</li>
     *   <li>Parameter types with full generic information</li>
     *   <li>Return type with generic information (or void for constructors)</li>
     *   <li>Exception types (may include generic information in rare cases)</li>
     * </ul>
     * <p>
     * For constructors (methods named "&lt;init&gt;"), creates a ConstructorNode instead of MethodNode.
     * Preserves annotation information for parameters and handles annotation default values for annotation methods.
     * <p>
     * Method signature format examples (JVMS notation):
     * <ul>
     *   <li>Non-generic: {@code ([Ljava/lang/String;)V} → {@code void method(String[])}</li>
     *   <li>Generic with bounds: {@code <T:Ljava/lang/Number;>(TT;)TT;} → {@code <T extends Number> T method(T)}</li>
     *   <li>Parameterized types: {@code (Ljava/util/List<Ljava/lang/String;>;)V} → {@code void method(List<String>)}</li>
     * </ul>
     *
     * @param resolver used to resolve class types from descriptors and internal names
     * @param method the method stub containing descriptor, signature, parameters, and exceptions
     * @return a MethodNode (or ConstructorNode for "&lt;init&gt;") with types and generics resolved
     *         and annotations applied to parameters
     *
     * @see FormalParameterParser for parsing method type parameters
     * @see TypeSignatureParser for parsing parameter and return types
     * @see MethodStub for method metadata
     * @see ConstructorNode for constructor representation
     * @see Parameter for method parameters with type and annotation info
     */
    static MethodNode createMethodNode(final AsmReferenceResolver resolver, final MethodStub method) {
        GenericsType[] typeParameters = null;

        ClassNode[] returnType = resolve(resolver, Type.getReturnType(method.desc));

        ClassNode[] parameterTypes = resolve(resolver, Type.getArgumentTypes(method.desc));

        ClassNode[] exceptionTypes = resolve(resolver, method.exceptions);

        if (method.signature != null) {
            FormalParameterParser parser = new FormalParameterParser(resolver) {
                private int exceptionIndex, parameterIndex;

                @Override
                public SignatureVisitor visitReturnType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(final ClassNode result) {
                            returnType[0] = applyErasure(result, returnType[0]);
                        }
                    };
                }

                @Override
                public SignatureVisitor visitParameterType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(final ClassNode result) {
                            parameterTypes[parameterIndex] = applyErasure(result, parameterTypes[parameterIndex]);
                            parameterIndex += 1;
                        }
                    };
                }

                @Override
                public SignatureVisitor visitExceptionType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(final ClassNode result) {
                            exceptionTypes[exceptionIndex] = applyErasure(result, exceptionTypes[exceptionIndex]);
                            exceptionIndex += 1;
                        }
                    };
                }
            };
            new SignatureReader(method.signature).accept(parser);
            typeParameters = parser.getTypeParameters();
        } else {
            returnType[0] = GenericsUtils.nonGeneric(returnType[0]);
            for (int i = 0, n = parameterTypes.length; i < n; i += 1) {
                parameterTypes[i] = GenericsUtils.nonGeneric(parameterTypes[i]);
            }
        }

        int nParameters = parameterTypes.length;
        Parameter[] parameters = new Parameter[nParameters];
        List<String> parameterNames = method.parameterNames;
        for (int i = 0; i < nParameters; i += 1) {
            String parameterName = "param" + i;
            if (parameterNames != null && i < parameterNames.size()) {
                String decompiledName = parameterNames.get(i);
                if (decompiledName != null) {
                    parameterName = decompiledName;
                }
            }
            parameters[i] = new Parameter(parameterTypes[i], parameterName);
        }

        if (method.parameterAnnotations != null) {
            for (Map.Entry<Integer, List<AnnotationStub>> entry : method.parameterAnnotations.entrySet()) {
                for (AnnotationStub stub : entry.getValue()) {
                    AnnotationNode annotationNode = Annotations.createAnnotationNode(stub, resolver);
                    if (annotationNode != null) {
                        parameters[entry.getKey()].addAnnotation(annotationNode);
                    }
                }
            }
        }

        MethodNode result;
        if ("<init>".equals(method.methodName)) {
            result = new ConstructorNode(method.accessModifiers, parameters, exceptionTypes, null);
        } else {
            result = new MethodNode(method.methodName, method.accessModifiers, returnType[0], parameters, exceptionTypes, null);
            Object defaultValue = method.annotationDefault;
            if (defaultValue != null) {
                if (defaultValue instanceof TypeWrapper) {
                    defaultValue = resolver.resolveType(Type.getType(((TypeWrapper) defaultValue).desc));
                }
                result.setCode(new ReturnStatement(new ConstantExpression(defaultValue, true)));
                result.setAnnotationDefault(true);
            }
        }
        if (typeParameters != null && typeParameters.length > 0) {
            result.setGenericsTypes(typeParameters);
        }
        return result;
    }

    /**
     * Resolves an array of internal class names to their corresponding ClassNode representations.
     * <p>
     * This helper method converts JVMS internal class names (e.g., "java/util/List") to
     * Groovy ClassNode objects by delegating to the resolver. Used to convert exception type names.
     * <p>
     * Example: {@code ["java/lang/IOException", "java/lang/NullPointerException"]}
     * → array of resolved ClassNodes for those types
     *
     * @param resolver used to resolve each class name to a ClassNode
     * @param names array of internal class names to resolve
     * @return array of ClassNodes, one for each input name in corresponding order
     *
     * @see AsmDecompiler#fromInternalName(String) for converting internal to binary names
     */
    private static ClassNode[] resolve(final AsmReferenceResolver resolver, final String[] names) {
        int n = names.length; ClassNode[] nodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            nodes[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(names[i]));
        }
        return nodes;
    }

    /**
     * Resolves an array of ASM Type descriptors to their corresponding ClassNode representations.
     * <p>
     * This helper method converts ASM Type objects (representing descriptors from the class file)
     * to Groovy ClassNode objects by delegating to the resolver. Used to convert parameter types,
     * return types, and exception types from the method descriptor.
     * <p>
     * Example: {@code [Type.INT_TYPE, Type.getObjectType("java/lang/String")]}
     * → array of resolved ClassNodes for those types
     *
     * @param resolver used to resolve each Type to a ClassNode
     * @param types variable-length array of ASM Type objects to resolve
     * @return array of ClassNodes, one for each input type in corresponding order
     *
     * @see org.objectweb.asm.Type for ASM type descriptors
     */
    private static ClassNode[] resolve(final AsmReferenceResolver resolver, final Type... types) {
        int n = types.length; ClassNode[] nodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            nodes[i] = resolver.resolveType(types[i]);
        }
        return nodes;
    }
}
