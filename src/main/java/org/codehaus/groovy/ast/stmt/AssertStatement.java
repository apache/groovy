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
package org.codehaus.groovy.ast.stmt;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Represents an assert statement.
 * E.g.:
 * <code>
 * assert i != 0 : "should never be zero";
 * </code>
 */
public class AssertStatement extends Statement {

    private BooleanExpression booleanExpression;
    private Expression messageExpression;
    
    public AssertStatement(BooleanExpression booleanExpression) {
        this(booleanExpression, ConstantExpression.NULL);
    }
    
    public AssertStatement(BooleanExpression booleanExpression, Expression messageExpression) {
        this.booleanExpression = booleanExpression;
        this.messageExpression = messageExpression;
    }
    
    public void accept(GroovyCodeVisitor visitor) {
        visitor.visitAssertStatement(this);
    }

    public Expression getMessageExpression() {
        return messageExpression;
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }
    public void setBooleanExpression(BooleanExpression booleanExpression) {
        this.booleanExpression = booleanExpression;
    }
    public void setMessageExpression(Expression messageExpression) {
        this.messageExpression = messageExpression;
    }
}
