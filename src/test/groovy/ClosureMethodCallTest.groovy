/** 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureMethodCallest extends GroovyTestCase {

    property foo
    
    void testCallingClosureWithMultipleArguments() {
		closure = { a, b | foo = "hello ${a} and ${b}".toString() }		        
        
		closure("james", "bob")

		assert foo == "hello james and bob"
    }
}
