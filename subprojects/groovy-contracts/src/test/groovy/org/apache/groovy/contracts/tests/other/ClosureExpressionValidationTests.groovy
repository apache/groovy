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
import org.codehaus.groovy.control.CompilationFailedException

class ClosureExpressionValidationTests extends GroovyShellTestCase {

    void testCheckMissingExpressionsClassInvariant() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
            import groovy.contracts.*

            @Invariant({})
            class A {}

            def a = new A()
            """
        }

        assertTrue msg.contains("Annotation does not contain any expressions")
    }

    void testCheckMissingExpressionsPrecondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {
                   @Requires({})
                   def op() {}
                }

                def a = new A()
                """
        }

        assertTrue msg.contains("Annotation does not contain any expressions")
    }

    void testCheckMissingExpressionsPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                    import groovy.contracts.*

                    class A {
                       @Ensures({})
                       def op() {}
                    }

                    def a = new A()
                    """
        }

        assertTrue msg.contains("Annotation does not contain any expressions")
    }

    void testParameterSpecifiedClassInvariant() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                @Invariant({ some -> 1 == 1 })
                class A {}

                def a = new A()
                """
        }

        assertTrue msg.contains("Annotation does not support parameters")
    }

    void testParameterSpecifiedPostcondition() {

        evaluate """
                import groovy.contracts.*

                class A {

                    @Ensures({ result })
                    def op() {}
                }

                def a = new A()
        """
    }

    void testParameterNamesPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
            import groovy.contracts.*

            class A {

                @Ensures({ test -> 1 == 1 })
                def op() {}
            }

            def a = new A()
        """
        }

        assertTrue msg.contains("Postconditions only allow 'old' and 'result' closure parameters")
    }

    void testParameterWithExplicitTypePostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {

                    @Ensures({ java.util.Map<String, Object> result -> 1 == 1 })
                    def op() {}
                }

                def a = new A()
            """
        }

        assertTrue msg.contains("Postconditions do not support explicit types")
    }

    void testClosureItAccess() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                        import groovy.contracts.*

                        @Invariant({ it })
                        class A {

                        }

                        def a = new A()
                    """
        }

        assertTrue msg.contains("Access to 'it' is not supported.")
    }

    void testPrefixOperatorUsage() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {

                    @Requires({ ++arg })
                    def op(def arg) {}
                }

                def a = new A()
            """
        }

        assertTrue msg.contains("State changing postfix & prefix operators are not supported.")
    }

    void testPostfixOperatorUsage() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                    import groovy.contracts.*

                    class A {

                        @Requires({ arg++ })
                        def op(def arg) {}
                    }

                    def a = new A()
                """
        }

        assertTrue msg.contains("State changing postfix & prefix operators are not supported.")
    }
}
