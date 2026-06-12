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
package org.apache.groovy.ginq.dsl.expression;

import org.apache.groovy.ginq.dsl.GinqAstVisitor;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.NodeMetaDataHandler;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;

/**
 * Represents GINQ expression which could hold metadata
 *
 * @since 4.0.0
 */
public abstract class AbstractGinqExpression extends Expression implements NodeMetaDataHandler {
    /**
     * Returns this expression because GINQ expressions are transformed elsewhere.
     *
     * @param transformer the transformer requesting the change
     * @return this expression
     */
    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        // do nothing for now
        return this;
    }

    /**
     * Accepts a GINQ visitor.
     *
     * @param visitor the visitor to accept
     * @param <R> the visit result type
     * @return the visit result
     */
    public abstract <R> R accept(GinqAstVisitor<R> visitor);

    /**
     * Does nothing because GINQ expressions are visited through {@link GinqAstVisitor}.
     *
     * @param visitor the Groovy code visitor
     */
    @Override
    public void visit(GroovyCodeVisitor visitor) {
        // do nothing for now
    }
}
