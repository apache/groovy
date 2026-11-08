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
package org.codehaus.groovy.transform

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for nested-path {@code copyWith} (map form and transactional
 * block) on {@code @RecordType} classes declared with {@code copyWith=true},
 * including record-in-record graphs.
 */
final class RecordNestedCopyWithTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    private static final String GRAPH = '''
        @RecordType(copyWith = true) class Address { String city; String zip }
        @RecordType(copyWith = true) class Company { String name; Address address }
        @RecordType(copyWith = true) class Person  { String name; Address address; Company company }
        def p = new Person('Alice',
                            new Address('NYC', '10001'),
                            new Company('Acme', new Address('Dock St', '07030')))
    '''

    @Test
    void record_nested_map_set() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('address.city': 'Boston')
            assert q.address.city == 'Boston'
            assert q.address.zip  == '10001'
            assert q.name == 'Alice'
        '''
    }

    @Test
    void record_deep_record_in_record() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('company.address.city': 'Jersey City')
            assert q.company.address.city == 'Jersey City'
            assert q.company.address.zip  == '07030'
            assert q.company.name == 'Acme'
            assert p.company.address.city == 'Dock St'
        '''
    }

    @Test
    void record_block_nested_set() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith {
                name = 'Bob'
                company.address.zip = '07310'
            }
            assert q.name == 'Bob'
            assert q.company.address.zip == '07310'
            assert q.company.address.city == 'Dock St'
        '''
    }

    @Test
    void record_block_functional_modify() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { address.city.modify { it.toUpperCase() } }
            assert q.address.city == 'NYC'.toUpperCase()
            assert q.address.zip == '10001'
        '''
    }

    @Test
    void record_block_old_nested_path() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { company.address.city = old.company.address.city.reverse() }
            assert q.company.address.city == 'Dock St'.reverse()
            assert q.company.address.zip == '07030'
            assert q.name == 'Alice'
        '''
    }

    @Test
    void record_block_empty_is_identity() {
        assertScript shell, GRAPH + '''
            assert p.copyWith { }.is(p)
        '''
    }

    // GROOVY-12713: the static type checker accepts a dotted key when every segment is a component of the type
    // reached so far, checks the value against the last segment's type, and reports an unknown segment
    @Test
    void record_nested_keys_under_static_type_checking() {
        assertScript shell, GRAPH + '''
            @groovy.transform.CompileStatic
            Person moved(Person person) {
                person.copyWith('address.city': 'Boston', 'company.address.zip': '07031')
            }
            def q = moved(p)
            assert q.address.city == 'Boston'
            assert q.company.address.zip == '07031'
            assert q.company.name == 'Acme'
        '''
    }

    // GROOVY-12713
    @Test
    void record_nested_key_with_unknown_segment_is_a_static_type_error() {
        def err = shouldFail shell, GRAPH + '''
            @groovy.transform.CompileStatic
            Person moved(Person person) {
                person.copyWith('address.state': 'MA')
            }
            moved(p)
        '''
        assert err.message.contains("unexpected named arg: address.state")
    }

    // GROOVY-12713
    @Test
    void record_nested_key_value_is_type_checked_against_the_last_segment() {
        def err = shouldFail shell, GRAPH + '''
            @groovy.transform.CompileStatic
            Person moved(Person person) {
                person.copyWith('company.address.zip': 7031)
            }
            moved(p)
        '''
        assert err.message.contains("argument for named param 'company.address.zip' has type 'int' but expected 'java.lang.String'")
    }

    // GROOVY-12713: the block form under static type checking: the record type is the block's delegate, its
    // components can be assigned at any depth, old is the original and modify derives a value from the current one
    @Test
    void record_block_form_under_static_type_checking() {
        assertScript shell, GRAPH + '''
            @groovy.transform.CompileStatic
            Person moved(Person person) {
                person.copyWith {
                    name = old.name.toUpperCase()
                    address.city = 'Boston'
                    company.address.zip = '07031'
                    company.name.modify { it + ' Inc' }
                }
            }
            def q = moved(p)
            assert q.name == 'ALICE'
            assert q.address.city == 'Boston'
            assert q.address.zip == '10001'
            assert q.company.address.zip == '07031'
            assert q.company.name == 'Acme Inc'
            assert p.name == 'Alice'
        '''
    }

    @Test
    void record_block_form_type_checks_the_assigned_values() {
        def err = shouldFail shell, '''
            @RecordType(copyWith = true) class Dimensions { int width; int height }
            @RecordType(copyWith = true) class Box { String label; Dimensions size }
            @groovy.transform.CompileStatic
            Box widened(Box box) {
                box.copyWith { size.width = 'wide' }
            }
            widened(new Box('crate', new Dimensions(2, 3)))
        '''
        assert err.message.contains('Cannot assign value of type java.lang.String to variable of type int')
    }

    @Test
    void record_block_form_rejects_an_unknown_component() {
        def err = shouldFail shell, GRAPH + '''
            @groovy.transform.CompileStatic
            Person moved(Person person) {
                person.copyWith { address.state = 'MA' }
            }
            moved(p)
        '''
        assert err.message.contains('No such property: state for class: Address')
    }

    // GROOVY-12713
    @Test
    void dotted_key_is_not_a_path_for_other_named_param_methods() {
        def err = shouldFail shell, '''
            def opts(@NamedParam(value = 'a', type = String) Map m) { m }
            @groovy.transform.CompileStatic
            def test() {
                opts('a.b': 1)
            }
            test()
        '''
        assert err.message.contains('unexpected named arg: a.b')
    }
}
