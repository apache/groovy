class StringTest extends GroovyTestCase {

    void testString() {
        s = "abcd"
        assert s.length() == 4
        assert 4 == s.length()
        
        // test polymorphic size() method like collections
        assert s.size() == 4
        
        s = s + "efg" + "hijk"
        
        assert s.size() == 11
        assert "abcdef".size() == 6
    }

    void testStringPlusNull() {
        y = null
        
        x = "hello " + y
        
        assert x == "hello null"
    }
    
    void testNextPrevious() {
    	x = 'a'
    	y = x.next() 
    	assert y == 'b'
    
    	z = 'z'.previous()
    	assert z == 'y'
    	
    	z = 'z'
    	b = z.next()
    	assert b != 'z'
    	
    	println(z.charAt(0))
    	println(b.charAt(0))
    	
    	assert b > z
    	
    	println "Incremented z: " + b
    }
    
    void testApppendToString() {
        name = "Gromit"
        result = "hello " << name << "!" 
        
        assert result.toString() == "hello Gromit!"
    }
    
    void testApppendToStringBuffer() {
        buffer = new StringBuffer()
        
        name = "Gromit"
        buffer << "hello " << name << "!" 
        
        assert buffer.toString() == "hello Gromit!"
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
}
