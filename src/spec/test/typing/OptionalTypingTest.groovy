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
package typing

import groovy.test.GroovyTestCase

class OptionalTypingTest extends GroovyTestCase {

    void testOptionalTypingInVariableDeclaration() {
        assertScript '''
            // tag::optionaltyping_var[]
            String aString = 'foo'                      // <1>
            assert aString.toUpperCase()                // <2>
            // end::optionaltyping_var[]
        '''
        assertScript '''
            // tag::optionaltyping_var_def[]
            def aString = 'foo'                         // <1>
            assert aString.toUpperCase()                // <2>
            // end::optionaltyping_var_def[]
        '''
    }
    void testOptionalTypingInMethodParameter() {
        assertScript '''
            // tag::optionaltyping_orig[]
            String concat(String a, String b) {
                a+b
            }
            assert concat('foo','bar') == 'foobar'
            // end::optionaltyping_orig[]
        '''
        assertScript '''
            // tag::optionaltyping_def[]
            def concat(def a, def b) {                              // <1>
                a+b
            }
            assert concat('foo','bar') == 'foobar'                  // <2>
            assert concat(1,2) == 3                                 // <3>
            // end::optionaltyping_def[]
        '''
        assertScript '''
            // tag::optionaltyping_notype[]
            private concat(a,b) {                                   // <1>
                a+b
            }
            assert concat('foo','bar') == 'foobar'                  // <2>
            assert concat(1,2) == 3                                 // <3>
            // end::optionaltyping_notype[]
        '''
    }
}
