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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10450 {

    private static final String ONE = 'domain.equalsIgnoreCase("google.com")'
    private static final String TWO = 'score == 11'

    private static final String CHECKS = '''\
        def logicalNot = one.negate()
        assert  logicalNot.test("acme.org", 11)
        assert !logicalNot.test("google.com", 11)

        def logicalOr = one | two
        assert  logicalOr.test("google.com", 11)
        assert  logicalOr.test("google.com", 0)
        assert  logicalOr.test("x", 11)
        assert !logicalOr.test("x", 0)

        def logicalAnd = one & two
        assert  logicalAnd.test("google.com", 11)
        assert !logicalAnd.test("google.com", 0)
        assert !logicalAnd.test("x", 11)
        assert !logicalAnd.test("x", 0)
    '''

    //--------------------------------------------------------------------------

    @Test
    void testAnonymousInnerClass() {
        assertScript """import java.util.function.*
            def one = new BiPredicate<String, Integer>() {
                @Override
                boolean test(String domain, Integer score) {
                    return $ONE
                }
            }
            def two = new BiPredicate<String, Integer>() {
                @Override
                boolean test(String domain, Integer score) {
                    return $TWO
                }
            }
            $CHECKS
        """
    }

    @Test
    void testClosure() {
        assertScript """import java.util.function.*
            BiPredicate<String, Integer> one = { domain, score -> $ONE }
            BiPredicate<String, Integer> two = { domain, score -> $TWO }
            $CHECKS
        """
    }

    @Test
    void testLambda() {
        assertScript """import java.util.function.*
            BiPredicate<String, Integer> one = (domain, score) -> $ONE
            BiPredicate<String, Integer> two = (domain, score) -> $TWO
            $CHECKS
        """
    }
}
