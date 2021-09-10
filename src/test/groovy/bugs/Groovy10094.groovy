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

import static groovy.test.GroovyAssert.shouldFail

final class Groovy10094 {

    @Test
    void testMethodDefaultArgumentSTC() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            class C {
                int m(int i = 'error') {
                    return i
                }
            }
        '''

        assert err =~ /Cannot assign value of type java.lang.String to variable of type int/
    }

    @Test
    void testClosureDefaultArgumentSTC() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            class C {
                def c = { int i = 'error' ->
                    return i
                }
            }
        '''

        assert err =~ /Cannot assign value of type java.lang.String to variable of type int/
    }

    @Test
    void testConstructorDefaultArgumentSTC() {
        def err = shouldFail '''
            @groovy.transform.TypeChecked
            class C {
                C(int i = 'error') {
                }
            }
        '''

        assert err =~ /Cannot assign value of type java.lang.String to variable of type int/
    }
}
