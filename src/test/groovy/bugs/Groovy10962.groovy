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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy10962 {

    @Test
    void testMixedProperty1() {
        assertScript '''
            class C {
                static getP() { 'get' }
                boolean isP() { false }
            }
            assert C.p == 'get'
            assert new C().p == false
        '''
    }

    @Test
    void testMixedProperty2() {
        assertScript '''
            class C {
                static boolean isP() { false }
                String getP() { 'get' }
            }
            assert C.p == false
            assert new C().p == false
        '''
    }

    @Test
    void testMixedProperty3() {
        assertScript '''
            class C {
                public static p = 'boo'
                String getP() { 'get' }
                boolean isP() { false }
            }
            assert C.p == 'boo'
            assert new C().p == false
        '''
    }

    @Test
    void testMixedProperty4() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail
            class C {
                static void setP(p) { }
                boolean isP() { false }
                String getP() { 'get' }
            }
            C.p = 'set'
            shouldFail(MissingPropertyException) {
                def x = C.p
            }
            assert new C().p == false
        '''
    }
}
