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

final class ChannelSelectTest {

    @Test
    void testSelectFirstAvailable() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def ch1 = AsyncChannel.create(10)
            def ch2 = AsyncChannel.create(10)

            def sel = ChannelSelect.from(ch1, ch2)

            async {
                Thread.sleep(50)
                ch1.send('from-ch1')
            }

            def result = await sel.select()
            assert result.index == 0
            assert result.value == 'from-ch1'
        '''
    }

    @Test
    void testSelectFromMultiple() {
        assertScript '''
            import groovy.concurrent.AsyncChannel
            import groovy.concurrent.ChannelSelect

            def fast = AsyncChannel.create(10)
            def slow = AsyncChannel.create(10)

            def sel = ChannelSelect.from(slow, fast)

            async { fast.send('fast-wins') }
            async { Thread.sleep(200); slow.send('slow') }

            def result = await sel.select()
            assert result.value == 'fast-wins'
            assert result.index == 1
        '''
    }
}
