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
package org.apache.groovy.typecheckers

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.Variable
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.SourceUnit

class CheckingVisitor extends ClassCodeVisitorSupport {
    protected final Map<Expression, Expression> localConstVars = new HashMap<>()

    protected Expression findConstExp(Expression exp, Class type) {
        if (exp instanceof ConstantExpression && exp.value.getClass().isAssignableFrom(type)) {
            return exp
        }
        if (exp instanceof VariableExpression) {
            def var = findTargetVariable(exp)
            if (var instanceof FieldNode && var.hasInitialExpression()) {
                return findConstExp(var.initialExpression, type)
            }
            if (localConstVars.containsKey(var)) {
                return findConstExp(localConstVars.get(var), type)
            }
        }
        return null
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return null
    }

    static Variable findTargetVariable(final VariableExpression ve) {
        Variable accessedVariable = ve.getAccessedVariable()
        if (accessedVariable != null && accessedVariable != ve) {
            if (accessedVariable instanceof VariableExpression) {
                return findTargetVariable((VariableExpression) accessedVariable)
            }
            return accessedVariable
        }
        return ve
    }
}
