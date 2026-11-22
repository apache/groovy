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
 * GROOVY-8906: dynamic compilation must not select non-public
 * ("privileged") constructors of JDK classes such as
 * {@code BigInteger(long)} or {@code String(char[], boolean)}.
 */
final class Groovy8906 {

    @Test
    void testDynamicBigIntegerZeroUsesPublicConstructor() {
        assertScript '''
            assert new BigInteger("0") == new BigInteger(0)
        '''
    }

    @Test
    void testDynamicStringCharArrayBooleanConstructorIsNotSelected() {
        def err = shouldFail '''
            new String((['G', 'o', 'o', 'd'] as char[]), true)
        '''
        assert err instanceof GroovyRuntimeException
        assert err.message.contains('Could not find matching constructor for: java.lang.String([C, Boolean)')
    }

    @Test
    void testDynamicBigDecimalNonPublicConstructorIsNotSelected() {
        def err = shouldFail '''
            new BigDecimal((BigInteger) null, Long.MIN_VALUE, 10, 10)
        '''
        assert err instanceof GroovyRuntimeException
        assert err.message.contains('Could not find matching constructor for: java.math.BigDecimal(null, Long, Integer, Integer)')
    }
}
