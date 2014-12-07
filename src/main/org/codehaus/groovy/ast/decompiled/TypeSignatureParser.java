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
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

/**
* @author Peter Gromov
*/
abstract class TypeSignatureParser extends SignatureVisitor {
    private final AsmReferenceResolver resolver;

    public TypeSignatureParser(AsmReferenceResolver resolver) {
        super(Opcodes.ASM5);
        this.resolver = resolver;
    }

    abstract void finished(ClassNode result);

    ClassNode base;
    private List<GenericsType> arguments = new ArrayList<GenericsType>();

    @Override
    public void visitTypeVariable(String name) {
        //todo duplicates Java5
        ClassNode cn = ClassHelper.makeWithoutCaching(name);
        cn.setGenericsPlaceHolder(true);
        ClassNode cn2 = ClassHelper.makeWithoutCaching(name);
        cn2.setGenericsPlaceHolder(true);
        GenericsType[] gts = new GenericsType[]{new GenericsType(cn2)};
        cn.setGenericsTypes(gts);
        cn.setRedirect(ClassHelper.OBJECT_TYPE);
        finished(cn);
    }

    @Override
    public void visitBaseType(char descriptor) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public SignatureVisitor visitArrayType() {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public void visitClassType(String name) {
        base = resolver.resolveClass(AsmDecompiler.fromInternalName(name));
    }

    @Override
    public void visitTypeArgument() {
//            todo ?
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

                //todo duplicates Java 5
                ClassNode base = ClassHelper.makeWithoutCaching("?");
                base.setRedirect(ClassHelper.OBJECT_TYPE);

                ClassNode[] upper = wildcard == EXTENDS ? new ClassNode[]{result} : null;
                ClassNode lower = wildcard == SUPER ? result : null;
                GenericsType t = new GenericsType(base, upper, lower);
                t.setWildcard(true);
                arguments.add(t);
            }
        };
    }

    @Override
    public void visitInnerClassType(String name) {
        throw new UnsupportedOperationException(); //todo
    }

    @Override
    public void visitEnd() {
        if (arguments.isEmpty()) {
            finished(base);
            return;
        }

        ClassNode bound = base.getPlainNodeReference();
        bound.setGenericsTypes(arguments.toArray(new GenericsType[arguments.size()]));
        finished(bound);
    }

}
