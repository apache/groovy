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
 * GROOVY-8187: a method call written with the closure argument on following
 * lines, as in {@code return (1..1000).any { ... }} without parentheses around
 * {@code any}, used to throw MissingPropertyException for property {@code any}
 * instead of invoking the method with the attached closure.
 */
final class Groovy8187 {

    @Test
    void testAnyWithParenthesizedClosureArgument() {
        assertScript '''
            assert (1..1000).any({ final it -> it % 2000 == 2000 }) == false
        '''
    }

    @Test
    void testAnyWithClosureOnFollowingLinesInsidePrintln() {
        assertScript '''
            def result
            println(
                result = (1..1000)
                        .any
                        {
                            final it ->
                                it % 2000 == 2000
                        }
            )
            assert result == false
        '''
    }

    @Test
    void testAnyWithClosureOnFollowingLinesAsReturnValue() {
        assertScript '''
            def crashMeOutside() {
                return (1..1000)
                        .any
                        {
                            final it ->
                                it % 2000 == 2000
                        }
            }
            assert crashMeOutside() == false
        '''
    }

    @Test
    void testNegatedAnyWithClosureOnFollowingLines() {
        assertScript '''
            final result = !(1..1000)
                    .any
                    {
                        final it ->
                            it % 2 == 0
                    }
            assert result == false
        '''
    }
}
