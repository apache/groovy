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

import groovy.test.GroovyTestCase

class Groovy4202Bug extends GroovyTestCase {
    void testSuccessiveMCModificationFirstClassThenInstance() {
        def inst0 = new Foo4202()
        def inst1 = new Foo4202()

        Foo4202.metaClass.addedMethod0 = { 'hello0'}

        inst0.metaClass.addedMethod1 = { 'hello10'}
        inst0.metaClass.addedMethod2 = { 'hello20'}
        
        inst1.metaClass.addedMethod1 = { 'hello11'}
        inst1.metaClass.addedMethod2 = { 'hello21'}
    }
}

class Foo4202 { }
