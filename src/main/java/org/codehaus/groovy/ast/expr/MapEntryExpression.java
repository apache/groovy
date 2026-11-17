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

import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents an entry inside a map expression such as {@code 1 : 2} or {@code 'foo' : 'bar'}.
 */
public class MapEntryExpression extends Expression {
    private Expression keyExpression;
    private Expression valueExpression;

    /**
     * Builds a map entry from the key and value expressions.
     *
     * @param keyExpression expression on the left of the colon
     * @param valueExpression expression on the right of the colon
     */
    public MapEntryExpression(Expression keyExpression, Expression valueExpression) {
        this.keyExpression = keyExpression;
        this.valueExpression = valueExpression;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitMapEntryExpression(this);
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new MapEntryExpression(transformer.transform(keyExpression), transformer.transform(valueExpression));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String toString() {
        return super.toString() + "(key: " + keyExpression + ", value: " + valueExpression + ")";
    }

    /**
     * Returns the key expression, the left side of the colon.
     *
     * @return the key expression
     */
    public Expression getKeyExpression() {
        return keyExpression;
    }

    /**
     * Returns the value expression, the right side of the colon.
     *
     * @return the mapped expression
     */
    public Expression getValueExpression() {
        return valueExpression;
    }

    /**
     * Replaces the key expression.
     *
     * @param keyExpression the expression to store on the left of the colon
     */
    public void setKeyExpression(Expression keyExpression) {
        this.keyExpression = keyExpression;
    }

    /**
     * Replaces the value expression.
     *
     * @param valueExpression the expression to store on the right of the colon
     */
    public void setValueExpression(Expression valueExpression) {
        this.valueExpression = valueExpression;
    }
}
