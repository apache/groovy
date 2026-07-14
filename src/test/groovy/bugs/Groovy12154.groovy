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

final class Groovy12154 {

    // A lambda with a primitive-typed parameter targeting a generic functional interface used to
    // compile cleanly but throw at runtime (LambdaConversionException: int is not a subtype of
    // class java.lang.Object), because the implementation method kept the primitive parameter type
    // while LambdaMetafactory links against the erased (reference) SAM signature.

    @Test
    void testPrimitiveParamTargetingGenericSam() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class T {
                Function<Integer, Integer> f = (int a) -> a * 2
                Integer m() { f.apply(5) }
            }
            assert new T().m() == 10
        '''
    }

    @Test
    void testMultiplePrimitiveParamsTargetingGenericSam() {
        assertScript '''
            import java.util.function.BiFunction
            @groovy.transform.CompileStatic
            class T {
                BiFunction<Integer, Integer, Integer> f = (int a, int b) -> a + b
                List m() { [f.apply(1, 2), f.apply(5, 5)] }
            }
            assert new T().m() == [3, 10]
        '''
    }

    @Test
    void testPrimitiveParamWithDefaultTargetingGenericSam() {
        assertScript '''
            import java.util.function.BiFunction
            @groovy.transform.CompileStatic
            class T {
                BiFunction<Integer, Integer, Integer> f = (int a, int b = 10) -> a + b
                List m() { [f.apply(1, 2), f.apply(5, 5)] }
            }
            assert new T().m() == [3, 10]
        '''
    }

    @Test
    void testLongPrimitiveParamTargetingGenericSam() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class T {
                Function<Long, Long> f = (long a) -> a * 2L
                Long m() { f.apply(5L) }
            }
            assert new T().m() == 10L
        '''
    }

    // Regressions: a primitive-parameter functional interface must still use a primitive impl param.

    @Test
    void testPrimitiveParamTargetingPrimitiveSam() {
        assertScript '''
            import java.util.function.IntUnaryOperator
            @groovy.transform.CompileStatic
            class T {
                IntUnaryOperator f = (int a) -> a * 2
                int m() { f.applyAsInt(5) }
            }
            assert new T().m() == 10
        '''
    }

    @Test
    void testUntypedAndBoxedParamsUnaffected() {
        assertScript '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class T {
                Function<Integer, Integer> u = (a) -> a * 2
                Function<Integer, Integer> b = (Integer a) -> a * 2
                List m() { [u.apply(5), b.apply(5)] }
            }
            assert new T().m() == [10, 10]
        '''
    }
}
