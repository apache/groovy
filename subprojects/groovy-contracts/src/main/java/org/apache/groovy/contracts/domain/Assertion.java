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
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * <p>Base class for all assertion types.</p>
 *
 * @param <T>
 */
public abstract class Assertion<T extends Assertion> {

    private BlockStatement originalBlockStatement;
    private BooleanExpression booleanExpression;

    public Assertion() {
        this.booleanExpression = new BooleanExpression(ConstantExpression.TRUE);
    }

    public Assertion(final BlockStatement blockStatement, final BooleanExpression booleanExpression) {
        Validate.notNull(booleanExpression);

        this.originalBlockStatement = blockStatement; // the BlockStatement might be null! we do not always have the original expression available
        this.booleanExpression = booleanExpression;
    }

    public BooleanExpression booleanExpression() {
        return booleanExpression;
    }

    public BlockStatement originalBlockStatement() {
        return originalBlockStatement;
    }

    public void renew(BooleanExpression booleanExpression) {
        Validate.notNull(booleanExpression);

        // don't renew the source position to keep the new assertion expression without source code replacement
        // booleanExpression.setSourcePosition(this.booleanExpression);

        this.booleanExpression = booleanExpression;
    }

    public void and(T other) {
        Validate.notNull(other);

        BooleanExpression newBooleanExpression =
                new BooleanExpression(
                        new BinaryExpression(
                                booleanExpression(),
                                Token.newSymbol(Types.LOGICAL_AND, -1, -1),
                                other.booleanExpression()
                        )
                );
        newBooleanExpression.setSourcePosition(booleanExpression());

        renew(newBooleanExpression);
    }

    public void or(T other) {
        Validate.notNull(other);

        BooleanExpression newBooleanExpression =
                new BooleanExpression(
                        new BinaryExpression(
                                booleanExpression(),
                                Token.newSymbol(Types.LOGICAL_OR, -1, -1),
                                other.booleanExpression()
                        )
                );
        newBooleanExpression.setSourcePosition(booleanExpression());

        renew(newBooleanExpression);
    }
}
