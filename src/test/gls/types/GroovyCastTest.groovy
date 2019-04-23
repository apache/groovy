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
package gls.types

import gls.CompilableTestSupport

class GroovyCastTest extends CompilableTestSupport {
    void testSAMVariable() {
        assertScript """
            interface SAM { def foo(); }

            SAM s = {1}
            assert s.foo() == 1
            def t = (SAM) {2}
            assert t.foo() == 2
        """
    }
    
    void testSAMProperty() {
        assertScript """
            interface SAM { def foo(); }
            class X {
                SAM s
            }
            def x = new X(s:{1})
            assert x.s.foo() == 1
        """
    }
    
    void testSAMAttribute() {
        assertScript """
            interface SAM { def foo(); }
            class X {
                public SAM s
            }
            def x = new X()
            x.s = {1}
            assert x.s.foo() == 1
            x = new X()
            x.@s = {2}
            assert x.s.foo() == 2
        """
    }
    
    void testSAMType() {
        assertScript """
            interface Foo {int foo()}
            Foo f = {1}
            assert f.foo() == 1
            abstract class Bar implements Foo {}
            Bar b = {2}
            assert b.foo() == 2
        """
        assertScript """
            interface Foo2 {
                String toString()
            }
            try {
                Foo2 f2 = {int i->"hi"}
                assert false
            } catch (ClassCastException ce) {
                assert true
            }
            abstract class Bar2 implements Foo2 {}
            try {
                Bar2 b2 = {"there"}
                assert false
            } catch (ClassCastException ce) {
                assert true
            }
        """
        assertScript """
            interface Foo3 {
                boolean equals(Object)
                int f()
            }
            Foo3 f3 = {1}
            assert f3.f() == 1
            abstract class Bar3 implements Foo3 {
                int f(){2}
            }
            try {
                Bar3 b3 = {2}
                assert false
            } catch (ClassCastException ce) {
                assert true
            }
        """
    }

    void testClosureShouldNotBeCoercedToRunnable() {
        assertScript '''
Class foo(Runnable r) {
    // please do not remove wrapping inside a closure
    // because that's precisely what this test is supposed to check!
    { -> bar(r) }()
}

Class bar(Runnable r) {
   r.class
}

assert Closure.isAssignableFrom(foo { 'Hello' })
'''
    }
}