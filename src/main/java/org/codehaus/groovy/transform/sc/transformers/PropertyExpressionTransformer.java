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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;

class PropertyExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    PropertyExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformPropertyExpression(final PropertyExpression pe) {
        MethodNode dmct = pe.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        // NOTE: BinaryExpressionTransformer handles the setter
        if (dmct != null && dmct.getParameters().length == 0) {
            MethodCallExpression mce = callX(scTransformer.transform(pe.getObjectExpression()), dmct.getName());
            mce.setImplicitThis(pe.isImplicitThis());
            mce.setMethodTarget(dmct);
            mce.setSourcePosition(pe);
            mce.setSpreadSafe(pe.isSpreadSafe());
            mce.setSafe(pe.isSafe());
            mce.copyNodeMetaData(pe);
            return mce;
        }

        return scTransformer.superTransform(pe);
    }
}
