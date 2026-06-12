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

import groovy.mock.interceptor.StubFor
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

final class Groovy2271 {

    private static final String TEST_TEXT = "I'm a mock"

    @Test
    void testClosureMock() {
        def fooStub = new StubFor(Foo)
        fooStub.demand.createBar(0..2) { TEST_TEXT }

        Closure closure = { createBar() }

        fooStub.use {
            def foo = new Foo()
            assertEquals(TEST_TEXT, foo.createBar())
            closure.delegate = foo
            assertEquals(TEST_TEXT, closure.call())
        }
    }

    static class Foo {
        Object createBar() {
            throw new RuntimeException('We should never get here!')
        }
    }
}
