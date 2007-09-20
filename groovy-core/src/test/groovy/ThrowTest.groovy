package groovy

class ThrowTest extends GroovyTestCase {
    
    void testThrow() {
        
        try {
	        throw new Exception("abcd")
	        
	        fail("Should have thrown an exception by now")
        }
        catch (Exception e) {
            assert e.message == "abcd"
            
            println("Caught exception ${e}")
        }
    }
}
