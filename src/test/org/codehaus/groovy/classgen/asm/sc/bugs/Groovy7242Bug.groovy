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

class Groovy7242Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testCallMethodOfTraitInsideClosure() {
        assertScript '''
            trait MyTrait {
                def f() {
                    ['a'].collect {String it -> f2(it)}
                }

                def f2(String s) {
                    s.toUpperCase()
                }
            }

            class A implements MyTrait {}
            def a = new A()
            assert a.f() == ['A']
        '''
    }

    void testCallMethodOfTraitInsideClosureAndClosureParamTypeInference() {
        assertScript '''
            trait MyTrait {
                def f() {
                    ['a'].collect {f2(it)}
                }

                def f2(String s) {
                    s.toUpperCase()
                }
            }

            class A implements MyTrait {}
            def a = new A()
            assert a.f() == ['A']
        '''
    }

    void testAccessTraitPropertyFromClosureInTrait() {
        assertScript '''
            trait MyTrait {
                int x
                def f() {
                    [1].each { x = it }
                }
            }
            class A implements MyTrait {}
            def a = new A()
            a.f()
            assert a.x == 1
        '''
    }

    void testCallPrivateMethodOfTraitInsideClosure() {
        assertScript '''
            trait MyTrait {
                def f() {
                    ['a'].collect {String it -> f2(it)}
                }

                private f2(String s) {
                    s.toUpperCase()
                }
            }

            class A implements MyTrait {}
            def a = new A()
            assert a.f() == ['A']
        '''
    }
}
