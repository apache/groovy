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
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
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
}
