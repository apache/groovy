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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy8446 {

    @Test
    void testVoidArray0() {
        assertScript '''
            class C {
                Void[] m() {}
            }
            println 'parsed'
        '''
    }

    @Test
    void testVoidArray1() {
        def err = shouldFail '''
            class C {
                void[] m() {}
            }
        '''
        assert err =~ /void\[\] is an invalid type|Unexpected input: '\('/
    }

    @Test
    void testVoidArray2() {
        def err = shouldFail '''
            class C {
                def meth(void... args) {}
            }
        '''
        assert err =~ /void\[\] is an invalid type|void is not allowed here/
    }
}
