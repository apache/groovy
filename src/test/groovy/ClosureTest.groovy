package groovy;

import org.codehaus.groovy.GroovyTestCase;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureTest extends GroovyTestCase {

	property callCount;

    void testSimpleBlockCall() {
        callCount = 0;

        block = {owner|owner.incrementCallCount(); }
        
        // block.call(this);
        
        // assert(callCount == 1);
    }

/** @todo parser        

    testBlockAsParameter() {
        callCount = 0;
        
        callBlock(5, { | owner | owner.incrementCallCount(); });
        
        assert(callCount == 5);
    }
*/


	incrementCallCount() {
	    callCount = callCount + 1;
	}
	
	protected callBlock(count, block) {
	    for i in range(0, count) {
			block.call(this);	        
	    }
	}
}
