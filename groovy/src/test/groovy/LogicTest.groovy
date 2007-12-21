package groovy

class LogicTest extends GroovyTestCase {

    void testAndWithTrue() {

        def x = false
        def n = 2
        
        if ( n > 1 && n < 10 ) {
            x = true
        }

        assert x == true
    }

    void testAndWithFalse() {

        def x = false
        def n = 20
        
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

        def x = false
        def n = 2
        
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

        def x = false
        def n = 11
        
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
