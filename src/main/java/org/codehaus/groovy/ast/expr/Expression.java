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
package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassNode;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.ast.ClassHelper.dynamicType;
import static org.codehaus.groovy.ast.ClassHelper.make;

/**
 * Base class for any expression.
 */
public abstract class Expression extends AnnotatedNode {

    public static final Expression[] EMPTY_ARRAY = new Expression[0];

    private static final ClassNode NULL_TYPE = make("null");

    private ClassNode type = NULL_TYPE;

    public ClassNode getType() {
        if (type == NULL_TYPE) {
            type = dynamicType();
        }
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
    }

    /**
     * Transforms this expression and any nested expressions.
     */
    public abstract Expression transformExpression(ExpressionTransformer transformer);

    /**
     * Transforms list of expressions.
     *
     * @return a new list of transformed expressions
     */
    protected List<Expression> transformExpressions(List<? extends Expression> expressions, ExpressionTransformer transformer) {
        List<Expression> list = new ArrayList<>(expressions.size());
        for (Expression expression : expressions) {
            expression = transformer.transform(expression);
            list.add(expression);
        }
        return list;
    }

    /**
     * Transforms list of expressions and checks that all transformed expressions have the given type.
     *
     * @return a new type-safe list of transformed expressions
     */
    protected <T extends Expression> List<T> transformExpressions(List<? extends Expression> expressions, ExpressionTransformer transformer, Class<T> targetType) {
        List<T> list = new ArrayList<>(expressions.size());
        for (Expression expression : expressions) {
            expression = transformer.transform(expression);
            if (!targetType.isInstance(expression)) {
                throw new GroovyBugError("Transformed expression should have type " +
                    targetType.getName() + " but has type " + expression.getClass().getName());
            }
            list.add(targetType.cast(expression));
        }
        return list;
    }
}
