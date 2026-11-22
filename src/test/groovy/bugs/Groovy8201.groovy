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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-8201: calling a method on a closure owner with the {@code this}
 * keyword should not go through {@code methodMissing}.
 */
final class Groovy8201 {

    @Test
    void testThisMethodCallInClosureDoesNotHitMethodMissing() {
        assertScript '''
            class RegressionReproducingClass {
                Closure getClosure() {
                    { -> this.getValue() }
                }
                String getValue() { 'foo' }
                def methodMissing(String name, args) {
                    throw new Exception("Should never get here!")
                }
            }
            assert new RegressionReproducingClass().closure.call() == 'foo'
        '''
    }

    @Test
    void testWorkaroundsStillWork() {
        assertScript '''
            class WithPropertyAccess {
                Closure getClosure() { { -> this.value } }
                String getValue() { 'foo' }
                def methodMissing(String name, args) { throw new Exception("missing $name") }
            }
            class WithoutThis {
                Closure getClosure() { { -> getValue() } }
                String getValue() { 'foo' }
                def methodMissing(String name, args) { throw new Exception("missing $name") }
            }
            assert new WithPropertyAccess().closure.call() == 'foo'
            assert new WithoutThis().closure.call() == 'foo'
        '''
    }
}
