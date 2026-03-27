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
 * Tests for {@code @Invariant} applied to loop statements.
 */
class LoopInvariantTests extends BaseTestClass {

    @Test
    void invariantOnForInLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int sum = 0
            @Invariant({ 0 <= i && i <= 4 })
            for (int i in 0..4) {
                sum += i
            }
            assert sum == 10
        '''
    }

    @Test
    void invariantOnClassicForLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int product = 1
            @Invariant({ product >= 1 })
            for (int i = 1; i <= 5; i++) {
                product *= i
            }
            assert product == 120
        '''
    }

    @Test
    void invariantOnWhileLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int n = 10
            @Invariant({ n >= 0 })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    @Test
    void invariantOnDoWhileLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int count = 0
            @Invariant({ count >= 0 })
            do {
                count++
            } while (count < 3)
            assert count == 3
        '''
    }

    @Test
    void multipleInvariantsOnLoop() {
        assertScript '''
            import groovy.contracts.Invariant

            int sum = 0
            @Invariant({ sum >= 0 })
            @Invariant({ sum <= 100 })
            for (int i in 1..5) {
                sum += i
            }
            assert sum == 15
        '''
    }

    @Test
    void invariantViolationThrows() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant

            int n = 5
            @Invariant({ n > 0 })
            while (n >= 0) {
                n--
            }
        '''
    }

    @Test
    void invariantWithComplexExpression() {
        assertScript '''
            import groovy.contracts.Invariant

            def items = []
            @Invariant({ items.size() <= 5 })
            for (int i in 1..5) {
                items << i
            }
            assert items == [1, 2, 3, 4, 5]
        '''
    }

    @Test
    void invariantViolationOnFirstIteration() {
        shouldFail AssertionError, '''
            import groovy.contracts.Invariant

            int x = -1
            @Invariant({ x >= 0 })
            for (int i in 0..2) {
                x = i
            }
        '''
    }

    @Test
    void classInvariantStillWorksWithLoopInvariantTransform() {
        assertScript '''
            import groovy.contracts.Invariant

            @Invariant({ property != null })
            class Foo {
                def property
                Foo(val) { property = val }
            }

            def f = new Foo('hello')
            assert f.property == 'hello'
        '''
    }
}

