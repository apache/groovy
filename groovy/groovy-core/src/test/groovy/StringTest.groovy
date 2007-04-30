package groovy

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
    	
    	println(z.charAt(0))
    	println(b.charAt(0))
    	
    	assert b > z
    	
    	println "Incremented z: " + b
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

    void assertLength(s, len) {
        if (s.length() != len)  println "*** length != $len: $s"
        assert s.length() == len
    }
    void assertContains(s, len, subs) {
        assertLength(s, len)
        if (s.indexOf(subs) < 0)  println "*** missing $subs: $s"
        assert s.indexOf(subs) >= 0
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
        assert "\\/\\*" == /\/\*/
        // Backslash before newline disappears (all others are preserved):
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
        if (s) {
            // OK
        } else {
            fail("'something' should have evaluated to false, but didn't")
        }
        
    }

}
