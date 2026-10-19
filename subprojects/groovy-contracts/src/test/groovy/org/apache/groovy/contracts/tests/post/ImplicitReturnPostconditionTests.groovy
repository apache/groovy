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

import org.apache.groovy.contracts.ClassInvariantViolation
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

    @Test
    void trailing_implicit_return_after_early_return_is_checked() {
        // GROOVY-12463: an explicit early return used to stop the implicit-return conversion,
        // so the trailing value was returned without the postcondition being checked
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result > 0 })
            static int f(boolean early) {
                if (early) return 1
                -1
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f(true) == 1
        shouldFail(PostconditionViolation) {
            clazz.f(false)
        }
    }

    @Test
    void trailing_implicit_return_after_early_return_value_not_corrupted() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ true })
            static int f(boolean early) {
                if (early) return 1
                2
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f(true) == 1
        assert clazz.f(false) == 2
    }

    @Test
    void trailing_implicit_if_else_after_early_return_is_checked() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result >= 0 })
            static int g(int a, int b) {
                if (a == 0) return 0
                if (a > b) a else b
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.g(0, -5) == 0
        assert clazz.g(3, 1) == 3
        shouldFail(PostconditionViolation) {
            clazz.g(-3, -1)
        }
    }

    @Test
    void trailing_implicit_return_after_early_return_compile_static() {
        def source = '''
        import groovy.contracts.*
        import groovy.transform.CompileStatic

        @CompileStatic
        class A {
            @Ensures({ !result.present || result.get() <= 28 })
            static Optional<Integer> day(String s) {
                if (!s.isInteger()) return Optional.<Integer>empty()
                Optional.of(s.toInteger())
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert !clazz.day('x').present
        assert clazz.day('12').get() == 12
        shouldFail(PostconditionViolation) {
            clazz.day('31')
        }
    }

    @Test
    void trailing_implicit_return_after_early_return_checks_postcondition_and_invariant() {
        // with both contracts present the implicit-return conversion also runs on the method
        // after postcondition weaving; each contract must still be checked on the trailing path
        def source = '''
        import groovy.contracts.*

        @Invariant({ count >= 0 })
        class A {
            int count = 0

            @Ensures({ result < 100 })
            int m(int x) {
                if (x == 0) return 1
                count = x
                x
            }
        }
        '''

        def a = create_instance_of(source)
        assert a.m(0) == 1
        assert a.m(5) == 5
        shouldFail(ClassInvariantViolation) {
            a.m(-3)
        }
        shouldFail(PostconditionViolation) {
            a.m(200)
        }
    }
}
