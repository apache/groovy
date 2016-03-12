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
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

public class CastExpressionOptimizer {
    private final StaticCompilationTransformer transformer;

    public CastExpressionOptimizer(StaticCompilationTransformer staticCompilationTransformer) {
        transformer = staticCompilationTransformer;
    }

    public Expression transformCastExpression(final CastExpression cast) {
        if (cast.isCoerce()) {
            Expression expression = cast.getExpression();
            ClassNode exprInferredType = transformer.getTypeChooser().resolveType(expression, transformer.getClassNode());
            ClassNode castType = cast.getType();
            if (castType.isArray() && expression instanceof ListExpression) {
                ArrayExpression arrayExpression = new ArrayExpression(castType.getComponentType(), ((ListExpression) expression).getExpressions());
                arrayExpression.setSourcePosition(cast);
                return transformer.transform(arrayExpression);
            }
            if (isOptimizable(exprInferredType, castType)) {
                // coerce is not needed
                CastExpression trn = new CastExpression(castType, transformer.transform(expression));
                trn.setSourcePosition(cast);
                trn.copyNodeMetaData(cast);
                return trn;
            }
        } else if (ClassHelper.char_TYPE.equals(cast.getType())) {
            Expression expression = cast.getExpression();
            if (expression instanceof ConstantExpression) {
                ConstantExpression ce = (ConstantExpression) expression;
                if (ClassHelper.STRING_TYPE.equals(ce.getType())) {
                    String val = (String) ce.getValue();
                    if (val!=null && val.length()==1) {
                        ConstantExpression result = new ConstantExpression(val.charAt(0),true);
                        result.setSourcePosition(cast);
                        return result;
                    }
                }
            }
        }
        return transformer.superTransform(cast);
    }

    private static boolean isOptimizable(final ClassNode exprInferredType, final ClassNode castType) {
        if (StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf(exprInferredType, castType)) {
            return true;
        }
        if (ClassHelper.isPrimitiveType(exprInferredType) && ClassHelper.isPrimitiveType(castType)) {
            return true;
        }
        return false;
    }
}
