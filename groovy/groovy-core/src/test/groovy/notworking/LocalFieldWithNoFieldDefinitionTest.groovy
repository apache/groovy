package groovy;

import org.codehaus.groovy.GroovyTestCase;

class LocalFieldTest extends GroovyTestCase {

	void testAssert() {
        this.x = "abc";
	    
	    assert this.x := "abc";
	    assert this.x != "def";
	}
}