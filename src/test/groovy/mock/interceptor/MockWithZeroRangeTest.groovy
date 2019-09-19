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

import groovy.mock.interceptor.MockFor
import groovy.test.GroovyTestCase
import junit.framework.AssertionFailedError

class MockForWithZeroRangeTest extends GroovyTestCase {
    void testMockWithZeroRangeDemandAndNoCall() {
        MockFor mockForFoo = new MockFor(Foo)
        mockForFoo.demand.createBar(0..0) {}
        mockForFoo.use {
            println 'Foo is not called'
        }
        // We should get here and the test should pass.
    }

    void testMockWithZeroRangeDemandAndOneCall() {
        MockFor mockForFoo = new MockFor(Foo)
        mockForFoo.demand.createBar(0..0) {}
        shouldFail(AssertionFailedError) {
            mockForFoo.use {
                new Foo().createBar()
            }
        }
    }
}


class Foo {
    def createBar() {
        println 'bar'
    }
}