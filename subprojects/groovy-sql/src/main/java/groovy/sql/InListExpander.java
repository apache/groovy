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
package groovy.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Rewrites a SQL string and parameter list so that {@link InListParameter}
 * markers expand to {@code ?, ?, ..., ?} placeholders in the SQL and their
 * contained values are spliced into the parameter list at the corresponding
 * position.
 * <p>
 * The SQL is walked character-by-character so that {@code ?} characters
 * appearing inside single-quoted strings, double-quoted identifiers, line
 * comments, and block comments are ignored.
 * <p>
 * Package-private; consumed by {@link Sql} prior to {@code prepareStatement}.
 */
final class InListExpander {

    private InListExpander() {}

    static SqlWithParams expand(String sql, List<?> params) {
        if (!containsInList(params)) {
            return new SqlWithParams(sql, params);
        }
        StringBuilder out = new StringBuilder(sql.length());
        List<Object> expanded = new ArrayList<>(params.size());
        int paramIdx = 0;
        int i = 0;
        int len = sql.length();
        while (i < len) {
            char c = sql.charAt(i);
            switch (c) {
                case '\'':
                    i = copyQuoted(sql, i, '\'', out);
                    break;
                case '"':
                    i = copyQuoted(sql, i, '"', out);
                    break;
                case '-':
                    if (i + 1 < len && sql.charAt(i + 1) == '-') {
                        i = copyLineComment(sql, i, out);
                    } else {
                        out.append(c);
                        i++;
                    }
                    break;
                case '/':
                    if (i + 1 < len && sql.charAt(i + 1) == '*') {
                        i = copyBlockComment(sql, i, out);
                    } else {
                        out.append(c);
                        i++;
                    }
                    break;
                case '?':
                    if (paramIdx < params.size() && params.get(paramIdx) instanceof InListParameter) {
                        Collection<?> values = ((InListParameter) params.get(paramIdx)).getValues();
                        // Sql.inList(...) enforces non-empty, but the interface is public
                        // so a custom implementation could still return an empty collection.
                        if (values == null || values.isEmpty()) {
                            throw new IllegalArgumentException(
                                "InListParameter must supply a non-null, non-empty collection");
                        }
                        Iterator<?> it = values.iterator();
                        out.append('?');
                        expanded.add(it.next());
                        while (it.hasNext()) {
                            out.append(", ?");
                            expanded.add(it.next());
                        }
                    } else {
                        out.append('?');
                        if (paramIdx < params.size()) {
                            expanded.add(params.get(paramIdx));
                        }
                    }
                    paramIdx++;
                    i++;
                    break;
                default:
                    out.append(c);
                    i++;
            }
        }
        // Append any trailing params that had no corresponding '?' in the SQL
        // (matches existing setParameters behaviour — the warning is logged there).
        while (paramIdx < params.size()) {
            Object p = params.get(paramIdx++);
            if (p instanceof InListParameter) {
                Collection<?> values = ((InListParameter) p).getValues();
                if (values == null || values.isEmpty()) {
                    throw new IllegalArgumentException(
                        "InListParameter must supply a non-null, non-empty collection");
                }
                expanded.addAll(values);
            } else {
                expanded.add(p);
            }
        }
        return new SqlWithParams(out.toString(), expanded);
    }

    private static boolean containsInList(List<?> params) {
        for (Object p : params) {
            if (p instanceof InListParameter) return true;
        }
        return false;
    }

    private static int copyQuoted(String sql, int start, char quote, StringBuilder out) {
        int len = sql.length();
        out.append(quote);
        int i = start + 1;
        while (i < len) {
            char c = sql.charAt(i);
            out.append(c);
            if (c == quote) {
                if (i + 1 < len && sql.charAt(i + 1) == quote) {
                    out.append(quote);
                    i += 2;
                } else {
                    return i + 1;
                }
            } else {
                i++;
            }
        }
        return i;
    }

    private static int copyLineComment(String sql, int start, StringBuilder out) {
        int len = sql.length();
        int i = start;
        while (i < len) {
            char c = sql.charAt(i);
            out.append(c);
            i++;
            if (c == '\n' || c == '\r') {
                return i;
            }
        }
        return i;
    }

    private static int copyBlockComment(String sql, int start, StringBuilder out) {
        int len = sql.length();
        out.append('/');
        out.append('*');
        int i = start + 2;
        while (i < len) {
            char c = sql.charAt(i);
            out.append(c);
            if (c == '*' && i + 1 < len && sql.charAt(i + 1) == '/') {
                out.append('/');
                return i + 2;
            }
            i++;
        }
        return i;
    }
}
