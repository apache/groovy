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

import java.util.List;

import static java.util.stream.Collectors.toList;

class ListExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    ListExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformListExpression(final ListExpression le) {
        MethodNode mn = le.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn instanceof ConstructorNode) {
            List<Expression> elements = le.getExpressions().stream().map(scTransformer::transform).collect(toList());

            if (mn.getDeclaringClass().isArray()) {
                var ae = new ArrayExpression(mn.getDeclaringClass().getComponentType(), elements);
                ae.setSourcePosition(le);
                return ae;
            }

            var cce = new ConstructorCallExpression(mn.getDeclaringClass(), new ArgumentListExpression(elements));
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, mn);
            cce.setSourcePosition(le);
            return cce;
        }

        return scTransformer.superTransform(le);
    }
}
