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

final class Groovy11829 {

    @Test
    void testCustomGetter() {
        assertScript '''
            class C {
                def get(String name) {
                    if (name == 'x') return 'yz'
                }
            }

            def c = new C()
            assert c.x == 'yz'
            assert c.y == null
        '''
    }

    @Test
    void testCustomSetter() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail
            class C {
                def set(String name, Object value) {
                    if (name != 'x') throw PropertyMissingException(name, C)
                }
            }

            def c = new C()
            c.x = 'xyz'
            shouldFail {
                c.y = null
            }
        '''
    }

    @Test
    void testCustomSetter2() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail
            class C {
                def set(Object name, Object value) {
                    throw AssertionError()
                }
                def set(String name, Object value) {
                    if (name != 'x') throw PropertyMissingException(name, C)
                }
            }

            def c = new C()
            c.x = 'xyz'
            shouldFail {
                c.y = null
            }
        '''
    }
}
