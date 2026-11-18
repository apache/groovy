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
 * The parenthesized body of {@code OVER}: partition, order, then an
 * optional frame. Absent lists are empty. Renderers emit the clauses in
 * that SQL order regardless of the order they were written in GINQ.
 * An empty specification renders {@code OVER ()}.
 *
 * @since 7.0.0
 */
@Incubating
@CompileStatic
class SqlWindow {
    final List<SqlExpr> partitionBy
    final List<SqlOrderSpec> orderBy
    final SqlWindowFrame frame

    SqlWindow(List<SqlExpr> partitionBy, List<SqlOrderSpec> orderBy, SqlWindowFrame frame) {
        this.partitionBy = partitionBy == null ? Collections.<SqlExpr>emptyList() : List.copyOf(partitionBy)
        this.orderBy = orderBy == null ? Collections.<SqlOrderSpec>emptyList() : List.copyOf(orderBy)
        this.frame = frame
    }
}
