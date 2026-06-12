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
}
