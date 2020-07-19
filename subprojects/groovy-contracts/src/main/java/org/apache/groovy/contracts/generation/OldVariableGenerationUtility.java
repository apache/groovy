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
package org.apache.groovy.contracts.generation;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.objectweb.asm.Opcodes;

import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.fieldX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;

/**
 * <p>Central place where code generation for the <tt>old</tt> closure variable
 * takes place.</p>
 */
public class OldVariableGenerationUtility {

    public static final String OLD_VARIABLES_METHOD = "$_gc_computeOldVariables";

    /**
     * Creates a synthetic method handling generation of the <tt>old</tt> variable map. If a super class declares
     * the same synthetic method it will be called and the results will be merged.
     *
     * @param classNode which contains postconditions, so an old variable generating method makes sense here.
     */
    public static void addOldVariableMethodNode(final ClassNode classNode) {
        if (classNode.getDeclaredMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null) return;

        final BlockStatement methodBlockStatement = new BlockStatement();

        final MapExpression oldVariablesMap = new MapExpression();

        // create variable assignments for old variables
        for (final FieldNode fieldNode : classNode.getFields()) {
            if (fieldNode.getName().startsWith("$")) continue;

            final ClassNode fieldType = ClassHelper.getWrapper(fieldNode.getType());

            if (fieldType.getName().startsWith("java.lang") || ClassHelper.isPrimitiveType(fieldType) || fieldType.getName().startsWith("java.math") ||
                    fieldType.getName().startsWith("java.util") ||
                    fieldType.getName().startsWith("java.sql") ||
                    fieldType.getName().equals("groovy.lang.GString") ||
                    fieldType.getName().equals("java.lang.String")) {

                MethodNode cloneMethod = fieldType.getMethod("clone", Parameter.EMPTY_ARRAY);
                // if a clone classNode is available, the value is cloned
                if (cloneMethod != null && fieldType.implementsInterface(ClassHelper.make("java.lang.Cloneable"))) {

                    final MethodCallExpression cloneField = callX(fieldX(fieldNode), "clone");
                    // return null if field is null
                    cloneField.setSafe(true);

                    VariableExpression oldVariable = localVarX("$old$" + fieldNode.getName(), fieldNode.getType());
                    Statement oldVariableAssignment = declS(oldVariable, cloneField);

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(new MapEntryExpression(constX(oldVariable.getName().substring("$old$".length())), oldVariable));

                } else if (ClassHelper.isPrimitiveType(fieldType)
                        || ClassHelper.isNumberType(fieldType)
                        || fieldType.getName().startsWith("java.math")
                        || fieldType.getName().equals("groovy.lang.GString")
                        || fieldType.getName().equals("java.lang.String")) {

                    VariableExpression oldVariable = localVarX("$old$" + fieldNode.getName(), fieldNode.getType());
                    Statement oldVariableAssignment = declS(oldVariable, fieldX(fieldNode));

                    methodBlockStatement.addStatement(oldVariableAssignment);
                    oldVariablesMap.addMapEntryExpression(new MapEntryExpression(constX(oldVariable.getName().substring("$old$".length())), oldVariable));
                }
            }
        }

        VariableExpression oldVariable = localVarX("old", new ClassNode(Map.class));
        methodBlockStatement.addStatement(declS(oldVariable, oldVariablesMap));
        VariableExpression mergedOldVariables = null;

        // let's ask the super class for old variables...
        if (classNode.getSuperClass() != null && classNode.getSuperClass().getMethod(OLD_VARIABLES_METHOD, Parameter.EMPTY_ARRAY) != null) {
            mergedOldVariables = localVarX("mergedOldVariables", new ClassNode(Map.class));
            methodBlockStatement.addStatement(declS(mergedOldVariables,
                    callX(oldVariable, "plus", args(callSuperX(OLD_VARIABLES_METHOD)))));
        }

        methodBlockStatement.addStatement(returnS(mergedOldVariables != null ? mergedOldVariables : oldVariable));

        final MethodNode preconditionMethodNode = classNode.addMethod(OLD_VARIABLES_METHOD, Opcodes.ACC_PROTECTED, new ClassNode(Map.class), Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, methodBlockStatement);
        preconditionMethodNode.setSynthetic(true);

    }
}
