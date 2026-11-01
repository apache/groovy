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
import groovy.transform.PackageScope
import org.apache.groovy.ginq.dsl.expression.GinqExpression
import org.apache.groovy.ginq.provider.sql.ir.SqlQuery
import org.apache.groovy.ginq.provider.sql.ir.SqlQueryNode

/**
 * Result of planning a query for mixed-mode execution.
 * <p>
 * When {@link #fullQuery} is set, the whole query translates to SQL.
 * Otherwise {@link #prefix} is the translatable clause prefix (star
 * projections) and {@link #tail} is the remainder, executed in memory.
 */
@PackageScope
@CompileStatic
class MixedPlan {
    final SqlQueryNode fullQuery
    final SqlQuery prefix
    final List<SqlQuery> columnCountQueries
    final GinqExpression tail
    final int pushedJoins

    private MixedPlan(SqlQueryNode fullQuery, SqlQuery prefix, List<SqlQuery> columnCountQueries,
                      GinqExpression tail, int pushedJoins) {
        this.fullQuery = fullQuery
        this.prefix = prefix
        this.columnCountQueries = columnCountQueries
        this.tail = tail
        this.pushedJoins = pushedJoins
    }

    static MixedPlan complete(SqlQueryNode fullQuery) {
        return new MixedPlan(fullQuery, null, Collections.<SqlQuery>emptyList(), null, 0)
    }

    static MixedPlan split(SqlQuery prefix, List<SqlQuery> columnCountQueries, GinqExpression tail, int pushedJoins) {
        return new MixedPlan(null, prefix, columnCountQueries, tail, pushedJoins)
    }
}
