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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy9779 {
    @Test
    void testCallOperatorOnDynamicProperties1() {
        assertScript '''
            class A {
                def call() { return 42 }
            }
            class C {
                static final x = new A()
                       final y = new A()
            }
            assert C.x() == 42
            assert new C().x() == 42
            assert new C().y() == 42
        '''
    }

    @Test
    void testCallOperatorOnDynamicProperties2() {
        assertScript '''
            class A {
                def call = { -> return 42 }
            }
            class C {
                static final x = new A()
                       final y = new A()
            }
            assert C.x() == 42
            assert new C().x() == 42
            assert new C().y() == 42
        '''
    }

    @Test // don't chain call properties together
    void testCallOperatorOnDynamicProperties3() {
        def err = shouldFail '''
            class A {
                def call = { -> return 42 }
            }
            class B {
                def call = new A()
            }
            class C {
                static final x = new B()
                       final y = new B()
            }
            C.x()
        '''
        assert err.message.contains('No signature of method: B.call() is applicable')
    }

    @Test // don't chain call properties together
    void testCallOperatorOnDynamicProperties4() {
        def err = shouldFail '''
            class A {
                def call(x) { assert x == 1; return 42 }
            }
            class B {
                def call = new A()
            }
            class C {
                def plus = new B()
            }
            assert new C() + 1 == 42
        '''
        assert err.message.contains('No signature of method: C.plus() is applicable')
    }

    @Test
    void testOperatorOverloadViaCallable() {
        assertScript '''
            class A {
                def call(x) { return x + 1 }
            }
            class C {
                def plus = new A()
            }
            assert new C() + 1 == 2
        '''
    }

    @Test
    void testOperatorOverloadViaClosure() {
        assertScript '''
            class C {
                def plus = { x -> x + 1 }
            }
            assert new C() + 1 == 2
        '''
    }
}
