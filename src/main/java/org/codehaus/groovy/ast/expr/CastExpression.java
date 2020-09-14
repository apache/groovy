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

/**
 * Represents a typecast expression.
 */
public class CastExpression extends Expression {

    private final Expression expression;
    private final boolean ignoreAutoboxing;

    private boolean coerce;
    private boolean strict;

    public static CastExpression asExpression(final ClassNode type, final Expression expression) {
        CastExpression answer = new CastExpression(type, expression);
        answer.setCoerce(true);
        return answer;
    }

    public CastExpression(final ClassNode type, final Expression expression) {
        this(type, expression, false);
    }

    public CastExpression(final ClassNode type, final Expression expression, final boolean ignoreAutoboxing) {
        this.setType(type);
        this.expression = expression;
        this.ignoreAutoboxing = ignoreAutoboxing;
    }

    public Expression getExpression() {
        return expression;
    }

    public boolean isIgnoringAutoboxing() {
        return ignoreAutoboxing;
    }

    public boolean isCoerce() {
        return coerce;
    }

    public void setCoerce(final boolean coerce) {
        this.coerce = coerce;
    }

    /**
     * If strict mode is true, then when the compiler generates a cast, it will
     * disable Groovy casts and rely on a strict cast (CHECKCAST).
     */
    public boolean isStrict() {
        return strict;
    }

    /**
     * If strict mode is true, then when the compiler generates a cast, it will
     * disable Groovy casts and rely on a strict cast (CHECKCAST).
     */
    public void setStrict(final boolean strict) {
        this.strict = strict;
    }

    @Override
    public String toString() {
        return super.toString() +"[(" + getType().getName() + ") " + expression + "]";
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitCastExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        CastExpression ret = new CastExpression(getType(), transformer.transform(expression), isIgnoringAutoboxing());
        ret.setCoerce(this.isCoerce());
        ret.setStrict(this.isStrict());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        return "(" + getType().toString(false) + ") " + expression.getText(); // TODO: add alternate for "as"?
    }

    @Override
    public void setType(final ClassNode type) {
        super.setType(type);
    }
}
