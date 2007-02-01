package groovy

class AssertNumberTest extends GroovyTestCase {

    void testCompare() {
        def x = null

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
        def x = 123

        assert x < 200
        assert x <= 200
        assert x <= 123
    }

    void testGreaterThan() {
        def x = 123
	    
        assert x > 10
        assert x >= 10
        assert x >= 123
    }
}
