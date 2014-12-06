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

import jdk.internal.org.objectweb.asm.Opcodes;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Gromov
 */
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
    }

    private static void parseClassSignature(final ClassNode classNode, String signature, final AsmReferenceResolver resolver) {
        final List<GenericsType> typeParameters = new ArrayList<GenericsType>();
        final List<ClassNode> interfaces = new ArrayList<ClassNode>();
        new SignatureReader(signature).accept(new SignatureVisitor(Opcodes.ASM5) {
            String currentTypeParameter;
            List<ClassNode> parameterBounds = new ArrayList<ClassNode>();

            @Override
            public void visitFormalTypeParameter(String name) {
                flushTypeParameter();
                currentTypeParameter = name;
            }

            private void flushTypeParameter() {
                if (currentTypeParameter != null) {
                    ClassNode[] upperBounds = parameterBounds.toArray(new ClassNode[parameterBounds.size()]);
                    ClassNode param = ClassHelper.make(currentTypeParameter);
                    param.setGenericsPlaceHolder(true);
                    typeParameters.add(new GenericsType(param, upperBounds, null));
                    currentTypeParameter = null;
                    parameterBounds.clear();
                }
            }

            @Override
            public SignatureVisitor visitClassBound() {
                return new TypeSignatureParser(resolver) {
                    @Override
                    void finished(ClassNode result) {
                        parameterBounds.add(result);
                    }
                };
            }

            @Override
            public SignatureVisitor visitInterfaceBound() {
                return visitClassBound();
            }

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

        });
        classNode.setGenericsTypes(typeParameters.toArray(new GenericsType[typeParameters.size()]));
        classNode.setInterfaces(interfaces.toArray(new ClassNode[interfaces.size()]));
    }
}
