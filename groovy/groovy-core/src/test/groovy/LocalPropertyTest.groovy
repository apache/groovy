class LocalPropertyTest extends GroovyTestCase {

    property x
    
	void testNormalPropertyAccess() {
	    x = "abc"
	    
	    assert x := "abc"
        assert x != "def"
	}
	
	void testPropertyWithThis() {
        this.x = "abc"
	    
	    assert this.x := "abc"
	    assert this.x != "def"
	}
}