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
 * Tests for {@code @Decreases} applied to loop statements.
 */
class LoopDecreasesTests extends BaseTestClass {

    @Test
    void decreasesOnWhileLoop() {
        assertScript '''
            import groovy.contracts.Decreases

            int n = 10
            @Decreases({ n })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    // GROOVY-12072: a statement-level @Decreases is never reached by the compiler's static-import
    // pass, so unqualified statically imported members in the variant closure must be resolved by
    // the loop transform itself.
    @Test
    void decreasesWithStaticImportedMethod() {
        assertScript '''
            import groovy.contracts.Decreases
            import static java.lang.Math.max

            int n = 10
            @Decreases({ max(0, n) })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    // GROOVY-12072: a qualified type reference in a statement-level @Decreases must be resolved by
    // the loop transform, as the compiler's ResolveVisitor never reaches the closure.
    @Test
    void decreasesWithQualifiedTypeReferenceUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Decreases
            import java.lang.Math

            @groovy.transform.CompileStatic
            static int go(int n) {
                int a = 0
                @Decreases({ Math.max(0, n - a) })
                while (a < n) { a++ }
                return a
            }

            assert go(5) == 5
        '''
    }

    @Test
    void decreasesOnClassicForLoop() {
        assertScript '''
            import groovy.contracts.Decreases

            int remaining = 5
            @Decreases({ remaining })
            for (int i = 0; i < 5; i++) {
                remaining--
            }
            assert remaining == 0
        '''
    }

    // GROOVY-12128: a classic for loop runs its update expression after the body, so a variant
    // whose progress lives in the update clause used to be re-evaluated before any progress had
    // registered and could never decrease. The variant is now measured at the start of each
    // iteration and consecutive iteration-start values are compared.
    @Test
    void decreasesOnClassicForLoopWithUpdateClauseProgress() {
        assertScript '''
            import groovy.contracts.Decreases

            int n = 3
            int i = 0
            @Decreases({ n - i })
            for (i = 0; i < n; i++) { }
            assert i == 3
        '''
    }

    // GROOVY-12128: compound update expressions (j += 2) progress in the update clause too
    @Test
    void decreasesOnClassicForLoopWithCompoundUpdate() {
        assertScript '''
            import groovy.contracts.Decreases

            int last = -1
            @Decreases({ 10 - j })
            for (int j = 0; j < 10; j += 2) {
                last = j
            }
            assert last == 8
        '''
    }

    // GROOVY-12128: the well-foundedness obligation is non-negativity at iteration entry, not
    // after the final body execution — a loop that overshoots below zero on its last pass still
    // terminates and must not be flagged.
    @Test
    void decreasesMayOvershootOnFinalIteration() {
        assertScript '''
            import groovy.contracts.Decreases

            int n = 5
            @Decreases({ n })
            while (n > 0) {
                n -= 2
            }
            assert n == -1
        '''
    }

    // GROOVY-12128: a for loop whose update clause makes no progress is still caught (at the
    // second iteration entry, where the variant has not decreased)
    @Test
    void decreasesViolationWhenForUpdateMakesNoProgress() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int guard = 0
            @Decreases({ 5 - i })
            for (int i = 0; i < 5; i = i) {
                if (++guard > 10) break
            }
        '''
    }

    // GROOVY-12128: a scalar measure that is already negative at first loop entry is not
    // well-founded, even when the loop happens to exit after a single iteration (the only
    // shape the consecutive-iteration comparison alone would let through)
    @Test
    void decreasesViolationWhenNegativeAtFirstEntry() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int n = 1
            @Decreases({ n - 2 })
            while (n > 0) {
                n--
            }
        '''
    }

    @Test
    void decreasesWithExpression() {
        assertScript '''
            import groovy.contracts.Decreases

            int lo = 0, hi = 10
            @Decreases({ hi - lo })
            while (lo < hi) {
                lo++
            }
            assert lo == 10
        '''
    }

    @Test
    void decreasesViolationWhenNotDecreasing() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int n = 5
            @Decreases({ n })
            while (n > 0) {
                // oops, not decreasing
                n++
                if (n > 10) break
            }
        '''
    }

    @Test
    void decreasesViolationWhenStayingSame() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int n = 5
            int count = 0
            @Decreases({ n })
            while (n > 0 && count < 3) {
                // n stays the same - should violate
                count++
            }
        '''
    }

    @Test
    void decreasesLexicographic() {
        assertScript '''
            import groovy.contracts.Decreases

            // outer stays the same while inner decreases,
            // then outer decreases and inner resets
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
        '''
    }

    @Test
    void decreasesLexicographicViolation() {
        shouldFail AssertionError, '''
            import groovy.contracts.Decreases

            int a = 5, b = 3
            @Decreases({ [a, b] })
            while (a > 0) {
                // a stays the same, b increases - should violate
                b++
                if (b > 10) break
            }
        '''
    }

    @Test
    void decreasesLexicographicFirstElementDecreases() {
        assertScript '''
            import groovy.contracts.Decreases

            // first element decreases, second can do anything
            int x = 5, y = 0
            @Decreases({ [x, y] })
            while (x > 0) {
                x--
                y += 10  // second element increases, but first decreased so it is OK
            }
            assert x == 0
        '''
    }

    @Test
    void decreasesCombinedWithInvariant() {
        assertScript '''
            import groovy.contracts.Decreases
            import groovy.contracts.Invariant

            int n = 5
            @Invariant({ n >= 0 })
            @Decreases({ n })
            while (n > 0) {
                n--
            }
            assert n == 0
        '''
    }

    @Test
    void decreasesUnderTypeChecked() {
        assertScript '''
            import groovy.contracts.Decreases
            import groovy.transform.TypeChecked

            @TypeChecked
            def method() {
                int n = 10
                @Decreases({ n })
                while (n > 0) {
                    n--
                }
                assert n == 0
            }
            method()
        '''
    }

    @Test
    void decreasesUnderCompileStatic() {
        assertScript '''
            import groovy.contracts.Decreases
            import groovy.transform.CompileStatic

            @CompileStatic
            def method() {
                int lo = 0, hi = 10
                @Decreases({ hi - lo })
                while (lo < hi) {
                    lo++
                }
                assert lo == 10
            }
            method()
        '''
    }

    // ----- global assertion toggle: -da suppresses the check, -ea (default) runs it -----

    @Test
    void disabledByDaJvmArg() {
        // A bounded loop whose measure increases: it is a LoopVariantViolation under -ea,
        // yet the loop itself terminates (via the break), so under -da (check suppressed)
        // it simply runs to completion. This mirrors the method-variant -da regression and
        // exercises the shared VariantSupport gating on the loop side.
        String script = '''
            import groovy.contracts.Decreases
            int n = 5
            @Decreases({ n })
            while (n > 0) {
                // oops, not decreasing
                n++
                if (n > 10) break
            }
            assert n == 11
        '''
        File tmp = File.createTempFile('gc_loop_decreases_da', '.groovy')
        tmp.deleteOnExit()
        tmp.text = script
        try {
            def da = runForked(tmp, '-da')
            assert da.code == 0: "expected clean run under -da, exit=${da.code}:\n${da.output}"

            def ea = runForked(tmp, '-ea')
            assert ea.code != 0: "expected a violation under -ea, but ran clean:\n${ea.output}"
            assert ea.output.contains('loop variant'): "expected LoopVariantViolation, got:\n${ea.output}"
        } finally {
            tmp.delete()
        }
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
}
