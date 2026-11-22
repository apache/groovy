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
 * GROOVY-11377: a typo at the end of a closure used to be reported as
 * "Unexpected input: '{'" at the opening brace, rather than at the
 * unexpected token.
 */
final class Groovy11377 {

    @Test
    void testClosureParseErrorIsReportedAtUnexpectedToken() {
        def err = shouldFail('10.times { bar()) }\n')
        def msg = err.message
        assert msg.contains("Missing '}'")
        assert msg.contains('column 17')
        assert !msg.contains("Unexpected input: '{'")
    }

    @Test
    void testNonClosureBlockErrorStaysAtTheUnexpectedToken() {
        def err = shouldFail('for (int n=0;n<10;n++) { foo()) }\n')
        def msg = err.message
        assert msg.contains("Missing '}'")
        assert msg.contains('column 31')
        assert !msg.contains("Unexpected input: '{'")
    }
}
