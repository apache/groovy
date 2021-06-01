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
 * Utility methods for lazy class loading.
 */
class MemberSignatureParser {

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
        return new FieldNode(field.fieldName, field.accessModifiers, type[0], owner, field.value != null ? new ConstantExpression(field.value) : null);
    }

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
            Object annDefault = method.annotationDefault;
            if (annDefault != null) {
                if (annDefault instanceof TypeWrapper) {
                    annDefault = resolver.resolveType(Type.getType(((TypeWrapper) annDefault).desc));
                }
                result.setCode(new ReturnStatement(new ConstantExpression(annDefault)));
                result.setAnnotationDefault(true);
            } else {
                // seems wrong but otherwise some tests fail (e.g. TestingASTTransformsTest)
                result.setCode(new ReturnStatement(new ConstantExpression(null)));
            }
        }
        if (typeParameters != null && typeParameters.length > 0) {
            result.setGenericsTypes(typeParameters);
        }
        return result;
    }

    private static ClassNode applyErasure(final ClassNode genericType, final ClassNode erasure) {
        if (genericType.isArray() && erasure.isArray() && genericType.getComponentType().isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
            genericType.getComponentType().setRedirect(erasure.getComponentType());
        } else if (genericType.isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
        }
        return genericType;
    }

    private static ClassNode[] resolve(final AsmReferenceResolver resolver, final String[] names) {
        int n = names.length; ClassNode[] nodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            nodes[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(names[i]));
        }
        return nodes;
    }

    private static ClassNode[] resolve(final AsmReferenceResolver resolver, final Type... types) {
        int n = types.length; ClassNode[] nodes = new ClassNode[n];
        for (int i = 0; i < n; i += 1) {
            nodes[i] = resolver.resolveType(types[i]);
        }
        return nodes;
    }
}
