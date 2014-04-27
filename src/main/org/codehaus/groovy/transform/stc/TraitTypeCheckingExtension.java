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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A type checking extension that will take care of handling errors which are specific to traits. In particular, it will
 * hanldle the "super" method calls within a trait.
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
public class TraitTypeCheckingExtension extends AbstractTypeCheckingExtension {
    private final static List<MethodNode> NOTFOUND = Collections.emptyList();

    /**
     * Builds a type checking extension relying on a Groovy script (type checking DSL).
     *
     * @param typeCheckingVisitor the type checking visitor
     */
    public TraitTypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
    }

    @Override
    public void setup() {
    }

    @Override
    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCall call) {
        String[] decomposed = Traits.decomposeSuperCallName(name);
        if (decomposed != null) {
            return convertToDynamicCall(call, receiver, decomposed, argumentTypes);
        }
        return NOTFOUND;
    }

    private List<MethodNode> convertToDynamicCall(MethodCall call, ClassNode receiver, String[] decomposed, ClassNode[] argumentTypes) {
        String traitName = decomposed[0];
        String name = decomposed[1];
        LinkedHashSet<ClassNode> traitsAsList = Traits.collectAllInterfacesReverseOrder(receiver, new LinkedHashSet<ClassNode>());
        ClassNode[] implementedTraits = traitsAsList.toArray(new ClassNode[traitsAsList.size()]);
        ClassNode nextTrait = null;
        for (int i = 0; i < implementedTraits.length - 1; i++) {
            ClassNode implementedTrait = implementedTraits[i];
            if (implementedTrait.getName().equals(traitName)) {
                nextTrait = implementedTraits[i + 1];
            }
        }
        ClassNode[] newArgs = new ClassNode[argumentTypes.length];
        System.arraycopy(argumentTypes, 0, newArgs, 0, newArgs.length);
        ClassNode inferredReturnType = inferTraitMethodReturnType(nextTrait, name, newArgs);

        return Arrays.asList(makeDynamic(call, inferredReturnType));
    }

    private ClassNode inferTraitMethodReturnType(ClassNode nextTrait, String methodName, ClassNode[] paramTypes) {
        ClassNode result = ClassHelper.OBJECT_TYPE;
        if (nextTrait != null) {
            List<MethodNode> candidates = typeCheckingVisitor.findMethod(nextTrait, methodName, paramTypes);
            if (candidates.size() == 1) {
                result = candidates.get(0).getReturnType();
            }
        }
        return result;
    }

}
