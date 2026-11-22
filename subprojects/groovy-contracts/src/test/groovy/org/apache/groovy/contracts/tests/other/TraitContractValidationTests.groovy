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
package org.apache.groovy.contracts.tests.other

import groovy.test.GroovyShellTestCase
import org.apache.groovy.contracts.PreconditionViolation
import org.codehaus.groovy.control.CompilationFailedException

/**
 * Contract annotations on traits: a trait or a trait method with a body is rejected with a compile error, and a
 * contract on an abstract trait method keeps applying to the implementing class like an interface contract.
 */
class TraitContractValidationTests extends GroovyShellTestCase {

    // GROOVY-12741: the trait transform moves a trait method with a body into the helper class as a static
    // method with a leading $self parameter, so the generated condition class no longer matched the method
    // it was applied to; the compiler failed with "Index 1 out of bounds for length 1" at instruction selection
    void testPreconditionOnTraitMethodRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                trait Bank {
                    BigDecimal balance = 0
                    @Requires({ amount > 0 })
                    void deposit(BigDecimal amount) { balance += amount }
                }
                class Account implements Bank {}

                new Account().deposit(1)
                """
        }

        assertTrue msg.contains("[groovy-contracts] @Requires is not supported on a trait method with a body (Bank.deposit); only an abstract trait method may carry a contract.")
    }

    // GROOVY-12741: without parameters the class compiled, then the check failed at run time with
    // "No such property: \$GCONTRACTS_ENABLED for class: Bank\$Trait\$Helper"
    void testPreconditionOnParameterlessTraitMethodRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                trait Bank {
                    BigDecimal balance = 0
                    @Requires({ balance >= 0 })
                    void reset() { balance = 0 }
                }
                class Account implements Bank {}

                new Account().reset()
                """
        }

        assertTrue msg.contains("[groovy-contracts] @Requires is not supported on a trait method with a body (Bank.reset)")
    }

    // GROOVY-12741: a postcondition compiled, then failed at run time like the parameterless precondition
    void testPostconditionOnTraitMethodRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                trait Bank {
                    BigDecimal balance = 0
                    @Ensures({ result == old.balance + amount })
                    BigDecimal deposit(BigDecimal amount) { balance += amount; balance }
                }
                class Account implements Bank {}

                new Account().deposit(1)
                """
        }

        assertTrue msg.contains("[groovy-contracts] @Ensures is not supported on a trait method with a body (Bank.deposit)")
    }

    // GROOVY-12741: a trait invariant was inherited by the implementing class like an interface invariant and
    // failed spuriously while the trait's fields were still being initialised
    void testClassInvariantOnTraitRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                @Invariant({ balance >= 0 })
                trait Bank {
                    BigDecimal balance = 0
                    void deposit(BigDecimal amount) { balance += amount }
                }
                class Account implements Bank {}

                new Account().deposit(1)
                """
        }

        assertTrue msg.contains("[groovy-contracts] @Invariant is not supported on a trait (Bank).")
    }

    // GROOVY-12741: the statically compiled trait failed the same way, in StaticInvocationWriter
    void testPreconditionOnStaticallyCompiledTraitMethodRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*
                import groovy.transform.CompileStatic

                @CompileStatic
                trait Bank {
                    BigDecimal balance = 0
                    @Requires({ amount > 0 })
                    void deposit(BigDecimal amount) { balance += amount }
                }
                class Account implements Bank {}

                new Account().deposit(1)
                """
        }

        assertTrue msg.contains("[groovy-contracts] @Requires is not supported on a trait method with a body (Bank.deposit)")
    }

    // GROOVY-12741: an abstract trait method keeps its contract, which applies to the implementing class
    // like an interface contract
    void testPreconditionOnAbstractTraitMethodIsInherited() {

        shouldFail PreconditionViolation, {
            evaluate """
                import groovy.contracts.*

                trait Bank {
                    @Requires({ amount > 0 })
                    abstract void deposit(BigDecimal amount)
                }
                class Account implements Bank {
                    BigDecimal balance = 0
                    void deposit(BigDecimal amount) { balance += amount }
                }

                def account = new Account()
                account.deposit(1)
                assert account.balance == 1
                account.deposit(-1)
                """
        }
    }
}
