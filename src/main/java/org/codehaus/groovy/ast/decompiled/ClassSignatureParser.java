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

import org.apache.groovy.util.ObjectHolder;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class ClassSignatureParser {
    static void configureClass(ClassNode classNode, ClassStub stub, AsmReferenceResolver resolver) {
        if (stub.signature != null) {
            parseClassSignature(classNode, stub.signature, resolver);
            return;
        }

        if (stub.superName != null) {
            classNode.setSuperClass(resolver.resolveClass(AsmDecompiler.fromInternalName(stub.superName)));
        }

        ClassNode[] interfaces = new ClassNode[stub.interfaceNames.length];
        for (int i = 0; i < stub.interfaceNames.length; i++) {
            interfaces[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(stub.interfaceNames[i]));
        }
        classNode.setInterfaces(interfaces);
        if (!stub.permittedSubclasses.isEmpty()) {
            List<ClassNode> permittedSubclasses = classNode.getPermittedSubclasses();
            for (String name : stub.permittedSubclasses) {
                permittedSubclasses.add(resolver.resolveClass(AsmDecompiler.fromInternalName(name)));
            }
        }

        if (!stub.recordComponents.isEmpty()) {
            classNode.setRecordComponentNodes(stub.recordComponents.stream().map(r -> {
                ClassNode type = resolver.resolveType(Type.getType(r.descriptor));
                ObjectHolder<ClassNode> typeHolder = new ObjectHolder<>(type);
                if (null != r.signature) {
                    new SignatureReader(r.signature).accept(new TypeSignatureParser(resolver) {
                        @Override
                        void finished(final ClassNode result) {
                            typeHolder.setObject(applyErasure(result, typeHolder.getObject()));
                        }
                    });
                }
                ClassNode cn = typeHolder.getObject();
                Annotations.addTypeAnnotations(r, cn, resolver);
                RecordComponentNode recordComponentNode = new RecordComponentNode(classNode, r.name, cn);
                Annotations.addAnnotations(r, recordComponentNode, resolver);
                return recordComponentNode;
            }).collect(Collectors.toList()));
        }
    }

    private static void parseClassSignature(final ClassNode classNode, String signature, final AsmReferenceResolver resolver) {
        final List<ClassNode> interfaces = new ArrayList<ClassNode>();
        FormalParameterParser v = new FormalParameterParser(resolver) {

            @Override
            public SignatureVisitor visitSuperclass() {
                flushTypeParameter();
                return new TypeSignatureParser(resolver) {
                    @Override
                    void finished(ClassNode result) {
                        classNode.setSuperClass(result);
                    }
                };
            }

            @Override
            public SignatureVisitor visitInterface() {
                flushTypeParameter();
                return new TypeSignatureParser(resolver) {
                    @Override
                    void finished(ClassNode result) {
                        interfaces.add(result);
                    }
                };
            }

        };
        new SignatureReader(signature).accept(v);
        GenericsType[] typeParameters = v.getTypeParameters();
        if (typeParameters.length > 0) {
            classNode.setGenericsTypes(typeParameters);
        }
        classNode.setInterfaces(interfaces.toArray(ClassNode.EMPTY_ARRAY));
    }
}
