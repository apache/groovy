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
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.trait.TraitASTTransformation;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.isClassClassNodeWrappingConcreteType;

/**
 * A type checking extension that will take care of handling errors which are specific to traits. In particular, it will
 * handle the "super" method calls within a trait.
 *
 * @since 2.3.0
 */
public class TraitTypeCheckingExtension extends AbstractTypeCheckingExtension {
    private static final List<MethodNode> NOTFOUND = Collections.emptyList();

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
        if (call instanceof MethodCallExpression) {
            MethodCallExpression mce = (MethodCallExpression) call;
            if (mce.getReceiver() instanceof VariableExpression) {
                VariableExpression var = (VariableExpression) mce.getReceiver();

                // GROOVY-7322
                // static method call in trait?
                ClassNode type = null;
                if (isStaticTraitReceiver(receiver, var)) {
                    type = receiver.getGenericsTypes()[0].getType();
                } else if (isThisTraitReceiver(var)) {
                    type = receiver;
                }
                if (Traits.isTrait(type) && !(type instanceof UnionTypeClassNode)) {
                    ClassNode helper = Traits.findHelper(type);
                    Parameter[] params = new Parameter[argumentTypes.length + 1];
                    params[0] = new Parameter(ClassHelper.CLASS_Type.getPlainNodeReference(), "staticSelf");
                    for (int i = 1; i < params.length; i++) {
                        params[i] = new Parameter(argumentTypes[i-1], "p" + i);
                    }
                    MethodNode method = helper.getDeclaredMethod(name, params);
                    if (method != null) {
                        return Collections.singletonList(makeDynamic(call, method.getReturnType()));
                    }
                }
            }

            ClassNode dynamic = mce.getNodeMetaData(TraitASTTransformation.DO_DYNAMIC);
            if (dynamic!=null) {
                return Collections.singletonList(makeDynamic(call, dynamic));
            }
        }
        return NOTFOUND;
    }

    private static boolean isStaticTraitReceiver(final ClassNode receiver, final VariableExpression var) {
        return Traits.STATIC_THIS_OBJECT.equals(var.getName()) && isClassClassNodeWrappingConcreteType(receiver);
    }

    private static boolean isThisTraitReceiver(final VariableExpression var) {
        return Traits.THIS_OBJECT.equals(var.getName());
    }

    private List<MethodNode> convertToDynamicCall(MethodCall call, ClassNode receiver, String[] decomposed, ClassNode[] argumentTypes) {
        String traitName = decomposed[0];
        String name = decomposed[1];
        LinkedHashSet<ClassNode> traitsAsList = Traits.collectAllInterfacesReverseOrder(receiver, new LinkedHashSet<>());
        ClassNode[] implementedTraits = traitsAsList.toArray(ClassNode.EMPTY_ARRAY);
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

        return Collections.singletonList(makeDynamic(call, inferredReturnType));
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
