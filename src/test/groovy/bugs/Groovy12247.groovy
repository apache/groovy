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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy12247 {

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
        assert err.message.contains('No signature of method: A.foo()')
    }

    @Test // super call to short form of method with default parameter
    void testSuperCallToBridgedMethod() {
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
}
