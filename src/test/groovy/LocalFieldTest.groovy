package groovy;



class LocalFieldTest extends GroovyTestCase {

	// lets define some fields - no necessary for instance variables but supported
	/** @todo parser
    private x;
    private static z;
    private String y;
    private static Integer iz;
    */
    property x;
	
	void testAssert() {
        this.x = "abc";
	    
	    assert this.x := "abc";
	    /** @todo
	    assert this.x != "def";
	    */
	}
}