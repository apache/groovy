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

    void testReadLines() {
        assert "a\nb".readLines() == ['a', 'b']
        assert "a\rb".readLines() == ['a', 'b']
        assert "a\r\nb".readLines() == ['a', 'b']
        assert "a\n\nb".readLines() == ['a', '', 'b']
    }
    
    void testReplace() {
        assert "".replace("", "") == ""
        assert "a".replace("b", "c") == "a"
        assert "a".replace("a", "c") == "c"
        assert "aa".replace("a", "c") == "cc"
        assert "ab".replace("b", "c") == "ac"
        assert "ba".replace("b", "c") == "ca"
        assert "aaa".replace("b", "c") == "aaa"
        assert "aaa".replace("a", "c") == "ccc"
        assert "aba".replace("b", "c") == "aca"
        assert "baa".replace("b", "c") == "caa"
        assert "aab".replace("b", "c") == "aac"
        assert "aa.".replace(".", "c") == "aac"
        assert 'aba'.replace('b', '$') == 'a$a'
        assert 'aba'.replace('b', '\\') == 'a\\a'
        assert 'a\\a'.replace('\\', 'x') == 'axa'
        assert '\\'.replace('\\', 'x') == 'x'
        assert '\\\\'.replace('\\', 'x') == 'xx'
        assert '\\z\\'.replace('\\', 'x') == 'xzx'
        assert 'a\\\\Ea'.replace('\\', 'x') == 'axxEa'
        assert '\\Qa\\\\Ea'.replace('\\', '$') == '$Qa$$Ea'
        assert 'a\\z\\Qa'.replace('\\', 'x') == 'axzxQa'
    }

    void testNormalize() {
        assert "a".normalize() == "a"
        assert "a\n".normalize() == "a\n"
        assert "a\n\n".normalize() == "a\n\n"
        assert "a\nb\rc\r\nd".normalize() == "a\nb\nc\nd"
        assert "a\n\nb".normalize() == "a\n\nb"
    }

    void testDenormalize() {
        def LS = System.getProperty('line.separator')
        assert 'a'.denormalize() == 'a'
        assert 'a\n'.denormalize() == 'a' + LS
        assert 'a\n\n'.denormalize() == 'a' + LS + LS
        assert 'a\n\nb'.denormalize() == 'a' + LS + LS + 'b'
        assert 'a\nb\r\nc\n\rd'.denormalize() == 'a' + LS + 'b' + LS + 'c' + LS + LS + 'd'
    }
    
    void innerNormalizationFileRoundTrip(String s) {
        def f = File.createTempFile("groovy.StringTest", ".txt")
        
        def sd = s.denormalize()
        f.write(sd)
        assert sd == f.text
        
        f.write(s); 
        assert s == f.text
        
        def rt = (s.denormalize()).normalize()
        assert s.normalize() == rt

        if (!s.contains('\r')) assert s == rt
    }

    void doNormalizationFileRoundTrip(String s) {
        [s, s.replace('\n', '\r'), s.replace('\n', '\r\n'), s.replace('\n', '\n\n')].each {
            innerNormalizationFileRoundTrip(it)
            innerNormalizationFileRoundTrip(it.reverse())
        }
    }

    void testNormalizationFileRoundTrip() {
        doNormalizationFileRoundTrip("a line 1\nline 2")
        doNormalizationFileRoundTrip("a line 1\nline 2\n")
        doNormalizationFileRoundTrip("")
        doNormalizationFileRoundTrip("\n")
        doNormalizationFileRoundTrip("a")
        doNormalizationFileRoundTrip("abcdef")
        doNormalizationFileRoundTrip("a\n")
        doNormalizationFileRoundTrip("abcdef\n")
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
