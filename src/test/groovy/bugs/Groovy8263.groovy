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
 * GROOVY-8263: a static import alias that itself looks like a getter
 * ({@code getAbc}) must not introduce a synthetic {@code abc} property
 * that resolves to the original method.
 */
final class Groovy8263 {

    @Test
    void testAliasDoesNotIntroduceAbcProperty() {
        def err = shouldFail(MissingPropertyException) {
            assertScript '''
                import static bugs.Groovy8263Bar.some as getAbc
                abc
            '''
        }
        assert err.message.contains('abc')
    }

    @Test
    void testGetterAliasStillResolves() {
        assertScript '''
            import static bugs.Groovy8263Bar.some as getAbc
            assert getAbc == 42
            assert getGetAbc() == 42
        '''
    }
}

class Groovy8263Bar {
    static getSome() { 42 }
}
