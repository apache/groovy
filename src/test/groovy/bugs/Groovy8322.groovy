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
 * GROOVY-8322: {@code Closure.call(Object[])} under {@code @CompileStatic}
 * must statically dispatch the array overload, not {@code call(Object)}
 * which would wrap the array as a single argument.
 */
final class Groovy8322 {

    @Test
    void testStaticDispatchOfCallObjectArray() {
        assertScript '''
            @groovy.transform.CompileStatic
            class A {
                Closure x
                def call(...args) {
                    x.call((Object[]) args)
                }
            }
            def seen = []
            def a = new A(x: { ...args -> seen << args[0] })
            a(1, 2, 3)
            assert seen == [1]
        '''
    }

    @Test
    void testDynamicDispatchMatchesStatic() {
        assertScript '''
            class A {
                Closure x
                def call(...args) {
                    x.call((Object[]) args)
                }
            }
            def seen = []
            def a = new A(x: { ...args -> seen << args[0] })
            a(1, 2, 3)
            assert seen == [1]
        '''
    }
}
