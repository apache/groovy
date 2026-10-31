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

import java.util.Locale

/**
 * Whitelist of SQL types accepted by {@code cast(expr, 'TYPE')}.
 * The canonical form is safe to splice into SQL text: letters, digits,
 * spaces, parentheses and one comma, with no quotes or other punctuation.
 * <p>
 * A length applies to character and binary types ({@code VARCHAR(20)}).
 * A precision, and an optional scale, apply to {@code DECIMAL} and
 * {@code NUMERIC} ({@code DECIMAL(10,2)}). Other parameterized forms,
 * including {@code INT(11)} and {@code TIMESTAMP(6)}, are rejected.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
final class SqlCastTypes {
    static final int MAX_LENGTH = 1_000_000
    static final int MAX_PRECISION = 1000

    private static final Set<String> BARE = names(
            'BOOLEAN', 'TINYINT', 'SMALLINT', 'INTEGER', 'INT', 'BIGINT',
            'REAL', 'FLOAT', 'DOUBLE', 'DOUBLE PRECISION',
            'DECIMAL', 'NUMERIC',
            'CHAR', 'CHARACTER', 'VARCHAR', 'CHARACTER VARYING',
            'DATE', 'TIME', 'TIMESTAMP',
            'CLOB', 'BLOB', 'BINARY', 'VARBINARY')
    private static final Set<String> LENGTH = names(
            'CHAR', 'CHARACTER', 'VARCHAR', 'CHARACTER VARYING', 'BINARY', 'VARBINARY')
    private static final Set<String> PRECISION = names('DECIMAL', 'NUMERIC')

    private SqlCastTypes() {
    }

    /**
     * Returns the canonical spelling of {@code raw}, or {@code null} when
     * it is not in the whitelist.
     *
     * @param raw the type text from the query, still in its source spelling
     * @return canonical SQL type text, or {@code null}
     */
    static String canonical(String raw) {
        if (raw == null) return null
        String compact = raw.trim().replaceAll('\\s+', ' ').toUpperCase(Locale.ROOT)
        if (!compact) return null
        if (BARE.contains(compact)) return compact

        int open = compact.indexOf('(')
        if (open <= 0 || !compact.endsWith(')')) return null
        String name = compact.substring(0, open).trim()
        String inner = compact.substring(open + 1, compact.length() - 1).trim()
        if (!name || inner.isEmpty() || inner.indexOf('(') >= 0 || inner.indexOf(')') >= 0) return null

        List<String> parts = new ArrayList<String>()
        for (String part : inner.split(',', -1)) {
            String piece = part.trim()
            if (!digits(piece)) return null
            parts.add(piece)
        }
        if (parts.size() == 1 && LENGTH.contains(name)) {
            int length = bounded(parts.get(0), 1, MAX_LENGTH)
            if (length < 0) return null
            return name + '(' + length + ')'
        }
        if (PRECISION.contains(name) && (parts.size() == 1 || parts.size() == 2)) {
            int precision = bounded(parts.get(0), 1, MAX_PRECISION)
            if (precision < 0) return null
            if (parts.size() == 1) return name + '(' + precision + ')'
            int scale = bounded(parts.get(1), 0, precision)
            if (scale < 0) return null
            return name + '(' + precision + ',' + scale + ')'
        }
        return null
    }

    private static Set<String> names(String... values) {
        Set<String> set = new LinkedHashSet<String>()
        Collections.addAll(set, values)
        return Collections.unmodifiableSet(set)
    }

    private static boolean digits(String piece) {
        if (!piece) return false
        for (int i = 0; i < piece.length(); i++) {
            char ch = piece.charAt(i)
            // char literals are Strings under static compilation
            if (ch < 48 || ch > 57) return false
        }
        return true
    }

    private static int bounded(String digits, int min, int max) {
        long value
        try {
            value = Long.parseLong(digits)
        } catch (NumberFormatException ignored) {
            return -1
        }
        if (value < min || value > max) return -1
        return (int) value
    }
}
