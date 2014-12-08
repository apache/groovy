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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

/**
* @author Peter Gromov
*/
abstract class FormalParameterParser extends SignatureVisitor {
    private final AsmReferenceResolver resolver;
    private String currentTypeParameter;
    private List<ClassNode> parameterBounds = new ArrayList<ClassNode>();
    private final List<GenericsType> typeParameters = new ArrayList<GenericsType>();

    public FormalParameterParser(AsmReferenceResolver resolver) {
        super(Opcodes.ASM5);
        this.resolver = resolver;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        flushTypeParameter();
        currentTypeParameter = name;
    }

    protected void flushTypeParameter() {
        if (currentTypeParameter != null) {
            typeParameters.add(createFormalTypeParameter(currentTypeParameter, parameterBounds));
            currentTypeParameter = null;
            parameterBounds.clear();
        }
    }

    private static GenericsType createFormalTypeParameter(String name, List<ClassNode> bounds) {
        //todo duplicates Java 5
        ClassNode base = TypeSignatureParser.createTypeVariableReference(name);
        ClassNode redirect = base.redirect();
        base.setRedirect(null);
        GenericsType gt;
        if (bounds.isEmpty()) {
            gt = new GenericsType(base);
        } else {
            ClassNode[] cBounds = bounds.toArray(new ClassNode[bounds.size()]);
            gt = new GenericsType(base, cBounds, null);
            gt.setName(base.getName());
            gt.setPlaceholder(true);
        }
        base.setRedirect(redirect);
        return gt;
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

    public GenericsType[] getTypeParameters() {
        flushTypeParameter();
        return typeParameters.toArray(new GenericsType[typeParameters.size()]);
    }
}
