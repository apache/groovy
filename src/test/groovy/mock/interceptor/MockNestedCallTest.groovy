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

import groovy.test.GroovyTestCase

class MockNestedCallTest extends GroovyTestCase {

    void testRestore() {
        def mockTail = new MockFor(Coin)
        mockTail.demand.flip(0..9) {"tail"}

        def mockHead = new MockFor(Coin)
        mockHead.demand.flip(0..9) {"head"}

        def c = new Coin()
        assert c.flip() == "edge"
        mockTail.use(c) {
            assert c.flip() == "tail"
            mockHead.use(c) {
                assert c.flip() == "head"
            }
            assert c.flip() == "tail"
        }
        assert c.flip() == "edge"
    }
}

class Coin {
    def flip() { "edge" }
}
