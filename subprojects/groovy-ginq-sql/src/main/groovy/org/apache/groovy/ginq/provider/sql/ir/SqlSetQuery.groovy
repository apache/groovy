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
 * Represents two queries combined with a SQL set operation.
 * The left side may itself be a {@code SqlSetQuery}, mirroring the
 * left-nested chaining of GINQ set operations. Ordering and limiting
 * apply to the combined result and reference select-list ordinals.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class SqlSetQuery implements SqlQueryNode {
    final SqlQueryNode left
    final SetOp op
    final SqlQuery right
    final List<SqlOrderSpec> orderBy = []
    SqlExpr offset
    SqlExpr fetch

    SqlSetQuery(SqlQueryNode left, SetOp op, SqlQuery right) {
        this.left = left
        this.op = op
        this.right = right
    }

    enum SetOp {
        UNION, UNION_ALL, INTERSECT, MINUS
    }
}
