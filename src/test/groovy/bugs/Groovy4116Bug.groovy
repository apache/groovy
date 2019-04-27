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

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4116Bug extends GroovyTestCase {
    void testAnInterfaceMethodNotImplementedPublic() {
        try {
            new GroovyShell().parse """
                class C4116 implements I4116 {
                    protected foo() {}
                }
                
                interface I4116 {
                    def foo()
                }
                
                def c = new C4116()
                c.foo()
            """
            fail('The compilation should have failed as the interface method is not implemented as public')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('The method foo should be public as it implements the corresponding method from interface I4116')
        }
    }

    void testAnInterfaceMethodNotImplementedPublicV2SuperClassInterface() {
        try {
            new GroovyShell().parse """
                abstract class S4116V2 implements I4116V2 { }
                class C4116V2 extends S4116V2 {
                    protected foo() {}
                }
                
                interface I4116V2 {
                    def foo()
                }
                
                def c = new C4116V2()
                c.foo()
            """
            fail('The compilation should have failed as the interface method is not implemented as public')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('The method foo should be public as it implements the corresponding method from interface I4116')
        }
    }
}