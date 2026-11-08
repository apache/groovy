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
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases

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

    // GROOVY-12052, GROOVY-12078: a static method has no instance state, so old may only reference a parameter
    void testOldFieldNotSupportedInStaticPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                    import groovy.contracts.*

                    class A {

                        private static int field = 1

                        @Ensures({ old.field == result })
                        static int op(int value) { value }
                    }

                    A.op(1)
                """
        }

        assertTrue msg.contains("'old' in a postcondition of a static method may only reference a method parameter")
    }

    // GROOVY-12728: the value is declared as a Class, so a class literal passed the annotation checks
    // but was then taken for the generated condition class and never checked
    void testClassLiteralPrecondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {
                    @Requires(Object)
                    def op(int x) {}
                }

                new A().op(-1)
                """
        }

        assertTrue msg.contains("@Requires requires a closure containing the condition, e.g. @Requires({ ... }); found a class java.lang.Object")
    }

    // GROOVY-12728
    void testClassLiteralPostcondition() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                class A {
                    @Ensures(String)
                    int op(int x) { x }
                }

                new A().op(-1)
                """
        }

        assertTrue msg.contains("@Ensures requires a closure containing the condition, e.g. @Ensures({ ... }); found a class java.lang.String")
    }

    // GROOVY-12728
    void testClassLiteralClassInvariant() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                @Invariant(Object.class)
                class A {
                    int count
                    void dec() { count-- }
                }

                new A().dec()
                """
        }

        assertTrue msg.contains("@Invariant requires a closure containing the condition, e.g. @Invariant({ ... }); found a class java.lang.Object")
    }

    // GROOVY-12728: with an inherited contract, a class literal failed compilation with an internal error
    void testClassLiteralBesideInheritedContracts() {

        def msg = shouldFail CompilationFailedException, {
            evaluate """
                import groovy.contracts.*

                @Invariant({ count >= 0 })
                class B {
                    int count
                    @Requires({ x > 0 }) void m(int x) {}
                    @Ensures({ result > 0 }) int n(int x) { x }
                }

                @Invariant(Object)
                class C extends B {
                    @Requires(Object) void m(int x) {}
                    @Ensures(Object) int n(int x) { x }
                }
                """
        }

        assertTrue msg.contains("@Invariant requires a closure")
        assertTrue msg.contains("@Requires requires a closure")
        assertTrue msg.contains("@Ensures requires a closure")
        assertFalse msg.contains("BUG!")
    }

    // GROOVY-12728: a supertype in another source unit may have its conditions replaced first
    void testContractsOfSupertypeInLaterSourceUnit() {

        def unit = new CompilationUnit()
        unit.addSource("C.groovy", """
            import groovy.contracts.*

            @Invariant({ count < 10 })
            class C extends B {
                @Requires({ x < 10 }) void m(int x) {}
                @Ensures({ result < 10 }) int n(int x) { x }
            }
            """)
        unit.addSource("B.groovy", """
            import groovy.contracts.*

            @Invariant({ count >= 0 })
            class B {
                int count
                @Requires({ x > 0 }) void m(int x) {}
                @Ensures({ result > 0 }) int n(int x) { x }
            }
            """)
        unit.compile(Phases.CLASS_GENERATION)

        assertTrue unit.classes*.name.containsAll(['B', 'C'])
    }
}
