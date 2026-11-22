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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.util.concurrent.TimeUnit

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-9213: an unclosed parenthesis followed by many statements must
 * produce a compilation error in bounded time rather than looping.
 * (The ANTLR 2 parser is no longer supported; this covers the default parser.)
 */
final class Groovy9213 {

    private static String unmatchedParenScript(int statements) {
        '''
            int a = 0
            (
        ''' + ('a = 0\n' * statements)
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testUnmatchedParenInLongScript50() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().parse(unmatchedParenScript(50))
        }
        assert err.message.contains("Missing ')'")
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testUnmatchedParenInLongScript200() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().parse(unmatchedParenScript(200))
        }
        assert err.message.contains("Missing ')'")
    }
}
