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
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a base class for expressions which evaluate as an object
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public abstract class Expression extends AnnotatedNode {
    public static final Expression[] EMPTY_ARRAY = new Expression[0];
    private ClassNode type=ClassHelper.DYNAMIC_TYPE;
    
    /**
     * Return a copy of the expression calling the transformer on any nested expressions 
     * @param transformer
     */
    public abstract Expression transformExpression(ExpressionTransformer transformer);

    /**
     * Transforms the list of expressions
     * @return a new list of transformed expressions
     */
    protected List<Expression> transformExpressions(List<? extends Expression> expressions, ExpressionTransformer transformer) {
        List<Expression> list = new ArrayList<>(expressions.size());
        for (Expression expr : expressions ) {
            list.add(transformer.transform(expr));
        }
        return list;
    }

    /**
     * Transforms the list of expressions, and checks that all transformed expressions have the given type.
     *
     * @return a new list of transformed expressions
     */
    protected <T extends Expression> List<T> transformExpressions(List<? extends Expression> expressions,
            ExpressionTransformer transformer, Class<T> transformedType) {
        List<T> list = new ArrayList<>(expressions.size());
        for (Expression expr : expressions) {
            Expression transformed = transformer.transform(expr);
            if (!transformedType.isInstance(transformed))
                throw new GroovyBugError(String.format("Transformed expression should have type %s but has type %s",
                    transformedType, transformed.getClass()));
            list.add(transformedType.cast(transformed));
        }
        return list;
    }
    
    public ClassNode getType() {
        return type;
    }
    
    public void setType(ClassNode t) {
        type=t;
    }
}
