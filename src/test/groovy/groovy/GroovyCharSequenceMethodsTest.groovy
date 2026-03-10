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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript


/**
 * Tests for DGM methods on CharSequence.
 */
class GroovyCharSequenceMethodsTest {

    private static CharSequence makeCharSequence(String s) {
        [
                toString   : { -> s },
                subSequence: { int f, int t -> s.substring(f, t) },
                length     : { -> s.length() },
                charAt     : { int i -> s.chars[i] },
        ] as CharSequence
    }

    def cs1 = makeCharSequence('Today is Thu Jul 28 06:38:07 EST 2011')

    def cs2 = makeCharSequence('Foobar')

    def cs3 = makeCharSequence('''\
                |Foo
                |bar
                |''')

    def csEmpty = makeCharSequence('')

    @Test
    void testIsCase() {
        // direct
        assert cs2.isCase('Foobar')
        assert !cs2.isCase('Baz')
        // typical usage
        switch(cs2) {
            case 'Foobar': break
            default: assert false, 'Should not get here'
        }
    }

    @Test
    void testSize() {
        assert cs1.size() == 37
    }

    @Test
    void testGetAtRange() {
        assert cs2.getAt(1..3) == 'oob'
        assert cs2[1..3] == 'oob'
        assert cs2.getAt(3..1) == 'boo'
        assert cs2.getAt(-3..-1) == 'bar'
        assert cs2[-3..-1] == 'bar'
    }

    @Test
    void testGetAtCollection() {
        assert cs2.getAt([0, 4, 5]) == 'Far'
        assert cs2[1, 3, 5] == 'obr'
        assert cs2[3, 5, 2] == 'bro'
    }

    @Test
    void testGetAtEmptyRange() {
        assert cs2.getAt(new EmptyRange(null)) == ''
    }

    @Test
    void testReverse() {
        assert cs2.reverse() == 'rabooF'
    }

    @Test
    void testStripMargin() {
        assert cs3.stripMargin() == 'Foo\nbar\n'
    }

    @Test
    void testStripIndent() {
        assert cs3.stripIndent() == '|Foo\n|bar\n|'
    }

    @Test
    void testIsAllWhitespace() {
        assert !cs2.isAllWhitespace()
        assert makeCharSequence(' \t\n\r').isAllWhitespace()
    }

    @Test
    void testReplace() {
        assert cs2.replaceFirst(~/[ab]/, csEmpty) == 'Fooar'
        assert cs2.replaceAll(~/[ab]/, csEmpty) == 'Foor'
    }

    @Test
    void testMatches() {
        assert cs2.matches(~/.oo.*/)
        assert !cs2.matches(~/.*z.*/)
    }

    @Test
    void testFind() {
        def csDigits = makeCharSequence(/\d{4}/)
        assert cs1.find(csDigits) == '2011'
        assert cs1.find(csDigits, {"--$it--"}) == '--2011--'
        assert cs1.find(~/\d\d:\d\d/) == '06:38'
        assert cs1.find(~/\d\d:\d\d/, {"|$it|"}) == '|06:38|'
    }

    @Test
    void testFindAll() {
        def csDigits = makeCharSequence(/\d\d/)
        assert cs1.findAll(csDigits) == ['28', '06', '38', '07', '20', '11']
        assert cs1.findAll(csDigits, {"<$it>"}) == ['<28>', '<06>', '<38>', '<07>', '<20>', '<11>']
        assert cs1.findAll(~/\s\d\d/) == [' 28', ' 06', ' 20']
        assert cs1.findAll(~/\s\d\d/, {"<$it >"}) == ['< 28 >', '< 06 >', '< 20 >']
    }

    @Test
    void testPad() {
        assert cs2.padLeft(10) == '    Foobar'
        assert cs2.padLeft(10, '+') == '++++Foobar'
        assert cs2.padRight(10) == 'Foobar    '
        assert cs2.padRight(10, '+') == 'Foobar++++'
        assert cs2.center(10) == '  Foobar  '
        assert cs2.center(10, '+') == '++Foobar++'
    }

    @Test
    void testDrop() {
        assert cs2.drop(3) == 'bar'
    }

    @Test
    void testDropTakeTC() {
        assertScript '''
            @groovy.transform.TypeChecked
            def method() {
                assert 'Foo Bar'.drop(4).toLowerCase() == 'bar'
                assert 'Foo Bar'.take(3).toLowerCase() == 'foo'
            }

            method()
        '''
    }

    @Test
    void testAsBoolean() {
        assert cs1 && cs2
        assert !csEmpty
    }

    @Test
    void testLeftShift() {
        assert cs2.leftShift('___').toString() == 'Foobar___'
        assert (cs2 << 'baz').toString() == 'Foobarbaz'
    }

    @Test
    void testSplit() {
        def parts = cs1.split()
        assert parts instanceof CharSequence[]
        assert parts.size() == 8
        assert parts[2] == 'Thu'
    }

    @Test
    void testTokenize() {
        def parts = cs1.tokenize()
        assert parts instanceof List
        assert parts.size() == 8
        assert parts[2] == 'Thu'
        parts = cs1.tokenize(':')
        assert parts.size() == 3
        assert parts[1] == '38'
    }

    @Test
    void testCapitalize() {
        def csfoo = makeCharSequence('foo')
        assert csfoo.capitalize() == 'Foo'
        assert cs2.capitalize() == 'Foobar'
    }

    @Test
    void testUncapitalize() {
        def csfoo = makeCharSequence('Foo')
        assert csfoo.uncapitalize() == 'foo'
        assert cs2.uncapitalize() == 'foobar'
    }

    @Test
    void testExpand() {
        def csfoobar = makeCharSequence('foo\tbar')
        assert csfoobar.expand() == 'foo     bar'
        assert csfoobar.expand(4) == 'foo bar'
        csfoobar = makeCharSequence('\tfoo\n\tbar')
        assert csfoobar.expand(4) == '    foo\n    bar'
    }

    @Test
    void testUnexpand() {
        def csfoobar = makeCharSequence('foo     bar')
        assert csfoobar.unexpand() == 'foo\tbar'
        assert csfoobar.unexpand(4) == 'foo\t\tbar'
        csfoobar = makeCharSequence('     foo\n    bar')
        assert csfoobar.unexpand(4) == '\t foo\n\tbar'
    }

    @Test
    void testPlus() {
        assert cs2.plus(42) == 'Foobar42'
        assert cs2 + 42 == 'Foobar42'
    }

    @Test
    void testMinus() {
        def csoo = makeCharSequence('oo')
        assert cs2.minus(42) == 'Foobar'
        assert cs2.minus(csoo) == 'Fbar'
        assert cs2 - csoo == 'Fbar'
    }

    @Test
    void testContains() {
        def csoo = makeCharSequence('oo')
        def csbaz = makeCharSequence('baz')
        assert cs2.contains(csoo)
        assert !cs2.contains(csbaz)
    }

    @Test
    void testCount() {
        def cszero = makeCharSequence('0')
        def csbar = makeCharSequence('|')
        assert cs1.count(cszero) == 3
        assert cs3.count(csbar) == 3
    }

    @Test
    void testNext() {
        assert cs2.next() == 'Foobas'
        assert ++cs2 == 'Foobas'
    }

    @Test
    void testPrevious() {
        assert cs2.previous() == 'Foobaq'
        assert --cs2 == 'Foobaq'
    }

    @Test
    void testMultiply() {
        assert cs2.multiply(2) == 'FoobarFoobar'
        assert cs2 * 3 == 'FoobarFoobarFoobar'
    }

    @Test
    void testToInteger() {
        def csFourteen = makeCharSequence('014')
        assert csFourteen.isInteger()
        def fourteen = csFourteen.toInteger()
        assert fourteen instanceof Integer
        assert fourteen == 14
    }

    @Test
    void testToLong() {
        def csFourteen = makeCharSequence('014')
        assert csFourteen.isLong()
        def fourteen = csFourteen.toLong()
        assert fourteen instanceof Long
        assert fourteen == 14L
    }

    @Test
    void testToShort() {
        def csFourteen = makeCharSequence('014')
        def fourteen = csFourteen.toShort()
        assert fourteen instanceof Short
        assert fourteen == 14
    }

    @Test
    void testToBigInteger() {
        def csFourteen = makeCharSequence('014')
        assert csFourteen.isBigInteger()
        def fourteen = csFourteen.toBigInteger()
        assert fourteen instanceof BigInteger
        assert fourteen == 14G
    }

    @Test
    void testToFloat() {
        def csThreePointFive = makeCharSequence('3.5')
        assert csThreePointFive.isFloat()
        def threePointFive = csThreePointFive.toFloat()
        assert threePointFive instanceof Float
        assert threePointFive == 3.5
    }

    @Test
    void testToDouble() {
        def csThreePointFive = makeCharSequence('3.5')
        assert csThreePointFive.isDouble()
        def threePointFive = csThreePointFive.toDouble()
        assert threePointFive instanceof Double
        assert threePointFive == 3.5D
    }

    @Test
    void testToBigDecimal() {
        def csThreePointFive = makeCharSequence('3.5')
        assert csThreePointFive.isBigDecimal()
        assert csThreePointFive.isNumber()
        def threePointFive = csThreePointFive.toBigDecimal()
        assert threePointFive instanceof BigDecimal
        assert threePointFive == 3.5G
    }

    @Test
    void testEachLine() {
        def result = []
        cs3.eachLine{ line, num -> result << "$num:${line.size()}" }
        assert result == ["0:20", "1:20", "2:17"]
        result = []
        cs3.eachLine(10){ line, num -> result << "$num:${line.size()}" }
        assert result == ["10:20", "11:20", "12:17"]
    }

    @Test
    void testSplitEachLine() {
        def regexOp = /\s*\*\s*/
        def csOp = makeCharSequence(regexOp)
        def csTwoLines = makeCharSequence('10*15\n11 * 9')
        def result = []
        csTwoLines.splitEachLine(csOp){ left, right -> result << left.toInteger() * right.toInteger() }
        assert result == [150, 99]
        result = []
        csTwoLines.splitEachLine(~regexOp){ left, right -> result << left.toInteger() * right.toInteger() }
        assert result == [150, 99]
    }

    @Test
    void testReadLines() {
        def lines = cs3.readLines()
        assert lines.size() == 3
        assert lines[1].trim() == '|bar'
    }

    @Test
    void testToList() {
        def chars = cs2.toList()
        assert chars.size() == 6
        assert chars[0] == 'F'
        assert chars[3] == 'b'
        assert chars[-1] == 'r'
    }

    @Test
    void testToSet() {
        def chars = cs2.toSet()
        assert chars.size() == 5
        assert 'F' in chars
        assert 'b' in chars
    }

    @Test
    void testGetChars() {
        def chars = cs2.chars
        assert chars instanceof char[]
        assert chars.size() == 6
        assert chars[0] == 'F'
        assert chars[3] == 'b'
        assert chars[-1] == 'r'
    }

    @Test
    void testGetCodePoints() {
        def ints = cs2.codePoints
        assert ints instanceof int[]
        assert ints.size() == 6
        assert ints[0] == 70
        assert ints[3] == 98
        assert ints[-1] == 114
    }

    private enum Coin { penny, nickel, dime, quarter }
    @Test
    void testAsType() {
        def csDime = makeCharSequence('dime')
        def dime = csDime as Coin
        assert dime instanceof Coin
        assert dime == Coin.dime
    }

    @Test
    void testEachMatch() {
        def result = []
        def regexDigits = /(\d)(.)(\d)/
        def csDigits = makeCharSequence(regexDigits)
        assert cs1.eachMatch(csDigits) { all, first, delim, second -> result << "$first $delim $second" }
        assert result == ['8   0', '6 : 3', '8 : 0', '2 0 1']
        result = []
        assert cs1.eachMatch(~regexDigits) { all, first, delim, second -> result << "$first $delim $second" }
        assert result == ['8   0', '6 : 3', '8 : 0', '2 0 1']
    }

    @Test
    void testTr() {
        assert cs1.tr(':uoa', '/___') == 'T_d_y is Th_ J_l 28 06/38/07 EST 2011'
    }

    @Test
    void testReplaceAllFirst() {
        def csDigit = makeCharSequence(/\d/)
        def csUnder = makeCharSequence(/_/)

        assert cs1.replaceAll(~/\d/, csUnder) == 'Today is Thu Jul __ __:__:__ EST ____'
        assert cs1.replaceAll(csDigit, csUnder) == 'Today is Thu Jul __ __:__:__ EST ____'
        assert cs1.replaceAll(csDigit, { '_' }) == 'Today is Thu Jul __ __:__:__ EST ____'
        assert cs1.replaceFirst(~/\d/, csUnder) == 'Today is Thu Jul _8 06:38:07 EST 2011'
        assert cs1.replaceFirst(csDigit, csUnder) == 'Today is Thu Jul _8 06:38:07 EST 2011'
        assert cs1.replaceFirst(csDigit, { '_' }) == 'Today is Thu Jul _8 06:38:07 EST 2011'
    }

    @Test
    void testNormalizeDenormalize() {
        def text = 'the quick brown\nfox jumped\r\nover the lazy dog'
        def csText = makeCharSequence(text)
        assert csText.normalize() == text.normalize()
        assert csText.normalize().denormalize() == text.normalize().denormalize()
    }

}
