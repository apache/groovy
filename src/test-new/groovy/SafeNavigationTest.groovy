class SafeNavigationTest extends GroovyTestCase {

    void testNormalPropertyNavigation() {
        x = ['foo':['bar':123, 'x':456], 'z':99]
        
        y = x->foo->bar
        
        println("found y ${x->foo->bar}")
        
        assert y == 123
    }

    void testNullPropertyNavigation() {
        x = null
        
		y = x->foo->bar
        
        assert y == null


       java.awt.Color color = null
        a = color->alpha
        assert a == null

    }
    
    void testNormalMethodCall() {
        x = 1234
        
        y = x->toString()
        
        assert y == "1234"
    }

    void testNullMethodCall() {
        x = null
        
        y = x->toString()
        
        assert y == null
    }
}
