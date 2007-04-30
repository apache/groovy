package groovy

class PostfixTest extends GroovyTestCase {

    void testIntegerPostfix() {
        def x = 1
        
        def y = x++
        
        assert y == 1
        assert x == 2
        
        assert x++ == 2
        assert x == 3
    }
    
    void testDoublePostfix() {
        def x = 1.2
        def y = x++

        assert y == 1.2
        assert x++ == 2.2
        assert x == 3.2
    }

     void testStringPostfix() {
        def x = "bbc"
        x++
        
        assert x == "bbd"
    }
}
