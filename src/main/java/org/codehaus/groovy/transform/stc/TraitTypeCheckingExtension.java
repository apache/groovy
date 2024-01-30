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

import static org.codehaus.groovy.ast.tools.GenericsUtils.makeClassSafe0;
import static org.codehaus.groovy.ast.tools.ParameterUtils.parametersCompatible;
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

    private static Parameter[] asParameters(final ClassNode traitClass, final ClassNode[] argumentTypes) {
        ClassNode classType = makeClassSafe0(ClassHelper.CLASS_Type, traitClass.asGenericsType());

        Parameter[] parameters = new Parameter[1 + argumentTypes.length];
        parameters[0] = new Parameter(classType,"self");
        for (int i = 1; i < parameters.length; i += 1) {
            parameters[i] = new Parameter(argumentTypes[i - 1], "p" + i);
        }
        return parameters;
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

            // GROOVY-7191, GROOVY-7322, GROOVY-8272, GROOVY-8587, GROOVY-10106, GROOVY-10312: STC: (this or $self or $static$self).m()
            ClassNode targetClass = isClassClassNodeWrappingConcreteType(receiver)? receiver.getGenericsTypes()[0].getType(): receiver;
            if (targetClass.getName().endsWith("$Trait$Helper")) targetClass = targetClass.getOuterClass();
            if (Traits.isTrait(targetClass)) {
                for (ClassNode trait : Traits.findTraits(targetClass)) {
                    for (MethodNode method : Traits.findHelper(trait).getDeclaredMethods(name)) {
                        if (method.isPublic() && method.isStatic() && parametersCompatible(
                                asParameters(trait, argumentTypes), method.getParameters())) {
                            return Collections.singletonList(makeDynamic(call, method.getReturnType()));
                        }
                    }
                }
            }
        }

        return Collections.emptyList();
    }
}
