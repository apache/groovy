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

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.LinkedList;
import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;

public class ListExpressionTransformer {
    private final StaticCompilationTransformer transformer;

    public ListExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        transformer = staticCompilationTransformer;
    }

    Expression transformListExpression(final ListExpression expr) {
        MethodNode target = expr.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (target instanceof ConstructorNode) {
            if (target.getDeclaringClass().isArray()) {
                return transformArrayConstructor(expr, target);
            }
            return transformRegularConstructor(expr, target);

        } else {
            return transformer.superTransform(expr);
        }
    }

    private Expression transformArrayConstructor(final ListExpression expr, final MethodNode target) {
        ArrayExpression aex = new ArrayExpression(target.getDeclaringClass().getComponentType(), transformArguments(expr));
        aex.setSourcePosition(expr);
        return aex;
    }

    private Expression transformRegularConstructor(final ListExpression expr, final MethodNode target) {
        // can be replaced with a direct constructor call
        List<Expression> transformedArgs = transformArguments(expr);
        ConstructorCallExpression cce = new ConstructorCallExpression(
                target.getDeclaringClass(),
                new ArgumentListExpression(transformedArgs)
        );
        cce.setSourcePosition(expr);
        cce.putNodeMetaData(DIRECT_METHOD_CALL_TARGET, target);
        return cce;
    }

    private List<Expression> transformArguments(final ListExpression expr) {
        List<Expression> expressions = expr.getExpressions();
        List<Expression> transformedArgs = new LinkedList<>();
        for (Expression expression : expressions) {
            transformedArgs.add(transformer.transform(expression));
        }
        return transformedArgs;
    }
}
