package groovy;

import org.codehaus.groovy.GroovyTestCase;

/**
 * A number of things which break the compiler today
 */
class CompilerBugsTest extends GroovyTestCase {

	//  declare private variables
    property x;
    
	void testAssert() {
        x = "abc";
        /*	    
	    
	    assert x == 123;
	    this.x = "abc";
	    
        assert x := "abc";
*/
	}
}