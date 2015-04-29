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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

public class StaticMethodCallExpressionTransformer {
    private final StaticCompilationTransformer transformer;

    public StaticMethodCallExpressionTransformer(final StaticCompilationTransformer staticCompilationTransformer) {
        transformer = staticCompilationTransformer;
    }

    Expression transformStaticMethodCallExpression(final StaticMethodCallExpression orig) {
        MethodNode target = (MethodNode) orig.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (target != null) {
            MethodCallExpression call = new MethodCallExpression(
                    new ClassExpression(orig.getOwnerType()),
                    orig.getMethod(),
                    orig.getArguments()
            );
            call.setMethodTarget(target);
            call.setSourcePosition(orig);
            call.copyNodeMetaData(orig);
            return transformer.transform(call);
        }
        return transformer.superTransform(orig);
    }

}