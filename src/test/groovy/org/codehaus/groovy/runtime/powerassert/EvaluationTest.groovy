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
package org.codehaus.groovy.runtime.powerassert

import org.junit.jupiter.api.Test

import static java.lang.Math.max
import static java.lang.Math.min
import static org.codehaus.groovy.runtime.powerassert.AssertionTestUtil.fails

/**
 * Checks that:
 * <ul>
 * <li>assertion transformation succeeds w/o compilation error (checked implicitly)</li>
 * <li>assertion execution succeeds w/o runtime error (checked explicitly)</li>
 * <li>assertion execution has the expected result (checked explicitly)</li>
 * </ul>
 */
final class EvaluationTest {

    @Test
    void testPassingAndFailingAsserts() {
        assert true
        fails { assert false }

        assert 1
        fails { assert 0 }

        assert 2 * 3 == 6
        fails { assert 2 * 3 == 7 }

        assert "abc"
        fails { assert "" }

        assert "abc" + "def" == "abcdef"
        fails { assert "abc" + "def" == "abcdefg" }
    }

    @Test
    void testMultiLineAsserts() {
        assert 2 *
3 ==

                      6

        fails {
            assert 2 *
3 ==

                      7
        }
    }

    @Test
    void testMethodCallExpression() {
        assert [1,2,3].size() == 3
        assert [1,2,3].getClass().getMethod("size", null).getParameterTypes().length == 0
        assert Integer.valueOf(String.valueOf(10)) == 10
    }

    @Test
    void testMethodCallExpressionSpreadDot() {
        assert ["1", "22"]*.size() == [1, 2]
    }

    @Test
    void testMethodCallExpressionSafe() {
        def a = null
        assert a?.foo() == null
    }

    @Test
    void testStaticMethodCallExpression() {
        assert max(1,2) == 2
        assert max(min(1,2),3) == 3
    }

    @Test
    void testConstructorCallExpression() {
        assert new ArrayList().empty
        assert new String("abc") == "abc"
        assert new String(new String("abc")) == "abc"
    }

    @Test
    void testTernaryExpression() {
        assert 1 ? 1 : 0
        assert "abc".size() == 0 ? 0 : 1
    }

    @Test
    void testShortTernaryExpression() {
        assert 1 ?: 0
        assert "".size() ?: 1
    }

    @Test
    void testBinaryExpression() {
        assert 1 == 1
        assert 2 > 1 && 1 < 2
        assert 1 * 1 / 1 + 1 - 1 ** 1 == 1
        assert 1 == [[[[[1]]]]][0][0][0][0][0]
    }

    @Test
    void testPrefixExpression() {
        def x = 0
        assert ++x == 1
        assert --x == 0
        assert x == 0
    }

    @Test
    void testPostfixExpression() {
        def x = 0
        assert x++ == 0
        assert x-- == 1
        assert x == 0
    }

    @Test
    void testBooleanExpression() {
        assert 1
        assert "abc"
        assert [1,2,3]
        assert 1 + 2 + 3
    }

    @Test
    void testClosureExpression() {
        def x = 0
        def test = { it -> assert ++x == 1; { it2 -> assert ++x == 2 }(); { it3 -> assert ++x == 3 } }()
        assert x == 2
        test()
        assert x == 3

    }

    @Test
    void testTupleExpression() {
        def a, b
        assert ((a, b) = [1, 2]) == [1, 2]
    }

    @Test
    void testMapExpression() {
        assert ![:]
        assert [a:1] + [b:2] == [a:1,b:2]
    }

    @Test
    void testListExpression() {
        assert [1,2,3].size() == 3
        assert [] + [1] + [2,3] == [1,2,3]
    }

    @Test
    void testRangeExpression() {
        assert (1..3).contains(3)
        assert !((1..<3).contains(3))
        assert !((1<..3).contains(1))
        assert (!(1<..<3).contains(1) && !(1<..<3).contains(3))
    }

    @Test
    void testPropertyExpression() {
        assert 'A'.bytes == [65] as byte[]
        assert (new Properties().next.next.next.x = 10) == 10
        assert Integer.MIN_VALUE < Integer.MAX_VALUE
    }

    @Test
    void testAttributeExpression() {
        def attrs = new Attributes()
        attrs.x = 1
        attrs.y = attrs
        assert attrs.x == attrs.@x
        assert attrs.@y.@x == 1
    }

    @Test
    void testMethodPointerExpression() {
        def pointers = new MethodPointers()
        assert pointers.&inc
        assert [1,2,3].collect(pointers.&inc) == [2,3,4]
    }

    @Test
    void testConstantExpression() {
        assert 1
        assert 1 == 1.0
        assert "abc".reverse() == "cba"
    }

    @Test
    void testClassExpression() {
        assert EvaluationTest == getClass()
        assert EvaluationTest.getClass() == Class.class
    }

    @Test
    void testVariableExpression() {
        def x = 1
        def y = 2
        assert x < y
        assert x + y == 2 * y - x
        assert Math.max(x,y) == 2
    }

    @Test
    void testRegexExpression() {
        assert (~"ab*a").matcher("abbba")
        assert !(~"ab*a").matcher("abcba")
    }

    @Test
    void testGStringExpression() {
        def x = 1
        def y = [1,2,3]
        assert "$x and ${y.size()}" == "1 and 3"
    }

    @Test
    void testArrayExpression() {
        assert ([1,2,3] as int[]).size() == 3
    }

    private add(x, y) { x + y }

    @Test
    void testSpreadExpression() {
        assert add(*[1,2]) == 3
        assert [1,*[2,*[3,*[4]]]] == [1,2,3,4]
    }

    private sub(args) { args.x - args.y }

    @Test
    void testSpreadMapExpression() {
        assert sub(*:[y:1,x:2]) == 1
        assert [a:1,b:2,c:3] == [c:3, *:[b:2,a:1]]
    }

    @Test
    void testNotExpression() {
        assert !false
        assert !!true
        assert !(true && false)
    }

    @Test
    void testUnaryMinusExpression() {
        assert -(-1) == 1
        assert -1 + -2 == -3
    }

    @Test
    void testUnaryPlusExpression() {
        assert +(+2) == 2
        assert +1 + +2 == +3
    }

    @Test
    void testBitwiseNegationExpression() {
        assert ~1 == -2
        assert ~~1 == 1
    }

    @Test
    void testCastExpression() {
        assert (List)[1,2,3]
        assert ([1,2,3] as int[]).getClass().isArray()
    }

    @Test
    void testArgumentListExpression() {
        assert 3.toString() == "3"
        assert Arrays.asList(1,2,3) == [1,2,3]
    }

    /*
    @Test
    void testMapEntryExpression() {
        // tested as part of testMapExpression
    }

    @Test
    void testFieldExpression() {
        // doesn't seem to be used
    }

    @Test
    void testDeclarationExpression() {
        // cannot occur in an assertion statement
    }

    @Test
    void testRegexExpression() {
        // doesn't seem to be used
    }

    @Test
    void testClosureListExpression() {
        // cannot occur in an assertion statement
    }

    @Test
    void testBytecodeExpression() {
        // cannot occur in an assertion statement
    }
    */
}

@groovy.transform.PackageScope class Properties {
    def getNext() { this }
    def x
}

@groovy.transform.PackageScope class Attributes {
    def x
    def y
}

@groovy.transform.PackageScope class MethodPointers {
    def inc(x) { x + 1 }
}
