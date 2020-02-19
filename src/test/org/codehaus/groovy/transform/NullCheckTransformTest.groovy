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

import groovy.test.GroovyTestCase

/**
 * Tests for the {@code @NullCheck} AST transform.
 */
class NullCheckTransformTest extends GroovyTestCase {

    // GROOVY-9406
    void testNullCheckDoesNotConflictWithGeneratedConstructors() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            class Base1 {
                Long id
                Base1(Long id) { this.id = id }
            }

            @NullCheck
            @InheritConstructors
            class Sub1A extends Base1 {}
            assert new Sub1A(null).id == null

            @InheritConstructors
            @NullCheck
            class Sub1B extends Base1 {}
            assert new Sub1B(null).id == null

            @NullCheck
            class Sub1C extends Base1 {
                Sub1C(Long id) { super(id) }
                Sub1C(Long id, Boolean flag) { super(id) }
            }
            def ex = shouldFail(IllegalArgumentException) { new Sub1C(null).id == null }
            assert ex.message == 'id cannot be null'
            ex = shouldFail(IllegalArgumentException) { new Sub1C(42L, null).id == null }
            assert ex.message == 'flag cannot be null'
        '''
    }

    void testNullCheckWithIncludeGenerated() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            @EqualsAndHashCode
            @NullCheck
            class OneHolder {
                String one
            }

            @EqualsAndHashCode
            @NullCheck(includeGenerated=true)
            class TwoHolder {
                String two
            }

            def one = new OneHolder(one: 'One')
            assert !one.equals(null)
            def two = new TwoHolder(two: 'Two')
            def ex = shouldFail(IllegalArgumentException) { two.equals(null) }
            assert ex.message == 'other cannot be null'
        '''
    }

    void testNullCheckMethodWithDefaultValues() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            @NullCheck
            class ConsWithDefaults {
                ConsWithDefaults(String first, String middle = 'M', String last = 'L') {}
                ConsWithDefaults(String part, Integer times, Boolean flag) {
                    this('dummy', 'value')
                }
            }

            def ex = shouldFail(IllegalArgumentException) { new ConsWithDefaults(null) }
            assert ex.message == 'first cannot be null'
            ex = shouldFail(IllegalArgumentException) { new ConsWithDefaults('F', null) }
            assert ex.message == 'middle cannot be null'
            ex = shouldFail(IllegalArgumentException) { new ConsWithDefaults('Foo', null, true) }
            assert ex.message == 'times cannot be null'
            ex = shouldFail(IllegalArgumentException) { new ConsWithDefaults('Foo', 3, null) }
            assert ex.message == 'flag cannot be null'
        '''
    }

    void testNullCheckMethodWithNullDefaultValue() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            @NullCheck
            class ConsWithNullDefault {
                ConsWithNullDefault(Integer first = null, Integer second = 42) {}
            }

            assert new ConsWithNullDefault(42)

            def ex = shouldFail(IllegalArgumentException) { new ConsWithNullDefault() }
            assert ex.message == 'first cannot be null'

            ex = shouldFail(IllegalArgumentException) { new ConsWithNullDefault(null) }
            assert ex.message == 'first cannot be null'

            ex = shouldFail(IllegalArgumentException) { new ConsWithNullDefault(42, null) }
            assert ex.message == 'second cannot be null'
        '''
    }
}
