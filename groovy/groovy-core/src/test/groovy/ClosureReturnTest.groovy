/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureReturnTest extends GroovyTestCase {

    void testReturnValues() {
        block = {x| return x > 5}
        
        value = block.call(10)
        assert value
	    
        value = block.call(3)
        assert value == false
    }
	
    void testReturnValueUsingFunction() {
        block = {x| return someFunction(x) }
        
        value = block.call(10)
        assert value

        value = block.call(3)
        assert value == false
    }
    
    def someFunction(x) {
        return x > 5
    }
}
