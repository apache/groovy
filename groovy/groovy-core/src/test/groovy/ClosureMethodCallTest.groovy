/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodCallTest extends GroovyTestCase {

    property foo
    
    void testCallingClosureWithMultipleArguments() {
		closure = { a, b | foo = "hello ${a} and ${b}".toString() }		        
        
		closure("james", "bob")

		assert foo == "hello james and bob"

		closure.call("sam", "james")

		assert foo == "hello sam and james"
	}
}
