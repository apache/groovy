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
 * A {@code ROWS} or {@code RANGE} frame, always {@code BETWEEN} a lower
 * and an upper bound. GINQ's {@code rows} and {@code range} each take
 * both bounds, so a one-sided frame is not represented.
 *
 * @since 7.0.0
 */
@Incubating
@CompileStatic
class SqlWindowFrame {
    enum Unit {
        ROWS, RANGE
    }

    final Unit unit
    final SqlWindowBound lower
    final SqlWindowBound upper

    SqlWindowFrame(Unit unit, SqlWindowBound lower, SqlWindowBound upper) {
        if (unit == null || lower == null || upper == null) {
            throw new IllegalArgumentException('a window frame needs a unit and both bounds')
        }
        this.unit = unit
        this.lower = lower
        this.upper = upper
    }
}
