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

import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.boolX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;

/**
 * <p>A loop-invariant assertion that must hold at the start of each iteration.</p>
 *
 * @since 6.0.0
 */
public class LoopInvariant extends Assertion<LoopInvariant> {

    /**
     * Shared invariant instance representing an unconstrained loop invariant.
     */
    public static final LoopInvariant DEFAULT = new LoopInvariant(block(), boolX(constX(true)));

    /**
     * Creates a loop invariant that defaults to {@code true}.
     */
    public LoopInvariant() {
    }

    /**
     * Creates a loop invariant from the supplied source block and boolean expression.
     *
     * @param blockStatement the original invariant block
     * @param booleanExpression the normalized invariant expression
     */
    public LoopInvariant(BlockStatement blockStatement, BooleanExpression booleanExpression) {
        super(blockStatement, booleanExpression);
    }
}
