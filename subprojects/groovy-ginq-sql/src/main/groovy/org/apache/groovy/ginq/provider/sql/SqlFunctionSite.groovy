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
package org.apache.groovy.ginq.provider.sql

import groovy.transform.CompileStatic
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression

/**
 * Operations a {@link SqlFunctionMapping} needs from the query translator.
 * Shapes that are not a plain SQL function call — {@code LIKE}, and
 * {@code SUBSTRING} with Groovy's 0-based indexes — are translated here
 * so the mapping stays a registry entry.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
interface SqlFunctionSite {
    /**
     * Translates a sub-expression.
     *
     * @param expression the GINQ expression
     * @return its SQL IR, or {@code null} when translation recorded an error
     */
    SqlExpr expr(Expression expression)

    /**
     * Records a compile-time rejection. In mixed mode this raises
     * {@link Untranslatable} so the clause can run in memory.
     *
     * @param message the diagnostic, without the mixed-mode hint
     * @param node the AST node to highlight
     */
    void fail(String message, ASTNode node)

    /**
     * Translates {@code contains}, {@code startsWith} or {@code endsWith}.
     *
     * @param call the method call
     * @param args the arguments
     * @param matchBefore whether the pattern may match before the text
     * @param matchAfter whether the pattern may match after the text
     * @return the {@code LIKE} expression, or {@code null} when rejected
     */
    SqlExpr like(MethodCallExpression call, List<Expression> args, boolean matchBefore, boolean matchAfter)

    /**
     * Translates {@code substring}, shifting Groovy's 0-based indexes.
     *
     * @param call the method call
     * @param args the arguments
     * @return the {@code SUBSTRING} expression, or {@code null} when rejected
     */
    SqlExpr substring(MethodCallExpression call, List<Expression> args)
}
