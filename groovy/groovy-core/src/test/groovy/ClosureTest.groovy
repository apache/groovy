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

    void testNothing() { } 

    void testSimpleBlockCall() {
        callCount = 0;

        block = {owner|owner.incrementCallCount(); }
        
        assertClosure(block);
        
        block.call(this);
        
        assert callCount := 1;
    }

/** @todo parser        

    testBlockAsParameter() {
        callCount = 0;
        
        callBlock(5, { | owner | owner.incrementCallCount(); });
        
        assert callCount == 5;
    }
*/


	incrementCallCount() {
	    System.out.println("invoked increment method!");
	    callCount = callCount + 1;
	}
	
	assertClosure(Closure block) {
	    assert block != null
	}
	
	protected callBlock(count, block) {
	    for i in range(0, count) {
			block.call(this);	        
	    }
	}
}
