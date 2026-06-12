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
package org.apache.groovy.contracts.domain;

import org.apache.groovy.contracts.util.Validate;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.andX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.orX;

/**
 * <p>Base class for all assertion types.</p>
 *
 * @param <T> the concrete assertion subtype used for logical composition
 */
public abstract class Assertion<T extends Assertion> {

    private BlockStatement originalBlockStatement;
    private BooleanExpression booleanExpression;

    /**
     * Creates an assertion that defaults to {@code true}.
     */
    public Assertion() {
        this.booleanExpression = boolX(ConstantExpression.TRUE);
    }

    /**
     * Creates an assertion backed by the original block and normalized boolean expression.
     *
     * @param blockStatement the original contract block, or {@code null} if unavailable
     * @param booleanExpression the normalized expression to evaluate
     */
    public Assertion(final BlockStatement blockStatement, final BooleanExpression booleanExpression) {
        Validate.notNull(booleanExpression);

        this.originalBlockStatement = blockStatement; // the BlockStatement might be null! we do not always have the original expression available
        this.booleanExpression = booleanExpression;
    }

    /**
     * Returns the boolean expression used when generating the runtime assertion.
     *
     * @return the current boolean expression
     */
    public BooleanExpression booleanExpression() {
        return booleanExpression;
    }

    /**
     * Returns the original block statement from which this assertion was derived.
     *
     * @return the original contract block, or {@code null} if it is unavailable
     */
    public BlockStatement originalBlockStatement() {
        return originalBlockStatement;
    }

    /**
     * Replaces the current boolean expression while preserving the existing source mapping strategy.
     *
     * @param booleanExpression the new boolean expression
     */
    public void renew(BooleanExpression booleanExpression) {
        Validate.notNull(booleanExpression);

        // don't renew the source position to keep the new assertion expression without source code replacement
        // booleanExpression.setSourcePosition(this.booleanExpression);

        this.booleanExpression = booleanExpression;
    }

    /**
     * Conjoins this assertion with another assertion of the same kind.
     *
     * @param other the assertion to combine with a logical AND
     */
    public void and(T other) {
        Validate.notNull(other);
        BooleanExpression newBooleanExpression = boolX(andX(booleanExpression(), other.booleanExpression()));
        newBooleanExpression.setSourcePosition(booleanExpression());
        renew(newBooleanExpression);
        // GROOVY-12083: the single original block (used for inline-mode assertion generation) only
        // reflects the first condition; once combined it can no longer represent the whole expression,
        // so drop it to force the tracker path that evaluates the combined booleanExpression
        this.originalBlockStatement = null;
    }

    /**
     * Disjoins this assertion with another assertion of the same kind.
     *
     * @param other the assertion to combine with a logical OR
     */
    public void or(T other) {
        Validate.notNull(other);
        BooleanExpression newBooleanExpression = boolX(orX(booleanExpression(), other.booleanExpression()));
        newBooleanExpression.setSourcePosition(booleanExpression());
        renew(newBooleanExpression);
        // GROOVY-12083: see and(); a combined expression can no longer be represented by the single
        // original block, so drop it to force the combined-expression tracker path
        this.originalBlockStatement = null;
    }
}
