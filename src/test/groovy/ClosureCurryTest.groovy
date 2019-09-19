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
package groovy

import groovy.test.GroovyTestCase
import org.codehaus.groovy.runtime.DefaultGroovyMethods

/**
 * Tests for curried closures
 */
class ClosureCurryTest extends GroovyTestCase {

    void testCurry() {
        def clos1 = {s1, s2 -> s1 + s2}
        def clos2 = clos1.curry("hi")
        def value = clos2("there")
        assert value == "hithere"

        def clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        def clos4 = clos3.curry('a')
        def clos5 = clos4.curry('b')
        def clos6 = clos4.curry('x')
        def clos7 = clos4.curry('f', 'g')
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}.asWritable()
        clos4 = clos3.curry('a')
        clos5 = clos4.curry('b')
        clos6 = clos4.curry('x')
        clos7 = clos4.curry('f', 'g')
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').asWritable()
        clos5 = clos4.curry('b').asWritable()
        clos6 = clos4.curry('x').asWritable()
        clos7 = clos4.curry('f', 'g').asWritable()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').clone()
        clos5 = clos4.curry('b').clone()
        clos6 = clos4.curry('x').clone()
        clos7 = clos4.curry('f', 'g').clone()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"

        clos3 = {s1, s2, s3 -> s1 + s2 + s3}
        clos4 = clos3.curry('a').asWritable().clone()
        clos5 = clos4.curry('b').asWritable().clone()
        clos6 = clos4.curry('x').asWritable().clone()
        clos7 = clos4.curry('f', 'g').asWritable().clone()
        value = clos5('c')
        assert value == "abc"
        value = clos6('c')
        assert value == "axc"
        value = clos4('y', 'z')
        assert value == "ayz"
        value = clos7()
        assert value == "afg"
    }

    void testParameterTypes() {
        def cl1 = {String s1, int i -> return s1 + i }
        assert "foo5" == cl1("foo", 5)
        assert [String, int] == cl1.getParameterTypes().toList()

        def cl2 = cl1.curry("bla")
        assert "bla4" == cl2(4)
        assert null != cl2.getParameterTypes()
        assert [int] == cl2.getParameterTypes().toList()
    }

    void testVarargParameterTypes() {
        def a = { Object one, Object[] others -> }
        def b = a.ncurry(2, 'x', 'y')
        assert b.parameterTypes.name == ['java.lang.Object', 'java.lang.Object', '[Ljava.lang.Object;']
        def c = a.ncurry(-3, 'x', 'y')
        assert c.parameterTypes.name == ['java.lang.Object', '[Ljava.lang.Object;']
        def d = a.curry('a')
        assert d.parameterTypes.name == ['[Ljava.lang.Object;']
        def g = { String s, Integer num, Date da, Object[] others ->  }
        def h = g.ncurry(1, 4, new Date(), 'foo')
        assert h.parameterTypes.name == ['java.lang.String', '[Ljava.lang.Object;']
    }

    void testVarargCurry() {
        def c = { arg, Object[] extras -> arg + ', ' + extras.join(', ') }
        def d = c.curry( 1 ) //curry first param only
        assert d( 2, 3, 4 ) == '1, 2, 3, 4'
        def e = c.curry( 1, 3 ) //curry part of Object[] also
        assert e( 5 ) == '1, 3, 5'
        def f = e.curry( 5, 7, 9, 11 ) //currying continues on Object
        assert f( 13, 15 ) == '1, 3, 5, 7, 9, 11, 13, 15'
    }

    void testDelegate() {
        def res = null
        def c = {a -> res = z}
        def cc = c.curry(1)

        cc.delegate = [z: "goodbye"]
        cc()
        assert res == cc.delegate.z
    }

    void testExpandoWithCurry() {
        def sz = 'java.util.Date'.size()
        def c = {arg -> arg + delegate.getClass().name.size() }
        def d = new Date()
        d.metaClass.foo = c
        assert d.foo(42) == 42 + sz
        d.metaClass.bar = c.curry('baz')
        assert d.bar() == 'baz' + sz
    }

    void testCurryMultiply() {
        def multiply = { a, b -> a * b }
        def doubler = multiply.curry(2)
        assert doubler(4) == 8
    }

    void testRCurryDivide() {
        def divide = { a, b -> a / b }
        def halver = divide.rcurry(2)
        assert halver(8) == 4
    }

    void testNCurryBinarySearch() {
        def caseInsensitive = { a, b -> a.toLowerCase() <=> b.toLowerCase() } as Comparator
        def caseSensitive = { a, b -> a <=> b } as Comparator
        def animals1 = ['ant', 'dog', 'BEE']
        def animals2 = animals1 + ['Cat']
        // curry middle param of this utility method:
        // Collections#binarySearch(List list, Object key, Comparator c)
        def catSearcher = Collections.&binarySearch.ncurry(1, "cat")
        def combos = [[animals1, animals2], [caseInsensitive, caseSensitive]].combinations()
        assert combos.collect{ List a, Comparator c ->
            // make use we use DGM#sort, not JDK8 sort
            def sorted = DefaultGroovyMethods.sort(a,c)
            catSearcher(sorted, c)
        } == [-3, 2, -3, -4]
    }

    void testNestedNcurryRcurry() {
        def operation = { int x, Closure f, int y -> f(x, y) }
        def divider = operation.ncurry(1) { a, b -> a / b }
        def halver = divider.rcurry(2)
        assert 50 == halver(100)
    }

    void testEquivalentsNormal() {
        def a = {one, two, three, four, five -> "$one,$two,$three,$four,$five"}
        def expected = '1,2,3,4,5'
        checkEquivalents(a, expected)
    }

    void testEquivalentsVararg() {
        def a = {one, two, Object[] others -> "one=$one, two=$two, others=${others.join(',')}"}
        def expected = 'one=1, two=2, others=3,4,5'
        checkEquivalents(a, expected)
    }

    private void checkEquivalents(a, expected) {
        def examples = [
            [a.curry(1), [2,3,4,5]],
            [a.curry(1,2), [3,4,5]],
            [a.curry(1,2,3), [4,5]],
            [a.curry(1,2,3,4), [5]],
            [a.curry(1,2,3,4,5), []],
            [a.curry(1,2).curry(3,4), [5]],
            [a.curry(1).curry(2).curry(3).curry(4), [5]],
            [a.rcurry(5), [1,2,3,4]],
            [a.rcurry(5).rcurry(3,4), [1,2]],
            [a.rcurry(4,5).rcurry(3), [1,2]],
            [a.rcurry(4,5), [1,2,3]],
            [a.rcurry(2,3,4,5), [1]],
            [a.rcurry(1,2,3,4,5), []],
            [a.ncurry(2,3), [1,2,4,5]],
            [a.ncurry(2,3,4), [1,2,5]],
            [a.ncurry(-3,3,4), [1,2,5]],
            [a.ncurry(-3,3), [1,2,4,5]]
        ]
        examples.each{ clos, args ->
            assert clos(args as Object[]) == expected
        }
    }

    void testNullVariants() {
        assert { x, y -> x ?: y }.curry(null)(null) == null
        assert { x, y -> x ?: y }.curry(null)(2) == 2
        assert { x, y -> x ?: y }.curry(1)(null) == 1
        assert { x, y -> x ?: y }.curry(1)(2) == 1

        assert { x, y -> x ?: y }.curry(null, null)() == null
        assert { x, y -> x ?: y }.curry(null, 2)() == 2
        assert { x, y -> x ?: y }.curry(1, null)() == 1
        assert { x, y -> x ?: y }.curry(1, 2)() == 1

        assert { x, y -> x ?: y }(null, null) == null
        assert { x, y -> x ?: y }(null, 2) == 2
        assert { x, y -> x ?: y }(1, null) == 1
        assert { x, y -> x ?: y }(1, 2) == 1

        assert { x, y -> x ?: y }.curry([null] as Object[])(2) == 2
    }

    private a(Integer b, Integer c, Integer d){ "3Int: $b $c $d" }
    private a(b,c,d){ "3Obj: $b $c $d" }
    private a(b,c){ "2Obj: $b $c" }
    private a(b){ "1Obj: $b" }


    void testCurryWithMethodClosure() {
        def c = (this.&a).curry(0)
        assert c(1,2) == '3Int: 0 1 2'
        assert c("foo",2) == '3Obj: 0 foo 2'
        assert c(1) == '2Obj: 0 1'
        assert c() == '1Obj: 0'

        def b = (this.&a).rcurry(0)
        assert b(1,2) == '3Int: 1 2 0'
        assert b("foo",2) == '3Obj: foo 2 0'
        assert b(1) == '2Obj: 1 0'
        assert b() == '1Obj: 0'
    }

    void testInsufficientSuppliedArgsWhenUsingNCurry() {
        def cl = { a, b, c = 'x' -> a + b + c }
        assert cl.ncurry(1, 'b')('a') == 'abx'
        // ncurry is done eagerly and implies a minimum number of params
        shouldFail(IllegalArgumentException) {
            cl.ncurry(1, 'b')()
        }
        // rcurry is done lazily
        assert cl.rcurry('b', 'c')('a') == 'abc'
        assert cl.rcurry('b', 'c')() == 'bcx'
    }

    void testCurryInComboWithDefaultArgs() {
        def cl = { a, b, c='c', d='d' -> a + b + c + d }
        assert 'abcd' == cl.ncurry(0, 'a')('b')
        assert 'xycd' == cl.ncurry(1, 'y')('x')
        assert 'abdd' == cl.ncurry(0, 'a').rcurry('d')('b')
        assert 'abcx' == cl.ncurry(3, 'x')('a', 'b', 'c')
        shouldFail(IllegalArgumentException) {
            // ncurry is done eagerly and implies a minimum number of params
            // default arguments are ignored prior to the ncurried argument
            cl.ncurry(4, 'd')('a', 'b')
        }
    }
}
