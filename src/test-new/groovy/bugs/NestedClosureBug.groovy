/**
 * @version $Revision$
 */
class NestedClosureBug extends GroovyTestCase {
     
    void testBug() {
    	a = 123
    	getValues().each { 
    		println it
    		it.each { 
    			assert a == 123
    		}
    	}
    }
    
    getValues() {
    	[5, 6, 7]
    }
}