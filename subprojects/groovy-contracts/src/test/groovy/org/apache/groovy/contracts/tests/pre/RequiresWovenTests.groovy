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
package org.apache.groovy.contracts.tests.pre

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Tests for the {@code woven} and {@code direct} members of {@code @Requires}:
 * an unwoven precondition is a documented caller obligation whose enforcement
 * already exists (in the body, or — {@code direct = false} — in invoked code),
 * so no assertion is generated; {@code direct} is pure metadata.
 */
class RequiresWovenTests extends BaseTestClass {

    @Test
    void wovenDefaultStillAsserts() {
        assertScript '''
            import groovy.contracts.Requires
            import org.apache.groovy.contracts.PreconditionViolation

            class C {
                @Requires({ n >= 0 })
                static int f(int n) { n }
            }
            assert C.f(5) == 5
            try {
                C.f(-1)
                assert false
            } catch (PreconditionViolation expected) {
            }
        '''
    }

    @Test
    void unwovenGeneratesNoAssertion() {
        assertScript '''
            import groovy.contracts.Requires

            class C {
                // the obligation is enforced by requireNonNull; a violating caller sees the
                // library's NPE, not a PreconditionViolation — the annotation documents, only
                @Requires(value = { s != null }, woven = false, direct = false)
                static Object describe(Object s) {
                    Objects.requireNonNull(s)
                    "value: $s"
                }
            }
            assert C.describe('x') == 'value: x'
            try {
                C.describe(null)
                assert false
            } catch (NullPointerException expected) {   // the enforcement, untouched
            }
        '''
    }

    @Test
    void unwovenArmIsFilteredFromTheAndedSet() {
        assertScript '''
            import groovy.contracts.Requires
            import org.apache.groovy.contracts.PreconditionViolation

            class C {
                @Requires({ n < 100 })                              // woven
                @Requires(value = { n >= 0 }, woven = false)        // enforced in-body (deliberately absent here)
                static int f(int n) { n }
            }
            assert C.f(5) == 5
            assert C.f(-1) == -1   // only the unwoven arm's condition is violated: no assertion fires
            try {
                C.f(200)           // the woven arm still asserts
                assert false
            } catch (PreconditionViolation expected) {
            }
        '''
    }

    @Test
    void unwovenDoesNotWeaveIntoInheritingOverrides() {
        assertScript '''
            import groovy.contracts.Requires

            class Base {
                @Requires(value = { n >= 0 }, woven = false)
                int f(int n) { n }
            }
            class Derived extends Base {
                @Override int f(int n) { n + 1 }   // inherits the (unwoven) obligation
            }
            assert new Derived().f(-5) == -4   // no inherited assertion is generated either
        '''
    }

    @Test
    void directIsIgnoredForWovenPreconditions() {
        assertScript '''
            import groovy.contracts.Requires
            import org.apache.groovy.contracts.PreconditionViolation

            class C {
                // direct is implicitly true for woven preconditions — setting it changes nothing
                @Requires(value = { n >= 0 }, direct = false)
                static int f(int n) { n }
            }
            try {
                C.f(-1)
                assert false, 'the assertion weaves regardless of direct'
            } catch (PreconditionViolation expected) {
            }
        '''
    }

    @Test
    void membersAreRuntimeRetainedStructuredMetadata() {
        assertScript '''
            import groovy.contracts.Requires

            class C {
                @Requires(value = { s != null }, woven = false, direct = false)
                static Object f(Object s) {
                    Objects.requireNonNull(s)
                    s
                }
            }
            def arms = C.getDeclaredMethod('f', Object).getAnnotationsByType(Requires)
            assert arms.length == 1
            assert !arms[0].woven() && !arms[0].direct()
        '''
    }
}
