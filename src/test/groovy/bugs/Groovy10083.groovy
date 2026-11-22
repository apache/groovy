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
 * GROOVY-10083: {@code ComposedClosure#setResolveStrategy} must not throw
 * {@code ClassCastException} when the owner is not a {@code Closure}
 * (historically {@code rehydrate(1, 1, 1)} then {@code setResolveStrategy}
 * failed with that exception).
 */
final class Groovy10083 {

    @Test
    void testSetResolveStrategyAfterRehydrateToNonClosureOwner() {
        assertScript '''
            def seen = []
            def a = { seen << 'aa' }
            def b = { seen << 'bb' } >> a
            def d = b.rehydrate(1, 1, 1)
            d.setResolveStrategy(Closure.DELEGATE_FIRST)
            d()
            assert seen == ['bb', 'aa']
        '''
    }
}
