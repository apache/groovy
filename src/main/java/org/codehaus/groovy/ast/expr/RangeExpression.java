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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;

/**
 * Represents a range expression such as for iterating.
 * E.g.:
 * <pre>for i in 0..10 {...}</pre>
 */
public class RangeExpression extends Expression {

    private final Expression from;
    private final Expression to;
    private final boolean inclusive;

    public RangeExpression(final Expression from, final Expression to, final boolean inclusive) {
        this.from = from;
        this.to = to;
        this.inclusive = inclusive;
        setType(ClassHelper.RANGE_TYPE.getPlainNodeReference());
    }

    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitRangeExpression(this);
    }

    public Expression transformExpression(final ExpressionTransformer transformer) {
        Expression ret = new RangeExpression(transformer.transform(getFrom()), transformer.transform(getTo()), isInclusive());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    public Expression getFrom() {
        return from;
    }

    public Expression getTo() {
        return to;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public String getText() {
        return "(" + getFrom().getText() + (isInclusive() ? ".." : "..<") + getTo().getText() + ")";
    }
}
