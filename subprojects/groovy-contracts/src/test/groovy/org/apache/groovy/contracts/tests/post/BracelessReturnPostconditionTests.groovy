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
import org.apache.groovy.contracts.PreconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

/**
 * GROOVY-12079: a postcondition must also be checked for a {@code return} that is the single
 * statement of a braceless {@code if}/{@code else} or loop branch. Such a return is held directly
 * by the branch rather than inside a block, so the rewriter used to skip it. The gap is normally
 * masked in recursive methods by a sibling {@code return <recursiveCall>} that is a block member,
 * but {@code @TailRecursive} converts that call into a {@code continue}, leaving only the braceless
 * branch return.
 */
class BracelessReturnPostconditionTests extends BaseTestClass {

    @Test
    void braceless_if_return_is_checked() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result == 2 })
            def m(boolean b) {
                if (b) return 1   // braceless branch return
                return 2
            }
        }
        '''

        def a = create_instance_of(source)
        assert a.m(false) == 2
        shouldFail(PostconditionViolation) {
            a.m(true)
        }
    }

    @Test
    void tail_recursive_valid_postcondition_passes() {
        def source = '''
        import groovy.contracts.*

        class Factorial {
            @groovy.transform.TailRecursive
            @Requires({ n >= 0 && acc >= 1 })
            @Ensures({ result >= acc })
            static long factHelper(long n, long acc) {
                if (n <= 1) return acc
                long next = n * acc
                return factHelper(n - 1, next)
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.factHelper(0, 1) == 1
        assert clazz.factHelper(3, 1) == 6
        assert clazz.factHelper(5, 1) == 120
    }

    @Test
    void tail_recursive_postcondition_violation_is_detected() {
        def source = '''
        import groovy.contracts.*

        class Factorial {
            @groovy.transform.TailRecursive
            @Requires({ n >= 0 && acc >= 1 })
            @Ensures({ result < 0 })   // never true: the accumulator is always positive
            static long factHelper(long n, long acc) {
                if (n <= 1) return acc
                long next = n * acc
                return factHelper(n - 1, next)
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        shouldFail(PostconditionViolation) {
            clazz.factHelper(3, 1)
        }
    }

    @Test
    void tail_recursive_precondition_still_enforced() {
        def source = '''
        import groovy.contracts.*

        class Factorial {
            @groovy.transform.TailRecursive
            @Requires({ n >= 0 && acc >= 1 })
            @Ensures({ result >= acc })
            static long factHelper(long n, long acc) {
                if (n <= 1) return acc
                long next = n * acc
                return factHelper(n - 1, next)
            }
        }
        '''

        def clazz = add_class_to_classpath(source)
        shouldFail(PreconditionViolation) {
            clazz.factHelper(-1, 1)
        }
    }
}
