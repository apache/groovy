package groovy;

import org.codehaus.groovy.GroovyTestCase;

class AssertTest extends GroovyTestCase {

    property x;
    
    void testAssert() {
	    assert x != "foo";
	}
}