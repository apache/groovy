package groovy

class MultiplyDivideEqualsTest extends GroovyTestCase {

    void testIntegerMultiplyEquals() {
        def x = 2
        def y = 3
        x *= y
        
        assert x == 6
        
        y *= 4
        
        assert y == 12
    }

    void testCharacterMultiplyEquals() {
        Character x = 2
        Character y = 3
        x *= y
        
        assert x == 6
        
        y *= 4
        
        assert y == 12
    }
    
    void testNumberMultiplyEquals() {
        def x = 1.2
        def y = 2
        x *= y
        
        assert x == 2.4
    }
    
    void testStringMultiplyEquals() {
        def x = "bbc"
        def y = 2
        x *= y
        
        assert x == "bbcbbc"

        x = "Guillaume"
        y = 0
        x *= y
        assert x == ""
    }
    
    
    void testIntegerDivideEquals() {
        def x = 18
        def y = 6
        x /= y
        
        assert x == 3.0
        
        y /= 3
        
        assert y == 2.0
    }
    
    void testCharacterDivideEquals() {
        Character x = 18
        Character y = 6
        x /= y
        
        assert x == 3
        
        y /= 3
        
        assert y == 2
    }
    
    void testNumberDivideEquals() {
        def x = 10.4
        def y = 2
        x /= y
        
        assert x == 5.2
    }
}
