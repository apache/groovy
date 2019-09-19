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

class AmbiguousListOrMethodTest extends GroovyTestCase {

    void testLocalVariableVersion() {
        def foo = [3, 2, 3]

        def val = foo [0]
        assert val == 3
    }

    void testUndefinedPropertyVersion() {
        shouldFail(MissingPropertyException) {
            def val = this.foo [0]
        }
    }

    void testMethodCallVersion() {
        def val = foo([0])
        assert val == 1
    }


    def foo(int val) {
        return null
    }

    def foo(List myList) {
        return myList.size()
    }

    void testCanFindCorrectMethod() {
        def e = new Example()
        assert e["", ""] == 2
        assert e[""] == 1
    }

}

class Example {
    def getAt(String a, String b) {return 2}
    def getAt(String a) {return 1}
}
