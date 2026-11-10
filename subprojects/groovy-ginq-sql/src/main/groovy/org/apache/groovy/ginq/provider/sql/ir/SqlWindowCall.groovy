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
import org.apache.groovy.ginq.provider.sql.SqlFunctionMapping
import org.apache.groovy.lang.annotation.Incubating

/**
 * A window function call: {@code function OVER (window)}.
 * <p>
 * {@code function} is the call without {@code OVER}, usually a
 * {@link SqlFunction}. {@code count()} with no argument is a
 * {@link SqlCountStar}. The native and jOOQ renderers both visit this node.
 * <p>
 * {@code SqlFunctionRegistry.standard()} registers the portable GINQ window
 * functions as {@link SqlFunctionMapping.Kind#WINDOW} before it is sealed.
 * {@code GinqToSqlTranslator} recognizes
 * {@code fn(...) over(partitionby ... orderby ... rows|range ...)} and wraps
 * the function in this node. Adjustments that are not part of the function
 * call itself, such as {@code rowNumber}'s {@code - 1}, sit outside this node.
 * A name that is not registered is rejected with a stable message so mixed
 * mode still evaluates the clause in memory.
 * <p>
 * Window {@code min} is that kind, not {@code Math.min} and not the grouped
 * aggregate path. Set-operation {@code ORDER BY} matching treats two window
 * calls as the same expression when the function, partition, order and frame
 * match, so a window projection can be ordered on a union result.
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
