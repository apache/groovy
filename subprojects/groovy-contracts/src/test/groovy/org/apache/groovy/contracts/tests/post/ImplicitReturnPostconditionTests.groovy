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
package org.apache.groovy.contracts.tests.post

import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12129: a method whose value is produced by an implicit return that is not a plain
 * trailing expression statement — an {@code if}/{@code else} whose branches are expressions, a
 * trailing {@code try}/{@code finally}, or a body already wrapped by a method-level
 * {@code @Decreases} — used to have {@code result} bound to a synthesized default value, and,
 * worse, the synthesized {@code return &lt;default&gt;} changed what the woven method returned.
 * Implicit-return conversion is now delegated to the compiler's own {@code ReturnAdder}.
 */
class ImplicitReturnPostconditionTests extends BaseTestClass {

    @Test
    void implicit_if_else_return_checks_actual_value() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result >= a && result >= b })
            static int max(int a, int b) {
                if (a > b) a else b
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.max(1, 0) == 1
        assert clazz.max(0, 1) == 1
        assert clazz.max(5, 5) == 5
    }

    @Test
    void implicit_if_else_return_value_not_corrupted_by_weaving() {
        // a vacuous postcondition never fires, so this pins the woven method's actual return
        // value (previously 0 — the synthesized default — instead of 1)
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ true })
            static int max(int a, int b) {
                if (a > b) { a } else { b }
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.max(1, 0) == 1
    }

    @Test
    void implicit_if_else_violation_still_detected() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result > 10 })
            static int max(int a, int b) {
                if (a > b) a else b
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        shouldFail(PostconditionViolation) {
            clazz.max(1, 0)
        }
    }

    @Test
    void implicit_try_finally_return() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result == 1 })
            static int f() {
                try {
                    1
                } finally {
                    // no cleanup needed
                }
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f() == 1
    }

    @Test
    void implicit_ternary_return_with_method_decreases() {
        // method-level @Decreases wraps the body in variant bookkeeping at SEMANTIC_ANALYSIS,
        // so the trailing ternary sits inside a try/finally by the time the postcondition is
        // woven at INSTRUCTION_SELECTION
        def source = '''
        import groovy.contracts.*

        class A {
            @Requires({ pad != null && s != null && pad.length() == 1 })
            @Ensures({ s.length() >= n ? result == s : result.length() == n })
            @Decreases({ n - s.length() })
            static String leftpad(String pad, int n, String s) {
                s.length() >= n ? s : leftpad(pad, n, pad + s)
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.leftpad('x', 0, '') == ''
        assert clazz.leftpad('x', 5, 'ab') == 'xxxab'
    }
}
