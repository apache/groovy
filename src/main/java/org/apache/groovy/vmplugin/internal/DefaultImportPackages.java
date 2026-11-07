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
package org.apache.groovy.vmplugin.internal;

import groovy.transform.Internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Orders default-import package prefixes by a caller-supplied precedence
 * array. Iteration order of the returned sets is that array's order.
 * Packages that are not in the array are dropped, so a {@code HashSet}
 * walk cannot put a later package ahead of an earlier one.
 *
 * @since 7.0.0
 */
@Internal
public final class DefaultImportPackages {

    private DefaultImportPackages() {
    }

    /**
     * Returns the members of {@code packages} that occur in {@code precedence},
     * iterated in {@code precedence} order. The result is unmodifiable.
     * An empty result is {@link Collections#emptySet()}. A single match is
     * {@link Collections#singleton(Object)}.
     *
     * @param packages packages discovered for one simple name; {@code null} is empty
     * @param precedence package prefixes in the order they must be tried; {@code null} yields an empty set
     * @return an immutable set in precedence order
     */
    public static Set<String> ordered(final Collection<String> packages, final String[] precedence) {
        if (packages == null || packages.isEmpty() || precedence == null || precedence.length == 0) {
            return Collections.emptySet();
        }
        List<String> matches = new ArrayList<>(Math.min(packages.size(), precedence.length));
        for (String candidate : precedence) {
            if (packages.contains(candidate)) {
                matches.add(candidate);
            }
        }
        if (matches.isEmpty()) {
            return Collections.emptySet();
        }
        if (matches.size() == 1) {
            return Collections.singleton(matches.get(0));
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(matches));
    }

    /**
     * Returns {@code existing} when it already contains {@code packagePrefix}.
     * Otherwise returns the precedence-ordered union. {@code packagePrefix}
     * values absent from {@code precedence} are not added.
     *
     * @param existing the packages already recorded, or {@code null}
     * @param packagePrefix the package to add
     * @param precedence package prefixes in try-order
     * @return an immutable set in precedence order, or {@code existing} when unchanged
     */
    public static Set<String> including(final Set<String> existing, final String packagePrefix, final String[] precedence) {
        if (existing != null && existing.contains(packagePrefix)) {
            return existing;
        }
        Set<String> combined = new HashSet<>(existing == null ? 2 : existing.size() + 1);
        if (existing != null) {
            combined.addAll(existing);
        }
        combined.add(packagePrefix);
        return ordered(combined, precedence);
    }

    /**
     * Unions {@code additions} into {@code target}. Each stored set is
     * rewritten with {@link #ordered}. An addition that contributes no
     * precedence package is ignored. When the value already stored contains
     * every incoming package, that same instance is kept.
     *
     * @param target class name to packages already collected; updated in place
     * @param additions class name to packages found by one scan; {@code null} is a no-op
     * @param precedence package prefixes in try-order
     */
    public static void merge(final Map<String, Set<String>> target, final Map<String, Set<String>> additions, final String[] precedence) {
        if (additions == null || additions.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Set<String>> entry : additions.entrySet()) {
            Set<String> incoming = ordered(entry.getValue(), precedence);
            if (incoming.isEmpty()) {
                continue;
            }
            target.merge(entry.getKey(), incoming, (existing, inc) -> {
                if (existing.containsAll(inc)) {
                    return existing;
                }
                Set<String> combined = new HashSet<>(existing.size() + inc.size());
                combined.addAll(existing);
                combined.addAll(inc);
                return ordered(combined, precedence);
            });
        }
    }
}
