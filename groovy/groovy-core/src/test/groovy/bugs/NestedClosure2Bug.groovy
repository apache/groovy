import org.codehaus.groovy.classgen.TestSupport

/**
 * @version $Revision$
 */
class NestedClosure2Bug extends TestSupport {
     
    void testBugOutsideOfScript() {
    	a = 123
    	b = 456
    	closure = { 
    		println b
    		return {
    			return { 
    				return a
    			}
    		}
    	}
    	c2 = closure()
    	c3 = c2()
    	value = c3()
    	
    	assert value == 123
    }
    
    void testBug() {
    	assertScript """
	    	a = 123
	    	closure = { 
	    		return {
	    			return { 
	    				return a
	    			}
	    		}
	    	}
	    	c2 = closure()
	    	c3 = c2()
	    	value = c3()
	    	
	    	assert value == 123
"""
	}
		
}