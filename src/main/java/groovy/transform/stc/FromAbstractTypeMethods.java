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
package groovy.transform.stc;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.LinkedList;
import java.util.List;

/**
 * This signature hint uses abstract methods from some type (abstract class or interface) in order
 * to infer the expected parameter types. This is especially useful for closure parameter type
 * inference when implicit closure coercion is in action.
 *
 * @since 2.3.0
 */
public class FromAbstractTypeMethods extends ClosureSignatureHint {
    @Override
    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final String[] options, final ASTNode usage) {
        String className = options[0];
        ClassNode cn = findClassNode(sourceUnit, compilationUnit, className);
        return extractSignaturesFromMethods(cn);
    }

    private static List<ClassNode[]> extractSignaturesFromMethods(final ClassNode cn) {
        List<MethodNode> methods = cn.getAllDeclaredMethods();
        List<ClassNode[]> signatures = new LinkedList<ClassNode[]>();
        for (MethodNode method : methods) {
            if (!method.isSynthetic() && method.isAbstract()) {
                extractParametersFromMethod(signatures, method);
            }
        }
        return signatures;
    }

    private static void extractParametersFromMethod(final List<ClassNode[]> signatures, final MethodNode method) {
        if (Traits.hasDefaultImplementation(method)) return;
        Parameter[] parameters = method.getParameters();
        ClassNode[] typeParams = new ClassNode[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            typeParams[i] = parameters[i].getOriginType();
        }
        signatures.add(typeParams);
    }
}
