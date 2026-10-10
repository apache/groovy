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
package org.apache.groovy.lsp.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CamelHumps matching for workspace symbol search. {@code ASF} matches
 * {@code AbstractServerFactory}; {@code ServerFactory} skips the
 * {@code Abstract} hump. An all-lowercase query is a single hump and
 * only matches inside one candidate hump.
 */
public final class NameHumps {

    private NameHumps() {
    }

    /**
     * Whether {@code query} matches {@code name} as CamelHumps or as a
     * case-insensitive substring.
     *
     * @param query typed query, possibly {@code null}
     * @param name candidate identifier, possibly {@code null}
     * @return {@code true} when the name should be shown
     */
    public static boolean matches(final String query, final String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        if (query == null || query.isEmpty()) {
            return true;
        }
        if (name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return matchesHumps(splitHumps(query), splitHumps(name));
    }

    private static boolean matchesHumps(final List<String> queryHumps, final List<String> candidateHumps) {
        int qi = 0;
        for (int ci = 0; ci < candidateHumps.size() && qi < queryHumps.size(); ci++) {
            if (humpMatches(queryHumps.get(qi), candidateHumps.get(ci))) {
                qi++;
            }
        }
        return qi == queryHumps.size();
    }

    private static boolean humpMatches(final String queryHump, final String candidateHump) {
        if (queryHump.isEmpty() || candidateHump.isEmpty()) {
            return false;
        }
        if (Character.toLowerCase(queryHump.charAt(0)) != Character.toLowerCase(candidateHump.charAt(0))) {
            return false;
        }
        return isSubsequence(queryHump.substring(1), candidateHump.substring(1));
    }

    private static boolean isSubsequence(final String needle, final String haystack) {
        int hi = 0;
        for (int ni = 0; ni < needle.length(); ni++) {
            char c = Character.toLowerCase(needle.charAt(ni));
            boolean found = false;
            while (hi < haystack.length()) {
                boolean match = Character.toLowerCase(haystack.charAt(hi)) == c;
                hi++;
                if (match) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    static List<String> splitHumps(final String name) {
        List<String> humps = new ArrayList<>();
        if (name == null || name.isEmpty()) {
            return List.of();
        }
        int start = 0;
        for (int i = 1; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) || Character.isDigit(c)) {
                humps.add(name.substring(start, i));
                start = i;
            }
        }
        humps.add(name.substring(start));
        return List.copyOf(humps);
    }
}
