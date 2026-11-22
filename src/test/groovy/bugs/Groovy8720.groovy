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
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-8720: a final variable assigned in try, with catch always
 * rethrowing, is definitely assigned after the try/catch and must compile.
 */
final class Groovy8720 {

    @Test
    void testFinalVariableAssignedInTryWithRethrowInCatchIsDefinitelyAssigned() {
        assertScript '''
            final Long ms
            try {
                ms = 1L
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(e)
            }
            assert ms == 1L
        '''
    }

    @Test
    void testFinalVariableAssignedInTryThenThrowStillCompilesAndRethrows() {
        def err = shouldFail(RuntimeException) {
            assertScript '''
                final Long ms
                try {
                    ms = 1L
                    throw new IllegalArgumentException('boom')
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e)
                }
                ms
            '''
        }
        assert err.cause instanceof IllegalArgumentException
        assert err.cause.message == 'boom'
    }
}
