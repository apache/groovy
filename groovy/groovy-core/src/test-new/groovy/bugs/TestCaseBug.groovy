import junit.framework.TestCase

/**
 * @version $Revision$
 */
class TestCaseBug extends TestCase {
    
    def TestCaseBug(String name) {
    		super(name)
    	}
    	
    	void testDummy() {
    		println "worked!"
    	}
}