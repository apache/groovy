package groovy;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureWithParamTest extends Test {

    testSimpleBlockCall() {
        this.callCount = 0;
        
        block = { |step| this.callCount += step; }
        
        block.call(2);
        
        assert(this.callCount == 2);
    }


    testBlockAsParameter() {
        this.callCount = 0;
        
        callBlock(5, 3, { |step| this.callCount += step; });
        
        assert(this.callCount == 15);
    }


	protected callBlock(count, step, block) {
	    for i in range(0, count) {
			block.call(step);	        
	    }
	}
}