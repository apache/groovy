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




package org.codehaus.groovy.transform.traitx

import groovy.test.GroovyTestCase

class Groovy7011Bug extends GroovyTestCase {
    void testTraitWithDefaultArgumentMethod() {
        assertScript '''
            trait HiSupport {
               def sayHi(String msg = "hi") {
                   msg
               }
            }

            class A implements HiSupport { }

            def result = new A().sayHi()
            assert result == 'hi'
        '''
    }

    void testTraitWithDefaultArgumentMethod2() {
        assertScript '''
            trait HiSupport {
               def sayHi(int x, String msg = "hi") {
                   "${msg}, $x"
               }
            }

            class A implements HiSupport { }

            def result = new A().sayHi(2)
            assert result == 'hi, 2'
        '''
    }

    void testTraitWithDefaultArgumentMethodCompileStatic() {
        assertScript '''import groovy.transform.CompileStatic

            @CompileStatic
            trait HiSupport {
               def sayHi(String msg = "hi") {
                   msg
               }
            }

            @CompileStatic
            class A implements HiSupport { }

            @CompileStatic
            void foo() {
                def result = new A().sayHi()
                assert result == 'hi'
            }
            foo()
        '''
    }

    void testTraitWithDefaultArgumentMethod2CompileStatic() {
        assertScript '''import groovy.transform.CompileStatic

            @CompileStatic
            trait HiSupport {
               def sayHi(int x=1, String msg = "hi") {
                   "${msg}, $x"
               }
            }

            @CompileStatic
            class A implements HiSupport { }

            @CompileStatic

            void foo() {
                def result = new A().sayHi(2)
                assert result == 'hi, 2'
            }
            foo()
        '''
    }
}
