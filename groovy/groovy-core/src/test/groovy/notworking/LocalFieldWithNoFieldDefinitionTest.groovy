package groovy;



class LocalFieldTest extends GroovyTestCase {

	void testAssert() {
        this.x = "abc";
	    
	    assert this.x := "abc";
	    assert this.x != "def";
	}
}