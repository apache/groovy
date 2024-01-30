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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.transform.trait.TraitASTTransformation;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.Collections;
import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType;

/**
 * An extension that handles field, super and static method calls within a trait.
 *
 * @since 2.3.0
 */
public class TraitTypeCheckingExtension extends AbstractTypeCheckingExtension {

    public TraitTypeCheckingExtension(final StaticTypeCheckingVisitor typeCheckingVisitor) {
        super(typeCheckingVisitor);
    }

    @Override
    public List<MethodNode> handleMissingMethod(final ClassNode receiver, final String name, final ArgumentListExpression argumentList, final ClassNode[] argumentTypes, final MethodCall call) {
        String[] decomposed = Traits.decomposeSuperCallName(name);
        if (decomposed != null) {
            String traitName = decomposed[0], methodName = decomposed[1];
            List<ClassNode> implementedTraits = Traits.findTraits(receiver);

            ClassNode nextTrait = null;
            for (int i = 0; i < implementedTraits.size() - 1; i += 1) {
                ClassNode implementedTrait = implementedTraits.get(i);
                if (implementedTrait.getName().equals(traitName)) {
                    nextTrait = implementedTraits.get(i + 1);
                }
            }

            ClassNode returnType = ClassHelper.OBJECT_TYPE;
            if (nextTrait != null) {
                List<MethodNode> candidates = typeCheckingVisitor.findMethod(nextTrait, methodName, argumentTypes);
                if (candidates.size() == 1) {
                    returnType = candidates.get(0).getReturnType();
                }
            }

            return Collections.singletonList(makeDynamic(call, returnType));
        }

        if (call instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) call;
            ClassNode returnType = mce.getNodeMetaData(TraitASTTransformation.DO_DYNAMIC);
            if (returnType != null) return Collections.singletonList(makeDynamic(call, returnType));

            // GROOVY-7322, GROOVY-8272, GROOVY-8587, GROOVY-8854, GROOVY-10312: trait: this.m($static$self)
            ClassNode targetClass = isClassClassNodeWrappingConcreteType(receiver)? receiver.getGenericsTypes()[0].getType(): receiver;
            if (Traits.isTrait(targetClass.getOuterClass()) && argumentTypes.length > 0 && ClassHelper.isClassType(argumentTypes[0])) {
                Parameter[] signature = java.util.Arrays.stream(argumentTypes).map(t -> new Parameter(t,"")).toArray(Parameter[]::new);
                List<ClassNode> traits = Traits.findTraits(targetClass.getOuterClass());
                traits.remove(targetClass.getOuterClass());

                for (ClassNode trait : traits) { // check super trait for static method
                    MethodNode method = Traits.findHelper(trait).getDeclaredMethod(name, signature);
                    if (method != null && method.isStatic()) {
                        return Collections.singletonList(makeDynamic(call, method.getReturnType()));
                    }
                }
            }
        }

        return Collections.emptyList();
    }
}
