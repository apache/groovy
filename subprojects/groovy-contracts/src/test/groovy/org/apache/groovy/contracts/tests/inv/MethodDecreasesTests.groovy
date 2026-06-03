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
package org.apache.groovy.contracts.tests.inv

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@code @Decreases} applied to a (recursive) method as a recursion
 * termination measure.
 */
class MethodDecreasesTests extends BaseTestClass {

    @Test
    void terminatingRecursionPasses() {
        assertScript '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            int sumUp(int n) {
                if (n == 0) return 0
                sumUp(n - 1) + n
            }
            assert sumUp(5) == 15
            assert sumUp(0) == 0
        '''
    }

    @Test
    void nonRecursiveCallIsUnaffected() {
        assertScript '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            int identity(int n) { n }
            assert identity(42) == 42
            assert identity(7) == 7      // sequential top-level calls: no stale state
        '''
    }

    @Test
    void measureThatDoesNotDecreaseIsCaught() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            int bad(int n) {
                bad(n)                   // recurses with the same measure
            }
            bad(3)
        '''
    }

    @Test
    void measureGoingNegativeIsCaught() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            int neg(int n) {
                if (n < -100) return 0
                neg(n - 1)               // decreases, but runs below the well-founded floor of 0
            }
            neg(0)
        '''
    }

    // ----- parity with the loop variant: any Comparable scalar -----

    @Test
    void stringMeasure() {
        assertScript '''
            import groovy.contracts.Decreases

            @Decreases({ s })
            String shrink(String s) {
                if (s <= 'a') return s
                shrink(s.previous())
            }
            assert shrink('z') == 'a'
        '''
    }

    // ----- parity with the loop variant: lexicographic List measure -----

    @Test
    void lexicographicMeasure() {
        assertScript '''
            import groovy.contracts.Decreases

            @Decreases({ [outer, inner] })
            def run(int outer, int inner) {
                if (outer == 0) return 'done'
                if (inner > 0) return run(outer, inner - 1)
                return run(outer - 1, 3)
            }
            assert run(2, 3) == 'done'
        '''
    }

    @Test
    void lexicographicMeasureIncreaseIsCaught() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            @Decreases({ [a, b] })
            def bad(int a, int b) {
                bad(a + 1, b)            // first component increases
            }
            bad(0, 0)
        '''
    }

    // ----- composition with the other contracts on the same method -----

    @Test
    void composesWithRequiresAndEnsures() {
        assertScript '''
            import groovy.contracts.Requires
            import groovy.contracts.Ensures
            import groovy.contracts.Decreases

            @Requires({ n >= 0 })
            @Ensures({ result >= n })
            @Decreases({ n })
            int sumUp(int n) {
                if (n == 0) return 0
                sumUp(n - 1) + n
            }
            assert sumUp(5) == 15
        '''
    }

    // ----- mutual recursion: each participant carries its own measure -----

    @Test
    void mutualRecursionTerminates() {
        assertScript '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            boolean isEven(int n) { n == 0 ? true : isOdd(n - 1) }

            @Decreases({ n })
            boolean isOdd(int n) { n == 0 ? false : isEven(n - 1) }

            assert isEven(10)
            assert !isOdd(10)
            assert isOdd(7)
        '''
    }

    @Test
    void mutualRecursionThatDoesNotDecreaseIsCaught() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            @Decreases({ n })
            def ping(int n) { pong(n) }      // no progress across the cycle

            @Decreases({ n })
            def pong(int n) { ping(n) }

            ping(3)
        '''
    }

    // ----- @CompileStatic compatibility -----

    // ----- global assertion toggle: -da suppresses the check, -ea (default) runs it -----

    @Test
    void disabledByDaJvmArg() {
        // A bounded recursion whose measure is constant: it never decreases, so it is
        // a RecursionVariantViolation under -ea, yet the recursion itself terminates,
        // so under -da (check suppressed) it simply runs to completion.
        String script = '''
            import groovy.contracts.Decreases
            @Decreases({ 5 })
            int f(int n) { n <= 0 ? 0 : f(n - 1) }
            assert f(3) == 0
        '''
        File tmp = File.createTempFile('gc_decreases_da', '.groovy')
        tmp.deleteOnExit()
        tmp.text = script
        try {
            def da = runForked(tmp, '-da')
            assert da.code == 0: "expected clean run under -da, exit=${da.code}:\n${da.output}"

            def ea = runForked(tmp, '-ea')
            assert ea.code != 0: "expected a violation under -ea, but ran clean:\n${ea.output}"
            assert ea.output.contains('recursion measure'): "expected RecursionVariantViolation, got:\n${ea.output}"
        } finally {
            tmp.delete()
        }
    }

    // ----- inheritance: empirical observation vs other contracts -----

    @Test
    void ensuresIsInheritedToOverride() {
        // Baseline (Liskov postcondition inheritance): groovy-contracts enforces a parent
        // @Ensures on a subclass override that does not redeclare it.
        shouldFail AssertionError, '''
            import groovy.contracts.Ensures
            class Base { @Ensures({ result >= 0 }) int g(int x) { x } }
            class Derived extends Base { int g(int x) { x } }
            new Derived().g(-1)
        '''
    }

    @Test
    void decreasesIsInheritedToOverride() {
        // Like @Requires/@Ensures: an override that does NOT redeclare @Decreases inherits the
        // parent's `n` measure, so recursing with an increasing n violates that inherited measure.
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases
            class Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            class Derived extends Base { int f(int n) { n >= 5 ? 0 : f(n + 1) } }
            new Derived().f(0)
        '''
    }

    @Test
    void inheritedMeasureAllowsACorrectlyDecreasingOverride() {
        assertScript '''
            import groovy.contracts.Decreases
            class Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            class Derived extends Base { int f(int n) { n <= 0 ? 0 : 2 * f(n - 1) } }  // still decreases n
            assert new Derived().f(4) == 0
        '''
    }

    @Test
    void inheritedMeasureFollowsRenamedParameters() {
        // The override renames the parameter (n -> m); the inherited measure must be re-mapped so it
        // references m. (If it weren't, `n` would be unresolved and we'd see a different failure than
        // the RecursionVariantViolation this expects.)
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases
            class Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            class Derived extends Base { int f(int m) { m >= 5 ? 0 : f(m + 1) } }
            new Derived().f(0)
        '''
    }

    @Test
    void decreasesOnOverrideWithItsOwnMeasureIsChecked() {
        // When the override redeclares @Decreases, the check applies to it.
        assertScript '''
            import groovy.contracts.Decreases
            class Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            class Derived extends Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            assert new Derived().f(4) == 0
        '''
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases
            class Base { @Decreases({ n }) int f(int n) { n <= 0 ? 0 : f(n - 1) } }
            class Derived extends Base { @Decreases({ n }) int f(int n) { n >= 5 ? 0 : f(n + 1) } }
            new Derived().f(0)
        '''
    }

    private static Map runForked(File scriptFile, String assertionFlag) {
        String javaBin = "${System.getProperty('java.home')}${File.separator}bin${File.separator}java"
        // Drop Spock from the child classpath: its global AST transform on the test
        // classpath is incompatible with this Groovy and would abort compilation of
        // any script. We only need Groovy + groovy-contracts to compile the snippet.
        String cp = System.getProperty('java.class.path')
                .split(File.pathSeparator)
                .findAll { !it.toLowerCase().contains('spock') }
                .join(File.pathSeparator)
        Process proc = new ProcessBuilder(javaBin, assertionFlag, '-cp', cp,
                'groovy.ui.GroovyMain', scriptFile.absolutePath)
                .redirectErrorStream(true)
                .start()
        String output = proc.inputStream.text
        proc.waitFor()
        [code: proc.exitValue(), output: output]
    }

    @Test
    void worksUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Decreases
            import groovy.transform.CompileStatic

            @CompileStatic
            class C {
                @Decreases({ n })
                static int sumUp(int n) {
                    if (n == 0) return 0
                    sumUp(n - 1) + n
                }
            }
            assert C.sumUp(5) == 15
        '''
    }
}