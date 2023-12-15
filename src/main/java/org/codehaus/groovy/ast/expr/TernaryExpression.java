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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.tools.WideningCategories;

/**
 * Represents a ternary expression (booleanExpression) ? expression : expression
 */
public class TernaryExpression extends Expression {

    private final BooleanExpression booleanExpression;
    private final Expression truthExpression;
    private final Expression falseExpression;

    public TernaryExpression(final BooleanExpression booleanExpression,
            final Expression truthExpression, final Expression falseExpression) {
        this.booleanExpression = booleanExpression;
        this.truthExpression = truthExpression;
        this.falseExpression = falseExpression;
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    public Expression getTrueExpression() {
        return truthExpression;
    }

    public Expression getFalseExpression() {
        return falseExpression;
    }

    @Override
    public String getText() {
        return "(" + getBooleanExpression().getText() + ") ? " + getTrueExpression().getText() + " : " + getFalseExpression().getText();
    }

    /**
     * @see org.codehaus.groovy.classgen.asm.BinaryExpressionHelper#evaluateTernaryExpression(TernaryExpression)
     */
    @Override
    public ClassNode getType() {
        return WideningCategories.lowestUpperBound(getTrueExpression().getType(), getFalseExpression().getType());
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getBooleanExpression() + " ? " + getTrueExpression() + " : " + getFalseExpression() + "]";
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new TernaryExpression(
                (BooleanExpression) transformer.transform(getBooleanExpression()),
                                    transformer.transform(getTrueExpression()),
                                    transformer.transform(getFalseExpression())
        );
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitTernaryExpression(this);
    }
}
