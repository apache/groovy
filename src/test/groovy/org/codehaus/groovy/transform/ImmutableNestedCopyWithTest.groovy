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
 * Tests for nested-path {@code copyWith} (e.g.
 * {@code person.copyWith('address.city': 'NYC')}) on {@code @Immutable}
 * classes declared with {@code copyWith=true}.
 */
final class ImmutableNestedCopyWithTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports { star 'groovy.transform' }
    }

    private static final String GRAPH = '''
        @Immutable(copyWith = true) class Address { String city; String zip }
        @Immutable(copyWith = true) class Company { String name; Address address }
        @Immutable(copyWith = true) class Person  { String name; Address address; Company company }
        def p = new Person('Alice',
                            new Address('NYC', '10001'),
                            new Company('Acme', new Address('Dock St', '07030')))
    '''

    @Test
    void single_level_nested_update() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('address.city': 'Boston')
            assert q.address.city == 'Boston'
            assert q.address.zip  == '10001'   // sibling preserved
            assert q.name         == 'Alice'   // unrelated preserved
            assert p.address.city == 'NYC'     // original untouched
        '''
    }

    @Test
    void multiple_keys_same_nested_node_merged() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('address.city': 'Boston', 'address.zip': '02101')
            assert q.address.city == 'Boston'
            assert q.address.zip  == '02101'
        '''
    }

    @Test
    void mixed_top_level_and_nested() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith(name: 'Bob', 'address.city': 'LA')
            assert q.name == 'Bob'
            assert q.address.city == 'LA'
        '''
    }

    @Test
    void deep_two_level_nesting() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('company.address.city': 'Jersey City')
            assert q.company.address.city == 'Jersey City'
            assert q.company.address.zip  == '07030'
            assert q.company.name         == 'Acme'
            assert p.company.address.city == 'Dock St'
        '''
    }

    @Test
    void identity_preserved_when_nested_value_unchanged() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith('address.city': p.address.city)
            assert q.is(p)   // no change anywhere -> original root returned
        '''
    }

    @Test
    void plain_top_level_copyWith_still_works() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith(name: 'Bob')
            assert q.name == 'Bob'
            assert q.address.is(p.address)
        '''
    }

    @Test
    void unsupported_nested_type_fails_clearly() {
        def err = shouldFail shell, '''
            @Immutable(copyWith = true) class Holder { String label; java.awt.Point pt }
            def h = new Holder('p', new java.awt.Point(1, 2))
            h.copyWith('pt.x': 9)
        '''
        assert err.message.contains('copyWith(Map)')
        assert err.message.contains('nested-copyWith domain')
    }

    @Test
    void null_intermediate_node_fails_clearly() {
        def err = shouldFail shell, '''
            @Immutable(copyWith = true) class Address { String city }
            @Immutable(copyWith = true) class Person { String name; Address address }
            def p = new Person(name: 'Alice')
            p.copyWith('address.city': 'Boston')
        '''
        assert err.message.contains("'address'")
        assert err.message.contains('null')
    }

    @Test
    void whole_and_nested_update_conflict_fails() {
        def err = shouldFail shell, GRAPH + '''
            def other = new Address('X', 'Y')
            p.copyWith(address: other, 'address.city': 'Z')
        '''
        assert err.message.contains('whole-value update and a nested update')
    }

    // === transactional copyWith { } block ===

    @Test
    void block_plain_and_nested_set() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith {
                name = 'Bob'
                address.city = 'LA'
            }
            assert q.name == 'Bob'
            assert q.address.city == 'LA'
            assert q.address.zip == '10001'
            assert p.name == 'Alice'
        '''
    }

    @Test
    void block_deep_nesting() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { company.address.city = 'Jersey City' }
            assert q.company.address.city == 'Jersey City'
            assert q.company.address.zip == '07030'
            assert q.company.name == 'Acme'
        '''
    }

    @Test
    void block_functional_modify() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith {
                name.modify { it.toUpperCase() }
                address.city.modify { it.reverse() }
            }
            assert q.name == 'ALICE'
            assert q.address.city == 'CYN'
            assert q.address.zip == '10001'
        '''
    }

    @Test
    void block_empty_is_identity() {
        assertScript shell, GRAPH + '''
            assert p.copyWith { }.is(p)
        '''
    }

    @Test
    void block_unsupported_nested_type_fails_clearly() {
        def err = shouldFail shell, '''
            @Immutable class Inner { String x }
            @Immutable(copyWith = true) class Outer { String label; Inner inner }
            def o = new Outer('l', new Inner('a'))
            o.copyWith { inner.x = 'b' }
        '''
        assert err.message.contains('copyWith(Map)')
    }

    // === 'old' (root pre-state) in the block, alongside .modify ===

    @Test
    void block_old_single_field() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { name = old.name.toUpperCase() }
            assert q.name == 'ALICE'
            assert q.address.is(p.address)
        '''
    }

    @Test
    void block_old_nested_path() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { address.city = old.address.city.reverse() }
            assert q.address.city == 'CYN'
            assert q.address.zip == '10001'
            assert p.address.city == 'NYC'
        '''
    }

    @Test
    void block_old_cross_field_derivation() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith { name = old.company.name + ' / ' + old.name }
            assert q.name == 'Acme / Alice'
        '''
    }

    @Test
    void block_old_and_modify_together() {
        assertScript shell, GRAPH + '''
            def q = p.copyWith {
                name = old.name.toUpperCase()
                address.city.modify { it.reverse() }
            }
            assert q.name == 'ALICE'
            assert q.address.city == 'CYN'
            assert q.address.zip == '10001'
        '''
    }

    @Test
    void old_is_escape_hatch_for_a_real_modify_method() {
        // The value type has its own modify(Closure). On a navigated path the
        // recorder's .modify shorthand would shadow it; via 'old' the RHS is
        // the real object, so the genuine domain method runs and its result
        // is recorded.
        assertScript shell, '''
            @Immutable class Money {
                int cents
                Money modify(Closure c) { new Money((int) c(cents)) }
            }
            @Immutable(copyWith = true) class Account {
                String owner
                Money balance
            }
            def a = new Account('Alice', new Money(100))
            def b = a.copyWith { balance = old.balance.modify { it + 50 } }
            assert b.balance.cents == 150
            assert b.owner == 'Alice'
            assert a.balance.cents == 100
        '''
    }
}
