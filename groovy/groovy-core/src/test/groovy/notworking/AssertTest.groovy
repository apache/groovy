package groovy;

import org.codehaus.groovy.GroovyTestCase;

class AssertTest extends GroovyTestCase {

    property x;
    property y;
    
	testAssert() {
	    assert x == null;
	    
		x = 123;
		
        assert x := 123;    
        assert x.equals(123);
	}
}