class AssertNumberTest extends GroovyTestCase {

    property x
    
    void testCompare() {
        assert x == null
   		assert x != 432
   		assert x != 423.2342
   		     
        x = 123

        assert x != null
        assert x != 432
        assert x != 423.2342
        assert x == 123
		
        x = 42.2342

        assert x != null
   		assert x != 432
   		assert x != 423.2342
		assert x == 42.2342
	}
	
	void testLessThan() {
	    x = 123
	    
        assert x < 200
        assert x <= 200
        assert x <= 123
	    
	}

    void testGreaterThan() {
        x = 123
	    
        assert x > 10
        assert x >= 10
        assert x >= 123
    }
}
