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
 * GROOVY-11320: parenthesized variables such as {@code (foo) + (bar)}
 * used to be parsed as a cast, with "unable to resolve class foo".
 */
final class Groovy11320 {

    @Test
    void testParenthesizedVariablesCanBeAdded() {
        assertScript '''
            def foo = "spongebog"
            def bar = " squarepants"
            assert (foo) + (bar) == "spongebog squarepants"
        '''
        assertScript '''
            def testObj = 0
            assert (int)(testObj) + 10 == 10
        '''
    }
}
