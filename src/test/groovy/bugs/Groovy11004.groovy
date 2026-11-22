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
 * GROOVY-11004: a diamond {@code new TreeMap<>(x)} passed to
 * {@code Collections.unmodifiableNavigableMap} must keep the key/value
 * type arguments of {@code x} under STC (historically the result was
 * typed as {@code NavigableMap<Object, Object>}).
 */
final class Groovy11004 {

    @Test
    void testDiamondTreeMapPassedToUnmodifiableNavigableMapKeepsTypeArguments() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Main {
                static Map<Number, String> test(Map<Number, String> x) {
                    Map<Number, String> y = Collections.unmodifiableNavigableMap(new TreeMap<>(x))
                    y
                }
            }
            def y = Main.test(new TreeMap<Number, String>())
            assert y instanceof java.util.NavigableMap
            assert y.isEmpty()
        '''
    }
}
