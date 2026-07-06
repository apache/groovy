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

/**
 * Runtime support for {@code checked} {@code @ThrowsIf} arm-sets (invoked from
 * generated code; not intended to be called by user code). The checks report a
 * broken contract <em>implementation</em> as a {@link ThrowsIfViolation} — never
 * as the arm's declared exception, which is defined behaviour delivered at method
 * entry.
 *
 * @since 6.0.0
 * @see ThrowsIfViolation
 */
public final class ThrowsIfSupport {

    private ThrowsIfSupport() {
    }

    /**
     * The <em>only-when</em> check, run when the method body throws: a throwable
     * matching some arm's exception type must be justified by some matching arm's
     * condition having held on entry. A justified (or unmatched, or
     * non-{@code exhaustive}) throwable is returned for rethrow — defined
     * behaviour propagates untouched.
     * <p>
     * {@link VirtualMachineError}s ({@code OutOfMemoryError}, {@code StackOverflowError},
     * ...) are never judged, whatever the arm types: a VM resource condition is outside
     * contract semantics — no {@code @ThrowsIf} claim, however {@code exhaustive}, reasons
     * about it — so it always propagates untouched.
     *
     * @param thrown     the throwable escaping the method body
     * @param held       per-arm: whether the arm's condition held on entry
     * @param types      per-arm: the arm's declared exception type
     * @param conditions per-arm: the arm's condition text (for the violation message)
     * @param exhaustive whether the whole arm-set claims exhaustiveness (an iff);
     *                   one-directional arm-sets are never only-when checked
     * @return {@code thrown}, for rethrow, when it is in-contract
     * @throws ThrowsIfViolation when {@code thrown} matches an arm's type but no
     *                           matching arm's condition held
     */
    public static Throwable onlyWhen(final Throwable thrown, final boolean[] held, final Class<?>[] types,
                                     final String[] conditions, final boolean exhaustive) {
        if (!exhaustive || thrown instanceof AssertionViolation || thrown instanceof VirtualMachineError) {
            return thrown;
        }
        boolean typeMatched = false;
        for (int i = 0; i < types.length; i++) {
            if (types[i].isInstance(thrown)) {
                if (held[i]) return thrown;   // justified: defined behaviour, propagate
                typeMatched = true;
            }
        }
        if (typeMatched) {
            throw new ThrowsIfViolation("@ThrowsIf violated: threw " + thrown.getClass().getSimpleName()
                    + " although no matching condition holds (conditions: " + String.join("; ", conditions) + ")");
        }
        return thrown;   // an exception type no arm mentions — outside the declared contract
    }

    /**
     * The <em>must-throw</em> check, run when the method body completes normally:
     * no unwoven arm's condition may have held on entry (a woven arm's guard makes
     * this unreachable for that arm by construction).
     *
     * @param held       per-unwoven-arm: whether the arm's condition held on entry
     * @param conditions per-unwoven-arm: the arm's condition text (for the violation message)
     * @throws ThrowsIfViolation when some condition held and the method nevertheless returned
     */
    public static void mustThrow(final boolean[] held, final String[] conditions) {
        for (int i = 0; i < held.length; i++) {
            if (held[i]) {
                throw new ThrowsIfViolation("@ThrowsIf violated: returned normally although the condition holds: "
                        + conditions[i]);
            }
        }
    }
}
