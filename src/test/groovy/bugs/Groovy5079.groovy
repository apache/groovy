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

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-5079: an {@code assert} with a custom message used to fall
 * back to the old assertion facility and drop the negation from the
 * recorded expression: {@code (files || (! failIfNotFound))} was
 * printed as {@code (files || failIfNotFound)}.
 */
final class Groovy5079 {

    @Test
    void testAssertMessageKeepsNegationInExpression() {
        def files = []
        def failIfNotFound = true
        def err = shouldFail(AssertionError) {
            assert (files || (!failIfNotFound)), 'Error message'
        }
        def msg = err.message
        assert msg.contains('Error message')
        assert msg.contains('!(failIfNotFound)') || msg.contains('!failIfNotFound') || msg.contains('! failIfNotFound')
        // the original buggy rendering omitted the '!' entirely
        assert !msg.contains('Expression: (files || failIfNotFound)')
    }
}
