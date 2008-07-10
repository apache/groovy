/*
 * Copyright 2003-2007 the original author or authors.
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
 * Various tests for Strings.
 *
 * @author Michael Baehr
 */
class StringTest extends GroovyTestCase {

    void testString() {
        def s = "abcd"
        assert s.length() == 4
        assert 4 == s.length()
        
        // test polymorphic size() method like collections
        assert s.size() == 4
        
        s = s + "efg" + "hijk"
        
        assert s.size() == 11
        assert "abcdef".size() == 6
    }

    void testStringPlusNull() {
        def y = null
        def x = "hello " + y
        assert x == "hello null"
    }
    
    void testNextPrevious() {
    	def x = 'a'
    	def y = x.next()
    	assert y == 'b'
    
    	def z = 'z'.previous()
    	assert z == 'y'
    	
    	z = 'z'
    	def b = z.next()
    	assert b != 'z'

        assert b > z
        assert z.charAt(0) == 'z'
        assert b.charAt(0) == '{'
    }
    
    void testApppendToString() {
        def name = "Gromit"
        def result = "hello " << name << "!"
        
        assert result.toString() == "hello Gromit!"
    }
    
    void testApppendToStringBuffer() {
        def buffer = new StringBuffer()
        
        def name = "Gromit"
        buffer << "hello " << name << "!" 
        
        assert buffer.toString() == "hello Gromit!"
    }

    void testApppendAndSubscipt() {
        def result =  'hello' << " Gromit!"
        result[1..4] = 'i'
        assert result.toString() == "hi Gromit!"
    }

    void testSimpleStringLiterals() {
        assertLength("\n", 1)
        assertLength("\"", 1)
        assertLength("\'", 1)
        assertLength("\\", 1)
        assertContains("\${0}", 4, "{0}")
        assertContains("x\
y", 2, "xy")

        assertLength('\n', 1)
        assertLength('\'', 1)
        assertLength('\\', 1)
        assertContains('${0}', 4, '{0}')
        assertContains('x\
y', 2, 'xy')
    }

    void testMinusRemovesFirstOccurenceOfString() {
        assert "abcdeabcd" - 'bc' == 'adeabcd'
    }

    void testMinusEscapesRegexChars() {
        assert "abcdeab.d.f" - '.d.' == 'abcdeabf'
    }

    void testMultilineStringLiterals() {
        assertContains(""""x""", 2, '"x');
        assertContains("""""x""", 3, '""x');
        assertContains("""x
y""", 3, 'x\ny');
        assertContains("""\n
\n""", 3, '\n\n\n');

        assertContains(''''x''', 2, "'x");
        assertContains('''''x''', 3, "''x");
        assertContains('''x
y''', 3, 'x\ny');
        assertContains('''\n
\n''', 3, '\n\n\n');

    }

    void testRegexpStringLiterals() {
        assert "foo" == /foo/
        assert '\\$$' == /\$$/
        // Backslash before newline or slash disappears (all others are preserved):
        assert "/\\*" == /\/\*/
        assert "\n" == /\
/
    }

    void testBoolCoerce() {
        // Explicit coercion
        assertFalse((Boolean) "")
        assertTrue((Boolean) "content")

        // Implicit coercion in statements
        String s = null
        if (s) {
            fail("null should have evaluated to false, but didn't")
        }
        s = ''
        if (s) {
            fail("'' should have evaluated to false, but didn't")
        }
        s = 'something'
        if (!s) {
            fail("'something' should have evaluated to false, but didn't")
        }
        
    }

    void testSplit() {
        def text = "hello there\nhow are you"
        def splitted = text.split().toList()
        assert splitted == ['hello', 'there', 'how', 'are', 'you']
    }

    void testSplitEmptyText() {
        def text = ""
        def splitted = text.split().toList()
        assert splitted == []
    }

    void testSplitEqualsTokenize() {
        def text = """
        A text with different words and
        numbers like 3453 and 3,345,454.97 and
        special characters %&)( and also
        everything mixed together 45!kw?
      """

        def splitted = text.split().toList()
        def tokenized = text.tokenize()
        assert splitted == tokenized
    }

    private assertLength(s, len) {
        if (s.length() != len)  println "*** length != $len: $s"
        assert s.length() == len
    }

    private assertContains(s, len, subs) {
        assertLength(s, len)
        if (s.indexOf(subs) < 0)  println "*** missing $subs: $s"
        assert s.indexOf(subs) >= 0
    }

}
