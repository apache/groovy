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

final class AsyncScopeNestingTest {

    @Test
    void testRootScopeHasNoParent() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { scope ->
                assert scope.parent == null
                null
            }
        '''
    }

    @Test
    void testNestedScopeTracksParent() {
        assertScript '''
            import groovy.concurrent.AsyncScope

            AsyncScope.withScope { outer ->
                assert AsyncScope.current() == outer

                outer.async {
                    AsyncScope.withScope { inner ->
                        assert inner.parent == outer
                    }
                }
                null
            }
        '''
    }

    @Test
    void testCancelPropagatesFromParentToChild() {
        assertScript '''
            import groovy.concurrent.AsyncScope
            import java.util.concurrent.CountDownLatch

            def innerStarted = new CountDownLatch(1)

            try {
                AsyncScope.withScope { outer ->
                    outer.async {
                        AsyncScope.withScope { inner ->
                            inner.async {
                                innerStarted.countDown()
                                Thread.sleep(5000)
                            }
                        }
                    }
                    innerStarted.await(2, java.util.concurrent.TimeUnit.SECONDS)
                    Thread.sleep(50)
                    outer.cancelAll()
                    throw new RuntimeException('cancelled')
                }
            } catch (RuntimeException e) {
                assert e.message == 'cancelled'
            }
        '''
    }
}
