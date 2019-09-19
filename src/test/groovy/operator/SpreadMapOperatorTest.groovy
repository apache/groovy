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

import groovy.test.GroovyTestCase

/**
 * Test the spread map operator "*:".
 *
 *   For an example,
 *            m = ['a':11, 'aa':22, 'aaa':33]
 *            z = ['c':100, *:m]
 *
 *            m = ['a':11, 'aa':22, 'aaa':33]
 *            w = ['c':100]
 *            m.each {w[it.key] = it.value }*
 *            assert z == w
 *
 */
class SpreadMapOperatorTest extends GroovyTestCase {
    def f(m) {
        println m.c
    }

    def func(m, i, j, k) {
        // The first argument m is a map.
        println m
        println i
        println j
        println k
    }

    def fn() {
        return [1: 'ein', 2: 'zwei', 3: 'drei']
    }

    void testSpreadMap() {
        try {
            def m = ["a": 100, "b": 200]
            def x = ['tt': 55, *: m]
            println x.size()
            println x
            x = ['tt': 55, 'yy': 77]
            println x
            x = [*: m, *: m]
            println x
            assert x == m

            x = [*: x, *: fn(), 100: 'hundred']
            println x
            println(x.getClass())
            assert x instanceof Map

            def y = [1: 1, 2: 2, *: [3: 3, 4: 4, *: [5: 5], 6: 6], 7: 7]
            println y
            println(y.getClass())
            assert y == [1: 1, 2: 2, 3: 3, 4: 4, 5: 5, 6: 6, 7: 7]
        }
        catch (Exception e) {
            e.printStackTrace()
        }
    }

    void testSpreadMapVsWithClosure() {
        def m = ['a': 11, 'aa': 22, 'aaa': 33]
        def z = ['c': 100, *: m]

        def w = ['c': 100]
        m.each { w[it.key] = it.value }

        println z
        println w
        assert z == w

        def z2 = [*: m, 'c': 100]
        def w2 = m
        w2['c'] = 100
        println z2
        println w2
        assert z2 == w2
        assert z == z2
        assert w == w2
    }

    void testSpecialSpreadMapIndexNotation() {
        assertScript '''
        @groovy.transform.ToString
        class Person { String name; int age }

        assert Person[ name:'Dave', age:32 ].toString() == 'Person(Dave, 32)'

        def timMap = [ name:'Tim', age:49 ]
        assert Person[ *:timMap ].toString() == 'Person(Tim, 49)'

        assert Person[ *:[ name:'John', age:29 ] ].toString() == 'Person(John, 29)'

        def ppl = [ [ name:'Tim', age:49 ], [ name:'Dave', age:32 ], [ name:'Steve', age:18 ] ]
        assert ppl.collect { Person [ *:it ] }*.age == [49, 32, 18]
        '''
    }

    void testSpreadMapFunctionCall() {
        def m = ['a': 10, 'b': 20, 'c': 30]
        f(*: m)                 // Call with only one spread map argument
        f(*: m, 'e': 50)      // Call with one spread map argument and one named argument
        f('e': 100, *: m)     // Call with one named argument and one spread map argument

        func('e': 100, 1, 2, 3, *: m)
        // Call with one named argument, three usual arguments,  and one spread map argument

        def l = [4, 5]
        func('e': 100, *l, *: m, 6)
        // Call with one named argument, one spread list argument, one spread map argument, and  one usual argument
        func(7, 'e': 100, *l, *: m)
        // Call with one usual argument, one named argument, one spread list argument, and one spread map argument
    }
}

