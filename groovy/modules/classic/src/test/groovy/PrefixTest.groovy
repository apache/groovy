class PrefixTest extends GroovyTestCase {

    void testIntegerPrefix() {
        x = 1
        
        y = ++x
        
        assert y == 2
        assert x == 2
        
        assert ++x == 3
    }
    
    void testDoublePrefix() {
        x = 1.2
        y = ++x
        
        assert y == 2.2
        assert x == 2.2
        assert ++x == 3.2
        assert x == 3.2
    }

    void testStringPrefix() {
        x = "bbc"
        x++
        
        assert x == "bbd"
        
        x--
        x--
        
        assert x == "bbb"
    }
}
