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
package groovy.concurrent

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

final class AsyncScopeTimeoutTest {

    @Test
    void testBodyCompletesBeforeTimeout() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.time.Duration

            def result = AsyncScope.withScope(Duration.ofSeconds(5)) { scope ->
                def a = scope.async { 42 }
                await(a)
            }
            assert result == 42
        '''
    }

    @Test
    void testTimeoutCancelsScope() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.time.Duration
            import java.util.concurrent.TimeoutException

            try {
                AsyncScope.withScope(Duration.ofMillis(100)) { scope ->
                    scope.async { Thread.sleep(5000); 'should not complete' }
                    Thread.sleep(5000)
                    'should not reach'
                }
                assert false : 'should have thrown'
            } catch (TimeoutException e) {
                // expected — scope cancelled by timeout
                assert e.message.startsWith('Scope timed out after')
            }
        '''
    }
}
