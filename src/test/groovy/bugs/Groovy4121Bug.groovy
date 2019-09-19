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

class Groovy4121Bug extends GroovyTestCase {
    void testAssignmentToAFieldMadeFinalByImmutable() {
        try {
            new GroovyShell().parse """
                @groovy.transform.Immutable
                class Account4121 {
                    BigDecimal balance
                    String customer
                    
                    Account4121 deposit(amount) {
                        balance = balance + amount
                        this
                    }
                }
                
                def acc = new Account4121(0.0, "Test")
                acc.deposit(3.1)
                assert 3.1 == acc.balance
            """
            fail('The compilation should have failed as a final field is being assigned to.')
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("cannot modify final field 'balance' outside of constructor")
        }
    }
}