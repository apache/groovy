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
