class IfPropertyTest extends GroovyTestCase {
	
	// This is because normal classes are not extensible, but scripts are extensible by default.
	Object get(String key) {
		return null
	}

    void testIfNullPropertySet() {
		if (cheese == null) {
			cheese = 1
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
