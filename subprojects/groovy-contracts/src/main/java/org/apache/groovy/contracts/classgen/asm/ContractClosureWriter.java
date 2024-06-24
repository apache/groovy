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
            closureParameters.add(new Parameter(param.getType().getPlainNodeReference(), param.getName()));
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
        BlockStatement block = new BlockStatement();
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
