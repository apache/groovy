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

/**
 * Tests that 6821 is fixed using an explicit Outer.this notation if not using @CompileStatic
 */
class Groovy6821Bug extends GroovyTestCase {
    void testShouldAccessOuterClassMethodFromSpecialConstructorCall() {
        assertScript '''
class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        String a

        private class Inner extends Parent {
           Inner() { super(Outer.this.getA()) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer(a:'ok')
    assert o.test() == 'ok'
'''
    }
    void testShouldAccessOuterClassMethodFromSpecialConstructorCallUsingPropertyAccess() {
        assertScript '''
class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        String a

        private class Inner extends Parent {
           Inner() { super(Outer.this.a) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer(a:'ok')
    assert o.test() == 'ok\'
'''
    }
}
