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

final class ChannelCompositionTest {

    @Test
    void testFilter() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def source = AsyncChannel.create(10)
            def evens = source.filter { it % 2 == 0 }

            async {
                (1..10).each { source.send(it) }
                source.close()
            }

            def results = []
            for (val in evens) { results << val }
            assert results == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testMap() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def source = AsyncChannel.create(10)
            def doubled = source.map { it * 2 }

            async {
                (1..5).each { source.send(it) }
                source.close()
            }

            def results = []
            for (val in doubled) { results << val }
            assert results == [2, 4, 6, 8, 10]
        '''
    }

    @Test
    void testFilterAndMap() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def source = AsyncChannel.create(10)
            def pipeline = source
                .filter { it > 3 }
                .map { it * 10 }

            async {
                (1..5).each { source.send(it) }
                source.close()
            }

            def results = []
            for (val in pipeline) { results << val }
            assert results == [40, 50]
        '''
    }

    @Test
    void testMerge() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def ch1 = AsyncChannel.create(10)
            def ch2 = AsyncChannel.create(10)
            def merged = ch1.merge(ch2)

            async {
                ch1.send(1); ch1.send(2); ch1.close()
            }
            async {
                ch2.send(10); ch2.send(20); ch2.close()
            }

            def results = []
            for (val in merged) { results << val }
            assert results.sort() == [1, 2, 10, 20]
        '''
    }

    @Test @org.junit.jupiter.api.Disabled("split cleanup issue when run with other tests")
    void testSplit() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.Awaitable

            def source = AsyncChannel.create(10)
            def (big, small) = source.split { it >= 5 }

            // Send all values and close — all fit in buffer, no blocking
            (1..8).each { source.send(it) }
            source.close()

            // Read both output channels concurrently
            def bigTask = Awaitable.go {
                def r = []
                for (val in big) { r << val }
                r
            }
            def smallTask = Awaitable.go {
                def r = []
                for (val in small) { r << val }
                r
            }

            assert await(bigTask).sort() == [5, 6, 7, 8]
            assert await(smallTask).sort() == [1, 2, 3, 4]
        '''
    }

    @Test
    void testTap() {
        assertScript '''
            import groovy.concurrent.AsyncChannel

            def source = AsyncChannel.create(10)
            def monitor = AsyncChannel.create(10)
            def output = source.tap(monitor)

            async {
                source.send('a')
                source.send('b')
                source.close()
            }

            def outputResults = []
            for (val in output) { outputResults << val }

            def monitorResults = []
            monitor.close()
            for (val in monitor) { monitorResults << val }

            assert outputResults == ['a', 'b']
            assert monitorResults == ['a', 'b']
        '''
    }
}
