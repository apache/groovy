package groovy;

import org.codehaus.groovy.GroovyTestCase;

class LocalPropertyTest extends GroovyTestCase {

    property x;
    
	void testAssert() {
        this.x = "abc";
	    
	    assert this.x := "abc";
	    
	    /** @todo
	    assert this.x != "def";
	    */
	}
}