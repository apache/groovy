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
 * GROOVY-8845: {@code @DelegatesTo} on a varargs {@code Closure...}
 * parameter must apply to every argument, not only the first.
 */
final class Groovy8845 {

    @Test
    void testDelegatesToAppliesToEveryVarargClosure() {
        assertScript '''
            def md(@DelegatesTo.Target Object target, @DelegatesTo(strategy = Closure.DELEGATE_FIRST) Closure... arg) {
                for (Closure a : arg) {
                    a.delegate = target
                    a.resolveStrategy = Closure.DELEGATE_FIRST
                    a()
                }
            }

            class D {
                def foo() { 42 }
            }

            @groovy.transform.CompileStatic
            def test() {
                def out = []
                md(
                    new D(),
                    { out << foo() },
                    { out << foo() },
                    { out << foo() },
                )
                out
            }

            assert test() == [42, 42, 42]
        '''
    }
}
