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

import groovy.lang.IntRange;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.List;

/**
 * This transformer focuses on ranges to produce optimized bytecode.
 */
public class RangeExpressionTransformer {
    private static final ClassNode INTRANGE_TYPE = ClassHelper.make(IntRange.class);
    private static final MethodNode INTRANGE_CTOR;

    static {
        final List<ConstructorNode> declaredConstructors = INTRANGE_TYPE.getDeclaredConstructors();
        ConstructorNode target = null;
        for (ConstructorNode constructor : declaredConstructors) {
            final Parameter[] parameters = constructor.getParameters();
            if (parameters.length==3 && ClassHelper.boolean_TYPE.equals(parameters[0].getOriginType())) {
                target = constructor;
                break;
            }
        }
        INTRANGE_CTOR = target;
    }

    private final StaticCompilationTransformer transformer;

    public RangeExpressionTransformer(final StaticCompilationTransformer transformer) {
        this.transformer = transformer;
    }

    public Expression transformRangeExpression(RangeExpression range) {
        final ClassNode inferred = range.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
        if (INTRANGE_TYPE.equals(inferred)) {
            ArgumentListExpression bounds = new ArgumentListExpression(new ConstantExpression(range.isInclusive(),true),range.getFrom(), range.getTo());
            ConstructorCallExpression cce = new ConstructorCallExpression(INTRANGE_TYPE, bounds);
            cce.setSourcePosition(range);
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, INTRANGE_CTOR);
            cce.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, INTRANGE_TYPE);
            return transformer.transform(cce);
        }
        return transformer.superTransform(range);
    }
}
