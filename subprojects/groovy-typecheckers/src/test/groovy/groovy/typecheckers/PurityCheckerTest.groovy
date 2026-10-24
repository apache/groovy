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
}
