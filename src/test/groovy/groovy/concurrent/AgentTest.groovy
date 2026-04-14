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

final class AgentTest {

    @Test
    void testCreateWithInitialValue() {
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create(0)
            assert agent.get() == 0
            agent.shutdown()
        '''
    }

    @Test
    void testSendUpdatesValue() {
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create(0)
            agent.send { it + 1 }
            agent.send { it + 1 }
            agent.send { it + 1 }
            def result = await(agent.getAsync())
            assert result == 3
            agent.shutdown()
        '''
    }

    @Test
    void testSendAndGetReturnsNewValue() {
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create(10)
            def result = await(agent.sendAndGet { it * 2 })
            assert result == 20
            agent.shutdown()
        '''
    }

    @Test
    void testConcurrentUpdatesAreSerialized() {
        assertScript '''
            import groovy.concurrent.Agent
            import java.util.concurrent.CountDownLatch

            def agent = Agent.create(0)
            def n = 100
            def latch = new CountDownLatch(n)

            n.times {
                Thread.start {
                    agent.send { it + 1 }
                    latch.countDown()
                }
            }
            latch.await(5, java.util.concurrent.TimeUnit.SECONDS)

            def result = await(agent.getAsync())
            assert result == n
            agent.shutdown()
        '''
    }

    @Test
    void testCollectionValueUpdates() {
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create([])
            agent.send { it + ['a'] }
            agent.send { it + ['b'] }
            def result = await(agent.sendAndGet { it + ['c'] })
            assert result == ['a', 'b', 'c']
            agent.shutdown()
        '''
    }

    @Test
    void testToString() {
        assertScript '''
            import groovy.concurrent.Agent

            def agent = Agent.create('hello')
            assert agent.toString() == 'Agent[hello]'
            agent.shutdown()
        '''
    }
}
