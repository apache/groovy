package groovy.operator

class BooleanOperationsTest extends GroovyTestCase {

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
	
	
	void testBooleanOps() {
	    boolean x = true
	    boolean y = false
	    assert (x & x) == true
	    assert (x & y) == false
	    assert (y & x) == false
	    assert (y & y) == false

	    assert (x | x) == true
	    assert (x | y) == true
	    assert (y | x) == true
	    assert (y | y) == false

	    assert (x ^ x) == false
	    assert (x ^ y) == true
	    assert (y ^ x) == true
	    assert (y ^ y) == false

	    assert (!x) == false
	    assert (!y) == true
	}


	void testBooleanAssignOps() {
	    boolean z = true
	    z &= true
	    assert z == true
	    z &= false
	    assert z == false

	    z = true
	    z |= true
	    assert z == true
	    z |= false
	    assert z == true
	    z = false
	    z |= false
	    assert z == false
	    z |= true
	    assert z == true

        z = true
        z ^= true
        assert z == false
        z ^= true
        assert z == true
        z ^= false
        assert z == true
        z ^= true
        assert z == false
        z ^= false
        assert z == false
	}

}
