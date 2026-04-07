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

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

class ModifiesTests extends BaseTestClass {

    @Test
    void modifies_alone_compiles_and_runs() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                List items = []

                @Modifies({ this.items })
                void addItem(String item) {
                    items.add(item)
                }
            }
        ''')
        instance.addItem('hello')
        assert instance.items == ['hello']
    }

    @Test
    void modifies_with_list_syntax() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                List items = []
                int count = 0

                @Modifies({ [this.items, this.count] })
                void addItem(String item) {
                    items.add(item)
                    count++
                }
            }
        ''')
        instance.addItem('hello')
        assert instance.items == ['hello']
        assert instance.count == 1
    }

    @Test
    void modifies_with_parameter_reference() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                @Modifies({ arr })
                void fillArray(int[] arr) {
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = i
                    }
                }
            }
        ''')
        def arr = new int[3]
        instance.fillArray(arr)
        assert arr == [0, 1, 2] as int[]
    }

    @Test
    void modifies_with_ensures_valid_old_reference() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                @Ensures({ old -> old.count < count })
                void increment() {
                    count++
                }
            }
        ''')
        instance.increment()
        assert instance.count == 1
    }

    @Test
    void modifies_with_ensures_valid_old_reference_list_syntax() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                List items = []
                int count = 0

                @Modifies({ [this.items, this.count] })
                @Ensures({ old -> old.count < count })
                void addItem(String item) {
                    items.add(item)
                    count++
                }
            }
        ''')
        instance.addItem('hello')
        assert instance.count == 1
    }

    @Test
    void modifies_with_ensures_invalid_old_reference_causes_compile_error() {
        shouldFail MultipleCompilationErrorsException, {
            add_class_to_classpath('''
                import groovy.contracts.*

                class A {
                    List items = []
                    int count = 0

                    @Modifies({ this.items })
                    @Ensures({ old -> old.count == count })
                    void addItem(String item) {
                        items.add(item)
                    }
                }
            ''')
        }
    }

    @Test
    void ensures_without_modifies_no_error() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                int count = 0

                @Ensures({ old -> old.count < count })
                void increment() {
                    count++
                }
            }
        ''')
        instance.increment()
        assert instance.count == 1
    }

    @Test
    void modifies_without_ensures_no_error() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                int count = 0

                @Modifies({ this.count })
                void increment() {
                    count++
                }
            }
        ''')
        instance.increment()
        assert instance.count == 1
    }

    @Test
    void multiple_modifies_via_repeatable() {
        def instance = create_instance_of('''
            import groovy.contracts.*

            class A {
                List items = []
                int count = 0

                @Modifies({ this.items })
                @Modifies({ this.count })
                @Ensures({ old -> old.count < count })
                void addItem(String item) {
                    items.add(item)
                    count++
                }
            }
        ''')
        instance.addItem('hello')
        assert instance.count == 1
    }

    @Test
    void modifies_with_nonexistent_field_causes_compile_error() {
        shouldFail MultipleCompilationErrorsException, {
            add_class_to_classpath('''
                import groovy.contracts.*

                class A {
                    List items = []

                    @Modifies({ this.nonExistent })
                    void addItem(String item) {
                        items.add(item)
                    }
                }
            ''')
        }
    }

    @Test
    void modifies_with_nonexistent_param_causes_compile_error() {
        shouldFail MultipleCompilationErrorsException, {
            add_class_to_classpath('''
                import groovy.contracts.*

                class A {
                    @Modifies({ noSuchParam })
                    void addItem(String item) {
                    }
                }
            ''')
        }
    }
}
