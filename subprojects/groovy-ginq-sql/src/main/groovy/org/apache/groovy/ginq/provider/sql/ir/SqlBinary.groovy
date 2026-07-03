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
 * Represents a binary operation with a SQL operator, e.g.
 * {@code =}, {@code <>}, {@code AND}, {@code OR}, {@code +}.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class SqlBinary extends SqlExpr {
    final String op
    final SqlExpr left
    final SqlExpr right

    SqlBinary(String op, SqlExpr left, SqlExpr right) {
        this.op = op
        this.left = left
        this.right = right
    }
}
