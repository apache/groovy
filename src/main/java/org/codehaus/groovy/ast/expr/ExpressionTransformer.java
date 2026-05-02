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

/**
 * Functional interface for transforming {@link Expression} nodes during AST traversal and manipulation.
 *
 * <p>This transformer interface enables expression-level transformations in AST processing. Implementations
 * typically take an input expression and return a (possibly modified) expression. This can be used for:
 * <ul>
 *   <li>Code generation and transformation passes</li>
 *   <li>Expression normalization or optimization</li>
 *   <li>Type-driven transformation in static type checking</li>
 *   <li>Custom DSL implementation and metaprogramming</li>
 * </ul>
 *
 * <p>Transformers may return:
 * <ul>
 *   <li>The same expression instance unchanged</li>
 *   <li>A new expression of the same type with modified properties</li>
 *   <li>A completely different expression type</li>
 *   <li>Null if the expression should be removed (usage-dependent)</li>
 * </ul>
 *
 * <p>This is a functional interface and can be implemented as a lambda expression.
 *
 * @see org.codehaus.groovy.ast.ClassCodeExpressionTransformer for abstract transformer base class
 * @see TransformingCodeVisitor for visitor using transformers
 */
@FunctionalInterface
public interface ExpressionTransformer {
    /**
     * Transforms the given expression.
     *
     * @param expression the expression to transform, may be null
     * @return the transformed expression, may be the same instance, a new instance, or null
     */
    Expression transform(Expression expression);
}
