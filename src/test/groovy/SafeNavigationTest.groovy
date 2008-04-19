package groovy

class SafeNavigationTest extends GroovySwingTestCase {

    void testNullNavigation() {
        def x = null
        def y = x?.bar

        assert y == null
    }

    void testNormalPropertyNavigation() {
        def x = ['a':456, 'foo':['bar':123, 'x':456], 'z':99]
        
        def y = x?.foo?.bar
        
        println("found y ${x?.foo?.bar}")
        
        assert y == 123
    }

    void testNullPropertyNavigation() {
        if (headless) return

        def x = null
        
        def y = x?.foo?.bar
        
        assert y == null


        def java.awt.Color color = null
        def a = color?.alpha
        assert a == null

    }
    
    void testNormalMethodCall() {
        def x = 1234
        
        def y = x?.toString()
        
        assert y == "1234"
    }

    void testNullMethodCall() {
        def x = null
        
        def y = x?.toString()
        
        assert y == null
    }

}
