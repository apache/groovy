package groovy

/** 
 * Tests exception handling inside of a closure
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ExceptionInClosureTest extends GroovyTestCase {

    void testCallingOfFailedClosure() {
        def closure = { it -> it.foo() }
        
        try {
	        closure.call("cheese")
	        
	        fail("Should have thrown an exception by now")
        }
        catch (MissingMethodException e) {
   			System.out.println("Caught: " + e)    
   			
   			assert e.method == "foo"
			assert e.type == String   			
        }
    }
}
