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
}
