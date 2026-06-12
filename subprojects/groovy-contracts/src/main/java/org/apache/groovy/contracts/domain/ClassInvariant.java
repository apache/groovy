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
 * <p>A class-invariant assertion.</p>
 */
public class ClassInvariant extends Assertion<ClassInvariant> {

    /**
     * Shared invariant instance representing an unconstrained class invariant.
     */
    public static final ClassInvariant DEFAULT = new ClassInvariant(block(), boolX(constX(true)));

    /**
     * Creates a class invariant that defaults to {@code true}.
     */
    public ClassInvariant() {
    }

    /**
     * Creates a class invariant from the supplied source block and boolean expression.
     *
     * @param blockStatement the original invariant block
     * @param booleanExpression the normalized invariant expression
     */
    public ClassInvariant(BlockStatement blockStatement, BooleanExpression booleanExpression) {
        super(blockStatement, booleanExpression);
    }
}
