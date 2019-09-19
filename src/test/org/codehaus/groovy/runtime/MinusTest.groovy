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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

class MinusTest extends GroovyTestCase {

    void doTestMinus(String type, def a, b, c, d) {
        assertEquals(type, [a, b], [a, b, c] - [c])
        assertEquals(type, [a, b], [a, b, c] - [c, d])
        assertEquals(type, [], [a, b, c] - [a, b, c])
        assertEquals(type, [], [a, b, c] - [c, b, a])
        assertEquals(type, [a, b, c], [a, b, c] - [])
        assertEquals(type, [], [] - [a, b, c])
    }

    void doTestMinusDuplicates(String type, def a, b, c, d) {
        assertEquals(type, [a, a], [a, a] - [])
        assertEquals(type, [a, b, b, c], [a, b, b, c] - [])
        assertEquals(type, [b, b, c], [a, b, b, c] - [a])
        assertEquals(type, [a], [a, b, b, c] - [b, c])
        assertEquals(type, [], [a] - [a, a])
    }

    void doTestMinusWithNull(String type, def a, b, c, d) {
        assertEquals(type, [a, b, c], [a, b, c] - [null])
        assertEquals(type, [a, b, c], [a, b, c, null] - [null])
        assertEquals(type, [a, b], [a, b, c, null] - [null, c])
        assertEquals(type, [], [] - [a, b, c, null])
        assertEquals(type, [a, b, c, null], [a, b, c, null] - [])
        assertEquals(type, [a, b, null], [a, b, c, null] - [c])
    }

    void testMinusComparable() {
        def a = 'a'
        def b = 'b'
        def c = 'c'
        def d = 'd'

        doTestMinus('Comparable', a, b, c, d)
        doTestMinusDuplicates('Comparable', a, b, c, d)
        doTestMinusWithNull('Comparable', a, b, c, d)
    }

    void testMinusNumber() {
        def a = 1
        def b = 2
        def c = 3
        def d = 4

        doTestMinus('Number', a, b, c, d)
        doTestMinusDuplicates('Number', a, b, c, d)
        doTestMinusWithNull('Number', a, b, c, d)
    }

    void testMinusNumbersMixed() {
        def a = 1
        def b = new BigInteger('2')
        def c = 3.0d
        def d = new BigDecimal('4.0')

        doTestMinus('NumbersMixed', a, b, c, d)
        doTestMinusDuplicates('NumbersMixed', a, b, c, d)
        doTestMinusWithNull('NumbersMixed', a, b, c, d)
    }

    void testMinusNonComparable() {
        def a = new Object()
        def b = new Object()
        def c = new Object()
        def d = new Object()

        doTestMinus('NonComparable', a, b, c, d)
        doTestMinusDuplicates('NonComparable', a, b, c, d)
        doTestMinusWithNull('NonComparable', a, b, c, d)
    }

    void testMinusMixed() {
        def a = new Object()
        def b = 2
        def c = '3'
        def d = new BigDecimal('4.0')

        doTestMinus('Mixed', a, b, c, d)
        doTestMinusDuplicates('Mixed', a, b, c, d)
        doTestMinusWithNull('Mixed', a, b, c, d)
    }

    void testArrayMinus() {
        def x = [1, 2, 3] as Object[]
        def y = [1, 2] as Object[]
        def z = [2, 3]
        assert x - 2 == [1, 3] as Object[]
        assert x - y == [3] as Object[]
        assert x - z == [1] as Object[]
    }

    void testMapMinus() {
        def x = [1: 1, 2: 2, 3: 3, 4: 4]
        def y = [1: 1, 2: 2]
        def z = [2: 2, 4: 4]

        assert x - y == [3: 3, 4: 4]
        assert x - z == [1: 1, 3: 3]
        assert x - x == [:]
        assert y - z == [1: 1]

        assert x - [1.0: 1] == [1:1, 2:2, 3:3, 4:4]
        assert x - [1: 1.0] == [2:2, 3:3, 4:4]
    }
}
