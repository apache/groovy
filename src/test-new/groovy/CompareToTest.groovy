class CompareToTest extends GroovyTestCase {

    void testCompareTo() {

        a = 12
        b = 20
        c = 30
        
        result = a <=> b
        assert result < 0

        result = a <=> 12
        assert result == 0

        result = c <=> b
        assert result > 0
        
        assert (a <=> b) < 0
        assert a <=> 12 == 0
        assert (c <=> b) > 0
    }

    void testNullCompares() {
    
    	a = 123
    	b = null
    	
    	result = a <=> b
    	assert result > 0
    	
    	result = b <=> a
    	assert result < 0
    	
    	result = b <=> null
    	assert result == 0
    }
}
