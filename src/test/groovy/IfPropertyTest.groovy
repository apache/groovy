package groovy

class IfPropertyTest extends GroovyTestCase {
	
    def dummy
    
	// This is because normal classes are not extensible, but scripts are extensible by default.
    Object get(String key) {
        println("asking for def " + key)
        return dummy
    }

    void set(Object key, Object value) {
        println("setting the def " + key + " to: " + value)
        dummy = value
    }

    void testIfNullPropertySet() {
        def cheese = null
        if (cheese == null) {
            cheese = 1
        }
        if (cheese != 1) {
            fail("Didn't change cheese")
        }
        assert cheese == 1
    }
    
    void testIfNullPropertySetRecheck() {
        def cheese = null
        if (cheese == null) {
            cheese = 1
        }
        if (cheese == 1) {
            cheese = 2
        }
        assert cheese == 2
    }
    
}
