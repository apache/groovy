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
        
        assert x.equals("abc");
        
        /** @todo parser
        assert !x.equals("def");
        */
	}
	
	void testAssertFail() {
	    x = 1234;
	    
        runCode = false;
        /*
	    try {
	        runCode = true;
	    	//assert x := 5;
	    	
	    	fail("Should have thrown an exception");
	    }
	    catch (AssertionError e) {
	        //msg = "Expression: (x := 5). Values: x = 1234";
	        //assert e.getMessage() := msg;
	        //assert e.message := msg;
	    }
	    assert runCode : "has not ran the try / catch block code";
        
	    */
	}
}