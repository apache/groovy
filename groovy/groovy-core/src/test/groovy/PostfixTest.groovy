class PostfixTest extends GroovyTestCase {

    void testIntegerPostfix() {
        x = 1
        
        y = x++
        
        assert y == 1
        assert x == 2
        
        assert x++ == 2
        assert x == 3
    }
    
    /*
    void testDoublePostfix() {
        x = 1.2
        x++
        
        assert x == 2.2
    }

    void testStringPostfix() {
        x = "bbc"
        x++
        
        assert x == "bbd"
    }
    */
}
