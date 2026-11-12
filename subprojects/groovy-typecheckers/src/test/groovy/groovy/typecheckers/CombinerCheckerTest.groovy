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
package groovy.typecheckers

import groovy.transform.TypeChecked
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Spike tests for {@link CombinerChecker}. Mirrors the harness style of
 * {@code PurityCheckerTest}.
 */
final class CombinerCheckerTest {

    private static GroovyShell lenientShell
    private static GroovyShell strictShell

    @BeforeAll
    static void setUp() {
        lenientShell = makeShell(null)
        strictShell = makeShell('strict')
    }

    private static GroovyShell makeShell(String mode) {
        String ext = mode ? "groovy.typecheckers.CombinerChecker(mode: '${mode}')" : 'groovy.typecheckers.CombinerChecker'
        new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(TypeChecked)
            customizer.annotationParameters = [extensions: ext]
            addCompilationCustomizers(customizer)
        })
    }

    // === lenient mode: only high-confidence problems are flagged ===

    // NB: most closures below type their parameters, but untyped ones work too:
    // under @TypeChecked their types are inferred from the receiver (sumParallel)
    // or the seed (injectParallel), so the checker sees the same closure either
    // way (untyped_inline_closure_is_checked_like_a_typed_one). The
    // annotated-method-reference path remains the robust one; inline analysis
    // is best-effort.

    @Test
    void associative_inline_closure_passes() {
        assertScript lenientShell, '''
            assert [1, 2, 3, 4].sumParallel { int a, int b -> a + b } == 10
            assert [1, 2, 3, 4].injectParallel(0) { int a, int b -> a + b } == 10
        '''
    }

    @Test
    void untyped_inline_closure_is_checked_like_a_typed_one() {
        def err = shouldFail lenientShell, '''
            [1, 2, 3, 4].sumParallel { a, b -> a - b }
        '''
        assert err.message.contains('non-associative')
        assertScript lenientShell, '''
            assert [1, 2, 3, 4].sumParallel { a, b -> a + b } == 10
        '''
    }

    @Test
    void subtraction_inline_closure_fails() {
        def err = shouldFail lenientShell, '''
            [1, 2, 3, 4].injectParallel(0) { int a, int b -> a - b }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void modulo_inline_closure_fails() {
        def err = shouldFail lenientShell, '''
            [12, 7, 5].sumParallel { int a, int b -> a % b }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void power_inline_closure_fails() {
        // also confirms '**' is a BinaryExpression with token text '**'
        def err = shouldFail lenientShell, '''
            [2, 3, 2].injectParallel(1) { int a, int b -> (a ** b) as int }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void annotated_method_reference_passes() {
        assertScript lenientShell, '''
            import groovy.transform.Associative

            class Maths {
                @Associative
                static int add(int a, int b) { a + b }
            }

            assert [1, 2, 3, 4].injectParallel(0, Maths.&add) == 10
        '''
    }

    @Test
    void reducer_seed_mismatch_fails() {
        def err = shouldFail lenientShell, '''
            import groovy.transform.Reducer

            class Maths {
                @Reducer(zero = '0')
                static int add(int a, int b) { a + b }
            }

            [1, 2, 3].injectParallel(5, Maths.&add)
        '''
        assert err.message.contains('does not match')
    }

    @Test
    void reducer_seed_match_passes() {
        assertScript lenientShell, '''
            import groovy.transform.Reducer

            class Maths {
                @Reducer(zero = '0')
                static int add(int a, int b) { a + b }
            }

            assert [1, 2, 3].injectParallel(0, Maths.&add) == 6
        '''
    }

    // === strict mode: a declared, verifiable contract is required ===

    @Test
    void strict_unannotated_inline_closure_fails_even_when_associative() {
        def err = shouldFail strictShell, '''
            [1, 2, 3].injectParallel(0) { int a, int b -> a + b }
        '''
        assert err.message.contains('strict')
    }

    @Test
    void strict_sumParallel_with_a_transform_has_no_combiner_to_check() {
        // sumParallel(transform) adds the transformed values with plus, so strict mode asks for no contract
        assertScript strictShell, '''
            List<String> words = ['a', 'bb', 'ccc']
            assert words.sumParallel { it.size() } == 6
            assert words.sumParallel { String w -> w.size() } == 6
            assert words.sumParallel((String w) -> w.size()) == 6
            assert words.sumParallel(String::length) == 6
            java.util.function.Function<String, Integer> size = String::length
            assert words.sumParallel(size) == 6
        '''
    }

    @Test
    void sumParallel_combiner_beside_the_transform_form_is_still_checked() {
        def err = shouldFail lenientShell, '''
            [1, 2, 3, 4].sumParallel { int a, int b -> a - b }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void strict_instance_bound_annotated_method_reference_passes() {
        assertScript strictShell, '''
            import groovy.transform.Associative

            class Maths {
                @Associative
                int add(int a, int b) { a + b }
            }

            def x = new Maths()
            assert [1, 2, 3].injectParallel(0, x.&add) == 6
        '''
    }

    @Test
    void non_thin_closure_with_bad_op_not_bypassed_by_carrier() {
        // Closure delegates to a Monoid *and* applies '-' to the params: it is
        // not a thin delegate, so the carrier fast-path must not suppress the
        // non-associative-operator scan.
        def err = shouldFail lenientShell, ALGEBRA + '''
            def m = new IntSum()
            [1, 2, 3].injectParallel(0) { int a, int b -> m.append(a, b); a - b }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void strict_annotated_method_reference_passes() {
        assertScript strictShell, '''
            import groovy.transform.Associative

            class Maths {
                @Associative
                static int add(int a, int b) { a + b }
            }

            assert [1, 2, 3, 4].sumParallel(Maths.&add) == 10
        '''
    }

    // === Monoid / Semigroup carrier recognition ===
    // Library-agnostic: matched by simple type name (Functional Java,
    // Palatable Lambda and Purefun all converge on Monoid/Semigroup).

    private static final String ALGEBRA = '''
        interface Semigroup<A> { A append(A a, A b) }
        interface Monoid<A> extends Semigroup<A> { A zero() }
        class IntSum implements Monoid<Integer> {
            Integer append(Integer a, Integer b) { a + b }
            Integer zero() { 0 }
        }
        class IntMax implements Semigroup<Integer> {
            Integer append(Integer a, Integer b) { Math.max(a, b) }
        }
    '''

    @Test
    void monoid_method_reference_accepted_lenient() {
        assertScript lenientShell, ALGEBRA + '''
            def m = new IntSum()
            assert [1, 2, 3].injectParallel(m.zero(), m.&append) == 6
        '''
    }

    @Test
    void monoid_method_reference_accepted_strict() {
        assertScript strictShell, ALGEBRA + '''
            def m = new IntSum()
            assert [1, 2, 3, 4].sumParallel(m.&append) == 10
        '''
    }

    @Test
    void semigroup_with_seeded_injectParallel_fails() {
        def err = shouldFail lenientShell, ALGEBRA + '''
            def s = new IntMax()
            [1, 2, 3].injectParallel(0, s.&append)
        '''
        assert err.message.contains('Semigroup')
        assert err.message.contains('identity')
    }

    @Test
    void semigroup_with_unseeded_sumParallel_accepted() {
        assertScript lenientShell, ALGEBRA + '''
            def s = new IntMax()
            assert [1, 5, 3].sumParallel(s.&append) == 5
        '''
    }

    @Test
    void delegating_closure_to_monoid_accepted_in_strict_mode() {
        assertScript strictShell, ALGEBRA + '''
            def m = new IntSum()
            assert [1, 2, 3].injectParallel(0) { a, b -> m.append(a, b) } == 6
        '''
    }

    // === JDK stream reductions (receiver-type gated, like RegexChecker) ===

    @Test
    void stream_reduce_non_associative_fails() {
        def err = shouldFail lenientShell, '''
            [1, 2, 3].stream().reduce(0) { int a, int b -> a - b }
        '''
        assert err.message.contains('non-associative')
    }

    @Test
    void stream_reduce_associative_passes() {
        assertScript lenientShell, '''
            assert [1, 2, 3].stream().reduce(0) { int a, int b -> a + b } == 6
        '''
    }

    @Test
    void stream_reduce_unseeded_optional_passes() {
        assertScript lenientShell, '''
            assert [1, 2, 3].stream().reduce { int a, int b -> a + b }.get() == 6
        '''
    }

    @Test
    void stream_reduce_monoid_carrier_accepted_strict() {
        assertScript strictShell, ALGEBRA + '''
            def m = new IntSum()
            assert [1, 2, 3].stream().reduce(m.zero(), m.&append) == 6
        '''
    }

    @Test
    void stream_reduce_semigroup_seeded_fails() {
        def err = shouldFail lenientShell, ALGEBRA + '''
            def s = new IntMax()
            [1, 2, 3].stream().reduce(0, s.&append)
        '''
        assert err.message.contains('Semigroup')
        assert err.message.contains('identity')
    }

    @Test
    void intstream_reduce_non_associative_fails() {
        def err = shouldFail lenientShell, '''
            import java.util.stream.IntStream
            IntStream.of(1, 2, 3).reduce(0) { int a, int b -> a - b }
        '''
        assert err.message.contains('non-associative')
    }
}
