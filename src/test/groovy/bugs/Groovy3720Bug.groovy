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

import groovy.mock.interceptor.*

class Groovy3720Bug extends GroovyTestCase {
    void testCreateStubNode() {
        def stubNodeContext1 = new StubFor(AnotherNode3720) 
        assertNotNull stubNodeContext1.proxyInstance()

        def stubNodeContext2 = new StubFor(MyNode3720) 
        assertNotNull stubNodeContext2.proxyInstance()
    }

    void testCreateStubNodeDelegate() {
        def stubNodeContext1 = new StubFor(AnotherNode3720) 
        assertNotNull stubNodeContext1.proxyDelegateInstance()

        def stubNodeContext2 = new StubFor(MyNode3720) 
        assertNotNull stubNodeContext2.proxyDelegateInstance()
    }
    
    void testCreateMockNode() {
        def mockNodeContext1 = new MockFor(AnotherNode3720) 
        assertNotNull mockNodeContext1.proxyInstance()

        def mockNodeContext2 = new MockFor(MyNode3720) 
        assertNotNull mockNodeContext2.proxyInstance()
    }

    void testCreateMockNodeDelegate() {
        def mockNodeContext1 = new MockFor(AnotherNode3720) 
        assertNotNull mockNodeContext1.proxyDelegateInstance()

        def mockNodeContext2 = new MockFor(MyNode3720) 
        assertNotNull mockNodeContext2.proxyDelegateInstance()
    }
}

abstract class MyNode3720 {}

abstract class BaseNode3720 {
    abstract m1()
}

abstract class AnotherNode3720 extends BaseNode3720 {
    abstract m2()
}