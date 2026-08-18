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
package bugs

import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.UnaryMinusExpression
import org.codehaus.groovy.control.CompilePhase
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A parenthesized bare name whose final segment does not start with an uppercase
 * letter (the class naming convention) is a value expression, not a cast type;
 * {@code in}/{@code as} after a parenthesized bare name always take their binary
 * reading. These tests deliberately assert evaluated results, not just successful
 * parsing (see GROOVY-8913 history).
 */
final class Groovy10355 {

    @Test
    void testParenthesizedNameBeforePlusAndMinus() {
        assertScript '''
            String b = "B"
            def r = "A" + (b) + "C"
            assert r == 'ABC'
        '''
        assertScript '''
            int answer = 42
            assert (answer) - 3 == 39
            assert (answer) + 3 == 45
        '''
        assertScript '''
            def map = [x: 1]
            assert (map.x) + 2 == 3
        '''
    }

    @Test
    void testLeftAssociativityIsPreserved() {
        assertScript '''
            def a = 10
            assert 20 - (a) - 5 == 5
        '''
    }

    @Test
    void testNestedParenthesizedNameInsidePrimitiveCast() {
        assertScript '''
            def testObj = 0
            def r = (int)(testObj) + 10
            assert r == 10
        '''
    }

    @Test
    void testRebalancingAcrossPrecedenceLevels() {
        assertScript '''
            def a = 4
            assert 2 * (a) - 3 == 5        // (2 * a) - 3
            assert 2 ** (a) - 3 == 13      // (2 ** a) - 3
            assert -(a) - 3 == -7          // (-a) - 3
            assert 3 - ((a) - 2) == 1      // parentheses shield the inner grouping
            def x = 2, y = 3
            assert x * (a) - 3 * y == -1   // (x * a) - (3 * y)
        '''
    }

    @Test
    void testParenthesizedNameBeforeInAndAs() {
        assertScript '''
            def a = 1
            def b = [1, 2]
            def r = (a) in b
            assert r
            if ((a) in b) {} else assert false
            assert ((a) as Long) instanceof Long
        '''
        // `in`/`as` take the binary reading for capitalized names too
        assertScript '''
            def A = 1
            def B = [1, 2]
            def r = (A) in B
            assert r
            assert ((A) as Long) instanceof Long
        '''
        // collection literals after `in` parse as a subscript on the keyword identifier
        assertScript '''
            def a = ""
            def r = (a) in []
            assert r == false
            def b = 1
            assert ((b) in [1, 2])
            assert ((b) in [1])
            assert !((b) in [k: 1])
            def k = "k"
            assert ((k) in [k: 1])
        '''
    }

    @Test
    void testUnderscoreAndDollarNamesAreValues() {
        assertScript '''
            def _foo = 1
            assert (_foo) + 1 == 2
            def $bar = 2
            assert ($bar) - 1 == 1
        '''
    }

    @Test
    void testInGroupsAtRelationalPrecedence() {
        // the repaired `in` binds like the unparenthesized form: tighter than
        // logical/equality operators, left-associative with a further `in`
        assertScript '''
            def x = 1
            def list = [1]
            assert ((x) in list && true)
            def r = (x) in list && true
            assert r == (x in list && true)
            if ((x) in list && true) {} else assert false
            assert ((x) in list == true)
            assert ((x) in list | false)
        '''
        assertScript '''
            def x = 1
            def a = [1]
            def b = [true]
            def r = (x) in a in b
            assert r == (x in a in b)
            assert r
        '''
        assertScript '''
            def x = 1
            def list = [1]
            assert ((x) in list ? 'y' : 'n') == 'y'
            def empty = []
            def s = (x) in empty ?: 'none'
            assert s == 'none'
        '''
    }

    @Test
    void testAsWithGenericArrayAndFollowingOperators() {
        assertScript '''
            def x = []
            def r = (x) as List<String>
            assert r instanceof List
        '''
        assertScript '''
            def x = ['a', 'b']
            def r = (x) as String[]
            assert r instanceof String[] && r.length == 2
        '''
        assertScript '''
            def x = null
            def r = (x) as Long ?: 42
            assert r == 42
            def y = 5
            def s = (y) as Long ?: 42
            assert s == 5L && s instanceof Long
        '''
    }

    @Test
    void testCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            def m() {
                String b = "B"
                int answer = 42
                assert "A" + (b) + "C" == 'ABC'
                assert (answer) - 3 == 39
            }
            m()
        '''
    }

    @Test
    void testConventionalCastsAreUnchanged() {
        assertScript '''
            assert (Integer) -1 == -1
            assert ((Integer) -1) instanceof Integer
            assert (java.lang.Integer) -1 == -1
            assert (int) -1 == -1
            assert (Integer) ~1 == -2
            def flag = true
            assert (Boolean) !flag == false
            def pre = 1
            assert (Integer) ++pre == 2
            def x = []
            assert (List<String>) x == []
            def sam = (Runnable) { }
            assert sam instanceof Runnable
            def coerced = (ArrayList) [1, 2]
            assert coerced instanceof ArrayList
        '''
    }

    @Test
    void testPostfixOnParenthesizedName() {
        assertScript '''
            def p = 1
            def q = (p)++
            assert q == 1 && p == 2
        '''
    }

    @Test
    void testUnresolvedBareNameCastHint() {
        // capitalized names keep the cast reading; the resolve error explains the ambiguity
        def err = shouldFail '''
            def ASDF = 'x'
            def r = (ASDF) + ""
        '''
        assert err.message.contains('unable to resolve class ASDF')
        assert err.message.contains("((ASDF))")
    }

    @Test
    void testUnresolvedBareNameCastHintForSubscriptCallAndClosureOperands() {
        // "(a)[b]" is a cast of the list literal [b] to type a; likewise "(f)(3)" is a cast
        // of the parenthesized 3 and "(r) { }" a cast of the closure — all keep the cast
        // reading, so each failure must carry the hint
        [
            'def a = [10, 20]; def b = 1\ndef r = (a)[b]',
            'def f = { it * 2 }\ndef r = (f)(3)',
            'def r = (r) { }',
        ].each { src ->
            def err = shouldFail(src)
            assert err.message.contains('unable to resolve class')
            assert err.message.contains('parsed as a cast')
        }
    }

    @Test
    void testMapLiteralOperandGetsCoercionHintNotWorkaround() {
        // "(a) [k: 1]" has no alternative value reading — a subscript may not contain map
        // entries either — so the hint explains the coercion requirement instead of
        // suggesting the double-parentheses workaround, which cannot help here
        def err = shouldFail '''
            def a = [:]
            def r = (a) [k: 1]
        '''
        assert err.message.contains('unable to resolve class a')
        assert err.message.contains('map entries are not allowed')
        assert !err.message.contains('((a))')
        // the capitalized coercion idiom itself is untouched
        assertScript '''
            @groovy.transform.Canonical class Point { int x, y }
            def p = (Point) [x: 3, y: 4]
            assert p.x == 3 && p.y == 4
        '''
    }

    @Test
    void testKeywordShapesWithoutFaithfulRewriteCarryHints() {
        // a parenthesized right-hand side parses as a method call on the keyword identifier,
        // so the cast reading survives; the failure must carry the standard hint
        [
            'def x = 1; def list = [1]\ndef r = (x) in (list)',
            'def x = 1\ndef r = (x) as (Long)',
        ].each { src ->
            def err = shouldFail(src)
            assert err.message.contains('unable to resolve class x')
            assert err.message.contains('parsed as a cast')
        }
        // an argument that cannot be read as a coercion type bails out of the repair;
        // the failure explains how the expression was read
        def err = shouldFail '''
            def x = 1
            def r = (x) as 3
        '''
        assert err.message.contains('unable to resolve class x')
        assert err.message.contains("binary 'as'")
    }

    @Test
    void testUnaryAndCastWrappersSpanOnlyTheirOperand() {
        def block = new AstBuilder().buildFromString(CompilePhase.CONVERSION, true, '''
            def a = 4
            def r = -(a) - 3
            def f = true
            def s = !(f) - 1
            def t = 0
            def u = (int)(t) + 10
        ''')[0]
        def wrappers = []
        def visitor = new CodeVisitorSupport() {
            @Override
            void visitUnaryMinusExpression(UnaryMinusExpression expression) {
                wrappers << expression
                super.visitUnaryMinusExpression(expression)
            }
            @Override
            void visitNotExpression(NotExpression expression) {
                wrappers << expression
                super.visitNotExpression(expression)
            }
            @Override
            void visitCastExpression(CastExpression expression) {
                wrappers << expression
                super.visitCastExpression(expression)
            }
        }
        block.statements.each { it.visit(visitor) }
        assert wrappers.size() == 3
        wrappers.each { wrapper ->
            assert wrapper.lastLineNumber == wrapper.expression.lastLineNumber
            assert wrapper.lastColumnNumber == wrapper.expression.lastColumnNumber
        }
    }

    @Test
    void testRebalancedNodesHaveSourcePositions() {
        def block = new AstBuilder().buildFromString(CompilePhase.CONVERSION, true, '''
            def b = "B"
            def r = "A" + (b) + "C"
            def a = 4
            def s = 2 * (a) - 3
        ''')[0]
        def missing = []
        def visitor = new CodeVisitorSupport() {
            @Override
            void visitBinaryExpression(BinaryExpression expression) {
                if (expression.lineNumber == -1) missing << expression.text
                super.visitBinaryExpression(expression)
            }
        }
        block.statements.each { it.visit(visitor) }
        assert missing.isEmpty()
    }

    @Test
    void testDoubleParenthesesWorkaround() {
        assertScript '''
            def ASDF = 'x'
            assert ((ASDF)) + "" == 'x'
        '''
        // the workaround the hint suggests restores the value reading for every shape
        assertScript '''
            def a = [10, 20]
            def b = 1
            assert ((a))[b] == 20
        '''
        assertScript '''
            def f = { it * 2 }
            assert ((f))(3) == 6
        '''
    }
}
