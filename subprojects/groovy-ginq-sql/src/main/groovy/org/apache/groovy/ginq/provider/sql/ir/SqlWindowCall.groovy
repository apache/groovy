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
package org.apache.groovy.ginq.provider.sql.ir

import groovy.transform.CompileStatic
import org.apache.groovy.lang.annotation.Incubating

/**
 * A window function call: {@code function OVER (window)}.
 * <p>
 * {@code function} is the call without {@code OVER}, usually a
 * {@link SqlFunction}. {@code count()} with no argument is a
 * {@link SqlCountStar}, which this node can already wrap; the built-in
 * registry does not register it yet. The native and jOOQ renderers both
 * visit this node. They are reached only when a translator actually
 * builds one.
 * <p>
 * <b>How a follow-up adds GINQ window functions.</b> Register each name
 * with {@link org.apache.groovy.ginq.provider.sql.SqlFunctionMapping#window}
 * on the built-in registry before it is sealed, for example
 * {@code rowNumber} to {@code ROW_NUMBER} with no arguments. The mapping
 * translates only the function call. {@code GinqToSqlTranslator} already
 * recognizes {@code fn(...) over(partitionby ... orderby ... rows|range ...)},
 * looks the name up as {@link org.apache.groovy.ginq.provider.sql.SqlFunctionMapping.Kind#WINDOW},
 * and wraps the function in this node. No change to the renderer spine
 * is required. Until those registrations exist,
 * {@code SqlFunctionRegistry.standard()} has no window mappings and
 * {@code over} is rejected with a stable message so mixed mode still
 * evaluates the clause in memory.
 * <p>
 * Window {@code min} is that kind, not {@code Math.min} and not the
 * aggregate path. Set-operation {@code ORDER BY} matching does not yet
 * treat two window calls as the same expression; extend it if a window
 * projection is ordered on a union result.
 *
 * @since 7.0.0
 */
@Incubating
@CompileStatic
class SqlWindowCall extends SqlExpr {
    final SqlExpr function
    final SqlWindow window

    SqlWindowCall(SqlExpr function, SqlWindow window) {
        if (function == null || window == null) {
            throw new IllegalArgumentException('a window call needs a function and a window specification')
        }
        this.function = function
        this.window = window
    }
}
