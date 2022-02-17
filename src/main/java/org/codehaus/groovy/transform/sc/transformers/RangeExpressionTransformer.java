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
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.List;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;

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
            if (parameters.length == 4
                    && ClassHelper.isPrimitiveBoolean(parameters[0].getOriginType())
                    && ClassHelper.isPrimitiveBoolean(parameters[1].getOriginType())) {
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

    public Expression transformRangeExpression(final RangeExpression range) {
        if (INTRANGE_TYPE.equals(range.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE))) {
            Expression inclLeft = constX(!range.isExclusiveLeft(),true), inclRight = constX(!range.isExclusiveRight(),true);
            Expression cce = ctorX(INTRANGE_TYPE, args(inclLeft, inclRight, range.getFrom(), range.getTo()));
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, INTRANGE_CTOR);
            cce.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, INTRANGE_TYPE);
            cce.setSourcePosition(range);

            return transformer.transform(cce);
        }
        return transformer.superTransform(range);
    }
}
