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
package org.codehaus.groovy.transform.tailrec

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.VariableExpression

/**
 * Replace all access to variables and args by new variables.
 * The variable names to replace as well as their replacement name and type have to be configured
 * in nameAndTypeMapping before calling replaceIn().
 *
 * The VariableReplacedListener can be set if clients want to react to variable replacement.
 */
@CompileStatic
class VariableAccessReplacer {

    /**
     * Nested map of variable accesses to replace
     * e.g.: [
     *          'varToReplace': [name: 'newVar', type: TypeOfVar],
     *          'varToReplace2': [name: 'newVar2', type: TypeOfVar2],
     *       ]
     */
    Map<String, Map> nameAndTypeMapping = [:]

    VariableReplacedListener listener = VariableReplacedListener.NULL

    void replaceIn(ASTNode root) {
        Closure<Boolean> whenParam = { VariableExpression expr ->
            return nameAndTypeMapping.containsKey(expr.name)
        }
        Closure<VariableExpression> replaceWithLocalVariable = { VariableExpression expr ->
            def newVar = AstHelper.createVariableReference(nameAndTypeMapping[expr.name])
            listener.variableReplaced(expr, newVar)
            return newVar
        }
        new VariableExpressionReplacer(when: whenParam, replaceWith: replaceWithLocalVariable).replaceIn(root)
    }
}

@CompileStatic
interface VariableReplacedListener {
    void variableReplaced(VariableExpression oldVar, VariableExpression newVar)

    public static VariableReplacedListener NULL = new VariableReplacedListener() {
        @Override
        void variableReplaced(VariableExpression oldVar, VariableExpression newVar) {
            //do nothing
        }
    }
}
