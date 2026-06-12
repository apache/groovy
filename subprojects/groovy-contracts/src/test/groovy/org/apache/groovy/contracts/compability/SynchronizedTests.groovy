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
package org.apache.groovy.contracts.compability

import groovy.test.GroovyShellTestCase

class SynchronizedTests extends GroovyShellTestCase {

    void test_Synchronized_on_methods() {

        def source = """
            import groovy.contracts.*

            class A {

                @groovy.transform.Synchronized
                @Requires({ a >= 0 })
                def m(int a) { return a}

            }

            def a = new A()
            a.m(12)
        """

        evaluate source
    }

    // GROOVY-12084: @Synchronized wraps the method body in a SynchronizedStatement
    // (at CANONICALIZATION) before contracts run; an @Ensures with no @Requires used
    // to throw ClassCastException because the postcondition generator assumed the body
    // was still a BlockStatement. Also assert the postcondition is actually evaluated
    // (not merely that compilation succeeds) by tripping a violation.
    void test_Synchronized_with_Ensures_but_no_Requires() {

        def source = """
            import groovy.contracts.*
            import org.apache.groovy.contracts.PostconditionViolation
            import static groovy.test.GroovyAssert.shouldFail

            class A {

                @groovy.transform.Synchronized
                @Ensures({ result >= 0 })
                int m(int a) { return a }

            }

            def a = new A()
            assert a.m(12) == 12
            shouldFail(PostconditionViolation) { a.m(-1) }
        """

        evaluate source
    }

    // GROOVY-12084: same wrapping scenario combined with a class invariant; the call
    // satisfies the postcondition but violates the invariant, proving the invariant is
    // genuinely woven into the synchronized method
    void test_Synchronized_with_Ensures_and_Invariant_but_no_Requires() {

        def source = """
            import groovy.contracts.*
            import org.apache.groovy.contracts.ClassInvariantViolation
            import static groovy.test.GroovyAssert.shouldFail

            @Invariant({ count >= 0 })
            class A {

                int count = 0

                @groovy.transform.Synchronized
                @Ensures({ result == a })
                int m(int a) { count = a; return a }

            }

            def a = new A()
            assert a.m(12) == 12
            shouldFail(ClassInvariantViolation) { a.m(-1) }
        """

        evaluate source
    }

    // GROOVY-12084: the inherited (default) postcondition path also reaches the cast;
    // a @Synchronized override of a method whose superclass declares @Ensures used to
    // throw the same ClassCastException
    void test_Synchronized_override_inheriting_Ensures() {

        def source = """
            import groovy.contracts.*
            import org.apache.groovy.contracts.PostconditionViolation
            import static groovy.test.GroovyAssert.shouldFail

            class A {
                @Ensures({ result >= 0 })
                int m(int a) { return a }
            }

            class B extends A {
                @groovy.transform.Synchronized
                @Override
                int m(int a) { return a }
            }

            def b = new B()
            assert b.m(12) == 12
            shouldFail(PostconditionViolation) { b.m(-1) }
        """

        evaluate source
    }
}
