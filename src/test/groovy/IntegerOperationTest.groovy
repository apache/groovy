class IntegerOperationTest extends GroovyTestCase {

    property x
    property y
    
    void testPlus() {
        x = 2 + 2
        assert x == 4
        
        y = x + 1
		assert y == 5
		
	z = y + x + 1 + 2
	assert z == 12 
    }
    
    void testMinus() {
        x = 6 - 2
        assert x == 4
        
        y = x - 1
	assert y == 3        
    }
    
    void testMultiply() {
        x = 3 * 2
        assert x == 6
        
        y = x * 2
        assert y == 12        
    }
    
    void testDivide() {
        x = 80 / 4
        assert x == 20.0 : "x = " + x
        
        y = x / 2
        assert y == 10.0 : "y = " + y        
    }
}
