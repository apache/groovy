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
package groovy.operator

/**
 * <code>[2, 3].toSpreadList() equals to *[2, 3]</code> <br><br>
 *
 * For an example, <pre>
 *        assert [1, *[2, 3], 4] == [1, 2, 3, 4]
 * </pre>
 */
class SpreadListOperatorTest extends GroovyTestCase {

    void testSpreadingInList() {
        println([1, *[222, 333], 456])

        assert [1, *[222, 333], 456] == [1, 222, 333, 456]

        def y = [1, 2, 3]

        assert [*y] == y
    }

    void testSpreadingRange() {
        def r = 1..10

        assert [*r] == r
        assert [*1..10] == r

    }

    void testSpreadingInMethodParameters() {
        assert sum(1, *[2, 3], 4) == 10
        assert sum(*[10, 20, 30, 40]) == 100

        def z = [11, 22, 33]

        assert sum(1, *z) == 67

        assert sum(*z, 2) == 68

        assert sum(*z, 44) == 110

        def x = ["foo", "Bar-"]

        assert sum(*x, *x) == "fooBar-fooBar-"
    }

    void testSliceWithSpread() {
        def result = (1..5)[1..3]
        assert result == [2, 3, 4]
        result = (1..5)[*1..3]
        assert result == [2, 3, 4]
        result = (1..5)[1..3, 0]
        assert result == [2, 3, 4, 1]
        result = (1..5)[*1..3, 0]
        assert result == [2, 3, 4, 1]
    }

    void testSettingViaSpreadWithinIndex() {
        def orig = 'a'..'f'

        def items = orig.toList()
        items[1..2] = 'X'
        assert items == ['a', 'X', 'd', 'e', 'f']

        items = orig.toList()
        items[*1..2] = 'X'
        assert items == ['a', 'X', 'X', 'd', 'e', 'f']

        items = orig.toList()
        items[*1..2, 4] = 'X'
        assert items == ['a', 'X', 'X', 'd', 'X', 'f']

        def message = shouldFail IllegalArgumentException, '''
            def items = [1, 2, 3, 4]
            items[*new Date()]
        '''
        assert message.contains('cannot spread the type java.util.Date')

        message = shouldFail IllegalArgumentException, '''
            def items = [1, 2, 3, 4]
            def map = [a: 1]
            items[*map]
        '''
        assert message.contains('cannot spread the type java.util.LinkedHashMap')
        assert message.contains('did you mean to use the spread-map operator instead?')
    }

    def sum(a, b, c, d) {
        return a + b + c + d
    }

    void testSpreadingInClosureParameters() {
        def twice = { it * 2 }
        assert twice(3) == 6
        assert twice("abcd") == 'abcdabcd'

        assert twice(*[11]) == 22
    }
}
