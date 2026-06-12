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
 * Base class for all expression nodes in the Groovy AST. Expressions represent values and computations
 * that can be evaluated at runtime, supporting type information and transformation operations.
 * All concrete expression types inherit from this abstract class and must implement the
 * {@link #transformExpression(ExpressionTransformer)} method for AST transformation support.
 *
 * @see org.codehaus.groovy.ast.stmt.Statement
 * @see ExpressionTransformer
 * @see org.codehaus.groovy.ast.GroovyCodeVisitor
 */
public abstract class Expression extends AnnotatedNode {

    public static final Expression[] EMPTY_ARRAY = new Expression[0];

    private static final ClassNode NULL_TYPE = make("null");

    private ClassNode type = NULL_TYPE;

    /**
     * Returns the type of this expression. If the type has not been explicitly set,
     * this method returns a dynamic type to support dynamic typing.
     *
     * @return the {@link ClassNode} representing this expression's type
     */
    public ClassNode getType() {
        if (type == NULL_TYPE) {
            type = dynamicType();
        }
        return type;
    }

    /**
     * Sets the type information for this expression. Used during type checking and compilation phases
     * to associate a specific type with this expression result.
     *
     * @param type the {@link ClassNode} representing this expression's type
     */
    public void setType(ClassNode type) {
        this.type = type;
    }

    /**
     * Transforms this expression and any nested expressions according to the provided transformer.
     * This method is called during AST transformation phases and must recursively transform
     * any nested expressions to support full AST tree transformation.
     *
     * @param transformer the {@link ExpressionTransformer} to apply
     * @return a transformed copy of this expression (or this expression itself if no changes are needed)
     */
    public abstract Expression transformExpression(ExpressionTransformer transformer);

    /**
     * Transforms a list of expressions by applying the provided transformer to each element.
     * Handles null expressions gracefully by including them in the result.
     *
     * @param expressions the list of {@link Expression}s to transform
     * @param transformer the {@link ExpressionTransformer} to apply
     * @return a new list containing transformed expressions
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
     * Transforms a list of expressions and verifies that all transformed expressions have a specific type.
     * This variant provides type safety during transformations by enforcing that transformed expressions
     * conform to a target type. Throws {@link GroovyBugError} if any expression has an incompatible type.
     *
     * @param expressions the list of {@link Expression}s to transform
     * @param transformer the {@link ExpressionTransformer} to apply
     * @param targetType the expected type of all transformed expressions
     * @param <T> the target expression type parameter
     * @return a new type-safe list of transformed expressions
     * @throws GroovyBugError if any transformed expression is not an instance of targetType
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
