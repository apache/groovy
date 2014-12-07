/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.ast.decompiled;

import groovy.lang.Reference;
import org.codehaus.groovy.ast.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.List;
import java.util.Map;

/**
 * @author Peter Gromov
 */
class MemberSignatureParser {
    static MethodNode createMethodNode(final AsmReferenceResolver resolver, MethodStub method) {
        GenericsType[] typeParameters = null;

        Type[] argumentTypes = Type.getArgumentTypes(method.desc);

        final ClassNode[] parameterTypes = new ClassNode[argumentTypes.length];
        final ClassNode[] exceptions = new ClassNode[method.exceptions.length];
        final Reference<ClassNode> returnType = new Reference<ClassNode>();

        if (method.signature != null) {
            FormalParameterParser v = new FormalParameterParser(resolver) {
                int paramIndex = 0;
                @Override
                public SignatureVisitor visitParameterType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            parameterTypes[paramIndex++] = result;
                        }
                    };
                }

                @Override
                public SignatureVisitor visitReturnType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            returnType.set(result);
                        }
                    };
                }

                int exceptionIndex = 0;
                @Override
                public SignatureVisitor visitExceptionType() {
                    return new TypeSignatureParser(resolver) {
                        @Override
                        void finished(ClassNode result) {
                            exceptions[exceptionIndex++] = result;
                        }
                    };
                }
            };
            new SignatureReader(method.signature).accept(v);
            typeParameters = v.getTypeParameters();
        } else {
            for (int i = 0; i < argumentTypes.length; i++) {
                parameterTypes[i] = resolver.resolveType(argumentTypes[i]);
            }

            for (int i = 0; i < method.exceptions.length; i++) {
                exceptions[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(method.exceptions[i]));
            }
        }


        Parameter[] parameters = new Parameter[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = new Parameter(parameterTypes[i], "param" + i);
        }

        for (Map.Entry<Integer, List<AnnotationStub>> entry : method.parameterAnnotations.entrySet()) {
            for (AnnotationStub stub : entry.getValue()) {
                parameters[entry.getKey()].addAnnotation(Annotations.createAnnotationNode(stub, resolver));
            }
        }

        MethodNode result;
        if ("<init>".equals(method.methodName)) {
            result = new ConstructorNode(method.accessModifiers, parameters, exceptions, null);
        } else {
            if (returnType.get() == null) {
                returnType.set(resolver.resolveType(Type.getReturnType(method.desc)));
            }
            result = new MethodNode(method.methodName, method.accessModifiers, returnType.get(), parameters, exceptions, null);
        }
        result.setGenericsTypes(typeParameters);
        return result;
    }

    static FieldNode createFieldNode(FieldStub field, AsmReferenceResolver resolver, DecompiledClassNode owner) {
        final Reference<ClassNode> type = new Reference<ClassNode>();
        if (field.signature != null) {
            new SignatureReader(field.signature).accept(new TypeSignatureParser(resolver) {
                @Override
                void finished(ClassNode result) {
                    type.set(result);
                }
            });
        } else {
            type.set(resolver.resolveType(Type.getType(field.desc)));
        }
        return new FieldNode(field.fieldName, field.accessModifiers, type.get(), owner, null);
    }
}

