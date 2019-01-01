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

/**
 * Testing Groovy Stub support for single calls to the Collaborator
 */
class StubSingleCallTest extends GroovyTestCase {

    StubFor stub

    void setUp() {
        stub = new StubFor(Collaborator.class)
    }

    void testFirstOptionalOmitted() {
        stub.demand.one(0..1) { 1 }
        stub.use {
            def caller = new Caller()
        }
        stub.expect.verify()
        // Getting here means no exception, which is what we want to test.  (Fix for GROOVY-2309)
    }
}



