class MultiplyDivideEqualsTest extends GroovyTestCase {

    void testIntegerMultiplyEquals() {
        x = 2
        y = 3
        x *= y
        
        assert x == 6
        
        y *= 4
        
        assert y == 12
    }
    
    void testNumberMultiplyEquals() {
        x = 1.2
        y = 2
        x *= y
        
        assert x == 2.4
    }
    
    void testStringMultiplyEquals() {
        x = "bbc"
        y = 2
        x *= y
        
        assert x == "bbcbbc"
    }
    
    
    void testIntegerDivideEquals() {
        x = 18
        y = 6
        x /= y
        
        assert x == 3.0
        
        y /= 3
        
        assert y == 2.0
    }
    
    void testNumberDivideEquals() {
        x = 10.4
        y = 2
        x /= y
        
        assert x == 5.2
    }
}
