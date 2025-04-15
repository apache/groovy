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
package groovy.mock.interceptor

import org.junit.jupiter.api.Test

final class MockForJavaTest {

    @Test
    void testIterator() {
        def iteratorContext = new MockFor(Iterator)
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        def iterator = iteratorContext.proxyDelegateInstance()
        iteratorContext.demand.next() { 'foo' }
        def iterator2 = iteratorContext.proxyDelegateInstance()

        assert new IteratorCounter().count(iterator2) == 2
        assert iterator2.next() == 'foo'
        iteratorContext.verify(iterator2)

        assert new IteratorCounter().count(iterator) == 2
        iteratorContext.verify(iterator)

        iteratorContext = new MockFor(Iterator)
        iteratorContext.demand.hasNext(7..7) { true }
        iteratorContext.demand.hasNext() { false }
        def iterator3 = iteratorContext.proxyDelegateInstance()
        assert new IteratorCounter().count(iterator3) == 7
        iteratorContext.verify(iterator3)
    }

    // GROOVY-4843
    @Test
    void testStream() {
        def streamContext = new MockFor(FileInputStream)
        streamContext.demand.read(1..1) { byte[] b ->
            b[0] = 1
            b[1] = 2
            return 2
        }
        def p = System.getProperty('os.name').contains('Windows') ? 'NUL' : '/dev/null'
        def s = streamContext.proxyDelegateInstance(p)
        byte[] buffer = new byte[2]
        assert s.read(buffer) == 2
        assert buffer[0] == 1
        assert buffer[1] == 2
        streamContext.verify(s)
    }

    @Test
    void testString() {
        def stringContext = new MockFor(String)
        stringContext.demand.endsWith(2..2) { String arg -> arg == 'foo' }
        def s = stringContext.proxyDelegateInstance()
        assert !s.endsWith('bar')
        assert s.endsWith('foo')
        stringContext.verify(s)
    }
}
