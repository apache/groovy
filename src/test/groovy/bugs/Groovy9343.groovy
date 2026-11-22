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
 * GROOVY-9343: under {@code @CompileStatic}, operator assignment
 * ({@code var += 1}) must update the inferred type the same way
 * {@code var = var + 1} does.
 */
final class Groovy9343 {

    @Test
    void testTypeUpdatedAfterPlusAssignment() {
        assertScript '''
            class A { B plus(x) { new B() } }
            class B { def foo() { 42 } }

            @groovy.transform.CompileStatic
            def cs() {
                def var
                var = new A()
                var = var + 1
                var.foo()
            }

            @groovy.transform.CompileStatic
            def cs2() {
                def var
                var = new A()
                var += 1
                var.foo()
            }

            assert cs() == 42
            assert cs2() == 42
        '''
    }
}
