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
 * Represents a short ternary expression x ?: y, which is equal 
 * to 
 * <pre>
 * def truePart = x
 * def booleanPart = truePart as boolean
 * booleanPart? truePart : y
 * </pre>
 * Even if x is no atomic expression, x will be evaluated only 
 * once. Example:
 * <pre class="groovyTestCase">
 * class Foo { 
 *   def index=0 
 *   def getX(){ index++; return index }
 * }
 * def foo = new Foo()
 * def result = foo.x ?: "false case" 
 * assert foo.index == 1
 * assert result == 1 
 * </pre>
 * 
 * @since 1.5
 */
public class ElvisOperatorExpression extends TernaryExpression {

    public ElvisOperatorExpression(Expression base, Expression falseExpression) {
        super(getBool(base), base, falseExpression);
    }
    
    private static BooleanExpression getBool(Expression base) {
       BooleanExpression be = new BooleanExpression(base);
       be.setSourcePosition(base);
       return be;
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitShortTernaryExpression(this);
    }
    
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ElvisOperatorExpression(
                transformer.transform(getTrueExpression()),
                transformer.transform(getFalseExpression()));
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret; 
    }
}
