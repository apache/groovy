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
 * GROOVY-12130: constants the compiler caches in per-class synthetic {@code $const$} fields
 * (BigDecimal, BigInteger, {@code Long} other than 0/1 — but not int/double) were misread in
 * inline-mode postconditions. The contract closure class and the inline assertion block were
 * built from the same expression nodes, and {@code OptimizerVisitor} <em>mutates</em> each
 * {@code ConstantExpression} with a per-class field name — so the class optimized last renamed
 * the shared spec constants and the woven method read the wrong {@code $const$} field
 * (e.g. {@code result &lt; 0.0} compiled to {@code result &lt; -2.5}).
 */
class ConstantCachingPostconditionTests extends BaseTestClass {

    @Test
    void bigdecimal_spec_constant_read_correctly() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result < 0.0 })
            static BigDecimal f() { return -2.5 }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f() == -2.5G
    }

    @Test
    void bigdecimal_arithmetic_spec_constant() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result == 0.3 })
            static BigDecimal f() { return 0.1 + 0.2 }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f() == 0.3G
    }

    @Test
    void long_spec_constant_read_correctly() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result < 7L })
            static long f() { return 5L }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f() == 5L
    }

    @Test
    void biginteger_spec_constant_read_correctly() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result < 7G })
            static BigInteger f() { return 5G }
        }
        '''

        def clazz = add_class_to_classpath(source)
        assert clazz.f() == 5G
    }

    @Test
    void bigdecimal_violation_still_detected() {
        def source = '''
        import groovy.contracts.*

        class A {
            @Ensures({ result < 0.0 })
            static BigDecimal f() { return 2.5 }
        }
        '''

        def clazz = add_class_to_classpath(source)
        shouldFail(PostconditionViolation) {
            clazz.f()
        }
    }
}
