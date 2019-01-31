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
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.List;
import java.util.Map;

/**
 * Utility methods for lazy class loading
 */
class MemberSignatureParser {
    static MethodNode createMethodNode(final AsmReferenceResolver resolver, MethodStub method) {
        GenericsType[] typeParameters = null;

        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        final ClassNode[] parameterTypes = new ClassNode[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            parameterTypes[i] = resolver.resolveType(argumentTypes[i]);
        }

        final ClassNode[] exceptions = new ClassNode[method.exceptions.length];
        for (int i = 0; i < method.exceptions.length; i++) {
            exceptions[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(method.exceptions[i]));
        }

        final ClassNode[] returnType = {resolver.resolveType(Type.getReturnType(method.desc))};

        if (method.signature != null) {
            FormalParameterParser v = new FormalParameterParser(resolver) {
                int paramIndex = 0;

                @Override
                public SignatureVisitor visitParameterType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            parameterTypes[paramIndex] = applyErasure(result, parameterTypes[paramIndex]);
                            paramIndex++;
                        }
                    };
                }

                @Override
                public SignatureVisitor visitReturnType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            returnType[0] = applyErasure(result, returnType[0]);
                        }
                    };
                }

                int exceptionIndex = 0;

                @Override
                public SignatureVisitor visitExceptionType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            exceptions[exceptionIndex] = applyErasure(result, exceptions[exceptionIndex]);
                            exceptionIndex++;
                        }
                    };
                }
            };
            new SignatureReader(method.signature).accept(v);
            typeParameters = v.getTypeParameters();
        }

        Parameter[] parameters = new Parameter[parameterTypes.length];
        List<String> parameterNames = method.parameterNames;
        for (int i = 0; i < parameterTypes.length; i++) {
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
            result = new ConstructorNode(method.accessModifiers, parameters, exceptions, null);
        } else {
            result = new MethodNode(method.methodName, method.accessModifiers, returnType[0], parameters, exceptions, null);
            Object annDefault = method.annotationDefault;
            if (annDefault != null) {
                if (annDefault instanceof TypeWrapper) {
                    annDefault = resolver.resolveType(Type.getType(((TypeWrapper) annDefault).desc));
                }
                result.setCode(new ReturnStatement(new ConstantExpression(annDefault)));
                result.setAnnotationDefault(true);
            } else {
                // Seems wrong but otherwise some tests fail (e.g. TestingASTTransformsTest)
                result.setCode(new ReturnStatement(ConstantExpression.NULL));
            }

        }
        if (typeParameters != null && typeParameters.length > 0) {
            result.setGenericsTypes(typeParameters);
        }
        return result;
    }

    private static ClassNode applyErasure(ClassNode genericType, ClassNode erasure) {
        if (genericType.isArray() && erasure.isArray() && genericType.getComponentType().isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
            genericType.getComponentType().setRedirect(erasure.getComponentType());
        } else if (genericType.isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
        }
        return genericType;
    }

    static FieldNode createFieldNode(FieldStub field, AsmReferenceResolver resolver, DecompiledClassNode owner) {
        final ClassNode[] type = {resolver.resolveType(Type.getType(field.desc))};
        if (field.signature != null) {
            new SignatureReader(field.signature).accept(new TypeSignatureParser(resolver) {
                @Override
                void finished(ClassNode result) {
                    type[0] = applyErasure(result, type[0]);
                }
            });
        }
        ConstantExpression value = field.value == null ? null : new ConstantExpression(field.value);
        return new FieldNode(field.fieldName, field.accessModifiers, type[0], owner, value);
    }
}

