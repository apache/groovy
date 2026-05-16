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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A visitor which collects the list of variable expressions which are closure shared.
 */
public class SharedVariableCollector extends ClassCodeVisitorSupport {

    private final Set<VariableExpression> closureSharedExpressions = new LinkedHashSet<VariableExpression>();
    private final SourceUnit unit;
    private boolean visited;

    /**
     * Creates a collector for the supplied source unit.
     */
    public SharedVariableCollector(final SourceUnit unit) {
        this.unit = unit;
    }

    /** {@inheritDoc} */
    @Override
    protected SourceUnit getSourceUnit() {
        return unit;
    }

    /**
     * Returns the collected closure-shared variable expressions.
     */
    public Set<VariableExpression> getClosureSharedExpressions() {
        return Collections.unmodifiableSet(closureSharedExpressions);
    }

    /**
     * Records closure-shared variables while avoiding recursive closure traversal.
     */
    @Override
    public void visitVariableExpression(final VariableExpression expression) {
        if (visited) {
            return; // we should not visit embedded closures recursively
        }
        visited = true;
        if (expression.isClosureSharedVariable()) closureSharedExpressions.add(expression);
        super.visitVariableExpression(expression);
    }
}
