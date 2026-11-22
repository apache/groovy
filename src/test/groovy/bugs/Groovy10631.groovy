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
 * GROOVY-10631: {@code <T> T m(T, T)} applied to {@code A<Boolean>} and
 * {@code A<String>} and assigned to {@code A<? extends Object>} used to
 * be rejected even though {@code T = A<? extends Object>} is valid.
 */
final class Groovy10631 {

    @Test
    void testSharedTypeParameterInferredAsWildcard() {
        assertScript '''
            import groovy.transform.TypeChecked

            class A<T> {}

            @TypeChecked
            class Test {
                static <T> T m(T x, T y) { return null }

                static void test() {
                    A<? extends Object> x = m(new A<Boolean>(), new A<String>())
                }
            }

            Test.test()
        '''
        assertScript '''
            import groovy.transform.CompileStatic

            class A<T> {}

            @CompileStatic
            class Test {
                static <T> T m(T x, T y) { return null }

                static void test() {
                    A<? extends Object> x = m(new A<Boolean>(), new A<String>())
                }
            }

            Test.test()
        '''
    }
}
