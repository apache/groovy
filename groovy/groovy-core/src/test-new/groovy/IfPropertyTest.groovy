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
        if (cheese == null) {
            cheese = 1
        }
        if (cheese != 1) {
            fail("Didn't change cheese")
        }
        assert cheese == 1
    }
    
    void testIfNullPropertySetRecheck() {
        if (cheese == null) {
            cheese = 1
        }
        if (cheese == 1) {
            cheese = 2
        }
        assert cheese == 2
    }
    
}
