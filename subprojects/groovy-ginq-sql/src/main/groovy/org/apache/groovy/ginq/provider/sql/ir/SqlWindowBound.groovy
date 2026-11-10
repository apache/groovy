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

import java.math.BigDecimal
import java.math.BigInteger

/**
 * One end of a window frame. GINQ writes a signed bound or {@code null};
 * the translator maps that onto these kinds before rendering:
 * {@code null} is unbounded, {@code 0} is the current row, a negative
 * number is {@code PRECEDING} of its magnitude, and a positive number is
 * {@code FOLLOWING}.
 * <p>
 * A {@link Kind#PRECEDING} or {@link Kind#FOLLOWING} offset is a
 * non-negative SQL expression, never a signed GINQ bound. The native
 * renderer prints that expression. The jOOQ renderer accepts an integer
 * literal and rejects anything else, so a follow-up that binds the offset
 * must extend that renderer or keep the offset a literal.
 *
 * @since 7.0.0
 */
@Incubating
@CompileStatic
class SqlWindowBound {
    enum Kind {
        UNBOUNDED_PRECEDING,
        PRECEDING,
        CURRENT_ROW,
        FOLLOWING,
        UNBOUNDED_FOLLOWING
    }

    final Kind kind
    /** Non-null for {@link Kind#PRECEDING} and {@link Kind#FOLLOWING}. */
    final SqlExpr offset

    private SqlWindowBound(Kind kind, SqlExpr offset) {
        this.kind = kind
        this.offset = offset
    }

    static SqlWindowBound unboundedPreceding() {
        return new SqlWindowBound(Kind.UNBOUNDED_PRECEDING, null)
    }

    static SqlWindowBound unboundedFollowing() {
        return new SqlWindowBound(Kind.UNBOUNDED_FOLLOWING, null)
    }

    static SqlWindowBound currentRow() {
        return new SqlWindowBound(Kind.CURRENT_ROW, null)
    }

    static SqlWindowBound preceding(SqlExpr offset) {
        return bounded(Kind.PRECEDING, offset)
    }

    static SqlWindowBound following(SqlExpr offset) {
        return bounded(Kind.FOLLOWING, offset)
    }

    private static SqlWindowBound bounded(Kind kind, SqlExpr offset) {
        if (offset == null) {
            throw new IllegalArgumentException('a bounded window frame offset is required')
        }
        if (offset instanceof SqlLiteral) {
            Object value = ((SqlLiteral) offset).value
            if (!(value instanceof Number) || !nonNegative((Number) value)) {
                throw new IllegalArgumentException(
                        'a window frame offset must be a non-negative number; the direction is PRECEDING or FOLLOWING')
            }
        }
        return new SqlWindowBound(kind, offset)
    }

    private static boolean nonNegative(Number number) {
        if (number instanceof BigDecimal) return ((BigDecimal) number).signum() >= 0
        if (number instanceof BigInteger) return ((BigInteger) number).signum() >= 0
        if (number instanceof Double || number instanceof Float) {
            double d = number.doubleValue()
            return !Double.isNaN(d) && !Double.isInfinite(d) && d >= 0d
        }
        return number.longValue() >= 0L
    }
}
