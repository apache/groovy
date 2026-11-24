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
 * An implicit call of a method of {@code Object} in a nested closure is passed on
 * by the enclosing closure, by its own resolve strategy, rather than answered by it.
 */
final class Groovy12829 {

    @Test
    void testObjectMethodInNestedClosure() {
        assertScript '''
            class Draft {
                String toString() { 'draft' }
            }

            class Report {
                String toString() { 'report' }

                List<String> describe() {
                    def outer = { ->
                        def inner = { -> toString() }
                        [inner(), toString()]
                    }
                    def plain = outer()

                    outer.resolveStrategy = Closure.DELEGATE_FIRST
                    outer.delegate = new Draft()
                    plain + outer()
                }
            }

            assert new Report().describe() == ['report', 'report', 'draft', 'draft']
        '''
    }
}
