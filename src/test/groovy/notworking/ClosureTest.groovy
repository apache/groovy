package groovy;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureTest extends Test {

	property callCount;

    testSimpleBlockCall() {
        callCount = 0;
        
        block = {owner| owner.incrementCallCount(); }
        
        block.call();
        
        assert(callCount == 1);
    }


    testBlockAsParameter() {
        callCount = 0;
        
        callBlock(5, { | owner | owner.incrementCallCount(); });
        
        assert(callCount == 5);
    }


	protected incrementCallCount() {
	    callCount = callCount + 1;
	}
	
	protected callBlock(count, block) {
	    for i in range(0, count) {
			block.call();	        
	    }
	}
}