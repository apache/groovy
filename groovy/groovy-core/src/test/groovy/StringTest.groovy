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
    
    void testIncrementDecrement() {
    	x = 'a'
    	y = x.increment()
    	assert y == 'b'
    
    	z = 'z'.decrement()
    	assert z == 'y'
    	
    	z = 'z'
    	b = z.increment()
    	assert b != 'z'
    	
    	println(z.charAt(0))
    	println(b.charAt(0))
    	
    	assert b > z
    	
    	println "Incremented z: " + b
    	
    	
	}
}
