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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ternaryX;

/**
 * Utility class for commonly called methods
 */
public class MethodCallUtils {

    private MethodCallUtils() { }

    /**
     * Creates a statement that appends the supplied expression to the target receiver.
     *
     * @param result the receiver of the {@code append} call
     * @param expr the expression to append
     * @return a statement wrapping the append call
     */
    public static Statement appendS(Expression result, Expression expr) {
        MethodCallExpression append = callX(result, "append", expr);
        append.setImplicitThis(false);
        return stmt(append);
    }

    /**
     * Creates an explicit {@code toString()} method call expression.
     *
     * @param object the receiver of the call
     * @return a {@code toString()} call expression
     */
    public static Expression toStringX(final Expression object) {
        MethodCallExpression toString = callX(object, "toString");
        toString.setImplicitThis(false);
        return toString;
    }

    /**
     * Creates a null-safe expression that renders {@code null} as the string {@code "null"}.
     *
     * @param object the expression to stringify
     * @return a conditional stringification expression
     */
    public static Expression maybeNullToStringX(final Expression object) {
        return ternaryX(isNullX(object), constX("null"), toStringX(object));
    }
}
