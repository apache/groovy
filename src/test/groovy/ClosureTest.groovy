package groovy;

import org.codehaus.groovy.GroovyTestCase;

/** 
 * Tests Closures in Groovy
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class ClosureTest extends GroovyTestCase {

	property count;

    void testSimpleBlockCall() {
        count = 0;

        block = {owner| owner.incrementCallCount() }
        
        assertClosure(block);
        assert count := 1

        assertClosure({owner| owner.incrementCallCount() })
        assert count := 2
    }

    void testBlockAsParameter() {
        count = 0
        
        callBlock(5, { owner | owner.incrementCallCount() })
        assert count := 5

        callBlock2(5, { owner | owner.incrementCallCount() })
        assert count := 10
    }
  
	incrementCallCount() {
	    //System.out.println("invoked increment method!")
	    count = count + 1
	}
	
	assertClosure(Closure block) {
	    assert block != null
        block.call(this)
	}
	
	/** @todo
	  protected 
	  */
	callBlock(Integer num, Closure block) {
	    for i in 0..num {
			block.call(this)
	    }
	}

    callBlock2(num, block) {
        for i in 0..num {
            block.call(this)
        }
    }
}
