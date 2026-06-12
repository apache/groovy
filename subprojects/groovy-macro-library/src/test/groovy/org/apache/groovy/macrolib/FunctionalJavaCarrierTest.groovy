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
package org.apache.groovy.macrolib

import fj.data.Option
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

/**
 * Functional Java participates via the name-keyed allow-list (it uses
 * {@code bind}/{@code map} with {@code fj.F} arguments, so structural and
 * {@code @Monadic} cannot apply). Exercises the registry supertype walk
 * ({@code fj.data.Some} &rarr; {@code fj.data.Option}) and the general
 * closure-to-SAM coercion (closure &rarr; {@code fj.F}).
 */
final class FunctionalJavaCarrierTest {

    @Test
    void composesAndShortCircuits() {
        def sum = DO(a in Option.some(2),
                     b in Option.some(3)) {
            Option.some(a + b)
        }
        assertEquals(5, sum.get())

        def shorted = DO(a in Option.none(),
                         b in Option.some(3)) {
            Option.some(b)
        }
        assertFalse(shorted.defined())
    }

    @Test
    void underCompileStaticViaMonadicChecker() {
        assertScript '''
            import fj.data.Option

            @groovy.transform.CompileStatic(extensions='groovy.typecheckers.MonadicChecker')
            class C {
                static int run() {
                    DO(a in Option.some(2),
                       b in Option.some(3)) {
                        Option.some(a + b)
                    }.get()
                }
            }
            assert C.run() == 5
        '''
    }
}
