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
package org.apache.groovy.contracts;

import org.apache.groovy.contracts.generation.Configurator;

import java.util.List;

/**
 * Shared runtime support for {@link groovy.contracts.Decreases} termination
 * measures, used by both the loop variant ({@code @Decreases} on a loop) and the
 * recursion variant ({@code @Decreases} on a method, via {@link MethodVariantSupport}).
 * <p>
 * It is the single source of truth for what "strictly decreases and stays
 * well-founded" means — a scalar {@link Comparable} (with a non-negative floor for
 * {@link Number}s) or a {@link List} compared lexicographically — and for whether
 * the check runs at all under the {@code -ea}/{@code -da} configuration.
 * The comparison itself is violation-agnostic ({@link #describeFailure} returns a
 * description rather than throwing) so each caller can raise the appropriate
 * violation type.
 *
 * @since 6.0.0
 */
public final class VariantSupport {

    private VariantSupport() {
    }

    /**
     * Whether termination-measure checks are enabled for {@code className} under the
     * current {@code -ea}/{@code -da} configuration (a {@code null} class name uses
     * the global default). Mirrors how {@code @Requires}/{@code @Ensures} gate.
     *
     * @param className the enclosing class name, or {@code null} for the global default
     * @return {@code true} if the check should run
     */
    public static boolean enabled(final String className) {
        return Configurator.checkAssertionsEnabled(className);
    }

    /**
     * Describe how {@code curr} fails to be a valid strict, well-founded decrease
     * from {@code prev}, or {@code null} if it is valid. {@link List} measures are
     * compared lexicographically.
     *
     * @param prev the measure before
     * @param curr the measure after
     * @return a failure description suffix, or {@code null} if the decrease is valid
     */
    public static String describeFailure(final Object prev, final Object curr) {
        if (prev instanceof List<?> && curr instanceof List<?>) {
            return describeLexicographic((List<?>) prev, (List<?>) curr);
        }
        if (prev instanceof Comparable && curr instanceof Comparable) {
            return describeScalar(prev, curr);
        }
        return "is not Comparable: prev=" + prev + ", curr=" + curr;
    }

    /**
     * Loop-variant entry point invoked from generated loop-body code: if enabled,
     * verify {@code curr} strictly decreased from {@code prev} and stayed
     * well-founded, throwing {@link LoopVariantViolation} otherwise.
     *
     * @param prev      the variant before the iteration
     * @param curr      the variant after the iteration
     * @param className the enclosing class name (for {@code -ea}/{@code -da} gating)
     */
    public static void checkLoopVariant(final Object prev, final Object curr, final String className) {
        if (!enabled(className)) return;
        String failure = describeFailure(prev, curr);
        if (failure != null) {
            throw new LoopVariantViolation("<groovy.contracts.Decreases> loop variant " + failure);
        }
    }

    /**
     * Loop-variant entry point for the first iteration of a loop, where no previous value
     * exists to compare against: if enabled, verify a scalar {@link Number} measure is
     * non-negative at loop entry (GROOVY-12128), throwing {@link LoopVariantViolation}
     * otherwise. This mirrors the non-negative floor {@link #describeFailure} applies to
     * every subsequent iteration-start value. {@link List} (lexicographic) measures have no
     * single deciding component at entry, so they are not floor-checked here.
     *
     * @param curr      the variant at first loop entry
     * @param className the enclosing class name (for {@code -ea}/{@code -da} gating)
     */
    public static void checkLoopVariantEntry(final Object curr, final String className) {
        if (!enabled(className)) return;
        if (curr instanceof Number && ((Number) curr).doubleValue() < 0) {
            throw new LoopVariantViolation("<groovy.contracts.Decreases> loop variant is negative at loop entry: " + curr);
        }
    }

    @SuppressWarnings("unchecked")
    private static String describeScalar(final Object prev, final Object curr) {
        Comparable<Object> prevComp = (Comparable<Object>) prev;
        if (prevComp.compareTo(curr) <= 0) {
            return "did not decrease: was " + prev + ", now " + curr;
        }
        if (curr instanceof Number && ((Number) curr).doubleValue() < 0) {
            return "became negative: " + curr;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String describeLexicographic(final List<?> prev, final List<?> curr) {
        int size = Math.min(prev.size(), curr.size());
        for (int i = 0; i < size; i++) {
            Object p = prev.get(i);
            Object c = curr.get(i);
            if (!(p instanceof Comparable) || !(c instanceof Comparable)) {
                return "element at position " + i + " is not Comparable: prev=" + p + ", curr=" + c;
            }
            int cmp = ((Comparable<Object>) p).compareTo(c);
            if (cmp > 0) {
                return null; // this element strictly decreased — lexicographically satisfied
            }
            if (cmp < 0) {
                return "increased at position " + i + ": was " + prev + ", now " + curr;
            }
            // equal at this position — inspect the next
        }
        return "did not decrease: was " + prev + ", now " + curr;
    }
}
