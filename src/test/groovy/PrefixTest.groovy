package groovy

class PrefixTest extends GroovyTestCase {

    void testIntegerPrefix() {
        def x = 1
        
        def y = ++x
        
        assert y == 2
        assert x == 2
        
        assert ++x == 3
    }
    
    void testDoublePrefix() {
        def x = 1.2
        def y = ++x
        
        assert y == 2.2
        assert x == 2.2
        assert ++x == 3.2
        assert x == 3.2
    }

    void testStringPrefix() {
        def x = "bbc"
        ++x
        
        assert x == "bbd"
        
        --x
        --x
        
        assert x == "bbb"

        def y = ++"bbc"
        assert y == "bbd"
    }
    
    void testArrayPrefix() {
        int[] i = [1]
        
        ++i[0]
        assert i[0] == 2
        
        --i[0]
        --i[0]
        assert i[0] == 0
    }
    
    void testConstantPostFix() {
        assert 2 == ++1
    }

    def valueReturned() { 0 }

    void testFunctionPostfix() {
        def z = ++(valueReturned())

        assert z == 1
    }

    void testPrefixAndPostfix() {
        def u = 0
        
        assert -1 == -- u --
        assert 0 == ++ u ++
        assert -2 == (--(--u))
        assert -1 == u
    }
}
