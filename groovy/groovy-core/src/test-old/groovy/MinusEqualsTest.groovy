class MinusEqualsTest extends GroovyTestCase {

    void testIntegerMinusEquals() {
        x = 4
        y = 2
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
        x = 4.2
        y = 2
        x -= y
        
        assert x == 2.2
        
        y -= 0.1
        
        assert y == 1.9
    }
    
    void testStringMinusEquals() {
        foo = "nice cheese"
        foo -= "cheese"
        
        assert foo == "nice "
    }
}
