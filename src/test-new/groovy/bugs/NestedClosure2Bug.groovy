import org.codehaus.groovy.classgen.TestSupport

/**
 * @version $Revision$
 */
class NestedClosure2Bug extends TestSupport {
     
    Object f
     
    void testFieldBug() {
    	closure = {
    		return {
	    		f = 123
	    		return null
	        }
	    }
        value = closure()
        value = value()
        assert f == 123
    }
     
    void testBugOutsideOfScript() {
    	a = 123
    	b = 456
    	closure = { 
    		println b
    		c = 999
    		return {
    			f = 2222111
    			
    			println f
    			
    			println c
    			d = 678
    			return { 
    				println f
    				assert f == 2222111
    				println d
    				return a
    			}
    		}
    	}
    	c2 = closure()
    	c3 = c2()
    	value = c3()

		assert f == 2222111    	
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