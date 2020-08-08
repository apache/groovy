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
package org.apache.groovy.contracts.compability

import groovy.test.GroovyShellTestCase
import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.PreconditionViolation

class CompileStaticTests extends GroovyShellTestCase {

    void testPrecondition() {
        evaluate """
            import groovy.contracts.*

            @groovy.transform.CompileStatic
            class A {
                @Requires({ param.size() > 0 })
                void someOperation(String param) { }
            }
            new A()
        """
    }

    void testPreconditionViolation() {
        shouldFail PreconditionViolation, {
            evaluate """
                import groovy.contracts.*

                @groovy.transform.CompileStatic
                class A {
                    @Requires({ param.length() > 0 })
                    void someOperation(String param) { }
                }
                new A().someOperation('')
            """
        }
    }

    void testPostcondition() {
        evaluate """
                import groovy.contracts.*

                @groovy.transform.CompileStatic
                class A {
                    @Ensures({ result == 3  })
                    Integer add() { return 1 + 1 }
                }
                new A()
            """
    }

    void testPostconditionViolation() {
        shouldFail PostconditionViolation, {
            evaluate """
                import groovy.contracts.*

                @groovy.transform.CompileStatic
                class A {
                    @Ensures({ result == 3  })
                    Integer add() { return 1 + 1 }
                }
                new A().add()
            """
        }
    }

    void testClassInvariant() {
        evaluate """
            import groovy.contracts.*

            @groovy.transform.CompileStatic
            @Invariant({ speed >= 0 })
            class A {
                Integer speed = 1
            }
            new A()
        """
    }

}
