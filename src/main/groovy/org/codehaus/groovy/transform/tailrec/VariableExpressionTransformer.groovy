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
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ExpressionTransformer
import org.codehaus.groovy.ast.expr.VariableExpression

/**
 * An expression transformer used in the process of replacing the access to variables
 */
@CompileStatic
class VariableExpressionTransformer implements ExpressionTransformer {

    Closure<Boolean> when
    Closure<VariableExpression> replaceWith

    @Override
    @SuppressWarnings('Instanceof')
    Expression transform(Expression expr) {
        if ((expr instanceof VariableExpression) && when(expr)) {
            VariableExpression newExpr = replaceWith(expr)
            newExpr.sourcePosition = expr
            newExpr.copyNodeMetaData(expr)
            return newExpr
        }
        expr.transformExpression(this)
    }
}
