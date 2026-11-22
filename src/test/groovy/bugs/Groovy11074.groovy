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

/**
 * GROOVY-11074: STC must type {@code m(x, 1)} as {@code List<Number>}
 * when {@code x} is {@code Number} and {@code m} is {@code <T> List<T>
 * m(T, T)} (historically the result was {@code List<? extends Number>}).
 */
final class Groovy11074 {

    @Test
    void testParameterizedMethodResultIsListOfNumberNotWildcard() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Main {
                static List<Number> test() {
                    Number x = 1
                    List<Number> y = m(x, 1)
                    y
                }
                static <T> List<T> m(T x, T y) { [x, y] }
            }
            assert Main.test() == [1, 1]
        '''
    }
}
