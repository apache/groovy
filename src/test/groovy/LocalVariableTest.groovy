import org.codehaus.groovy.runtime.InvokerException

class LocalVariableTest extends GroovyTestCase {

    void testAssert() {
        x = "abc"

        assert x != "foo"
        assert x !=  null
        assert x != "def"
        assert x == "abc"
        
        assert x.equals("abc")
	}
    
    void testUnknownVariable() {
        try {
	        y = x
	        fail("x is undefined, should throw an exception")
        }
        catch (InvokerException e) {
            text = e.message
            assert text == "Unknown property: x"
        }
    }
	    
}
