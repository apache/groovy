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

import groovy.test.GroovyTestCase

class Groovy3519Bug extends GroovyTestCase {

    void testShouldSkipPrivateMethodsFromCovariantReturnTypeChecks() {
        assertScript '''
            class A {
                private String foo() { "1" }
                def bar() { foo() }
            }
            def a = new A()
            assert a.bar() == "1"
            class B extends A {
                Integer foo() {2}
            }
            def b = new B()
            assert b.bar()=="1"
        '''
    }

    void testShouldSkipPrivateMethodsFromCovariantReturnTypeChecksCS() {
        assertScript '''import groovy.transform.CompileStatic

            @CompileStatic
            class A {
                private String foo() { "1" }
                def bar() { foo() }
            }
            def a = new A()
            assert a.bar() == "1"
            @CompileStatic
            class B extends A {
                Integer foo() {2}
            }
            def b = new B()
            assert b.bar()=="1"
        '''
    }

}




