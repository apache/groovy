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

         def y = "bbc"++
         assert y == "bbc"
    }
    
    
    void testArrayPostfix() {
        int[] i = [1]
        
        def y = i[0]++
        
        assert y == 1
        assert i[0]++ == 2
        assert i[0] == 3
    }
    
    void testConstantPostFix() {
        assert 1 == 1++
    }

    def valueReturned() { 0 }

    void testFunctionPostfix() {
        def z = (valueReturned())++

        assert z == 0
    }

    void testPrefixAndPostfix() {
        def u = 0
        
        assert -1 == -- u --
        assert 0 == ++ u ++
        assert 0 == u
        assert 0 == (u++)++
        assert 2 == u
    }
}
