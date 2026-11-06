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

/**
 * Static final fields whose initializers refer back to each other are not
 * compile-time constants; they are initialized at runtime in declaration order.
 */
final class Groovy12710 {

    @Test
    void testUnqualifiedCycle() {
        assertScript '''
            class Cycle {
                static final String R = S
                static final String S = R
            }
            assert Cycle.R == null && Cycle.S == null
        '''
    }

    @Test
    void testQualifiedCycle() {
        assertScript '''
            class Cycle {
                static final String R = Cycle.S
                static final String S = Cycle.R
            }
            assert Cycle.R == null && Cycle.S == null
        '''
    }

    @Test
    void testCycleAcrossClasses() {
        assertScript '''
            class A { static final String X = B.Y }
            class B { static final String Y = A.X }
            assert A.X == null && B.Y == null
        '''
        assertScript '''
            interface I { String R = J.S }
            interface J { String S = I.R }
            assert I.R == null && J.S == null
        '''
    }

    @Test
    void testSelfReference() {
        assertScript '''
            class Cycle { static final String R = R }
            assert Cycle.R == null
        '''
        assertScript '''
            class Cycle { static final String R = Cycle.R }
            assert Cycle.R == null
        '''
    }

    @Test
    void testCycleThroughArithmetic() {
        assertScript '''
            class Cycle {
                static final int A = Cycle.B + 1
                static final int B = Cycle.A + 1
            }
            assert Cycle.A == 1 && Cycle.B == 2
        '''
    }

    @Test
    void testCycleWithStaticCompilation() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Cycle {
                static final String R = Cycle.S
                static final String S = Cycle.R
                static String r() { R }
            }
            assert Cycle.r() == null
        '''
    }

    @Test
    void testCyclicFieldAsAnnotationAttribute() {
        def err = shouldFail '''
            import java.lang.annotation.*
            @Retention(RetentionPolicy.RUNTIME) @interface Ann { String value() }
            class Cycle {
                static final String R = Cycle.S
                static final String S = Cycle.R
            }
            @Ann(Cycle.R) class User {}
        '''
        assert !(err instanceof StackOverflowError)
        assert err.message.contains("Expected 'Cycle.R' to be an inline constant of type java.lang.String")
    }

    @Test
    void testFieldsWithoutCycleAreStillInlined() {
        assertScript '''
            import java.lang.annotation.*
            @Retention(RetentionPolicy.RUNTIME) @interface Ann { String value() }
            @Retention(RetentionPolicy.RUNTIME) @interface Num { int value() }
            class Holder {
                static final String R = S
                static final String S = 'x'
                static final int A = 1
                static final int B = A + A
            }
            @Ann(Holder.R) @Num(Holder.B) class User {}
            assert User.getAnnotation(Ann).value() == 'x'
            assert User.getAnnotation(Num).value() == 2
        '''
    }
}
