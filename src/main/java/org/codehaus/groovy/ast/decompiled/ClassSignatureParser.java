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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

class ClassSignatureParser {

    static void configureClass(final ClassNode classNode, final ClassStub stub, final AsmReferenceResolver resolver) {
        if (stub.signature != null) {
            parseClassSignature(classNode, stub.signature, resolver);
            return;
        }

        if (stub.superName != null) {
            ClassNode sc = resolver.resolveClass(AsmDecompiler.fromInternalName(stub.superName));
            classNode.setSuperClass(sc);
        }

        {
            final int nInterfaces = stub.interfaceNames.length;
            ClassNode[] interfaces = new ClassNode[nInterfaces];
            for (int i = 0; i < nInterfaces; i += 1) { String name = stub.interfaceNames[i];
                interfaces[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(name));
            }
            classNode.setInterfaces(interfaces);
        }

        if (!stub.permittedSubclasses.isEmpty()) {
            List<ClassNode> permitted = classNode.getPermittedSubclasses(); // collector
            for (String name : stub.permittedSubclasses) {
                ClassNode ps = resolver.resolveClass(AsmDecompiler.fromInternalName(name));
                permitted.add(ps);
            }
        }

        if (!stub.recordComponents.isEmpty()) {
            List<RecordComponentNode> recordComponents =
                    new ArrayList<>(stub.recordComponents.size());
            for (RecordComponentStub rc : stub.recordComponents) {
                ClassNode[] type = {resolver.resolveType(Type.getType(rc.descriptor))};
                if (rc.signature != null) {
                    new SignatureReader(rc.signature).accept(new TypeSignatureParser(resolver) {
                        @Override
                        void finished(final ClassNode result) {
                            type[0] = applyErasure(result, type[0]);
                        }
                    });
                } else {
                    type[0] = type[0].getPlainNodeReference();
                }

                RecordComponentNode recordComponent = new RecordComponentNode(classNode, rc.name, type[0]);
                Annotations.addAnnotations(rc, recordComponent, resolver);
                Annotations.addTypeAnnotations(rc, type[0], resolver);
                recordComponents.add(recordComponent);
            }
            classNode.setRecordComponents(recordComponents);
        }
    }

    private static void parseClassSignature(final ClassNode classNode, final String signature, final AsmReferenceResolver resolver) {
        List<ClassNode> interfaces = new ArrayList<>();

        FormalParameterParser parser = new FormalParameterParser(resolver) {
            @Override
            public SignatureVisitor visitSuperclass() {
                flushTypeParameter();
                return new TypeSignatureParser(resolver) {
                    @Override
                    void finished(final ClassNode superClass) {
                        classNode.setSuperClass(superClass);
                    }
                };
            }
            @Override
            public SignatureVisitor visitInterface() {
                flushTypeParameter();
                return new TypeSignatureParser(resolver) {
                    @Override
                    void finished(final ClassNode superInterface) {
                        interfaces.add(superInterface);
                    }
                };
            }
        };
        new SignatureReader(signature).accept(parser);

        classNode.setInterfaces(interfaces.isEmpty() ? ClassNode.EMPTY_ARRAY :
                                    interfaces.toArray(ClassNode.EMPTY_ARRAY));

        GenericsType[] typeParameters = parser.getTypeParameters();
        if (typeParameters.length > 0) classNode.setGenericsTypes(typeParameters);
    }
}
