package groovy;

import org.codehaus.groovy.GroovyTestCase;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureReturnTest extends GroovyTestCase {

	/** @todo
    void testReturnValues() {
        block = {x| return x > 5}
        
        value = block.call(10)
        assert value
	    
        value = block.call(3)
        assert value == false
    }
	*/
	
    void testReturnValueUsingFunction() {
        block = {x| return true }
        
        value = block.call(10)
        assert value

		/** @todo	    
        value = block.call(3)
        assert value == false
        */
    }
    
    someFunction(x) {
        /** @todo
        return x > 5
         */
        
        /** @todo parser
      	if x > 5 {
      	    return true
      	}
      	return false;
		*/
        
        /** @todo parser	        
        answer = false
      	if x > 5 {
      	    answer = true
      	}
      	*/
      	return false
    }
}
