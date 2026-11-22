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
 * GROOVY-10724: a parenthesized expression is not a type-cast when
 * followed by {@code in}, {@code as}, unary plus, or unary minus
 * (historically {@code (a) in []} failed with "unable to resolve class a").
 */
final class Groovy10724 {

    @Test
    void testParenthesizedExpressionBeforeInIsNotACast() {
        assertScript '''
            def a = 'x'
            assert !((a) in [])
            assert ((a)) in ['x']
            assert a in ['x']
        '''
    }

    @Test
    void testParenthesizedExpressionBeforeAsIsNotACast() {
        assertScript '''
            def a = 3
            assert ((a) as Integer) == 3
            assert (((a)) as Integer) == 3
        '''
    }

    @Test
    void testParenthesizedExpressionBeforeUnaryPlusOrMinusIsNotACast() {
        assertScript '''
            def a = 3
            assert ((a) + 4) == 7
            assert ((a) - 4) == -1
        '''
    }
}
