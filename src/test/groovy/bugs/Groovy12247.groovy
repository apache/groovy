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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy12247 {

    @Test // super call to short form of method with default parameter
    void testSuperCallToBridgedMethod1() {
        assertScript '''
            abstract class Base<T extends Number> {
                protected String process(T value, String extra = null) {
                    "base($value,$extra)"
                }
            }

            abstract class Mid extends Base<Integer> {
            }

            class Sub extends Mid {
                @Override
                protected String process(Integer value) { // bridge process(Number)
                    'sub->' + super.process(value) // invoke super$2$process(Number)
                }
            }

            assert new Sub().process(42) == 'sub->base(42,null)'
        '''
    }

    @Test // more than one class between the declaring class and the override
    void testSuperCallToBridgedMethod2() {
        assertScript '''
            abstract class Base<T extends Number> {
                protected String process(T value, String extra = null) {
                    "base($value,$extra)"
                }
            }

            abstract class Mid extends Base<Integer> {
            }

            abstract class Mid2 extends Mid {
            }

            class Sub extends Mid2 {
                @Override
                protected String process(Integer value) { // bridge process(Number)
                    'sub->' + super.process(value) // invoke super$3$process(Number)
                }
            }

            assert new Sub().process(42) == 'sub->base(42,null)'
        '''
    }

    @Test // covariant return in place of generics as the source of the bridge method
    void testSuperCallToBridgedMethod3() {
        assertScript '''
            abstract class Base {
                protected Object process(Number value, String extra = null) {
                    "base($value,$extra)"
                }
            }

            abstract class Mid extends Base {
            }

            class Sub extends Mid {
                @Override
                protected String process(Number value) { // bridge Object process(Number)
                    'sub->' + super.process(value)
                }
            }

            assert new Sub().process(42) == 'sub->base(42,null)'
        '''
    }

    @Test // GROOVY-6663: a bridge must not resolve to a MOP method of a super class
    void testSuperCallDoesNotSkipLevel() {
        assertScript '''
            class A<T> {
                protected String getText(T t) { 'A with ' + t }
            }

            class B extends A<String> {
                @Override
                protected String getText(String s) { 'B then ' + super.getText(s) }
                // bridge String getText(Object o) { this.getText((String) o); }
                //        String super$2$getText(Object o) { super.getText(o); }
            }

            class C extends B {
                @Override
                protected String getText(String s) { 'C then ' + super.getText(s) }
            }

            class D extends C {
                @Override
                protected String getText(String s) { 'D then ' + super.getText(s) }
            }

            assert new D().getText(null) == 'D then C then B then A with null'
        '''
    }

    @Test // no super method for the given arguments
    void testSuperCallWithoutMatch() {
        def err = shouldFail MissingMethodException, '''
            class A {
                def foo(String s, String t) { 'a' }
            }

            class B extends A {
                @Override
                def foo(String s, String t) { 'b' }
                def bar() { super.foo('x','y','z') }
            }

            new B().bar()
        '''
        assert err.message.contains('No signature of method: foo for class: A')
    }
}
