package groovy

class LocalPropertyTest extends GroovyTestCase {

    def x
    
	void testNormalPropertyAccess() {
	    x = "abc"
	    
	    assert x == "abc"
        assert x != "def"
	}
	
	void testPropertyWithThis() {
        this.x = "abc"
	    
	    assert this.x == "abc"
	    assert this.x != "def"
	}
}
