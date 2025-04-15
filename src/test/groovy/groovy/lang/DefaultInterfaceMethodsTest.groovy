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
package groovy.lang

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class DefaultInterfaceMethodsTest {

    @Test
    void testDefaultMethod() {
        assertScript '''
            interface I {
                default String m() {
                    return 'Hello'
                }
            }

            class C implements I {
            }

            assert new C().m() == 'Hello'
        '''
    }

    @Test
    void testDefaultMethodOverride() {
        assertScript '''
            interface I {
                default String m() {
                    return 'Hello'
                }
            }

            class C implements I {
                @Override String m() {
                    return 'Bon jour'
                }
            }

            assert new C().m() == 'Bon jour'
        '''
    }
}
