class PlusEqualsTest extends GroovyTestCase {

    void testIntegerPlusEquals() {
        x = 1
        y = 2
        x += y
        
        assert x == 3
        
        y += 10
        
        assert y == 12
    }

    void testCharacterPlusEquals() {
        Character x = 1
        Character y = 2
        x += y
        
        assert x == 3
        
        y += 10
        
        assert y == 12
    }
    
    void testNumberPlusEquals() {
        x = 1.2
        y = 2
        x += y
        
        assert x == 3.2
        
        y += 10.1
        
        assert y == 12.1
    }
    
    void testStringPlusEquals() {
        x = "bbc"
        y = 2
        x += y
        
        assert x == "bbc2"
        
        foo = "nice cheese"
        foo += " gromit"
        
        assert foo == "nice cheese gromit"
    }
}
