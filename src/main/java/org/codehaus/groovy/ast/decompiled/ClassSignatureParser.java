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

/**
 * Parses Java generic type signatures from compiled class bytecode according to JVMS &sect;4.7.9.1.
 * <p>
 * This parser extracts type parameters, superclass type information, and interface type information
 * from a class's generic signature attribute. The signature format follows the Java Virtual Machine
 * Specification encoding for generic types:
 * <ul>
 *   <li>Generic class with type parameters: {@code <T:Ljava/lang/Object;>Ljava/lang/Object;Ljava/io/Serializable;}</li>
 *   <li>Parameterized superclass: {@code Ljava/util/AbstractList<TT;>;}</li>
 *   <li>Type variable reference: {@code TT;} represents a type variable named "T"</li>
 * </ul>
 * <p>
 * When a class has a generic signature, it is parsed to extract:
 * <ul>
 *   <li>Formal type parameters with their bounds (e.g., {@code T extends Number})</li>
 *   <li>Parameterized superclass type with type arguments</li>
 *   <li>Parameterized interface types with type arguments</li>
 *   <li>Record component types for Java 16+ record classes</li>
 * </ul>
 *
 * @see GenericsType for the representation of generic type information
 * @see FormalParameterParser for parsing type parameter declarations
 * @see TypeSignatureParser for parsing individual type signatures
 * @see MemberSignatureParser for parsing method and field signatures
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se19/html/jvms-4.html#jvms-4.7.9.1">
 *      JVMS §4.7.9.1 Signatures</a>
 */
class ClassSignatureParser {

    /**
     * Configures the given class node with type information extracted from the class stub.
     * <p>
     * This method processes the class's generic signature (if present) and resolves:
     * <ul>
     *   <li>Generic type parameters with their bounds</li>
     *   <li>Superclass type (with type arguments if parameterized)</li>
     *   <li>Interface types (with type arguments if parameterized)</li>
     *   <li>Permitted subclasses for sealed classes</li>
     *   <li>Record component types for record classes</li>
     * </ul>
     * <p>
     * If no generic signature is present, uses the class's raw superclass and interface information.
     *
     * @param classNode the class node to configure (modified in place)
     * @param stub the ASM-derived class metadata containing signature and class info
     * @param resolver used to resolve class types and references from internal names
     */
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

    /**
     * Parses a class's generic signature and configures the class node with type information.
     * <p>
     * Parses the generic signature according to JVMS &sect;4.7.9.1 to extract:
     * <ul>
     *   <li>Formal type parameters and their bounds (e.g., {@code <T extends Number>})</li>
     *   <li>Generic superclass with type arguments (e.g., {@code extends List<String>})</li>
     *   <li>Generic interfaces with type arguments (e.g., {@code implements Comparable<T>})</li>
     * </ul>
     * <p>
     * Example signature format (JVMS notation):
     * {@code <T:Ljava/lang/Number;>Ljava/util/AbstractList<TT;>;Ljava/io/Serializable;}
     * represents: {@code <T extends Number> extends AbstractList<T> implements Serializable}
     *
     * @param classNode the class node to configure (modified in place with superclass, interfaces, and generics)
     * @param signature the generic signature string from the class's Signature attribute, in JVMS format
     * @param resolver used to resolve class types from internal names to ClassNodes
     *
     * @see FormalParameterParser for parsing type parameter declarations
     * @see TypeSignatureParser for parsing type expressions
     * @see <a href="https://docs.oracle.com/javase/specs/jvms/se19/html/jvms-4.html#jvms-ClassSignature">
     *      JVMS §4.7.9.1 ClassSignature Production</a>
     */
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
