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
package org.apache.groovy.ginq.provider.sql;

import org.apache.groovy.lang.annotation.Incubating;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Which ANSI functions a dialect can render, including under a different
 * spelling. The translator consults this before emitting a {@code SqlFunction},
 * and the renderer consults it again so a function that cannot be rendered is
 * never written into SQL. A dialect that lacks a function is not a special
 * case in the planner: the name is added to {@link #UNSUPPORTED} and both
 * strict mode and mixed mode pick it up.
 * <p>
 * A function that the dialect spells differently but can still render
 * ({@code CHAR_LENGTH} as SQLite {@code LENGTH}, {@code GREATEST} as
 * {@code MAX}) stays supported. Only functions with no faithful rendering
 * belong here.
 *
 * @since 7.0.0
 */
@Incubating
public final class FunctionSupport {
    private static final Map<String, Set<String>> UNSUPPORTED = unsupported();

    private FunctionSupport() {
    }

    /**
     * Canonical dialect family for a configured name. Unknown names are
     * lower-cased and then support every function, which is what a jOOQ
     * dialect without a known gap does.
     *
     * @param dialectName the configured dialect, or {@code null}
     * @return the family name used by {@link #supports(String, String)}
     */
    public static String canonical(String dialectName) {
        if (dialectName == null || dialectName.isEmpty()) {
            return "ansi";
        }
        switch (dialectName.toLowerCase(Locale.ROOT)) {
            case "postgresql":
            case "postgres":
                return "postgres";
            case "mariadb":
            case "mysql":
                return "mysql";
            case "sqlite":
                return "sqlite";
            case "h2":
                return "h2";
            case "default":
            case "ansi":
                return "ansi";
            default:
                return dialectName.toLowerCase(Locale.ROOT);
        }
    }

    /**
     * Whether {@code ansiName} can be rendered for {@code dialectName}.
     *
     * @param dialectName the configured dialect
     * @param ansiName the ANSI function name, for example {@code FLOOR}
     * @return {@code false} only when the dialect has no rendering of that function
     */
    public static boolean supports(String dialectName, String ansiName) {
        if (ansiName == null || ansiName.isEmpty()) {
            return false;
        }
        Set<String> missing = UNSUPPORTED.get(canonical(dialectName));
        return missing == null || !missing.contains(ansiName);
    }

    private static Map<String, Set<String>> unsupported() {
        Map<String, Set<String>> map = new HashMap<>();
        map.put("sqlite", set("FLOOR", "CEILING"));
        return Collections.unmodifiableMap(map);
    }

    private static Set<String> set(String... names) {
        Set<String> values = new HashSet<>();
        Collections.addAll(values, names);
        return Collections.unmodifiableSet(values);
    }
}
