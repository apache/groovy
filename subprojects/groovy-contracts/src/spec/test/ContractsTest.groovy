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
import groovy.test.GroovyTestCase

class ContractsTest extends GroovyTestCase {

    void testBasicExample() {
        assertScript '''
        // tag::basic_example[]
        package acme

        import groovy.contracts.*

        @Invariant({ speed() >= 0 })
        class Rocket {
            int speed = 0
            boolean started = true

            @Requires({ isStarted() })
            @Ensures({ old.speed < speed })
            def accelerate(inc) { speed += inc }

            def speed() { speed }
        }

        def r = new Rocket()
        r.accelerate(5)
        // end::basic_example[]
        '''
    }

    void testStackExample() {
        assertScript '''
        // tag::stack_example[]
        import groovy.contracts.*

        @Invariant({ elements != null })
        class Stack<T> {

            List<T> elements

            @Ensures({ is_empty() })
            def Stack()  {
                elements = []
            }

            @Requires({ preElements?.size() > 0 })
            @Ensures({ !is_empty() })
            def Stack(List<T> preElements)  {
                elements = preElements
            }

            boolean is_empty()  {
                elements.isEmpty()
            }

            @Requires({ !is_empty() })
            T last_item()  {
                elements.get(count() - 1)
            }

            def count() {
                elements.size()
            }

            @Ensures({ result == true ? count() > 0 : count() >= 0  })
            boolean has(T item)  {
                elements.contains(item)
            }

            @Ensures({ last_item() == item })
            def push(T item)  {
               elements.add(item)
            }

            @Requires({ !is_empty() })
            @Ensures({ last_item() == item })
            def replace(T item)  {
                remove()
                elements.add(item)
            }

            @Requires({ !is_empty() })
            @Ensures({ result != null })
            T remove()  {
                elements.remove(count() - 1)
            }

            String toString() { elements.toString() }
        }

        def stack = new Stack<Integer>()
        // end::stack_example[]
        '''
    }

    void testLoopInvariantForIn() {
        assertScript '''
        // tag::loop_invariant_for_example[]
        import groovy.contracts.Invariant

        int sum = 0
        @Invariant({ 0 <= i && i <= 4 })
        for (int i in 0..4) {
            sum += i
        }
        assert sum == 10
        // end::loop_invariant_for_example[]
        '''
    }

    void testLoopInvariantWhile() {
        assertScript '''
        // tag::loop_invariant_while_example[]
        import groovy.contracts.Invariant

        int n = 10
        @Invariant({ n >= 0 })
        while (n > 0) {
            n--
        }
        assert n == 0
        // end::loop_invariant_while_example[]
        '''
    }

    void testLoopInvariantMultiple() {
        assertScript '''
        // tag::loop_invariant_multiple_example[]
        import groovy.contracts.Invariant

        int sum = 0
        @Invariant({ sum >= 0 })
        @Invariant({ sum <= 100 })
        for (int i in 1..5) {
            sum += i
        }
        assert sum == 15
        // end::loop_invariant_multiple_example[]
        '''
    }

    void testDecreasesWhile() {
        assertScript '''
        // tag::decreases_while_example[]
        import groovy.contracts.Decreases

        int n = 10
        @Decreases({ n })
        while (n > 0) {
            n--
        }
        assert n == 0
        // end::decreases_while_example[]
        '''
    }

    void testDecreasesFor() {
        assertScript '''
        // tag::decreases_for_example[]
        import groovy.contracts.Decreases

        int remaining = 5
        @Decreases({ remaining })
        for (int i = 0; i < 5; i++) {
            remaining--
        }
        assert remaining == 0
        // end::decreases_for_example[]
        '''
    }

    void testDecreasesLexicographic() {
        assertScript '''
        // tag::decreases_lexicographic_example[]
        import groovy.contracts.Decreases

        int outer = 2, inner = 3
        @Decreases({ [outer, inner] })
        while (outer > 0) {
            if (inner > 0) {
                inner--
            } else {
                outer--
                inner = 3
            }
        }
        assert outer == 0
        // end::decreases_lexicographic_example[]
        '''
    }

    void testDecreasesRecursion() {
        assertScript '''
        // tag::decreases_recursion_example[]
        import groovy.contracts.Decreases

        @Decreases({ n })                       // strictly decreases on every recursive call
        int factorial(int n) {
            n <= 1 ? 1 : n * factorial(n - 1)
        }
        assert factorial(5) == 120
        // end::decreases_recursion_example[]
        '''
    }

    void testJep445Script() {
        runScript '''
        // tag::jep445_example[]
        import groovy.contracts.*
        import org.apache.groovy.contracts.*
        import static groovy.test.GroovyAssert.shouldFail

        @Requires({ arg > 0 })
        @Ensures({ result < arg })
        def sqrt(arg) { Math.sqrt(arg) }

        def main() {
            assert sqrt(4) == 2
            shouldFail(PreconditionViolation) { sqrt(-1) }
        }
        // end::jep445_example[]
        '''
    }

    void testThrowsIfWoven() {
        runScript '''
        // tag::throwsif_woven_example[]
        import groovy.contracts.ThrowsIf
        import static groovy.test.GroovyAssert.shouldFail

        class Calculator {
            @ThrowsIf(value = { b == 0 }, exception = ArithmeticException)
            static int divide(int a, int b) { a.intdiv(b) }
        }

        assert Calculator.divide(4, 2) == 2
        shouldFail(ArithmeticException) { Calculator.divide(1, 0) }   // the woven guard
        // end::throwsif_woven_example[]
        '''
    }

    void testThrowsIfBody() {
        runScript '''
        // tag::throwsif_body_example[]
        import groovy.contracts.ThrowsIf
        import static groovy.test.GroovyAssert.shouldFail

        class MathUtil {
            @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false)
            static int fact(int n) {
                if (n < 0) throw new IllegalArgumentException('negative')   // the guard is already here
                n <= 1 ? 1 : n * fact(n - 1)
            }
        }

        assert MathUtil.fact(4) == 24
        shouldFail(IllegalArgumentException) { MathUtil.fact(-1) }
        // end::throwsif_body_example[]
        '''
    }

    void testThrowsIfIndirect() {
        runScript '''
        // tag::throwsif_indirect_example[]
        import groovy.contracts.ThrowsIf

        class Wrapper {
            @ThrowsIf(value = { s == null }, exception = NullPointerException, woven = false, direct = false)
            static Object describe(Object s) {
                Objects.requireNonNull(s)   // the library throws; the annotation records the contract
                "value: $s"
            }
        }

        assert Wrapper.describe('x') == 'value: x'
        // end::throwsif_indirect_example[]
        '''
    }

    void testThrowsIfChecked() {
        runScript '''
        // tag::throwsif_checked_example[]
        import groovy.contracts.ThrowsIf
        import org.apache.groovy.contracts.ThrowsIfViolation
        import static groovy.test.GroovyAssert.shouldFail

        class Broken {
            // the body SHOULD throw when n < 0 but does not — a broken implementation,
            // reported as a violation (never the declared exception)
            @ThrowsIf(value = { n < 0 }, exception = IllegalArgumentException, woven = false, checked = true)
            static int identity(int n) { n }
        }

        assert Broken.identity(5) == 5
        shouldFail(ThrowsIfViolation) { Broken.identity(-5) }
        // end::throwsif_checked_example[]
        '''
    }

    void testRequiresUnwoven() {
        runScript '''
        // tag::requires_unwoven_example[]
        import groovy.contracts.Requires
        import static groovy.test.GroovyAssert.shouldFail

        class Greeter {
            // the obligation is already enforced by requireNonNull: the annotation documents it,
            // no assertion is generated, and a violating caller sees the library's NPE
            @Requires(value = { name != null }, woven = false, direct = false)
            static String greet(String name) {
                Objects.requireNonNull(name)
                "Hello, $name!"
            }
        }

        assert Greeter.greet('World') == 'Hello, World!'
        shouldFail(NullPointerException) { Greeter.greet(null) }   // the enforcement, untouched
        // end::requires_unwoven_example[]
        '''
    }

    void testRequiresMixedArms() {
        runScript '''
        // tag::requires_mixed_example[]
        import groovy.contracts.Requires
        import org.apache.groovy.contracts.PreconditionViolation
        import static groovy.test.GroovyAssert.shouldFail

        class Adjuster {
            @Requires({ amount <= 1000 })                              // woven: asserted
            @Requires(value = { amount >= 0 }, woven = false)          // enforced in the body
            static int adjust(int amount) {
                if (amount < 0) throw new IllegalArgumentException('negative')
                amount
            }
        }

        assert Adjuster.adjust(5) == 5
        shouldFail(PreconditionViolation) { Adjuster.adjust(2000) }        // the woven arm
        shouldFail(IllegalArgumentException) { Adjuster.adjust(-1) }       // the body's own guard
        // end::requires_mixed_example[]
        '''
    }

    private static void runScript(String scriptText) {
        new GroovyShell().run(scriptText, 'ScriptSnippet', [] as String[])
    }
}
