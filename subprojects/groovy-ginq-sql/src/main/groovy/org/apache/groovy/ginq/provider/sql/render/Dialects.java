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
package org.apache.groovy.ginq.provider.sql.render;

import org.apache.groovy.ginq.provider.sql.ir.SqlExpr;
import org.apache.groovy.lang.annotation.Incubating;

import java.util.Locale;

/**
 * The native dialects that cover the high-variance points called out by
 * GEP-26: limit/offset syntax, identifier quoting, and a few function names.
 * Comprehensive dialect coverage stays with the jOOQ provider.
 *
 * @since 6.0.0
 */
@Incubating
public final class Dialects {
    /**
     * Names accepted by {@link #resolve(String)}, for diagnostics.
     */
    public static final String SUPPORTED = "ansi, h2, postgres, mysql, mariadb, sqlite";

    private Dialects() {
    }

    /**
     * Returns the dialect named {@code name}, or ANSI when {@code name} is
     * null or empty. The match is case-insensitive. Unknown names return null.
     *
     * @param name the dialect name
     * @return the dialect, or null when the name is not supported
     */
    public static Dialect resolve(String name) {
        if (name == null || name.isEmpty()) {
            return new AnsiDialect();
        }
        switch (name.toLowerCase(Locale.ROOT)) {
            case "ansi":
            case "default":
            case "h2":
                // H2 folds unquoted identifiers to upper case, so quoting a
                // lowercase name would miss tables created without quotes.
                // Its limit syntax, null ordering and function names are ANSI.
                return new AnsiDialect();
            case "postgres":
            case "postgresql":
                return new PostgresDialect();
            case "mysql":
            case "mariadb":
                return new MySqlDialect();
            case "sqlite":
                return new SqliteDialect();
            default:
                return null;
        }
    }

    private static String quote(String identifier, char mark) {
        String q = String.valueOf(mark);
        return q + identifier.replace(q, q + q) + q;
    }

    private static void limitOffset(SqlExpr offset, SqlExpr fetch, Dialect.LimitRender out) {
        if (fetch == null && offset != null) {
            // LIMIT is required before OFFSET. This is the largest unsigned
            // BIGINT, the usual stand-in for "no limit".
            out.text(" LIMIT 18446744073709551615");
        } else if (fetch != null) {
            out.text(" LIMIT ");
            out.expr(fetch);
        }
        if (offset != null) {
            out.text(" OFFSET ");
            out.expr(offset);
        }
    }

    private static final class PostgresDialect implements Dialect {
        @Override
        public String identifier(String identifier) {
            return quote(identifier, '"');
        }

        @Override
        public String realTypeName() {
            return "DOUBLE PRECISION";
        }
    }

    private static final class MySqlDialect implements Dialect {
        @Override
        public String identifier(String identifier) {
            return quote(identifier, '`');
        }

        @Override
        public boolean explicitNullOrdering() {
            return false;
        }

        @Override
        public Dialect.SearchPosition searchPosition() {
            return Dialect.SearchPosition.LOCATE;
        }

        @Override
        public void renderLimit(SqlExpr offset, SqlExpr fetch, LimitRender out) {
            limitOffset(offset, fetch, out);
        }
    }

    private static final class SqliteDialect implements Dialect {
        @Override
        public String identifier(String identifier) {
            return quote(identifier, '"');
        }

        @Override
        public String realTypeName() {
            return "REAL";
        }

        @Override
        public String functionName(String ansiName) {
            switch (ansiName) {
                case "CHAR_LENGTH":
                    return "LENGTH";
                case "SUBSTRING":
                    return "SUBSTR";
                case "GREATEST":
                    // scalar max/min; GREATEST/LEAST arrived only in later SQLite
                    return "MAX";
                case "LEAST":
                    return "MIN";
                case "FLOOR":
                case "CEILING":
                    throw new IllegalArgumentException(ansiName
                            + " is not supported by the sqlite dialect. Rewrite the query without floor() or ceil(),"
                            + " or use a dialect that provides those functions");
                default:
                    return ansiName;
            }
        }

        @Override
        public Dialect.SearchPosition searchPosition() {
            return Dialect.SearchPosition.INSTR;
        }

        @Override
        public boolean substringFromFor() {
            return false;
        }

        @Override
        public boolean moduloFunction() {
            return false;
        }

        @Override
        public boolean explicitNullOrdering() {
            return false;
        }

        @Override
        public void renderLimit(SqlExpr offset, SqlExpr fetch, LimitRender out) {
            limitOffset(offset, fetch, out);
        }
    }
}
