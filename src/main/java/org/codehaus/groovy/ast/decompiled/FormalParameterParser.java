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
import org.codehaus.groovy.vmplugin.v8.Java8;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.control.CompilerConfiguration.ASM_API_VERSION;

abstract class FormalParameterParser extends SignatureVisitor {

    private String currentTypeParameter;
    private final List<ClassNode> parameterBounds = new ArrayList<>();
    private final List<GenericsType> typeParameters = new ArrayList<>();

    private final AsmReferenceResolver resolver;

    public FormalParameterParser(final AsmReferenceResolver resolver) {
        super(ASM_API_VERSION);
        this.resolver = resolver;
    }

    protected void flushTypeParameter() {
        if (currentTypeParameter != null) {
            ClassNode ref = Java8.configureTypeVariableReference(currentTypeParameter);
            ClassNode[] theBoundTypes = parameterBounds.toArray(ClassNode.EMPTY_ARRAY);
            typeParameters.add(Java8.configureTypeVariableDefinition(ref, theBoundTypes));

            parameterBounds.clear();
            currentTypeParameter = null;
        }
    }

    public GenericsType[] getTypeParameters() {
        flushTypeParameter();
        return typeParameters.toArray(GenericsType.EMPTY_ARRAY);
    }

    @Override
    public void visitFormalTypeParameter(final String name) {
        flushTypeParameter();
        currentTypeParameter = name;
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
}
