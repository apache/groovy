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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.vmplugin.v5.Java5;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

abstract class TypeSignatureParser extends SignatureVisitor {
    private final AsmReferenceResolver resolver;

    public TypeSignatureParser(AsmReferenceResolver resolver) {
        super(CompilerConfiguration.ASM_API_VERSION);
        this.resolver = resolver;
    }

    abstract void finished(ClassNode result);

    private String baseName;
    private final List<GenericsType> arguments = new ArrayList<GenericsType>();

    @Override
    public void visitTypeVariable(String name) {
        finished(Java5.configureTypeVariableReference(name));
    }

    @Override
    public void visitBaseType(char descriptor) {
        finished(resolver.resolveType(Type.getType(String.valueOf(descriptor))));
    }

    @Override
    public SignatureVisitor visitArrayType() {
        final TypeSignatureParser outer = this;
        return new TypeSignatureParser(resolver) {
            @Override
            void finished(ClassNode result) {
                outer.finished(result.makeArray());
            }
        };
    }

    @Override
    public void visitClassType(String name) {
        baseName = AsmDecompiler.fromInternalName(name);
    }

    @Override
    public void visitTypeArgument() {
        arguments.add(createWildcard(new ClassNode[]{ClassHelper.OBJECT_TYPE}, null));
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        return new TypeSignatureParser(resolver) {
            @Override
            void finished(ClassNode result) {
                if (wildcard == INSTANCEOF) {
                    arguments.add(new GenericsType(result));
                    return;
                }

                ClassNode[] upper = wildcard == EXTENDS ? new ClassNode[]{result} : null;
                ClassNode lower = wildcard == SUPER ? result : null;
                arguments.add(createWildcard(upper, lower));
            }
        };
    }

    private static GenericsType createWildcard(ClassNode[] upper, ClassNode lower) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);
        GenericsType t = new GenericsType(base, upper, lower);
        t.setWildcard(true);
        return t;
    }

    @Override
    public void visitInnerClassType(String name) {
        baseName += "$" + name;
        arguments.clear();
    }

    @Override
    public void visitEnd() {
        ClassNode base = resolver.resolveClass(baseName);
        if (arguments.isEmpty()) {
            finished(base);
            return;
        }

        ClassNode bound = base.getPlainNodeReference();
        bound.setGenericsTypes(arguments.toArray(GenericsType.EMPTY_ARRAY));
        finished(bound);
    }

}
