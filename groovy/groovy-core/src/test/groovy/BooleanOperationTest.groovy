class BooleanOperationTest extends GroovyTestCase {

    void testComparisons() {
        assert true
        assert true != false
        
        def x = true
        
        assert x
        assert x == true
        assert x != false
        
        x = false
        
        assert x == false
        assert x != true
        
        assert !x
        
        def y = false        
        assert x == y
        
        y = true
        assert x != y
    }
    
    
    void testIfBranch() {
        def x = false
        def r = false
        
        if ( x ) {
            // ignore
        }
        else {
            r = true
        }

        assert r
        
        x = true
        r = false
        
        if ( x ) {
            r = true
        }
        else {
            // ignore
        }
        assert r
        
        if ( !x ) {
            r = false
        }
        else {
            r = true
        }
        
        assert r
    }


	void testBooleanExpression() {
	    def x = 5
	    def value = x > 2
	    assert value
	    
	    value = x < 2
	    assert value == false
	}
}
