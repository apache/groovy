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

class Groovy4111Bug extends GroovyTestCase {
    void testConstructorCallOnAnAbstractType() {
        try {
            new GroovyShell().parse """
                class Test4111 {
                    static main(args) {
                        def obj = new I4111()
                    }
                }
                
                interface I4111 {}
            """
            fail('The compilation should have failed as the constructor call is on an interface')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains('You cannot create an instance from the abstract interface')
        }
    }
}