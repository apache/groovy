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

final class ModifiesCheckerTest {

    private static GroovyShell shell

    @BeforeAll
    static void setUp() {
        shell = new GroovyShell(new CompilerConfiguration().tap {
            def customizer = new ASTTransformationCustomizer(groovy.transform.TypeChecked)
            customizer.annotationParameters = [extensions: 'groovy.typecheckers.ModifiesChecker']
            addCompilationCustomizers(customizer)
        })
    }

    // === Direct field assignments ===

    @Test
    void assignment_to_declared_field_passes() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                void increment() {
                    count++
                }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }

    @Test
    void assignment_to_undeclared_field_fails() {
        def err = shouldFail shell, '''
            import groovy.contracts.*

            class A {
                int count = 0
                int other = 0

                @Modifies({ this.count })
                void broken() {
                    other = 5
                }
            }
        '''
        assert err.message.contains('@Modifies violation')
        assert err.message.contains('other')
    }

    @Test
    void explicit_this_assignment_to_undeclared_field_fails() {
        def err = shouldFail shell, '''
            import groovy.contracts.*

            class A {
                int count = 0
                int other = 0

                @Modifies({ this.count })
                void broken() {
                    this.other = 5
                }
            }
        '''
        assert err.message.contains('@Modifies violation')
        assert err.message.contains('other')
    }

    @Test
    void local_variable_assignment_always_ok() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                void increment() {
                    def temp = count + 1
                    count = temp
                }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }

    // === Method calls on parameters ===

    @Test
    void mutating_call_on_modifiable_param_passes() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                @Modifies({ items })
                void addItem(List items, String item) {
                    items.add(item)
                }
            }
            def list = []
            new A().addItem(list, 'hello')
            assert list == ['hello']
        '''
    }

    @Test
    void non_mutating_call_on_non_modifiable_param_passes() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                void countSize(List items) {
                    count = items.size()
                }
            }
            def a = new A()
            a.countSize([1, 2, 3])
            assert a.count == 3
        '''
    }

    @Test
    void immutable_receiver_always_safe() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                String result

                @Modifies({ this.result })
                void process(String input) {
                    result = input.toUpperCase()
                }
            }
            def a = new A()
            a.process('hello')
            assert a.result == 'HELLO'
        '''
    }

    @Test
    void safe_method_name_on_non_modifiable_passes() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                void countItems(List items) {
                    count = items.size()
                    def s = items.toString()
                    def has = items.contains('x')
                    def empty = items.isEmpty()
                }
            }
            new A().countItems([])
        '''
    }

    // === Calls on this ===

    @Test
    void call_to_pure_method_on_this_passes() {
        assertScript shell, '''
            import groovy.contracts.*
            import groovy.transform.Pure

            class A {
                int count = 0

                @Modifies({ this.count })
                void increment() {
                    count = currentCount() + 1
                }

                @Pure
                int currentCount() {
                    return count
                }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }

    @Test
    void call_to_method_with_compatible_modifies_passes() {
        assertScript shell, '''
            import groovy.contracts.*

            class A {
                List items = []
                int count = 0

                @Modifies({ [this.items, this.count] })
                void addItem(String item) {
                    doAdd(item)
                }

                @Modifies({ [this.items, this.count] })
                private void doAdd(String item) {
                    items.add(item)
                    count++
                }
            }
            def a = new A()
            a.addItem('hello')
            assert a.count == 1
        '''
    }

    // === @Pure implies @Modifies({}) ===

    @Test
    void pure_method_with_no_field_writes_passes() {
        assertScript shell, '''
            import groovy.transform.Pure

            class A {
                int count = 0

                @Pure
                int currentCount() {
                    return count
                }
            }
            assert new A().currentCount() == 0
        '''
    }

    @Test
    void pure_method_with_field_write_fails() {
        def err = shouldFail shell, '''
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
        assert err.message.contains('@Modifies violation')
    }

    // === No @Modifies or @Pure — checker is silent ===

    @Test
    void no_modifies_annotation_no_checking() {
        assertScript shell, '''
            class A {
                int count = 0
                int other = 0

                void doAnything() {
                    count = 1
                    other = 2
                }
            }
            def a = new A()
            a.doAnything()
            assert a.count == 1
        '''
    }

    // === @SideEffectFree implies @Modifies({}) ===

    @Test
    void side_effect_free_method_checked_for_field_writes() {
        def err = shouldFail shell, '''
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
        assert err.message.contains('@Modifies violation')
    }

    @Test
    void side_effect_free_method_without_writes_passes() {
        assertScript shell, '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface SideEffectFree {}

            class A {
                int value = 42

                @SideEffectFree
                int doubled() { return value * 2 }
            }
            assert new A().doubled() == 84
        '''
    }

    // === @Contract(pure=true) implies @Modifies({}) ===

    @Test
    void contract_pure_implies_empty_modifies() {
        def err = shouldFail shell, '''
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
        assert err.message.contains('@Modifies violation')
    }

    // === @Contract(mutates=...) on callees ===

    @Test
    void contract_mutates_empty_is_safe() {
        assertScript shell, '''
            import groovy.contracts.*
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

                @Modifies({ this.count })
                void increment() {
                    count = helper(count)
                }

                @Contract(mutates = "")
                int helper(int x) { return x + 1 }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }

    @Test
    void contract_mutates_this_warns_on_callee() {
        def err = shouldFail shell, '''
            import groovy.contracts.*
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
                int cache = 0

                @Modifies({ this.count })
                void increment() {
                    count++
                    updateCache()
                }

                @Contract(mutates = "this")
                void updateCache() { cache = count * 2 }
            }
        '''
        assert err.message.contains('@Modifies warning')
        assert err.message.contains('mutates')
    }

    @Test
    void contract_mutates_param_warns_when_arg_not_in_modifies() {
        def err = shouldFail shell, '''
            import groovy.contracts.*
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                List items = []
                List other = []

                @Modifies({ this.items })
                void process() {
                    addToList(other, 'x')
                }

                @Contract(mutates = "param1")
                static void addToList(List list, String item) { list.add(item) }
            }
        '''
        assert err.message.contains('@Modifies warning')
        assert err.message.contains("'other'")
    }

    @Test
    void contract_mutates_param_passes_when_arg_in_modifies() {
        assertScript shell, '''
            import groovy.contracts.*
            import java.lang.annotation.*

            @Retention(RetentionPolicy.CLASS)
            @Target(ElementType.METHOD)
            @interface Contract {
                String value() default ""
                boolean pure() default false
                String mutates() default ""
            }

            class A {
                List items = []

                @Modifies({ this.items })
                void process() {
                    addToList(items, 'x')
                }

                @Contract(mutates = "param1")
                static void addToList(List list, String item) { list.add(item) }
            }
            def a = new A()
            a.process()
            assert a.items == ['x']
        '''
    }

    // === Callee recognition of @SideEffectFree and @Contract(pure=true) ===

    @Test
    void callee_with_side_effect_free_is_safe() {
        assertScript shell, '''
            import groovy.contracts.*
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target(ElementType.METHOD)
            @interface SideEffectFree {}

            class A {
                int count = 0

                @Modifies({ this.count })
                void increment() {
                    count = helper(count)
                }

                @SideEffectFree
                int helper(int x) { return x + 1 }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }

    @Test
    void callee_with_contract_pure_is_safe() {
        assertScript shell, '''
            import groovy.contracts.*
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

                @Modifies({ this.count })
                void increment() {
                    count = helper(count)
                }

                @Contract(pure = true)
                int helper(int x) { return x + 1 }
            }
            def a = new A()
            a.increment()
            assert a.count == 1
        '''
    }
}
