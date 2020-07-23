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

import java.lang.reflect.Field

class GStringTest extends GroovyTestCase {

    void check(template, teststr) {
        assert template instanceof GString

        def count = template.getValueCount()
        assert count == 1
        assert template.getValue(0) == "Bob"

        def string = template.toString()
        assert string == teststr
    }

    void testEmptyGString() {
        def foo = 'Foo'
        def bar = 'Bar'
        def g = GString.EMPTY + "$foo".toString() + "$bar"
        assert g instanceof GString
        assert g.values == ['Bar']
        assert 'FooBar' == g
    }

    void testWithOneVariable() {
        def name = "Bob"
        def teststr = "hello Bob how are you?"

        check("hello $name how are you?", teststr)
        check("hello ${name} how are you?", teststr)
        check("hello ${(name + '  ').trim()} how are you?", teststr)
        check(/hello $name how are you?/, teststr)
        check(/hello ${name} how are you?/, teststr)
        check(/hello ${(name + '  ').trim()} how are you?/, teststr)
    }

    void testWithVariableAtEnd() {
        def name = "Bob"
        def teststr = "hello Bob"

        check("hello $name", teststr)
        check("hello ${name}", teststr)
        check(/hello $name/, teststr)
        check(/hello ${name}/, teststr)
    }

    void testWithVariableAtBeginning() {
        def name = "Bob"
        def teststr = "Bob hey"
        check("$name hey", teststr)
        check("${name} hey", teststr)
        name = ""
        check("${name += "Bob"; name} hey", teststr)
        assert name == "Bob"
        check(/$name hey/, teststr)
        check(/${name} hey/, teststr)
        name = ""
        check(/${name += "Bob"; name} hey/, teststr)
    }

    void testWithJustVariable() {
        def teststr
        def name = teststr = "Bob"
        check("$name", teststr)
        check("${name}", teststr)
        check("${assert name == "Bob"; name}", teststr)
        // Put punctuation after the variable name:
        check("$name.", "Bob.")
        check("$name...", "Bob...")
        check("$name?", "Bob?")

        check(/$name/, teststr)
        check(/${name}/, teststr)
        check(/${assert name == "Bob"; name}/, teststr)
        // Put punctuation after the variable name:
        check(/$name./, "Bob.")
        check(/$name.../, "Bob...")
        check(/$name?/, "Bob?")
        check(/$name\?/, "Bob\\?")
        check(/$name$/, "Bob\$")

        def person = [name: name]
        check("${person.name}", "Bob")
        check("$person.name", "Bob")
        check("$person.name.", "Bob.")
        check("$person.name...", "Bob...")
        check("$person.name?", "Bob?")
        check(/$person.name/, "Bob")
        check(/$person.name./, "Bob.")
        check(/$person.name.../, "Bob...")
        check(/$person.name?/, "Bob?")
        check(/$person.name\?/, "Bob\\?")
        check(/$person.name$/, "Bob\$")
    }

    void testWithTwoVariables() {
        def name = "Bob"
        def template = "${name}${name}"
        def string = template.toString()

        assert string == "BobBob"
    }

    void testWithTwoVariablesWithSpace() {
        def name = "Bob"
        def template = "${name} ${name}"
        def string = template.toString()

        assert string == "Bob Bob"
    }

    void testAppendString() {
        def a = "dog"
        def b = "a ${a}"
        def c = b + " cat"
        assert c.toString() == "a dog cat", c

        b += " cat"
        assert b.toString() == "a dog cat", b
    }

    void testAppendGString() {
        def a = "dog"
        def b = "a ${a}"
        b += " cat${a}"

        assert b.toString() == "a dog catdog", b
    }

    void testReturnString() {
        def value = dummyMethod()
        assert value == "Hello Gromit!"
    }

    String dummyMethod() {
        def name = "Gromit"
        return "Hello ${name}!"
    }

    void testCoerce() {
        def enc = "US-ASCII"
        def value = "test".getBytes("${enc}")
         assert value == [116, 101, 115, 116]
    }

    void testGroovy441() {
        def arg = "test"
        def content = "${arg} ="
        if (arg != "something") {
            content += "?"
        }
        content += "= ${arg}."
        assert content == "test =?= test."
    }

    void testTwoStringsInMiddle() {
        def a = "---"
        def b = "${a} :"
        b += "<<"
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<<>>: ---"
    }

    void testAlternatingGStrings() {
        def a = "---"
        def b = "${a} :"
        b += "<<"
        b += " [[${a}]] "
        b += ">>"
        b += ": ${a}"
        assert b == "--- :<< [[---]] >>: ---"
    }

     // Test case for GROOVY-599
    void testGStringInStaticMethod() {
        int value = 2
        String str = "1${value}3"
        int result = Integer.parseInt(str)
        assert result == 123
        result = Integer.parseInt("1${value}3")
        assert result == 123
    }

    // Test case for GROOVY-2275
    void testGetAtWithRange() {
        def number = 1234567
        def numberString = "${number}"
        def realString = "1234567"
        assert numberString[0..-1] == '1234567'
        assert realString[0..-1] == '1234567'
    }

    void testEmbeddedClosures() {
        def c1 = {-> "hello"}
        def c2 = {out -> out << "world"}
        def c3 = {a, b -> b << a}
        def c4 = c3.curry(5)

        def g1 = "${-> "hello"} ${out -> out << "world"}"
        def g2 = "$c1 $c2"
        def g3 = "${-> c1} ${-> c2}"
        def g4 = "$c4"
        def g5 = "$c3"

        def w = new StringWriter()
        w << g1
        assertEquals(w.buffer.toString(), "hello world")
        assertEquals(g1.toString(), "hello world")
        w = new StringWriter()
        w << g2
        assertEquals(w.buffer.toString(), "hello world")
        assertEquals(g2.toString(), "hello world")
        w = new StringWriter()
        w << g3
        assert w.buffer.toString().contains("closure")
        assert g3.toString().contains("closure")
        w = new StringWriter()
        w << g4
        assertEquals(w.buffer.toString(), "5")
        assertEquals(g4.toString(), "5")
        try {
            w << g5
            fail("should throw a GroovyRuntimeException")
        } catch (GroovyRuntimeException e) {
        }
        try {
            g5.toString()
            fail("should throw a GroovyRuntimeException")
        } catch (GroovyRuntimeException e) {
        }
    }

    /**
     * Tests comparing two strings which have the same string representation but
     * only one of which uses a template. This is a test for GROOVY-626.
     */
    void testEqualsTemplateToLiteral() {
        def template = "${2}"
        def literal = "2"

        // succeeds
        assertTrue("template == literal false", template == literal)
        assertTrue("literal == template false", literal == template)

        // these fail
        assertFalse("literal not equal to template", literal.equals(template))
        assertFalse("template not equal to literal", template.equals(literal))
        assertTrue("hash codes not equal", literal.hashCode() != template.hashCode())
    }

    /**
     * Tests getting a character by index where the index is a reference instead of a constant.
     * This is a test for GROOVY-1139.
     */
    void testCharAtWithIntegerReference() {
        def literal = "0123456789";
        def template = "${literal}";

        def i = 0
        assertEquals("wrong character at position 0", '0', literal[i]);
        assertEquals("wrong character at position 0", '0', template[i]);

        i = 5
        assertEquals("wrong character at position 5", '5', literal[i]);
        assertEquals("wrong character at position 5", '5', template[i]);

        i = 9
        assertEquals("wrong character at position 9", '9', literal[i]);
        assertEquals("wrong character at position 9", '9', template[i]);
    }

    /**
     * Tests getting a character by index, counting from the beginning of the string.
     */
    void testCharAtFromStart() {
        def literal = "0123456789";
        def template = "${literal}";

        assertEquals("wrong character at position 0", '0', literal[0]);
        assertEquals("wrong character at position 5", '5', literal[5]);
        assertEquals("wrong character at position 9", '9', literal[9]);

        assertEquals("wrong character at position 0", '0', template[0]);
        assertEquals("wrong character at position 5", '5', template[5]);
        assertEquals("wrong character at position 9", '9', template[9]);
    }

    /**
     * Tests getting a character by index, counting from the end of the string.
     */
    void testCharAtFromEnd() {
        def literal = "0123456789";
        def template = "${literal}";

        assertEquals("wrong character at position -1", '9', literal[-1]);
        assertEquals("wrong character at position -5", '5', literal[-5]);
        assertEquals("wrong character at position -10", '0', literal[-10]);

        assertEquals("wrong character at position -1", '9', template[-1]);
        assertEquals("wrong character at position -5", '5', template[-5]);
        assertEquals("wrong character at position -10", '0', template[-10]);
    }

    /**
     * Tests extracting a range which starts at position zero and ends before the
     * end of the string.
     */
    private void doTestExtractRangeStartToBeforeEnd(string) {
        // inclusive
        assertEquals("string[0..-3]", "01234567", string[0..-3]);

        // exclusive
        assertEquals("string[0..<-3]", "0123456", string[0..<-3]);
    }

    /**
     * Tests extracting a range which starts before the end of the string and ends at 0.
     * For an inclusive range, this reverses the string.  For an exclusive range, this
     * counts from the end to the last character.
     */
    void doTestExtractRangeBeforeEndToStart(string) {
        // inclusive
        assertEquals("string[-3..0]", "76543210", string[-3..0]);

        // exclusive
        assertEquals("string[-3..<0]", "7654321", string[-3..<0]);
    }

    /**
     * Calls <code>doTestExtractRangeStartToBeforeEnd</code> with a non-template string.
     *
     * GROOVY-781
     */
    void testExtractRangeStartToBeforeEndLiteral() {
        def literal = "0123456789";
        doTestExtractRangeStartToBeforeEnd(literal);
    }

    /**
     * Calls <code>doTestExtractRangeStartToBeforeEnd</code> with a template string.
     *
     * GROOVY-781
     */
    void testExtractRangeStartToBeforeEndTemplate() {
        def literal = "0123456789";
        def template = "${literal}";

        doTestExtractRangeStartToBeforeEnd(template);
    }

    /**
     * Calls <code>doTestExtractRangeBeforeEndToStart</code> with a non-template string.
     *
     * GROOVY-781
     */
    void testExtractRangeBeforeEndToStartLiteral() {
        def literal = "0123456789";
        doTestExtractRangeBeforeEndToStart(literal);
    }

    /**
     * Calls <code>doTestExtractRangeBeforeEndToStart</code> with a template string.
     *
     * GROOVY-781
     */
    void testExtractRangeBeforeEndToStartTemplate() {
        def literal = "0123456789";
        def template = "${literal}";

        doTestExtractRangeBeforeEndToStart(template);
    }

    /**
     * Tests replacing a value in a string with an undefined variable.
     */
    void testReplaceValueWithUndefinedVariable() {
        try {
            def str = "replace <${undefined}>"
            fail("undefined value not detected");
        }
        catch (MissingPropertyException e) {
        }
    }

    /**
     * Tests replacing a value with a null expression.
     */
    void testReplaceValueWithNullExpression() {
        def str = "replace <${null}>";
        assertEquals(str, 'replace <null>');
    }

    /**
     * Tests replacing a value with a compound expression.
     */
    void testReplaceValueWithCompoundExpression() {
        def i = 1, j = 2
        def str = "replace <${i; j}>"
        assertEquals('value replaced', 'replace <2>', str)
    }

    /**
     * Tests incrementing the variable being substituted after creating the string.  This
     * shouldn't have any effect because the string holds a reference to the original value.
     */
    void testIncrementAfterCreatingString() {
        def i = 1
        def str = "replace <${i}> <${i * 2}>"
        assertEquals("value ok", "replace <1> <2>", str)
        i++
        assertEquals("value ok", "replace <1> <2>", str)
    }

    /**
     * Tests evaluating a closure embedded in a string.
     */
    void testClosureInString() {
        def i = 1
        def closure = {i};
        def str = "<${closure()}>"
        assertEquals('closure replacement ok', '<1>', str)

        // this has no effect because the closure is only evaluated once when the string
        // is created
        i++
        assertEquals('closure replacement ok', '<1>', str)
    }

    /**
     * Tests embedding a mutable object in a string.
     */
    void testEmbedMutableObject() {
        def buffer = new StringBuffer("value")
        def stringValue = "value";
        def str = "buffer: <${buffer}>"
        assertEquals("buffer: <value>", str)
    }

    /**
     * Tests modifying a string embedded in another string, which should have no effect.
     */
    void testAppendToEmbeddedStringValue() {
        def stringValue = "value";
        def str = "string: <${stringValue}>"
        assertEquals("string: <value>", str)

        // this has no effect because the string contains a reference stringValue
        // and += for strings creates a new string instead of modifying the existing value.
        stringValue += " more"
        assertEquals("string: <value>", str)
    }

    /**
     * Tests including a map in a string
     */
    void testMapInString() {
        def map = ["key": 1];
        def str = "map.key: <${map.key}>; map: <${map}>";
        assertEquals("map replacement ok", 'map.key: <1>; map: <[key:1]>', str)
        map.key++;

        // The map shows the effects of the change because the string holds a reference
        // to the mutable map.  map.key doesn't show the effect of the change because
        // in this slot the string holds a reference to the original value and
        // map.kep++ created a new value which was stored in the map.
        assertEquals("map replacement ok", 'map.key: <1>; map: <[key:2]>', str)
    }

    /**
     * Tests including a string in itself recursively.
     */
    void testRecursiveReplacement() {
        def str = "1";
        str = "<${str}>";
        assertEquals("recursive string replaced", '<1>', str);
    }

    /**
     * Void method
     */
    void doNothing()
    {
    }

    /**
     * Tests replacing a value with a void statement.
     */
    void testReplaceValueWithVoidStatement() {
        def str = "replace <${doNothing()}>"
        assertEquals('value replaced', 'replace <null>', str)
    }

    /**
     * Tests replacing a value with an empty statement.
     */
    void testReplaceValueWithEmptyStatement() {
        def str = "replace <${;}>"
        assertEquals('value replaced', 'replace <null>', str)
    }

    /**
     * Tests replacing a value with an empty expression.
     */
    void testReplaceValueWithEmptyExpression() {
        assertEquals('replace <null>', "replace <${}>")
    }

    /**
     * Tests GString concatenation. GROOVY-2848
     */
    void testGStringConcatenationAddsNoNewValues() {
        def x = "dog"
        def y = "woof-woof"
        def gs1 = "the ${x} says "
        assert gs1.getValues() == ["dog"]
        assert gs1.toString() == "the dog says "

        gs1 = gs1.plus(" ${y} ")
        assert gs1.getValues() == ["dog", "woof-woof"]
        assert gs1.toString() == "the dog says  woof-woof "

        gs1 = gs1.plus(" not the cat")
        assert gs1.getValues() == ["dog", "woof-woof"]
        assert gs1.toString() == "the dog says  woof-woof  not the cat"
    }

    /**
     * Tests GString splitting. GROOVY-3359
     */
    void testGStringSplitting() {
        def gs = "The quick brown ${'xof'.reverse()}"
        assert gs.split() == ['The', 'quick', 'brown', 'fox'] as String[]
        assert gs.split('o') == ['The quick br', 'wn f', 'x'] as String[]
    }

    def foo(String s) {1}
    void testGStringArgumentForStringParameter() {
        def a = 1
        def b = "$a"
        assert foo(b) == 1 
    }

    /**
     * GROOVY-5761 - getBytes for GString
     */
    public void testGetBytes() {
        String string = 'Hello world'
        String world = 'world'
        GString gstring = "Hello ${world}"

        assert gstring.bytes == string.bytes
        assert gstring.getBytes('UTF-8') ==  string.getBytes('UTF-8')
    }

    /**
     * GROOVY-7377: Interpolated variable followed by asterisk in slashy-string causes compiler error
     */
    void testSlashyStringWithInterpolatedVariableFollowedByAsterisk() {
        assert Eval.me('''def foo='bar'; /$foo*baz/''') == 'bar*baz'
        assert Eval.me('''def foo='bar'; /${foo}*baz/''') == 'bar*baz'
        assert Eval.me('''def foo='bar'; /$foo\u002abaz/''') == 'bar*baz'
        assert Eval.me('''def foo='bar'; /${foo}\u002abaz/''') == 'bar*baz'
    }

    void testImmutableNestedGString() {
        def gstr = "a${"${123}"}b"
        assert 'a123b' == gstr
        assert gstr.toString() === gstr.toString()

        Field immutableField = GString.getDeclaredFields().find {f -> f.name == 'immutable'}
        immutableField.setAccessible(true)
        assert true == immutableField.get(gstr)

        Field cachedStringLiteralField = GString.getDeclaredFields().find {f -> f.name == 'cachedStringLiteral'}
        cachedStringLiteralField.setAccessible(true)
        assert 'a123b' == cachedStringLiteralField.get(gstr)
    }

    void testImmutableGString() {
        def gstr = "a${'1'}"
        assert gstr.toString() === gstr.toString()

        def gstr2 = "a${true}"
        assert gstr2.toString() === gstr2.toString()

        def gstr3 = "a${(byte) 1}"
        assert gstr3.toString() === gstr3.toString()

        def gstr4 = "a${(char) 65}"
        assert gstr4.toString() === gstr4.toString()

        def gstr5 = "a${1D}"
        assert gstr5.toString() === gstr5.toString()

        def gstr6 = "a${1F}"
        assert gstr6.toString() === gstr6.toString()

        def gstr7 = "a${1}"
        assert gstr7.toString() === gstr7.toString()

        def gstr8 = "a${1L}"
        assert gstr8.toString() === gstr8.toString()

        def gstr9 = "a${(short) 1}"
        assert gstr9.toString() === gstr9.toString()

        def gstr10 = "a${Map.class}"
        assert gstr10.toString() === gstr10.toString()
    }

    void testImmutableStringsAndValues() {
        def x = 42G
        def y = "Answer is $x"
        def literal = y.toString()

        assert 'Answer is 42' == y
        y.values[0] = 'the question'
        assert 'Answer is 42' == y

        y.strings[0] = '6 x 7 = '
        assert 'Answer is 42' == y

        assert literal === y.toString()
    }
}
