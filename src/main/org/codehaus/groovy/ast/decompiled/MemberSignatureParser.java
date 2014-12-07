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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;

/**
 * @author Peter Gromov
 */
class MemberSignatureParser {
    static MethodNode createMethodNode(AsmReferenceResolver resolver, MethodStub method) {
        //todo method generics
        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        Parameter[] parameters = new Parameter[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            parameters[i] = new Parameter(resolver.resolveType(argumentTypes[i]), "param" + i);
        }

        for (Map.Entry<Integer, List<AnnotationStub>> entry : method.parameterAnnotations.entrySet()) {
            for (AnnotationStub stub : entry.getValue()) {
                parameters[entry.getKey()].addAnnotation(Annotations.createAnnotationNode(stub, resolver));
            }
        }

        ClassNode[] exceptions = new ClassNode[method.exceptions.length];
        for (int i = 0; i < method.exceptions.length; i++) {
            exceptions[i] = resolver.resolveClass(AsmDecompiler.fromInternalName(method.exceptions[i]));
        }

        return "<init>".equals(method.methodName) ?
                new ConstructorNode(method.accessModifiers, parameters, exceptions, null) :
                new MethodNode(method.methodName, method.accessModifiers, resolver.resolveType(Type.getReturnType(method.desc)), parameters, exceptions, null);
    }
}
