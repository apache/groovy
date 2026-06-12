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
package org.apache.groovy.contracts.tests.post

import org.apache.groovy.contracts.PostconditionViolation
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.jupiter.api.Test

/**
 * GROOVY-12078: {@code old.<name>} should snapshot the entry value of a (non-{@code final})
 * method parameter, so a postcondition can compare against an argument that the body reassigns.
 *
 * @see groovy.contracts.Ensures
 */
final class OldParameterPostconditionTests extends BaseTestClass {

    @Test
    void swap_compares_result_against_old_parameter_values() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Swapper {
                @Ensures({ result.a == old.b && result.b == old.a })
                Map<String, Integer> swap(int a, int b) {
                    (a, b) = [b, a]
                    [a: a, b: b]
                }
            }
        '''
        assert instance.swap(1, 3) == [a: 3, b: 1]
    }

    @Test
    void type_checked_swap() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Swapper {
                @groovy.transform.TypeChecked
                @Ensures({ result.a == old.b && result.b == old.a })
                Map<String, Integer> swap(int a, int b) {
                    (a, b) = [b, a]
                    [a: a, b: b]
                }
            }
        '''
        assert instance.swap(1, 3) == [a: 3, b: 1]
    }

    @Test
    void old_holds_the_entry_value_not_the_reassigned_value() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Swapper {
                @Ensures({ old.a == 1 && old.b == 3 })
                Map<String, Integer> swap(int a, int b) {
                    (a, b) = [b, a]
                    [a: a, b: b]
                }
            }
        '''
        assert instance.swap(1, 3) == [a: 3, b: 1]
    }

    @Test
    void static_method_snapshots_old_parameter_value() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Swapper {
                @Ensures({ result.a == old.b && result.b == old.a })
                static Map<String, Integer> swap(int a, int b) {
                    (a, b) = [b, a]
                    [a: a, b: b]
                }
            }
        '''
        assert instance.swap(1, 3) == [a: 3, b: 1]
    }

    @Test
    void old_parameter_is_typed_under_type_checking() {
        // old.s.length() only resolves under @TypeChecked if old.s is typed as the parameter's type
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class A {
                @groovy.transform.TypeChecked
                @Ensures({ old.s.length() == result })
                int truncatedLength(String s) {
                    s = s + '!'
                    s.length() - 1
                }
            }
        '''
        assert instance.truncatedLength('abc') == 3
    }

    @Test
    void mutable_cloneable_parameter_is_defensively_copied() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Mutator {
                @Ensures({ old.list == [1, 2] && list == [1, 2, 3] })
                ArrayList grow(ArrayList list) {
                    list.add(3)
                    list
                }
            }
        '''
        def list = [1, 2]
        assert instance.grow(list) == [1, 2, 3]
    }

    @Test
    void final_parameter_is_still_snapshotted() {
        // 'final' prevents reassignment but not in-place mutation, so old.<name> must still capture
        // the entry state (defensively copied for a cloneable type)
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Mutator {
                @Ensures({ old.list == [1, 2] && list == [1, 2, 3] })
                ArrayList grow(final ArrayList list) {
                    list.add(3)
                    list
                }
            }
        '''
        assert instance.grow([1, 2]) == [1, 2, 3]
    }

    @Test
    void val_parameter_is_snapshotted() {
        // 'val' is an alias of 'final' (both set ACC_FINAL), so it is snapshotted the same way
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class A {
                @Ensures({ old.x == 1 && result == 2 })
                def inc(val x) {
                    x + 1
                }
            }
        '''
        assert instance.inc(1) == 2
    }

    @Test
    void user_defined_cloneable_parameter_is_defensively_copied() {
        // a Cloneable type outside the JDK is detected via ImmutablePropertyUtils, so its snapshot is
        // a real defensive copy even though its package is not one of the java.* value-like prefixes
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class A {
                Box makeBox(int v) { new Box(v) }

                @Ensures({ old.box.v == 1 && box.v == 2 })
                Box bump(Box box) {
                    box.v = 2
                    box
                }
            }

            class Box implements Cloneable {
                int v
                Box(int v) { this.v = v }
                @Override Object clone() { new Box(v) }
            }
        '''
        instance.bump(instance.makeBox(1))
    }

    @Test
    void violated_postcondition_reports_old_parameter_value() {
        def instance = create_instance_of '''
            package tests

            import groovy.contracts.*

            class Swapper {
                @Ensures({ result.a == old.a })
                Map<String, Integer> swap(int a, int b) {
                    (a, b) = [b, a]
                    [a: a, b: b]
                }
            }
        '''
        shouldFail(PostconditionViolation) {
            instance.swap(1, 3)
        }
    }
}
