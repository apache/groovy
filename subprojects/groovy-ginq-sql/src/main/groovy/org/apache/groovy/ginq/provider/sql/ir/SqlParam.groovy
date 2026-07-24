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
import org.codehaus.groovy.ast.expr.Expression

/**
 * Represents a bound parameter. Every literal and captured variable of the
 * GINQ query becomes a {@code SqlParam} holding the original Groovy expression;
 * the renderer emits a positional {@code ?} placeholder and collects the
 * expressions in emission order, so SQL text never contains user values.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
class SqlParam extends SqlExpr {
    final Expression valueExpr

    SqlParam(Expression valueExpr) {
        this.valueExpr = valueExpr
    }
}
