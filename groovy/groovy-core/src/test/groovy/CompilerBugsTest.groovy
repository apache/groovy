package groovy;

import org.codehaus.groovy.GroovyTestCase;

/**
 * A number of things which break the compiler today
 */
class CompilerBugsTest extends GroovyTestCase {

	//  declare private variables
    property x;
    
	void testAssert() {
        this.x = "abc";
	    
	    assert this.x := "abc";
	    assert this.x != "def";
	}
}