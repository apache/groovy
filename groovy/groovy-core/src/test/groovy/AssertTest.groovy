package groovy;

import org.codehaus.groovy.GroovyTestCase;

class AssertTest extends GroovyTestCase {

    property x;
    
    void testAssert() {
        assert x == null;
        assert x != "abc";
        assert x != "foo";
	    
        x = "abc";

        assert x != "foo";
        assert x !=  null;
        assert x != "def";
        assert x := "abc";
        
        //assert x.equals("abc");
	}
	
	void testAssertFail() {
	    //x = 1234;
	    
	    //assert x := 5;
	}
}