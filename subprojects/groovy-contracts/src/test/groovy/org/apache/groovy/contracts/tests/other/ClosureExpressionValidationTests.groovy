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

    // GROOVY-12059: 'it' belonging to a nested closure is supported (only the contract closure's own 'it' is rejected)
    void testNestedClosureImplicitItIsAllowed() {

        assertEquals 6, evaluate("""
            import groovy.contracts.*

            class A {
                @Requires({ ns.every { it > 0 } })
                int total(int[] ns) { ns.sum() }
            }

            new A().total([1, 2, 3] as int[])
        """)
    }

    // GROOVY-12059: a nested closure declaring its own parameter is supported
    void testNestedClosureExplicitParameterIsAllowed() {

        assertEquals 6, evaluate("""
            import groovy.contracts.*

            class A {
                @Requires({ ns.every { n -> n > 0 } })
                int total(int[] ns) { ns.sum() }
            }

            new A().total([1, 2, 3] as int[])
        """)
    }

    // GROOVY-12059: a nested closure may reference an outer method parameter (capture must be wired up)
    void testNestedClosureReferencingMethodParameterIsAllowed() {

        assertEquals 6, evaluate("""
            import groovy.contracts.*

            class A {
                @Requires({ ns.indices.every { ns[it] > 0 } })
                int total(int[] ns) { ns.sum() }
            }

            new A().total([1, 2, 3] as int[])
        """)
    }

    // GROOVY-12059: a parameter on the contract closure itself is still rejected for preconditions
    void testTopLevelParameterPreconditionRejected() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {
                    @Requires({ n -> n > 0 })
                    int op(int n) { n }
                }

                def a = new A()
            """
        }

        assertTrue msg.contains("Annotation does not support parameters")
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

    // GROOVY-12052
    void testOldNotSupportedInStaticPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                    import groovy.contracts.*

                    class A {

                        @Ensures({ old.value == result })
                        static int op(int value) { value }
                    }

                    A.op(1)
                """
        }

        assertTrue msg.contains("'old' is not supported in postconditions of static methods")
    }
}
