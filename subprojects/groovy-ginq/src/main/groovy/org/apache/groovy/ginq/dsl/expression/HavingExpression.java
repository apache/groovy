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
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Represents having expression
 *
 * @since 4.0.0
 */
public class HavingExpression extends FilterExpression {
    public HavingExpression(Expression filterExpr) {
        super(filterExpr);
    }

    @Override
    public <R> R accept(GinqAstVisitor<R> visitor) {
        return visitor.visitHavingExpression(this);
    }

    @Override
    public String getText() {
        return "having " + filterExpr.getText();
    }

    @Override
    public String toString() {
        return getText();
    }
}
