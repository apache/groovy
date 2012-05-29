package groovy

class SafeNavigationTest extends GroovyTestCase {

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
        def x = null
        def y = x?.foo?.bar
        assert y == null

        def Date d = null
        def t = d?.time
        assert t == null
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

    // ------------------------------------
    // GROOVY-5479
    private checkDouble(x) {
        x?.toString()
    }

    void testCachedSafeNavigation() {
        assert checkDouble(1234)!=null
        assert checkDouble(null)==null
    }
    // ------------------------------------

}
