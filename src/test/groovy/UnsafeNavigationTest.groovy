class UnsafeNavigationTest extends GroovyTestCase {

    void testUnsafePropertyNavigations() {
        x = null
        
        try {
	        y = x.foo
	        fail("should fail")
        }
        catch (NullPointerException e) {
            assert e != null
        }
    }
}
