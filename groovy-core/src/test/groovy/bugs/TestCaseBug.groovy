package groovy.bugs

import junit.framework.TestCase

/**
 * @version $Revision$
 */
class TestCaseBug extends TestCase {

    // using def here is wrong
    TestCaseBug(String name) {
    		super(name)
    	}
    	
    	void testDummy() {
    		println "worked!"
    	}

    static void main(args) {
        def foo = new TestCaseBug("hey")
        foo.testDummy()
    }
    
}