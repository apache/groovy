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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * An expression transformer used in the process of replacing the access to variables
 */
class VariableExpressionTransformer implements ExpressionTransformer {
    public VariableExpressionTransformer(Closure<Boolean> when, Closure<VariableExpression> replaceWith) {
        this.when = when;
        this.replaceWith = replaceWith;
    }

    @Override
    @SuppressWarnings("Instanceof")
    public Expression transform(Expression expr) {
        if ((expr instanceof VariableExpression) && when.call(expr)) {
            VariableExpression newExpr = replaceWith.call(expr);
            newExpr.setSourcePosition(expr);
            newExpr.copyNodeMetaData(expr);
            return newExpr;
        }

        return expr.transformExpression(this);
    }

    public Closure<Boolean> getWhen() {
        return when;
    }

    public void setWhen(Closure<Boolean> when) {
        this.when = when;
    }

    public Closure<VariableExpression> getReplaceWith() {
        return replaceWith;
    }

    public void setReplaceWith(Closure<VariableExpression> replaceWith) {
        this.replaceWith = replaceWith;
    }

    private Closure<Boolean> when;
    private Closure<VariableExpression> replaceWith;
}
