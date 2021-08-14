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
package org.codehaus.groovy.transform.tailrec;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;

import java.lang.reflect.Modifier;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.declS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.localVarX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Helping to create a few standard AST constructs
 */
class AstHelper {
    public static ExpressionStatement createVariableDefinition(String variableName, ClassNode variableType, Expression value) {
        return createVariableDefinition(variableName, variableType, value, false);
    }

    public static ExpressionStatement createVariableDefinition(String variableName, ClassNode variableType, Expression value, boolean variableShouldBeFinal) {
        VariableExpression newVariable = localVarX(variableName, variableType);
        if (variableShouldBeFinal)
            newVariable.setModifiers(Modifier.FINAL);
        return (ExpressionStatement) declS(newVariable, value);
    }

    public static ExpressionStatement createVariableAlias(String aliasName, ClassNode variableType, String variableName) {
        return createVariableDefinition(aliasName, variableType, varX(variableName, variableType));
    }

    public static VariableExpression createVariableReference(Map<String, ?> variableSpec) {
        return varX((String) variableSpec.get("name"), (ClassNode) variableSpec.get("type"));
    }

    /**
     * This statement should make the code jump to surrounding while loop's start label
     * Does not work from within Closures
     */
    public static Statement recurStatement() {
        //continue _RECUR_HERE_
        return new ContinueStatement(InWhileLoopWrapper.LOOP_LABEL);
    }

    /**
     * This statement will throw exception which will be caught and redirected to jump to surrounding while loop's start label
     * Also works from within Closures but is a tiny bit slower
     */
    public static Statement recurByThrowStatement() {
        // throw InWhileLoopWrapper.LOOP_EXCEPTION
        return throwS(propX(classX(InWhileLoopWrapper.class), "LOOP_EXCEPTION"));
    }
}
