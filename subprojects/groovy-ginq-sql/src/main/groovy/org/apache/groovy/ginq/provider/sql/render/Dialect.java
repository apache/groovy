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

/**
 * Abstracts the dialect-specific concerns of the native SQL renderer.
 * The defaults produce ANSI SQL.
 *
 * @since 6.0.0
 */
@Incubating
public interface Dialect {
    /**
     * Renders an identifier, e.g. a table name, table alias or column name.
     * The default applies no quoting so identifiers follow the database's
     * case-folding of unquoted DDL identifiers.
     *
     * @param identifier the identifier
     * @return the rendered identifier
     */
    default String identifier(String identifier) {
        return identifier;
    }

    /**
     * Renders a simple or schema-qualified name. Each part is passed through
     * {@link #identifier(String)}, so quoting matches other table and column
     * references in this dialect.
     *
     * @param name a plain table name, possibly schema-qualified
     * @return the rendered name
     * @since 6.0.0
     */
    default String qualifiedName(String name) {
        String[] parts = name.split("\\.", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('.');
            }
            sb.append(identifier(parts[i]));
        }
        return sb.toString();
    }

    /**
     * Maps an ANSI function name, e.g. {@code UPPER} or {@code SUM},
     * to its dialect-specific counterpart. {@code GREATEST} and {@code LEAST}
     * are included; a dialect that spells those as scalar {@code MAX} and
     * {@code MIN} overrides this method.
     *
     * @param ansiName the ANSI function name
     * @return the dialect-specific function name
     */
    default String functionName(String ansiName) {
        return ansiName;
    }

    /**
     * How a 1-based character search is spelled. The translator subtracts
     * one so the result matches Groovy {@code indexOf}: the first character
     * is 0, and a search that finds nothing is -1, because these functions
     * return 0 when the text is absent.
     *
     * @return the spelling
     * @since 6.0.0
     */
    default SearchPosition searchPosition() {
        return SearchPosition.POSITION_IN;
    }

    /**
     * Spelling of the search behind Groovy {@code indexOf}.
     *
     * @since 6.0.0
     */
    enum SearchPosition {
        /** {@code POSITION(needle IN haystack)}. */
        POSITION_IN,
        /** {@code LOCATE(needle, haystack)}. */
        LOCATE,
        /** {@code INSTR(haystack, needle)}. */
        INSTR
    }

    /**
     * Whether {@code SUBSTRING} is rendered in the ANSI
     * {@code SUBSTRING(x FROM start FOR length)} form. Dialects that only
     * accept {@code SUBSTR(x, start, length)} return {@code false}.
     *
     * @return {@code true} for the ANSI form
     * @since 6.0.0
     */
    default boolean substringFromFor() {
        return true;
    }

    /**
     * Whether {@code MOD(x, y)} is available. Dialects that only have the
     * {@code %} operator return {@code false}.
     *
     * @return {@code true} when {@code MOD} is a function
     * @since 6.0.0
     */
    default boolean moduloFunction() {
        return true;
    }

    /**
     * Whether {@code ORDER BY} accepts {@code NULLS FIRST}/{@code NULLS LAST}.
     * When {@code false}, the renderer emulates the requested null order so
     * results stay faithful to GINQ, which defaults to nulls last.
     *
     * @return {@code true} when the dialect has explicit null ordering
     * @since 6.0.0
     */
    default boolean explicitNullOrdering() {
        return true;
    }

    /**
     * SQL type name that forces real division, matching Groovy {@code /}
     * rather than integer truncation.
     *
     * @return the type name
     * @since 6.0.0
     */
    default String realTypeName() {
        return "DOUBLE";
    }

    /**
     * Returns the set operator subtracting one query result from another.
     *
     * @return the operator text, {@code EXCEPT} by default
     */
    default String minusOperator() {
        return "EXCEPT";
    }

    /**
     * Appends the {@code OFFSET}/{@code FETCH} clause. The default is ANSI
     * {@code OFFSET n ROWS FETCH NEXT m ROWS ONLY}, using {@code FETCH FIRST}
     * when there is no offset. Implementations must pass each non-null
     * expression to {@link LimitRender#expr(SqlExpr)} exactly once, in the
     * order their text emits placeholders, so the parameter list stays aligned.
     *
     * @param offset the offset expression, or {@code null}
     * @param fetch the row-count expression, or {@code null}
     * @param out receives clause text and expressions
     * @since 6.0.0
     */
    default void renderLimit(SqlExpr offset, SqlExpr fetch, LimitRender out) {
        if (offset != null) {
            out.text(" OFFSET ");
            out.expr(offset);
            out.text(" ROWS");
        }
        if (fetch != null) {
            out.text(offset != null ? " FETCH NEXT " : " FETCH FIRST ");
            out.expr(fetch);
            out.text(" ROWS ONLY");
        }
    }

    /**
     * Receives the text and expressions of a limit clause, in placeholder order.
     *
     * @since 6.0.0
     */
    interface LimitRender {
        /**
         * Appends literal SQL text that contains no placeholders.
         *
         * @param text the text to append
         */
        void text(String text);

        /**
         * Renders an offset or fetch expression, including its placeholder.
         *
         * @param expr the expression to render
         */
        void expr(SqlExpr expr);
    }
}
