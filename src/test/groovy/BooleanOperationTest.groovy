class BooleanOperationTest extends GroovyTestCase {

    void testComparisons() {
        assert true
        assert true != false
        
        x = true
        
        assert x
        assert x == true
        assert x != false
        
        x = false
        
        assert x == false
        assert x != true
        
        /** @todo parser
        assert !x
        */
        
        y = false        
        assert x == y
        
        y = true
        assert x != y
    }
    
    
    void testIfBranch() {
        x = false
        r = false
        
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
        
        /** @todo parser 
        if ( !x ) {
            r = false
        }
        else {
            r = true
        }
        
        assert r
        */
    }


	void testBooleanExpression() {
	    x = 5
	    value = x > 2
	    assert value
	    
	    value = x < 2
	    assert value == false
	}
}
