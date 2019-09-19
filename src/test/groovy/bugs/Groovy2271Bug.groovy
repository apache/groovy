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
package groovy.bugs

import groovy.mock.interceptor.StubFor
import groovy.test.GroovyTestCase

class Groovy2271Bug extends GroovyTestCase {
    static final String TEST_TEXT = "I'm a mock"

    def void testClosureMock() {
        StubFor fooStub = new StubFor(Groovy2271Foo)

        fooStub.demand.createBar(0..2) {TEST_TEXT}

        Closure closure = {createBar()}

        fooStub.use {
            Groovy2271Foo foo = new Groovy2271Foo()
            assertEquals(TEST_TEXT, foo.createBar())
            closure.delegate = foo
            assertEquals(TEST_TEXT, closure.call())
        }
    }
}

class Groovy2271Foo {
    def createBar() {
        throw new RuntimeException("We should never get here!")
    }
}

