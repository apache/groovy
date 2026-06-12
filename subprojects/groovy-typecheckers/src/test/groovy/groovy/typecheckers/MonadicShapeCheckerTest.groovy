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

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for {@link MonadicShapeChecker}. Mirrors the {@code CombinerCheckerTest}
 * harness: two shared shells, one lenient, one strict.
 */
final class MonadicShapeCheckerTest {

    private static GroovyShell lenientShell
    private static GroovyShell strictShell

    @BeforeAll
    static void setUp() {
        lenientShell = makeShell(null)
        strictShell = makeShell('strict')
    }

    private static GroovyShell makeShell(String mode) {
        String ext = mode ? "groovy.typecheckers.MonadicShapeChecker(mode: '${mode}')" : 'groovy.typecheckers.MonadicShapeChecker'
        new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: ext]
            addCompilationCustomizers(customizer)
        })
    }

    // ===== Optional =====

    @Test
    void optional_flatMap_returning_optional_passes() {
        assertScript lenientShell, '''
            assert Optional.of(2).flatMap { Integer x -> Optional.of(x + 1) }.get() == 3
        '''
    }

    @Test
    void optional_flatMap_returning_bare_value_fails() {
        // JDK's flatMap signature requires Optional but STC can miss closures
        // returning a bare value via Groovy SAM coercion.
        def err = shouldFail lenientShell, '''
            Optional.of(2).flatMap { Integer x -> x + 1 }
        '''
        assert err.message.contains("'flatMap' on java.util.Optional")
        assert err.message.contains('expects its function to return another java.util.Optional')
    }

    @Test
    void optional_flatMap_returning_stream_flags_cross_carrier() {
        def err = shouldFail lenientShell, '''
            import java.util.stream.Stream
            Optional.of(2).flatMap { Integer x -> Stream.of(x) }
        '''
        assert err.message.contains('crossing carrier types is almost certainly a bug')
    }

    @Test
    void optional_map_returning_optional_flags_nesting() {
        def err = shouldFail lenientShell, '''
            Optional.of(2).map { Integer x -> Optional.of(x + 1) }
        '''
        assert err.message.contains("'map' on java.util.Optional")
        assert err.message.contains("did you mean 'flatMap'")
    }

    @Test
    void optional_map_returning_plain_value_passes() {
        assertScript lenientShell, '''
            assert Optional.of(2).map { Integer x -> x + 1 }.get() == 3
        '''
    }

    // ===== Stream =====

    @Test
    void stream_flatMap_returning_stream_passes() {
        assertScript lenientShell, '''
            import java.util.stream.Stream
            assert Stream.of(1, 2).flatMap { Integer x -> Stream.of(x, x + 10) }.toList() == [1, 11, 2, 12]
        '''
    }

    @Test
    void stream_map_returning_stream_flags_nesting() {
        def err = shouldFail lenientShell, '''
            import java.util.stream.Stream
            Stream.of(1, 2).map { Integer x -> Stream.of(x) }
        '''
        assert err.message.contains("'map' on java.util.stream.Stream")
        assert err.message.contains("did you mean 'flatMap'")
    }

    // ===== CompletableFuture / CompletionStage =====

    @Test
    void cf_thenCompose_returning_cf_passes() {
        assertScript lenientShell, '''
            import java.util.concurrent.CompletableFuture
            def r = CompletableFuture.completedFuture(2)
                .thenCompose { Integer x -> CompletableFuture.completedFuture(x + 1) }
            assert r.get() == 3
        '''
    }

    @Test
    void cf_thenCompose_returning_bare_value_fails() {
        def err = shouldFail lenientShell, '''
            import java.util.concurrent.CompletableFuture
            CompletableFuture.completedFuture(2).thenCompose { Integer x -> x + 1 }
        '''
        assert err.message.contains("'thenCompose' on java.util.concurrent.CompletionStage")
    }

    @Test
    void cf_thenApply_returning_cf_flags_nesting() {
        def err = shouldFail lenientShell, '''
            import java.util.concurrent.CompletableFuture
            CompletableFuture.completedFuture(2).thenApply { Integer x -> CompletableFuture.completedFuture(x) }
        '''
        assert err.message.contains("'thenApply' on java.util.concurrent.CompletionStage")
        assert err.message.contains("did you mean 'thenCompose'")
    }

    // ===== @Monadic user-declared carriers =====

    @Test
    void monadic_annotated_carrier_bind_returning_carrier_passes() {
        assertScript lenientShell, '''
            import groovy.transform.Monadic
            @Monadic
            class Box<T> {
                final T v
                Box(T v) { this.v = v }
                Box flatMap(Closure c) { (Box) c.call(v) }
                Box map(Closure c) { new Box(c.call(v)) }
            }

            assert new Box(2).flatMap { Integer x -> new Box(x + 1) }.v == 3
        '''
    }

    @Test
    void monadic_annotated_carrier_map_returning_carrier_flags_nesting() {
        def err = shouldFail lenientShell, '''
            import groovy.transform.Monadic
            @Monadic
            class Box<T> {
                final T v
                Box(T v) { this.v = v }
                Box flatMap(Closure c) { (Box) c.call(v) }
                Box map(Closure c) { new Box(c.call(v)) }
            }

            new Box(2).map { Integer x -> new Box(x + 1) }
        '''
        assert err.message.contains("did you mean 'flatMap'")
    }

    @Test
    void monadic_with_custom_bind_map_names() {
        // @Monadic(bind='chain', map='transform') — verify the configured names are honoured.
        def err = shouldFail lenientShell, '''
            import groovy.transform.Monadic
            import java.util.function.Function

            @Monadic(bind = 'chain', map = 'transform')
            class Res {
                final Object v
                Res(Object v) { this.v = v }
                Res chain(Function f) { (Res) f.apply(v) }
                Res transform(Function f) { new Res(f.apply(v)) }
            }

            new Res(2).transform { x -> new Res(x) }
        '''
        assert err.message.contains("did you mean 'chain'")
    }

    // ===== modes =====

    @Test
    void lenient_does_not_flag_when_return_is_unknown() {
        // Closure passed as a variable — no static handle on its return type.
        assertScript lenientShell, '''
            Closure c = { Integer x -> Optional.of(x + 1) }
            Optional.of(2).flatMap(c)
        '''
    }

    @Test
    void strict_flags_when_return_is_unknown() {
        def err = shouldFail strictShell, '''
            Closure c = { Integer x -> Optional.of(x + 1) }
            Optional.of(2).flatMap(c)
        '''
        assert err.message.contains('cannot statically verify')
    }

    // ===== passthrough / non-engagement =====

    @Test
    void non_carrier_receiver_ignored() {
        // Compile-only: the assertion is the checker does NOT raise a static error
        // on a non-registered type, even though the shapes here (flatMap returning
        // bare, map returning the receiver) would be flagged on a carrier.
        lenientShell.parse '''
            class Holder<T> {
                final T v
                Holder(T v) { this.v = v }
                Holder flatMap(Closure c) { (Holder) c.call(v) }
                Holder map(Closure c) { new Holder(c.call(v)) }
            }
            void check() {
                new Holder(2).flatMap { Integer x -> x + 1 }
                new Holder(2).map { Integer x -> new Holder(x) }
            }
        '''
    }

    @Test
    void unrelated_map_call_ignored() {
        // List<E>.collect/Map<K,V>.collect are 'map'-like but not in the carrier
        // registry; the checker must not interfere.
        assertScript lenientShell, '''
            assert [1, 2, 3].collect { it + 1 } == [2, 3, 4]
        '''
    }

    @Test
    void comprehensions_dispatcher_calls_are_left_to_monadic_checker() {
        // A direct Comprehensions.bind/map call is MonadicChecker's job; this
        // checker must not double-flag a (correct) shape there.
        assertScript lenientShell, '''
            import org.apache.groovy.runtime.Comprehensions
            assert ((Optional<Integer>) Comprehensions.bind(Optional.of(2)) { Integer x ->
                Comprehensions.bind(Optional.of(3)) { Integer y -> Optional.of(x + y) }
            }).get() == 5
        '''
    }
}
