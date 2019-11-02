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

package groovy.transform.stc

/**
 * Unit tests for static type checking : with method.
 */
class WithSTCTest extends StaticTypeCheckingTestCase {

    // GROOVY-5907
    void testGenericReturnType() {
        assertScript '''
            class Test {
              static List<String> a( String s ) {
                s.with { String it -> [ "$it".toString() ] }
              }
            }

            assert Test.a( 'tim' ) == [ 'tim' ]
        '''
    }

    void testStringReturnType() {
        assertScript '''
            class Test {
              static String a( String s ) {
                s.with { String it -> it.toLowerCase() }
              }
            }

            assert Test.a( 'TIM' ) == 'tim'
        '''
    }

    void testIntReturnType() {
        assertScript '''
            class Test {
               static int a(String s) {
                    s.toCharArray().with {
                        length
                    }
                }
            }

            assert Test.a( 'Daniel' ) == 6 
        '''
    }

    void testLongReturnType() {
        assertScript '''
            class Test {
               static long a() {
                    Long.with {
                        MAX_VALUE
                    }
                }
            }

            assert Test.a() == Long.MAX_VALUE
        '''
    }
}

