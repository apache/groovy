package groovy;

import org.codehaus.groovy.GroovyTestCase;

class LocalVariableTest extends GroovyTestCase {

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
}