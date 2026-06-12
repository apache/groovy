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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.codehaus.groovy.ast.tools.ClosureUtils.getParametersSafe;

/**
 * Applies static-compilation-specific rewrites to closure expressions.
 */
public class ClosureExpressionTransformer {
    private final StaticCompilationTransformer transformer;

    /**
     * Creates a closure-expression transformer backed by the owning static compilation transformer.
     *
     * @param staticCompilationTransformer the shared transformer context
     */
    public ClosureExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        transformer = staticCompilationTransformer;
    }

    /**
     * Transforms closure parameters and visits the closure body in the current static-compilation context.
     *
     * @param expr the closure expression to transform
     * @return the transformed closure expression
     */
    Expression transformClosureExpression(final ClosureExpression expr) {
        for (Parameter parameter : getParametersSafe(expr)) {
            if (parameter.hasInitialExpression()) {
                parameter.setInitialExpression(transformer.transform(parameter.getInitialExpression()));
            }
        }
        Statement code = expr.getCode();
        transformer.visitClassCodeContainer(code);
        return transformer.superTransform(expr);
    }

}