package groovy.bugs

/**
 * @version $Revision$
 */
class NestedClosureBug extends GroovyTestCase {
     
    void testBug() {
    	def a = 123
    	getValues().each { 
    		println it
    		it.each { 
    			assert a == 123
    		}
    	}
    }
    
    def getValues() {
    	[5, 6, 7]
    }
}