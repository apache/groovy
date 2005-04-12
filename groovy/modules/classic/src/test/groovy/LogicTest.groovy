class LogicTest extends GroovyTestCase {

    void testAndWithTrue() {

        x = false
        n = 2
        
        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == true
    }

    void testAndWithFalse() {

        x = false
        n = 20
        
        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == false

        n = 0
        
        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == false
	}

    void testOrWithTrue() {

        x = false
        n = 2
        
        if ( n > 1 || n < 10 ) {
            x = true
        }

        assert x == true

        x = false
        n = 0
        
        if ( n > 1 || n == 0 ) {
            x = true
        }

        assert x == true
    }

    void testOrWithFalse() {

        x = false
        n = 11
        
        if ( n < 10 || n > 20 ) {
            x = true
        }

        assert x == false

        n = 11
        
        if ( n < 10 || n > 20 ) {
            x = true
        }
    
        assert x == false
    }
}
