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
package org.apache.groovy.runtime.methoddispatching.vm8

import groovy.transform.CompileStatic

import static groovy.test.GroovyAssert.isAtLeastJdk

@CompileStatic
class StaticMethodOverloadCompileStaticTest extends GroovyTestCase {
    void testOneStaticMethod() {
        if (isJdk9()) return
        assert FooOne.foo() == "FooOne.foo()"
        assert BarOne.foo() == "BarOne.foo()"
    }

    void testTwoStaticMethods() {
        if (isJdk9()) return
        assert FooTwo.foo() == "FooTwo.foo()"
        assert FooTwo.foo(0) == "FooTwo.foo(0)"
        assert BarTwo.foo() == "BarTwo.foo()"
        assert BarTwo.foo(0) == "BarTwo.foo(0)"
    }

    void testMoreThanTwoStaticMethods() {
        if (isJdk9()) return
        assert FooThree.foo() == "FooThree.foo()"
        assert FooThree.foo(0) == "FooThree.foo(0)"
        assert FooThree.foo(0, 1) == "FooThree.foo(0, 1)"
        assert BarThree.foo() == "BarThree.foo()"
        assert BarThree.foo(0) == "BarThree.foo(0)"
        assert BarThree.foo(0, 1) == "BarThree.foo(0, 1)"
    }

    // FIX_JDK9 JDK9 (and presumably 10+) doesn't like the way we do static methods in interfaces - remove this version
    // check once we fix the problem
    boolean isJdk9() {
        isAtLeastJdk('9.0')
    }
}
