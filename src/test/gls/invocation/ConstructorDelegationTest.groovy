/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gls.invocation

import gls.CompilableTestSupport

public class ConstructorDelegationTest extends CompilableTestSupport {

    public void testThisCallWithParameter() {
        assertScript """
            class A {
                def foo
                A(String x){foo=x}
                A(){this("bar")}
            }
            def a = new A()
            assert a.foo == "bar"
        """
    }

    public void testThisCallWithoutParameter() {
        assertScript """
            class A {
                def foo
                A(String x){this(); foo=x}
                A(){foo="bar"}
            }
            def a = new A("foo")
            assert a.foo == "foo"
        """
    }

    public void testThisConstructorCallNotOnFirstStmt() {
        shouldNotCompile """
            class ThisConstructorCall {
                public ThisConstructorCall() {
                    println 'dummy first statement'
                    this(19)
                }
                public ThisConstructorCall(int b) {
                    println 'another dummy statement'
                }
            }
            1
        """
    }

    public void testSuperConstructorCallNotOnFirstStmt() {
        shouldNotCompile """
            class SuperConstructorCall {
                public SuperConstructorCall() {
                    println 'dummy first statement'
                    super()
                }
            }
            1
        """
    }

    public void testConstructorDelegationWithThisOrSuperInArgs() {
        def scriptStr
        // all 4 cases below were compiling earlier but giving VerifyError at runtime
        scriptStr = """
            class MyClosure3128V1 extends Closure {
                MyClosure3128V1() {
                    super(this)
                }
                void run() { println 'running' }
            }
        """
        shouldNotCompile(scriptStr)

        scriptStr = """
            class MyClosure3128V2 extends Closure {
                MyClosure3128V2() {
                    super(super)
                }
                void run() { println 'running' }
            }
        """
        shouldNotCompile(scriptStr)

        scriptStr = """
            class MyClosure3128V3 extends Closure {
                MyClosure3128V3() {
                    this(this)
                }
                MyClosure3128V3(owner) {}
                void run() { println 'running' }
            }
        """
        shouldNotCompile(scriptStr)

        scriptStr = """
            class MyClosure3128V4 extends Closure {
                MyClosure3128V4() {
                    this(super)
                }
                MyClosure3128V4(owner) {}
                void run() { println 'running' }
            }
        """
        shouldNotCompile(scriptStr)
    }
}