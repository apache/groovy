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
 * Represents an entry inside a map expression such as 1 : 2.
 */
public class MapEntryExpression extends Expression {
    private Expression keyExpression;
    private Expression valueExpression;

    public MapEntryExpression(Expression keyExpression, Expression valueExpression) {
        this.keyExpression = keyExpression;
        this.valueExpression = valueExpression;
    }

    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitMapEntryExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new MapEntryExpression(transformer.transform(keyExpression), transformer.transform(valueExpression));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;        
    }

    public String toString() {
        return super.toString() + "(key: " + keyExpression + ", value: " + valueExpression + ")";
    }

    public Expression getKeyExpression() {
        return keyExpression;
    }

    public Expression getValueExpression() {
        return valueExpression;
    }

    public void setKeyExpression(Expression keyExpression) {
        this.keyExpression = keyExpression;
    }

    public void setValueExpression(Expression valueExpression) {
        this.valueExpression = valueExpression;
    }
}
