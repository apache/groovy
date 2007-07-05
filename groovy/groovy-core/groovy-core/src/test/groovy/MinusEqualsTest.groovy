package groovy

class MinusEqualsTest extends GroovyTestCase {

    void testIntegerMinusEquals() {
        def x = 4
        def y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }

    void testCharacterMinusEquals() {
        Character x = 4
        Character y = 2
        x -= y
        
        assert x == 2
        
        y -= 1
        
        assert y == 1
    }
    
    void testNumberMinusEquals() {
        def x = 4.2
        def y = 2
        x -= y
        
        assert x == 2.2
        
        y -= 0.1
        
        assert y == 1.9
    }
    
    void testStringMinusEquals() {
        def foo = "nice cheese"
        foo -= "cheese"
        
        assert foo == "nice "
    }
}
