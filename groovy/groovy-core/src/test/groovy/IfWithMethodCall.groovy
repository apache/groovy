package groovy

import org.codehaus.groovy.GroovyTestCase

class IfWithMethodCallTest extends GroovyTestCase {

	void testIfWithMethodCall() {
	    x = ["foo", "cheese"]
	    
        if x.contains("cheese") {
            // ignore
        }
        else {
            assert false : "x should contain cheese!"
        }
	}
}
