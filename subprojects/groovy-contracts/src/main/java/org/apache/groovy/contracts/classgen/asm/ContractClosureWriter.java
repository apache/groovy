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
package org.apache.groovy.contracts.classgen.asm;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveVoid;
import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

/**
 * <p>Replaces annotation closures with closure implementation classes.</p>
 *
 * <p>Attention: large parts of this class have been backported from Groovy 1.8 and customized
 * for usage in groovy-contracts.</p>
 */
public class ContractClosureWriter {

    private int closureCount = 1;

    /**
     * Generates a synthetic closure implementation class for the supplied contract closure.
     *
     * @param classNode the declaring class
     * @param methodNode the owning method or constructor, or {@code null} for class invariants
     * @param expression the rewritten closure body
     * @param addOldVariable whether to expose the {@code old} map parameter
     * @param addResultVariable whether to expose the {@code result} parameter
     * @param mods the access modifiers to apply to the generated class
     * @return the generated closure implementation class
     */
    public ClassNode createClosureClass(ClassNode classNode, MethodNode methodNode, ClosureExpression expression, boolean addOldVariable, boolean addResultVariable, int mods) {
        ClassNode outerClass = getOutermostClass(classNode);
        String name = outerClass.getName() + "$" + getClosureInnerName(outerClass, classNode);

        // fetch all method parameters, and possibly add 'old' and 'result'
        List<Parameter> parametersTemp = new ArrayList<>(Arrays.asList(expression.getParameters()));
        removeParameter("old", parametersTemp);
        removeParameter("result", parametersTemp);

        if (methodNode != null && addResultVariable && !isPrimitiveVoid(methodNode.getReturnType())) {
            parametersTemp.add(new Parameter(methodNode.getReturnType(), "result"));
        }

        if (addOldVariable) {
            parametersTemp.add(new Parameter(new ClassNode(Map.class), "old"));
        }

        // contains all params of the original method
        List<Parameter> closureParameters = new ArrayList<>();
        for (Parameter param : parametersTemp) {
            closureParameters.add(new Parameter(plainNodeReferenceWithGenerics(param.getType()), param.getName()));
        }

        ClassNode answer = new ClassNode(name, mods | ACC_FINAL, ClassHelper.CLOSURE_TYPE.getPlainNodeReference());
        answer.setSourcePosition(expression);
        answer.setSynthetic(true);

        MethodNode doCall = answer.addMethod("doCall", ACC_PUBLIC, ClassHelper.Boolean_TYPE, closureParameters.toArray(Parameter.EMPTY_ARRAY), ClassNode.EMPTY_ARRAY, expression.getCode());
        doCall.setSourcePosition(expression);

        VariableScope varScope = expression.getVariableScope();
        if (varScope == null) {
            throw new RuntimeException("Must have a VariableScope by now! for expression: " + expression + " class: " + name);
        } else {
            doCall.setVariableScope(varScope.copy());
        }

        // let's add a typesafe call method
        ArgumentListExpression arguments = new ArgumentListExpression();
        for (Parameter parameter : closureParameters) {
            arguments.addExpression(varX(parameter));
        }

        MethodNode call = answer.addMethod(
                "call",
                ACC_PUBLIC,
                ClassHelper.Boolean_TYPE,
                closureParameters.toArray(Parameter.EMPTY_ARRAY),
                ClassNode.EMPTY_ARRAY,
                returnS(callThisX("doCall", arguments))
        );
        call.setSourcePosition(expression);
        call.setSynthetic(true);

        // let's make the constructor
        BlockStatement block = block();
        // this block does not get a source position, because we don't
        // want this synthetic constructor to show up in corbertura reports
        VariableExpression outer = varX("_outerInstance");
        outer.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(outer);
        VariableExpression thisObject = varX("_thisObject");
        thisObject.setSourcePosition(expression);
        block.getVariableScope().putReferencedLocalVariable(thisObject);
        block.addStatement(stmt(ctorSuperX(args(outer, thisObject))));

        Parameter[] ctorParams = {
                new Parameter(ClassHelper.OBJECT_TYPE, "_outerInstance"),
                new Parameter(ClassHelper.OBJECT_TYPE, "_thisObject")
        };
        var ctor = answer.addConstructor(ACC_PUBLIC, ctorParams, ClassNode.EMPTY_ARRAY, block);
        ctor.setSourcePosition(expression);
        correctAccessedVariable(doCall, expression);
        return answer;
    }

    private void removeParameter(String name, List<Parameter> parameters) {
        parameters.removeIf(parameter -> parameter.getName().equals(name));
    }

    /**
     * Returns a plain node reference for the supplied closure-parameter type but, unlike
     * {@link ClassNode#getPlainNodeReference()} on its own, carries over any concrete generics
     * information (GROOVY-12071). This lets static type checking of a contract closure see, for
     * example, that a {@code result} parameter typed {@code Map<String, Integer>} yields
     * {@code Integer} values, removing the need for explicit casts in {@code @Ensures} conditions.
     * <p>
     * Generics that mention a type-parameter placeholder (e.g. the {@code T} of a {@code <T> T m(...)}
     * method, or the {@code Class<T>} of one of its parameters) are dropped: the generated closure
     * class does not declare those type parameters, so carrying the placeholder through would leave
     * an unresolved type variable that breaks downstream generic-signature handling (e.g. JavaBeans
     * introspection of the closure class).
     */
    private static ClassNode plainNodeReferenceWithGenerics(ClassNode type) {
        ClassNode ref = type.getPlainNodeReference();
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes != null && genericsTypes.length > 0 && !usesPlaceholder(type)) {
            ref.setGenericsTypes(genericsTypes);
            ref.setUsingGenerics(true);
        }
        return ref;
    }

    /**
     * Reports whether {@code type} mentions a generics placeholder (an unresolved type-parameter
     * reference such as {@code T}) at any nesting depth of its generics or array component.
     */
    private static boolean usesPlaceholder(ClassNode type) {
        if (type == null) return false;
        if (type.isArray()) return usesPlaceholder(type.getComponentType());
        if (type.isGenericsPlaceHolder()) return true;
        GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes != null) {
            for (GenericsType gt : genericsTypes) {
                if (gt.isPlaceholder()) return true;
                if (gt.isWildcard()) {
                    if (usesPlaceholder(gt.getLowerBound())) return true;
                    ClassNode[] upperBounds = gt.getUpperBounds();
                    if (upperBounds != null) {
                        for (ClassNode upperBound : upperBounds) {
                            if (usesPlaceholder(upperBound)) return true;
                        }
                    }
                } else if (usesPlaceholder(gt.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private ClassNode getOutermostClass(ClassNode outermostClass) {
        while (outermostClass instanceof InnerClassNode) {
            outermostClass = outermostClass.getOuterClass();
        }
        return outermostClass;
    }

    private void correctAccessedVariable(MethodNode methodNode, ClosureExpression ce) {
        CodeVisitorSupport visitor = new CodeVisitorSupport() {
            @Override
            public void visitVariableExpression(VariableExpression expression) {
                Variable v = expression.getAccessedVariable();
                if (v == null) return;
                String name = expression.getName();
                if (v instanceof DynamicVariable) {
                    for (Parameter param : methodNode.getParameters()) {
                        if (name.equals(param.getName())) {
                            expression.setAccessedVariable(param);
                        }
                    }

                }
            }
        };
        visitor.visitClosureExpression(ce);
    }

    private String getClosureInnerName(ClassNode owner, ClassNode enclosingClass) {
        String ownerShortName = owner.getNameWithoutPackage();
        String classShortName = enclosingClass.getNameWithoutPackage();
        if (classShortName.equals(ownerShortName)) {
            classShortName = "";
        } else {
            classShortName += "_";
        }
        // remove $
        int dp = classShortName.lastIndexOf("$");
        if (dp >= 0) {
            classShortName = classShortName.substring(++dp);
        }
        // remove leading _
        if (classShortName.startsWith("_")) {
            classShortName = classShortName.substring(1);
        }

        return "_gc_" + classShortName + "closure" + closureCount++;
    }
}
