package groovy;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureTest extends Test {

    testSimpleBlockCall() {
        this.callCount = 0;
        
        block = { ++ this.callCount; }
        
        block.call();
        
        assert(this.called == 1);
    }


    testBlockAsParameter() {
        this.callCount = 0;
        
        callBlock(5, { ++ this.callCount; });
        
        assert(this.called == 5);
    }


	protected callBlock(count, block) {
	    for i in range(0, count) {
			block.call();	        
	    }
	}
}