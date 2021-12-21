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

import groovy.lang.Closure;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.Map;

/**
 * Replace all access to variables and args by new variables.
 * The variable names to replace as well as their replacement name and type have to be configured
 * in nameAndTypeMapping before calling replaceIn().
 * <p>
 * The VariableReplacedListener can be set if clients want to react to variable replacement.
 */
public class VariableAccessReplacer {
    public VariableAccessReplacer(Map<String, Map> nameAndTypeMapping) {
        this.nameAndTypeMapping = nameAndTypeMapping;
    }

    public VariableAccessReplacer(Map<String, Map> nameAndTypeMapping, VariableReplacedListener listener) {
        this.nameAndTypeMapping = nameAndTypeMapping;
        this.listener = listener;
    }

    public void replaceIn(ASTNode root) {
        Closure<Boolean> whenParam = new Closure<Boolean>(this, this) {
            public Boolean doCall(VariableExpression expr) {
                return nameAndTypeMapping.containsKey(expr.getName());
            }

        };
        Closure<VariableExpression> replaceWithLocalVariable = new Closure<VariableExpression>(this, this) {
            public VariableExpression doCall(VariableExpression expr) {
                VariableExpression newVar = AstHelper.createVariableReference(nameAndTypeMapping.get(expr.getName()));
                getListener().variableReplaced(expr, newVar);
                return newVar;
            }

        };
        new VariableExpressionReplacer(whenParam, replaceWithLocalVariable).replaceIn(root);
    }

    public void setNameAndTypeMapping(Map<String, Map> nameAndTypeMapping) {
        this.nameAndTypeMapping = nameAndTypeMapping;
    }

    public VariableReplacedListener getListener() {
        return listener;
    }

    public void setListener(VariableReplacedListener listener) {
        this.listener = listener;
    }

    /**
     * Nested map of variable accesses to replace
     * e.g.: [
     * 'varToReplace': [name: 'newVar', type: TypeOfVar],
     * 'varToReplace2': [name: 'newVar2', type: TypeOfVar2],
     * ]
     */
    private Map<String, Map> nameAndTypeMapping;
    private VariableReplacedListener listener = VariableReplacedListener.NULL;
}
