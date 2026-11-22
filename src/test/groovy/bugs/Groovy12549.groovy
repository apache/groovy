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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-12549: an unqualified {@code setProperty} call inside a
 * {@code tap} closure (DELEGATE_FIRST) must resolve to the delegate's
 * exact-match method, not as an ambiguity on {@code Closure}'s own
 * {@code setProperty} overloads.
 */
final class Groovy12549 {

    @ParameterizedTest
    @ValueSource(strings = ['TypeChecked', 'CompileStatic'])
    void testSetPropertyInTapResolvesToDelegate(String mode) {
        assertScript """
            @groovy.transform.$mode
            class TapProbe {
                static Conf build() { new Conf().tap { setProperty('a', 'b') } }
            }
            class Conf {
                Map<String, Object> props = [:]
                void setProperty(String k, Object v) { props[k] = v }
            }
            assert TapProbe.build().props == [a: 'b']
        """
    }
}
