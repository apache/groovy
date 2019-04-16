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

import static AssertionTestUtil.*
import static java.lang.Math.min

/**
 * Tests rendering of whole assertions.
 */

class AssertionRenderingTest extends GroovyTestCase {
    void testSimpleAssertion() {
        isRendered """
assert x == 1
       | |
       2 false
        """, {
            def x = 2
            assert x == 1
        }
    }

    void testMultiLineAssertion() {
        isRendered """
assert 1 + 2 == 4 - 2
         |   |    |
         3   |    2
             false
        """, {
            assert 1 +
                2 ==



4 -

                          2

        }
    }

    private one(x) { 0 }

    void testMethodCallExpressionWithImplicitTarget() {
        isRendered """
assert one(a)
       |   |
       0   1
        """, {
            def a = 1
            assert one(a)
        }
    }

    void testMethodCallExpressionWithExplicitTarget() {
        isRendered """
assert a.get(b) == null
       | |   |  |
       | 1   0  false
       [1]
        """, {
            def a = [1]
            def b = 0
            assert a.get(b) == null
        }
    }

    void testMethodCallExpressionWithGStringMethod() {
        isRendered """
assert [1]."\$x"(0) == null
           | |     |
           1 'get' false
        """, {
            def x = "get"
            assert [1]."$x"(0) == null
        }
    }

    void testMethodCallExpressionCallingStaticMethod() {
        isRendered """
assert Math.max(a,b) == null
            |   | |  |
            2   1 2  false
        """, {
            def a = 1
            def b = 2
            assert Math.max(a,b) == null
        }
    }

    void testMethodCallExpressionSpreadDot() {
        isRendered """
assert ["1", "22"]*.size() == null
                    |      |
                    [1, 2] false
        """, {
            assert ["1", "22"]*.size() == null
        }
    }

    void testMethodCallExpressionSafe() {
        isRendered """
assert a?.foo()
       |  |
       |  null
       null
        """, {
            def a = null
            assert a?.foo()
        }
    }

    void testStaticMethodCallExpression() {
        isRendered """
assert min(a,b) == null
       |   | |  |
       1   1 2  false
        """, {
            def a = 1
            def b = 2
            assert min(a,b) == null
        }
    }

    void testConstructorCallExpression() {
        isRendered """
assert new ArrayList(a) == null
       |             |  |
       []            1  false
        """, {
            def a = 1
            assert new ArrayList(a) == null
        }

    }

    void testTernaryExpression() {
        isRendered """
assert a ? b : c
       |   |
       1   0
        """, {
            def a = 1
            def b = 0
            def c = 1
            assert a ? b : c
        }

        isRendered """
assert a ? b : c
       |       |
       0       0
        """, {
            def a = 0
            def b = 1
            def c = 0
            assert a ? b : c
        }
    }

    void testShortTernaryExpression() {
        isRendered """
assert (a ?: b) == null
        |       |
        1       false
        """, {
            def a = 1
            def b = 2
            assert (a ?: b) == null
        }

        isRendered """
assert a ?: b
       |    |
       0    0
        """, {
            def a = 0
            def b = 0
            assert a ?: b
        }
    }

    void testBinaryExpression() {
        isRendered """
assert a * b
       | | |
       0 0 1
        """, {
            def a = 0
            def b = 1
            assert a * b
        }

        isRendered """
assert a[b]
       |||
       ||0
       |false
       [false]
        """, {
            def a = [false]
            def b = 0
            assert a[b]
        }
    }

    void testPrefixExpression() {
        isRendered """
assert ++x == null
       | | |
       1 0 false
        """, {
            def x = 0
            assert ++x == null
        }
    }

    void testPostfixExpression() {
        isRendered """
assert x++ == null
       ||  |
       |0  false
       0
        """, {
            def x = 0
            assert x++ == null
        }
    }

    void testBooleanExpression() {
        isRendered """
assert a
       |
       null
        """, {
            def a = null
            assert a
        }
    }

    void testClosureExpression() {
        isRendered """
assert { 1 + 2 } == null
                 |
                 false
        """, {
            assert { 1 + 2 } == null
        }
    }

    void testTupleExpression() {
        // TupleExpression is only used on LHS of (multi-)assignment,
        // but LHS of assignment is not rewritten
        isRendered """
assert ((a,b) = [1,2]) && false
              |        |
              [1, 2]   false
        """, {
          def a
          def b
          assert ((a,b) = [1,2]) && false
        }
    }

    void testMapExpression() {
        isRendered """
assert [a:b, c:d] == null
          |    |  |
          2    4  false
        """, {
            def b = 2
            def d = 4
            assert [a:b, c:d] == null
        }

        isRendered """
assert [(a):b, (c):d] == null
        |   |  |   |  |
        1   2  3   4  false
        """, {
            def a = 1
            def b = 2
            def c = 3
            def d = 4
            assert [(a):b, (c):d] == null
        }
    }

    void testListExpression() {
        isRendered """
assert [a,b,c] == null
        | | |  |
        1 2 3  false
        """, {
            def a = 1
            def b = 2
            def c = 3
            assert [a,b,c] == null
        }
    }

    void testRangeExpression() {
        isRendered """
assert (a..b) == null
        |  |  |
        1  2  false
        """, {
            def a = 1
            def b = 2
            assert (a..b) == null
        }

        isRendered """
assert (a..<b) == null
        |   |  |
        1   2  false
        """, {
            def a = 1
            def b = 2
            assert (a..<b) == null
        }
    }

    void testPropertyExpression() {
        isRendered """
assert a.bytes == null
       | |     |
       | [65]  false
       'A'

        """, {
            def a = 'A'
            assert a.bytes == null
        }

        isRendered """
assert Integer.MIN_VALUE == null
               |         |
               |         false
               -2147483648
        """, {
            assert Integer.MIN_VALUE == null
        }
    }

    void testAttributeExpression() {
        isRendered """
assert holder.@x
       |       |
       h       0
        """, {
            def holder = new Holder()
            assert holder.@x
        }
    }

    void testMethodPointerExpression() {
        isRendered """
assert a.&"\$b" == null
       |    |  |
       []   |  false
            'get'
        """, {
            def a = []
            def b = "get"
            assert a.&"$b" == null
        }
    }

    void testConstantExpression() {
        isRendered """
assert 1 == "abc"
         |
         false
        """, {
            assert 1 == "abc"
        }
    }

    void testClassExpression() {
        isRendered """
assert List == String
            |
            false
        """, {
            assert List == String
        }
    }

    void testVariableExpression() {
        isRendered """
assert x
       |
       0
        """, {
            def x = 0
            assert x
        }
    }

    void testGStringExpression() {
        isRendered '''
assert "$a and ${b + c}" == null
         |       | | |   |
         1       2 5 3   false
        ''', {
            def a = 1
            def b = 2
            def c = 3
            assert "$a and ${b + c}" == null
        }
    }

    void testArrayExpression() {
        isRendered """
assert new int[a][b] == null
               |  |  |
               1  2  false
        """, {
            def a = 1
            def b = 2
            assert new int[a][b] == null
        }
    }

    private two(a, b) { 0 }

    void testSpreadExpression() {
        isRendered """
assert two(*a)
       |    |
       0    [1, 2]
        """, {
            def a = [1, 2]
            assert two(*a)
        }

        isRendered """
assert [1, *a] == null
            |  |
            |  false
            [2, 3]
        """, {
            def a = [2, 3]
            assert [1, *a] == null
        }
    }

    void testSpreadMapExpression() {
        isRendered """
assert one(*:m)
       |     |
       0     ['a':1, 'b':2]
        """, {
            def m = [a:1, b:2]
            assert one(*:m)
        }

        isRendered """
assert [a:1, *:m] == null
               |  |
               |  false
               ['b':2, 'c':3]
        """, {
            def m = [b:2, c:3]
            assert [a:1, *:m] == null
        }
    }

    void testNotExpression() {
        isRendered """
assert !a
       ||
       |true
       false
        """, {
            def a = true
            assert !a
        }
    }

    void testUnaryMinusExpression() {
        isRendered """
assert -a == null
       || |
       |1 false
       -1
        """, {
            def a = 1
            assert -a == null
        }
    }

    void testUnaryPlusExpression() {
        isRendered """
assert +a == null
       || |
       |1 false
       1
        """, {
            def a = 1
            assert +a == null
        }
    }

    void testBitwiseNegationExpression() {
        isRendered """
assert ~a == null
       || |
       |1 false
       -2
        """, {
            def a = 1
            assert ~a == null
        }
    }

    void testCastExpression() {
        isRendered """
assert (List)a
             |
             null
        """, {
            def a = null
            assert (List)a
        }

        isRendered """
assert a as int[]
       |
       null
        """, {
            def a = null
            assert a as int[]
        }
    }

    private three(a,b,c) { 0 }

    void testArgumentListExpression() {
        isRendered """
assert three(a, b,c)
       |     |  | |
       0     1  2 3
        """, {
            def a = 1
            def b = 2
            def c = 3
            assert three(a, b,c)
        }
    }

    void testExplicitClosureCall() {
        def func = { it }

        isRendered """
assert func.call(42) == null
       |    |        |
       |    42       false
       ${func.toString()}
        """, {
            assert func.call(42) == null
        }
    }

    /*
    void testMapEntryExpression() {
        // tested as part of testMapExpression
    }

    void testFieldExpression() {
        // doesn't seem to be used
    }

    void testDeclarationExpression() {
        // cannot occur in an assertion statement
    }

    void testRegexExpression() {
        // doesn't seem to be used
    }

    void testClosureListExpression() {
        // cannot occur in an assertion statement
    }

    void testBytecodeExpression() {
        // cannot occur in an assertion statement
    }
    */
}

@groovy.transform.PackageScope class Holder {
    public x = 0
    def getX() { 9 }
    String toString() { "h" }
}
