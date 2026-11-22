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
 * GROOVY-11065: STC must not confuse a class type parameter with a
 * method type parameter of the same name when inferring the result of
 * {@code applyWriteLocked} (historically {@code TreeSet#floor} was
 * typed as taking {@code PropertyPermission} instead of {@code X}).
 */
final class Groovy11065 {

    @Test
    void testSameTypeParameterNameDoesNotReplaceTheMethodTypeVariable() {
        assertScript '''
            import java.util.function.Function

            class LockVisitor<X> {
                <T> T applyWriteLocked(Function<X, T> func) { func.apply(null) }
            }

            @groovy.transform.CompileStatic
            class Main {
                static final <X> X test() {
                    Function<PropertyPermission, Collection<? extends X>> func = { PropertyPermission p -> [] }
                    LockVisitor<PropertyPermission> rec = new LockVisitor<>()
                    def x = rec.applyWriteLocked(func)
                    X arg = null
                    TreeSet<X> y = new TreeSet<>(x)
                    y.floor(arg)
                }
            }
            assert Main.test() == null
        '''
    }
}
