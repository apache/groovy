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
package org.apache.groovy.contracts.tests.inv

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@code @Decreases} applied to loop statements.
 */
class LoopDecreasesTests extends BaseTestClass {

    @Test
    void decreasesOnWhileLoop() {
        assertScript '''
            import groovy.contracts.Decreases

            int n = 10
            @Decreases({ n })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    @Test
    void decreasesOnClassicForLoop() {
        assertScript '''
            import groovy.contracts.Decreases

            int remaining = 5
            @Decreases({ remaining })
            for (int i = 0; i < 5; i++) {
                remaining--
            }
            assert remaining == 0
        '''
    }

    @Test
    void decreasesWithExpression() {
        assertScript '''
            import groovy.contracts.Decreases

            int lo = 0, hi = 10
            @Decreases({ hi - lo })
            while (lo < hi) {
                lo++
            }
            assert lo == 10
        '''
    }

    @Test
    void decreasesViolationWhenNotDecreasing() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int n = 5
            @Decreases({ n })
            while (n > 0) {
                // oops, not decreasing
                n++
                if (n > 10) break
            }
        '''
    }

    @Test
    void decreasesViolationWhenStayingSame() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int n = 5
            int count = 0
            @Decreases({ n })
            while (n > 0 && count < 3) {
                // n stays the same - should violate
                count++
            }
        '''
    }

    @Test
    void decreasesLexicographic() {
        assertScript '''
            import groovy.contracts.Decreases

            // outer stays the same while inner decreases,
            // then outer decreases and inner resets
            int outer = 2, inner = 3
            @Decreases({ [outer, inner] })
            while (outer > 0) {
                if (inner > 0) {
                    inner--
                } else {
                    outer--
                    inner = 3
                }
            }
            assert outer == 0
        '''
    }

    @Test
    void decreasesLexicographicViolation() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int a = 5, b = 3
            @Decreases({ [a, b] })
            while (a > 0) {
                // a stays the same, b increases - should violate
                b++
                if (b > 10) break
            }
        '''
    }

    @Test
    void decreasesLexicographicFirstElementDecreases() {
        assertScript '''
            import groovy.contracts.Decreases

            // first element decreases, second can do anything
            int x = 5, y = 0
            @Decreases({ [x, y] })
            while (x > 0) {
                x--
                y += 10  // second element increases, but first decreased so it is OK
            }
            assert x == 0
        '''
    }

    @Test
    void decreasesCombinedWithInvariant() {
        assertScript '''
            import groovy.contracts.Decreases
            import groovy.contracts.Invariant

            int n = 5
            @Invariant({ n >= 0 })
            @Decreases({ n })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }
}
