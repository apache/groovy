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
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * Represents a short ternary expression x ?: y, which is equal to
 * <pre>
 * def truePart = x
 * def booleanPart = truePart as boolean
 * booleanPart? truePart : y
 * </pre>
 * Even if x is no atomic expression, x will be evaluated only once. Example:
 * <pre class="language-groovy groovyTestCase">
 * class Foo {
 *   def index=0
 *   def getX(){ index++; return index }
 * }
 * def foo = new Foo()
 * def result = foo.x ?: "false case"
 * assert foo.index == 1
 * assert result == 1
 * </pre>
 * The null-coalescing form x ?? y tests x against {@code null} instead of
 * using Groovy Truth, so falsy non-null values such as {@code 0}, {@code false}
 * or {@code ''} are kept (see {@link #isNullCoalescing()}).
 *
 * @since 1.5
 */
public class ElvisOperatorExpression extends TernaryExpression {

    private final boolean nullCoalescing;

    public ElvisOperatorExpression(final Expression base, final Expression falseValue) {
        this(base, falseValue, false);
    }

    /**
     * Creates a short ternary expression.
     *
     * @param base the expression evaluated once and returned when it passes the test
     * @param falseValue the expression evaluated and returned when {@code base} fails the test
     * @param nullCoalescing {@code true} to test {@code base} against {@code null} (x ?? y),
     *        {@code false} to test its Groovy Truth (x ?: y)
     * @since 7.0.0
     */
    public ElvisOperatorExpression(final Expression base, final Expression falseValue, final boolean nullCoalescing) {
        super(nullCoalescing ? notNullExpression(base) : asBooleanExpression(base), base, falseValue);
        this.nullCoalescing = nullCoalescing;
    }

    /**
     * Returns whether this is the null-coalescing form x ?? y, whose test is
     * {@code x != null} rather than the Groovy Truth of x.
     *
     * @return {@code true} for x ?? y, {@code false} for x ?: y
     *
     * @since 7.0.0
     */
    public boolean isNullCoalescing() {
        return nullCoalescing;
    }

    private static BooleanExpression notNullExpression(final Expression base) {
        BinaryExpression ne = new BinaryExpression(base, Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1), ConstantExpression.NULL);
        ne.setSourcePosition(base);
        BooleanExpression be = new BooleanExpression(ne);
        be.setSourcePosition(base);
        return be;
    }

    private static BooleanExpression asBooleanExpression(final Expression base) {
        if (base instanceof BooleanExpression) return (BooleanExpression) base;
        BooleanExpression be = new BooleanExpression(base);
        be.setSourcePosition(base);
        return be;
    }

    @Override
    public Expression transformExpression(ExpressionTransformer transformer) {
        Expression ret = new ElvisOperatorExpression(
                transformer.transform(getTrueExpression()),
                transformer.transform(getFalseExpression()),
                isNullCoalescing());
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitShortTernaryExpression(this);
    }
}
