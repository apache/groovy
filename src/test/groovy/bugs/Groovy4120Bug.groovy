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
import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4120Bug extends GroovyTestCase {
    void testInitCallOnNonStaticInnerClassWOEnclosingInstance() {
        try {
            new GroovyShell().parse """
                class Test4120A {
                    static main(args) {
                        new A4120A()
                    }
                    class A4120A {
                        def A4120A() {}
                        def A4120A(num){}
                    }
                }
            """
            fail('The compilation should have failed a constructor call is made on a non-static inner class without an encosing instance')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('No enclosing instance passed in constructor call of a non-static inner class')
        }
    }

    void testInitCallOnNonStaticInnerClassWithEnclosingInstance() {
        new GroovyShell().parse """
            class Test4120B {
                static main(args) {
                    new A4120B(new Test4120B())
                }
                class A4120B {
                    def A4120B() {}
                    def A4120B(num){}
                }
            }
        """
    }
}