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
public class VariableExpressionTransformer implements ExpressionTransformer {
    /**
     * Creates a transformer with the supplied predicate and replacement strategy.
     *
     * @param when decides whether a variable expression should be replaced
     * @param replaceWith creates the replacement variable expression
     */
    public VariableExpressionTransformer(Closure<Boolean> when, Closure<VariableExpression> replaceWith) {
        this.when = when;
        this.replaceWith = replaceWith;
    }

    /** {@inheritDoc} */
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

    /**
     * Returns the predicate that decides whether a variable expression should be replaced.
     *
     * @return the replacement predicate
     */
    public Closure<Boolean> getWhen() {
        return when;
    }

    /**
     * Sets the predicate that decides whether a variable expression should be replaced.
     *
     * @param when the replacement predicate
     */
    public void setWhen(Closure<Boolean> when) {
        this.when = when;
    }

    /**
     * Returns the closure that creates replacement variable expressions.
     *
     * @return the replacement closure
     */
    public Closure<VariableExpression> getReplaceWith() {
        return replaceWith;
    }

    /**
     * Sets the closure that creates replacement variable expressions.
     *
     * @param replaceWith the replacement closure
     */
    public void setReplaceWith(Closure<VariableExpression> replaceWith) {
        this.replaceWith = replaceWith;
    }

    private Closure<Boolean> when;
    private Closure<VariableExpression> replaceWith;
}
