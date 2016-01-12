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

class StubForJavaTest extends GroovyTestCase {
    void testIterator() {
//        ProxyGenerator.INSTANCE.debug = true
        def iteratorContext = new StubFor(Iterator)
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        iteratorContext.demand.hasNext() { true }
        iteratorContext.demand.hasNext() { false }
        iteratorContext.ignore('dump')
        iteratorContext.ignore('getMetaClass')
        def iterator = iteratorContext.proxyDelegateInstance()
        def counter = new IteratorCounter()
        assert counter.count(iterator) == 1
        assert counter.count(iterator) == 1
        iteratorContext.verify(iterator)
    }

    void testString() {
        ProxyGenerator.INSTANCE.debug = false
        def stringContext = new StubFor(String)
        stringContext.demand.startsWith(2) { String arg -> arg == "wiz" }
        stringContext.demand.endsWith(2..2) { String arg -> arg == "foo" }
        def s = stringContext.proxyDelegateInstance()
        assert !s.endsWith("bar")
        assert s.endsWith("foo")
        assert !s.startsWith("bar")
        assert s.startsWith("wiz")
        stringContext.verify(s)
    }

}