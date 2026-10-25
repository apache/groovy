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

final class PurityCheckerTest {

    private static GroovyShell strictShell
    private static GroovyShell loggingShell
    private static GroovyShell nondetShell
    private static GroovyShell ioShell

    @BeforeAll
    static void setUp() {
        strictShell = makeShell('')
        loggingShell = makeShell('LOGGING')
        nondetShell = makeShell('NONDETERMINISM')
        ioShell = makeShell('IO')
    }

    private static GroovyShell makeShell(String allows) {
        String ext = allows ? "groovy.typecheckers.PurityChecker(allows: '${allows}')" : 'groovy.typecheckers.PurityChecker'
        new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(TypeChecked)
            customizer.annotationParameters = [extensions: ext]
            addCompilationCustomizers(customizer)
        })
    }

    // === Field mutation checks ===

    @Test
    void pure_method_with_no_side_effects_passes() {
        assertScript strictShell, '''
            import groovy.transform.Pure

            class A {
                int value = 42

                @Pure
                int doubled() {
                    return value * 2
                }
            }
            assert new A().doubled() == 84
        '''
    }

    @Test
    void pure_method_with_field_write_fails() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                int count = 0

                @Pure
                int increment() {
                    count++
                    return count
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('field assignment')
    }

    @Test
    void pure_method_writing_to_parameter_property_fails() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class Item { int value }

            class A {
                @Pure
                void broken(Item item) {
                    item.value = 42
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('property assignment')
    }

    // === Calling other pure methods ===

    @Test
    void pure_method_calling_another_pure_method_passes() {
        assertScript strictShell, '''
            import groovy.transform.Pure

            class A {
                int value = 42

                @Pure
                int doubled() { return value * 2 }

                @Pure
                int quadrupled() { return doubled() * 2 }
            }
            assert new A().quadrupled() == 168
        '''
    }

    @Test
    void pure_method_calling_memoized_method_passes() {
        assertScript strictShell, '''
            import groovy.transform.Pure
            import groovy.transform.Memoized

            class A {
                @Memoized
                int expensiveCompute(int x) { return x * x }

                @Pure
                int useComputed(int x) { return expensiveCompute(x) + 1 }
            }
            assert new A().useComputed(5) == 26
        '''
    }

    // === Immutable receiver types ===

    @Test
    void calls_on_immutable_types_always_pass() {
        assertScript strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                String process(String input) {
                    return input.toUpperCase().trim().substring(1)
                }
            }
            assert new A().process(' HELLO') == 'ELLO'
        '''
    }

    @Test
    void calls_on_bigdecimal_pass() {
        assertScript strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                BigDecimal calculate(BigDecimal price, BigDecimal tax) {
                    return price.multiply(tax)
                }
            }
            assert new A().calculate(100.0, 0.1) == 10.0
        '''
    }

    // === Known pure methods on mutable types ===

    @Test
    void known_pure_methods_on_collections_pass() {
        assertScript strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                int countItems(List items) {
                    return items.size()
                }

                @Pure
                boolean hasItem(List items, Object item) {
                    return items.contains(item)
                }
            }
            assert new A().countItems([1,2,3]) == 3
            assert new A().hasItem([1,2,3], 2)
        '''
    }

    // === NONDETERMINISM ===

    @Test
    void system_nanotime_fails_in_strict_mode() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                long getTimestamp() {
                    return System.nanoTime()
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('non-deterministic')
    }

    @Test
    void system_nanotime_passes_when_nondeterminism_allowed() {
        assertScript nondetShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                long getTimestamp() {
                    return System.nanoTime()
                }
            }
            new A().getTimestamp()
        '''
    }

    @Test
    void math_random_fails_in_strict_mode() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                double randomValue() {
                    return Math.random()
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('non-deterministic')
    }

    @Test
    void new_date_fails_in_strict_mode() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                Date now() {
                    return new Date()
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('non-deterministic')
    }

    @Test
    void new_date_passes_when_nondeterminism_allowed() {
        assertScript nondetShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                Date now() {
                    return new Date()
                }
            }
            new A().now()
        '''
    }

    // === LOGGING ===

    @Test
    void println_fails_in_strict_mode() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                int compute(int x) {
                    println("computing $x")
                    return x * 2
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('logging')
    }

    @Test
    void println_passes_when_logging_allowed() {
        assertScript loggingShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                int compute(int x) {
                    println("computing $x")
                    return x * 2
                }
            }
            assert new A().compute(5) == 10
        '''
    }

    // === IO ===

    @Test
    void file_constructor_fails_in_strict_mode() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                boolean fileExists(String path) {
                    return new File(path).exists()
                }
            }
        '''
        assert err.message.contains('@Pure violation')
        assert err.message.contains('I/O')
    }

    @Test
    void file_constructor_passes_when_io_allowed() {
        assertScript ioShell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                boolean fileExists(String path) {
                    return new File(path).exists()
                }
            }
            new A().fileExists('/nonexistent')
        '''
    }

    // === Combined allows ===

    @Test
    void combined_allows_logging_and_nondeterminism() {
        def shell = makeShell('LOGGING|NONDETERMINISM')
        assertScript shell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                long computeWithLogging(int x) {
                    println("computing at ${System.nanoTime()}")
                    return x * 2L
                }
            }
            new A().computeWithLogging(5)
        '''
    }

    @Test
    void combined_allows_still_flags_disallowed_categories() {
        def shell = makeShell('LOGGING')
        def err = shouldFail shell, '''
            import groovy.transform.Pure

            class A {
                @Pure
                long computeWithTime() {
                    println("starting")
                    return System.nanoTime()
                }
            }
        '''
        assert err.message.contains('non-deterministic')
    }

    // === No annotation — checker is silent ===

    @Test
    void no_pure_annotation_no_checking() {
        assertScript strictShell, '''
            class A {
                int count = 0

                void doAnything() {
                    count++
                    println("modified")
                    System.nanoTime()
                }
            }
            new A().doAnything()
        '''
    }

    // === @Memoized methods are checked too ===

    @Test
    void memoized_method_with_field_write_fails() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Memoized

            class A {
                int callCount = 0

                @Memoized
                int compute(int x) {
                    callCount++
                    return x * x
                }
            }
        '''
        assert err.message.contains('@Pure violation')
    }

    // === @SideEffectFree ===

    @Test
    void side_effect_free_method_allows_nondeterminism_implicitly() {
        assertScript strictShell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface SideEffectFree {}

            class A {
                @SideEffectFree
                long timestamped(int x) {
                    return x + System.nanoTime()
                }
            }
            new A().timestamped(1)
        '''
    }

    @Test
    void side_effect_free_method_still_rejects_field_writes() {
        def err = shouldFail strictShell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface SideEffectFree {}

            class A {
                int count = 0

                @SideEffectFree
                int broken() {
                    count++
                    return count
                }
            }
        '''
        assert err.message.contains('@Pure violation')
    }

    // === @Contract(pure = true) ===

    @Test
    void contract_pure_method_is_checked() {
        assertScript strictShell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                int value = 42

                @Contract(pure = true)
                int doubled() {
                    return value * 2
                }
            }
            assert new A().doubled() == 84
        '''
    }

    @Test
    void contract_pure_method_rejects_field_writes() {
        def err = shouldFail strictShell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                int count = 0

                @Contract(pure = true)
                int broken() {
                    count++
                    return count
                }
            }
        '''
        assert err.message.contains('@Pure violation')
    }

    @Test
    void contract_without_pure_is_not_checked() {
        assertScript strictShell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                int count = 0

                @Contract(value = "_ -> !null")
                int notPure() {
                    count++
                    return count
                }
            }
            assert new A().notPure() == 1
        '''
    }

    // === Callee recognition ===

    @Test
    void pure_method_can_call_side_effect_free_callee() {
        assertScript strictShell, '''
            import groovy.transform.Pure
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface SideEffectFree {}

            class A {
                @SideEffectFree
                int helper(int x) { return x * 2 }

                @Pure
                int compute(int x) { return helper(x) + 1 }
            }
            assert new A().compute(5) == 11
        '''
    }

    @Test
    void pure_method_can_call_contract_pure_callee() {
        assertScript strictShell, '''
            import groovy.transform.Pure
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                @Contract(pure = true)
                int helper(int x) { return x * 2 }

                @Pure
                int compute(int x) { return helper(x) + 1 }
            }
            assert new A().compute(5) == 11
        '''
    }

    // === @Pure parameters and locals (GROOVY-12460) ===

    private static final String PIPELINE = '''
        import groovy.transform.Pure
        import java.util.function.Function

        class Maths {
            @Pure static int square(int x) { x * x }
            static int noisy(int x) { println x; x }
        }

        class Pipeline {
            @Pure
            static int applyTwice(@Pure Closure<Integer> f, int x) { f(f.call(x)) }

            @Pure
            static int applyFn(@Pure Function<Integer, Integer> f, int x) { f.apply(x) }
        }
    '''

    @Test
    void pure_method_may_invoke_pure_closure_parameter() {
        assertScript strictShell, PIPELINE + '''
            assert Pipeline.applyTwice({ int v -> v + 1 }, 3) == 5
            assert Pipeline.applyFn((Integer v) -> v * 3, 3) == 9
        '''
    }

    @Test
    void pure_method_may_not_invoke_unannotated_closure_parameter() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure
                static int applyTwice(Closure<Integer> f, int x) { f(f(x)) }
            }
        '''
        assert err.message.contains("call to 'f.call()'")
    }

    @Test
    void closure_with_captured_write_rejected_for_pure_parameter() {
        def err = shouldFail strictShell, PIPELINE + '''
            int counter = 0
            Pipeline.applyTwice({ int v -> counter++; v }, 3)
        '''
        assert err.message.contains("@Pure violation in closure passed to @Pure parameter 'f': write to captured variable 'counter'")
    }

    @Test
    void closure_with_own_locals_accepted_for_pure_parameter() {
        assertScript strictShell, PIPELINE + '''
            assert Pipeline.applyTwice({ int v -> int w = v; w += 1; w }, 3) == 5
        '''
    }

    @Test
    void closure_with_logging_rejected_for_pure_parameter() {
        def err = shouldFail strictShell, PIPELINE + '''
            Pipeline.applyTwice({ int v -> println v; v }, 3)
        '''
        assert err.message.contains("@Pure violation in closure passed to @Pure parameter 'f': 'println()' is a logging/output call")
    }

    @Test
    void closure_with_logging_accepted_for_pure_parameter_when_allowed() {
        assertScript loggingShell, PIPELINE + '''
            assert Pipeline.applyTwice({ int v -> println v; v }, 3) == 3
        '''
    }

    @Test
    void closure_with_nondeterminism_or_io_rejected_for_pure_parameter() {
        def err = shouldFail strictShell, PIPELINE + '''
            Pipeline.applyTwice({ int v -> v + (int) System.nanoTime() }, 3)
        '''
        assert err.message.contains("closure passed to @Pure parameter 'f': 'java.lang.System.nanoTime()' is non-deterministic")
        err = shouldFail strictShell, PIPELINE + '''
            Pipeline.applyTwice({ int v -> new File('x').text.size() + v }, 3)
        '''
        assert err.message.contains("closure passed to @Pure parameter 'f': 'new File(...)' is an I/O operation")
    }

    @Test
    void lambda_rejected_like_closure_for_pure_parameter() {
        def err = shouldFail strictShell, PIPELINE + '''
            Pipeline.applyFn((Integer v) -> { println v; v }, 3)
        '''
        assert err.message.contains("closure passed to @Pure parameter 'f': 'println()' is a logging/output call")
    }

    @Test
    void method_reference_to_pure_method_accepted_for_pure_parameter() {
        assertScript strictShell, PIPELINE + '''
            assert Pipeline.applyTwice(Maths.&square, 3) == 81
            assert Pipeline.applyFn(Maths::square, 3) == 9
        '''
    }

    @Test
    void method_reference_to_unannotated_method_rejected_for_pure_parameter() {
        def err = shouldFail strictShell, PIPELINE + '''
            Pipeline.applyTwice(Maths.&noisy, 3)
        '''
        assert err.message.contains("method reference passed to @Pure parameter 'f' does not refer to a @Pure method")
    }

    @Test
    void pure_local_accepted_and_unannotated_local_rejected_for_pure_parameter() {
        assertScript strictShell, PIPELINE + '''
            @Pure Closure<Integer> inc = { int v -> v + 1 }
            assert Pipeline.applyTwice(inc, 3) == 5
        '''
        def err = shouldFail strictShell, PIPELINE + '''
            Closure<Integer> inc = { int v -> v + 1 }
            Pipeline.applyTwice(inc, 3)
        '''
        assert err.message.contains("'inc' passed to @Pure parameter 'f' is not declared @Pure")
    }

    @Test
    void pure_local_is_checked_where_assigned() {
        def err = shouldFail strictShell, PIPELINE + '''
            @Pure Closure<Integer> inc = { int v -> println v; v + 1 }
        '''
        assert err.message.contains("closure assigned to @Pure variable 'inc': 'println()' is a logging/output call")
        err = shouldFail strictShell, PIPELINE + '''
            @Pure Closure<Integer> inc = { int v -> v + 1 }
            inc = { int v -> println v; v }
        '''
        assert err.message.contains("closure assigned to @Pure variable 'inc': 'println()' is a logging/output call")
    }

    @Test
    void pure_parameter_may_be_passed_on() {
        assertScript strictShell, PIPELINE + '''
            class Twice {
                @Pure
                static int run(@Pure Closure<Integer> g, int x) { Pipeline.applyTwice(g, x) }
            }
            assert Twice.run({ int v -> v * 2 }, 1) == 4
        '''
    }

    @Test
    void closure_argument_violation_reported_once_inside_pure_method() {
        def err = shouldFail strictShell, PIPELINE + '''
            class Caller {
                @Pure
                static int run(int x) { Pipeline.applyTwice({ int v -> println v; v }, x) }
            }
        '''
        assert err.message.count("'println()' is a logging/output call") == 1
    }

    // === Closure-taking GDK and stream methods ===

    @Test
    void pure_method_may_use_closure_taking_gdk_methods() {
        assertScript strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static List<Integer> doubled(List<Integer> xs) { xs.collect { int v -> v * 2 } }
                @Pure static int total(List<Integer> xs) { xs.inject(0) { int a, int b -> a + b } }
                @Pure static List<Integer> evens(List<Integer> xs) { xs.findAll { int v -> v % 2 == 0 }.toSorted() }
                @Pure static boolean anyBig(List<Integer> xs) { xs.any { int v -> v > 10 } }
                @Pure static int squares(List<Integer> xs) {
                    int sum = 0
                    xs.each { int v -> sum += v * v }   // writes a local of the @Pure method: fine
                    sum
                }
            }
            assert A.doubled([1, 2]) == [2, 4]
            assert A.total([1, 2, 3]) == 6
            assert A.evens([4, 1, 2]) == [2, 4]
            assert !A.anyBig([1, 2])
            assert A.squares([1, 2]) == 5
        '''
    }

    @Test
    void pure_method_may_use_stream_pipelines() {
        assertScript strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static List<Integer> doubled(List<Integer> xs) {
                    xs.stream().map(v -> v * 2).filter(v -> v > 2).toList()
                }
            }
            assert A.doubled([1, 2, 3]) == [4, 6]
        '''
    }

    @Test
    void impure_closure_body_in_gdk_call_reported_for_body_only() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static void shout(List<Integer> xs) { xs.each { int v -> println v } }
            }
        '''
        assert err.message.contains("'println()' is a logging/output call")
        assert !err.message.contains("xs.each()")
    }

    @Test
    void gdk_call_mutating_parameter_in_closure_rejected() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static int leak(List<Integer> xs, List<Integer> sink) {
                    xs.each { int v -> sink.add(v) }
                    xs.size()
                }
            }
        '''
        assert err.message.contains("call to 'sink.add()'")
    }

    @Test
    void gdk_call_with_unannotated_closure_variable_rejected() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static List<Integer> mapAll(List<Integer> xs, Closure<Integer> f) { xs.collect(f) }
            }
        '''
        assert err.message.contains("call to 'xs.collect()'")
        assertScript strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static List<Integer> mapAll(List<Integer> xs, @Pure Closure<Integer> f) { xs.collect(f) }
            }
            assert A.mapAll([1, 2]) { int v -> v + 1 } == [2, 3]
        '''
    }

    @Test
    void mutating_gdk_sort_still_rejected() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static List<Integer> sorted(List<Integer> xs) { xs.sort { int a, int b -> a <=> b } }
            }
        '''
        assert err.message.contains("call to 'xs.sort()'")
    }

    // === Mutating operators (GROOVY-12464) ===

    private static final String OPERATOR_TYPES = '''
        import groovy.transform.OperatorRename
        import groovy.transform.Pure

        class Acc {
            int total
            void plusAssign(int v) { total += v }
            void leftShiftAssign(int v) { total += v }
            void addInPlace(int v) { total += v }
        }
        class PureAcc {
            @Pure void plusAssign(int v) { }
            @Pure PureAcc leftShift(int v) { this }
        }
        class Plain {
            int total
            Plain plus(int v) { new Plain(total: total + v) }
        }
        class Holder {
            List<Integer> list = []
        }
    '''

    @Test
    void mutating_operators_on_non_local_receivers_rejected() {
        [
            '@Pure static int f(List<Integer> s, int v) { s << v; v }'                 : "'<<' on 's' calls leftShift()",
            '@Pure static int f(StringBuilder sb, String v) { sb << v; 0 }'            : "'<<' on 'sb' calls leftShift()",
            '@Pure static int f(List<Integer> s, int v) { s[0] = v; v }'               : "'[]=' on 's' calls putAt()",
            '@Pure static int f(Map<String, Integer> m, int v) { m["k"] = v; v }'      : "'[]=' on 'm' calls putAt()",
            '@Pure static int f(int[] arr, int v) { arr[0] = v; v }'                   : "'[]=' on 'arr' calls putAt()",
            '@Pure static int f(List<Integer> s) { s[0]++; 0 }'                        : "'[]++' on 's' calls putAt()",
            '@Pure static int f(List<Integer> s) { --s[0]; 0 }'                        : "'[]--' on 's' calls putAt()",
            '@Pure static int f(List<Integer> s, int v) { s[0] += v; v }'              : "'[]+=' on 's' calls putAt()",
            'class P { static List<Integer> items = []; @Pure static int f(int v) { items << v; v } }' : "'<<' on 'items' calls leftShift()",
            'class P { List<Integer> list = []; @Pure int f(int v) { this.list << v; v } }'            : "'<<' on 'list' calls leftShift()",
            '@Pure static int f(Holder h, int v) { h.list << v; v }'                   : "'<<' on 'h.list' calls leftShift()",
        ].each { body, expected ->
            def err = shouldFail strictShell, OPERATOR_TYPES + body
            assert err.message.contains('@Pure warning')
            assert err.message.contains(expected)
        }
    }

    @Test
    void compound_assignment_resolved_to_assign_method_rejected() {
        [
            '@Pure static int f(Acc a, int v) { a += v; v }'                           : "'+=' on 'a' calls plusAssign()",
            '@Pure static int f(Acc a, int v) { a <<= v; v }'                          : "'<<=' on 'a' calls leftShiftAssign()",
            'class P { Acc acc = new Acc(); @Pure int f(int v) { acc += v; v } }'      : "'+=' on 'acc' calls plusAssign()",
            '@Pure @OperatorRename(plusAssign = "addInPlace") static int f(Acc a, int v) { a += v; v }' : "'compound assignment' on 'a' calls addInPlace()",
        ].each { body, expected ->
            def err = shouldFail strictShell, OPERATOR_TYPES + body
            assert err.message.contains('@Pure warning')
            assert err.message.contains(expected)
        }
    }

    @Test
    void mutating_operators_named_forms_and_field_reassignment_still_rejected() {
        [
            '@Pure static int f(List<Integer> s, int v) { s.add(v); v }'               : "call to 's.add()'",
            '@Pure static int f(Acc a, int v) { a.plusAssign(v); v }'                  : "call to 'a.plusAssign()'",
            '@Pure static int f(Map<String, Integer> m, int v) { m.k = v; v }'         : "property assignment to 'm.k'",
            'class P { static List<Integer> items = []; @Pure static int f(int v) { items += v; v } }' : "field assignment to 'items'",
        ].each { body, expected ->
            def err = shouldFail strictShell, OPERATOR_TYPES + body
            assert err.message.contains(expected)
        }
    }

    @Test
    void operators_that_do_not_mutate_a_non_local_receiver_pass() {
        assertScript strictShell, OPERATOR_TYPES + '''
            class P {
                // no *Assign method for List or Plain: rebinds the parameter, the caller's object is unchanged
                @Pure static int rebindList(List<Integer> s, int v) { s += v; s.size() }
                @Pure static int rebindPlain(Plain a, int v) { a += v; a.total }
                @Pure static BigDecimal sum(BigDecimal a, BigDecimal b) { a + b }
                @Pure static int shift(int x) { x << 2 }
                // the callee is @Pure
                @Pure static int pureOperators(PureAcc a, int v) { a += v; a << v; v }
                // building up a value the method owns
                @Pure static int localList(int v) { List<Integer> s = []; s << v; s[0] = v; s[0]++; s.size() }
                @Pure static int localAcc(int v) { Acc a = new Acc(); a += v; a.total }
                @Pure static String localBuilder(String v) { def sb = new StringBuilder(); sb << v; sb.toString() }
            }
            def caller = [1]
            assert P.rebindList(caller, 2) == 2
            assert caller == [1]
            assert P.rebindPlain(new Plain(total: 1), 2) == 3
            assert P.sum(1.5G, 2.5G) == 4.0G
            assert P.shift(1) == 4
            assert P.pureOperators(new PureAcc(), 1) == 1
            assert P.localList(3) == 1
            assert P.localAcc(3) == 3
            assert P.localBuilder('x') == 'x'
        '''
    }

    @Test
    void mutating_operators_follow_the_allowed_effects() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static int write(Writer w) { w << 'x'; 0 }
            }
        '''
        assert err.message.contains("'<<' (leftShift()) is an I/O operation (allow with IO)")
        assertScript ioShell, '''
            import groovy.transform.Pure
            class A {
                @Pure static int write(Writer w) { w << 'x'; 0 }
            }
            def w = new StringWriter()
            assert A.write(w) == 0
            assert w.toString() == 'x'
        '''
    }

    @Test
    void mutating_operators_in_pure_closures() {
        def err = shouldFail strictShell, '''
            import groovy.transform.Pure
            class A {
                static int apply(@Pure Closure<Integer> c) { c(1) }
                static int f() { List<Integer> out = []; apply { int x -> out << x; x } }
            }
        '''
        assert err.message.contains("'<<' on 'out' calls leftShift()")
        assertScript strictShell, '''
            import groovy.transform.Pure
            class A {
                static int apply(@Pure Closure<Integer> c) { c(1) }
                static int f() { apply { int x -> List<Integer> l = []; l << x; l.size() } }
            }
            assert A.f() == 1
        '''
    }
}
