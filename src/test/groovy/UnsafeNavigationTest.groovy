package groovy

class UnsafeNavigationTest extends GroovyTestCase {

    void testUnsafePropertyNavigations() {
        def x = null
        
        try {
            def y = x.foo
            fail("should fail")
        }
        catch (NullPointerException e) {
            assert e != null
        }
    }
}
