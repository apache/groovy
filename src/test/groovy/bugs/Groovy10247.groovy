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
 * GROOVY-10247: under a Turkish default locale, property access for a field
 * named {@code id} used to look up {@code getİd} instead of {@code getId},
 * so {@code @CompileStatic} rejected {@code simpleObject.id}. The locale is
 * restored afterwards.
 */
final class Groovy10247 {

    @Test
    void testCompileStaticIdPropertyAccessUnderTurkishLocale() {
        def original = Locale.default
        try {
            Locale.default = new Locale('tr', 'TR')
            assertScript '''
                @groovy.transform.CompileStatic
                class Holder {
                    static String go() {
                        def simpleObject = new SimpleClass('test')
                        return simpleObject.id
                    }
                }
                class SimpleClass {
                    private final String id
                    SimpleClass(String id) { this.id = id }
                    String getId() { id }
                }
                assert Holder.go() == 'test'
            '''
        } finally {
            Locale.default = original
        }
    }
}
