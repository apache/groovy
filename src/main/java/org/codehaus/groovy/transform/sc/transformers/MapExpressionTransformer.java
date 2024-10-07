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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.LinkedHashMap;

class MapExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    MapExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformMapExpression(final MapExpression me) {
        if (me.getMapEntryExpressions().isEmpty()) { // GROOVY-11309: skip SBA.createMap
            ClassNode linkedHashMap = ClassHelper.makeWithoutCaching(LinkedHashMap.class);
            ConstructorNode noArgConstructor = linkedHashMap.getDeclaredConstructor(Parameter.EMPTY_ARRAY);

            var cce = new ConstructorCallExpression(linkedHashMap, ArgumentListExpression.EMPTY_ARGUMENTS);
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, noArgConstructor);
            cce.setSourcePosition(me);
            return cce;
        }

        return scTransformer.superTransform(me);
    }
}
