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

package org.apache.groovy.lsp.internal.feature;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;

import java.util.List;

/**
 * Shared call-argument shape used by signature help and inlays.
 */
public final class CallSites {

    private CallSites() {
    }

    static boolean hasNamedArgs(final Expression arguments) {
        for (Expression arg : argumentExpressions(arguments)) {
            if (arg instanceof MapExpression) {
                return true;
            }
        }
        return false;
    }

    static List<Expression> argumentExpressions(final Expression arguments) {
        if (arguments instanceof TupleExpression tuple) {
            return tuple.getExpressions();
        }
        if (arguments == null) {
            return List.of();
        }
        return List.of(arguments);
    }
}
