/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

/**
 * Tests for DGM methods on CharSequence.
 *
 * @author Paul King
 */
class GroovyCharSequenceMethodsTest extends GroovyTestCase {

    def cs1 = [
            toString:{ -> 'Today is Thu Jul 28 06:38:07 EST 2011' },
            length:{->37}
    ] as CharSequence
    def cs2 = [
            toString:{ -> 'Foobar' },
            subSequence:{int f, int t->'Foobar'.substring(f, t)},
            length:{->6},
            charAt:{ int i -> 'Foobar'.chars[i] }
    ] as CharSequence
    def cs3 = [
            toString: { -> '''\
                |Foo
                |bar
                |'''
            }
    ] as CharSequence
    def csEmpty = [toString:{->''}, length:{->0}] as CharSequence

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

    void testSize() {
        assert cs1.size() == 37
    }

    void testGetAtRange() {
        assert cs2.getAt(1..3) == 'oob'
        assert cs2[1..3] == 'oob'
        assert cs2.getAt(3..1) == 'boo'
        assert cs2.getAt(-3..-1) == 'bar'
        assert cs2[-3..-1] == 'bar'
    }

    void testGetAtCollection() {
        assert cs2.getAt([0, 4, 5]) == 'Far'
        assert cs2[1, 3, 5] == 'obr'
        assert cs2[3, 5, 2] == 'bro'
    }

    void testGetAtEmptyRange() {
        assert cs2.getAt(new EmptyRange(null)) == ''
    }

    void testReverse() {
        assert cs2.reverse() == 'rabooF'
    }

    void testStripMargin() {
        assert cs3.stripMargin() == 'Foo\nbar\n'
    }

    void testStripIndent() {
        assert cs3.stripIndent() == '|Foo\n|bar\n|'
    }

    void testIsAllWhitespace() {
        assert !cs2.isAllWhitespace()
        assert ([toString:{->' \t\n\r'}] as CharSequence).isAllWhitespace()
    }

    void testReplace() {
        assert cs2.replaceFirst(~/[ab]/, csEmpty) == 'Fooar'
        assert cs2.replaceAll(~/[ab]/, csEmpty) == 'Foor'
    }

    void testMatches() {
        assert cs2.matches(~/.oo.*/)
        assert !cs2.matches(~/.*z.*/)
    }

    void testFind() {
        def csDigits = [toString:{->/\d{4}/}] as CharSequence
        assert cs1.find(csDigits) == '2011'
        assert cs1.find(csDigits, {"--$it--"}) == '--2011--'
        assert cs1.find(~/\d\d:\d\d/) == '06:38'
        assert cs1.find(~/\d\d:\d\d/, {"|$it|"}) == '|06:38|'
    }

    void testFindAll() {
        def csDigits = [toString:{->/\d\d/}] as CharSequence
        assert cs1.findAll(csDigits) == ['28', '06', '38', '07', '20', '11']
        assert cs1.findAll(csDigits, {"<$it>"}) == ['<28>', '<06>', '<38>', '<07>', '<20>', '<11>']
        assert cs1.findAll(~/\s\d\d/) == [' 28', ' 06', ' 20']
        assert cs1.findAll(~/\s\d\d/, {"<$it >"}) == ['< 28 >', '< 06 >', '< 20 >']
    }

    void testPad() {
        assert cs2.padLeft(10) == '    Foobar'
        assert cs2.padLeft(10, '+') == '++++Foobar'
        assert cs2.padRight(10) == 'Foobar    '
        assert cs2.padRight(10, '+') == 'Foobar++++'
        assert cs2.center(10) == '  Foobar  '
        assert cs2.center(10, '+') == '++Foobar++'
    }

    void testDrop() {
        assert cs2.drop(3) == 'bar'
    }

    void testAsBoolean() {
        assert cs1 && cs2
        assert !csEmpty
    }

    void testLeftShift() {
        assert cs2.leftShift('___').toString() == 'Foobar___'
        assert (cs2 << 'baz').toString() == 'Foobarbaz'
    }

    void testSplit() {
        def parts = cs1.split()
        assert parts instanceof CharSequence[]
        assert parts.size() == 8
        assert parts[2] == 'Thu'
    }

    void testTr() {
        assert cs1.tr(':uoa', '/___') == 'T_d_y is Th_ J_l 28 06/38/07 EST 2011'
    }

}
