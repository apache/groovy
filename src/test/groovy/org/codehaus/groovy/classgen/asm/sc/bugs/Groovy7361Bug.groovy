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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

final class Groovy7361Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testShouldNotThrowVerifyError() {
        assertScript '''
            class A {
                private final Map<Long, String> map = [1L:'x', 2L:'y']
                def m() {
                    def list = [1L]
                    list.each {
                        synchronized (map) {
                            map.remove(it)
                        }
                    }
                    map
                }
            }
            assert new A().m() == [2L:'y']
        '''
    }

    void testShouldNotThrowClassCastException() {
        assertScript '''
            class A {
                private final Map map = [:]
                def m() {
                    new Runnable() {
                        @Override
                        void run() {
                            { -> map['x'] = 'y' }.call()
                        }
                    }.run()
                    map
                }
            }
            assert new A().m() == [x:'y']
        '''
    }

    // GROOVY-9699
    void testShouldNotEmitErrorForSubscriptPrivateAccess() {
        assertScript '''
            class A {
                private static final java.util.regex.Pattern PATTERN = ~/.*/
                void checkList() {
                    def list = []
                    def closure = { ->
                        list << PATTERN.pattern()
                    }
                    closure()
                }
                void checkMap() {
                    def map = [:]
                    def closure = { ->
                        map[PATTERN.pattern()] = 1
                    }
                    closure()
                }
            }
            class B extends A {
            }
            new A().checkList()
            new B().checkList()
            new A().checkMap()
            new B().checkMap()
        '''
    }

    // GROOVY-9771
    void testShouldNotThrowClassCastExceptionForSubscriptPrivateAccess() {
        assertScript '''
            class C {
                private final Map<String, Boolean> map = [:]
                void checkMap() {
                    { ->
                        map['key'] = true
                    }.call()
                    assert map == [key: true]
                }
            }
            new C().checkMap()
        '''
    }
}
