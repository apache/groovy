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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ConstructorThisCallBug {

    @Test // GROOVY-7014
    void testThisCallingInstanceMethod() {
        def err = shouldFail '''
            class Base {
                String getData() { return "ABCD" }
                Base() { this(getData()) }
                Base(String arg) {}
            }
        '''
        assert err.message.contains("Can't access instance method 'getData' before the class is constructed")
    }

    @Test
    void testNestedClassThisCallingInstanceMethod() {
        def err = shouldFail '''
            class Base {
                static class Nested {
                    String getData() { return "ABCD" }
                    Nested() { this(getData()) }
                    Nested(String arg) {}
                }
            }
        '''
        assert err.message.contains("Can't access instance method 'getData' before the class is constructed")
    }

    @Test
    void testThisCallingStaticMethod() {
        assertScript '''
            class Base {
                private String b
                static String getData() { return "ABCD" }
                Base() { this(getData()) }
                Base(String b) { this.b = b }
                String toString() { b }
            }
            assert new Base().toString() == 'ABCD'
        '''
    }

    @Test
    void testNestedThisCallingStaticMethod() {
        assertScript '''
            class Base {
                static class Nested {
                    private String b
                    static String getData() { return "ABCD" }
                    Nested() { this(getData()) }
                    Nested(String b) { this.b = b }
                    String toString() { b }
                }
            }
            assert new Base.Nested().toString() == 'ABCD'
        '''
    }

    @Test
    void testInnerClassSuperCallingInstanceMethod() {
        assertScript '''
            class Parent {
                String str
                Parent(String s) { str = s }
            }
            class Outer {
                static String a

                private class Inner extends Parent {
                   Inner() { super(myA()) }
                }

                String test() { new Inner().str }
                String myA() { a }
            }
            def o = new Outer()
            Outer.a = 'ok'
            assert o.test() == 'ok'
        '''
    }

    @Test
    void testInnerClassSuperCallingStaticProperty() {
        assertScript '''
            class Parent {
                String str
                Parent(String s) { str = s }
            }
            class Outer {
                static String a

                private class Inner extends Parent {
                   Inner() { super(getA()) }
                }

                String test() { new Inner().str }
            }
            def o = new Outer()
            Outer.a = 'ok'
            assert o.test() == 'ok'
        '''
    }

    @Test // GROOVY-994
    void testCallA() {
        assert new ConstructorCallA("foo").toString() == 'foo'
        assert new ConstructorCallA(9).toString() == '81'
        assert new ConstructorCallA().toString() == '100'
    }

    private static class ConstructorCallA {
        private String a

        ConstructorCallA(String a) { this.a = a }

        ConstructorCallA(int a) { this("" + (a * a)) } // call another constructor

        ConstructorCallA() { this(10) } // call another constructor

        String toString() { a }
    }
}
